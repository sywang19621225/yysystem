<template>
  <div>
    <div style="margin-bottom:8px; display:flex; gap:8px;">
      <el-button size="small" @click="handleRefresh">刷新</el-button>
      <el-button size="small" type="primary" @click="newItem">新增条目</el-button>
    </div>
    <el-table :data="rows" border style="width: 100%" size="small">
      <el-table-column prop="location" label="位置信息" width="160" />
      <el-table-column prop="systemName" label="系统名称" width="180" />
      <el-table-column prop="ipAddress" label="IP地址" width="180" />
      <el-table-column prop="remoteIp" label="远程IP地址" width="180" />
      <el-table-column prop="remoteMethod" label="远程方式" width="160" />
      <el-table-column prop="systemAccount" label="系统账号" width="160" />
      <el-table-column prop="systemPassword" label="系统密码" width="160" />
      <el-table-column prop="remoteAccount" label="远程账号" width="160" />
      <el-table-column prop="remotePassword" label="远程密码" width="160" />
      <el-table-column label="附件上传" min-width="260">
        <template #default="scope">
          <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
            <el-upload
              action="/api/common/upload"
              :headers="uploadHeaders"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :on-success="onSuccessForRow(scope.row)"
              :on-error="onUploadError"
            >
              <el-button size="small">上传</el-button>
            </el-upload>
            <template v-if="Array.isArray(scope.row.attachmentsParsed)">
              <div v-for="(att,idx) in scope.row.attachmentsParsed" :key="att.url" style="display:flex; align-items:center; gap:6px;">
                <a :href="att.url" target="_blank">{{ att.name }}</a>
                <el-button link type="danger" size="small" @click="removeAttachment(scope.row, idx)">删除</el-button>
              </div>
            </template>
            <span v-else>未上传</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="techLeader" label="技术负责人" width="160" />
      <el-table-column prop="techContact" label="联系方式" width="160" />
      <el-table-column prop="techWechat" label="微信账号" width="160" />
      <el-table-column prop="remark" label="备注" min-width="200" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="scope">
          <el-button link size="small" type="primary" @click="startEdit(scope.row)">修改</el-button>
          <el-button link size="small" type="danger" @click="removeById(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top:12px;">
      <el-card shadow="never">
        <div style="margin-bottom:8px; font-weight:600;">{{ current.id ? '编辑条目' : '新增条目' }}</div>
        <el-form :model="current" label-width="100px">
          <el-row :gutter="12">
            <el-col :span="8"><el-form-item label="技术负责人">
              <el-select v-model="current._techLeaderId" filterable placeholder="请选择" style="width:100%" @change="onTechLeaderChange">
                <el-option v-for="c in contactOptions" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item></el-col>
            <el-col :span="8"><el-form-item label="联系方式"><el-input v-model="current.techContact" placeholder="请填写技术负责人的联系方式" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="微信账号"><el-input v-model="current.techWechat" placeholder="请填写技术负责人的微信号" /></el-form-item></el-col>
          </el-row>
          <el-row :gutter="12">
            <el-col :span="8"><el-form-item label="位置信息"><el-input v-model="current.location" placeholder="请填写服务器存放位置" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="系统名称"><el-input v-model="current.systemName" placeholder="请填写服务器上运行的系统或软件名称" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="IP地址"><el-input v-model="current.ipAddress" placeholder="请填写服务器的IP地址和端口号" /></el-form-item></el-col>
          </el-row>
          <el-row :gutter="12">
            <el-col :span="8"><el-form-item label="系统账号"><el-input v-model="current.systemAccount" placeholder="请填写服务器系统的登录账号" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="系统密码"><el-input v-model="current.systemPassword" placeholder="请填写服务器系统的登录密码" /></el-form-item></el-col>
          </el-row>
          <el-row :gutter="12">
            <el-col :span="8"><el-form-item label="远程方式">
              <el-select v-model="current.remoteMethod" placeholder="请选择" style="width:100%">
                <el-option v-for="m in remoteMethods" :key="m" :label="m" :value="m" />
              </el-select>
            </el-form-item></el-col>
            <el-col :span="8"><el-form-item label="远程账号"><el-input v-model="current.remoteAccount" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="远程密码"><el-input v-model="current.remotePassword" /></el-form-item></el-col>
          </el-row>
          <el-row :gutter="12">
            <el-col :span="8"><el-form-item label="远程IP地址"><el-input v-model="current.remoteIp" placeholder="请填写远程IP地址和端口号" /></el-form-item></el-col>
          </el-row>
          <el-row :gutter="12">
            <el-col :span="8">
              <el-form-item label="附件上传">
                <div style="display:flex; align-items:center; gap:8px;">
                  <el-upload
                    action="/api/common/upload"
                    :headers="uploadHeaders"
                    :show-file-list="false"
                    :before-upload="beforeUpload"
                    :on-success="onSuccessForCurrent"
                    :on-error="onUploadError"
                    multiple
                  >
                    <el-button size="small">上传</el-button>
                  </el-upload>
                  <div v-if="Array.isArray(current.attachmentsParsed)" style="display:flex; gap:8px; flex-wrap:wrap;">
                    <div v-for="(att,idx) in current.attachmentsParsed" :key="att.url" style="display:flex; align-items:center; gap:6px;">
                      <a :href="att.url" target="_blank">{{ att.name }}</a>
                      <el-button link type="danger" size="small" @click="removeAttachmentCurrent(idx)">删除</el-button>
                    </div>
                  </div>
                </div>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="备注">
            <el-input v-model="current.remark" type="textarea" :rows="2" />
          </el-form-item>
          <div style="text-align:right;">
            <el-button @click="cancelEdit" size="small">取消</el-button>
            <el-button type="primary" @click="saveCurrent" size="small" :loading="saving">保存</el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted, onUnmounted, nextTick } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps<{ customerId: number }>()

