<template>
  <el-dialog v-model="visible" width="800px" @close="handleClose" :before-close="onOuterBeforeClose">
    <template #header>
      <div class="dialog-header">
        <span>跟进记录</span>
        <div class="header-actions">
          <el-button @click="visible = false">取消</el-button>
          <el-button type="primary" @click="handleAdd">新增</el-button>
        </div>
      </div>
    </template>
    <div style="margin-bottom: 20px; display: flex; gap: 10px;">
      <el-button type="primary" @click="handleAdd">新增跟进</el-button>
      <el-button @click="handleExport">导出CSV</el-button>
    </div>
    
    <div style="max-height: 500px; overflow-y: auto; padding-right: 10px;">
      <el-timeline>
        <el-timeline-item
          v-for="(activity, index) in activities"
          :key="index"
          :timestamp="activity.followTime"
          placement="top"
        >
          <el-card>
            <h4>
              {{ transFollowType(activity.followType) }} -
              <el-link type="primary" @click="viewUser()">{{ getUserName(activity) }}</el-link>
              <span v-if="getContactDisplay(activity)" style="margin-left:8px; font-size:12px; color:#666;">
                联系方式：<template v-if="getContactPhone(activity)"><a :href="`tel:${getContactPhone(activity)}`">{{ getContactDisplay(activity) }}</a></template><template v-else>{{ getContactDisplay(activity) }}</template>
              </span>
            </h4>
            <p style="white-space: pre-wrap;">{{ activity.followContent }}</p>
            <p v-if="activity.nextFollowTime" style="color: #E6A23C; font-size: 12px; margin-top: 5px;">
              下次跟进: {{ activity.nextFollowTime }}
            </p>
            <div style="margin-top:8px; display:flex; align-items:center; gap:12px;">
              <span v-if="(activity.attachments||[]).length">附件: {{ (activity.attachments||[]).length }} 个</span>
              <span v-if="(activity.sitePhotos||[]).length">照片: {{ (activity.sitePhotos||[]).length }} 张</span>
              <el-button size="small" type="primary" link @click="toggleDetail(activity)">详情</el-button>
            </div>
            <div v-if="activity._expanded" style="margin-top:8px;">
              <el-table :data="activity.attachments || []" border size="small" v-if="(activity.attachments||[]).length">
                <el-table-column label="文件名" min-width="220">
                  <template #default="scope">
                    {{ getAttachmentName(scope.row) }}
                  </template>
                </el-table-column>
                <el-table-column label="查看" width="120">
                  <template #default="scope">
                    <el-link :href="normalizeUrl(getAttachmentUrl(scope.row))" target="_blank">打开</el-link>
                  </template>
                </el-table-column>
              </el-table>
              <div style="display:flex; flex-wrap:wrap; gap:8px; margin-top:8px;" v-if="(activity.sitePhotos||[]).length">
                <a v-for="(p,idx) in activity.sitePhotos" :key="idx" :href="normalizeUrl(getPhotoUrl(p))" target="_blank">
                  <img :src="normalizeUrl(getPhotoUrl(p))" style="width:80px;height:80px;object-fit:cover;border-radius:4px;" />
                </a>
              </div>
            </div>
            <div style="margin-top: 10px; text-align: right;">
              <el-button type="danger" link size="small" @click="handleDelete(activity)">删除</el-button>
            </div>
          </el-card>
        </el-timeline-item>
        <el-empty v-if="activities.length === 0" description="暂无跟进记录" />
      </el-timeline>
    </div>

    <!-- Inner Dialog for Add -->
    <el-dialog v-model="innerVisible" width="500px" append-to-body :before-close="onInnerBeforeClose">
      <template #header>
        <div class="dialog-header">
          <span>新增跟进</span>
          <div class="header-actions">
            <el-button @click="innerVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSubmit">保存</el-button>
          </div>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="联系人">
          <el-select v-model="form.contactId" filterable clearable style="width:100%" @change="onFormContactChange">
            <el-option v-for="ct in contacts" :key="ct.id" :label="ct.name" :value="ct.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="跟进时间" prop="followTime">
          <el-date-picker v-model="form.followTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="跟进方式" prop="followType">
          <el-select v-model="form.followType" style="width: 100%">
            <el-option label="电话" value="PHONE" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="拜访" value="VISIT" />
            <el-option label="邮件" value="EMAIL" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="跟进内容" prop="followContent">
          <el-input type="textarea" v-model="form.followContent" rows="4" />
        </el-form-item>
        <el-form-item label="下次跟进">
          <el-date-picker v-model="form.nextFollowTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-divider>附件</el-divider>
        <div style="display:flex; align-items:center; gap:8px; margin-bottom:8px;">
          <el-upload
            action="/api/common/upload"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="onAttachmentSuccess"
            :before-upload="beforeUploadAttachment"
            accept=".zip,.rar,.7z,.doc,.docx,.xls,.xlsx,.pdf,.ppt,.pptx,.txt,.csv,.jpg,.jpeg,.png">
            <el-button size="small">上传附件</el-button>
          </el-upload>
        </div>
        <el-table :data="attachments" border size="small" v-if="attachments.length">
          <el-table-column prop="name" label="文件名" min-width="200" />
          <el-table-column label="预览" width="120">
            <template #default="scope">
              <el-link :href="normalizeUrl(scope.row.url)" target="_blank">查看</el-link>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button link type="danger" size="small" @click="removeAttachment(scope.$index)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-divider>现场照片</el-divider>
        <div style="display:flex; align-items:center; gap:8px; margin-bottom:8px;">
          <el-upload
            action="/api/common/upload"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="onPhotoSuccess"
            :before-upload="beforeUploadPhoto"
            accept="image/*">
            <el-button size="small">上传照片</el-button>
          </el-upload>
        </div>
        <div style="display:flex; flex-wrap:wrap; gap:8px;" v-if="sitePhotos.length">
          <a v-for="(p,idx) in sitePhotos" :key="idx" :href="normalizeUrl(p.url)" target="_blank">
            <img :src="normalizeUrl(p.url)" style="width:80px;height:80px;object-fit:cover;border-radius:4px;" />
          </a>
          <el-button v-if="sitePhotos.length" size="small" type="danger" @click="clearPhotos">清空照片</el-button>
        </div>
      </el-form>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { useUserStore } from '@/store/user'

