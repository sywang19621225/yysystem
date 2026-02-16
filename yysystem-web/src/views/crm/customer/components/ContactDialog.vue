<template>
  <el-dialog v-model="visible" title="联系人管理" width="900px" @close="handleClose">
    <div style="margin-bottom: 10px; display:flex; gap:10px;">
      <el-button type="primary" @click="handleAdd">新增联系人</el-button>
    </div>
    
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="name" label="联系人" width="120" />
      <el-table-column prop="gender" label="性别" width="80">
        <template #default="scope">
          {{ scope.row.gender === 1 ? '男' : '女' }}
        </template>
      </el-table-column>
      <el-table-column prop="position" label="职务岗位" width="120" />
      <el-table-column prop="phone" label="联系电话" width="120" />
      <el-table-column prop="wechat" label="微信号" width="120" />
      <el-table-column label="个人照片" width="120">
        <template #default="scope">
          <img v-if="scope.row.avatarUrl" :src="scope.row.avatarUrl" style="width:36px;height:36px;border-radius:50%;object-fit:cover;" />
        </template>
      </el-table-column>
      <el-table-column prop="isPrimary" label="主要" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.isPrimary" type="success">是</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button link type="primary" @click="handleEdit(scope.row)">修改</el-button>
          <el-button link type="success" @click="handleSetPrimary(scope.row)">设为默认</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Inner Dialog for Edit -->
    <el-dialog v-model="innerVisible" :title="form.id ? '编辑联系人' : '新增联系人'" width="500px" append-to-body :before-close="onInnerBeforeClose">
      <div class="dialog-top-actions">
        <el-button @click="innerVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="联系人" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="职务岗位" prop="position">
          <el-input v-model="form.position" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="微信号" prop="wechat">
          <el-input v-model="form.wechat" />
        </el-form-item>
        <el-form-item label="个人照片">
          <div style="display:flex; align-items:center; gap:10px;">
            <el-upload
              action="/api/common/upload"
              :headers="uploadHeaders"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :on-success="onAvatarSuccess"
              :on-error="onUploadError"
            >
              <img v-if="form.avatarUrl" :src="form.avatarUrl" style="width:60px;height:60px;border-radius:50%;object-fit:cover;" />
              <el-button v-else size="small">上传</el-button>
            </el-upload>
            <el-button v-if="form.avatarUrl" size="small" type="danger" @click="form.avatarUrl=''">删除</el-button>
          </div>
        </el-form-item>
        <el-form-item label="主要联系人">
          <el-switch v-model="form.isPrimary" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="form.remark" />
        </el-form-item>
      </el-form>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted, onUnmounted, nextTick, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps<{
  modelValue: boolean
  customerId: number
}>()

const emit = defineEmits(['update:modelValue'])

const visible = ref(false)
const innerVisible = ref(false)
const loading = ref(false)
const tableData = ref([])
const formRef = ref<FormInstance>()
const originalForm = ref<any>({})

const form = reactive({
  id: undefined,
  customerId: undefined as number | undefined,
  name: '',
  gender: 1,
  position: '',
  phone: '',
  email: '',
  wechat: '',
  isPrimary: 0,
  remark: '',
  avatarUrl: ''
})

const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
})

// 上传请求头（包含认证token）
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    Authorization: token ? `Bearer ${token}` : ''
  }
})

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.customerId) {
    fetchData()
  }
})

const handleClose = () => {
  emit('update:modelValue', false)
}

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await request({
      url: '/crm/contact/list',
      method: 'get',
      params: { customerId: props.customerId }
    })
    tableData.value = Array.isArray(res) ? res : (res?.data || [])
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  form.id = undefined
  form.customerId = props.customerId
  form.name = ''
  form.gender = 1
  form.position = ''
  form.phone = ''
  form.email = ''
  form.wechat = ''
  form.isPrimary = 0
  form.remark = ''
  innerVisible.value = true
  nextTick(() => {
    originalForm.value = JSON.parse(JSON.stringify(form))
  })
}

const handleEdit = (row: any) => {
  Object.assign(form, row)
  innerVisible.value = true
  nextTick(() => {
    originalForm.value = JSON.parse(JSON.stringify(form))
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该联系人吗？', '提示', { type: 'warning' }).then(async () => {
    await request({ url: `/crm/contact/${row.id}`, method: 'delete' })
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (form.id) {
        await request({ url: `/crm/contact/${form.id}`, method: 'put', data: form })
        ElMessage.success('更新成功')
      } else {
        await request({ url: '/crm/contact', method: 'post', data: form })
        ElMessage.success('创建成功')
      }
      innerVisible.value = false
      fetchData()
    }
  })
}
const beforeUpload = (file: File) => {
  const max = 50 * 1024 * 1024
  if (file.size > max) {
    ElMessage.error('照片大小不能超过50MB')
    return false
  }
  return true
}
const onAvatarSuccess = (res: any, _file: any) => { form.avatarUrl = res.data }
const onUploadError = () => { ElMessage.error('上传失败') }
const handleSetPrimary = async (row: any) => {
  await request({ url: `/crm/contact/set-primary/${row.id}`, method: 'post' })
  ElMessage.success('已设为默认联系人')
  fetchData()
}
const isChanged = () => {
  const a = JSON.stringify(form)
  const b = JSON.stringify(originalForm.value || {})
  return a !== b
}
const onInnerBeforeClose = async (done: () => void) => {
  if (!isChanged()) {
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
  if (innerVisible.value && isChanged()) {
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
.dialog-top-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-bottom: 10px;
}
</style>
