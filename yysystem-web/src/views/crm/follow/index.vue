<template>
  <div class="app-container">
    <h2 class="page-title">项目跟进</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #409EFF;">{{ statistics.totalCount || 0 }}</div>
            <div style="color: #909399; margin-top: 8px;">跟单总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #67C23A;">{{ statistics.customerCount || 0 }}</div>
            <div style="color: #909399; margin-top: 8px;">跟进客户数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #E6A23C;">{{ statistics.thisMonthCount || 0 }}</div>
            <div style="color: #909399; margin-top: 8px;">本月跟单</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #F56C6C;">¥{{ formatAmount(statistics.totalBudget) }}</div>
            <div style="color: #909399; margin-top: 8px;">预算总额</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索栏 -->
    <div class="toolbar">
      <el-select v-model="selectedCustomerId" filterable placeholder="选择客户" style="width: 200px; margin-right: 10px;" @change="onCustomerChange" clearable>
        <el-option label="全部客户" value="" />
        <el-option v-for="c in customerOptions" :key="c.id" :label="c.customerName" :value="c.id" />
      </el-select>
      <el-input v-model="searchKeyword" placeholder="跟单内容" clearable style="width: 200px; margin-right: 10px;" @keyup.enter="fetchRecords" />
      <el-button @click="fetchRecords">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button v-if="canCreate" type="primary" @click="handleAdd">新增跟单</el-button>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="customerId" label="客户" min-width="160">
        <template #default="scope">
          {{ customerNameById(scope.row.customerId) }}
        </template>
      </el-table-column>
      <el-table-column label="联系人" width="140">
        <template #default="scope">
          {{ scope.row.contactName || contactNameById(scope.row.contactId) }}
        </template>
      </el-table-column>
      <el-table-column label="联系电话" width="140">
        <template #default="scope">
          {{ scope.row.contactPhone || scope.row.phone || '' }}
        </template>
      </el-table-column>
      <el-table-column prop="followType" label="跟单类型" width="120" />
      <el-table-column prop="followContent" label="跟单内容" min-width="200" show-overflow-tooltip />
      <el-table-column prop="nextFollowTime" label="下次跟单时间" width="160" />
      <el-table-column prop="followerId" label="跟单人" width="120">
        <template #default="scope">
          {{ usersById[scope.row.followerId]?.name || usersById[scope.row.followerId]?.realName || usersById[scope.row.followerId]?.nickname || usersById[scope.row.followerId]?.username || '' }}
        </template>
      </el-table-column>
      <el-table-column label="附件" min-width="180">
        <template #default="scope">
          <div v-if="parseJsonArray(scope.row.attachments).length">
            <el-link v-for="f in parseJsonArray(scope.row.attachments)" :key="f.url" :href="f.url" target="_blank" type="primary" style="margin-right:8px">{{ f.name }}</el-link>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="现场照片" min-width="160">
        <template #default="scope">
          <div style="display:flex;gap:6px;flex-wrap:wrap;">
            <img v-for="p in parseJsonArray(scope.row.sitePhotos)" :key="p.url" :src="p.url" style="width:40px;height:40px;object-fit:cover;border-radius:4px;" />
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="budgetAmount" label="预算金额" width="120" />
      <el-table-column prop="followTime" label="跟单时间" width="160" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button v-if="canUpdate" link type="primary" @click="handleEdit(scope.row)">修改</el-button>
          <el-button v-if="canDelete" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="innerVisible" width="720px">
      <template #header>
        <div class="dialog-header">
          <span>{{ form.id ? '修改跟单' : '新增跟单' }}</span>
          <div class="header-actions">
            <el-button @click="innerVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSubmit">保存</el-button>
          </div>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="客户" prop="customerId">
              <el-select v-model="form.customerId" filterable placeholder="请选择客户" @change="onFormCustomerChange" clearable>
                <el-option label="全部客户" value="" />
                <el-option v-for="c in customerOptions" :key="c.id" :label="c.customerName" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactId">
              <el-select v-model="form.contactId" filterable placeholder="请选择联系人" @change="onFormContactChange" clearable>
                <el-option label="全部联系人" :value="undefined" />
                <el-option v-for="ct in contactOptions" :key="ct.id" :label="ct.name" :value="ct.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联报价单">
              <el-select v-model="form.quoteId" filterable placeholder="请选择报价单" clearable>
                <el-option v-for="q in quoteOptions" :key="q.id" :label="(q.quoteNo || '') + ' - ' + (q.customerName || '')" :value="q.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="跟单类型" prop="followType">
              <el-select v-model="form.followType" placeholder="请选择类型" clearable>
                <el-option v-for="t in followTypeOptions" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="跟单内容" prop="followContent">
          <el-input type="textarea" v-model="form.followContent" :rows="3" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="跟单时间" prop="followTime">
              <el-date-picker v-model="form.followTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下次跟单时间" prop="nextFollowTime">
              <el-date-picker v-model="form.nextFollowTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="跟单人" prop="followerId">
              <el-select v-model="form.followerId" filterable placeholder="请选择跟单人" clearable>
                <el-option v-for="u in userOptions" :key="u.id" :label="u.name || u.realName || u.nickname || u.username" :value="u.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预算金额">
              <el-input-number v-model="form.budgetAmount" :min="0" :step="1" :precision="2" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="附件上传">
          <div style="display:flex;gap:10px;align-items:center;flex-wrap:wrap;">
            <el-upload
              action="/api/common/upload"
              :headers="uploadHeaders"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :on-success="onAttachmentSuccess"
              :on-error="onUploadError"
              :accept="acceptTypes"
            >
              <el-button size="small">上传</el-button>
            </el-upload>
            <div v-for="f in attachments" :key="f.url" style="display:flex;align-items:center;gap:6px;">
              <el-link :href="f.url" target="_blank">{{ f.name }}</el-link>
              <el-button size="small" type="danger" @click="removeAttachment(f)">删除</el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="现场照片">
          <div style="display:flex;gap:10px;align-items:center;flex-wrap:wrap;">
            <el-upload
              action="/api/common/upload"
              :headers="uploadHeaders"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :on-success="onPhotoSuccess"
              :on-error="onUploadError"
              accept="image/*"
            >
              <el-button size="small">上传照片</el-button>
            </el-upload>
            <div v-for="p in sitePhotos" :key="p.url" style="display:flex;align-items:center;gap:6px;">
              <img :src="p.url" style="width:60px;height:60px;border-radius:6px;object-fit:cover;" />
              <el-button size="small" type="danger" @click="removePhoto(p)">删除</el-button>
            </div>
          </div>
        </el-form-item>

        <el-divider>可扩展字段</el-divider>
        <div style="display:flex;flex-direction:column;gap:8px;">
          <div v-for="(ef, idx) in extendFields" :key="ef._key" style="display:flex;gap:8px;">
            <el-input v-model="ef.name" placeholder="字段名" style="width:160px" />
            <el-select v-model="ef.type" placeholder="字段类型" style="width:160px">
              <el-option label="文本" value="text" />
              <el-option label="数字" value="number" />
              <el-option label="日期" value="date" />
            </el-select>
            <el-checkbox v-model="ef.required">必填</el-checkbox>
            <el-button size="small" type="danger" @click="removeExtendField(idx)">删除</el-button>
          </div>
          <el-button size="small" @click="addExtendField">新增扩展字段</el-button>
        </div>
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