const props = defineProps<{
  modelValue: boolean
  customerId: number
}>()

const emit = defineEmits(['update:modelValue'])

const visible = ref(false)
const innerVisible = ref(false)
const activities = ref<any[]>([])
const formRef = ref<FormInstance>()
const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  customerId: undefined as number | undefined,
  contactId: undefined as number | undefined,
  phone: '',
  followTime: '',
  followType: 'PHONE',
  followContent: '',
  nextFollowTime: ''
})

const rules = reactive<FormRules>({
  followTime: [{ required: true, message: '请选择跟进时间', trigger: 'change' }],
  followType: [{ required: true, message: '请选择跟进方式', trigger: 'change' }],
  followContent: [{ required: true, message: '请输入跟进内容', trigger: 'blur' }]
})

const attachments = ref<Array<{ name: string; url: string }>>([])
const sitePhotos = ref<Array<{ url: string }>>([])
const originalSnapshot = ref<any>({})
const serializeSnapshot = () => {
  return JSON.stringify({
    form,
    attachments: attachments.value,
    sitePhotos: sitePhotos.value
  })
}
const isChanged = () => {
  const a = serializeSnapshot()
  const b = JSON.stringify(originalSnapshot.value || {})
  return a !== b
}
const uploadHeaders = reactive<Record<string, string>>({
  Authorization: localStorage.getItem('token') ? ('Bearer ' + localStorage.getItem('token')) : ''
})
const users = ref<any[]>([])
const usersById = reactive<Record<number, any>>({})
const usersByUsername = reactive<Record<string, any>>({})
const contactsById = reactive<Record<number, any>>({})
const contacts = ref<any[]>([])

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.customerId) {
    fetchData()
    fetchUsers()
    fetchContacts()
  }
})

