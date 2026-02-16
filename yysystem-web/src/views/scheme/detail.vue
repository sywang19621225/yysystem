<template>
  <div class="app-container">
    <el-page-header @back="goBack" title="方案详情" />
    
    <el-card class="detail-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="方案编号">{{ scheme.schemeNo }}</el-descriptions-item>
            <el-descriptions-item label="方案标题">{{ scheme.title }}</el-descriptions-item>
            <el-descriptions-item label="客户名称">{{ scheme.customerName }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(scheme.status)">{{ getStatusText(scheme.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="销售负责人">{{ scheme.salesName }}</el-descriptions-item>
            <el-descriptions-item label="设计师">{{ scheme.designerName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ scheme.createTime }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ scheme.updateTime }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        
        <el-tab-pane label="客户要求" name="requirements">
          <div class="section-content">
            <h4>客户要求</h4>
            <p>{{ scheme.customerRequirements || '暂无' }}</p>
            
            <h4>客户资料</h4>
            <div v-if="customerFiles.length > 0" class="file-list">
              <div 
                v-for="(file, index) in customerFiles" 
                :key="index"
                class="file-item"
              >
                <el-icon><Document /></el-icon>
                <el-link 
                  :href="file.url || file.response?.data"
                  target="_blank"
                  type="primary"
                >
                  {{ file.name }}
                </el-link>
              </div>
            </div>
            <p v-else>暂无资料</p>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="文字方案" name="text">
          <div class="section-content">
            <div class="action-bar">
              <el-button type="primary" @click="generateAI" :loading="generating" v-if="hasPermission('SOL:update')">
                <el-icon><MagicStick /></el-icon>
                AI生成方案
              </el-button>
              <el-button type="success" @click="saveText" v-if="hasPermission('SOL:update')">保存</el-button>
            </div>
            <el-input
              v-model="textContent"
              type="textarea"
              :rows="20"
              placeholder="请输入或生成文字方案内容"
            />
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="设计文件" name="design">
          <div class="section-content">
            <el-upload
              v-if="hasPermission('SOL:design')"
              action="/api/common/upload"
              :headers="uploadHeaders"
              :on-success="handleUploadSuccess"
              :before-upload="beforeUpload"
              multiple
            >
              <el-button type="primary">上传设计文件</el-button>
              <template #tip>
                <div class="el-upload__tip">支持jpg/png/pdf/dwg等格式，单个文件不超过50MB</div>
              </template>
            </el-upload>
            
            <el-table :data="designFiles" style="margin-top: 20px">
              <el-table-column prop="name" label="文件名" />
              <el-table-column prop="type" label="类型" width="100" />
              <el-table-column prop="uploader" label="上传人" width="120" />
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-link :href="row.url" target="_blank" type="primary">下载</el-link>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="审批记录" name="approval">
          <div class="section-content">
            <el-timeline>
              <el-timeline-item
                v-for="record in approvalRecords"
                :key="record.id"
                :type="record.approvalStatus === 1 ? 'success' : 'danger'"
                :timestamp="record.approvalTime"
              >
                <h4>{{ record.approverName }} - {{ record.approvalStatus === 1 ? '通过' : '驳回' }}</h4>
                <p>{{ record.approvalOpinion }}</p>
              </el-timeline-item>
            </el-timeline>
            
            <div v-if="canApprove" class="approval-form">
              <el-divider />
              <h4>审批操作</h4>
              <el-input
                v-model="approvalOpinion"
                type="textarea"
                :rows="3"
                placeholder="请输入审批意见"
              />
              <div class="approval-actions">
                <el-button type="success" @click="handleApprove(1)">通过</el-button>
                <el-button type="danger" @click="handleApprove(2)">驳回</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MagicStick, Document } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 上传请求头（包含认证token）
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    Authorization: token ? `Bearer ${token}` : ''
  }
})

const schemeId = route.params.id

const activeTab = ref('basic')
const scheme = ref<any>({})
const textContent = ref('')
const generating = ref(false)
const designFiles = ref<any[]>([])
const customerFiles = ref<any[]>([])
const approvalRecords = ref<any[]>([])
const approvalOpinion = ref('')

const hasPermission = (perm: string) => {
  const userInfo: any = userStore.userInfo
  const perms: string[] = userInfo?.permissionCodes || []
  return perms.some((code: string) => code.startsWith(perm + ':'))
}

const canApprove = computed(() => {
  return hasPermission('SOL:approve') && scheme.value.status === 30
})

const getStatusType = (status: number) => {
  const map: Record<number, string> = {
    10: 'info',
    20: 'warning',
    30: 'warning',
    40: 'danger',
    50: 'success',
    60: 'success'
  }
  return map[status] || 'info'
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    10: '草稿',
    20: '设计中',
    30: '待审批',
    40: '已驳回',
    50: '已通过',
    60: '已报送'
  }
  return map[status] || '未知'
}

const loadScheme = async () => {
  try {
    const res: any = await request({
      url: `/scheme/${schemeId}`,
      method: 'get'
    })
    scheme.value = res
    textContent.value = res.textContent || ''
    designFiles.value = JSON.parse(res.designFiles || '[]')
    customerFiles.value = JSON.parse(res.customerFiles || '[]')
  } catch (error) {
    console.error('加载方案失败', error)
  }
}

const generateAI = async () => {
  generating.value = true
  try {
    const res: any = await request({
      url: `/scheme/${schemeId}/generate-ai`,
      method: 'post'
    })
    textContent.value = res
    ElMessage.success('AI生成成功')
  } catch (error) {
    console.error('AI生成失败', error)
  } finally {
    generating.value = false
  }
}

const saveText = async () => {
  try {
    await request({
      url: '/scheme',
      method: 'put',
      data: {
        id: schemeId,
        textContent: textContent.value
      }
    })
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存失败', error)
  }
}

const handleUploadSuccess = (response: any) => {
  designFiles.value.push({
    name: response.name,
    url: response.url,
    type: response.type,
    uploader: userStore.userInfo?.name
  })
  ElMessage.success('上传成功')
}

const beforeUpload = (file: File) => {
  const maxSize = 50 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过50MB')
    return false
  }
  return true
}

const handleApprove = async (status: number) => {
  try {
    await request({
      url: `/scheme/${schemeId}/approve`,
      method: 'post',
      data: {
        approvalStatus: status,
        approvalOpinion: approvalOpinion.value
      }
    })
    ElMessage.success(status === 1 ? '审批通过' : '已驳回')
    loadScheme()
  } catch (error) {
    console.error('审批失败', error)
  }
}

const goBack = () => {
  router.push('/crm/solution')
}

onMounted(() => {
  loadScheme()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.detail-card {
  margin-top: 20px;
}
.section-content {
  padding: 20px;
}
.action-bar {
  margin-bottom: 20px;
}
.approval-form {
  margin-top: 30px;
}
.approval-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}
h4 {
  margin: 20px 0 10px;
  font-weight: 600;
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style>
