<template>
  <div class="app-container">
    <div class="header-actions">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="商品编号">
          <el-input v-model="queryParams.productCode" placeholder="请输入商品编号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button v-if="canExport" type="success" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
      <el-table-column prop="flowNo" label="流水号" width="180" align="center" />
      <el-table-column prop="productCode" label="商品编号" width="150" align="center" />
      <el-table-column prop="warehouseId" label="仓库" width="150" align="center">
          <template #default="scope">
              {{ getWarehouseName(scope.row.warehouseId) }}
          </template>
      </el-table-column>
      <el-table-column prop="flowType" label="类型" width="100" align="center">
        <template #default="scope">
            <el-tag :type="getFlowTypeTag(scope.row.flowType)">{{ getFlowTypeName(scope.row.flowType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="100" align="center">
           <template #default="scope">
              <span :style="{ color: scope.row.quantity > 0 ? 'green' : 'red' }">
                  {{ scope.row.quantity > 0 ? '+' + scope.row.quantity : scope.row.quantity }}
              </span>
           </template>
      </el-table-column>
      <el-table-column prop="beforeStock" label="变动前" width="100" align="center" />
      <el-table-column prop="afterStock" label="变动后" width="100" align="center" />
      <el-table-column prop="refNo" label="关联单号" width="180" align="center" />
      <el-table-column prop="createTime" label="时间" align="center" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { getStockFlowList } from '@/api/inventory'
import { getWarehouseList } from '@/api/warehouse'
import { useUserStore } from '@/store/modules/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

// 权限检查函数
const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canExport = computed(() => hasPermission('IOF:export'))

const tableData = ref<any[]>([])
const warehouseList = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const queryParams = reactive({
  productCode: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      ...queryParams
    }
    const res: any = await getStockFlowList(params)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const fetchWarehouses = async () => {
    const res: any = await getWarehouseList({ current: 1, size: 100 })
    warehouseList.value = res.records
}

const getWarehouseName = (id: number) => {
    const warehouse = warehouseList.value.find(w => w.id === id)
    return warehouse ? warehouse.warehouseName : id
}

const getFlowTypeTag = (type: string) => {
    switch (type) {
        case 'IN': return 'success'
        case 'OUT': return 'danger'
        case 'CHECK_IN': return 'warning'
        case 'CHECK_OUT': return 'warning'
        default: return 'info'
    }
}

const getFlowTypeName = (type: string) => {
    switch (type) {
        case 'IN': return '入库'
        case 'OUT': return '出库'
        case 'CHECK_IN': return '盘盈'
        case 'CHECK_OUT': return '盘亏'
        default: return type
    }
}

const handleQuery = () => {
  currentPage.value = 1
  fetchData()
}

const resetQuery = () => {
  queryParams.productCode = ''
  handleQuery()
}

const handleExport = () => {
  // 导出功能待实现
  ElMessage.success('导出功能开发中...')
}

onMounted(() => {
  fetchWarehouses()
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
