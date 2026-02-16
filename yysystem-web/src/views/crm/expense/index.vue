<template>
  <div class="app-container">
    <h2 class="page-title">费用报销</h2>
    <div class="toolbar">
      <el-select v-model="selectedApplicantId" filterable clearable placeholder="选择申请人" style="width:300px" @change="fetchList">
        <el-option label="全部申请人" :value="undefined" />
        <el-option v-for="u in userOptions" :key="u.id" :label="u.name || u.realName || u.nickname || u.username" :value="u.id" />
      </el-select>
      <el-button @click="fetchList">刷新</el-button>
      <el-button v-if="canCreate" type="primary" @click="handleAdd">新增报销</el-button>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="applicantId" label="申请人" min-width="160">
        <template #default="scope">
          {{ applicantNameById(scope.row.applicantId) }}
        </template>
      </el-table-column>
      <el-table-column prop="applyTime" label="申请时间" width="160" />
      <el-table-column prop="totalAmount" label="合计金额" width="120" />
      <el-table-column prop="reimburseTime" label="报销时间" width="160" />
      <el-table-column prop="reimburseMethod" label="报销方式" width="120" />
      <el-table-column prop="applyRemark" label="申请备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="scope">
          <el-button v-if="canUpdate" link type="primary" :disabled="disableEdit(scope.row)" @click="handleEdit(scope.row)">修改</el-button>
          <el-button v-if="canDelete" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="innerVisible" :title="form.id ? '修改报销' : '新增报销'" width="820px" :before-close="onDialogBeforeClose">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="页面阶段">
          <el-radio-group v-model="currentStage">
            <el-radio-button label="apply">申请</el-radio-button>
            <el-radio-button label="finance">财务</el-radio-button>
            <el-radio-button label="receipt">回告</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="报销编码" prop="reimburseCode">
              <el-input v-model="form.reimburseCode" disabled placeholder="保存后自动生成" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申请时间" prop="applyTime">
              <el-date-picker v-model="form.applyTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="报销类型" prop="reimburseType">
              <el-select v-model="form.reimburseType" filterable clearable>
                <el-option v-for="t in reimburseTypes" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="报销单位" prop="reimburseUnit">
              <el-select 
                v-model="form.reimburseUnit" 
                filterable 
                clearable 
                style="width:100%"
                placeholder="请选择报销单位"
                @change="onReimburseUnitChange"
              >
                <el-option label="原邑智能科技（上海）有限公司" value="原邑智能科技（上海）有限公司" />
                <el-option label="安徽维斯顿智能科技有限公司" value="安徽维斯顿智能科技有限公司" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="申请人">
              <el-input :model-value="applicantNameById(form.applicantId)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门">
              <el-input :model-value="applicantDeptNameById(form.applicantId)" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="报销金额">
              <el-input :model-value="String(form.totalAmount || 0)" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="申请备注">
          <el-input v-model="form.applyRemark" type="textarea" :rows="2" />
        </el-form-item>

        <el-divider>报销明细</el-divider>
        <el-button size="small" @click="openAddDetail">新增明细</el-button>
        <el-table :data="details" border style="margin-top:8px;">
          <el-table-column label="费用归属" min-width="180">
            <template #default="scope">
              {{ detailOwnershipText(scope.row) }}
            </template>
          </el-table-column>
          <el-table-column label="费用分类" width="160">
            <template #default="scope">
              {{ scope.row.category }}
            </template>
          </el-table-column>
          <el-table-column label="摘要" min-width="220">
            <template #default="scope">
              {{ scope.row.content }}
            </template>
          </el-table-column>
          <el-table-column label="金额" width="120">
            <template #default="scope">
              {{ Number(scope.row.amount || 0).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="发票类型" width="160">
            <template #default="scope">
              {{ scope.row.invoiceType }}
            </template>
          </el-table-column>
          <el-table-column label="发票附件" min-width="160">
            <template #default="scope">
              <el-link 
                v-if="scope.row.invoiceAttachment" 
                :href="normalizeUrl(scope.row.invoiceAttachment)" 
                target="_blank"
                style="word-break: break-all; white-space: normal; line-height: 1.4;">
                {{ scope.row.invoiceAttachmentName || fileNameFromUrl(scope.row.invoiceAttachment) || '附件' }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button size="small" type="danger" link @click="removeDetail(scope.$index)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-dialog v-model="detailDialogVisible" title="新增报销明细" width="520px">
          <el-form :model="detailForm" label-width="120px">
            <el-form-item label="费用归属">
              <template v-if="form.reimburseType === '业务费用'">
                <el-select v-model="detailForm.customerId" filterable clearable style="width:100%;">
                  <el-option v-for="c in customers" :key="c.id" :label="c.customerName" :value="c.id" />
                </el-select>
              </template>
              <template v-else-if="form.reimburseType === '项目费用'">
                <el-select v-model="detailForm.contractName" filterable clearable style="width:100%;">
                  <el-option v-for="c in contractOptions" :key="c.id" :label="c.contractName || c.name || c.contractNo" :value="c.contractName || c.name || c.contractNo" />
                </el-select>
              </template>
              <template v-else>
                <el-input :model-value="'公司费用'" disabled />
              </template>
            </el-form-item>
            <el-form-item label="费用分类">
              <el-select v-model="detailForm.category" filterable clearable style="width:100%;">
                <el-option v-for="c in detailCategoryOptions" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
            <el-form-item label="摘要">
              <el-input v-model="detailForm.content" />
            </el-form-item>
            <el-form-item label="金额">
              <el-input-number v-model="detailForm.amount" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
            <el-form-item label="发票类型">
              <el-select v-model="detailForm.invoiceType" filterable clearable style="width:100%;">
                <el-option v-for="t in invoiceTypes" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
            <el-form-item label="发票附件">
              <div style="display:flex;align-items:flex-start;gap:8px;flex-wrap:wrap;">
                <el-upload
                  action="/api/common/upload"
                  :show-file-list="true"
                  :file-list="detailUploadFileList"
                  :headers="uploadHeaders"
                  :on-success="onDetailDialogUploadSuccess"
                  :on-remove="onDetailDialogRemove"
                  :limit="1"
                  :before-upload="beforeUploadInvoice"
                  accept=".pdf,.jpg">
                  <el-button size="small">上传</el-button>
                </el-upload>
              </div>
            </el-form-item>
          </el-form>
          <template #footer>
            <span style="display:flex;gap:10px;justify-content:flex-end;">
              <el-button @click="detailDialogVisible=false">取消</el-button>
              <el-button type="primary" @click="saveDetail">保存</el-button>
            </span>
          </template>
        </el-dialog>
        <el-divider>页面下部</el-divider>
        <el-row v-if="currentStage !== 'apply'" :gutter="16">
          <el-col :span="12">
            <el-form-item label="报销方式">
              <el-select v-model="form.reimburseMethod" filterable clearable>
                <el-option v-for="m in reimburseMethods" :key="m" :label="m" :value="m" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="报销时间">
              <el-date-picker v-model="form.reimburseTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="currentStage !== 'apply'" :gutter="16">
          <el-col :span="12">
            <el-form-item label="财务人员">
              <el-select v-model="form.financeUserId" filterable clearable>
                <el-option v-for="u in userOptions" :key="u.id" :label="u.name || u.realName || u.nickname || u.username" :value="u.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="currentStage === 'receipt'" :span="12">
            <el-form-item label="确认收款时间">
              <el-date-picker v-model="form.receiptConfirmTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer" style="display:flex; gap:10px;">
          <el-button @click="innerVisible = false">取消</el-button>
          <el-button v-if="canEditApply" type="primary" @click="handleSubmit">保存申请</el-button>
          <el-button v-if="form.id && canEditFinance" type="warning" @click="handleFinanceConfirm">财务确认报销</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed, nextTick } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { getCustomerList } from '@/api/customer'
import { getContractList } from '@/api/contract'
import { useUserStore } from '@/store/user'

const customers = ref<any[]>([])
const contractOptions = ref<any[]>([])
const userOptions = ref<any[]>([])
const usersById = reactive<Record<number, any>>({})
const selectedApplicantId = ref<number | undefined>(undefined)
const tableData = ref<any[]>([])
const loading = ref(false)
const innerVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<any>({
  id: undefined,
  applyTime: '',
  reimburseTime: '',
  reimburseMethod: '',
  applyRemark: '',
  receiptFeedback: '',
  totalAmount: 0,
  applicantId: undefined,
  reimburseCode: '',
  reimburseType: '',
  reimburseUnit: '',
  financeUserId: undefined,
  receiptConfirmTime: ''
})
const details = ref<any[]>([])
const rules = reactive<FormRules>({
  applyTime: [{ required: true, message: '请选择申请时间', trigger: 'change' }],
  reimburseType: [{ required: true, message: '请选择报销类型', trigger: 'change' }]
})
const uploadHeaders = reactive<Record<string, string>>({ Authorization: localStorage.getItem('token') ? ('Bearer ' + localStorage.getItem('token')) : '' })
const expenseCategories = ref<string[]>([])
const reimburseMethods = ref<string[]>([])
const invoiceTypes = ref<string[]>([])
const reimburseTypes = ref<string[]>([])
const businessExpenseCategories = ref<string[]>([])
const projectExpenseCategories = ref<string[]>([])
const dailyExpenseCategories = ref<string[]>([])
const userStore = useUserStore()
const ui = computed<any>(() => userStore.userInfo || {})
const isAdmin = computed(() => String(ui.value.userType || '').toLowerCase() === 'admin')
const isFinance = computed(() => String(ui.value.userType || '').toLowerCase() === 'finance')
const isOwner = computed(() => !!form.applicantId && form.applicantId === ui.value.id)
const canEditApply = computed(() => isAdmin.value || (!isFinance.value && (form.id ? isOwner.value : true)))
const canEditFinance = computed(() => isAdmin.value || isFinance.value)
const canEditReceipt = computed(() => isAdmin.value || isOwner.value)

const hasPermission = (permCode: string) => {
  const perms: string[] = ui.value?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canCreate = computed(() => hasPermission('ER:create'))
const canUpdate = computed(() => hasPermission('ER:update'))
const canDelete = computed(() => hasPermission('ER:delete'))

const currentStage = ref<'apply' | 'finance' | 'receipt'>('apply')
const detailDialogVisible = ref(false)
const detailUploadFileList = ref<any[]>([])
const detailForm = reactive<any>({
  customerId: undefined,
  contractName: '',
  category: '',
  content: '',
  amount: 0,
  invoiceType: '',
  invoiceAttachment: ''
})
const detailCategoryOptions = computed<string[]>(() => {
  const t = form.reimburseType
  if (t === '业务费用') return businessExpenseCategories.value.length ? businessExpenseCategories.value : expenseCategories.value
  if (t === '项目费用') return projectExpenseCategories.value.length ? projectExpenseCategories.value : expenseCategories.value
  if (t === '日常费用') return dailyExpenseCategories.value.length ? dailyExpenseCategories.value : expenseCategories.value
  return expenseCategories.value
})

const applicantNameById = (id?: number) => {
  const u = usersById[id || -1]
  return u?.name || u?.realName || u?.nickname || u?.username || ''
}
const applicantDeptNameById = (id?: number) => {
  const u = usersById[id || -1]
  return u?.deptName || u?.departmentName || u?.dept || ''
}
const customerNameById = (id?: number) => {
  const c = customers.value.find(x => x.id === id)
  return c?.customerName || ''
}
const detailOwnershipText = (row:any) => {
  if (form.reimburseType === '业务费用') return customerNameById(row.customerId)
  if (form.reimburseType === '项目费用') return row.contractName || ''
  return '公司费用'
}

// 报销单位与财务经理映射关系
const unitToFinanceManager: Record<string, number> = {
  '原邑智能科技（上海）有限公司': 2,  // 李珂
  '安徽维斯顿智能科技有限公司': 34    // 赵曼
}

// 根据员工ID获取默认报销单位
const getDefaultUnitByUser = (userId?: number): string => {
  const u = usersById[userId || -1]
  if (!u) return ''
  // 根据员工所属公司判断
  const deptName = u?.deptName || u?.departmentName || ''
  const companyName = u?.companyName || ''
  
  // 如果员工属于安徽维斯顿，返回安徽维斯顿
  if (deptName.includes('安徽') || companyName.includes('安徽') || deptName.includes('维斯顿') || companyName.includes('维斯顿')) {
    return '安徽维斯顿智能科技有限公司'
  }
  // 默认返回原邑智能
  return '原邑智能科技（上海）有限公司'
}

// 报销单位变更时自动分派财务经理
const onReimburseUnitChange = (unit: string) => {
  const financeManagerId = unitToFinanceManager[unit]
  if (financeManagerId) {
    form.financeUserId = financeManagerId
  }
}
const openAddDetail = async () => {
  detailForm.customerId = undefined
  detailForm.contractName = ''
  detailForm.category = ''
  detailForm.content = ''
  detailForm.amount = 0
  detailForm.invoiceType = ''
  detailForm.invoiceAttachment = ''
  detailForm.invoiceAttachmentName = ''
  detailUploadFileList.value = []
  if (form.reimburseType === '项目费用' && contractOptions.value.length === 0) {
    const res:any = await getContractList({ current: 1, size: 500 })
    const list = res?.records || []
    // 按合同名称拼音排序
    try {
      const collator = new Intl.Collator(['zh-Hans-u-co-pinyin', 'zh'], { usage: 'sort', sensitivity: 'base' })
      list.sort((a:any, b:any) => collator.compare(String(a.contractName || a.name || ''), String(b.contractName || b.name || '')))
    } catch {
      list.sort((a:any, b:any) => String(a.contractName || a.name || '').localeCompare(String(b.contractName || b.name || ''), 'zh'))
    }
    contractOptions.value = list
  }
  detailDialogVisible.value = true
}
const saveDetail = () => {
  const d:any = {
    customerId: form.reimburseType === '业务费用' ? detailForm.customerId : undefined,
    contractName: form.reimburseType === '项目费用' ? detailForm.contractName : undefined,
    category: detailForm.category,
    content: detailForm.content,
    amount: detailForm.amount,
    invoiceType: detailForm.invoiceType,
    invoiceAttachment: detailForm.invoiceAttachment,
    invoiceAttachmentName: detailForm.invoiceAttachmentName
  }
  details.value.push(d)
  detailDialogVisible.value = false
  calculateTotal()
}

const fetchConfig = async () => {
  const res:any = await request({ url: '/system/config/list', method: 'get', params: { size: 100 } })
  const list = res.records || res || []
  const general = list.find((item:any) => item.configKey === 'general_settings')
  let cats:string[] = []
  let methods:string[] = []
  let invs:string[] = []
  let rtypes:string[] = []
  let bexps:string[] = []
  let pexps:string[] = []
  let dexps:string[] = []
  if (general?.configValue) {
    try {
      const cfg = JSON.parse(general.configValue)
      cats = Array.isArray(cfg.expenseCategories) ? cfg.expenseCategories : []
      methods = Array.isArray(cfg.reimburseMethods) ? cfg.reimburseMethods : []
      invs = Array.isArray(cfg.invoiceTypes) ? cfg.invoiceTypes : []
      rtypes = Array.isArray(cfg.reimburseTypes) ? cfg.reimburseTypes : []
      const custom = cfg.customCategories || {}
      if (cats.length === 0) {
        const k = Object.keys(custom).find(x => x.includes('费用分类'))
        cats = k ? (custom[k] || []) : []
      }
      if (methods.length === 0) {
        const k = Object.keys(custom).find(x => x.includes('报销方式'))
        methods = k ? (custom[k] || []) : []
      }
      if (invs.length === 0) {
        const k = Object.keys(custom).find(x => x.includes('发票类型') || x.includes('发票分类'))
        invs = k ? (custom[k] || []) : []
      }
      if (rtypes.length === 0) {
        const k = Object.keys(custom).find(x => x.includes('报销类型'))
        rtypes = k ? (custom[k] || []) : []
      }
      const kb = Object.keys(custom).find(x => x.includes('业务费用'))
      const kp = Object.keys(custom).find(x => x.includes('项目费用'))
      const kd = Object.keys(custom).find(x => x.includes('日常费用'))
      bexps = kb ? (custom[kb] || []) : []
      pexps = kp ? (custom[kp] || []) : []
      dexps = kd ? (custom[kd] || []) : []
    } catch {}
  }
  expenseCategories.value = cats
  reimburseMethods.value = methods
  invoiceTypes.value = invs
  reimburseTypes.value = rtypes.length ? rtypes : ['业务费用', '项目费用', '日常费用']
  businessExpenseCategories.value = bexps
  projectExpenseCategories.value = pexps
  dailyExpenseCategories.value = dexps
}

const fetchCustomers = async () => {
  const res:any = await getCustomerList({ current: 1, size: 1000 })
  const list = res.records || []
  // 按客户名称拼音排序
  try {
    const collator = new Intl.Collator(['zh-Hans-u-co-pinyin', 'zh'], { usage: 'sort', sensitivity: 'base' })
    list.sort((a:any, b:any) => collator.compare(String(a.customerName || ''), String(b.customerName || '')))
  } catch {
    list.sort((a:any, b:any) => String(a.customerName || '').localeCompare(String(b.customerName || ''), 'zh'))
  }
  customers.value = list
}
const fetchUsers = async () => {
  const res:any = await request({ url: '/system/user/list', method: 'get', params: { size: 1000 } })
  const userList:any[] = res?.records || (Array.isArray(res) ? res : []) || []
  userOptions.value = userList
  ;(userOptions.value as any[]).forEach((u:any) => { if (u && u.id != null) usersById[u.id] = u })
}
const fetchList = async () => {
  loading.value = true
  try {
    const params:any = { current: 1, size: 50 }
    if (selectedApplicantId.value) params.applicantId = selectedApplicantId.value
    const res:any = await request({ url: '/crm/expense/list', method: 'get', params })
    let list = res.records || []
    
    // 财务经理只能看到属于自己公司的报销单
    if (isFinance.value && !isAdmin.value) {
      const currentUserId = ui.value?.id
      // 财务经理与报销单位映射关系
      const unitToFinanceManager: Record<string, number> = {
        '原邑智能科技（上海）有限公司': 2,  // 李珂
        '安徽维斯顿智能科技有限公司': 34    // 赵曼
      }
      
      list = list.filter((x: any) => {
        // 如果没有报销单位信息，显示给所有财务经理
        if (!x.reimburseUnit) return true
        
        // 只显示分配给当前财务经理的报销单
        const assignedManagerId = unitToFinanceManager[x.reimburseUnit]
        return assignedManagerId === currentUserId
      })
    }
    
    tableData.value = list
  } finally {
    loading.value = false
  }
}
const disableEdit = (row:any) => {
  if (isAdmin.value || isFinance.value) return false
  const isApplicant = !!row?.applicantId && (row.applicantId === (ui.value?.id))
  const submitted = !!row?.id
  return isApplicant && submitted
}
const financeReminded = ref(false)
const handleAdd = () => {
  form.id = undefined
  form.applicantId = selectedApplicantId.value || ui.value.id || undefined
  form.applyTime = formatNow()
  form.reimburseTime = ''
  form.reimburseMethod = isFinance.value ? (reimburseMethods.value[0] || '') : ''
  form.applyRemark = ''
  form.totalAmount = 0
  form.reimburseCode = ''
  form.reimburseType = ''
  // 根据申请人自动设置报销单位和财务经理
  const applicantId = form.applicantId
  const defaultUnit = getDefaultUnitByUser(applicantId)
  form.reimburseUnit = defaultUnit
  form.financeUserId = unitToFinanceManager[defaultUnit] || (isFinance.value ? ui.value.id : undefined)
  form.receiptConfirmTime = ''
  details.value = []
  innerVisible.value = true
  currentStage.value = 'apply'
  nextTick(() => { originalSnapshot.value = JSON.parse(serializeSnapshot()) })
}
const handleEdit = (row:any) => {
  Object.assign(form, row)
  details.value = []
  try {
    const ext = row.extendFields ? JSON.parse(row.extendFields) : {}
    if (Array.isArray(ext.details)) {
      details.value = (ext.details as any[]).map((d:any) => ({
        ...d,
        invoiceAttachmentName: d?.invoiceAttachmentName || d?.originalName || fileNameFromUrl(d?.invoiceAttachment)
      }))
    }
  } catch {}
  innerVisible.value = true
  if (canEditFinance.value) {
    currentStage.value = 'finance'
    if (isFinance.value) {
      form.financeUserId = ui.value.id
      if (!form.reimburseMethod) form.reimburseMethod = reimburseMethods.value[0] || ''
    }
  } else if (canEditReceipt.value) {
    currentStage.value = 'receipt'
  } else {
    currentStage.value = 'apply'
  }
  nextTick(() => { originalSnapshot.value = JSON.parse(serializeSnapshot()) })
}
const handleDelete = (row:any) => {
  ElMessageBox.confirm('确认删除该报销记录吗？', '提示', { type: 'warning' }).then(async () => {
    await request({ url: `/crm/expense/${row.id}`, method: 'delete' })
    ElMessage.success('删除成功')
    fetchList()
  })
}
const removeDetail = (idx:number) => {
  details.value.splice(idx, 1)
  calculateTotal()
}
const calculateTotal = () => {
  form.totalAmount = details.value.reduce((sum:number, d:any) => sum + Number(d.amount || 0), 0)
}


const onDetailDialogUploadSuccess = (res:any, file:any) => {
  const url = res?.data
  if (url) {
    detailForm.invoiceAttachment = url
    detailForm.invoiceAttachmentName = file?.name || ''
    detailUploadFileList.value = [{ name: file?.name || '附件', url: normalizeUrl(url) }]
  }
}
const onDetailDialogRemove = () => {
  detailForm.invoiceAttachment = ''
  detailForm.invoiceAttachmentName = ''
  detailUploadFileList.value = []
}
const beforeUploadInvoice = (file: File) => {
  const isAllowedType = ['application/pdf', 'image/jpeg'].includes(file.type)
  const isLt100M = file.size / 1024 / 1024 < 100
  if (!isAllowedType) { ElMessage.error('只能上传PDF或JPG文件'); return false }
  if (!isLt100M) { ElMessage.error('文件大小不能超过100MB'); return false }
  return true
}
const normalizeUrl = (u?: string) => {
  if (!u) return ''
  if (u.startsWith('http')) return u
  const path = u.startsWith('/files/') ? u : (`/files/${u.replace(/^\/+/, '')}`)
  return `${window.location.origin}${path}`
}
const fileNameFromUrl = (u?: string) => {
  if (!u) return ''
  try {
    const p = u.split('?')[0]
    const segs = p.split('/')
    const last = segs[segs.length - 1] || ''
    return decodeURIComponent(last)
  } catch {
    return ''
  }
}

const originalSnapshot = ref<any>({})
const serializeSnapshot = () => {
  return JSON.stringify({
    form,
    details: details.value
  })
}
const isChanged = () => {
  const a = serializeSnapshot()
  const b = JSON.stringify(originalSnapshot.value || {})
  return a !== b
}
const onDialogBeforeClose = async (done: () => void) => {
  if (!isChanged()) {
    done()
    return
  }
  try {
    await ElMessageBox.confirm('内容已修改，是否保存？', '提示', { confirmButtonText: '保存并关闭', cancelButtonText: '直接关闭' })
    await handleSubmit()
    done()
  } catch {
    done()
  }
}
const beforeUnloadHandler = (e: BeforeUnloadEvent) => {
  if (innerVisible.value && isChanged()) {
    e.preventDefault()
    e.returnValue = ''
  }
}
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      calculateTotal()
      const payload:any = { ...form }
      payload.extendFields = JSON.stringify({ details: details.value })
      if (form.id) {
        await request({ url: `/crm/expense/${form.id}`, method: 'put', data: payload })
        ElMessage.success('更新成功')
      } else {
        await request({ url: '/crm/expense', method: 'post', data: payload })
        ElMessage.success('创建成功')
      }
      innerVisible.value = false
      fetchList()
    }
  })
}
const handleFinanceConfirm = async () => {
  if (!form.id) return
  if (!String(form.reimburseMethod || '').trim()) {
    ElMessage.warning('请填写报销方式')
    return
  }
  await request({ url: `/crm/expense/${form.id}/finance`, method: 'put', data: { reimburseTime: form.reimburseTime, reimburseMethod: form.reimburseMethod, financeUserId: form.financeUserId } })
  ElMessage.success('财务确认成功')
  innerVisible.value = false
  fetchList()
}

