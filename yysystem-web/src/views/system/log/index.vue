<template>
  <div class="app-container">
    <h2 class="page-title">操作日志</h2>
    <div class="toolbar">
      <el-button v-if="canExport" type="success" @click="handleExport">导出</el-button>
      <el-button v-if="canDelete" type="danger" @click="handleBatchDelete">批量删除</el-button>
    </div>
    <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" v-if="canDelete" />
      <el-table-column prop="operation" label="操作类型" width="150" align="center" />
      <el-table-column prop="content" label="操作内容" show-overflow-tooltip />
      <el-table-column prop="operator" label="操作人" width="120" align="center" />
      <el-table-column prop="operationTime" label="操作时间" width="200" align="center" />
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
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/modules/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()

// 权限检查函数
const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canDelete = computed(() => hasPermission('OL:delete'))
const canExport = computed(() => hasPermission('OL:export'))

const tableData = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedIds = ref<number[]>([])

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await request({
      url: '/system/log/list',
      method: 'get',
      params: { current: currentPage.value, size: pageSize.value }
    })
    if (res && res.records) {
      tableData.value = res.records
      total.value = res.total
    } else {
      tableData.value = []
      total.value = 0
    }
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleExport = () => {
  // 导出功能待实现
  ElMessage.success('导出功能开发中...')
}

const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的日志')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条日志吗？`, '提示', {
      type: 'warning'
    })
    await request({
      url: '/system/log/batch-delete',
      method: 'post',
      data: { ids: selectedIds.value }
    })
    ElMessage.success('删除成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.toolbar {
  margin-bottom: 16px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>