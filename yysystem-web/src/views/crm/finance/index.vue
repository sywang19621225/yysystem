<template>
  <div class="app-container">
    <h2 class="page-title">销售收支管理</h2>
    
    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 20px; font-weight: bold; color: #409EFF;">{{ formatCurrency(statistics.totalAmount) }}</div>
            <div style="color: #909399; margin-top: 8px; font-size: 12px;">收支总额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 20px; font-weight: bold; color: #67C23A;">{{ formatCurrency(statistics.totalIncome) }}</div>
            <div style="color: #909399; margin-top: 8px; font-size: 12px;">收款总额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 20px; font-weight: bold; color: #E6A23C;">{{ formatCurrency(statistics.totalExpense) }}</div>
            <div style="color: #909399; margin-top: 8px; font-size: 12px;">支出总额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 20px; font-weight: bold; color: #F56C6C;">{{ statistics.pendingCount || 0 }}</div>
            <div style="color: #909399; margin-top: 8px; font-size: 12px;">待处理笔数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索栏 -->
    <div class="toolbar">
      <el-input v-model="queryParams.customerName" placeholder="搜索客户名称" style="width: 200px; margin-right: 10px;" clearable @keyup.enter="handleQuery" />
      <el-select v-model="queryParams.type" placeholder="类型" clearable style="width: 120px; margin-right: 10px;">
        <el-option label="收款" value="IN" />
        <el-option label="支出" value="OUT" />
      </el-select>
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
      <el-button type="success" @click="handleExport">导出XLSX</el-button>
    </div>

    <el-table :data="filteredData" border stripe v-loading="loading" style="margin-top: 16px;">
      <el-table-column type="index" width="50" align="center" />
      <el-table-column label="类型" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.type === 'IN' ? 'success' : 'danger'">{{ scope.row.type === 'IN' ? '收款' : '支出' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="合同单号" min-width="150">
        <template #default="scope">
          <el-link type="primary" @click="viewContract(scope.row.contractId)">{{ getContractNo(scope.row.contractId) || '-' }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="客户名称" min-width="180" show-overflow-tooltip>
        <template #default="scope">
          {{ getCustomerName(scope.row.customerId) || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="companyName" label="公司名称" min-width="180" show-overflow-tooltip />
      <el-table-column label="居间费" width="120" align="right">
        <template #default="scope">
          <span :class="{ 'text-warning': getContractIntermediaryFee(scope.row.contractId) > 0 }">
            ¥{{ getContractIntermediaryFee(scope.row.contractId)?.toFixed(2) || '0.00' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="居间费状态" width="100" align="center">
        <template #default="scope">
          <el-tag v-if="getContractIntermediaryFee(scope.row.contractId) > 0" 
                  :type="getContractIntermediaryStatus(scope.row.contractId) === 'PAID' ? 'success' : 'warning'"
                  size="small">
            {{ getContractIntermediaryStatus(scope.row.contractId) === 'PAID' ? '已付' : '未付' }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="收支分类" width="140" align="center" />
      <el-table-column prop="amount" label="金额" width="140" align="right">
        <template #default="scope">
          ¥{{ scope.row.amount?.toFixed(2) || '0.00' }}
        </template>
      </el-table-column>
      <el-table-column prop="arrivalAmount" label="到账金额" width="140" align="right">
        <template #default="scope">
          <span :class="{ 'text-success': scope.row.arrivalAmount }">
            ¥{{ scope.row.arrivalAmount?.toFixed(2) || '0.00' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="arrivalTime" label="到账时间" width="160" align="center">
        <template #default="scope">
          {{ formatDate(scope.row.arrivalTime) || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="invoiceNo" label="发票号码" width="140" align="center" />
      <el-table-column prop="paymentVoucher" label="到账凭证" width="100" align="center">
        <template #default="scope">
          <div v-if="scope.row.paymentVoucher">
            <el-popover placement="top" :width="260" trigger="hover">
              <template #reference>
                <el-button link type="primary">查看</el-button>
              </template>
              <div v-for="(u,idx) in parseVouchers(scope.row.paymentVoucher)" :key="idx" style="margin-bottom:6px;">
                <template v-if="isImage(u)">
                  <el-image :src="normalizeUrl(u)" :preview-src-list="parseVouchers(scope.row.paymentVoucher).filter(isImage).map(normalizeUrl)" style="width: 80px; height: 80px; object-fit: cover;" />
                </template>
                <template v-else>
                  <a :href="normalizeUrl(u)" target="_blank">打开文件</a>
                </template>
              </div>
            </el-popover>
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button 
            link 
            :type="scope.row.auditStatus === 'PENDING' && !scope.row.arrivalTime ? 'success' : 'primary'" 
            @click="handleArrival(scope.row)"
          >
            {{ scope.row.auditStatus === 'PENDING' && !scope.row.arrivalTime ? '收支' : '查看' }}
          </el-button>
          <!-- 货款转保证金的记录不允许编辑 -->
          <el-button v-if="scope.row.depositSourceType !== 'CONVERT' && canUpdate" link type="warning" :disabled="!!scope.row.arrivalTime || scope.row.auditStatus === 'PASSED'" @click="handleEdit(scope.row)">修改</el-button>
          <el-button v-if="scope.row.depositSourceType !== 'CONVERT' && canDelete" link type="danger" :disabled="!!scope.row.arrivalTime || scope.row.auditStatus === 'PASSED'" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>
    <el-dialog v-model="arrivalDialogVisible" :title="arrivalForm.type === 'OUT' ? '财务支出' : '财务到账'" width="500px" :before-close="onArrivalBeforeClose">
      <el-form :model="arrivalForm" label-width="100px">
        <el-form-item label="类型">
          <el-tag :type="arrivalForm.type === 'IN' ? 'success' : 'danger'">{{ arrivalForm.type === 'IN' ? '收款' : '支出' }}</el-tag>
        </el-form-item>
        <el-form-item label="公司名称">
          <el-input v-model="arrivalForm.companyName" disabled style="width:100%" />
        </el-form-item>
        <el-form-item label="金额">
          <el-tag type="info">{{ formatCurrency(arrivalForm.amount) }}</el-tag>
        </el-form-item>
        <el-form-item :label="arrivalForm.type === 'OUT' ? '支出金额' : '到账金额'">
          <el-input-number v-model="arrivalForm.arrivalAmount" :precision="2" :step="100" style="width:100%" />
        </el-form-item>
        <el-form-item :label="arrivalForm.type === 'OUT' ? '支出时间' : '到账时间'">
          <el-date-picker v-model="arrivalForm.arrivalTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="发票号码" v-if="arrivalForm.type === 'IN'">
          <el-select v-model="arrivalForm.invoiceNo" filterable allow-create default-first-option placeholder="选择或输入发票号码" style="width:100%">
            <el-option v-for="n in invoiceOptions" :key="n" :label="n" :value="n" />
          </el-select>
        </el-form-item>
        <el-form-item :label="arrivalForm.type === 'OUT' ? '支出凭证' : '到账凭证'">
          <el-upload
            action="/api/common/upload"
            :headers="uploadHeaders"
            list-type="picture-card"
            :file-list="arrivalVoucherList"
            :on-success="onVoucherSuccess"
            :on-remove="onVoucherRemove"
            :on-preview="onVoucherPreview"
            :limit="10"
            multiple
            accept="image/*"
          >
            <el-icon><i class="el-icon-plus"></i></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="备注" required>
          <el-input v-model="arrivalForm.collectionDetail" type="textarea" placeholder="请必须填写收支说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="arrivalDialogVisible=false">取消</el-button>
        <el-button v-if="!arrivalInitialArrived" type="danger" @click="returnFinance">退回</el-button>
        <el-button type="primary" :disabled="arrivalMismatch" @click="submitArrival">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="修改收支记录" width="500px" :before-close="onEditBeforeClose">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="类型">
          <el-select v-model="editForm.type" style="width:100%">
            <el-option label="支出" value="OUT" />
          </el-select>
        </el-form-item>
        <el-form-item label="公司名称">
          <el-input v-model="editForm.companyName" disabled style="width:100%" />
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="editForm.amount" :precision="2" :step="100" style="width:100%" />
        </el-form-item>
        <el-form-item :label="editForm.type === 'OUT' ? '支出金额' : '到账金额'">
          <el-input-number v-model="editForm.arrivalAmount" :precision="2" :step="100" style="width:100%" />
        </el-form-item>
        <el-form-item :label="editForm.type === 'OUT' ? '支出时间' : '到账时间'">
          <el-date-picker v-model="editForm.arrivalTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item :label="editForm.type === 'OUT' ? '支出凭证' : '到账凭证'">
          <el-upload
            action="/api/common/upload"
            :headers="uploadHeaders"
            list-type="picture-card"
            :file-list="editVoucherList"
            :on-success="onEditVoucherSuccess"
            :on-remove="onEditVoucherRemove"
            :on-preview="onEditVoucherPreview"
            :limit="10"
            multiple
            accept="image/*"
          >
            <el-icon><i class="el-icon-plus"></i></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="备注" required>
          <el-input v-model="editForm.remark" type="textarea" placeholder="请必须填写收支说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible=false">取消</el-button>
        <el-button type="primary" :disabled="editMismatch" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getFinanceList, deleteFinance, updateFinance, auditFinance } from '@/api/finance'
import { getInvoiceList } from '@/api/invoice'
import { getCustomerList } from '@/api/customer'
import { getContractList } from '@/api/contract'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

// 上传请求头（包含认证token）
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    Authorization: token ? `Bearer ${token}` : ''
  }
})

const canCreate = computed(() => hasPermission('IER:create'))
const canUpdate = computed(() => hasPermission('IER:update'))
const canDelete = computed(() => hasPermission('IER:delete'))

const tableData = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statistics = ref<any>({})

const queryParams = reactive({
  type: '' as '' | 'IN' | 'OUT',
  customerName: '',
  contractId: undefined as number | undefined,
  auditStatus: ''
})

const customers = ref<any[]>([])
const contracts = ref<any[]>([])
const categories = ref<string[]>([])
const route = useRoute()
const router = useRouter()
const routeContractId = ref<string | undefined>(undefined)
const routeType = ref<string | undefined>(undefined)
const routeOpenEdit = ref<string | undefined>(undefined)

const fetchCustomers = async () => {
  const res:any = await getCustomerList({ current: 1, size: 1000 })
  customers.value = res.records || []
}
const fetchContracts = async () => {
  const res:any = await getContractList({ current: 1, size: 1000 })
  contracts.value = res.records || []
}
const fetchCategories = async () => {
  try {
    const res:any = await request({ url: '/system/config/list', method: 'get', params: { size: 100 } })
    const general = (res.records || res || []).find((item: any) => item.configKey === 'general_settings')
    const cfg = general?.configValue ? JSON.parse(general.configValue) : {}
    const groups = cfg?.customCategories || {}
    categories.value = groups['收支分类'] || []
  } catch {}
}

const getCustomerName = (id?: number) => {
  if (!id) return ''
  const c = customers.value.find((x:any) => x.id === id)
  return c?.customerName || id
}
const getContractNo = (id?: number) => {
  if (!id) return ''
  const c = contracts.value.find((x:any) => x.id === id)
  return c?.contractNo || id
}

const getContractIntermediaryFee = (id?: number) => {
  if (!id) return 0
  const c = contracts.value.find((x:any) => x.id === id)
  return Number(c?.intermediaryFee || 0)
}

const getContractIntermediaryStatus = (id?: number) => {
  if (!id) return ''
  const c = contracts.value.find((x:any) => x.id === id)
  return String(c?.intermediaryFeeStatus || 'UNPAID')
}

const viewContract = (contractId: number) => {
  if (contractId) {
    router.push(`/crm/contract`)
  }
}

const formatCurrency = (value: number | string | undefined | null) => {
  const n = Number(value ?? 0)
  return `¥${n.toFixed(2)}`
}
const formatDate = (val: string | undefined) => {
  if (!val) return ''
  return String(val).replace('T', ' ')
}

const fetchData = async () => {
  loading.value = true
  try {
    const params: any = {
      current: currentPage.value,
      size: pageSize.value
    }
    // 客户名称后端模糊查询
    if (queryParams.customerName) {
      params.customerName = queryParams.customerName
    }
    // 类型筛选
    if (queryParams.type) {
      params.type = queryParams.type
    }
    const res:any = await getFinanceList(params)
    tableData.value = res.records || []
    total.value = res.total || 0
    // 确保合同列表已加载
    if (contracts.value.length === 0) {
      await fetchContracts()
    }
    // 获取后端统计数据（按全部数据计算）
    await fetchStatistics()
  } finally {
    loading.value = false
  }
}

// 获取后端统计数据
const fetchStatistics = async () => {
  try {
    const params: any = {}
    if (queryParams.contractId) {
      params.contractId = queryParams.contractId
    }
    if (queryParams.customerName) {
      params.customerName = queryParams.customerName
    }
    if (queryParams.type) {
      params.type = queryParams.type
    }
    if (queryParams.auditStatus) {
      params.auditStatus = queryParams.auditStatus
    }
    
    const res: any = await request({
      url: '/crm/finance/statistics',
      method: 'get',
      params
    })
    
    if (res) {
      statistics.value.totalIncome = res.totalIncome || 0
      statistics.value.totalExpense = res.totalExpense || 0
      statistics.value.pendingCount = res.pendingCount || 0
      // 收支总额 = 收款总额 - 支出总额
      statistics.value.totalAmount = (res.totalIncome || 0) - (res.totalExpense || 0)
    }
  } catch (e) {
    console.error('获取统计数据失败', e)
  }
}

const filteredData = computed(() => {
  let rows = tableData.value
  if (routeContractId.value) {
    rows = rows.filter(x => String(x.contractId) === String(routeContractId.value))
  }
  if (routeType.value) {
    rows = rows.filter(x => x.type === routeType.value)
  }
  // 类型和客户名称已由后端查询处理，前端不再过滤
  return rows
})

const handleQuery = () => {
  currentPage.value = 1
  fetchData()
}

const resetQuery = () => {
  queryParams.type = ''
  queryParams.customerName = ''
  handleQuery()
}

const handleExport = () => {
  const params = new URLSearchParams()
  if (queryParams.customerName) {
    params.append('customerName', queryParams.customerName)
  }
  if (queryParams.type) {
    params.append('type', queryParams.type)
  }
  const url = `/api/crm/finance/export?${params.toString()}`
  window.open(url, '_blank')
  ElMessage.success('正在导出...')
}

const handleDelete = (row: any) => {
  if (row.arrivalTime) {
    ElMessage.warning('已到账记录禁止删除')
    return
  }
  ElMessageBox.confirm('确认删除该记录吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteFinance(row.id)
    ElMessage.success('删除成功')
    fetchData()
  })
}

const arrivalDialogVisible = ref(false)
const arrivalInitialArrived = ref(false)
const arrivalMismatch = computed(() => {
  const a = Number(arrivalForm.arrivalAmount ?? 0).toFixed(2)
  const b = Number(arrivalForm.amount ?? 0).toFixed(2)
  return a !== b
})
  const arrivalForm = reactive<any>({
    id: undefined,
    type: 'IN',
    amount: 0,
    arrivalAmount: 0,
    arrivalTime: '',
    paymentVoucher: '',
    collectionDetail: '',
    invoiceNo: '',
    contractId: undefined,
    companyName: ''
  })
const invoiceOptions = ref<string[]>([])
const arrivalVoucherList = ref<any[]>([])
  const handleArrival = (row: any) => {
    arrivalForm.id = row.id
    arrivalForm.type = row.type
    arrivalForm.amount = Number(row.amount || 0)
    arrivalForm.arrivalAmount = Number(row.arrivalAmount || row.amount || 0)
    arrivalForm.arrivalTime = row.arrivalTime ? dayjs(row.arrivalTime).format('YYYY-MM-DD HH:mm:ss') : dayjs().format('YYYY-MM-DD HH:mm:ss')
    arrivalForm.paymentVoucher = row.paymentVoucher || ''
    arrivalForm.collectionDetail = row.collectionDetail || ''
    arrivalForm.contractId = row.contractId
    arrivalForm.invoiceNo = row.invoiceNo || ''
    arrivalForm.companyName = row.companyName || ''
    arrivalDialogVisible.value = true
    arrivalInitialArrived.value = !!row.arrivalTime
    arrivalOriginal.value = JSON.parse(JSON.stringify(arrivalForm))
    arrivalVoucherList.value = arrivalForm.paymentVoucher ? parseVouchers(arrivalForm.paymentVoucher).map((u:string) => ({ name: '到账凭证', url: u })) : []
    if (arrivalForm.type === 'IN' && arrivalForm.contractId) {
      getInvoiceList({ current: 1, size: 1000 }).then((res:any) => {
        // 已完成状态列表：DONE(已开票)、冲抵货款、收据
        const completedStatuses = ['DONE', '已开票', '冲抵货款', '收据']
        const list = (res.records || []).filter((x:any) => x.contractId === arrivalForm.contractId && completedStatuses.includes(x.invoiceStatus))
        invoiceOptions.value = list.map((x:any) => x.invoiceNo).filter(Boolean)
      })
    } else {
      invoiceOptions.value = []
    }
  }
const onVoucherSuccess = (res:any) => {
  const url = res?.data
  if (!url) return
  arrivalVoucherList.value = [...arrivalVoucherList.value, { name: '到账凭证', url }]
  arrivalForm.paymentVoucher = arrivalVoucherList.value.map((f:any) => f.url).join(',')
}
const onVoucherRemove = (_file:any, files:any[]) => {
  arrivalVoucherList.value = files
  arrivalForm.paymentVoucher = arrivalVoucherList.value.map((f:any) => f.url).join(',')
}
const onVoucherPreview = (file:any) => {
  const u = file?.url
  if (u) window.open(normalizeUrl(u), '_blank')
}
  const submitArrival = async () => {
    if (arrivalMismatch.value) {
      ElMessage.error('到账金额必须与申请金额一致')
      return
    }
    if (arrivalForm.type === 'IN' && !arrivalForm.invoiceNo) {
      ElMessage.error('请填写发票号码')
      return
    }
    if (!String(arrivalForm.collectionDetail || '').trim()) {
      ElMessage.error('请必须填写收支说明')
      return
    }
    const payload = {
      arrivalTime: arrivalForm.arrivalTime,
      arrivalAmount: arrivalForm.arrivalAmount,
      paymentVoucher: arrivalForm.paymentVoucher,
      collectionDetail: arrivalForm.collectionDetail,
      invoiceNo: arrivalForm.invoiceNo
    }
    await updateFinance(arrivalForm.id, payload)
  ElMessage.success('到账信息已更新')
  arrivalDialogVisible.value = false
  fetchData()
}
const returnFinance = async () => {
  if (arrivalInitialArrived.value) {
    ElMessage.warning('已到账记录禁止退回')
    return
  }
  await auditFinance(arrivalForm.id, 'REJECTED', '财务退回：金额不匹配')
  ElMessage.success('已退回，请销售修改后重新提交')
  arrivalDialogVisible.value = false
  fetchData()
}

const editDialogVisible = ref(false)
const editInitialStatus = ref<string>('')
const editMismatch = computed(() => {
  const a = Number(editForm.arrivalAmount ?? 0).toFixed(2)
  const b = Number(editForm.amount ?? 0).toFixed(2)
  return a !== b
})
const editForm = reactive<any>({
  id: undefined,
  type: 'IN',
  amount: 0,
  remark: '',
  arrivalTime: '',
  arrivalAmount: 0,
  paymentVoucher: '',
  companyName: ''
})
const editVoucherList = ref<any[]>([])
  const handleEdit = (row: any) => {
  editForm.id = row.id
  editForm.type = row.type
  editForm.amount = Number(row.amount || 0)
  editForm.remark = row.remark || ''
  editForm.arrivalAmount = Number(row.arrivalAmount || row.amount || 0)
  editForm.arrivalTime = row.arrivalTime ? dayjs(row.arrivalTime).format('YYYY-MM-DD HH:mm:ss') : ''
  editForm.paymentVoucher = row.paymentVoucher || ''
  editForm.invoiceNo = row.invoiceNo || ''
  editForm.companyName = row.companyName || ''
  editInitialStatus.value = row.auditStatus || ''
  editDialogVisible.value = true
  editOriginal.value = JSON.parse(JSON.stringify(editForm))
  editVoucherList.value = editForm.paymentVoucher ? parseVouchers(editForm.paymentVoucher).map((u:string) => ({ name: '凭证', url: u })) : []
}
const submitEdit = async () => {
  if (editMismatch.value) {
    ElMessage.error('到账金额必须与申请金额一致')
    return
  }
  if (editForm.type === 'IN' && editForm.arrivalTime && !editForm.invoiceNo) {
    ElMessage.error('请填写发票号码')
    return
  }
  if (!String(editForm.remark || '').trim()) {
    ElMessage.error('请必须填写收支说明')
    return
  }
  const payload = {
    type: editForm.type,
    amount: editForm.amount,
    remark: editForm.remark,
    arrivalTime: editForm.arrivalTime,
    arrivalAmount: editForm.arrivalAmount,
    paymentVoucher: editForm.paymentVoucher,
    invoiceNo: editForm.invoiceNo
  }
  await updateFinance(editForm.id, payload)
  if (editInitialStatus.value === 'REJECTED') {
    await auditFinance(editForm.id, 'PENDING', '销售已修改，待财务确认')
  }
  ElMessage.success('修改成功')
  editDialogVisible.value = false
  fetchData()
  if (routeOpenEdit.value) {
    router.push({ path: '/crm/contract' })
  }
}
const onEditVoucherSuccess = (res:any) => {
  const url = res?.data
  if (!url) return
  editVoucherList.value = [...editVoucherList.value, { name: '凭证', url }]
  editForm.paymentVoucher = editVoucherList.value.map((f:any) => f.url).join(',')
}
const onEditVoucherRemove = (_file:any, files:any[]) => {
  editVoucherList.value = files
  editForm.paymentVoucher = editVoucherList.value.map((f:any) => f.url).join(',')
}
const onEditVoucherPreview = (file:any) => {
  const u = file?.url
  if (u) window.open(normalizeUrl(u), '_blank')
}

onMounted(async () => {
  await Promise.all([fetchCustomers(), fetchContracts(), fetchCategories()])
  routeContractId.value = (route.query.contractId as string) || undefined
  routeType.value = (route.query.type as string) || undefined
  routeOpenEdit.value = (route.query.openEdit as string) || undefined
  await fetchData()
  if (routeOpenEdit.value) {
    const rows = filteredData.value
    const target = rows.find(x => x.type === 'IN' && x.auditStatus === 'REJECTED')
    if (target) {
      handleEdit(target)
    }
  }
})
const arrivalOriginal = ref<any>({})
const editOriginal = ref<any>({})
const isArrivalChanged = () => {
  return JSON.stringify(arrivalForm) !== JSON.stringify(arrivalOriginal.value || {})
}
const isEditChanged = () => {
  return JSON.stringify(editForm) !== JSON.stringify(editOriginal.value || {})
}
const onArrivalBeforeClose = async (done: () => void) => {
  if (!isArrivalChanged()) {
    done()
    return
  }
  try {
    await ElMessageBox.confirm('内容已修改，是否保存？', '提示', { confirmButtonText: '保存并关闭', cancelButtonText: '直接关闭' })
    await submitArrival()
    done()
  } catch {
    done()
  }
}
const onEditBeforeClose = async (done: () => void) => {
  if (!isEditChanged()) {
    done()
    return
  }
  try {
    await ElMessageBox.confirm('内容已修改，是否保存？', '提示', { confirmButtonText: '保存并关闭', cancelButtonText: '直接关闭' })
    await submitEdit()
    done()
  } catch {
    done()
  }
}
const beforeUnloadHandler = (e: BeforeUnloadEvent) => {
  if ((arrivalDialogVisible.value && isArrivalChanged()) || (editDialogVisible.value && isEditChanged())) {
    e.preventDefault()
    e.returnValue = ''
  }
}
onMounted(() => {
  window.addEventListener('beforeunload', beforeUnloadHandler)
})
onUnmounted(() => {
  window.removeEventListener('beforeunload', beforeUnloadHandler)
})
const parseVouchers = (v:string) => {
  return String(v||'').split(',').map((x:string) => x.trim()).filter(Boolean)
}
const normalizeUrl = (u?: string) => {
  if (!u) return ''
  if (u.startsWith('http')) return u
  const path = u.startsWith('/files/') ? u : (`/files/${u.replace(/^\/+/, '')}`)
  return `${window.location.origin}${path}`
}
const isImage = (u: string) => {
  const lower = u.toLowerCase()
  return lower.endsWith('.jpg') || lower.endsWith('.jpeg') || lower.endsWith('.png') || lower.endsWith('.gif') || lower.startsWith('data:image')
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.toolbar {
  margin-bottom: 16px;
}
.pagination-container {
  margin-top: 16px;
  text-align: right;
}
.text-success {
  color: #67C23A;
}
.text-warning {
  color: #E6A23C;
}
</style>