const formatNow = () => {
  const p = (n:number) => n < 10 ? ('0' + n) : String(n)
  const d = new Date()
  const Y = d.getFullYear()
  const M = p(d.getMonth() + 1)
  const D = p(d.getDate())
  const h = p(d.getHours())
  const m = p(d.getMinutes())
  const s = p(d.getSeconds())
  return `${Y}-${M}-${D} ${h}:${m}:${s}`
}
onMounted(async () => {
  await fetchCustomers()
  await fetchUsers()
  await fetchConfig()
  await fetchList()
  window.addEventListener('beforeunload', beforeUnloadHandler)
  if (isFinance.value && !financeReminded.value) {
    const pendingCount = (tableData.value || []).filter((x:any) => String(x.auditStatus || 'PENDING') === 'PENDING').length
    ElNotification({
      title: '待处理提醒',
      message: `您有 ${pendingCount} 条报销申请待处理`,
      type: pendingCount > 0 ? 'warning' : 'info',
      duration: 5000
    })
    financeReminded.value = true
  }
})
onUnmounted(() => {
  window.removeEventListener('beforeunload', beforeUnloadHandler)
})
</script>

<style scoped>
.app-container { padding: 20px; }
.toolbar { display:flex; gap:10px; align-items:center; margin-bottom: 12px; }
</style>
