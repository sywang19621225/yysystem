<template>
  <div class="app-container">
    <h2 class="page-title">采购入库</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #409EFF;">{{ statistics.totalCount || 0 }}</div>
            <div style="color: #909399; margin-top: 8px;">入库单总数</div>
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
            <div style="color: #909399; margin-top: 8px;">入库总金额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #F56C6C;">{{ statistics.totalQuantity || 0 }}</div>
            <div style="color: #909399; margin-top: 8px;">入库总数量</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索栏 -->
    <div class="toolbar">
      <el-input v-model="queryParams.inboundNo" placeholder="入库单号" clearable style="width: 200px; margin-right: 10px;" @keyup.enter="handleQuery" />
      <el-input v-model="queryParams.supplierName" placeholder="供应商" clearable style="width: 200px; margin-right: 10px;" @keyup.enter="handleQuery" />
      <el-select v-model="queryParams.auditStatus" placeholder="审核状态" clearable style="width: 150px; margin-right: 10px;">
        <el-option label="待审核" value="PENDING" />
        <el-option label="已通过" value="PASSED" />
        <el-option label="已驳回" value="REJECTED" />
      </el-select>
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
      <el-button v-if="canCreate" type="primary" @click="handleAdd">新增采购入库</el-button>
    </div>

    <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
      <el-table-column prop="inboundNo" label="入库单号" width="180" align="center" />
      <el-table-column prop="inboundType" label="入库类型" width="120" align="center">
        <template #default>
          <el-tag type="success">采购入库</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="supplierName" label="供应商" min-width="150" show-overflow-tooltip />
      <el-table-column prop="purchaseOrderNo" label="采购单号" width="150" align="center" />
      <el-table-column prop="inboundTime" label="入库时间" width="180" align="center" />
      <el-table-column prop="totalCost" label="入库金额" width="120" align="right">
        <template #default="scope">
          {{ formatCurrency(scope.row.totalCost) }}
        </template>
      </el-table-column>
      <el-table-column prop="auditStatus" label="审核状态" width="120" align="center">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.auditStatus)">
            {{ getStatusText(scope.row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="280" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleView(scope.row)">查看详情</el-button>
          <el-button v-if="canUpdate && scope.row.auditStatus === 'PENDING'" link type="primary" @click="handleEdit(scope.row)">修改</el-button>
          <el-button v-if="canDelete && scope.row.auditStatus === 'PENDING'" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          <el-button v-if="canAudit && scope.row.auditStatus === 'PENDING'" link type="success" @click="handleAudit(scope.row)">审核入库</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]" :total="total" layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData" @current-change="fetchData" />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="dialogVisible" title="采购入库单详情" width="80%">
      <div v-if="currentInbound">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="入库单号">{{ currentInbound.inboundNo }}</el-descriptions-item>
          <el-descriptions-item label="入库类型">
            <el-tag type="success">采购入库</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="供应商">{{ currentInbound.supplierName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="采购单号">{{ currentInbound.purchaseOrderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="仓库">{{ getWarehouseName(currentInbound.warehouseId) }}</el-descriptions-item>
          <el-descriptions-item label="入库金额">{{ formatCurrency(currentInbound.totalCost) }}</el-descriptions-item>
          <el-descriptions-item label="入库时间">{{ formatDate(currentInbound.inboundTime) }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">{{ getStatusText(currentInbound.auditStatus) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentInbound.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin-top: 20px; margin-bottom: 10px;">入库明细</h4>
        <el-table :data="currentInbound.detailList" border stripe>
          <el-table-column prop="productName" label="商品名称" width="200" />
          <el-table-column prop="productCode" label="商品编码" width="150" />
          <el-table-column prop="productSpec" label="商品规格" width="150" />
          <el-table-column prop="productUnit" label="单位" width="80" align="center" />
          <el-table-column prop="quantity" label="入库数量" width="100" align="center" />
          <el-table-column prop="costPrice" label="成本价" width="100" align="right">
            <template #default="scope">
              {{ formatCurrency(scope.row.costPrice) }}
            </template>
          </el-table-column>
          <el-table-column prop="costAmount" label="成本金额" width="120" align="right">
            <template #default="scope">
              {{ formatCurrency(scope.row.costAmount) }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 新增入库单弹窗 -->
    <el-dialog v-model="addDialogVisible" title="选择采购申请表创建入库" width="70%">
      <el-form :model="purchaseQueryParams" ref="purchaseQueryForm" :inline="true">
        <el-form-item label="申请单号">
          <el-input v-model="purchaseQueryParams.requestNo" placeholder="请输入申请单号" clearable />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="purchaseQueryParams.supplierName" placeholder="请输入供应商" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchPurchaseList">查询</el-button>
          <el-button @click="resetPurchaseQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="purchaseList" style="width: 100%" v-loading="purchaseLoading" border stripe>
        <el-table-column prop="requestNo" label="申请单号" width="150" align="center" />
        <el-table-column prop="supplierName" label="供应商" min-width="150" show-overflow-tooltip />
        <el-table-column prop="purchaseOrderNo" label="采购单号" width="150" align="center" />
        <el-table-column prop="totalAmount" label="采购金额" width="120" align="right">
          <template #default="scope">
            {{ formatCurrency(scope.row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.auditStatus === 'PASSED' ? 'success' : 'warning'">
              {{ scope.row.auditStatus === 'PASSED' ? '已通过' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleSelectPurchase(scope.row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination v-model:current-page="purchaseCurrentPage" v-model:page-size="purchasePageSize"
          :page-sizes="[10, 20, 50]" :total="purchaseTotal" layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchPurchaseList" @current-change="fetchPurchaseList" />
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { getInboundList, getInboundById, auditInbound, createInboundFromPurchaseRequest, deleteInbound } from '@/api/inbound'
import { useRouter } from 'vue-router'
import { getPurchaseRequestList } from '@/api/purchase'
import { getWarehouseList } from '@/api/warehouse'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

// 权限检查函数
const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canCreate = computed(() => hasPermission('WHM:create'))
const canUpdate = computed(() => hasPermission('WHM:update'))
const canDelete = computed(() => hasPermission('WHM:delete'))
const canAudit = computed(() => hasPermission('WHM:audit'))

const tableData = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const currentInbound = ref<any>(null)
const warehouseList = ref<any[]>([])

// 新增入库单相关变量
const addDialogVisible = ref(false)
const purchaseList = ref<any[]>([])
const purchaseLoading = ref(false)
const purchaseCurrentPage = ref(1)
const purchasePageSize = ref(10)
const purchaseTotal = ref(0)

const queryParams = reactive({
  inboundNo: '',
  supplierName: '',
  auditStatus: ''
})

// 统计数据
const statistics = reactive({
  totalCount: 0,
  pendingCount: 0,
  totalAmount: 0,
  totalQuantity: 0
})

const purchaseQueryParams = reactive({
  requestNo: '',
  supplierName: '',
  auditStatus: 'PASSED'
})

const formatCurrency = (value: any) => {
  if (value === undefined || value === null) return '0.00 元'
  return `${parseFloat(value).toFixed(2)} 元`
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const getStatusType = (status: string) => {
  if (status === 'PASSED') return 'success'
  if (status === 'REJECTED') return 'danger'
  if (status === 'PENDING') return 'warning'
  return 'info'
}

const getStatusText = (status: string) => {
  if (status === 'PASSED') return '已入库'
  if (status === 'REJECTED') return '已驳回'
  if (status === 'PENDING') return '待审核'
  return status || '未知'
}

// 获取仓库名称
const getWarehouseName = (warehouseId: number) => {
  if (!warehouseId) return '-'
  const warehouse = warehouseList.value.find(w => w.id === warehouseId)
  return warehouse?.warehouseName || '-'
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      inboundType: 'PURCHASE',
      ...queryParams
    }
    const res: any = await getInboundList(params)
    tableData.value = res.records || []
    total.value = res.total || 0
    
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
  statistics.totalAmount = tableData.value.reduce((sum: number, item: any) => sum + (Number(item.totalCost) || 0), 0)
  statistics.totalQuantity = tableData.value.reduce((sum: number, item: any) => sum + (Number(item.totalQuantity) || 0), 0)
}

const handleQuery = () => {
  currentPage.value = 1
  fetchData()
}

const resetQuery = () => {
  queryParams.inboundNo = ''
  queryParams.supplierName = ''
  queryParams.auditStatus = ''
  handleQuery()
}

// 获取采购申请表列表
const fetchPurchaseList = async () => {
  purchaseLoading.value = true
  try {
    const params = {
      current: purchaseCurrentPage.value,
      size: purchasePageSize.value,
      ...purchaseQueryParams
    }
    const res: any = await getPurchaseRequestList(params)
    purchaseList.value = res.records || []
    purchaseTotal.value = res.total || 0
  } catch (error) {
    ElMessage.error('获取采购申请表列表失败')
    console.error('获取采购申请表列表失败:', error)
  } finally {
    purchaseLoading.value = false
  }
}

const resetPurchaseQuery = () => {
  purchaseQueryParams.requestNo = ''
  purchaseQueryParams.supplierName = ''
  purchaseCurrentPage.value = 1
  fetchPurchaseList()
}

// 加载仓库列表
const fetchWarehouseList = async () => {
  try {
    const res: any = await getWarehouseList({ current: 1, size: 100 })
    warehouseList.value = res.records || []
  } catch (error) {
    console.error('获取仓库列表失败:', error)
  }
}

const handleView = async (row: any) => {
  // 确保仓库列表已加载
  if (warehouseList.value.length === 0) {
    await fetchWarehouseList()
  }
  const res: any = await getInboundById(row.id!)
  currentInbound.value = res
  dialogVisible.value = true
}

const handleAudit = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认执行入库操作吗？这将增加库存。', '提示', { type: 'warning' })
    await auditInbound(row.id!, 'PASSED')
    ElMessage.success('入库成功')
    fetchData()
  } catch (error) {
    // 用户取消操作
  }
}

// 打开新增入库单弹窗
const handleAdd = () => {
  fetchPurchaseList()
  addDialogVisible.value = true
}

// 选择采购申请表创建入库单
const handleSelectPurchase = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确认根据采购申请表【${row.requestNo}】创建入库单吗？`,
      '提示',
      { type: 'warning' }
    )
    await createInboundFromPurchaseRequest(row.id!)
    ElMessage.success('入库单创建成功')
    addDialogVisible.value = false
    fetchData()
  } catch (error) {
    // 用户取消操作
  }
}

// 修改入库单
const handleEdit = (row: any) => {
  router.push(`/scm/inbound/purchase-inbound-edit?id=${row.id}`)
}

// 删除入库单
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该入库单吗？删除后不可恢复！', '提示', { type: 'warning' })
    await deleteInbound(row.id!)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    // 用户取消操作
  }
}

onMounted(() => {
  fetchData()
  fetchWarehouseList()
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
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
