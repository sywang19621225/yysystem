<template>
  <div class="app-container">
    <h2 class="page-title">用户管理</h2>
    <div class="header-actions">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button v-if="hasPermission('system:user:add')" type="primary" @click="handleAdd">新增用户</el-button>
    </div>

    <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
      <el-table-column prop="username" label="用户名" width="120" align="center" />
      <el-table-column prop="name" label="姓名" width="120" align="center" />
      <el-table-column prop="userType" label="用户类型" width="120" align="center">
        <template #default="scope">
          {{ getUserTypeLabel(scope.row.userType) }}
        </template>
      </el-table-column>
      <el-table-column prop="roleName" label="角色" width="120" align="center" />
      <el-table-column prop="deptName" label="部门" width="120" align="center" />
      <el-table-column prop="teamName" label="团队" width="120" align="center" />
      <el-table-column prop="companyName" label="所属公司" width="150" align="center" />
      <el-table-column prop="phone" label="手机号" width="120" align="center" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button v-if="hasPermission('system:user:edit')" link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button v-if="hasPermission('system:user:delete')" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          <el-button v-if="isAdmin" link type="warning" @click="openPwdDialog(scope.row)">修改密码</el-button>
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

    <!-- Dialog for Add/Edit -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="600px" :before-close="onDialogBeforeClose">
      <div class="dialog-top-actions">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit(formRef)">保存</el-button>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="请选择" style="width: 100%" clearable @change="handleRoleChange">
            <el-option v-for="item in roleList" :key="item.id" :label="item.roleName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="form.userType" placeholder="请选择" style="width: 100%" clearable>
            <el-option label="管理员" value="admin" />
            <el-option label="业务员" value="sales" />
            <el-option label="财务" value="finance" />
            <el-option label="采购" value="purchase" />
            <el-option label="库管" value="stock" />
            <el-option label="售后" value="after_sales" />
            <el-option label="技术" value="tech" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门" prop="deptId">
          <el-select v-model="form.deptId" placeholder="请选择" style="width: 100%" clearable>
            <el-option v-for="item in deptList" :key="item.id" :label="item.deptName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="团队" prop="teamId">
          <el-select v-model="form.teamId" placeholder="请选择" style="width: 100%" clearable>
            <el-option v-for="item in teamList" :key="item.id" :label="item.teamName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属公司" prop="companyId">
          <el-select v-model="form.companyId" placeholder="请选择" style="width: 100%" clearable filterable>
            <el-option v-for="item in companyList" :key="item.id" :label="item.customerName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="头像" prop="faceAvatar">
          <el-upload
            class="avatar-uploader"
            action="/api/common/upload"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <img v-if="form.faceAvatar" :src="form.faceAvatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
    </el-dialog>

    <!-- Password Change Dialog -->
    <el-dialog v-model="pwdDialogVisible" title="修改用户密码" width="500px">
      <el-form ref="pwdChangeFormRef" :model="pwdChangeForm" :rules="pwdChangeRules" label-width="120px" style="max-width: 460px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdChangeForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdChangeForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="pwdDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitChangePwd">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import type { FormInstance, FormRules, UploadProps } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
// Mock API or real API if exists
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'

const tableData = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const roleList = ref<any[]>([])
const deptList = ref<any[]>([])
const teamList = ref<any[]>([])
const companyList = ref<any[]>([])
const formRef = ref<FormInstance>()
const pwdChangeFormRef = ref<FormInstance>()

const queryParams = reactive({
  username: ''
})

const form = reactive<any>({
  id: undefined,
  username: '',
  name: '',
  userType: '',
  roleId: undefined,
  deptId: undefined,
  teamId: undefined,
  companyId: undefined,
  phone: '',
  faceAvatar: '',
  status: 1
})
const originalForm = ref<any>({})

const userStore = useUserStore()
const isAdmin = computed(() => {
  const ui:any = userStore.userInfo
  const type = ui?.userType
  const username = ui?.username
  const roleId = ui?.roleId
  const roleName = ui?.roleName
  const roles = ui?.roles || []
  return type === 'admin'
    || username === 'admin'
    || roleId === 1
    || String(roleName||'').includes('管理员')
    || roles.some((r:any) => String(r.roleName||r.name||'').includes('管理员') || String(r.roleKey||'').includes('admin'))
})

// 判断是否有指定权限码
const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo
  const permissionCodes = ui?.permissionCodes || []
  return isAdmin.value || permissionCodes.includes(permCode)
}

// 上传请求头（包含认证token）
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    Authorization: token ? `Bearer ${token}` : ''
  }
})

const pwdDialogVisible = ref(false)
const pwdChangeForm = reactive<{ id?: number; newPassword: string; confirmPassword: string }>({
  id: undefined,
  newPassword: '',
  confirmPassword: ''
})
const pwdChangeRules = reactive<FormRules>({
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '至少6位', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule:any, value:any, cb:any) => {
        if (value !== pwdChangeForm.newPassword) cb(new Error('两次密码不一致'))
        else cb()
      },
      trigger: 'blur'
    }
  ]
})

const rules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^\d{11}$/, message: '手机号必须为11位数字', trigger: 'blur' }
  ]
})

const handleAvatarSuccess: UploadProps['onSuccess'] = (response, _uploadFile) => {
  if (response.code === 200) {
      form.faceAvatar = response.data
  } else {
      ElMessage.error(response.message || '上传失败')
  }
}

