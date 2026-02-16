<template>
  <div class="app-container">
    <h2 class="page-title">出库单</h2>
    <div class="header-actions">
      <el-form :inline="true" :model="queryParams" class="search-form" @submit.prevent>
        <el-form-item label="出库单号">
          <el-input v-model="queryParams.outStockNo" placeholder="请输入出库单号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div style="display: flex; gap: 8px;">
        <el-button type="warning" @click="handleBatchPrint" :disabled="!selectedRows.length">批量打印标签</el-button>
        <el-button v-if="canCreate" type="primary" @click="handleCreate">新建出库单</el-button>
      </div>
    </div>

    <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column prop="outStockNo" label="出库单号" width="180" align="center" />
      <el-table-column prop="outboundCategory" label="出库分类" width="100" align="center">
        <template #default="scope">
          <el-tag type="info">{{ scope.row.outboundCategory || '销售' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="contractName" label="合同名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="customerName" label="终端客户" min-width="150" show-overflow-tooltip />
      <el-table-column prop="customerAddress" label="客户地址" min-width="200" show-overflow-tooltip />
      <el-table-column prop="actualShipTime" label="出库日期" width="160" align="center" />
      <el-table-column prop="auditStatus" label="状态" width="120" align="center">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.auditStatus)">
            {{ getStatusText(scope.row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" align="center">
        <template #default="scope">
          <div style="display:flex; align-items:center; gap:12px; justify-content:center; white-space:nowrap;">
            <template v-if="scope.row.auditStatus === 'PENDING' && canAudit">
              <el-button link type="primary" @click="handleAudit(scope.row)">审核</el-button>
            </template>
            <template v-else-if="scope.row.auditStatus !== 'PENDING'">
              <span class="text-gray">已审核</span>
            </template>
            <el-button link type="primary" @click="gotoDetail(scope.row)">详情</el-button>
            <el-button v-if="canDelete" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </div>
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

    <!-- 第一步：选择设备弹窗 -->
    <el-dialog v-model="selectDeviceDialogVisible" title="选择要打印的设备" width="900px">
      <div style="margin-bottom: 16px;">
        <el-alert type="info" :closable="false">
          <template #title>
            <div>已选择 {{ selectedDevices.length }} 个设备，共 {{ allDevices.length }} 个设备，每页显示 {{ devicePageSize }} 条</div>
          </template>
        </el-alert>
      </div>
      <!-- 设备选择器 -->
      <div style="margin-bottom: 16px; display: flex; align-items: center; gap: 10px;">
        <span>按设备筛选：</span>
        <el-select v-model="selectedDeviceFilter" placeholder="请选择设备（留空显示全部）" clearable multiple collapse-tags style="width: 400px" @change="handleDeviceFilterChange">
          <el-option
            v-for="device in uniqueDevices"
            :key="device.productCode"
            :label="device.productName + ' (' + device.productCode + ')'"
            :value="device.productCode"
          />
        </el-select>
        <el-button type="primary" @click="selectAllByDevice" :disabled="!selectedDeviceFilter || selectedDeviceFilter.length === 0">选中所选设备全部</el-button>
        <el-button @click="clearSelection">清空选择</el-button>
      </div>
      <el-table ref="deviceTableRef" :data="displayedDevices" border stripe @selection-change="handleDeviceSelectionChange" height="500">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="productCode" label="商品编号" width="120" />
        <el-table-column prop="productSpec" label="规格" min-width="120" />
        <el-table-column prop="productQrCode" label="二维码编号" min-width="150" />
        <el-table-column prop="outStockNo" label="出库单号" width="140" />
        <el-table-column prop="customerName" label="客户" min-width="120" />
      </el-table>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="deviceCurrentPage"
          v-model:page-size="devicePageSize"
          :page-sizes="[8, 16, 24, 32]"
          :total="deviceTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleDevicePageChange"
          @current-change="handleDevicePageChange"
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="selectDeviceDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="previewPrint" :disabled="!selectedDevices.length">预览打印 ({{ selectedDevices.length }})</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 第二步：预览打印弹窗 -->
    <el-dialog v-model="previewPrintDialogVisible" title="预览标签" width="1400px">
      <div style="margin-bottom: 16px;">
        <el-alert type="info" :closable="false">
          <template #title>
            <div>共 {{ selectedDevices.length }} 个标签，每页显示 {{ previewPageSize }} 个标签，当前第 {{ previewCurrentPage }} 页</div>
          </template>
        </el-alert>
      </div>
      <div id="batch-print-content">
        <div class="label-container">
          <div v-for="(item, index) in displayedPreviewDevices" :key="index" class="label-item">
            <div class="label-content">
              <div class="label-info">
                <p class="product-name">{{ item.productName }}</p>
                <p>规格：{{ item.productSpec }}</p>
                <p>出厂日期：{{ item.manufactureDate || '-' }}</p>
                <p>制造商：{{ item.manufacturer || '-' }}</p>
                <p>联系电话：{{ item.contactPhone || '-' }}</p>
                <p>网址：{{ item.website || '-' }}</p>
              </div>
              <div class="label-qr">
                <img :src="`https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${encodeURIComponent(item.productQrCode)}`" alt="二维码" />
                <div class="label-qr-code">编号：{{ item.productQrCode }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="previewCurrentPage"
          v-model:page-size="previewPageSize"
          :page-sizes="[8, 16, 24, 32]"
          :total="selectedDevices.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handlePreviewPageChange"
          @current-change="handlePreviewPageChange"
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="previewPrintDialogVisible = false; selectDeviceDialogVisible = true">返回选择</el-button>
          <el-button type="primary" @click="goToPrintPage">前往打印页面</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { getOutboundList, auditOutbound, deleteOutbound, getOutboundDetails } from '@/api/outbound'
import { getSupplierList } from '@/api/supplier'
import { getProductList } from '@/api/product'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'

const tableData = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const router = useRouter()
const userStore = useUserStore()

// 判断是否为管理员
const isAdmin = computed(() => {
  const ui: any = userStore.userInfo
  const type = ui?.userType
  const username = ui?.username
  const roleId = ui?.roleId
  const roleName = ui?.roleName
  return type === 'admin'
    || username === 'admin'
    || roleId === 1
    || String(roleName || '').includes('管理员')
})

// 判断是否有指定权限码
const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo
  if (!ui) return false
  const permissionCodes = ui?.permissionCodes || []
  return isAdmin.value || permissionCodes.includes(permCode)
}

// 权限控制
const canCreate = computed(() => hasPermission('OM:create'))
const canDelete = computed(() => hasPermission('OM:delete'))
const canAudit = computed(() => hasPermission('OM:audit') || isAdmin.value)

// 批量打印
const selectedRows = ref<any[]>([])
const selectDeviceDialogVisible = ref(false)
const previewPrintDialogVisible = ref(false)
const allDevices = ref<any[]>([])
const selectedDevices = ref<any[]>([])
// 设备选择分页
const deviceCurrentPage = ref(1)
const devicePageSize = ref(8)
const deviceTotal = ref(0)
const displayedDevices = ref<any[]>([])
// 设备筛选
const selectedDeviceFilter = ref<string[]>([])
const uniqueDevices = ref<any[]>([])
const deviceTableRef = ref<any>(null)
// 预览分页
const previewCurrentPage = ref(1)
const previewPageSize = ref(8)
const displayedPreviewDevices = ref<any[]>([])

const queryParams = reactive({
  outStockNo: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      outStockType: 'SALES',
      ...queryParams
    }
    const res: any = await getOutboundList(params)
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
  queryParams.outStockNo = ''
  handleQuery()
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'PENDING': return 'warning'
    case 'PASSED': return 'success'
    case 'REJECTED': return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'PENDING': return '待审核'
    case 'PASSED': return '已通过'
    case 'REJECTED': return '已驳回'
    default: return status
  }
}

const handleAudit = (row: any) => {
  ElMessageBox.confirm('确认审核通过该出库单吗？审核通过后将扣减库存。', '审核', {
    confirmButtonText: '通过',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await auditOutbound(row.id, 'PASSED', '人工审核通过')
    ElMessage.success('审核成功')
    fetchData()
  })
}
const gotoDetail = (row:any) => {
  router.push({ path: '/scm/outbound/sales-outbound-detail', query: { id: row.id } })
}
const handleCreate = async () => {
  router.push({ path: '/scm/outbound/sales-outbound-detail' })
}
const handleDelete = (row:any) => {
  ElMessageBox.confirm('确认删除该出库单吗？将同时删除其明细。', '提示', { type: 'warning' }).then(async () => {
    await deleteOutbound(row.id)
    ElMessage.success('删除成功')
    fetchData()
  })
}

onMounted(() => {
  fetchData()
})

// 表格选择变化
const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

// 批量打印标签
const handleBatchPrint = async () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请选择要打印的出库单')
    return
  }

  // 先获取所有商品和供应商信息，建立映射
  let productMap: Map<string, any> = new Map()
  let supplierMap: Map<number, any> = new Map()
  
  try {
    // 获取所有商品
    const productRes: any = await getProductList({ current: 1, size: 1000 })
    const products = productRes?.records || productRes?.data?.records || []
    console.log('获取到商品数量:', products.length)
    
    // 建立商品编码到商品的映射
    products.forEach((p: any) => {
      if (p.productCode) {
        productMap.set(p.productCode, p)
      }
    })
    console.log('商品编码映射数量:', productMap.size)
    
    // 获取所有供应商
    const supplierRes: any = await getSupplierList({ current: 1, size: 1000 })
    const suppliers = supplierRes?.records || supplierRes?.data?.records || []
    console.log('获取到供应商数量:', suppliers.length)
    
    // 建立供应商ID到供应商的映射
    suppliers.forEach((s: any) => {
      if (s.id) {
        supplierMap.set(Number(s.id), s)
      }
    })
    console.log('供应商映射数量:', supplierMap.size)
  } catch (e) {
    console.error('获取商品或供应商列表失败:', e)
  }

  // 获取选中出库单的所有明细
  const allDetails: any[] = []
  for (const row of selectedRows.value) {
    try {
      const res: any = await getOutboundDetails(row.id)
      const details = Array.isArray(res) ? res : (res?.records || res?.data || [])
      // 只添加有二维码的明细
      for (const d of details) {
        if (d.productQrCode) {
          // 通过商品编码获取供应商信息
          let supplierInfo = null
          console.log('处理明细:', d.productCode, d.productName)
          
          if (d.productCode) {
            const product = productMap.get(d.productCode)
            console.log('查找到商品:', product?.productCode, '供应商ID:', product?.supplierId)
            
            if (product && product.supplierId) {
               const supplierId = Number(product.supplierId)
               console.log('查找供应商ID:', supplierId, '类型:', typeof supplierId)
               console.log('供应商Map中的keys:', Array.from(supplierMap.keys()).slice(0, 10))
               supplierInfo = supplierMap.get(supplierId)
               console.log('查找到供应商:', supplierInfo?.supplierName, supplierInfo)
             }
          }
          
          allDetails.push({
            ...d,
            outStockNo: row.outStockNo,
            contractName: row.contractName,
            customerName: row.customerName,
            manufactureDate: row.actualShipTime || row.outTime || '-',
            manufacturer: supplierInfo?.supplierName || supplierInfo?.name || '-',
            contactPhone: supplierInfo?.contactPhone || supplierInfo?.phone || '-',
            website: supplierInfo?.website || '-'
          })
        }
      }
    } catch (e) {
      console.error(`获取出库单 ${row.id} 明细失败`, e)
    }
  }

  if (!allDetails.length) {
    ElMessage.warning('选中的出库单中没有可打印的明细（需要有二维码）')
    return
  }

  allDevices.value = allDetails
  selectedDeviceFilter.value = []
  calculateUniqueDevices()
  deviceCurrentPage.value = 1
  updateDisplayedDevices()
  selectedDevices.value = []
  selectDeviceDialogVisible.value = true
}

// 更新显示的设备（分页）
const updateDisplayedDevices = () => {
  let filteredDevices = allDevices.value
  // 如果有设备筛选，先过滤（支持多选）
  if (selectedDeviceFilter.value && selectedDeviceFilter.value.length > 0) {
    filteredDevices = filteredDevices.filter(d => selectedDeviceFilter.value.includes(d.productCode))
  }
  deviceTotal.value = filteredDevices.length
  const start = (deviceCurrentPage.value - 1) * devicePageSize.value
  const end = start + devicePageSize.value
  displayedDevices.value = filteredDevices.slice(start, end)
}

// 设备分页变化
const handleDevicePageChange = () => {
  updateDisplayedDevices()
}

// 计算唯一的设备列表（用于下拉选择）
const calculateUniqueDevices = () => {
  const deviceMap = new Map()
  allDevices.value.forEach(d => {
    if (!deviceMap.has(d.productCode)) {
      deviceMap.set(d.productCode, {
        productCode: d.productCode,
        productName: d.productName
      })
    }
  })
  uniqueDevices.value = Array.from(deviceMap.values())
}

// 设备筛选变化
const handleDeviceFilterChange = () => {
  deviceCurrentPage.value = 1
  updateDisplayedDevices()
}

// 选中该设备的全部记录
const selectAllByDevice = () => {
  if (!selectedDeviceFilter.value || selectedDeviceFilter.value.length === 0) return
  // 找到所选设备的所有记录
  const deviceRecords = allDevices.value.filter(d => selectedDeviceFilter.value.includes(d.productCode))
  // 添加到已选择列表
  const existingCodes = new Set(selectedDevices.value.map(d => d.productQrCode))
  let addedCount = 0
  deviceRecords.forEach(record => {
    if (!existingCodes.has(record.productQrCode)) {
      selectedDevices.value.push(record)
      addedCount++
    }
  })
  const deviceNames = selectedDeviceFilter.value.join(', ')
  ElMessage.success(`已选中 ${addedCount} 个设备（${deviceNames}）`)
}

// 清空选择
const clearSelection = () => {
  selectedDevices.value = []
  if (deviceTableRef.value) {
    deviceTableRef.value.clearSelection()
  }
}

// 设备选择变化
const handleDeviceSelectionChange = (rows: any[]) => {
  selectedDevices.value = rows
}

// 预览打印
const previewPrint = () => {
  if (!selectedDevices.value.length) {
    ElMessage.warning('请至少选择一个设备')
    return
  }
  selectDeviceDialogVisible.value = false
  previewCurrentPage.value = 1
  updateDisplayedPreviewDevices()
  previewPrintDialogVisible.value = true
}

// 更新预览显示的设备（分页）
const updateDisplayedPreviewDevices = () => {
  const start = (previewCurrentPage.value - 1) * previewPageSize.value
  const end = start + previewPageSize.value
  displayedPreviewDevices.value = selectedDevices.value.slice(start, end)
}

// 预览分页变化
const handlePreviewPageChange = () => {
  updateDisplayedPreviewDevices()
}

// 跳转到打印页面
const goToPrintPage = () => {
  // 将选中的设备数据存储到 sessionStorage
  sessionStorage.setItem('printLabels', JSON.stringify(selectedDevices.value))
  // 打开新页面进行打印
  window.open('/scm/outbound/label-print', '_blank')
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.page-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #303133;
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
.tips {
  color: #909399;
  font-size: 14px;
}
.text-gray {
  color: #909399;
}

/* 打印标签预览样式 */
#batch-print-content {
  width: 100%;
  overflow-x: auto;
}
.label-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(4, 1fr);
  gap: 10px;
  width: 100%;
  max-width: 210mm;
  aspect-ratio: 210 / 297;
  padding: 15px;
  background: #f5f5f5;
  margin: 0 auto;
  box-sizing: border-box;
}
.label-item {
  border: 2px solid #333;
  padding: 10px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-sizing: border-box;
  height: 100%;
  background: white;
  min-height: 0;
}
.label-content {
  display: flex;
  gap: 4mm;
  flex: 1;
  align-items: center;
}
.label-info {
  flex: 1;
  font-size: 14px;
  line-height: 1.5;
}
.label-info p {
  margin: 3px 0;
}
.label-info .product-name {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 8px;
  color: #000;
}
.label-qr {
  text-align: center;
  width: 40mm;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.label-qr img {
  width: 35mm;
  height: 35mm;
}
.label-qr-code {
  font-size: 12px;
  color: #333;
  margin-top: 4px;
  word-break: break-all;
  font-weight: 500;
}
</style>
