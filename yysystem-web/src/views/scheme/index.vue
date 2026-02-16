<template>
  <div class="app-container">
    <div class="header-actions">
      <el-button v-if="canCreate" type="primary" @click="handleAdd">新增方案</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" style="width: 100%">
      <el-table-column prop="schemeNo" label="方案编号" width="150" />
      <el-table-column prop="customerName" label="客户名称" width="180" />
      <el-table-column prop="title" label="方案标题" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="salesName" label="销售负责人" width="120" />
      <el-table-column prop="designerName" label="设计师" width="120" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button link type="primary" @click="handleEdit(row)" v-if="canUpdate && (isAdmin || row.status === 10)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)" v-if="canDelete && (isAdmin || row.status === 10)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="1000px" top="5vh">
      <el-form :model="form" label-width="100px" :rules="formRules" ref="formRef">
        <el-form-item label="客户" prop="customerId">
          <el-select 
            v-model="form.customerId" 
            placeholder="选择客户" 
            style="width: 100%" 
            :disabled="!canEdit"
            filterable
          >
            <el-option
              v-for="item in customerOptions"
              :key="item.id"
              :label="item.customerName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="方案标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入方案标题" :disabled="!canEdit" />
        </el-form-item>
        
        <el-form-item label="客户要求" prop="customerRequirements">
          <div class="info-tip" v-if="canEdit">
            <el-icon><InfoFilled /></el-icon>
            <div class="tip-content">
              <div class="tip-title">客户要求填写规范：</div>
              <div class="tip-item">1. 需求的主题：老馆改造，功能区新建，智能化建设，全馆规划等要求</div>
              <div class="tip-item">2. 需求的详细描述：改造范围要求，什么功能区，智能化是单独添加？还是全新规划，全馆规划的详细内容。</div>
            </div>
          </div>
          <el-input
            v-model="form.customerRequirements"
            type="textarea"
            :rows="6"
            placeholder="请按照上方规范填写客户要求..."
            :disabled="!canEdit"
            show-word-limit
          />
          <div class="warning-tip" v-if="canEdit && form.customerRequirements && form.customerRequirements.length < 100">
            <el-icon color="#E6A23C"><WarningFilled /></el-icon>
            <span>客户要求需超过100字才能使用AI生成方案</span>
          </div>
        </el-form-item>

        <!-- 解决方案 -->
        <el-form-item label="解决方案">
          <div class="solution-section">
            <div class="ai-actions" v-if="canEdit">
              <el-button 
                type="primary" 
                :loading="generating" 
                :disabled="!canGenerateAI"
                @click="generateSolution"
              >
                <el-icon><MagicStick /></el-icon>
                AI生成方案
              </el-button>
              <span class="ai-tip" v-if="!form.customerRequirements">请先填写客户要求</span>
              <span class="ai-tip warning" v-else-if="form.customerRequirements.length < 100">客户要求需超过100字</span>
            </div>
            
            <!-- AI生成进度条 -->
            <div class="progress-section" v-if="generating">
              <el-progress :percentage="aiProgress" :stroke-width="10" :show-text="false" />
              <div class="progress-text">
                <el-icon class="loading-icon"><Loading /></el-icon>
                <span>AI正在生成方案，请耐心等待...</span>
              </div>
            </div>
            
            <!-- 提示信息 -->
            <div class="info-tip" v-if="canEdit && !generating">
              <el-icon><InfoFilled /></el-icon>
              <div class="tip-content">
                <div class="tip-item">AI 生成的方案将显示在此框内，生成后你可以进行手动编辑调整。生成方案可能需要1-10分钟，如果是你自己写，估计1天都写不完，所以请耐心等待，偷着乐吧，你！</div>
              </div>
            </div>
            
            <el-input
              v-model="form.solutionContent"
              type="textarea"
              :rows="15"
              placeholder="解决方案内容..."
              :disabled="!canEdit"
              class="solution-textarea"
            />
          </div>
        </el-form-item>

        <!-- 客户资料上传 -->
        <el-form-item label="客户资料">
          <div class="customer-files-section">
            <el-upload
              ref="uploadRef"
              action="/api/common/upload"
              :headers="uploadHeaders"
              :on-success="handleUploadSuccess"
              :on-remove="handleUploadRemove"
              :before-upload="beforeUpload"
              v-model:file-list="fileList"
              :disabled="!canEdit"
              multiple
              :show-file-list="false"
            >
              <el-button type="primary" :disabled="!canEdit">
                <el-icon><Upload /></el-icon>
                上传客户资料
              </el-button>
            </el-upload>
            
            <div class="info-tip" style="margin-bottom: 0; margin-top: 12px;">
              <el-icon><InfoFilled /></el-icon>
              <div class="tip-content">
                <div class="tip-item">客户相关资料：支持压缩包，图片，文件，视频格式。单个文件不要超过300MB</div>
              </div>
            </div>
            
            <!-- 文件列表 -->
            <div class="file-list" v-if="fileList.length > 0">
              <div class="file-item" v-for="file in fileList" :key="file.uid">
                <el-icon class="file-icon"><Document /></el-icon>
                <span class="file-name" :title="file.name">{{ truncateFileName(file.name) }}</span>
                <div class="file-actions">
                  <el-button link type="primary" size="small" @click="handleDownload(file)">下载</el-button>
                  <el-button 
                    v-if="canDeleteFile" 
                    link 
                    type="danger" 
                    size="small" 
                    @click.stop="handleRemoveFile(file)"
                  >删除</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ canEdit ? '取消' : '关闭' }}</el-button>
          <el-button v-if="canEdit" type="info" @click="handleSaveDraft" :loading="savingDraft">保存草稿</el-button>
          <el-button v-if="canEdit" type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { MagicStick, Upload, Document, WarningFilled, InfoFilled, Loading } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