const rows = reactive<any[]>([])
const saving = ref(false)
const current = reactive<any>({
  id: undefined,
  location: '',
  systemName: '',
  ipAddress: '',
  remoteMethod: '',
  remoteIp: '',
  systemAccount: '',
  systemPassword: '',
  remoteAccount: '',
  remotePassword: '',
  techLeader: '',
  techContact: '',
  techWechat: '',
  remark: '',
  _techLeaderId: undefined,
  attachmentsParsed: []
})

const uploadHeaders = reactive<Record<string, string>>({
  Authorization: localStorage.getItem('token') ? ('Bearer ' + localStorage.getItem('token')) : ''
})
const remoteMethods = ref<string[]>([])
const contactOptions = ref<any[]>([])
const originalSnapshot = ref<any>({})
const serializeSnapshot = () => {
  return JSON.stringify({
    current,
    attachments: current.attachmentsParsed || []
  })
}
const isChanged = () => {
  const a = serializeSnapshot()
  const b = JSON.stringify(originalSnapshot.value || {})
  return a !== b
}

const load = async () => {
  if (!props.customerId) return
  const res:any = await request({ url: `/crm/customer/${props.customerId}/tech-info`, method: 'get' })
  const techList:any[] = Array.isArray(res) ? res : (res?.records || [])
  ;(techList || []).forEach((it:any) => {
    try {
      const arr = JSON.parse(String(it.attachments || '[]'))
      it.attachmentsParsed = Array.isArray(arr) ? arr : []
    } catch { it.attachmentsParsed = [] }
  })
  rows.splice(0, rows.length, ...(techList as any[]))
}
const handleRefresh = async () => {
  if (isChanged()) {
    try {
      await ElMessageBox.confirm('内容已修改，是否保存？', '提示', { confirmButtonText: '保存并刷新', cancelButtonText: '直接刷新' })
      await saveCurrent()
      await load()
    } catch {
      await load()
    }
  } else {
    await load()
  }
}
const resetCurrent = () => {
  Object.assign(current, { id: undefined, location: '', systemName: '', ipAddress: '', remoteMethod: '', remoteIp: '', systemAccount: '', systemPassword: '', remoteAccount: '', remotePassword: '', techLeader: '', techContact: '', techWechat: '', remark: '', attachmentsParsed: [] })
  nextTick(() => { originalSnapshot.value = JSON.parse(serializeSnapshot()) })
}

const startEdit = (row: any) => { 
  Object.assign(current, { ...row as any, attachmentsParsed: (row.attachmentsParsed || []) }) 
  // 尝试匹配负责人ID
  const match = (contactOptions.value as any[]).find((c:any)=>String(c.name||'') === String(current.techLeader||''))
  current._techLeaderId = match ? match.id : undefined
  nextTick(() => { originalSnapshot.value = JSON.parse(serializeSnapshot()) })
}
const newItem = async () => {
  if (isChanged()) {
    try {
      await ElMessageBox.confirm('内容已修改，是否保存？', '提示', { confirmButtonText: '保存并新建', cancelButtonText: '直接新建' })
      await saveCurrent()
    } catch {}
  }
  resetCurrent()
}
const cancelEdit = async () => {
  if (isChanged()) {
    try {
      await ElMessageBox.confirm('内容已修改，是否保存？', '提示', { confirmButtonText: '保存并取消', cancelButtonText: '直接取消' })
      await saveCurrent()
    } catch {}
  }
  resetCurrent()
}