const beforeAvatarUpload: UploadProps['beforeUpload'] = (_rawFile) => {
  // if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
  //   ElMessage.error('头像必须是 JPG/PNG 格式!')
  //   return false
  // } else if (rawFile.size / 1024 / 1024 > 2) {
  //   ElMessage.error('头像大小不能超过 2MB!')
  //   return false
  // }
  return true
}

const handleRoleChange = (val: any) => {
  if (!val) {
    form.userType = ''
    return
  }
  const role = roleList.value.find(item => item.id === val)
  if (role) {
    const name = role.roleName
    if (name.includes('管理员') || name.includes('admin')) {
      form.userType = 'admin'
    } else if (name.includes('业务') || name.includes('销售')) {
      form.userType = 'sales'
    } else if (name.includes('财务')) {
      form.userType = 'finance'
    } else if (name.includes('采购')) {
      form.userType = 'purchase'
    } else if (name.includes('库')) {
      form.userType = 'stock'
    } else if (name.includes('售后')) {
      form.userType = 'after_sales'
    } else if (name.includes('技术') || name.includes('运维') || name.includes('研发')) {
      form.userType = 'tech'
    } else {
      form.userType = 'sales' // 默认值
    }
  }
}

const getUserTypeLabel = (type: string) => {
  const map: any = {
    admin: '管理员',
    sales: '业务员',
    finance: '财务',
    purchase: '采购',
    stock: '库管',
    after_sales: '售后',
    tech: '技术'
  }
  return map[type] || type
}

const fetchOptions = async () => {
  try {
    const roleRes: any = await request({ url: '/system/role/list', params: { size: 100 } })
    roleList.value = roleRes.records || []

    const deptRes: any = await request({ url: '/system/dept/list', params: { size: 100 } })
    deptList.value = deptRes.records || []

    const teamRes: any = await request({ url: '/system/team/list', params: { size: 100 } })
    teamList.value = teamRes.records || []

    // 加载客户列表作为公司选择
    const companyRes: any = await request({ url: '/crm/customer/list', params: { size: 1000 } })
    companyList.value = companyRes.records || []
  } catch (e) {
    console.error(e)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    // Using existing user API endpoint
    const res: any = await request({
      url: '/system/user/list',
      method: 'get',
      params: {
        current: currentPage.value,
        size: pageSize.value,
        ...queryParams
      }
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

const handleQuery = () => {
  currentPage.value = 1
  fetchData()
}

const resetQuery = () => {
  queryParams.username = ''
  handleQuery()
}

const handleAdd = () => {
  form.id = undefined
  form.username = ''
  form.name = ''
  form.userType = ''
  form.roleId = undefined
  form.deptId = undefined
  form.teamId = undefined
  form.companyId = undefined
  form.phone = ''
  form.faceAvatar = ''
  form.status = 1
  originalForm.value = JSON.parse(JSON.stringify(form))
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  Object.assign(form, row)
  originalForm.value = JSON.parse(JSON.stringify(form))
  dialogVisible.value = true
}

const openPwdDialog = (row: any) => {
  pwdChangeForm.id = row.id
  pwdChangeForm.newPassword = ''
  pwdChangeForm.confirmPassword = ''
  pwdDialogVisible.value = true
}

const submitChangePwd = async () => {
  if (!pwdChangeFormRef.value) return
  await pwdChangeFormRef.value.validate(async (valid) => {
    if (!valid) return
    await request({
      url: '/system/user',
      method: 'put',
      data: { id: pwdChangeForm.id, password: pwdChangeForm.newPassword }
    })
    ElMessage.success('密码已更新')
    pwdDialogVisible.value = false
    fetchData()
  })
}
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该用户吗？', '提示', { type: 'warning' }).then(async () => {
    await request({
      url: `/system/user/${row.id}`,
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
      // 兜底设置 userType
      if (!form.userType && form.roleId) {
         handleRoleChange(form.roleId)
         if (!form.userType) form.userType = 'sales'
      }

      if (form.id) {
        await request({
          url: '/system/user',
          method: 'put',
          data: form
        })
        ElMessage.success('更新成功')
      } else {
        try {
          const existed:any = await request({
            url: '/system/user/list',
            method: 'get',
            params: { username: String(form.username||'').trim(), exact: true, size: 1, current: 1 }
          })
          if (Number(existed?.total || 0) > 0) {
            ElMessage.error('用户名已存在，请更换后再保存')
            return
          }
          await request({
            url: '/system/user',
            method: 'post',
            data: form
          })
          ElMessage.success('创建成功')
        } catch (e:any) {
          const msg = String(e?.message || '')
          if (msg.includes('Duplicate entry') || msg.includes('uk_username')) {
            ElMessage.error('用户名已存在，请更换后再保存')
            return
          }
          throw e
        }
      }
      dialogVisible.value = false
      fetchData()
    }
  })
}

onMounted(async () => {
  if (userStore.getInfo) {
    await userStore.getInfo()
  }
  fetchData()
  fetchOptions()
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
    await handleSubmit(formRef.value)
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
onMounted(() => {
  window.addEventListener('beforeunload', beforeUnloadHandler)
})
onUnmounted(() => {
  window.removeEventListener('beforeunload', beforeUnloadHandler)
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  background-color: #fafafa;
  width: 80px;
  height: 80px;
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 18px;
  color: #8c939d;
  width: 80px;
  height: 80px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar {
  width: 80px;
  height: 80px;
  display: block;
  object-fit: cover;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  align-items: center;
}
.search-form {
  margin-bottom: -18px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.dialog-top-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-bottom: 10px;
}
</style>
