<template>
  <div class="app-container">
    <h2 class="page-title">销售发票管理</h2>
    
    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #409EFF;">{{ formatCurrency(statistics.totalAmount) }}</div>
            <div style="color: #909399; margin-top: 8px;">开票总额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #67C23A;">{{ formatCurrency(statistics.doneAmount) }}</div>
            <div style="color: #909399; margin-top: 8px;">已开票金额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #E6A23C;">{{ formatCurrency(statistics.waitingAmount) }}</div>
            <div style="color: #909399; margin-top: 8px;">待开票金额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #F56C6C;">{{ statistics.totalCount || 0 }}</div>
            <div style="color: #909399; margin-top: 8px;">发票总数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索栏 -->
    <div class="toolbar">
      <el-input v-model="queryParams.contractNo" placeholder="搜索合同编号" style="width: 180px; margin-right: 10px;" clearable @keyup.enter="handleQuery" />
      <el-input v-model="queryParams.customerName" placeholder="搜索客户名称" style="width: 180px; margin-right: 10px;" clearable />
      <el-input v-model="queryParams.invoiceNo" placeholder="搜索发票编号" style="width: 180px; margin-right: 10px;" clearable @keyup.enter="handleQuery" />
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <el-table :data="filteredData" border stripe v-loading="loading" style="margin-top: 16px;">
      <el-table-column type="index" width="50" align="center" />
      <el-table-column label="合同编号" min-width="150">
        <template #default="scope">
          {{ getContractNo(scope.row.contractId) || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="客户名称" min-width="180" show-overflow-tooltip>
        <template #default="scope">
          {{ getCustomerName(scope.row.customerId) || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="invoiceUnit" label="开票单位" min-width="180" show-overflow-tooltip />
      <el-table-column prop="creditCode" label="信用代号" width="140" align="center" />
      <el-table-column prop="invoiceAmount" label="开票金额" width="140" align="right">
        <template #default="scope">
          <span :class="{ 'text-success': completedStatusList.includes(scope.row.invoiceStatus) }">
            ¥{{ scope.row.invoiceAmount?.toFixed(2) || '0.00' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="开票状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="getInvoiceType(scope.row.invoiceStatus)" size="small">{{ getInvoiceText(scope.row.invoiceStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="invoiceNo" label="发票号码" width="140" align="center" />
      <el-table-column label="开票人" width="120" align="center">
        <template #default="scope">{{ getUserName(scope.row.invoicerId) || '-' }}</template>
      </el-table-column>
      <el-table-column prop="invoiceTime" label="开票时间" width="160" align="center">
        <template #default="scope">{{ formatDate(scope.row.invoiceTime) || '-' }}</template>
      </el-table-column>
      <el-table-column prop="invoiceAttachment" label="发票附件" width="100" align="center">
        <template #default="scope">
          <div v-if="scope.row.invoiceAttachment">
            <el-button type="primary" link @click="downloadFile(scope.row.invoiceAttachment)">
              <el-icon><Download /></el-icon>
            </el-button>
            <el-button type="info" link @click="previewFile(scope.row.invoiceAttachment)">
              <el-icon><View /></el-icon>
            </el-button>
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="applyRemark" label="申请备注" min-width="180" show-overflow-tooltip />
      <el-table-column label="申请人" width="100" align="center">
        <template #default="scope">{{ getUserName(scope.row.applicantId) || '-' }}</template>
      </el-table-column>
      <el-table-column prop="applyTime" label="申请时间" width="160" align="center">
        <template #default="scope">{{ formatDate(scope.row.applyTime) || '-' }}</template>
      </el-table-column>
      <el-table-column prop="invoiceGiveDesc" label="给付说明" min-width="150" show-overflow-tooltip />
      <el-table-column prop="giveTime" label="给付时间" width="160" align="center">
        <template #default="scope">{{ formatDate(scope.row.giveTime) || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button 
            link 
            :type="scope.row.invoiceStatus === 'WAITING' || scope.row.invoiceStatus === '待开票' ? 'success' : 'primary'" 
            @click="openDetails(scope.row)"
          >
            {{ scope.row.invoiceStatus === 'WAITING' || scope.row.invoiceStatus === '待开票' ? '开票' : '明细' }}
          </el-button>
          <!-- 销售只能查看，不能编辑和删除 -->
          <el-button v-if="canUpdate && !isSalesUser" link type="warning" @click="gotoEdit(scope.row)">编辑</el-button>
          <el-button v-if="canDelete && !isSalesUser" link type="danger" :disabled="completedStatusList.includes(String(scope.row.invoiceStatus))" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10,20,50,100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>

    <el-dialog v-model="editVisible" :title="editForm.id ? '编辑发票' : '新增发票'" width="700px">
      <el-form :model="editForm" label-width="120px">
        <el-form-item label="客户" required>
          <el-select v-model="editForm.customerId" filterable style="width:100%" @change="onCustomerChange">
            <el-option v-for="c in customers" :key="c.id" :label="c.customerName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="合同" required>
          <el-select v-model="editForm.contractId" filterable style="width:100%">
            <el-option v-for="c in filteredContracts" :key="c.id" :label="c.contractNo" :value="c.id">
              <span :style="{ color: c.outStockDate ? '#F56C6C' : '' }">
                {{ c.contractNo }}
                <el-tag v-if="c.outStockDate" type="danger" size="small" style="margin-left: 4px;">已发货</el-tag>
                <span style="margin-left: 8px; color: #909399; font-size: 12px;">
                  (未开票: ¥{{ (Number(c.contractAmount || 0) - Number(c.invoicedAmount || 0)).toFixed(2) }})
                </span>
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="信用代号">
          <el-input v-model="editForm.creditCode" />
        </el-form-item>
        <el-form-item label="开票金额">
          <el-input-number v-model="editForm.invoiceAmount" :precision="2" :step="100" style="width:100%" />
        </el-form-item>
        <el-form-item label="申请备注">
          <el-input v-model="editForm.applyRemark" type="textarea" />
        </el-form-item>
        <el-form-item label="开票状态">
          <el-select v-model="editForm.invoiceStatus" style="width:100%">
            <el-option label="待开票" value="WAITING" />
            <el-option label="已开票" value="DONE" />
            <el-option label="作废" value="VOID" />
          </el-select>
        </el-form-item>
        <el-form-item label="发票附件">
          <el-upload action="/api/common/upload" :headers="uploadHeaders" :show-file-list="false" :on-success="onAttachmentSuccess">
            <el-button>上传附件</el-button>
          </el-upload>
          <a v-if="editForm.invoiceAttachment" :href="editForm.invoiceAttachment" target="_blank" style="margin-left:10px;">查看</a>
        </el-form-item>
        <el-form-item label="发票号码">
          <el-input v-model="editForm.invoiceNo" />
        </el-form-item>
        <el-form-item label="开票人">
          <el-select v-model="editForm.invoicerId" filterable style="width:100%">
            <el-option v-for="u in users" :key="u.id" :label="u.name" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开票时间">
          <el-date-picker v-model="editForm.invoiceTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="发票给付说明">
          <el-input v-model="editForm.invoiceGiveDesc" />
        </el-form-item>
        <el-form-item label="给付时间">
          <el-date-picker v-model="editForm.giveTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <div style="margin:10px 0;display:flex;justify-content:space-between;align-items:center;font-weight:bold;">
          <span>开票明细</span>
          <div style="display:flex;gap:10px;">
            <el-button type="primary" link @click="addEditDetail">添加明细</el-button>
            <el-button type="warning" link @click="importEditContractDetails">调用合同明细</el-button>
          </div>
        </div>
        <el-table :data="editForm.detailList" border>
          <el-table-column prop="productName" label="商品名称" min-width="200">
            <template #default="scope">
              <el-input v-model="scope.row.productName" />
            </template>
          </el-table-column>
          <el-table-column prop="productSpec" label="型号" min-width="160">
            <template #default="scope">
              <el-input v-model="scope.row.productSpec" />
            </template>
          </el-table-column>
          <el-table-column prop="salesQuantity" label="数量" width="120">
            <template #default="scope">
              <el-input-number v-model="scope.row.salesQuantity" :min="1" />
            </template>
          </el-table-column>
          <el-table-column prop="salesPrice" label="单价" width="140">
            <template #default="scope">
              <el-input-number v-model="scope.row.salesPrice" :precision="2" :step="100" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="140">
            <template #default="scope">
              {{ (Number(scope.row.salesPrice||0) * Number(scope.row.salesQuantity||0)).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default="scope">
              <el-button type="danger" link @click="removeEditDetail(scope.$index)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="editVisible=false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="发票详情" width="900px">
      <div style="margin-bottom:10px;display:flex;gap:8px;align-items:center;">
        <el-tag type="info">合同：{{ getContractNo(detailInvoice.contractId) }}</el-tag>
        <el-tag type="success">客户：{{ getCustomerName(detailInvoice.customerId) }}</el-tag>
        <el-button v-if="!isSalesUser" type="primary" @click="importContractDetails">调用合同明细</el-button>
      </div>
      <el-table :data="detailInvoice.detailList || []" border>
        <el-table-column prop="productName" label="商品名称" min-width="200" />
        <el-table-column prop="productSpec" label="型号" min-width="160" />
        <el-table-column prop="salesQuantity" label="数量" width="120" />
        <el-table-column prop="salesPrice" label="单价" width="140" />
        <el-table-column label="小计" width="140">
          <template #default="scope">
            {{ (Number(scope.row.salesPrice||0) * Number(scope.row.salesQuantity||0)).toFixed(2) }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="detailVisible=false">关闭</el-button>
        <el-button v-if="!isSalesUser" type="primary" @click="saveDetails">保存明细</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getInvoiceList, saveInvoice, updateInvoice, deleteInvoice, getInvoiceById } from '@/api/invoice'
import { getCustomerList } from '@/api/customer'
import { getContractList, getContractById } from '@/api/contract'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, View } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statistics = ref<any>({})

// 用户角色判断
const isFinanceUser = computed(() => {
  const ui:any = userStore.userInfo
  const type = ui?.userType
  const roleName = ui?.roleName
  const roles = ui?.roles || []
  return type === 'finance'
    || String(roleName||'').includes('财务')
    || roles.some((r:any) => String(r.roleName||r.name||'').includes('财务') || String(r.roleKey||'').includes('finance'))
})

const isAdminUser = computed(() => {
  const ui:any = userStore.userInfo
  const type = ui?.userType
  const username = ui?.username
  const roleId = ui?.roleId
  const roleName = ui?.roleName
  const roles = ui?.roles || []
  return type === 'admin'
    || username === 'admin'
    || roleId === 1
    || String(roleName||'').includes('管理员')
    || roles.some((r:any) => String(r.roleName||r.name||'').includes('管理员') || String(r.roleKey||'').includes('admin'))
})

const isSalesUser = computed(() => !isFinanceUser.value && !isAdminUser.value)

const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canCreate = computed(() => hasPermission('IM:create'))
const canUpdate = computed(() => hasPermission('IM:update'))
const canDelete = computed(() => hasPermission('IM:delete'))

// 上传请求头（包含认证token）
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    Authorization: token ? `Bearer ${token}` : ''
  }
})

const queryParams = reactive({ contractNo: '', customerName: '', invoiceNo: '' })

const customers = ref<any[]>([])
const contracts = ref<any[]>([])
const users = ref<any[]>([])

// 过滤还有未开票金额的合同
const filteredContracts = computed(() => {
  return contracts.value.filter(c => {
    const contractAmount = Number(c.contractAmount || 0)
    const invoicedAmount = Number(c.invoicedAmount || 0)
    const remainingAmount = contractAmount - invoicedAmount
    return remainingAmount > 0
  })
})

// 开票状态字典
const invoiceStatusDict = ref<string[]>([])

// 状态值映射（值 -> 显示文本）
const invoiceStatusMap: Record<string, string> = {
  'WAITING': '待开票',
  'DONE': '已开票',
  'VOID': '作废',
  '冲抵货款': '冲抵货款',
  '收据': '收据'
}

// 状态类型映射（值 -> 标签类型）
const invoiceStatusTypeMap: Record<string, string> = {
  'WAITING': 'warning',
  'DONE': 'success',
  'VOID': 'danger',
  '冲抵货款': 'success',
  '收据': 'success'
}

// 加载开票状态字典
const loadInvoiceStatusDict = async () => {
  try {
    const res: any = await request({ url: '/system/config/list', method: 'get', params: { size: 100 } })
    const general = (res.records || res || []).find((item: any) => item.configKey === 'general_settings')
    if (general && general.configValue) {
      const config = typeof general.configValue === 'string' ? JSON.parse(general.configValue) : general.configValue
      invoiceStatusDict.value = config.customCategories?.开票状态 || ['待开票', '已开票', '作废']
    }
  } catch {
    // 使用默认值
    invoiceStatusDict.value = ['待开票', '已开票', '作废']
  }
}

const fetchCustomers = async () => {
  const res:any = await getCustomerList({ current: 1, size: 1000 })
  customers.value = res.records || []
}
const fetchContracts = async () => {
  const res:any = await getContractList({ current: 1, size: 1000 })
  contracts.value = res.records || []
}
const fetchUsers = async () => {
  const res:any = await request({ url: '/system/user/list', method: 'get', params: { size: 1000 } })
  users.value = res.records || []
}

const getCustomerName = (id?: number) => {
  if (!id) return ''
  const c = customers.value.find((x:any) => x.id === id)
  if (!c) {
    console.warn('未找到客户ID:', id, '客户列表长度:', customers.value.length)
    return `客户${id}`
  }
  return c.customerName || `客户${id}`
}

const getContractNo = (id?: number) => {
  if (!id) return ''
  const c = contracts.value.find((x:any) => x.id === id)
  return c?.contractNo || id
}

const getUserName = (id?: number) => {
  if (!id) return ''
  const u = users.value.find((x:any) => x.id === id)
  return u?.name || id
}

const formatCurrency = (v: number | string | undefined | null) => {
  const n = Number(v ?? 0)
  return `¥${n.toFixed(2)}`
}
const formatDate = (val: string | undefined) => {
  if (!val) return ''
  return String(val).replace('T', ' ')
}
const getInvoiceType = (s: string | undefined) => {
  return invoiceStatusTypeMap[s || ''] || 'info'
}
const getInvoiceText = (s: string | undefined) => {
  return invoiceStatusMap[s || ''] || '未知'
}

const fetchData = async () => {
  loading.value = true
  try {
    const params: any = { current: currentPage.value, size: pageSize.value }
    // 客户名称后端模糊查询
    if (queryParams.customerName) {
      params.customerName = queryParams.customerName
    }
    // 发票编号后端模糊查询
    if (queryParams.invoiceNo) {
      params.invoiceNo = queryParams.invoiceNo
    }
    const res:any = await getInvoiceList(params)
    tableData.value = res.records || []
    total.value = res.total || 0
    // 计算统计数据
    calculateStatistics()
  } finally {
    loading.value = false
  }
}

// 已完成状态列表（包含已开票、冲抵货款、收据）
const completedStatusList = ['DONE', '已开票', '冲抵货款', '收据']

const calculateStatistics = () => {
  const rows = tableData.value
  let totalAmount = 0
  let doneAmount = 0
  let waitingAmount = 0
  
  rows.forEach((row: any) => {
    const amount = Number(row.invoiceAmount || 0)
    totalAmount += amount
    // 已完成状态包含：DONE、已开票、冲抵货款、收据
    if (completedStatusList.includes(row.invoiceStatus)) {
      doneAmount += amount
    } else if (row.invoiceStatus === 'WAITING' || row.invoiceStatus === '待开票') {
      waitingAmount += amount
    }
  })
  
  statistics.value = {
    totalAmount,
    doneAmount,
    waitingAmount,
    totalCount: rows.length
  }
}

const filteredData = computed(() => {
  let rows = tableData.value
  if (queryParams.contractNo) {
    rows = rows.filter(x => String(getContractNo(x.contractId)).includes(queryParams.contractNo))
  }
  // 发票编号前端过滤（作为后备）
  if (queryParams.invoiceNo) {
    rows = rows.filter(x => String(x.invoiceNo || '').includes(queryParams.invoiceNo))
  }
  // 客户名称已由后端模糊查询处理，前端不再过滤
  return rows
})

const handleQuery = () => {
  currentPage.value = 1
  fetchData()
}
const resetQuery = () => {
  queryParams.contractNo = ''
  queryParams.customerName = ''
  handleQuery()
}

const router = useRouter()
const editVisible = ref(false)
const editForm = reactive<any>({
  id: undefined,
  customerId: undefined,
  contractId: undefined,
  creditCode: '',
  auditStatus: 'PENDING',
  invoiceAmount: 0,
  applyRemark: '',
  invoiceStatus: 'WAITING',
  invoiceAttachment: '',
  invoicerId: undefined,
  invoiceTime: '',
  applicantId: undefined,
  applyTime: '',
  invoiceGiveDesc: '',
  giveTime: '',
  invoiceNo: '',
  detailList: []
})
const onAttachmentSuccess = (res:any) => { editForm.invoiceAttachment = res.data }
const gotoEdit = (row: any) => {
  router.push(`/crm/invoice/${row.id}`)
}
const handleDelete = async (row: any) => {
  ElMessageBox.confirm('确认删除该发票吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteInvoice(row.id)
    ElMessage.success('删除成功')
    fetchData()
  })
}
const submitEdit = async () => {
  if (!editForm.customerId || !editForm.contractId) {
    ElMessage.warning('请选择客户与合同')
    return
  }
  if (Array.isArray(editForm.detailList) && editForm.detailList.length) {
    const total = (editForm.detailList || []).reduce((sum:number, d:any) => {
      return sum + Number(d.salesPrice || 0) * Number(d.salesQuantity || 0)
    }, 0)
    editForm.invoiceAmount = total
  }
  try {
    if (editForm.id) {
      await updateInvoice(editForm.id, editForm)
      ElMessage.success('更新成功')
    } else {
      await saveInvoice(editForm)
      ElMessage.success('创建成功')
    }
    editVisible.value = false
    fetchData()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

const onCustomerChange = (id: number) => {
  const c = customers.value.find((x:any) => x.id === id)
  editForm.creditCode = c?.creditCode || ''
  editForm.contractId = undefined
}

const detailVisible = ref(false)
const detailInvoice = reactive<any>({ id: undefined, customerId: undefined, contractId: undefined, detailList: [] })
const openDetails = async (row: any) => {
  // 如果是待开票状态，跳转到编辑页面进行开票
  if (row.invoiceStatus === 'WAITING' || row.invoiceStatus === '待开票') {
    router.push(`/crm/invoice/${row.id}`)
  } else {
    // 其他状态显示详情
    const res:any = await getInvoiceById(row.id)
    Object.assign(detailInvoice, res)
    detailVisible.value = true
  }
}
const importContractDetails = async () => {
  if (!detailInvoice.contractId) {
    ElMessage.warning('请先选择合同')
    return
  }
  const c:any = await getContractById(detailInvoice.contractId)
  detailInvoice.detailList = (c.detailList || []).map((d:any) => ({
    productName: d.productName,
    productSpec: d.productSpec,
    salesQuantity: d.salesQuantity,
    salesPrice: d.salesPrice
  }))
}
const saveDetails = async () => {
  const total = (detailInvoice.detailList || []).reduce((sum:number, d:any) => {
    return sum + Number(d.salesPrice || 0) * Number(d.salesQuantity || 0)
  }, 0)
  await updateInvoice(detailInvoice.id, { detailList: detailInvoice.detailList, invoiceAmount: total })
  ElMessage.success('明细已保存')
  detailVisible.value = false
  fetchData()
}
const addEditDetail = () => {
  editForm.detailList.push({ productName: '', productSpec: '', salesQuantity: 1, salesPrice: 0 })
}
const removeEditDetail = (idx: number) => {
  editForm.detailList.splice(idx, 1)
}
const importEditContractDetails = async () => {
  if (!editForm.contractId) {
    ElMessage.warning('请先选择合同')
    return
  }
  const c:any = await getContractById(editForm.contractId)
  editForm.detailList = (c.detailList || []).map((d:any) => ({
    productName: d.productName,
    productSpec: d.productSpec,
    salesQuantity: d.salesQuantity,
    salesPrice: d.salesPrice
  }))
}

const downloadFile = async (url: string, originalName?: string) => {
  if (!url) return
  const absoluteUrl = url.startsWith('http') ? url : `${window.location.origin}${url}`
  const lastPart = decodeURIComponent(absoluteUrl.substring(absoluteUrl.lastIndexOf('/') + 1))
  let fileName = originalName || lastPart || 'invoice_attachment'
  const extMatch = fileName.match(/\.([^.]+)$/) || lastPart.match(/\.([^.]+)$/)
  const ext = (extMatch ? extMatch[1].toLowerCase() : (absoluteUrl.includes('.pdf') ? 'pdf' : 'jpg'))
  if (!fileName.endsWith(`.${ext}`)) fileName = `${fileName}.${ext}`
  fileName = fileName.replace(/[^a-zA-Z0-9._-]/g, '_')
  const mime = ext === 'pdf' ? 'application/pdf' : 'image/jpeg'
  try {
    const res = await fetch(absoluteUrl)
    const blob = await res.blob()
    const typedBlob = blob.type ? blob : new Blob([blob], { type: mime })
    const objectUrl = URL.createObjectURL(typedBlob)
    const a = document.createElement('a')
    a.href = objectUrl
    a.download = fileName
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(objectUrl)
  } catch (e) {
    const aFallback = document.createElement('a')
    aFallback.href = absoluteUrl
    aFallback.download = fileName
    document.body.appendChild(aFallback)
    aFallback.click()
    document.body.removeChild(aFallback)
  }
}

const previewFile = (url: string) => {
  if (!url) return
  
  // 检查是否为PDF文件
  const isPdf = url.toLowerCase().endsWith('.pdf')
  
  if (isPdf) {
    try {
      // 确保URL是绝对路径
      const absoluteUrl = url.startsWith('http') ? url : `${window.location.origin}${url}`
      
      // 使用浏览器内置PDF查看器
      const pdfWindow = window.open(absoluteUrl, '_blank')
      
      if (!pdfWindow) {
        // 如果弹窗被阻止，提供下载选项
        ElMessage.warning('PDF预览被阻止，请尝试下载查看')
        downloadFile(url)
      }
    } catch (error) {
      console.error('PDF预览失败:', error)
      ElMessage.error('PDF预览失败，请尝试下载查看')
      downloadFile(url)
    }
  } else {
    // 对于图片文件，直接在新窗口打开
    window.open(url, '_blank')
  }
}

onMounted(async () => {
  await Promise.all([fetchCustomers(), fetchContracts(), fetchUsers(), loadInvoiceStatusDict()])
  fetchData()
})
</script>

<style scoped>
.app-container { padding: 20px; }
.toolbar { margin-bottom: 16px; }
.pagination-container { margin-top: 16px; text-align: right; }
.text-success { color: #67C23A; }
</style>