const handleClose = () => {
  emit('update:modelValue', false)
}

const onOuterBeforeClose = (done: () => void) => {
  // 外层对话框主要是查看功能，添加简单确认提示
  ElMessageBox.confirm('确定要关闭跟进记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    done()
  }).catch(() => {
    // 用户取消关闭，不做任何操作
  })
}

const fetchData = async () => {
  try {
    const res: any = await request({
      url: '/crm/follow-record/list',
      method: 'get',
      params: { 
        customerId: props.customerId,
        size: 100
      }
    })
    const recordList = res.records || []
    activities.value = recordList.map((it:any) => {
      let att:any[] = []
      let photos:any[] = []
      try {
        att = Array.isArray(it.attachments) ? it.attachments : JSON.parse(it.attachments || '[]')
      } catch {}
      try {
        photos = Array.isArray(it.sitePhotos) ? it.sitePhotos : JSON.parse(it.sitePhotos || '[]')
      } catch {}
      att = (att || []).map((x:any) => {
        if (typeof x === 'string') {
          const nm = x.split('/').pop() || ''
          return { name: nm, url: x }
        }
        return x
      })
      photos = (photos || []).map((x:any) => (typeof x === 'string') ? { url: x } : x)
      return { ...it, attachments: att, sitePhotos: photos, _expanded: false }
    })
  } catch (e) {}
}

const fetchUsers = async () => {
  try {
    const res:any = await request({ url: '/system/user/list', method: 'get', params: { size: 1000 } })
    const userList = res.records || res || []
    users.value = userList
    ;(userList as any[]).forEach((u:any) => {
      if (u && u.id != null) usersById[u.id] = u
      const un = String(u?.username || '')
      if (un) usersByUsername[un] = u
    })
  } catch {}
}
const fetchContacts = async () => {
  try {
    const res:any = await request({ url: '/crm/contact/list', method: 'get', params: { customerId: props.customerId } })
    const contactList = Array.isArray(res) ? res : (res?.records || [])
    contacts.value = contactList || []
    ;(contactList || []).forEach((c:any) => { if (c && c.id != null) contactsById[c.id] = c })
  } catch {}
}
const getUserName = (item:any) => {
  if (item?.followerName) return item.followerName
  if (item?.creatorName) return item.creatorName
  const byId = (item?.followerId != null) ? usersById[item.followerId] : null
  if (byId) return byId.name || byId.realName || byId.nickname || byId.username || '未知用户'
  const byUn = usersByUsername[String(item?.createBy || '')]
  if (byUn) return byUn.name || byUn.realName || byUn.nickname || byUn.username || '未知用户'
  const ui:any = userStore.userInfo
  if (ui && String(item?.createBy || '') === String(ui.username || '')) {
    return ui.name || ui.realName || ui.nickname || ui.username || '未知用户'
  }
  return item?.createBy || '未知用户'
}
const viewUser = () => {
  router.push('/system/user')
}
const transFollowType = (t:string) => {
  const m:Record<string,string> = { PHONE: '电话', WECHAT: '微信', VISIT: '拜访', EMAIL: '邮件', OTHER: '其他' }
  return m[String(t||'').toUpperCase()] || '其他'
}
const getContactPhone = (item:any) => {
  const cid = item?.contactId
  const byId = (cid != null) ? contactsById[cid] : null
  return item?.phone || byId?.phone || ''
}
const getContactDisplay = (item:any) => {
  const cid = item?.contactId
  const byId = (cid != null) ? contactsById[cid] : null
  const name = byId?.name || ''
  const phone = getContactPhone(item)
  if (name && phone) return `${name} ${phone}`
  return name || phone || ''
}

