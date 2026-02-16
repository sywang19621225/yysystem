<template>
  <div class="customer-list">
    <h2 class="page-title">客户管理</h2>
    <div class="search-bar">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="客户名称">
          <el-input v-model="queryParams.customerName" placeholder="请输入客户名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="header">
      <el-button v-if="canCreate" type="primary" @click="handleAdd">新增客户</el-button>
    </div>

    <el-table :data="tableData" style="width: 100%" v-loading="loading" border>
      <el-table-column prop="customerCode" label="客户编号" width="150" />
      <el-table-column prop="customerName" label="客户" min-width="150" show-overflow-tooltip />
      <el-table-column prop="customerTag" label="客户标签" width="120" />
      <el-table-column prop="customerCategory" label="客户分类" width="120" />
      <el-table-column prop="linkman" label="联系人" width="120" />
      <el-table-column prop="phone" label="联系电话" width="140" />
      <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
      <el-table-column prop="status" label="启用" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '1' ? 'success' : 'info'">{{ scope.row.status === '1' ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="负责人" width="120">
        <template #default="scope">
          {{ scope.row.principalName || usersById[scope.row.principalId]?.name || usersById[scope.row.principalId]?.realName || usersById[scope.row.principalId]?.nickname || usersById[scope.row.principalId]?.username || '' }}
        </template>
      </el-table-column>
      <el-table-column prop="createBy" label="创建人" width="100" />
      <el-table-column prop="updateTime" label="更新时间" width="160" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleFollow(scope.row)">跟进</el-button>
          <el-button link type="primary" @click="handleContact(scope.row)">联系人</el-button>
          <el-button v-if="canUpdate" link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button v-if="canDelete" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchData"
      />
    </div>

    <ContactDialog v-model="contactDialogVisible" :customer-id="currentCustomerId" />
    <FollowRecordDialog v-model="followDialogVisible" :customer-id="currentCustomerId" />

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑客户' : '新增客户'" width="1000px" :before-close="onDialogBeforeClose">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户编号">
              <el-input v-model="form.customerCode" placeholder="自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户" prop="customerName">
              <el-input v-model="form.customerName" />
              <div style="color: #f56c6c; font-size: 12px; margin-top: 4px;">
                客户名称必须按照工商注册名称，禁止使用**大学+校区
                <a href="https://www.tianyancha.com/" target="_blank" style="color: #409eff; margin-left: 8px; text-decoration: underline;">天眼查</a>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="有效期">
              <el-date-picker v-model="form.validUntil" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="登记状态">
              <el-select v-model="form.registrationStatus" placeholder="请选择" style="width:100%">
                <el-option label="正常" value="正常" />
                <el-option label="异常" value="异常" />
                <el-option label="注销" value="注销" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="登记管理机关">
              <el-input v-model="form.registrationAuthority" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="微信公众号">
              <el-input v-model="form.wechatOfficialAccount" placeholder="请填写客户的微信公众号名称（以图书馆优先）" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="企业微信号">
              <el-input v-model="form.wechatId" placeholder="请填写客户的企业微信号，一般为字母数字" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="微信公众号二维码">
              <div style="display:flex; flex-direction:column; align-items:flex-start; gap:10px;">
                <el-upload
                  class="avatar-uploader"
                  action="/api/common/upload"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :on-success="handleQrSuccess"
                  :on-error="handleLogoError"
                  accept="image/*"
                >
                  <img v-if="form.qrCode" :src="normalizeAsset(form.qrCode)" class="avatar" />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
                <el-button v-if="form.qrCode" type="danger" size="small" @click="handleQrRemove">删除</el-button>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户标签">
              <el-select v-model="form.customerTag" style="width: 100%" clearable placeholder="请选择">
                <el-option v-for="t in customerTagsOptions" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户分类">
              <el-select v-model="form.customerCategory" style="width: 100%" clearable placeholder="请选择">
                <el-option v-for="c in customerCategoriesOptions" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人">
              <div style="display:flex; gap:8px; align-items:center;">
                <el-input v-model="form.linkman" />
                <el-button size="small" @click="handleSyncDefaultContact" :disabled="!form.id">同步默认联系人</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" @input="form.phone = (form.phone || '').replace(/\\D+/g,'')" placeholder="仅限数字" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="启用">
              <el-switch v-model="form.statusSwitch" active-value="1" inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人">
              <el-select v-model="form.principalId" filterable style="width: 100%" clearable>
                <el-option v-for="u in userOptions" :key="u.id" :label="u.name || u.realName || u.nickname || u.username" :value="u.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="网址">
              <el-input v-model="form.website" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系地址">
              <el-input v-model="form.address" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="经费来源">
              <el-select v-model="form.fundingSource" clearable placeholder="请选择" style="width:100%">
                <el-option v-for="fs in fundingSourceOptions" :key="fs" :label="fs" :value="fs" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="注册金（万）">
              <el-input-number v-model="form.registeredCapital" :min="0" :step="1" :precision="2" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开户名称">
              <el-input v-model="form.bankAccountName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开户银行">
              <el-input v-model="form.bankName" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开户账号">
              <el-input v-model="form.bankAccount" @input="form.bankAccount = (form.bankAccount || '').replace(/\\D+/g,'')" placeholder="仅限数字" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="信用代号">
              <el-input v-model="form.creditCode" @input="form.creditCode = (form.creditCode || '').replace(/[^A-Za-z0-9]/g,'').toUpperCase()" placeholder="数字+大写英文" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="注册地址">
              <el-input v-model="form.registerAddress" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="注册电话">
              <el-input v-model="form.registerPhone" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="单位logo">
              <div style="display:flex; align-items:center; gap:10px;">
                <el-upload
                  class="avatar-uploader"
                  action="/api/common/upload"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :on-success="handleLogoSuccess"
                  :on-error="handleLogoError"
                  accept="image/*"
                >
                  <img v-if="form.companyLogo" :src="normalizeAsset(form.companyLogo)" class="avatar" />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
                <el-button v-if="form.companyLogo" type="danger" size="small" @click="handleLogoRemove">删除</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="交通方式">
              <el-input type="textarea" :rows="3" v-model="form.transportationMode" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="单位简介">
          <el-input type="textarea" v-model="form.companyIntro" :rows="6" />
        </el-form-item>

        <el-divider v-if="!isAgentCustomer">基础技术信息</el-divider>
        <TechInfoPanel v-if="!isAgentCustomer" :customer-id="form.id || 0" />

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
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import type { FormInstance, FormRules, UploadProps } from 'element-plus'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import ContactDialog from './components/ContactDialog.vue'
import FollowRecordDialog from './components/FollowRecordDialog.vue'
import { Plus } from '@element-plus/icons-vue'
import TechInfoPanel from './components/TechInfoPanel.vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canCreate = computed(() => hasPermission('CM:create'))
const canUpdate = computed(() => hasPermission('CM:update'))
const canDelete = computed(() => hasPermission('CM:delete'))