const canCreate = computed(() => hasPermission('PF:create'))
const canUpdate = computed(() => hasPermission('PF:update'))
const canDelete = computed(() => hasPermission('PF:delete'))

const selectedCustomerId = ref<number | undefined>(undefined)
const searchKeyword = ref('')
const customerOptions = ref<any[]>([])
const contactOptions = ref<any[]>([])
const quoteOptions = ref<any[]>([])
const followTypeOptions = ref<string[]>([])
const userOptions = ref<any[]>([])
const usersById = reactive<Record<number, any>>({})
const tableData = ref<any[]>([])
const loading = ref(false)
const innerVisible = ref(false)
const formRef = ref<FormInstance>()
const acceptTypes = 'image/*,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.zip,.rar,.7z,.txt'
const uploadHeaders = reactive<Record<string, string>>({ Authorization: localStorage.getItem('token') ? ('Bearer ' + localStorage.getItem('token')) : '' })

// 统计数据
const statistics = reactive({
  totalCount: 0,
  customerCount: 0,
  thisMonthCount: 0,
  totalBudget: 0
})

// 格式化金额
const formatAmount = (amount: number | undefined) => {
  if (!amount) return '0.00'
  return amount.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

  const form = reactive<any>({
    id: undefined,
    customerId: undefined,
    contactId: undefined,
    quoteId: undefined,
    phone: '',
    followType: '',
    followContent: '',
    nextFollowTime: '',
    followerId: undefined,
    budgetAmount: 0,
    followTime: ''
  })

const attachments = ref<Array<{name:string,url:string,type?:string,size?:number}>>([])
const sitePhotos = ref<Array<{name:string,url:string,type?:string,size?:number}>>([])
const extendFields = ref<Array<{_key:string,name:string,type:string,required:boolean}>>([])

const rules = reactive<FormRules>({
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  followType: [{ required: true, message: '请选择跟单类型', trigger: 'change' }],
  followContent: [{ required: true, message: '请输入跟单内容', trigger: 'blur' }],
  followTime: [{ required: true, message: '请选择跟单时间', trigger: 'change' }]
})

const customerNameById = (id: number) => {
  const c = customerOptions.value.find((x:any) => x.id === id)
  return c?.customerName || ''
}
const contactNameById = (id: number) => {
  const c = contactOptions.value.find((x:any) => x.id === id)
  return c?.name || ''
}

const fetchCustomers = async () => {
  const res:any = await request({ url: '/crm/customer/list', method: 'get', params: { size: 999, current: 1 } })
  const list = res.records || res || []
  try {
    const collator = new Intl.Collator(['zh-Hans-u-co-pinyin', 'zh'], { usage: 'sort', sensitivity: 'base' })
    list.sort((a:any, b:any) => collator.compare(String(a.customerName || ''), String(b.customerName || '')))
  } catch {
    list.sort((a:any, b:any) => String(a.customerName || '').localeCompare(String(b.customerName || ''), 'zh'))
  }
  customerOptions.value = list
  selectedCustomerId.value = undefined
  await fetchRecords()
}

const fetchContacts = async (customerId: number) => {
  const res:any = await request({ url: '/crm/contact/list', method: 'get', params: { customerId } })
  contactOptions.value = Array.isArray(res) ? res : (res?.records || [])
}
const fetchQuotes = async (customerId: number) => {
  const res:any = await request({ url: '/crm/quote/list', method: 'get', params: { customerId, size: 100 } })
  quoteOptions.value = res?.records || []
}

const fetchFollowTypes = async () => {
  const res:any = await request({ url: '/system/config/list', method: 'get', params: { size: 100 } })
  const list = res.records || res || []
  const general = list.find((item: any) => item.configKey === 'general_settings')
  let types:string[] = []
  if (general?.configValue) {
    try {
      const cfg = JSON.parse(general.configValue)
      if (Array.isArray(cfg.followTypes) && cfg.followTypes.length > 0) {
        types = cfg.followTypes
      } else {
        const custom = cfg.customCategories || {}
        const keys = Object.keys(custom)
        const k = keys.find(x => x.includes('跟单类型'))
        types = k ? (custom[k] || []) : []
      }
    } catch (e) {}
  }
  followTypeOptions.value = types
}

const fetchUserOptions = async () => {
  const res: any = await request({ url: '/system/user/list', method: 'get', params: { size: 100 } })
  const list: any[] = Array.isArray(res?.records) ? res.records : (Array.isArray(res) ? res : [])
  userOptions.value = list
  list.forEach((u: any) => {
    if (u && u.id != null) usersById[u.id] = u
  })
}

const fetchRecords = async () => {
  loading.value = true
  try {
    const params:any = { current: 1, size: 50 }
    if (selectedCustomerId.value) params.customerId = selectedCustomerId.value
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res:any = await request({ url: '/crm/follow-record/list', method: 'get', params })
    tableData.value = (res?.records || [])
    
    // 计算统计数据
    calculateStatistics(tableData.value)
  } finally {
    loading.value = false
  }
}

// 计算统计数据
const calculateStatistics = (data: any[]) => {
  statistics.totalCount = data.length
  statistics.customerCount = new Set(data.map(item => item.customerId)).size
  
  // 本月跟单数
  const now = new Date()
  const thisMonth = now.getMonth()
  const thisYear = now.getFullYear()
  statistics.thisMonthCount = data.filter(item => {
    const followTime = new Date(item.followTime)
    return followTime.getMonth() === thisMonth && followTime.getFullYear() === thisYear
  }).length
  
  // 预算总额
  statistics.totalBudget = data.reduce((sum, item) => sum + (Number(item.budgetAmount) || 0), 0)
}

// 重置搜索
const resetSearch = () => {
  selectedCustomerId.value = undefined
  searchKeyword.value = ''
  fetchRecords()
}

const onCustomerChange = async () => {
  if (selectedCustomerId.value) {
    await fetchContacts(Number(selectedCustomerId.value))
    await fetchQuotes(Number(selectedCustomerId.value))
  }
  await fetchRecords()
}

const handleAdd = () => {
  form.id = undefined
  form.customerId = selectedCustomerId.value || null
  form.contactId = undefined
  form.phone = ''
  form.followType = (followTypeOptions.value.includes('报价跟单') ? '报价跟单' : '')
  form.followContent = ''
  form.nextFollowTime = ''
  form.followerId = undefined
  form.budgetAmount = 0
  form.followTime = ''
  attachments.value = []
  sitePhotos.value = []
  extendFields.value = []
  innerVisible.value = true
  if (form.customerId) onFormCustomerChange()
}

const handleEdit = (row: any) => {
  Object.assign(form, row)
  attachments.value = parseJsonArray(row.attachments)
  sitePhotos.value = parseJsonArray(row.sitePhotos)
  extendFields.value = parseJsonArray(row.extendFields).map((it:any) => ({ _key: Math.random().toString(36).slice(2), name: it.name, type: it.type, required: !!it.required }))
  innerVisible.value = true
  onFormCustomerChange()
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该跟单记录吗？', '提示', { type: 'warning' }).then(async () => {
    await request({ url: `/crm/follow-record/${row.id}`, method: 'delete' })
    ElMessage.success('删除成功')
    fetchRecords()
  })
}

