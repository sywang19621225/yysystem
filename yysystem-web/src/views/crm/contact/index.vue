<template>
  <div class="app-container">
    <h2 class="page-title">客户联系人管理</h2>
    <div class="toolbar">
      <el-input v-model="queryParams.name" placeholder="联系人姓名" clearable style="width: 150px; margin-right: 10px;" @keyup.enter="handleQuery" />
      <el-input v-model="queryParams.phone" placeholder="手机号" clearable style="width: 150px; margin-right: 10px;" @keyup.enter="handleQuery" />
      <el-input v-model="queryParams.customerName" placeholder="单位名称" clearable style="width: 200px; margin-right: 10px;" @keyup.enter="handleQuery" />
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
      <el-button v-if="canCreate" type="success" @click="handleAdd">新增联系人</el-button>
    </div>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column label="客户" min-width="220">
        <template #default="scope">
          <div>
            <div>{{ getCustomerNameById(scope.row.customerId) }}</div>
            <div style="font-size: 12px; color: #666;">
              {{ getCustomerCompanyById(scope.row.customerId) }}
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="gender" label="性别" width="80">
        <template #default="scope">
          {{ scope.row.gender === 1 ? '男' : '女' }}
        </template>
      </el-table-column>
      <el-table-column prop="name" label="联系人" width="140" />
      <el-table-column prop="phone" label="联系电话" width="140" />
      <el-table-column prop="wechat" label="微信号" width="140" />
      <el-table-column prop="position" label="职务岗位" width="160" />
      <el-table-column label="个人照片" width="120">
        <template #default="scope">
          <img v-if="scope.row.avatarUrl" :src="normalizeUrl(scope.row.avatarUrl)" style="width:36px;height:36px;border-radius:50%;object-fit:cover;" />
        </template>
      </el-table-column>
      <el-table-column prop="isPrimary" label="主要" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.isPrimary" type="success">是</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <el-button v-if="canUpdate" link type="success" @click="handleSetPrimary(scope.row)">设为默认</el-button>
          <el-button v-if="canUpdate" link type="primary" @click="handleEdit(scope.row)">修改</el-button>
          <el-button v-if="canDelete" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="innerVisible" width="500px">
      <template #header>
        <div class="dialog-header">
          <span>{{ form.id ? '修改联系人' : '新增联系人' }}</span>
          <div class="header-actions">
            <el-button @click="innerVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSubmit">保存</el-button>
            <el-button type="success" @click="handleSubmit">确定</el-button>
          </div>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="form.customerId" filterable placeholder="请选择客户">
            <el-option v-for="c in customerOptions" :key="c.id" :label="c.customerName" :value="c.id" />
          </el-select>
        </el-form-item>
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
          <el-select v-model="form.position" placeholder="请选择职务岗位" clearable filterable>
            <el-option v-for="p in positionOptions" :key="p" :label="p" :value="p" />
          </el-select>
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
              <img v-if="form.avatarUrl" :src="normalizeUrl(form.avatarUrl)" style="width:60px;height:60px;border-radius:50%;object-fit:cover;" />
              <el-button v-else size="small">上传</el-button>
            </el-upload>
            <el-button v-if="form.avatarUrl" size="small" type="danger" @click="form.avatarUrl=''">删除</el-button>
          </div>
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isPrimary" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="form.remark" :rows="2" />
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canCreate = computed(() => hasPermission('CC:create'))
const canUpdate = computed(() => hasPermission('CC:update'))
const canDelete = computed(() => hasPermission('CC:delete'))

// 上传请求头（包含认证token）
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    Authorization: token ? `Bearer ${token}` : ''
  }
})

const selectedCustomerId = ref<number | null>(null)
const selectedCustomerName = ref<string>('')
const customerOptions = ref<any[]>([])
const tableData = ref<any[]>([])
const loading = ref(false)
const innerVisible = ref(false)
const formRef = ref<FormInstance>()
const showAll = ref<boolean>(true)

const queryParams = reactive({
  name: '',
  phone: '',
  customerName: ''
})

const form = reactive<any>({
  id: undefined,
  customerId: undefined,
  name: '',
  gender: 1,
  position: '',
  phone: '',
  wechat: '',
  avatarUrl: '',
  isPrimary: 0,
  remark: ''
})
const positionOptions = ref<string[]>([])

const rules = reactive<FormRules>({
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  name: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
})