const normalizeAsset = (u: string) => {
  if (!u) return ''
  const s = String(u)
  if (s.startsWith('http://') || s.startsWith('https://')) return s
  if (s.startsWith('/files/')) return s
  if (s.startsWith('files/')) return '/' + s
  return '/files/' + s.replace(/^\/+/, '')
}

const tableData = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const contactDialogVisible = ref(false)
const followDialogVisible = ref(false)
const currentCustomerId = ref(0)
const formRef = ref<FormInstance>()
const route = useRoute()

const queryParams = reactive({
  customerName: ''
})

const customerTagsOptions = ref<string[]>([])
const customerCategoriesOptions = ref<string[]>([])
const fundingSourceOptions = ref<string[]>([])
const userOptions = ref<any[]>([])
const usersById = reactive<Record<number, any>>({})

const form = reactive<any>({
  id: undefined,
  customerCode: '',
  customerName: '',
  customerTag: '',
  customerCategory: '',
  linkman: '',
  phone: '',
  website: '',
  address: '',
  bankAccountName: '',
  bankName: '',
  bankAccount: '',
  creditCode: '',
  registerAddress: '',
  registerPhone: '',
  statusSwitch: '1',
  principalId: undefined,
  companyIntro: '',
  companyLogo: '',
  transportationMode: ''
  ,
  fundingSource: '',
  registeredCapital: 0,
  validUntil: '',
  registrationStatus: '',
  registrationAuthority: '',
  wechatOfficialAccount: '',
  wechatId: '',
  qrCode: ''
})
const originalForm = ref<any>({})

