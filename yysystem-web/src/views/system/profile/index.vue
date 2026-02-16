<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>个人资料</span>
        </div>
      </template>
      <el-form ref="profileFormRef" :model="form" :rules="profileRules" label-width="100px" style="max-width: 500px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select v-model="form.userType" placeholder="请选择" style="width:100%" :disabled="!isAdmin">
            <el-option label="管理员" value="admin" />
            <el-option label="业务员" value="sales" />
            <el-option label="财务" value="finance" />
            <el-option label="采购" value="purchase" />
            <el-option label="库管" value="stock" />
            <el-option label="售后" value="after_sales" />
            <el-option label="技术" value="tech" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item>
          <el-button v-if="canUpdate" type="primary" @click="handleUpdateProfile(profileFormRef)">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>修改密码</span>
        </div>
      </template>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" style="max-width: 500px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleChangePassword(pwdFormRef)">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

// 权限检查函数
const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canUpdate = computed(() => hasPermission('PC:update'))

const profileFormRef = ref<FormInstance>()
const pwdFormRef = ref<FormInstance>()

const form = reactive({
  id: undefined,
  username: '',
  name: '',
  userType: '',
  phone: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const profileRules = reactive<FormRules>({
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^\d{11}$/, message: '手机号必须为11位数字', trigger: 'blur' }
  ]
})
const isAdmin = (() => {
  const ui:any = useUserStore().userInfo || {}
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
})()

const pwdRules = reactive<FormRules>({
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: any, callback: any) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

const fetchProfile = async () => {
  if (!userStore.userInfo) {
    await userStore.getInfo()
  }
  if (userStore.userInfo) {
    Object.assign(form, userStore.userInfo)
  }
}

const handleUpdateProfile = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      await request({
        url: '/system/user',
        method: 'put',
        data: form
      })
      ElMessage.success('保存成功')
      // 更新 store 中的信息
      await userStore.getInfo()
    }
  })
}

const handleChangePassword = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      // 这里需要后端提供修改密码接口，暂时仅前端校验通过
      ElMessage.success('修改成功 (演示)')
    }
  })
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
</style>
