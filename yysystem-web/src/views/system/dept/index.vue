<template>
  <div class="app-container">
    <h2 class="page-title">部门管理</h2>
    <div class="header-actions">
      <el-button v-if="canCreate" type="primary" @click="handleAdd">新增部门</el-button>
    </div>

    <el-table
      :data="tableData"
      style="width: 100%"
      v-loading="loading"
      row-key="id"
      border
      default-expand-all
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    >
      <el-table-column prop="deptName" label="部门名称" />
      <el-table-column prop="createTime" label="创建时间" width="200" align="center" />
      <el-table-column label="操作" width="250" align="center">
        <template #default="scope">
          <el-button v-if="canCreate" link type="primary" @click="handleAddSub(scope.row)">新增下级</el-button>
          <el-button v-if="canUpdate" link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button v-if="canDelete" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑部门' : '新增部门'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="上级部门" v-if="form.parentId">
          <el-input :value="getParentName(form.parentId)" disabled />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit(formRef)">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

// 权限检查函数
const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canCreate = computed(() => hasPermission('DM:create'))
const canUpdate = computed(() => hasPermission('DM:update'))
const canDelete = computed(() => hasPermission('DM:delete'))

const tableData = ref<any[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<any>({
  id: undefined,
  deptName: '',
  parentId: 0
})

const rules = reactive<FormRules>({
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
})

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await request({
      url: '/system/dept/tree',
      method: 'get'
    })
    tableData.value = res
  } finally {
    loading.value = false
  }
}

const getParentName = (parentId: number) => {
  // Simple search in tableData or flatten list. For now just ID or Mock
  return `父级ID: ${parentId}`
}

const handleAdd = () => {
  form.id = undefined
  form.deptName = ''
  form.parentId = 0
  dialogVisible.value = true
}

const handleAddSub = (row: any) => {
  form.id = undefined
  form.deptName = ''
  form.parentId = row.id
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  form.id = row.id
  form.deptName = row.deptName
  form.parentId = row.parentId
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该部门吗？', '提示', { type: 'warning' }).then(async () => {
    await request({
      url: `/system/dept/${row.id}`,
      method: 'delete'
    })
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleSubmit = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      try {
        if (form.id) {
          await request({
            url: '/system/dept',
            method: 'put',
            data: form
          })
          ElMessage.success('更新成功')
        } else {
          await request({
            url: '/system/dept',
            method: 'post',
            data: form
          })
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (e) {
      }
    }
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
  margin-bottom: 20px;
}
</style>