const onFormCustomerChange = async () => {
  if (!form.customerId) return
  await fetchContacts(form.customerId)
  await fetchQuotes(form.customerId)
  const primary = contactOptions.value.find((x:any) => x.isPrimary === 1) || contactOptions.value[0]
  if (primary && primary.id != null) {
    form.contactId = primary.id
    if (primary.phone) form.phone = primary.phone
  } else {
    const cust = customerOptions.value.find((x:any) => x.id === form.customerId)
    if (cust?.phone) form.phone = cust.phone
  }
}

const onFormContactChange = () => {
  const ct = contactOptions.value.find((x:any) => x.id === form.contactId)
  if (ct?.phone) form.phone = ct.phone
}

const beforeUpload = (file: File) => {
  const max = 500 * 1024 * 1024
  if (file.size > max) {
    ElMessage.error('文件大小不能超过500MB')
    return false
  }
  return true
}
const onAttachmentSuccess = (res: any, file: any) => {
  attachments.value.push({ name: file.name, url: res.data, type: file.type, size: file.size })
}
const onPhotoSuccess = (res: any, file: any) => {
  sitePhotos.value.push({ name: file.name, url: res.data, type: file.type, size: file.size })
}
const onUploadError = () => { ElMessage.error('上传失败') }
const removeAttachment = (f: any) => {
  attachments.value = attachments.value.filter(x => x.url !== f.url)
}
const removePhoto = (p: any) => {
  sitePhotos.value = sitePhotos.value.filter(x => x.url !== p.url)
}