// 权限检查函数
const hasPermission = (permCode: string) => {
  const perms: string[] = userStore.userInfo?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canCreate = computed(() => hasPermission('SOL:create'))
const canUpdate = computed(() => hasPermission('SOL:update'))
const canDelete = computed(() => hasPermission('SOL:delete'))

// 状态
const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const customerOptions = ref<any[]>([])
const formRef = ref<FormInstance>()
const uploadRef = ref()

// 查询参数
const queryParams = reactive({
  current: 1,
  size: 10
})

// 表单数据
const form = reactive({
  id: null as number | null,
  customerId: null as number | null,
  title: '',
  customerRequirements: '',
  solutionContent: ''
})

// 文件列表
const fileList = ref<any[]>([])

// 当前编辑的行数据
const currentRow = ref<any>(null)

// 用户权限
const userPermissions = computed(() => {
  return userStore.userInfo?.permissionCodes || []
})

// 是否是管理员（拥有所有权限或总经理角色）
const isAdmin = computed(() => {
  if (userPermissions.value.includes('*')) return true
  if (userPermissions.value.length > 150) return true
  const hasAllSchemePerms = ['SOL:list', 'SOL:get', 'SOL:create', 'SOL:update', 'SOL:delete'].every(
    perm => userPermissions.value.includes(perm)
  )
  return hasAllSchemePerms
})

// 是否是设计师（拥有方案设计权限）
const isDesigner = computed(() => {
  const hasDesign = userPermissions.value.includes('SOL:design')
  const hasUpdate = userPermissions.value.includes('SOL:update')
  console.log('权限检查 - SOL:design:', hasDesign, 'SOL:update:', hasUpdate)
  return hasDesign || hasUpdate
})

// 是否可编辑（草稿状态/设计中状态或新增，或管理员/设计师）
const canEdit = computed(() => {
  console.log('canEdit检查 - isAdmin:', isAdmin.value, 'isDesigner:', isDesigner.value, 'status:', currentRow.value?.status)
  if (isAdmin.value) return true
  // 设计师可以编辑设计中状态的方案
  if (isDesigner.value && currentRow.value?.status === 20) {
    console.log('设计师可以编辑设计中状态的方案')
    return true
  }
  // 销售只能编辑草稿状态
  return !currentRow.value || currentRow.value.status === 10
})

// 是否可删除文件（草稿状态或管理员/设计师）
const canDeleteFile = computed(() => {
  if (isAdmin.value) return true
  // 设计师可以删除设计中状态的附件
  if (isDesigner.value && currentRow.value?.status === 20) return true
  // 销售只能删除草稿状态的附件
  return !currentRow.value || currentRow.value.status === 10
})

// 是否可生成AI（客户要求超过100字）
const canGenerateAI = computed(() => {
  return form.customerRequirements && form.customerRequirements.length >= 100
})

// 上传请求头
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return { Authorization: token ? `Bearer ${token}` : '' }
})