const saveCurrent = async () => {
  if (!props.customerId) { ElMessage.warning('请先保存客户基本信息'); return }
  saving.value = true
  try {
    const trim = (v: any) => (typeof v === 'string' ? v.trim() : v)
    const requiredOK = trim(current.location) && trim(current.systemName)
    if (!requiredOK) {
      ElMessage.error('位置信息与系统名称为必填')
      return
    }
    const fields = [
      'location','systemName','ipAddress','remoteMethod',
      'systemAccount','systemPassword','remoteAccount','remotePassword',
      'techLeader','techContact','techWechat','remark','remoteIp'
    ]
    const hasContent = fields.some(k => trim((current as any)[k]))
    if (!hasContent) {
      ElMessage.warning('未填写任何信息，未保存')
      return
    }
    const payload = { ...current, attachments: JSON.stringify(current.attachmentsParsed || []) }
    if (current.id) {
      await request({ url: `/crm/tech-info/${current.id}`, method: 'put', data: payload })
      ElMessage.success('已更新条目')
    } else {
      await request({ url: `/crm/customer/${props.customerId}/tech-info`, method: 'post', data: payload })
      ElMessage.success('已新增条目')
    }
    await load()
    resetCurrent()
  } finally {
    saving.value = false
  }
}

const removeById = async (row: any) => {
  if (row && row.id) {
    await request({ url: `/crm/tech-info/${row.id}`, method: 'delete' })
    ElMessage.success('已删除')
    await load()
  }
}

const onUploadAttachmentSuccess = async (res: any, file: any, target: any) => {
  const currentAttachments = Array.isArray(target.attachmentsParsed) ? target.attachmentsParsed : []
  const newList = [...(currentAttachments as any[]), { name: file.name, url: res.data }]
  target.attachmentsParsed = newList
  try {
    if (target.id) {
      const payload = { ...target, attachments: JSON.stringify(newList) }
      await request({ url: `/crm/tech-info/${target.id}`, method: 'put', data: payload })
    }
    ElMessage.success('附件已上传')
  } catch {
    ElMessage.error('附件保存失败')
  }
}
const onUploadError = () => { ElMessage.error('上传失败') }
const beforeUpload = (file: File) => {
  const max = 500 * 1024 * 1024
  if (file.size > max) {
    ElMessage.error('文件大小超过500MB限制')
    return false
  }
  return true
}

const removeAttachmentCurrent = async (idx: number) => {
  const currentAttachments = Array.isArray(current.attachmentsParsed) ? current.attachmentsParsed : []
  ;(currentAttachments as any[]).splice(idx, 1)
  current.attachmentsParsed = currentAttachments
  if (current.id) {
      const payload = { ...current, attachments: JSON.stringify(currentAttachments) }
    await request({ url: `/crm/tech-info/${current.id}`, method: 'put', data: payload })
    await load()
  }
}
const removeAttachment = async (row: any, idx: number) => {
  if (!row) return
  const rowAttachments = Array.isArray(row.attachmentsParsed) ? row.attachmentsParsed : []
  ;(rowAttachments as any[]).splice(idx, 1)
  row.attachmentsParsed = rowAttachments
  if (row.id) {
    const payload = { ...row, attachments: JSON.stringify(rowAttachments) }
    await request({ url: `/crm/tech-info/${row.id}`, method: 'put', data: payload })
    await load()
  }
}

const onSuccessForRow = (row:any) => {
  return (res:any, file:any) => onUploadAttachmentSuccess(res, file, row)
}
const onSuccessForCurrent = (res:any, file:any) => {
  return onUploadAttachmentSuccess(res, file, current)
}
const beforeUnloadHandler = (e: BeforeUnloadEvent) => {
  if (isChanged()) {
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
const loadRemoteMethods = async () => {
  try {
    const res:any = await request({ url: '/system/config/list', method: 'get', params: { size: 100, current: 1 } })
    const configList = res.records || res || []
    const general = configList.find((item:any) => item.configKey === 'general_settings')
    let cfg:any = {}
    try { cfg = general?.configValue ? JSON.parse(general.configValue) : {} } catch { cfg = {} }
    const custom = cfg.customCategories || {}
    const keys = Object.keys(custom || {})
    const pickByName = (keyword:string) => {
      const k = keys.find(name => String(name||'').includes(keyword))
      return k ? (Array.isArray(custom[k]) ? custom[k] : []) : []
    }
    let rmList:string[] = Array.isArray(cfg.remoteMethods) ? cfg.remoteMethods : pickByName('远程方式')
    remoteMethods.value = rmList.length ? rmList : ['TeamViewer','AnyDesk','RDP','VNC']
  } catch { remoteMethods.value = [] }
}
const loadContacts = async () => {
  if (!props.customerId) return
  const res:any = await request({ url: '/crm/contact/list', method: 'get', params: { customerId: props.customerId } })
  contactOptions.value = Array.isArray(res) ? res : (res?.records || [])
}
const onTechLeaderChange = (id:any) => {
  const c = (contactOptions.value as any[]).find((x:any)=>String(x.id) === String(id))
  if (!c) return
  current.techLeader = c.name || ''
  current.techContact = c.phone || c.mobile || ''
  current.techWechat = c.wechat || c.wechatId || c.wechatAccount || ''
}

watch(() => props.customerId, async () => { await load(); await loadRemoteMethods(); await loadContacts() }, { immediate: true })
</script>