const parseJsonArray = (v: any) => {
  if (!v) return []
  if (Array.isArray(v)) return v
  try {
    const obj = JSON.parse(v)
    return Array.isArray(obj) ? obj : []
  } catch (e) {
    return []
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
  const payload = {
    ...form,
    attachments: JSON.stringify(attachments.value),
    sitePhotos: JSON.stringify(sitePhotos.value),
    extendFields: JSON.stringify(extendFields.value.map(it => ({ name: it.name, type: it.type, required: it.required })))
  }
      if (form.id) {
        await request({ url: `/crm/follow-record/${form.id}`, method: 'put', data: payload })
        ElMessage.success('更新成功')
      } else {
        await request({ url: '/crm/follow-record', method: 'post', data: payload })
        ElMessage.success('创建成功')
      }
      innerVisible.value = false
      fetchRecords()
    }
  })
}

const addExtendField = () => {
  extendFields.value.push({ _key: Math.random().toString(36).slice(2), name: '', type: 'text', required: false })
}
const removeExtendField = (idx: number) => {
  extendFields.value.splice(idx, 1)
}

onMounted(async () => {
  await fetchUserOptions()
  await fetchFollowTypes()
  await fetchCustomers()
})
</script>

<style scoped>
.app-container { padding: 20px; }
.toolbar { display:flex; gap:10px; align-items:center; margin-bottom: 12px; }
.dialog-header { display:flex; justify-content:space-between; align-items:center; }
.header-actions { display:flex; gap:8px; align-items:center; }
</style>
