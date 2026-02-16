<template>
  <div class="app-container">
    <h2 class="page-title">商品采购</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #409EFF;">{{ statistics.totalCount || 0 }}</div>
            <div style="color: #909399; margin-top: 8px;">采购单总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #67C23A;">{{ statistics.pendingCount || 0 }}</div>
            <div style="color: #909399; margin-top: 8px;">待审核</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #E6A23C;">{{ formatCurrency(statistics.totalAmount) }}</div>
            <div style="color: #909399; margin-top: 8px;">采购总金额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #F56C6C;">{{ statistics.totalQuantity || 0 }}</div>
            <div style="color: #909399; margin-top: 8px;">采购总数量</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索栏 -->
    <div class="toolbar">
      <el-input v-model="queryParams.purchaseNo" placeholder="采购单号" clearable style="width: 200px; margin-right: 10px;" @keyup.enter="handleQuery" />
      <el-select v-model="queryParams.auditStatus" placeholder="审核状态" clearable style="width: 150px; margin-right: 10px;">
        <el-option label="待审核" value="PENDING" />
        <el-option label="已通过" value="PASSED" />
        <el-option label="已驳回" value="REJECTED" />
      </el-select>
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
      <el-button v-if="canCreate" type="primary" @click="handleAdd">新增采购单</el-button>
    </div>

    <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
      <el-table-column prop="purchaseNo" label="采购单号" width="180" align="center" />
      <el-table-column prop="supplierName" label="供应商" width="180" align="center" />
      <el-table-column prop="totalAmount" label="总金额" width="150" align="right">
        <template #default="scope">
          {{ formatCurrency(scope.row.totalAmount) }}
        </template>
      </el-table-column>
      <el-table-column prop="purchaseQuantity" label="采购数量" width="100" align="center" />
      <el-table-column prop="deliveryDate" label="交货日期" width="120" align="center" />
      <el-table-column prop="auditStatus" label="审核状态" width="120" align="center">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.auditStatus)">
            {{ getStatusText(scope.row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="300" align="center">
        <template #default="scope">
          <el-button v-if="canUpdate && scope.row.auditStatus !== 'PASSED'" link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button v-if="canAudit && scope.row.auditStatus === 'PENDING'" link type="success" @click="handleAudit(scope.row)">审核</el-button>
          <el-button v-if="canPay && scope.row.auditStatus === 'PASSED'" link type="warning" @click="handlePay(scope.row)">付款</el-button>
          <el-button link type="warning" @click="handleToInbound(scope.row)" v-if="scope.row.auditStatus === 'PASSED' && scope.row.purchaseInbound !== 1">生成入库单</el-button>
           <el-tag type="info" v-if="scope.row.purchaseInbound === 1">已入库</el-tag>
          <el-button v-if="canDelete && scope.row.auditStatus !== 'PASSED'" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
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

    <PurchaseForm v-model="dialogVisible" :id="currentId" @success="fetchData" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { getPurchaseList, deletePurchase, auditPurchase } from '@/api/purchase'
import { createInboundFromPurchase } from '@/api/inbound'
import { ElMessage, ElMessageBox } from 'element-plus'
import PurchaseForm from './PurchaseForm.vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

// 权限检查函数
const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canCreate = computed(() => hasPermission('PA:create'))
const canUpdate = computed(() => hasPermission('PA:update'))
const canDelete = computed(() => hasPermission('PA:delete'))
const canAudit = computed(() => hasPermission('PA:audit'))
const canPay = computed(() => hasPermission('PA:pay'))

const tableData = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const currentId = ref<number | undefined>(undefined)

const queryParams = reactive({
  purchaseNo: '',
  auditStatus: ''
})

// 统计数据
const statistics = reactive({
  totalCount: 0,
  pendingCount: 0,
  totalAmount: 0,
  totalQuantity: 0
})

const formatCurrency = (value: number | undefined) => {
  if (value === undefined || value === null) return '¥0.00'
  return `¥${value.toFixed(2)}`
}

const getStatusType = (status: string) => {
  if (status === 'PASSED') return 'success'
  if (status === 'REJECTED') return 'danger'
  if (status === 'PENDING') return 'warning'
  return 'info'
}

const getStatusText = (status: string) => {
  if (status === 'PASSED') return '已通过'
  if (status === 'REJECTED') return '已驳回'
  if (status === 'PENDING') return '待审核'
  return status || '未知'
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      ...queryParams
    }
    const res: any = await getPurchaseList(params)
    tableData.value = res.records
    total.value = res.total
    
    // 计算统计数据
    calculateStatistics()
  } finally {
    loading.value = false
  }
}

// 计算统计数据
const calculateStatistics = () => {
  statistics.totalCount = total.value
  statistics.pendingCount = tableData.value.filter((item: any) => item.auditStatus === 'PENDING').length
  statistics.totalAmount = tableData.value.reduce((sum: number, item: any) => sum + (Number(item.totalAmount) || 0), 0)
  statistics.totalQuantity = tableData.value.reduce((sum: number, item: any) => sum + (Number(item.purchaseQuantity) || 0), 0)
}

const handleQuery = () => {
  currentPage.value = 1
  fetchData()
}

const resetQuery = () => {
  queryParams.purchaseNo = ''
  queryParams.auditStatus = ''
  handleQuery()
}

const handleAdd = () => {
  currentId.value = undefined
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  currentId.value = row.id
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该采购单吗？', '提示', { type: 'warning' }).then(async () => {
    await deletePurchase(row.id!)
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleAudit = (row: any) => {
  ElMessageBox.confirm('确认审核通过该采购单吗？', '提示', { type: 'info' }).then(async () => {
    await auditPurchase(row.id!, 'APPROVED')
    ElMessage.success('审核成功')
    fetchData()
  })
}

const handleToInbound = (row: any) => {
  ElMessageBox.confirm('确认根据该采购单生成入库单吗？', '提示', { type: 'info' }).then(async () => {
    await createInboundFromPurchase(row.id!)
    ElMessage.success('入库单生成成功，请到入库管理查看')
    router.push('/scm/inbound/purchase-inbound')
  })
}

const handlePay = (row: any) => {
  ElMessageBox.confirm('确认付款吗？', '提示', { type: 'info' }).then(async () => {
    // 付款功能待实现
    ElMessage.success('付款功能开发中...')
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.search-form {
  margin-bottom: -18px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
