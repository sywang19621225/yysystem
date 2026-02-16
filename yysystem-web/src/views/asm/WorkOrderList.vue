<template>
  <div class="app-container">
    <div class="header-actions">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="工单编号">
          <el-input v-model="queryParams.workOrderNo" placeholder="请输入工单编号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="handleAdd">新增工单</el-button>
    </div>

    <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
      <el-table-column prop="workOrderNo" label="工单编号" width="180" align="center" />
      <el-table-column prop="customerName" label="客户" width="120" align="center" />
      <el-table-column prop="productName" label="产品名称" width="150" align="center" />
      <el-table-column prop="problemDesc" label="故障内容" show-overflow-tooltip />
      <el-table-column prop="priority" label="优先级" width="100" align="center">
        <template #default="scope">
          <el-tag :type="getPriorityType(scope.row.priority)">
            {{ getPriorityText(scope.row.priority) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120" align="center">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="isEnabled" label="启用/禁用" width="100" align="center">
        <template #default="scope">
           <el-tag :type="scope.row.isEnabled === 1 ? 'success' : 'danger'">
            {{ scope.row.isEnabled === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <el-button link type="primary" @click="handleEdit(scope.row)">处理/详情</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
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

    <WorkOrderForm v-model="dialogVisible" :id="currentId" @success="fetchData" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getWorkOrderList, deleteWorkOrder } from '@/api/asm'
import { ElMessage, ElMessageBox } from 'element-plus'
import WorkOrderForm from './WorkOrderForm.vue'

const tableData = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const currentId = ref<number | undefined>(undefined)

const queryParams = reactive({
  workOrderNo: ''
})

const getPriorityType = (priority: string) => {
  if (priority === 'HIGH') return 'danger'
  if (priority === 'MEDIUM') return 'warning'
  return 'info'
}

const getPriorityText = (priority: string) => {
  if (priority === 'HIGH') return '高'
  if (priority === 'MEDIUM') return '中'
  return '低'
}

const getStatusType = (status: string) => {
  if (status === 'COMPLETED') return 'success'
  if (status === 'CANCELLED') return 'info'
  if (status === 'PROCESSING') return 'primary'
  return 'warning' // PENDING
}

const getStatusText = (status: string) => {
  if (status === 'COMPLETED') return '已完成'
  if (status === 'CANCELLED') return '已取消'
  if (status === 'PROCESSING') return '处理中'
  if (status === 'PENDING') return '待处理'
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
    const res: any = await getWorkOrderList(params)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  currentPage.value = 1
  fetchData()
}

const resetQuery = () => {
  queryParams.workOrderNo = ''
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
  ElMessageBox.confirm('确认删除该工单吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteWorkOrder(row.id!)
    ElMessage.success('删除成功')
    fetchData()
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