const isAgentCustomer = computed(() => {
  const category = String(form.customerCategory || '').toUpperCase()
  const tag = String(form.customerTag || '')
  return category === 'AGENT' || tag.includes('代理')
})

const rules = reactive<FormRules>({
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }]
  ,
  phone: [{ validator: (_rule, value, callback) => {
    if (!value) return callback()
    if (!/^\d+$/.test(String(value))) return callback(new Error('联系电话仅限数字'))
    callback()
  }, trigger: 'blur' }]
})

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await request({
      url: '/crm/customer/list',
      method: 'get',
      params: {
        current: currentPage.value,
        size: pageSize.value,
        customerName: queryParams.customerName
      }
    })
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleReset = () => {
  queryParams.customerName = ''
  handleSearch()
}

const handleAdd = () => {
  form.id = undefined
  form.customerCode = ''
  form.customerName = ''
  form.customerTag = ''
  form.customerCategory = ''
  form.linkman = ''
  form.phone = ''
  form.website = ''
  form.address = ''
  form.bankAccountName = ''
  form.bankName = ''
  form.bankAccount = ''
  form.creditCode = ''
  form.registerAddress = ''
  form.registerPhone = ''
  form.statusSwitch = '1'
  form.principalId = undefined
  form.companyIntro = ''
  form.companyLogo = ''
  form.transportationMode = ''
  form.fundingSource = ''
  form.registeredCapital = 0
  form.validUntil = ''
  form.registrationStatus = '正常'
  form.registrationAuthority = ''
  form.wechatOfficialAccount = ''
  form.wechatId = ''
  form.qrCode = ''
  // 确保标签/分类为最新
  fetchGeneralOptions()
  originalForm.value = JSON.parse(JSON.stringify(form))
  dialogVisible.value = true
}

const handleFollow = (row: any) => {
  currentCustomerId.value = row.id
  followDialogVisible.value = true
}

const handleContact = (row: any) => {
  currentCustomerId.value = row.id
  contactDialogVisible.value = true
}

const handleEdit = (row: any) => {
  Object.assign(form, row)
  form.statusSwitch = row.status === '1' ? '1' : '0'
  // 确保标签/分类为最新
  fetchGeneralOptions()
  dialogVisible.value = true
  syncDefaultContactIfNeeded(row.id)
  originalForm.value = JSON.parse(JSON.stringify(form))
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该客户吗？', '提示', { type: 'warning' }).then(async () => {
    await request({ url: `/crm/customer/${row.id}`, method: 'delete' })
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleSubmit = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      const { statusSwitch, ...rest } = form
      const payload = { ...rest, status: statusSwitch }
      if (form.id) {
        await request({ url: `/crm/customer/${form.id}`, method: 'put', data: payload })
        ElMessage.success('更新成功')
        // 技术信息通过子组件批量保存
      } else {
        await request({ url: '/crm/customer', method: 'post', data: payload })
        ElMessage.success('创建成功')
        // 重新拉取列表拿到新ID并保存技术信息（简单处理：刷新后编辑保存）
      }
      dialogVisible.value = false
      fetchData()
    }
  })
}


const fetchDefaultContact = async (customerId: number) => {
  const res:any = await request({ url: '/crm/contact/list', method: 'get', params: { customerId } })
  const list = Array.isArray(res) ? res : (res?.records || [])
  const def = (list || []).find((x:any) => x.isPrimary === 1) || (list || [])[0]
  return def
}

