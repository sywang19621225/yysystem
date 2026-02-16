<template>
  <div class="app-container">
    <h2 class="page-title">权限管理</h2>
    <div class="header-actions">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="权限编码">
          <el-input v-model="queryParams.permCode" placeholder="编码" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="资源">
          <el-input v-model="queryParams.resource" placeholder="资源" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button v-if="canCreate" type="primary" @click="handleAdd">新增权限</el-button>
    </div>
    <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
      <el-table-column prop="permCode" label="权限编码" width="200" />
      <el-table-column prop="permName" label="权限名称" width="200" />
      <el-table-column prop="resource" label="资源" width="220" />
      <el-table-column prop="action" label="动作" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="200" />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button v-if="canUpdate" link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button v-if="canDelete" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
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
        @size-change="onSizeChange"
        @current-change="fetchData"
      />
    </div>
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑权限' : '新增权限'" width="600px" :before-close="onDialogBeforeClose">
      <div class="dialog-top-actions">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </div>
      <el-form :model="form" label-width="100px">
        <el-form-item label="权限编码" required>
          <el-input v-model="form.permCode" placeholder="例如 system:user:list" />
        </el-form-item>
        <el-form-item label="权限名称" required>
          <el-input v-model="form.permName" placeholder="名称" />
        </el-form-item>
        <el-form-item label="资源">
          <el-input v-model="form.resource" placeholder="资源标识" />
        </el-form-item>
        <el-form-item label="动作">
          <el-select v-model="form.action" placeholder="动作">
            <el-option label="读取" value="READ" />
            <el-option label="写入" value="WRITE" />
            <el-option label="新增" value="CREATE" />
            <el-option label="修改" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="statusEnabled" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="备注" />
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { getPermissionList, savePermission, updatePermission, deletePermission } from '@/api/permission'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

// 权限检查函数
const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canCreate = computed(() => hasPermission('PM:create'))
const canUpdate = computed(() => hasPermission('PM:update'))
const canDelete = computed(() => hasPermission('PM:delete'))

const tableData = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const queryParams = reactive<any>({ permCode: '', resource: '' })

const form = reactive<any>({
  id: undefined,
  permCode: '',
  permName: '',
  resource: '',
  action: '',
  status: 1,
  remark: ''
})
const originalForm = ref<any>({})
const statusEnabled = computed({
  get: () => form.status === 1,
  set: (v: boolean) => { form.status = v ? 1 : 0 }
})

const fetchData = async () => {
  loading.value = true
  try {
    const params = { current: currentPage.value, size: pageSize.value, ...queryParams }
    const res: any = await getPermissionList(params)
    tableData.value = res.records || res.data?.records || []
    total.value = res.total || res.data?.total || 0
  } finally {
    loading.value = false
  }
}
const handleQuery = () => {
  currentPage.value = 1
  fetchData()
}
const resetQuery = () => {
  queryParams.permCode = ''
  queryParams.resource = ''
  handleQuery()
}
const handleAdd = () => {
  form.id = undefined
  form.permCode = ''
  form.permName = ''
  form.resource = ''
  form.action = ''
  form.status = 1
  form.remark = ''
  originalForm.value = JSON.parse(JSON.stringify(form))
  dialogVisible.value = true
}
const handleEdit = (row: any) => {
  Object.assign(form, row)
  originalForm.value = JSON.parse(JSON.stringify(form))
  dialogVisible.value = true
}
const handleSubmit = async () => {
  if (!form.permCode || !form.permName) {
    ElMessage.warning('请填写权限编码与名称')
    return
  }
  if (form.id) {
    await updatePermission(form)
    ElMessage.success('更新成功')
  } else {
    await savePermission(form)
    ElMessage.success('创建成功')
  }
  dialogVisible.value = false
  fetchData()
}
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该权限吗？', '提示', { type: 'warning' }).then(async () => {
    await deletePermission(row.id)
    ElMessage.success('删除成功')
    fetchData()
  })
}
const onSizeChange = (val: number) => {
  const oldSize = pageSize.value
  const oldPage = currentPage.value
  pageSize.value = val
  const startIndex = (oldPage - 1) * oldSize
  currentPage.value = Math.floor(startIndex / val) + 1
  fetchData()
}

onMounted(() => {
  fetchData()
  window.addEventListener('beforeunload', beforeUnloadHandler)
})
onUnmounted(() => {
  window.removeEventListener('beforeunload', beforeUnloadHandler)
})

const isFormChanged = () => {
  const a = JSON.stringify(form)
  const b = JSON.stringify(originalForm.value || {})
  return a !== b
}
const onDialogBeforeClose = async (done: () => void) => {
  if (!isFormChanged()) {
    done()
    return
  }
  try {
    await ElMessageBox.confirm('内容已修改，是否保存？', '提示', { confirmButtonText: '保存并关闭', cancelButtonText: '直接关闭' })
    await handleSubmit()
    done()
  } catch {
    done()
  }
}
const beforeUnloadHandler = (e: BeforeUnloadEvent) => {
  if (dialogVisible.value && isFormChanged()) {
    e.preventDefault()
    e.returnValue = ''
  }
}
</script>

<style scoped>
.app-container { padding: 20px; }
.header-actions { display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; }
.search-form { margin-bottom:-18px; }
.pagination-container { margin-top:16px; display:flex; justify-content:flex-end; }
.dialog-top-actions { display:flex; justify-content:flex-end; gap:8px; margin-bottom:10px; }
</style>