const handleAdd = () => {
  form.customerId = props.customerId
  form.followTime = dayjs().format('YYYY-MM-DD HH:mm:ss')
  form.followType = 'PHONE'
  form.followContent = ''
  form.nextFollowTime = ''
  attachments.value = []
  sitePhotos.value = []
  const primary = contacts.value.find((x:any) => Number(x.isPrimary) === 1) || contacts.value[0]
  if (primary && primary.id != null) {
    form.contactId = primary.id
    form.phone = primary.phone || ''
  } else {
    form.contactId = undefined
    form.phone = ''
  }
  innerVisible.value = true
  nextTick(() => { originalSnapshot.value = JSON.parse(serializeSnapshot()) })
}

const handleDelete = (item: any) => {
  ElMessageBox.confirm('确认删除这条跟进记录吗？', '提示', { type: 'warning' }).then(async () => {
    await request({ url: `/crm/follow-record/${item.id}`, method: 'delete' })
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      const payload:any = { ...form }
      payload.attachments = JSON.stringify(attachments.value)
      payload.sitePhotos = JSON.stringify(sitePhotos.value)
      const ui:any = userStore.userInfo
      if (ui && ui.id != null) payload.followerId = ui.id
      await request({ url: '/crm/follow-record', method: 'post', data: payload })
      ElMessage.success('添加成功')
      innerVisible.value = false
      fetchData()
    }
  })
}

const onFormContactChange = (id:number) => {
  const ct = contactsById[id]
  if (ct?.phone) form.phone = ct.phone
}

const handleExport = () => {
  const rows = activities.value.map((a: any) => ({
    跟进时间: a.followTime || '',
    跟进方式: a.followType || '',
    跟进内容: (a.followContent || '').replace(/\r?\n/g, ' '),
    下次跟进: a.nextFollowTime || '',
    跟进人: a.createBy || ''
  }))
  const header = ['跟进时间','跟进方式','跟进内容','下次跟进','跟进人']
  const csv = [header.join(','), ...rows.map(r => header.map(h => `"${(r as any)[h] || ''}"`).join(','))].join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `follow_records_${props.customerId}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

const onAttachmentSuccess = (res:any, file:any) => {
  const url = res?.data
  const name = file?.name || (String(url||'').split('/').pop() || '')
  if (url) attachments.value.push({ name, url })
}
const removeAttachment = (idx:number) => {
  attachments.value.splice(idx, 1)
}
const onPhotoSuccess = (res:any) => {
  const url = res?.data
  if (url) sitePhotos.value.push({ url })
}
const clearPhotos = () => {
  sitePhotos.value = []
}
const beforeUploadAttachment = (file: File) => {
  const name = (file.name || '').toLowerCase()
  const exts = ['.zip','.rar','.7z','.doc','.docx','.xls','.xlsx','.pdf','.ppt','.pptx','.txt','.csv','.jpg','.jpeg','.png']
  const okExt = exts.some(e => name.endsWith(e))
  const okSize = file.size / 1024 / 1024 < 100
  if (!okExt) { ElMessage.error('仅支持常见文档/表格/压缩包/图片'); return false }
  if (!okSize) { ElMessage.error('文件大小不能超过100MB'); return false }
  return true
}
const beforeUploadPhoto = (file: File) => {
  const okSize = file.size / 1024 / 1024 < 20
  if (!okSize) { ElMessage.error('照片大小不能超过20MB'); return false }
  return true
}
const normalizeUrl = (u?: string) => {
  if (!u) return ''
  if (u.startsWith('http')) return u
  const path = u.startsWith('/files/') ? u : (`/files/${u.replace(/^\/+/, '')}`)
  return `${window.location.origin}${path}`
}
const toggleDetail = (item:any) => { item._expanded = !item._expanded }
const getAttachmentUrl = (row:any) => row?.url || ''
const getAttachmentName = (row:any) => row?.name || (String(row?.url||'').split('/').pop() || '')
const getPhotoUrl = (row:any) => row?.url || ''

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
.dialog-header { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  width: 100%;
  padding: 0 4px;
}
.header-actions { 
  display: flex; 
  gap: 8px; 
  align-items: center; 
}

/* 统一按钮样式 */
.header-actions .el-button {
  min-width: 60px;
}
</style>
