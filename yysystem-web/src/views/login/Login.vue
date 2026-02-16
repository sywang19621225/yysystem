<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="login-header">
          <h2>原邑销售管理平台</h2>
        </div>
      </template>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="0">
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="用户名" 
            prefix-icon="User" 
            @keyup.enter="focusPassword"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            ref="passwordInputRef"
            v-model="loginForm.password" 
            type="password" 
            placeholder="密码" 
            prefix-icon="Lock" 
            show-password 
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-button" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref()
const passwordInputRef = ref()
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 用户名输入框回车时聚焦到密码输入框
const focusPassword = () => {
  passwordInputRef.value?.focus()
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login(loginForm)
        // 登录成功后获取用户信息（包括权限码）
        await userStore.getInfo()
        if (rememberMe.value) {
          localStorage.setItem('remember_username', loginForm.username)
          localStorage.setItem('remember_password', loginForm.password) // 注意：实际生产中不建议明文存储密码，这里仅为演示
          localStorage.setItem('remember_me', 'true')
        } else {
          localStorage.removeItem('remember_username')
          localStorage.removeItem('remember_password')
          localStorage.removeItem('remember_me')
        }
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error) {
        // error handled in request.ts
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  const isRemember = localStorage.getItem('remember_me')
  if (isRemember === 'true') {
    loginForm.username = localStorage.getItem('remember_username') || ''
    loginForm.password = localStorage.getItem('remember_password') || ''
    rememberMe.value = true
  }
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.login-card {
  width: 400px;
}
.login-header {
  text-align: center;
}
.login-button {
  width: 100%;
}
</style>