// AI生成状态
const generating = ref(false)
const aiProgress = ref(0)
const savingDraft = ref(false)
const submitting = ref(false)

// 表单验证规则
const formRules: FormRules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  title: [{ required: true, message: '请输入方案标题', trigger: 'blur' }],
  customerRequirements: [{ required: true, message: '请输入客户要求', trigger: 'blur' }]
}

// 状态映射
const getStatusType = (status: number) => {
  const map: Record<number, string> = {
    10: 'info', 20: 'warning', 30: 'warning', 40: 'danger', 50: 'success', 60: 'success'
  }
  return map[status] || 'info'
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    10: '草稿', 20: '设计中', 30: '待审批', 40: '已驳回', 50: '已通过', 60: '已报送'
  }
  return map[status] || '未知'
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res: any = await request({
      url: '/scheme/list',
      method: 'get',
      params: queryParams
    })
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载数据失败', error)
  } finally {
    loading.value = false
  }
}

// 加载客户列表
const loadCustomers = async () => {
  try {
    const res: any = await request({
      url: '/crm/customer/list',
      method: 'get',
      params: { size: 1000 }
    })
    customerOptions.value = res.records || []
  } catch (error) {
    console.error('加载客户列表失败', error)
  }
}

// 新增
const handleAdd = () => {
  currentRow.value = null
  dialogTitle.value = '新增方案'
  form.id = null
  form.customerId = null
  form.title = ''
  form.customerRequirements = ''
  form.solutionContent = ''
  fileList.value = []
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  currentRow.value = row
  dialogTitle.value = '编辑方案'
  form.id = row.id
  form.customerId = row.customerId
  form.title = row.title
  form.customerRequirements = row.customerRequirements || ''
  form.solutionContent = row.solutionContent || ''
  
  // 加载文件列表
  if (row.customerFiles) {
    try {
      const files = JSON.parse(row.customerFiles)
      fileList.value = files.map((f: any, i: number) => ({
        ...f,
        uid: f.uid || `file_${i}_${Date.now()}`
      }))
    } catch {
      fileList.value = []
    }
  } else {
    fileList.value = []
  }
  
  dialogVisible.value = true
}

// 查看
const handleView = (row: any) => {
  router.push(`/scheme/detail/${row.id}`)
}

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该方案？', '提示', { type: 'warning' })
    await request({ url: `/scheme/${row.id}`, method: 'delete' })
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    console.error('删除失败', error)
  }
}

// AI生成方案
const generateSolution = async () => {
  if (!canGenerateAI.value) {
    ElMessage.warning('客户要求需超过100字才能使用AI生成')
    return
  }
  
  generating.value = true
  aiProgress.value = 0
  
  const progressTimer = setInterval(() => {
    if (aiProgress.value < 90) {
      aiProgress.value += Math.random() * 10
    }
  }, 500)
  
  try {
    const res: any = await request({
      url: '/scheme/generate-solution',
      method: 'post',
      data: {
        customerRequirements: form.customerRequirements,
        customerId: form.customerId
      },
      timeout: 600000
    })
    form.solutionContent = res
    aiProgress.value = 100
    ElMessage.success('AI生成成功')
  } catch (error) {
    console.error('AI生成失败', error)
    ElMessage.error('AI生成失败，请重试')
  } finally {
    clearInterval(progressTimer)
    generating.value = false
    aiProgress.value = 0
  }
}

// 文件上传成功
const handleUploadSuccess = (res: any, file: any, files: any[]) => {
  if (res.code === 200) {
    fileList.value = files
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
    const idx = fileList.value.findIndex(f => f.uid === file.uid)
    if (idx > -1) fileList.value.splice(idx, 1)
  }
}

