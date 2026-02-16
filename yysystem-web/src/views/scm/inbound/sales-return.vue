<template>
  <div class="app-container">
    <div class="header-actions">
      <h2>销售退货入库</h2>
      <el-button v-if="canCreate" type="primary" @click="handleAdd">新增退货入库</el-button>
    </div>
    
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" class="search-form">
      <el-form-item label="入库单号">
        <el-input v-model="queryParams.inboundNo" placeholder="请输入入库单号" clearable />
      </el-form-item>
      <el-form-item label="审核状态">
        <el-select v-model="queryParams.auditStatus" placeholder="请选择状态" clearable>
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="PASSED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    
    <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
      <el-table-column prop="inboundNo" label="入库单号" width="180" align="center" />
      <el-table-column prop="inboundType" label="入库类型" width="120" align="center">
        <template #default>
          <el-tag type="warning">销售退货</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="inboundTime" label="入库时间" width="180" align="center" />
      <el-table-column prop="totalCost" label="退货金额" width="120" align="right">
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
      <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
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
    
    <!-- 新增退货入库弹窗 -->
    <el-dialog
      v-model="addDialogVisible"
      title="选择发货单创建退货入库"
      width="70%"
    >
      <el-form :model="queryOutboundParams" ref="outboundQueryForm" :inline="true">
        <el-form-item label="发货单号">
          <el-input v-model="queryOutboundParams.outStockNo" placeholder="请输入发货单号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchOutboundList">查询</el-button>
          <el-button @click="resetOutboundQuery">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="outboundList" style="width: 100%" v-loading="outboundLoading" border stripe>
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="outStockNo" label="发货单号" width="180" align="center" />
        <el-table-column prop="outStockDate" label="发货日期" width="150" align="center" />
        <el-table-column prop="customerId" label="客户" width="150" align="center">
          <template #default="scope">
            {{ getCustomerName(scope.row.customerId) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalSales" label="发货金额" width="120" align="right">
          <template #default="scope">
            {{ formatCurrency(scope.row.totalSales) }}
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.auditStatus === 'PASSED' ? 'success' : 'warning'">
              {{ scope.row.auditStatus === 'PASSED' ? '已审核' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleSelectOutbound(scope.row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="outboundCurrentPage"
          v-model:page-size="outboundPageSize"
          :page-sizes="[10, 20, 50]"
          :total="outboundTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchOutboundList"
          @current-change="fetchOutboundList"
        />
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 详情弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="退货入库单详情"
      width="80%"
    >
      <div v-if="currentInbound">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="入库单号">{{ currentInbound.inboundNo }}</el-descriptions-item>
          <el-descriptions-item label="入库类型">
            <el-tag type="warning">销售退货</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="仓库">{{ getWarehouseName(currentInbound.warehouseId) }}</el-descriptions-item>
          <el-descriptions-item label="退货金额">{{ formatCurrency(currentInbound.totalCost) }}</el-descriptions-item>
          <el-descriptions-item label="入库时间">{{ formatDate(currentInbound.inboundTime) }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">{{ getStatusText(currentInbound.auditStatus) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentInbound.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
        
        <h4 style="margin-top: 20px; margin-bottom: 10px;">退货明细</h4>
        <el-table :data="currentInbound.detailList" border stripe>
          <el-table-column prop="productName" label="商品名称" width="200" />
          <el-table-column prop="productCode" label="商品编码" width="150" />
          <el-table-column prop="productSpec" label="商品规格" width="150" />
          <el-table-column prop="productUnit" label="单位" width="80" align="center" />
          <el-table-column prop="quantity" label="退货数量" width="100" align="center" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { getInboundList, getInboundById, auditInbound, createInboundFromSalesReturn, deleteInbound } from '@/api/inbound'
import { getWarehouseList } from '@/api/warehouse'
import { useRouter } from 'vue-router'
import { getOutboundList } from '@/api/outbound'
import { getCustomerList } from '@/api/customer'
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

// 新增退货入库相关变量
const addDialogVisible = ref(false)
const outboundList = ref<any[]>([])
const outboundLoading = ref(false)
const outboundCurrentPage = ref(1)
const outboundPageSize = ref(10)
const outboundTotal = ref(0)
const customerList = ref<any[]>([])

const queryParams = reactive({
  inboundNo: '',
  auditStatus: ''
})

const queryOutboundParams = reactive({
  outStockNo: '',
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

const getCustomerName = (customerId: number) => {
  const customer = customerList.value.find(c => c.id === customerId)
  return customer ? customer.customerName : '-'
}

// 获取仓库名称
const getWarehouseName = (warehouseId: number) => {
  if (!warehouseId) return '-'
  const warehouse = warehouseList.value.find(w => w.id === warehouseId)
  return warehouse?.warehouseName || '-'
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

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      inboundType: 'SALES_RETURN',
      ...queryParams
    }
    const res: any = await getInboundList(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  currentPage.value = 1
  fetchData()
}

const resetQuery = () => {
  queryParams.inboundNo = ''
  queryParams.auditStatus = ''
  handleQuery()
}

// 获取客户列表
const fetchCustomerList = async () => {
  try {
    const res: any = await getCustomerList({ current: 1, size: 1000 })
    customerList.value = res.records || []
  } catch (error) {
    console.error('获取客户列表失败:', error)
  }
}

// 获取发货单列表
const fetchOutboundList = async () => {
  outboundLoading.value = true
  try {
    const params = {
      current: outboundCurrentPage.value,
      size: outboundPageSize.value,
      ...queryOutboundParams
    }
    const res: any = await getOutboundList(params)
    outboundList.value = res.records || []
    outboundTotal.value = res.total || 0
  } finally {
    outboundLoading.value = false
  }
}

const resetOutboundQuery = () => {
  queryOutboundParams.outStockNo = ''
  outboundCurrentPage.value = 1
  fetchOutboundList()
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
    await ElMessageBox.confirm('确认执行退货入库操作吗？这将增加库存。', '提示', { type: 'warning' })
    await auditInbound(row.id!, 'PASSED')
    ElMessage.success('退货入库成功')
    fetchData()
  } catch (error) {
    // 用户取消操作
  }
}

// 打开新增退货入库弹窗
const handleAdd = () => {
  fetchOutboundList()
  addDialogVisible.value = true
}

// 选择发货单创建退货入库
const handleSelectOutbound = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认根据发货单【${row.outStockNo}】创建退货入库单吗？`, '提示', { type: 'warning' })
    await createInboundFromSalesReturn(row.id!)
    ElMessage.success('退货入库单创建成功')
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
  fetchCustomerList()
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