const syncDefaultContactIfNeeded = async (customerId: number) => {
  const def = await fetchDefaultContact(customerId)
  if (!def) return
  if (!form.linkman) form.linkman = def.name || ''
  if (!form.phone) form.phone = def.phone || ''
}

const handleSyncDefaultContact = async () => {
  if (!form.id) return
  const def = await fetchDefaultContact(form.id)
  if (!def) {
    ElMessage.info('该客户暂无联系人')
    return
  }
  form.linkman = def.name || ''
  form.phone = def.phone || ''
  const idx = (tableData.value || []).findIndex((x:any) => x.id === form.id)
  if (idx >= 0) {
    tableData.value[idx] = { ...tableData.value[idx], linkman: form.linkman, phone: form.phone }
  }
  await request({ url: `/crm/customer/${form.id}`, method: 'put', data: { linkman: form.linkman, phone: form.phone } })
  ElMessage.success('已同步默认联系人信息')
}

onMounted(() => {
  fetchGeneralOptions()
  fetchUserOptions().then(() => {
    fetchData()
  })
  if (String(route.query.create || '') === '1') {
    handleAdd()
  }
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

const handleLogoSuccess: UploadProps['onSuccess'] = (res: any) => {
  form.companyLogo = res.data
}

const handleLogoRemove = () => {
  form.companyLogo = ''
}

const handleLogoError: UploadProps['onError'] = (_err: any) => {
  ElMessage.error('上传失败，请检查登录状态或重试')
}

const uploadHeaders = reactive<Record<string, string>>({
  Authorization: localStorage.getItem('token') ? ('Bearer ' + localStorage.getItem('token')) : ''
})

const handleQrSuccess: UploadProps['onSuccess'] = (res: any) => {
  form.qrCode = res.data
}

const handleQrRemove = () => {
  form.qrCode = ''
}
const fetchUserOptions = async () => {
  const res: any = await request({ url: '/system/user/list', method: 'get', params: { size: 100 } })
  userOptions.value = res.records || res || []
  ;(userOptions.value || []).forEach((u: any) => {
    if (u && u.id != null) {
      usersById[u.id] = u
    }
  })
}

const fetchGeneralOptions = async () => {
  const res: any = await request({ url: '/system/config/list', method: 'get', params: { size: 100 } })
  const list = res.records || res || []
  const general = list.find((item: any) => item.configKey === 'general_settings')
  if (general && general.configValue) {
    try {
      const cfg = JSON.parse(general.configValue)
      // 优先读取独立的客户标签/分类；若不存在则从自定义分类组中回退
      const custom = cfg.customCategories || {}
      const customKeys = Object.keys(custom)
      const pickByName = (keyword: string) => {
        const key = customKeys.find(k => k.includes(keyword))
        return key ? (custom[key] || []) : []
      }
      const firstGroup = customKeys.length > 0 ? (custom[customKeys[0]] || []) : []
      customerTagsOptions.value = (Array.isArray(cfg.customerTags) && cfg.customerTags.length > 0)
        ? cfg.customerTags
        : (pickByName('标签').length > 0 ? pickByName('标签') : firstGroup)
      customerCategoriesOptions.value = (Array.isArray(cfg.customerCategories) && cfg.customerCategories.length > 0)
        ? cfg.customerCategories
        : (pickByName('分类').length > 0 ? pickByName('分类') : firstGroup)
      fundingSourceOptions.value = (Array.isArray(cfg.fundingSources) && cfg.fundingSources.length > 0)
        ? cfg.fundingSources
        : (pickByName('经费来源').length > 0 ? pickByName('经费来源') : [])
    } catch (e) {}
  }
}










</script>

<style scoped>
.customer-list {
  padding: 20px;
}
.header {
  margin-bottom: 20px;
}
.search-bar {
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 100px;
  height: 100px;
  background-color: #fafafa;
}
.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}
.el-icon.avatar-uploader-icon {
  font-size: 24px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}
.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: contain;
}
.debug-block pre {
  background: #f6f8fa;
  padding: 8px;
  font-size: 12px;
  border-radius: 6px;
  overflow: auto;
}
</style>
