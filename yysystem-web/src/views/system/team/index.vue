<template>
  <div class="app-container">
    <h2 class="page-title">团队管理</h2>
    <div class="header-actions">
      <el-button v-if="canCreate" type="primary" @click="handleAdd">新增团队</el-button>
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
      <el-table-column prop="teamName" label="团队名称" />
      <el-table-column prop="managerName" label="主管" width="120" align="center" />
      <el-table-column prop="area" label="地区" width="120" align="center" />
      <el-table-column prop="teamDesc" label="描述" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="200" align="center" />
      <el-table-column label="操作" width="250" align="center">
        <template #default="scope">
          <el-button v-if="canCreate" link type="primary" @click="handleAddSub(scope.row)">新增下级</el-button>
          <el-button v-if="canUpdate" link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button v-if="canDelete" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑团队' : '新增团队'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="上级团队" v-if="form.parentId">
          <el-input :value="getParentName(form.parentId)" disabled />
        </el-form-item>
        <el-form-item label="团队名称" prop="teamName">
          <el-input v-model="form.teamName" />
        </el-form-item>
        <el-form-item label="主管" prop="managerId">
           <el-select v-model="form.managerId" placeholder="选择主管" filterable clearable>
             <el-option v-for="u in userList" :key="u.id" :label="u.name" :value="u.id" />
           </el-select>
        </el-form-item>
        <el-form-item label="成员" prop="memberIds">
           <el-select v-model="form.memberIds" placeholder="选择成员" multiple filterable clearable>
             <el-option v-for="u in userList" :key="u.id" :label="u.name" :value="String(u.id)" />
           </el-select>
        </el-form-item>
        <el-form-item label="地区" prop="area">
          <el-input v-model="form.area" />
        </el-form-item>
        <el-form-item label="描述" prop="teamDesc">
          <el-input type="textarea" v-model="form.teamDesc" />
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

const canCreate = computed(() => hasPermission('TM:create'))
const canUpdate = computed(() => hasPermission('TM:update'))
const canDelete = computed(() => hasPermission('TM:delete'))

const tableData = ref<any[]>([])
const userList = ref<any[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<any>({
  id: undefined,
  teamName: '',
  parentId: 0,
  managerId: undefined,
  memberIds: [],
  area: '',
  teamDesc: ''
})

const rules = reactive<FormRules>({
  teamName: [{ required: true, message: '请输入团队名称', trigger: 'blur' }],
  managerId: [{ required: true, message: '请选择团队主管', trigger: 'change' }]
})

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await request({
      url: '/system/team/tree',
      method: 'get'
    })
    tableData.value = res
  } finally {
    loading.value = false
  }
}

const fetchUsers = async () => {
  const res: any = await request({
    url: '/system/user/list',
    method: 'get',
    params: { current: 1, size: 1000 }
  })
  userList.value = res.records
}

const getParentName = (parentId: number) => {
  return `父级ID: ${parentId}`
}

const handleAdd = () => {
  form.id = undefined
  form.teamName = ''
  form.parentId = 0
  form.managerId = undefined
  form.memberIds = []
  form.area = ''
  form.teamDesc = ''
  dialogVisible.value = true
}

const handleAddSub = (row: any) => {
  form.id = undefined
  form.teamName = ''
  form.parentId = row.id
  form.managerId = undefined
  form.memberIds = []
  form.area = ''
  form.teamDesc = ''
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  Object.assign(form, row)
  // Handle memberIds conversion from string to array for select
  if (row.memberIds) {
    form.memberIds = row.memberIds.split(',')
  } else {
    form.memberIds = []
  }
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该团队吗？', '提示', { type: 'warning' }).then(async () => {
    await request({
      url: `/system/team/${row.id}`,
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
        // Clone form to avoid modifying the reactive object directly if we were to change types
        const submitData = { ...form }
        
        // Convert memberIds array to comma-separated string
        if (Array.isArray(submitData.memberIds)) {
            submitData.memberIds = submitData.memberIds.join(',')
        }

        if (form.id) {
          await request({
            url: '/system/team',
            method: 'put',
            data: submitData
          })
          ElMessage.success('更新成功')
        } else {
          await request({
            url: '/system/team',
            method: 'post',
            data: submitData
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
  fetchUsers()
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