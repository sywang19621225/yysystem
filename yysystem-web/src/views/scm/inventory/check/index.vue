<template>
  <div class="app-container">
    <h2 class="page-title">库存盘点管理</h2>
    <div class="header-actions">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="盘点单号">
          <el-input v-model="queryParams.checkNo" placeholder="请输入盘点单号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button v-if="canCreate" type="primary" @click="handleAdd">新增盘点</el-button>
    </div>

    <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
      <el-table-column prop="checkNo" label="盘点单号" width="180" align="center" />
      <el-table-column prop="warehouseId" label="仓库" width="150" align="center">
         <template #default="scope">
           {{ getWarehouseName(scope.row.warehouseId) }}
         </template>
      </el-table-column>
      <el-table-column prop="checkDate" label="盘点日期" width="120" align="center" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'COMPLETED' ? 'success' : 'warning'">
            {{ scope.row.status === 'COMPLETED' ? '已完成' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <el-button v-if="canUpdate && scope.row.status !== 'COMPLETED'" link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button link type="primary" @click="handleDetail(scope.row)" v-else>详情</el-button>
          <el-button v-if="canDelete && scope.row.status !== 'COMPLETED'" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
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

    <InventoryCheckForm v-model="dialogVisible" :id="currentId" @success="fetchData" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { getCheckList, deleteCheck } from '@/api/inventory'
import { getWarehouseList } from '@/api/warehouse'
import { ElMessage, ElMessageBox } from 'element-plus'
import InventoryCheckForm from './InventoryCheckForm.vue'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

// 权限检查函数
const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canCreate = computed(() => hasPermission('IC:create'))
const canUpdate = computed(() => hasPermission('IC:update'))
const canDelete = computed(() => hasPermission('IC:delete'))

const tableData = ref<any[]>([])
const warehouseList = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const currentId = ref<number | undefined>(undefined)

const queryParams = reactive({
  checkNo: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      ...queryParams
    }
    const res: any = await getCheckList(params)
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

const handleQuery = () => {
  currentPage.value = 1
  fetchData()
}

const resetQuery = () => {
  queryParams.checkNo = ''
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

const handleDetail = (row: any) => {
  currentId.value = row.id
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该盘点单吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteCheck(row.id)
    ElMessage.success('删除成功')
    fetchData()
  })
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
