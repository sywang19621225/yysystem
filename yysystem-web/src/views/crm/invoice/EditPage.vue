<template>
  <div class="app-container">
    <div class="header-actions">
      <el-button @click="goBack">返回</el-button>
      <el-button type="primary" @click="handleSubmit">
        {{ isSalesNewInvoice ? '开票申请' : (isFinanceUser && form.id && form.invoiceStatus === 'WAITING') ? '开具发票' : '保存' }}
      </el-button>
    </div>
    <el-form :model="form" label-width="120px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="客户" required>
            <el-select 
              v-model="form.customerId" 
              filterable 
              style="width:100%" 
              @change="onCustomerChange" 
              clearable
              placeholder="请输入姓名或单位搜索"
              :disabled="true">
              <el-option label="请选择客户" value="" disabled />
              <el-option 
                v-for="c in customers" 
                :key="c.id" 
                :label="c.customerName + (c.company ? ' - ' + c.company : '')" 
                :value="c.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="isSalesUser || isFinanceUser || isAdminUser">
          <el-form-item label="合同附件">
            <el-upload 
              action="/api/common/upload"
              :headers="uploadHeaders"
              :show-file-list="false" 
              :on-success="onContractUploadSuccess"
              :before-upload="beforeUpload"
              accept=".pdf,.jpg"
              v-if="(isSalesUser && !readonlySalesAfterDone) || isFinanceUser || isAdminUser">
              <el-button>上传合同附件</el-button>
            </el-upload>
            <div style="margin-top:6px;color:#999;font-size:12px;">只能上传PDF、JPG文件，大小不超过100MB，下载按原后缀名保存，支持图片和PDF预览</div>
            <div v-if="form.contractAttachment" style="margin-top: 8px;">
              <div style="font-size:12px;color:#666;">文件：{{ form.contractAttachmentName || (form.contractAttachment.split('/').pop() || '') }}</div>
              <div style="font-size:12px;color:#666;">路径：{{ form.contractAttachment }}</div>
              <el-button type="primary" link @click="downloadFile(form.contractAttachment)">
                <el-icon><Download /></el-icon>下载
              </el-button>
              <el-button type="info" link @click="previewFile(form.contractAttachment)">
                <el-icon><View /></el-icon>预览
              </el-button>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="合同" required>
            <el-select v-model="form.contractId" filterable style="width:100%" clearable :disabled="true">
              <el-option label="请选择合同" value="" disabled />
              <el-option 
                v-for="c in filteredContracts" 
                :key="c.id" 
                :label="c.contractNo" 
                :value="c.id" />
            </el-select>
            <div v-if="form.contractId" style="margin-top: 8px; font-size: 12px; color: #666;">
              已选择: {{ contracts.find(c => c.id === form.contractId)?.contractNo || '未知合同' }}
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="信用代号">
            <el-input v-model="form.creditCode" :disabled="true" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="发票类型" required>
            <el-select
              v-model="form.invoiceType"
              filterable
              clearable
              style="width:100%"
              :disabled="readonlySalesView || (readonlySalesAfterDone && !(isFinanceUser || isAdminUser))"
              placeholder="请选择发票类型"
            >
              <el-option v-for="t in invoiceTypes" :key="t" :label="t" :value="t" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="开票单位" required>
            <el-select
              v-model="form.invoiceUnit"
              filterable
              clearable
              style="width:100%"
              placeholder="请选择开票单位"
              :disabled="readonlySalesView || (readonlySalesAfterDone && !(isFinanceUser || isAdminUser))"
              @change="onInvoiceUnitChange"
            >
              <el-option label="原邑智能科技（上海）有限公司" value="原邑智能科技（上海）有限公司" />
              <el-option label="原邑信息科技（上海）有限公司" value="原邑信息科技（上海）有限公司" />
              <el-option label="安徽维斯顿智能科技有限公司" value="安徽维斯顿智能科技有限公司" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="开票金额">
            <el-input-number 
              v-model="form.invoiceAmount" 
              :precision="2" 
              :step="100" 
              :max="maxInvoiceAmount"
              style="width:100%" 
              :disabled="readonlySalesView || ((readonlySalesAfterDone || maxInvoiceAmount === 0) && !(isFinanceUser || isAdminUser))" 
            />
            <div style="margin-top:4px;font-size:12px;color:#999;">
              明细合计: ¥{{ detailTotal.toFixed(2) }}
              <el-button v-if="!readonlySalesView && (!readonlySalesAfterDone || isFinanceUser || isAdminUser)" type="primary" link size="small" @click="syncAmountFromDetail">同步明细</el-button>
              <el-button v-if="!readonlySalesView && (!readonlySalesAfterDone || isFinanceUser || isAdminUser) && contractInfo && maxInvoiceAmount > 0" type="danger" link size="small" @click="syncUninvoicedAmount">同步未开票</el-button>
            </div>
            <div v-if="form.contractId && contractInfo" style="margin-top:4px;font-size:12px;color:#666;">
              <el-tag type="info" size="small">合同: ¥{{ contractInfo.contractAmount?.toFixed(2) || '0.00' }}</el-tag>
              <el-tag type="warning" size="small" style="margin-left:4px;">已开: ¥{{ contractInfo.invoicedAmount?.toFixed(2) || '0.00' }}</el-tag>
              <el-tag :type="maxInvoiceAmount > 0 ? 'danger' : 'info'" size="small" style="margin-left:4px;">
                未开: ¥{{ (contractInfo.contractAmount - (contractInfo.invoicedAmount || 0)).toFixed(2) }}
                <span v-if="maxInvoiceAmount === 0" style="color: #F56C6C; font-weight: bold;">(已完全开票)</span>
              </el-tag>
            </div>
            <div v-if="maxInvoiceAmount !== undefined && maxInvoiceAmount >= 0" style="margin-top:4px;font-size:12px;color:#909399;">
              最大可开票金额: ¥{{ maxInvoiceAmount.toFixed(2) }}
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="申请备注">
            <el-input v-model="form.applyRemark" type="textarea" :disabled="readonlySalesView || (readonlySalesAfterDone && !(isFinanceUser || isAdminUser))" />
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="!hideFinanceFields">
          <el-form-item label="开票状态">
            <el-select v-model="form.invoiceStatus" style="width:100%" :disabled="!(isFinanceUser || isAdminUser)">
              <el-option 
                v-for="status in invoiceStatusDict" 
                :key="status" 
                :label="status" 
                :value="getInvoiceStatusValue(status)" 
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="!hideFinanceFields">
          <el-form-item label="发票号码" required>
            <el-input v-model="form.invoiceNo" :disabled="!(isFinanceUser || isAdminUser)" placeholder="请输入发票号码" />
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="!hideFinanceFields">
          <el-form-item label="发票附件">
            <el-upload v-if="isFinanceUser || isAdminUser"
              action="/api/common/upload"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="onUploadSuccess"
              :before-upload="beforeUpload"
              accept=".pdf,.jpg">
              <el-button>上传附件</el-button>
            </el-upload>
            <div style="margin-top:6px;color:#999;font-size:12px;">只能上传PDF、JPG文件，大小不超过100MB，下载按原后缀名保存，支持图片和PDF预览</div>
            <div v-if="form.invoiceAttachment" style="margin-top: 8px;">
              <div style="font-size:12px;color:#666; word-break: break-all; white-space: normal; line-height: 1.4;">文件：{{ form.invoiceAttachmentName || (form.invoiceAttachment.split('/').pop() || '') }}</div>
              <div style="font-size:12px;color:#666; word-break: break-all; white-space: normal; line-height: 1.4;">路径：{{ form.invoiceAttachment }}</div>
              <el-button type="primary" link @click="downloadFile(form.invoiceAttachment)">
                <el-icon><Download /></el-icon>下载
              </el-button>
              <el-button type="info" link @click="previewFile(form.invoiceAttachment)">
                <el-icon><View /></el-icon>预览
              </el-button>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="!hideFinanceFields">
          <el-form-item label="开票人">
            <el-select v-model="form.invoicerId" filterable style="width:100%" clearable :disabled="!(isFinanceUser || isAdminUser)">
              <el-option label="全部开票人" value="" />
              <el-option v-for="u in users" :key="u.id" :label="u.name" :value="u.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="!hideFinanceFields">
          <el-form-item label="开票时间">
            <el-date-picker v-model="form.invoiceTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" :disabled="!(isFinanceUser || isAdminUser)" />
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="!hideFinanceFields">
          <el-form-item label="发票给付说明">
            <el-input v-model="form.invoiceGiveDesc" :disabled="!(isFinanceUser || isAdminUser || readonlySalesAfterDone)" />
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="!hideFinanceFields">
          <el-form-item label="给付时间">
            <el-date-picker v-model="form.giveTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" :disabled="!(isFinanceUser || isAdminUser || readonlySalesAfterDone)" />
          </el-form-item>
        </el-col>
      </el-row>
      <div class="detail-header">
        <span>开票明细</span>
        <div style="display:flex;gap:10px;">
          <el-button v-if="!readonlySalesAfterDone || isFinanceUser || isAdminUser" type="primary" link @click="addDetail">添加明细</el-button>
          <el-button v-if="!readonlySalesAfterDone || isFinanceUser || isAdminUser" type="warning" link @click="importContractDetails">调用合同明细</el-button>
        </div>
      </div>
      <el-table :data="form.detailList" border>
        <el-table-column prop="productName" label="商品名称" min-width="200">
          <template #default="scope">
            <el-input v-model="scope.row.productName" :disabled="readonlySalesAfterDone && !(isFinanceUser || isAdminUser)" />
          </template>
        </el-table-column>
        <el-table-column prop="productSpec" label="型号" min-width="160">
          <template #default="scope">
            <el-input v-model="scope.row.productSpec" :disabled="readonlySalesAfterDone && !(isFinanceUser || isAdminUser)" />
          </template>
        </el-table-column>
        <el-table-column prop="salesQuantity" label="数量" width="120">
          <template #default="scope">
            <el-input-number v-model="scope.row.salesQuantity" :min="1" @change="onDetailChange" :disabled="readonlySalesAfterDone && !(isFinanceUser || isAdminUser)" />
          </template>
        </el-table-column>
        <el-table-column prop="salesPrice" label="单价" width="140">
          <template #default="scope">
            <el-input-number v-model="scope.row.salesPrice" :precision="2" :step="100" @change="onDetailChange" :disabled="readonlySalesAfterDone && !(isFinanceUser || isAdminUser)" />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="140">
          <template #default="scope">
            {{ (Number(scope.row.salesPrice||0) * Number(scope.row.salesQuantity||0)).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button v-if="!readonlySalesAfterDone || isFinanceUser || isAdminUser" type="danger" link @click="removeDetail(scope.$index)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-form>
  </div>
  </template>
  
  <script setup lang="ts">
  import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download, View } from '@element-plus/icons-vue'
  import { getInvoiceById, saveInvoice, updateInvoice } from '@/api/invoice'
  import { getCustomerList } from '@/api/customer'
  import { getContractList, getContractById } from '@/api/contract'
  import request from '@/utils/request'
  import { useUserStore } from '@/store/user'
  
  const route = useRoute()
  const router = useRouter()
  
  const customers = ref<any[]>([])
  const contracts = ref<any[]>([])
  const users = ref<any[]>([])
  const userStore = useUserStore()
  const invoiceTypes = ref<string[]>([])
  const invoiceStatusDict = ref<string[]>([])
  // isSalesUser/hideFinanceFields defined later after role computations
  const isFinanceUser = computed(() => {
    const ui:any = userStore.userInfo
    const type = ui?.userType
    const roleName = ui?.roleName
    const roles = ui?.roles || []
    return type === 'finance'
      || String(roleName||'').includes('财务')
      || roles.some((r:any) => String(r.roleName||r.name||'').includes('财务') || String(r.roleKey||'').includes('finance'))
  })
  const isAdminUser = computed(() => {
    const ui:any = userStore.userInfo
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
  })
  const isSalesUser = computed(() => !isFinanceUser.value && !isAdminUser.value)
  const hideFinanceFields = computed(() => isSalesUser.value && !form.id)
  const readonlyFinanceEdit = computed(() => !!form.id && isFinanceUser.value)
  const readonlySalesAfterDone = computed(() => !!form.id && isSalesUser.value && form.invoiceStatus === 'DONE')
  const isSalesNewInvoice = computed(() => isSalesUser.value && !form.id)
  // 销售查看已有发票时只能查看不能修改
  const readonlySalesView = computed(() => !!form.id && isSalesUser.value)

  // 上传请求头（包含认证token）
  const uploadHeaders = computed(() => {
    const token = localStorage.getItem('token')
    return {
      Authorization: token ? `Bearer ${token}` : ''
    }
  })
  
  // 计算明细合计金额
  const detailTotal = computed(() => {
    return (form.detailList || []).reduce((sum: number, d: any) => {
      return sum + Number(d.salesPrice || 0) * Number(d.salesQuantity || 0)
    }, 0)
  })

  // 开票状态显示文本与值的映射
  const invoiceStatusLabelMap: Record<string, string> = {
    '待开票': 'WAITING',
    '已开票': 'DONE',
    '作废': 'VOID',
    '冲抵货款': '冲抵货款',
    '收据': '收据'
  }

  // 获取开票状态值（从显示文本转换为存储值）
  const getInvoiceStatusValue = (label: string): string => {
    return invoiceStatusLabelMap[label] || label
  }

  // 已完成状态列表
  // 计算最大可开票金额
  const maxInvoiceAmount = computed(() => {
    if (!contractInfo.value) {
      // 如果没有合同信息，返回一个较大的值（编辑已有发票时）
      return form.id ? 999999999 : 0
    }
    // 计算剩余可开票金额 = 合同金额 - 已开票金额 + 当前发票金额（如果是编辑）
    let invoicedAmount = contractInfo.value.invoicedAmount || 0
    // 如果是编辑已有发票，需要把当前发票的金额加回去（因为已开票金额包含了当前发票）
    if (form.id && form.invoiceStatus !== 'WAITING') {
      // 已开票金额已经包含了当前发票，不需要调整
    }
    const remaining = contractInfo.value.contractAmount - invoicedAmount
    return remaining > 0 ? remaining : 0
  })
  
  // 合同信息（用于显示未开票金额）
  const contractInfo = ref<any>(null)
  
  // 加载合同信息
  const loadContractInfo = async (contractId: number) => {
    if (!contractId) {
      contractInfo.value = null
      return
    }
    try {
      const c: any = await getContractById(contractId)
      if (c) {
        contractInfo.value = {
          contractNo: c.contractNo,
          contractAmount: Number(c.contractAmount || 0),
          invoicedAmount: Number(c.invoicedAmount || 0)
        }
      }
    } catch {
      contractInfo.value = null
    }
  }
  
  const form = reactive<any>({
    id: undefined,
    customerId: undefined,
    contractId: undefined,
    creditCode: '',
    auditStatus: 'PENDING',
    invoiceAmount: 0,
    applyRemark: '',
    invoiceStatus: 'WAITING',
    invoiceAttachment: '',
    invoiceAttachmentName: '',
    contractAttachment: '',
    contractAttachmentName: '',
    invoicerId: undefined,
    invoiceTime: '',
    invoiceGiveDesc: '',
    giveTime: '',
    invoiceNo: '',
    invoiceType: '',
    invoiceUnit: '',
    detailList: []
  })
  
const goBack = () => {
  const from:any = route.query?.fromContract
  if (from) router.push('/crm/contract')
  else router.push('/crm/invoice')
}
  
  const onAttachmentSuccess = (res:any, file?: any) => { 
    form.invoiceAttachment = res.data
    // 保存原始文件名信息
    if (file && file.name) {
      // 可以在这里保存原始文件名，如果需要的话
      console.log('上传文件:', file.name)
    }
  }
  const onUploadSuccess = (res: any, file: any) => {
    onAttachmentSuccess(res, file)
    form.invoiceAttachmentName = (file && file.name) ? file.name : String(res?.data || '').split('/').pop() || ''
  }
  const onContractUploadSuccess = (res: any, file: any) => {
    form.contractAttachment = res.data
    form.contractAttachmentName = (file && file.name) ? file.name : String(res?.data || '').split('/').pop() || ''
  }

  const beforeUpload = (file: File) => {
    const isAllowedType = ['application/pdf', 'image/jpeg'].includes(file.type)
    const isLt100M = file.size / 1024 / 1024 < 100

    if (!isAllowedType) {
      ElMessage.error('只能上传PDF或JPG文件')
      return false
    }
    if (!isLt100M) {
      ElMessage.error('文件大小不能超过100MB')
      return false
    }
    return true
  }

  const normalizeUrl = (u?: string) => {
    if (!u) return ''
    if (u.startsWith('http')) return u
    const path = u.startsWith('/files/') ? u : (`/files/${u.replace(/^\/+/, '')}`)
    return `${window.location.origin}${path}`
  }
  const downloadFile = async (url: string, originalName?: string) => {
    if (!url) return
    const absoluteUrl = normalizeUrl(url)
    const lastPart = decodeURIComponent(absoluteUrl.substring(absoluteUrl.lastIndexOf('/') + 1))
    let fileName = originalName || lastPart || 'invoice_attachment'
    const extMatch = fileName.match(/\.([^.]+)$/) || lastPart.match(/\.([^.]+)$/)
    const ext = (extMatch ? extMatch[1].toLowerCase() : (absoluteUrl.includes('.pdf') ? 'pdf' : 'jpg'))
    if (!fileName.endsWith(`.${ext}`)) fileName = `${fileName}.${ext}`
    fileName = fileName.replace(/[^a-zA-Z0-9._-]/g, '_')
    const mime = ext === 'pdf' ? 'application/pdf' : 'image/jpeg'
    try {
      const res = await fetch(absoluteUrl)
      const blob = await res.blob()
      const typedBlob = blob.type ? blob : new Blob([blob], { type: mime })
      const objectUrl = URL.createObjectURL(typedBlob)
      const a = document.createElement('a')
      a.href = objectUrl
      a.download = fileName
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(objectUrl)
    } catch (e) {
      const aFallback = document.createElement('a')
      aFallback.href = absoluteUrl
      aFallback.download = fileName
      document.body.appendChild(aFallback)
      aFallback.click()
      document.body.removeChild(aFallback)
    }
  }

  const previewFile = (url: string) => {
    if (!url) return
    
    // 检查是否为PDF文件
    const isPdf = url.toLowerCase().endsWith('.pdf')
    
    if (isPdf) {
      try {
        // 确保URL是绝对路径
        const absoluteUrl = normalizeUrl(url)
        
        // 使用浏览器内置PDF查看器
        const pdfWindow = window.open(absoluteUrl, '_blank')
        
        if (!pdfWindow) {
          // 如果弹窗被阻止，提供下载选项
          ElMessage.warning('PDF预览被阻止，请尝试下载查看')
          downloadFile(url)
        }
      } catch (error) {
        console.error('PDF预览失败:', error)
        ElMessage.error('PDF预览失败，请尝试下载查看')
        downloadFile(url)
      }
    } else {
      // 对于图片文件，直接在新窗口打开
      window.open(normalizeUrl(url), '_blank')
    }
  }

  
  
  // 监听合同选择变化
  watch(() => form.contractId, async (newVal) => {
    if (newVal) {
      await loadContractInfo(newVal)
    } else {
      contractInfo.value = null
    }
  })

  const onCustomerChange = (id: number) => {
    const c = customers.value.find((x:any) => x.id === id)
    if (c) {
      form.creditCode = c.creditCode || ''
      // 清空之前选择的合同
      form.contractId = undefined
      contractInfo.value = null
      console.log('已选择客户:', c.customerName, '单位:', c.company || '个人', '信用代号:', c.creditCode)
    } else {
      form.creditCode = ''
      form.contractId = undefined
    }
  }
  
  // 开票单位变更时自动分派财务经理
  const onInvoiceUnitChange = (unit: string) => {
    // 开票单位与财务经理映射关系
    const unitToFinanceManager: Record<string, number> = {
      '原邑智能科技（上海）有限公司': 2,  // 李珂
      '原邑信息科技（上海）有限公司': 2,  // 李珂
      '安徽维斯顿智能科技有限公司': 34    // 赵曼
    }
    
    const financeManagerId = unitToFinanceManager[unit]
    if (financeManagerId) {
      form.invoicerId = financeManagerId
    }
  }
  
  const addDetail = () => {
    form.detailList.push({ productName: '', productSpec: '', salesQuantity: 1, salesPrice: 0 })
  }
  const calculateTotal = () => {
    // 只在开票金额为0时自动计算，否则保留用户手动输入的金额
    if (!form.invoiceAmount || form.invoiceAmount === 0) {
      form.invoiceAmount = detailTotal.value
    }
  }
  
  // 明细变化时自动同步开票金额
  const onDetailChange = () => {
    // 只有当明细合计大于0时才更新开票金额
    if (detailTotal.value > 0) {
      form.invoiceAmount = detailTotal.value
    }
  }
  
  // 手动同步明细金额到开票金额
  const syncAmountFromDetail = () => {
    form.invoiceAmount = detailTotal.value
    ElMessage.success('开票金额已同步为明细合计')
  }
  
  // 同步未开票金额 - 按比例调整明细单价
  const syncUninvoicedAmount = () => {
    if (!contractInfo.value) return
    
    const uninvoiced = contractInfo.value.contractAmount - (contractInfo.value.invoicedAmount || 0)
    const currentTotal = detailTotal.value
    
    if (currentTotal === 0) {
      // 如果没有明细，直接设置金额
      form.invoiceAmount = uninvoiced
      ElMessage.success('开票金额已同步为未开票金额')
      return
    }
    
    // 按比例调整每个明细的单价
    const ratio = uninvoiced / currentTotal
    for (const detail of form.detailList) {
      const originalPrice = Number(detail.salesPrice || 0)
      detail.salesPrice = Number((originalPrice * ratio).toFixed(2))
    }
    
    // 重新计算开票金额
    form.invoiceAmount = detailTotal.value
    ElMessage.success(`明细单价已按比例调整，合计: ¥${form.invoiceAmount.toFixed(2)}`)
  }

  // 根据选择的客户筛选合同，只显示还有未开票金额的合同
  const filteredContracts = computed(() => {
    if (!form.customerId) return []
    return contracts.value.filter(c => {
      if (!c || c.customerId !== form.customerId) return false
      // 计算未开票金额 = 合同金额 - 已开票金额
      const contractAmount = Number(c.contractAmount || 0)
      const invoicedAmount = Number(c.invoicedAmount || 0)
      const remainingAmount = contractAmount - invoicedAmount
      // 只显示还有未开票金额的合同
      return remainingAmount > 0
    })
  })

  const removeDetail = (idx: number) => {
    form.detailList.splice(idx, 1)
    onDetailChange()
  }
  const importContractDetails = async () => {
    if (!form.contractId) {
      ElMessage.warning('请先选择合同')
      return
    }
    const c:any = await getContractById(form.contractId)
    form.detailList = (c.detailList || []).map((d:any) => ({
      productName: d.productName,
      productSpec: d.productSpec,
      salesQuantity: d.salesQuantity,
      salesPrice: d.salesPrice
    }))
    onDetailChange()
  }
  
  const handleSubmit = async () => {
    if (!form.customerId || !form.contractId) {
      ElMessage.warning('请选择客户与合同')
      return
    }

    // 检查发票类型是否填写
    if (!form.invoiceType) {
      ElMessage.warning('请选择发票类型')
      return
    }

    // 检查开票金额是否超过最大可开票金额
    if (form.invoiceAmount > maxInvoiceAmount.value) {
      ElMessage.warning(`开票金额不能超过最大可开票金额 ¥${maxInvoiceAmount.value.toFixed(2)}`)
      return
    }
    
    // 检查合同是否已完全开票
    if (maxInvoiceAmount.value === 0) {
      ElMessage.warning('该合同已完全开票，不能再开票')
      return
    }
    
    // 财务开票时，发票号码必填
    if (isFinanceUser.value && form.id && form.invoiceStatus === 'WAITING') {
      if (!form.invoiceNo || form.invoiceNo.trim() === '') {
        ElMessage.warning('请输入发票号码')
        return
      }
    }
    
    if (Array.isArray(form.detailList) && form.detailList.length) {
      calculateTotal()
    }
    try {
      if (form.id) {
      // 财务开票时，自动设置状态为已开票
      if (isFinanceUser.value && form.invoiceStatus === 'WAITING') {
        form.invoiceStatus = 'DONE'
        form.auditStatus = 'PASSED'
        if (!form.invoiceTime) {
          const now = new Date()
          const pad = (n:number) => n.toString().padStart(2, '0')
          form.invoiceTime = `${now.getFullYear()}-${pad(now.getMonth()+1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
        }
      }
      if (isFinanceUser.value && String(form.invoiceStatus) === 'DONE') {
        form.auditStatus = 'PASSED'
      }
      const ext:any = {}
      try {
        if (typeof form.extendFields === 'string' && form.extendFields) {
          Object.assign(ext, JSON.parse(form.extendFields))
        }
      } catch {}
      if (form.contractAttachment) {
        ext.contractAttachment = { url: form.contractAttachment, name: form.contractAttachmentName || '' }
      }
      if (Object.keys(ext).length) {
        form.extendFields = JSON.stringify(ext)
      }
      // merge extend fields
      const ext2:any = {}
      try {
        if (typeof form.extendFields === 'string' && form.extendFields) {
          Object.assign(ext2, JSON.parse(form.extendFields))
        }
      } catch {}
      if (form.contractAttachment) {
        ext2.contractAttachment = { url: form.contractAttachment, name: form.contractAttachmentName || '' }
      }
      if (form.invoiceType) {
        ext2.invoiceType = form.invoiceType
      }
      if (Object.keys(ext2).length) {
        form.extendFields = JSON.stringify(ext2)
      }
      await updateInvoice(form.id, form)
      ElMessage.success('更新成功')
    } else {
      const ui:any = userStore.userInfo
      form.applicantId = ui?.id
      if (!form.applyTime) {
        const now = new Date()
        const pad = (n:number) => n.toString().padStart(2, '0')
        form.applyTime = `${now.getFullYear()}-${pad(now.getMonth()+1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
      }
      const ext:any = {}
      if (form.contractAttachment) {
        ext.contractAttachment = { url: form.contractAttachment, name: form.contractAttachmentName || '' }
      }
      if (form.invoiceType) {
        ext.invoiceType = form.invoiceType
      }
      if (Object.keys(ext).length) {
        form.extendFields = JSON.stringify(ext)
      }
      await saveInvoice(form)
      ElMessage.success('创建成功')
    }
    router.push('/crm/invoice')
    } catch (e: any) {
      ElMessage.error(e.message || '操作失败')
    }
  }
  
  const fetchCustomers = async () => {
    const res:any = await getCustomerList({ current: 1, size: 1000 })
    customers.value = res.records || []
  }
  const fetchContracts = async () => {
    const res:any = await getContractList({ current: 1, size: 1000 })
    contracts.value = res.records || []
  }
  const fetchUsers = async () => {
    const res:any = await request({ url: '/system/user/list', method: 'get', params: { size: 1000 } })
    users.value = res.records || []
  }
  const fetchInvoiceTypes = async () => {
    try {
      const res:any = await request({ url: '/system/config/list', method: 'get', params: { size: 100 } })
      const list = res.records || res || []
      const general = list.find((item:any) => item.configKey === 'general_settings')
      let invs:string[] = []
      if (general?.configValue) {
        try {
          const cfg = JSON.parse(general.configValue)
          invs = Array.isArray(cfg.invoiceTypes) ? cfg.invoiceTypes : []
          const custom = cfg.customCategories || {}
          if (invs.length === 0) {
            const k = Object.keys(custom).find(x => x.includes('发票分类') || x.includes('发票类型'))
            invs = k ? (custom[k] || []) : []
          }
          // 加载开票状态字典
          invoiceStatusDict.value = custom.开票状态 || ['待开票', '已开票', '作废']
        } catch {
          invoiceStatusDict.value = ['待开票', '已开票', '作废']
        }
      }
      invoiceTypes.value = invs
    } catch {
      invoiceStatusDict.value = ['待开票', '已开票', '作废']
    }
  }
  
  onMounted(async () => {
    if (!userStore.userInfo && userStore.getInfo) {
      await userStore.getInfo()
    }
    await Promise.all([fetchCustomers(), fetchContracts(), fetchUsers(), fetchInvoiceTypes()])
    const id = route.params.id as string | undefined
    if (id) {
      const full:any = await getInvoiceById(Number(id))
      Object.assign(form, full)
      if (!Array.isArray(form.detailList)) form.detailList = []
      // 加载合同信息（用于显示未开票金额）
      if (form.contractId) {
        await loadContractInfo(form.contractId)
      }
      // 根据开票单位自动分派财务经理
      if (form.invoiceUnit && !form.invoicerId) {
        onInvoiceUnitChange(form.invoiceUnit)
      }
      if (isFinanceUser.value && !form.invoicerId) {
        const ui:any = userStore.userInfo
        form.invoicerId = ui?.id
      }
      if (typeof full.extendFields === 'string' && full.extendFields) {
        try {
          const ext = JSON.parse(full.extendFields)
          const ca = ext?.contractAttachment
          if (ca && ca.url) {
            form.contractAttachment = ca.url
            form.contractAttachmentName = ca.name || ''
          }
          if (ext?.invoiceType) {
            form.invoiceType = ext.invoiceType
          }
        } catch {}
      }
    } else {
      const q:any = route.query || {}
      if (q.customerId) {
        form.customerId = Number(q.customerId)
        onCustomerChange(form.customerId)
      }
      if (q.contractId) form.contractId = Number(q.contractId)
      if (form.contractId) {
        // 加载合同信息（用于显示未开票金额）
        await loadContractInfo(form.contractId)
      }
      if (form.contractId && (!form.detailList || form.detailList.length === 0)) {
        try {
          // 加载合同明细
          const c:any = await getContractById(form.contractId)
          let contractDetails = c.detailList || []
          
          // 查询该合同已有的开票记录
          const invoiceRes:any = await request({ 
            url: '/crm/invoice/list', 
            method: 'get', 
            params: { current: 1, size: 1000, contractId: form.contractId } 
          })
          const invoiceList = invoiceRes.records || []
          
          // 收集已开票的商品数量（按名称+规格）
          const invoicedQuantities: Record<string, number> = {}
          for (const inv of invoiceList) {
            // 查询开票详情获取明细
            try {
              const detailRes:any = await request({ 
                url: `/crm/invoice/${inv.id}`, 
                method: 'get' 
              })
              const invoiceDetail = detailRes.data || detailRes
              if (invoiceDetail && invoiceDetail.detailList && invoiceDetail.detailList.length > 0) {
                for (const detail of invoiceDetail.detailList) {
                  const key = `${detail.productName}|${detail.productSpec || ''}`
                  const qty = Number(detail.salesQuantity || 0)
                  invoicedQuantities[key] = (invoicedQuantities[key] || 0) + qty
                }
              }
            } catch {}
          }
          
          // 计算剩余未开票数量
          const remainingDetails: any[] = []
          for (const d of contractDetails) {
            const key = `${d.productName}|${d.productSpec || ''}`
            const contractQty = Number(d.salesQuantity || 0)
            const invoicedQty = invoicedQuantities[key] || 0
            const remainingQty = contractQty - invoicedQty
            
            if (remainingQty > 0) {
              remainingDetails.push({
                productName: d.productName,
                productSpec: d.productSpec,
                salesQuantity: remainingQty,
                salesPrice: d.salesPrice
              })
            }
          }
          
          if (remainingDetails.length > 0) {
            // 有未开票的商品，显示明细
            form.detailList = remainingDetails
            calculateTotal()
          } else {
            // 所有商品都已开票，只设置开票金额
            form.detailList = []
            if (q.remaining != null) {
              const rem = Number(q.remaining)
              if (!Number.isNaN(rem)) form.invoiceAmount = rem
            }
          }
        } catch {}
      }
    }
  })
  </script>
  
  <style scoped>
  .app-container { padding: 20px; }
  .header-actions { display:flex; justify-content: space-between; align-items:center; margin-bottom: 12px; }
  .detail-header { display:flex; justify-content: space-between; align-items:center; margin: 10px 0; font-weight: bold; }
  </style>
