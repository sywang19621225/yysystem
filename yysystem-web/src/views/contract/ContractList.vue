<template>
  <div class="app-container">
    <h2 class="page-title">合同订单管理</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #409EFF;">{{ formatCurrency(summary.contractTotal) }}</div>
            <div style="color: #909399; margin-top: 8px;">总销售额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #67C23A;">{{ formatCurrency(summary.collectionTotal) }}</div>
            <div style="color: #909399; margin-top: 8px;">总收款</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #E6A23C;">{{ formatCurrency(summary.netReceivableTotal) }}</div>
            <div style="color: #909399; margin-top: 8px;">总应收款</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #F56C6C;">{{ formatCurrency(summary.invoicedTotal) }}</div>
            <div style="color: #909399; margin-top: 8px;">已开票金额</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索栏 -->
    <div class="toolbar">
      <el-input v-model="queryParams.contractNo" placeholder="合同编号" clearable style="width: 200px; margin-right: 10px;" @keyup.enter="handleQuery" />
      <el-input v-model="queryParams.customerName" placeholder="客户名称" clearable style="width: 200px; margin-right: 10px;" @keyup.enter="handleQuery" />
      <el-select v-model="queryParams.auditStatus" placeholder="审核状态" clearable style="width: 150px; margin-right: 10px;" @change="handleQuery">
        <el-option label="待审核" value="PENDING" />
        <el-option label="审核中" value="UNDER_REVIEW" />
        <el-option label="已通过" value="PASSED" />
      </el-select>
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
      <el-button v-if="hasPermission('CO:create')" type="primary" @click="handleAdd">新增合同</el-button>
    </div>

    <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
      <el-table-column label="商品出库" width="120" align="center" fixed="left">
        <template #default="scope">
          <el-tooltip v-if="scope.row.contractQty > 0" :content="`已出库: ${scope.row.outboundQty || 0} / 合同数量: ${scope.row.contractQty} / 剩余: ${Math.max(0, (scope.row.contractQty || 0) - (scope.row.outboundQty || 0))}`" placement="top">
            <el-tag :type="getOutboundStatusType(scope.row.outboundStatus)">
              {{ getOutboundStatusText(scope.row.outboundStatus) }}
            </el-tag>
          </el-tooltip>
          <span v-else style="color: #999;">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="contractNo" label="合同编号" width="180" align="center" />
      <el-table-column prop="contractName" label="合同名称" min-width="200" align="center" show-overflow-tooltip />
      <el-table-column prop="auditStatus" label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.auditStatus)">
            {{ getStatusText(scope.row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="contractAmount" label="合同金额" width="130" align="right">
        <template #default="scope">
          {{ formatCurrency(scope.row.contractAmount) }}
        </template>
      </el-table-column>
      <el-table-column prop="totalCollection" label="累计收款" width="130" align="right">
        <template #default="scope">
          {{ formatCurrency(scope.row.totalCollection) }}
        </template>
      </el-table-column>
      <el-table-column label="应收款" width="130" align="right">
        <template #default="scope">
          <span :style="{ color: (Number(scope.row.contractAmount || 0) - Number(scope.row.totalCollection || 0)) > 0 ? '#E6A23C' : '#67C23A' }">
            {{ formatCurrency(Number(scope.row.contractAmount || 0) - Number(scope.row.totalCollection || 0)) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="invoicedAmount" label="开票金额" width="130" align="right">
        <template #default="scope">
          {{ formatCurrency(scope.row.invoicedAmount) }}
        </template>
      </el-table-column>
      <el-table-column label="未开票金额" width="130" align="right">
        <template #default="scope">
          {{ formatCurrency(Number(scope.row.contractAmount || 0) - Number(scope.row.invoicedAmount || 0)) }}
        </template>
      </el-table-column>
      <el-table-column label="居间费支付" width="120" align="center">
        <template #default="scope">
          <el-tag v-if="Number(scope.row.intermediaryFee || 0) <= 0" type="info">无居间费</el-tag>
          <el-tag v-else :type="scope.row.intermediaryFeeStatus === 'PAID' ? 'success' : 'warning'">
            {{ scope.row.intermediaryFeeStatus === 'PAID' ? '已支付' : '未支付' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="280" align="center" fixed="right">
        <template #default="scope">
          <el-button v-if="hasPermission('CO:update')" link size="small" type="primary" @click="handleEdit(scope.row)">{{ scope.row.auditStatus === 'PASSED' ? '浏览' : '修改' }}</el-button>
          <el-button v-if="hasPermission('CO:update')" link size="small" type="primary" @click="handleInvoice(scope.row)">开票</el-button>
          <el-button v-if="hasPermission('CO:update')" link size="small" :type="scope.row.hasRejectedIncome ? 'danger' : 'success'" @click="handleFinance(scope.row)">
            {{ scope.row.hasRejectedIncome ? '修改付款' : '收款' }}
          </el-button>
          <el-button v-if="hasPermission('CO:update')" link size="small" type="warning" @click="handleExpense(scope.row)">{{ scope.row.hasRejectedExpense ? '修改支出' : '支出' }}</el-button>
          <el-button v-if="isAdmin && scope.row.auditStatus === 'PASSED'" link size="small" type="warning" @click="handleUnlock(scope.row)">解锁</el-button>
          <el-button v-if="hasPermission('CO:delete')" link size="small" type="danger" :disabled="scope.row.auditStatus === 'PASSED'" @click="handleDelete(scope.row)">删除</el-button>
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

    <FinanceDialog v-model="financeVisible" :contract-id="currentContractId" :customer-id="currentCustomerId" :type="financeType" :finance-id="financeEditId" @success="fetchData" />
    <ContractForm v-model="dialogVisible" :id="currentId" @success="fetchData" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onActivated } from 'vue'
import { getContractList, deleteContract, getContractFlags, unlockContract, getContractSummary, type Contract } from '@/api/contract'
import { getFinanceList } from '@/api/finance'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import FinanceDialog from './FinanceDialog.vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import ContractForm from './ContractForm.vue'

type ContractRow = Contract & {
  hasRejectedIncome?: boolean
  hasRejectedExpense?: boolean
  hasDepositExpense?: boolean
  intermediaryFeeStatus?: string
  intermediaryFee?: number
  invoicedAmount?: number
  totalCollection?: number
  totalExpenditure?: number
  advancePayment?: number
  attachment?: string
  extendFields?: any
  totalPayable?: number
  payableAmount?: number
  arrears?: number
  contractQty?: number
  outboundQty?: number
  outboundStatus?: string
  productTotal?: number
}
const tableData = ref<ContractRow[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const summary = reactive({
  contractTotal: 0,
  netReceivableTotal: 0,
  collectionTotal: 0,
  payableTotal: 0,
  advanceTotal: 0,
  expenditureTotal: 0,
  invoicedTotal: 0
})

const queryParams = reactive({
  contractNo: '',
  customerName: '',
  auditStatus: ''
})

const userStore = useUserStore()
const isAdmin = ref(false)

// 判断是否有指定权限码
const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const permissionCodes = ui?.permissionCodes || []
  return isAdmin.value || permissionCodes.includes(permCode)
}

onMounted(() => {
  const ui: any = userStore.userInfo || {}
  const ut = String(ui.userType || '').toLowerCase()
  const roleId = ui.roleId
  const roleName = String(ui.roleName || '')
  isAdmin.value = (ut === 'admin') || (roleId === 1) || roleName.includes('管理员')
})

const router = useRouter()
const route = useRoute()

onActivated(() => {
  if (route.query.refresh === '1') {
    fetchData()
    router.replace({ query: {} })
  }
})

const formatCurrency = (value: number | undefined) => {
  if (value === undefined || value === null) return '¥0.00'
  return `¥${value.toFixed(2)}`
}

const getStatusType = (status: string | undefined) => {
  if (status === 'PASSED') return 'success'
  if (status === 'REJECTED') return 'danger'
  if (status === 'PENDING') return 'warning'
  if (status === 'UNDER_REVIEW') return 'primary'
  return 'info'
}

const getStatusText = (status: string | undefined) => {
  if (status === 'PASSED') return '已通过'
  if (status === 'REJECTED') return '已驳回'
  if (status === 'PENDING') return '待审核'
  if (status === 'UNDER_REVIEW') return '审核中'
  return status || '未知'
}

// 商品出库状态样式
const getOutboundStatusType = (status: string | undefined) => {
  if (status === 'COMPLETED') return 'success'
  if (status === 'PARTIAL') return 'warning'
  return 'info'
}

const getOutboundStatusText = (status: string | undefined) => {
  if (status === 'COMPLETED') return '已出库'
  if (status === 'PARTIAL') return '部分出库'
  return '未出库'
}

const parseAttachments = (extendFields: any) => {
  if (!extendFields) return []
  try {
    const ef = typeof extendFields === 'string' ? JSON.parse(extendFields) : extendFields
    return ef?.attachments || []
  } catch {
    return []
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params: any = {
      current: currentPage.value,
      size: pageSize.value
    }
    if (queryParams.contractNo) {
      params.contractNo = queryParams.contractNo
    }
    if (queryParams.customerName) {
      params.customerName = queryParams.customerName
    }
    if (queryParams.auditStatus) {
      params.auditStatus = queryParams.auditStatus
    }
    const res: any = await getContractList(params)
    const rows = res.records || []
    total.value = res.total || 0

    const contractIds = rows.map((row: ContractRow) => row.id).filter((id: number | undefined): id is number => id !== undefined)
    let flags: Record<number, any> = {}
    if (contractIds.length > 0) {
      const flagsRes: any = await getContractFlags(contractIds)
      if (Array.isArray(flagsRes)) {
        flagsRes.forEach((item: any) => {
          if (item.id !== undefined) {
            flags[item.id] = item
          }
        })
      }
    }

    tableData.value = rows.map((row: ContractRow) => {
      const rowId = row.id ?? 0
      const rowFlags = flags[rowId] || {}
      return {
        ...row,
        hasRejectedIncome: rowFlags.hasRejectedIncome || false,
        hasRejectedExpense: rowFlags.hasRejectedExpense || false,
        hasDepositExpense: rowFlags.hasDepositExpense || false,
        invoicedAmount: Number(row.invoicedAmount || 0),
        totalCollection: Number(row.totalCollection || 0),
        totalExpenditure: Number(row.totalExpenditure || 0),
        advancePayment: Number(row.advancePayment || 0),
        contractQty: rowFlags.contractQty || row.productTotal || 0,
        outboundQty: rowFlags.outboundQty || 0,
        outboundStatus: rowFlags.outboundStatus || 'NONE'
      }
    })

    await fetchSummary()
  } catch (error) {
    console.error('获取合同列表失败:', error)
    ElMessage.error('获取合同列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  currentPage.value = 1
  fetchData()
}

const resetQuery = () => {
  queryParams.contractNo = ''
  queryParams.customerName = ''
  queryParams.auditStatus = ''
  handleQuery()
}

// 获取全量统计数据
const fetchSummary = async () => {
  try {
    const res: any = await getContractSummary()
    if (res) {
      summary.contractTotal = Number(res.contractTotal || 0)
      summary.netReceivableTotal = Number(res.arrearsTotal || 0)
      summary.collectionTotal = Number(res.collectionTotal || 0)
      summary.payableTotal = Number(res.payableTotal || 0)
      summary.advanceTotal = Number(res.advanceTotal || 0)
      summary.expenditureTotal = Number(res.expenditureTotal || 0)
      summary.invoicedTotal = Number(res.invoicedTotal || 0)
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const dialogVisible = ref(false)
const currentId = ref<number | undefined>(undefined)

const handleAdd = () => {
  currentId.value = undefined
  dialogVisible.value = true
}

const handleEdit = (row: ContractRow) => {
  currentId.value = row.id
  dialogVisible.value = true
}

const handleDelete = (row: ContractRow) => {
  ElMessageBox.confirm('确定删除该合同吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      if (row.id !== undefined) {
        await deleteContract(row.id)
        ElMessage.success('删除成功')
        fetchData()
      }
    } catch (error) {
      console.error('删除合同失败:', error)
      ElMessage.error('删除合同失败')
    }
  }).catch(() => {})
}

// 开票
const handleInvoice = async (row: ContractRow) => {
  const remaining = Number(row.contractAmount || 0) - Number(row.invoicedAmount || 0)
  
  // 检查合同是否已完全开票
  if (remaining <= 0) {
    ElMessage.warning('该合同已完全开票，不能再开票')
    return
  }
  
  router.push({
    path: '/crm/invoice/edit',
    query: {
      contractId: row.id,
      customerId: row.customerId,
      remaining: remaining > 0 ? remaining : 0
    }
  })
}

// 收款/付款
const financeVisible = ref(false)
const currentContractId = ref<number | undefined>(undefined)
const currentCustomerId = ref<number | undefined>(undefined)
const financeType = ref<'IN' | 'OUT'>('IN')
const financeEditId = ref<number | undefined>(undefined)

const handleFinance = (row: ContractRow) => {
  const remaining = Number(row.contractAmount || 0) - Number(row.totalCollection || 0)
  
  // 检查合同是否已完全收款
  if (remaining <= 0) {
    ElMessage.warning('该合同已完全收款，不能再收款')
    return
  }
  
  currentContractId.value = row.id
  currentCustomerId.value = row.customerId
  financeType.value = 'IN'
  // 如果有驳回的收款记录，获取最新的一个
  if (row.hasRejectedIncome) {
    getFinanceList({ current: 1, size: 100, contractId: row.id, type: 'IN' }).then((res: any) => {
      const list = res.records || []
      // 找到最新的驳回记录
      const target = list.filter((x:any) => x.contractId === row.id && x.type === 'IN' && x.auditStatus === 'REJECTED').sort((a:any,b:any) => (new Date(b.createTime||0).getTime()) - (new Date(a.createTime||0).getTime()))[0]
      if (target) {
        financeEditId.value = target.id
      } else {
        financeEditId.value = undefined
      }
      financeVisible.value = true
    })
  } else {
    financeEditId.value = undefined
    financeVisible.value = true
  }
}

// 支出
const handleExpense = (row: ContractRow) => {
  currentContractId.value = row.id
  currentCustomerId.value = row.customerId
  financeType.value = 'OUT'
  // 如果有驳回的支出记录，获取最新的一个
  if (row.hasRejectedExpense) {
    getFinanceList({ current: 1, size: 100, contractId: row.id, type: 'OUT' }).then((res: any) => {
      const list = res.records || []
      // 找到最新的驳回记录
      const target = list.filter((x:any) => x.contractId === row.id && x.type === 'OUT' && x.auditStatus === 'REJECTED').sort((a:any,b:any) => (new Date(b.createTime||0).getTime()) - (new Date(a.createTime||0).getTime()))[0]
      if (target) {
        financeEditId.value = target.id
      } else {
        financeEditId.value = undefined
      }
      financeVisible.value = true
    })
  } else {
    financeEditId.value = undefined
    financeVisible.value = true
  }
}

const getIntermediaryPayInfo = (row: ContractRow) => {
  if (!row.extendFields) return { info: '', time: '' }
  try {
    const ef = typeof row.extendFields === 'string' ? JSON.parse(row.extendFields) : row.extendFields
    const pay = ef?.intermediaryPay
    if (!pay) return { info: '', time: '' }
    const lines: string[] = []
    if (pay.payTime) lines.push(`支付时间：${pay.payTime}`)
    if (pay.payMethod) lines.push(`支付方式：${pay.payMethod}`)
    if (pay.payAccount) lines.push(`支付账户：${pay.payAccount}`)
    if (pay.receiveAccount) lines.push(`收款账户：${pay.receiveAccount}`)
    if (pay.remark) lines.push(`备注：${pay.remark}`)
    return { info: lines.join('\n'), time: pay.recordTime || '' }
  } catch {
    return { info: '', time: '' }
  }
}

// 解锁合同
const handleUnlock = async (row: ContractRow) => {
  try {
    await ElMessageBox.confirm('确定要解锁该合同吗？解锁后将允许修改合同内容。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    if (row.id !== undefined) {
      await unlockContract(row.id)
      ElMessage.success('解锁成功')
      fetchData()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('解锁合同失败:', error)
      ElMessage.error('解锁合同失败')
    }
  }
}

onMounted(() => {
  fetchData()
  fetchSummary()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.page-title {
  margin-bottom: 20px;
  font-size: 20px;
  font-weight: bold;
}
.summary-bar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.search-form {
  display: flex;
  gap: 12px;
}
.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