const fetchCustomers = async () => {
  const res:any = await request({ url: '/crm/customer/list', method: 'get', params: { size: 999, current: 1 } })
  const list = res.records || res || []
  try {
    const collator = new Intl.Collator(['zh-u-co-pinyin', 'zh'], { usage: 'sort', sensitivity: 'base' })
    list.sort((a:any, b:any) => collator.compare(String(a.customerName || ''), String(b.customerName || '')))
  } catch {
    list.sort((a:any, b:any) => String(a.customerName || '').localeCompare(String(b.customerName || ''), 'zh'))
  }
  customerOptions.value = list
  if (!selectedCustomerId.value && list.length > 0) {
    selectedCustomerId.value = list[0].id
    selectedCustomerName.value = list[0].customerName
    await fetchContacts()
    if ((tableData.value || []).length === 0 && list.length > 1) {
      for (let i = 1; i < list.length; i++) {
        selectedCustomerId.value = list[i].id
        selectedCustomerName.value = list[i].customerName
        await fetchContacts()
        if ((tableData.value || []).length > 0) break
      }
    }
  }
}

const fetchGeneralPositionOptions = async () => {
  try {
    const res:any = await request({ url: '/system/config/list', method: 'get', params: { size: 200, current: 1 } })
    const list = res.records || res || []
    const general = list.find((item:any) => item.configKey === 'general_settings')
    let cfg:any = {}
    try { cfg = general?.configValue ? JSON.parse(general.configValue) : {} } catch { cfg = {} }
    const custom = cfg.customCategories || {}
    const keys = Object.keys(custom || {})
    const pickByName = (keyword:string) => {
      const k = keys.find(name => String(name||'').includes(keyword))
      return k ? (Array.isArray(custom[k]) ? custom[k] : []) : []
    }
    let posList:string[] = Array.isArray(cfg.positionTitles) ? cfg.positionTitles : []
    if (!posList.length) {
      const byPos = pickByName('职位')
      const byPost = pickByName('岗位')
      posList = (byPos && byPos.length) ? byPos : byPost
    }
    positionOptions.value = posList || []
  } catch {
    positionOptions.value = []
  }
}

const getCustomerNameById = (id?: number) => {
  if (!id) return ''
  const c = (customerOptions.value || []).find((x:any) => x.id === id)
  return c?.customerName || `客户${id}`
}
const getCustomerCompanyById = (id?: number) => {
  if (!id) return ''
  const c = (customerOptions.value || []).find((x:any) => x.id === id)
  return c?.company || ''
}

const fetchContacts = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (queryParams.name) {
      params.name = queryParams.name
    }
    if (queryParams.phone) {
      params.phone = queryParams.phone
    }
    if (queryParams.customerName) {
      params.customerName = queryParams.customerName
    }
    const res: any = await request({ url: '/crm/contact/list-by-condition', method: 'get', params })
    tableData.value = res || res?.data || []
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  fetchContacts()
}

const resetQuery = () => {
  queryParams.name = ''
  queryParams.phone = ''
  queryParams.customerName = ''
  fetchContacts()
}

const onCustomerChange = () => {
  const found = (customerOptions.value || []).find((c:any) => c.id === selectedCustomerId.value)
  selectedCustomerName.value = found?.customerName || ''
  fetchContacts()
}
const onShowAllChange = () => {
  fetchContacts()
}

const handleAdd = () => {
  form.id = undefined
  form.customerId = selectedCustomerId.value || null
  form.name = ''
  form.gender = 1
  form.position = ''
  form.phone = ''
  form.wechat = ''
  form.avatarUrl = ''
  form.isPrimary = 0
  form.remark = ''
  innerVisible.value = true
}

const handleEdit = (row: any) => {
  Object.assign(form, row)
  form.customerId = row.customerId ?? selectedCustomerId.value ?? null
  innerVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该联系人吗？', '提示', { type: 'warning' }).then(async () => {
    await request({ url: `/crm/contact/${row.id}`, method: 'delete' })
    ElMessage.success('删除成功')
    fetchContacts()
  })
}

const handleSetPrimary = async (row: any) => {
  await request({ url: `/crm/contact/set-primary/${row.id}`, method: 'post' })
  ElMessage.success('已设为默认联系人')
  fetchContacts()
}

const beforeUpload = (file: File) => {
  const max = 50 * 1024 * 1024
  if (file.size > max) {
    ElMessage.error('照片大小不能超过50MB')
    return false
  }
  return true
}
const onAvatarSuccess = (res: any) => { form.avatarUrl = res?.data || res?.url || '' }
const onUploadError = () => { ElMessage.error('上传失败') }
const normalizeUrl = (u?: string) => {
  if (!u) return ''
  if (u.startsWith('http')) return u
  const path = u.startsWith('/files/') ? u : (`/files/${u.replace(/^\/+/, '')}`)
  return `${window.location.origin}${path}`
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
      fetchContacts()
    }
  })
}

onMounted(async () => {
  await fetchGeneralPositionOptions()
  await fetchCustomers()
  await fetchContacts()
})
</script>

<style scoped>
.app-container { padding: 20px; }
.toolbar { display:flex; gap:10px; align-items:center; margin-bottom: 12px; }
.dialog-header { display:flex; justify-content:space-between; align-items:center; }
.header-actions { display:flex; gap:8px; align-items:center; }
</style>