// 文件移除
const handleUploadRemove = (file: any, files: any[]) => {
  fileList.value = files
}

// 手动删除文件
const handleRemoveFile = async (file: any) => {
  try {
    await ElMessageBox.confirm('确认删除该文件？', '提示', { type: 'warning' })
  } catch {
    return
  }
  
  fileList.value = fileList.value.filter(f => f.uid !== file.uid)
  
  if (form.id) {
    try {
      const customerFilesValue = fileList.value.length > 0 
        ? JSON.stringify(extractFileData()) 
        : '[]'
      
      const data = {
        id: form.id,
        customerFiles: customerFilesValue
      }
      await request({ url: '/scheme', method: 'put', data })
      ElMessage.success('文件已删除')
    } catch (error) {
      console.error('更新数据库失败', error)
      ElMessage.error('删除失败')
    }
  } else {
    ElMessage.success('文件已删除')
  }
}

// 上传前校验
const beforeUpload = (file: File) => {
  const maxSize = 300 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过300MB')
    return false
  }
  return true
}

// 下载文件
const handleDownload = (file: any) => {
  const url = file.response?.data || file.url
  if (url) {
    window.open(url, '_blank')
  } else {
    ElMessage.warning('文件链接不存在')
  }
}

// 截断文件名
const truncateFileName = (name: string) => {
  if (name.length <= 20) return name
  const ext = name.substring(name.lastIndexOf('.'))
  const baseName = name.substring(0, name.lastIndexOf('.'))
  if (baseName.length <= 16) return name
  return baseName.substring(0, 16) + '...' + ext
}

// 提取文件数据
const extractFileData = () => {
  return fileList.value.map(f => ({
    uid: f.uid,
    name: f.name,
    url: f.response?.data || f.url
  }))
}

// 保存草稿
const handleSaveDraft = async () => {
  if (!form.customerId || !form.title) {
    ElMessage.warning('请填写客户和方案标题')
    return
  }
  
  savingDraft.value = true
  try {
    const data = {
      ...form,
      status: 10,
      customerFiles: fileList.value.length > 0 ? JSON.stringify(extractFileData()) : null
    }
    
    if (form.id) {
      await request({ url: '/scheme', method: 'put', data })
    } else {
      await request({ url: '/scheme', method: 'post', data })
    }
    
    ElMessage.success('草稿保存成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('保存失败', error)
    ElMessage.error('保存失败')
  } finally {
    savingDraft.value = false
  }
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      '提交后将进入设计阶段，销售不能再修改内容。确认提交吗？',
      '确认提交',
      { type: 'warning' }
    )
  } catch {
    return
  }
  
  submitting.value = true
  try {
    const data = {
      ...form,
      status: 20,
      customerFiles: fileList.value.length > 0 ? JSON.stringify(extractFileData()) : null
    }
    
    if (form.id) {
      await request({ url: '/scheme', method: 'put', data })
    } else {
      await request({ url: '/scheme', method: 'post', data })
    }
    
    ElMessage.success('提交成功，已进入设计阶段')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败', error)
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

// 分页
const handleSizeChange = (val: number) => {
  queryParams.size = val
  loadData()
}

const handleCurrentChange = (val: number) => {
  queryParams.current = val
  loadData()
}

// 初始化
onMounted(() => {
  loadData()
  loadCustomers()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.header-actions {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.solution-section {
  width: 100%;
}

.ai-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.ai-tip {
  color: #909399;
  font-size: 13px;
}

.ai-tip.warning {
  color: #E6A23C;
}

.progress-section {
  margin-bottom: 12px;
}

.progress-text {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  color: #409EFF;
  font-size: 13px;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.solution-textarea {
  font-size: 14px;
  line-height: 1.8;
}

.customer-files-section {
  width: 100%;
}

.file-list {
  margin-top: 12px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-bottom: 1px solid #ebeef5;
}

.file-item:last-child {
  border-bottom: none;
}

.file-icon {
  margin-right: 8px;
  color: #409EFF;
}

.file-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.file-actions {
  display: flex;
  gap: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
