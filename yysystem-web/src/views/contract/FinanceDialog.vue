<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="550px" :before-close="onBeforeClose">
    <el-form :model="form" label-width="100px">
      <el-form-item :label="props.type === 'OUT' ? '累计支付金额' : '应收款金额'">
        <el-tag type="warning">{{ formatCurrency(displayAmount) }}</el-tag>
      </el-form-item>
      <el-form-item label="公司名称" required>
        <el-select v-model="form.companyId" placeholder="请选择公司" style="width:100%" @change="handleCompanyChange">
          <el-option v-for="c in companyOptions" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item :label="props.type === 'OUT' ? '支出金额' : '收款金额'" required>
        <el-input-number v-model="form.amount" :precision="2" :step="100" />
      </el-form-item>
      <el-form-item label="收支分类">
        <el-select v-model="form.category" placeholder="请选择分类" filterable style="width:100%" @change="handleCategoryChange">
          <el-option v-for="c in financeCategories" :key="c" :label="c" :value="c" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="isDepositCategory" label="保证金退回日期" required>
        <el-date-picker v-model="form.refundDueDate" type="date" value-format="YYYY-MM-DD" style="width: 100%;" placeholder="请选择退回日期" />
      </el-form-item>
      <el-form-item label="备注" required>
        <el-input v-model="form.remark" type="textarea" placeholder="请必须填写收支说明" />
      </el-form-item>
    </el-form>
    <el-divider content-position="left">历史记录</el-divider>
    <el-table :data="historyList" size="small" border height="240" v-loading="historyLoading">
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column prop="arrivalTime" label="到账时间" width="160" />
      <el-table-column prop="category" label="分类" width="120" show-overflow-tooltip />
      <el-table-column label="金额" width="120" align="right">
        <template #default="scope">
          {{ formatCurrency(Number(scope.row.arrivalAmount || scope.row.amount || 0)) }}
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getAuditStatusType(scope.row.auditStatus)" size="small">
            {{ getAuditStatusText(scope.row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <span class="dialog-footer">
        <el-button :disabled="submitting" @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" :disabled="submitting" @click="handleSubmit">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, computed, watch, ref, onMounted, onUnmounted } from 'vue'
import { saveFinance, getFinanceById, updateFinance, auditFinance, getFinanceList } from '@/api/finance'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { getContractById } from '@/api/contract'
import { useUserStore } from '@/store/user'
import { getCustomerList } from '@/api/customer'

const props = defineProps<{
  modelValue: boolean
  contractId?: number
  customerId?: number
  type?: 'IN' | 'OUT'
  financeId?: number
}>()

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const form = reactive<any>({
  contractId: undefined,
  customerId: undefined,
  type: 'IN',
  amount: 0,
  remark: '',
  category: '',
  auditStatus: undefined,
  extendFields: '',
  companyId: undefined,
  companyName: '',
  refundDueDate: ''
})

// 判断是否为保证金支出分类
const isDepositCategory = computed(() => {
  const category = form.category || ''
  return category.includes('保证金')
})

// 分类变化处理
const handleCategoryChange = () => {
  // 如果不是保证金分类，清空退回日期
  if (!isDepositCategory.value) {
    form.refundDueDate = ''
  }
}

const receivable = ref<number>(0)
const totalExpenditure = ref<number>(0)
const displayAmount = computed(() => (props.type === 'OUT' ? totalExpenditure.value : receivable.value))
const formatCurrency = (value: number | undefined) => {
  if (value === undefined || value === null) return '¥0.00'
  return `¥${Number(value).toFixed(2)}`
}

const getAuditStatusText = (status: string | undefined) => {
  if (status === 'PASSED') return '已通过'
  if (status === 'REJECTED') return '已驳回'
  if (status === 'PENDING') return '待审核'
  return status || '未知'
}

const getAuditStatusType = (status: string | undefined) => {
  if (status === 'PASSED') return 'success'
  if (status === 'REJECTED') return 'danger'
  if (status === 'PENDING') return 'warning'
  return 'info'
}

const submitting = ref(false)
const clientRequestId = ref<string>('')
const historyList = ref<any[]>([])
const historyLoading = ref(false)

const newClientRequestId = () => {
  try {
    // @ts-ignore
    if (typeof crypto !== 'undefined' && crypto?.randomUUID) return crypto.randomUUID()
  } catch {}
  return `${Date.now().toString(36)}_${Math.random().toString(36).slice(2, 10)}`
}
const ensureClientRequestId = () => {
  if (!clientRequestId.value) clientRequestId.value = newClientRequestId()
  let ext: any = {}
  try {
    if (form.extendFields) {
      ext = typeof form.extendFields === 'string' ? JSON.parse(form.extendFields) : (form.extendFields || {})
    }
  } catch {
    ext = {}
  }
  ext.clientRequestId = clientRequestId.value
  form.extendFields = JSON.stringify(ext)
}
const financeCategories = ref<string[]>([])

// 公司选项（从客户表加载）
const companyOptions = ref<{id: number, name: string}[]>([])

// 加载公司列表（从客户表获取）
const loadCompanyOptions = async () => {
  try {
    const res: any = await getCustomerList({ current: 1, size: 1000 })
    const list = res.records || []
    // 过滤出公司：原邑智能、安徽维斯顿、原邑信息科技
    companyOptions.value = list
      .filter((item: any) => {
        const name = item.customerName || ''
        return name.includes('原邑智能') || name.includes('维斯顿') || name.includes('原邑信息科技')
      })
      .map((item: any) => ({
        id: item.id,
        name: item.customerName
      }))
  } catch {
    companyOptions.value = []
  }
}

// 获取用户所属公司默认值
const userStore = useUserStore()
const getDefaultCompany = () => {
  const userInfo = userStore.userInfo || {}
  // 如果用户有companyId，使用用户的公司从客户表查找
  if (userInfo.companyId) {
    const company = companyOptions.value.find(c => c.id === userInfo.companyId)
    if (company) {
      return { id: company.id, name: company.name }
    }
  }
  // 默认返回第一个公司
  if (companyOptions.value.length > 0) {
    return { id: companyOptions.value[0].id, name: companyOptions.value[0].name }
  }
  return { id: undefined, name: '' }
}

// 公司选择变化处理
const handleCompanyChange = (companyId: number) => {
  const company = companyOptions.value.find(c => c.id === companyId)
  form.companyName = company ? company.name : ''
}
const loadFinanceCategories = async () => {
  try {
    const res:any = await request({ url: '/system/config/list', method: 'get', params: { size: 100 } })
    const general = (res.records || res || []).find((item: any) => item.configKey === 'general_settings')
    const cfg = general?.configValue ? JSON.parse(general.configValue) : {}
    const groups = cfg?.customCategories || {}
    const pick = (props.type === 'OUT' ? (groups['支出分类'] || []) : (groups['收款分类'] || []))
    financeCategories.value = (Array.isArray(pick) && pick.length > 0) ? pick : (groups['收支分类'] || [])
  } catch {}
}

const loadHistory = async () => {
  if (!props.contractId) {
    historyList.value = []
    return
  }
  historyLoading.value = true
  try {
    const res: any = await getFinanceList({ current: 1, size: 1000, contractId: props.contractId, type: props.type })
    const list = (res?.records || res || []) as any[]
    historyList.value = list
  } catch {
    historyList.value = []
  } finally {
    historyLoading.value = false
  }
}

const dialogTitle = computed(() => {
  if (props.financeId) return props.type === 'OUT' ? '修改支出' : '修改收款'
  return props.type === 'OUT' ? '新增支出' : '新增收款'
})

const originalForm = ref<any>({})
watch(() => props.modelValue, (val) => {
  if (val) {
    originalForm.value = JSON.parse(JSON.stringify(form))
  }
})
const isChanged = () => {
  return JSON.stringify(form) !== JSON.stringify(originalForm.value || {})
}
const onBeforeClose = async (done: () => void) => {
  if (!isChanged()) {
    done()
    return
  }
  try {
    await ElMessageBox.confirm('内容已修改，是否保存？', '提示', { confirmButtonText: '保存并关闭', cancelButtonText: '直接关闭' })
    const saved = await handleSubmit()
    if (saved) done()
  } catch {
    done()
  }
}
const beforeUnloadHandler = (e: BeforeUnloadEvent) => {
  if (visible.value && isChanged()) {
    e.preventDefault()
    e.returnValue = ''
  }
}
watch(() => props.contractId, async (val) => {
  if (val) {
    form.contractId = val
    form.customerId = props.customerId
    form.type = props.type || 'IN'
    form.amount = 0
    form.remark = ''
    form.category = ''
    form.extendFields = ''
    // 先确保公司列表已加载，再设置默认公司
    if (companyOptions.value.length === 0) {
      await loadCompanyOptions()
    }
    // 设置默认公司
    const defaultCompany = getDefaultCompany()
    form.companyId = defaultCompany.id
    form.companyName = defaultCompany.name
    clientRequestId.value = props.financeId ? '' : newClientRequestId()
    getContractById(val).then((res:any) => {
      receivable.value = Number(res.arrears || 0)
      totalExpenditure.value = Number(res.totalExpenditure || 0)
      // 新增收款时，默认将收款金额设置为应收款金额
      if (!props.financeId && props.type !== 'OUT') {
        form.amount = Number(res.arrears || 0)
      }
    })
    loadFinanceCategories()
    loadHistory()
  }
})

watch(() => props.financeId, async (val) => {
  if (val) {
    // 先确保公司列表已加载
    if (companyOptions.value.length === 0) {
      await loadCompanyOptions()
    }
    const fin:any = await getFinanceById(val)
    form.contractId = fin.contractId
    form.customerId = fin.customerId
    form.type = fin.type || (props.type || 'IN')
    form.amount = Number(fin.amount || fin.arrivalAmount || 0)
    form.remark = fin.remark || ''
    form.category = fin.category || ''
    form.auditStatus = fin.auditStatus || 'PENDING'
    // 设置公司信息（优先使用已有记录的公司，否则使用默认值）
    if (fin.companyId) {
      form.companyId = fin.companyId
      form.companyName = fin.companyName || ''
    } else {
      const defaultCompany = getDefaultCompany()
      form.companyId = defaultCompany.id
      form.companyName = defaultCompany.name
    }
    if (fin.extendFields) {
      form.extendFields = typeof fin.extendFields === 'string' ? fin.extendFields : JSON.stringify(fin.extendFields)
    } else {
      form.extendFields = ''
    }
    clientRequestId.value = ''
    loadHistory()
  }
})

watch(() => props.type, () => {
  loadFinanceCategories()
  loadHistory()
})

const handleSubmit = async () => {
  if (submitting.value) return false
  try {
    if (!String(form.remark || '').trim()) {
      ElMessage.error('请必须填写收支说明')
      return false
    }
    // 保证金支出必须填写退回日期
    if (isDepositCategory.value && !form.refundDueDate) {
      ElMessage.error('保证金支出必须填写保证金退回日期')
      return false
    }
    if (!props.financeId) {
      ensureClientRequestId()
    }
    submitting.value = true
    let ok:any
    if (props.financeId) {
      form.auditStatus = 'PENDING'
      ok = await updateFinance(props.financeId, form)
      if (ok) {
        await auditFinance(props.financeId, 'PENDING')
      }
    } else {
      ok = await saveFinance(form)
    }
    if (ok) ElMessage.success(props.financeId ? '收支记录已更新' : '收支记录已保存')
    visible.value = false
    emit('success')
    loadHistory()
    return true
  } catch (e) {
    // handled
    return false
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadFinanceCategories()
  loadCompanyOptions()
  loadHistory()
  window.addEventListener('beforeunload', beforeUnloadHandler)
})
onUnmounted(() => {
  window.removeEventListener('beforeunload', beforeUnloadHandler)
})
</script>
