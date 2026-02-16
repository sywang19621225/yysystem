<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑采购退货单' : '新建采购退货单' }}</span>
          <div>
            <el-button @click="goBack">返回</el-button>
            <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
            <el-button v-if="isEdit && outbound.auditStatus === 'PENDING'" type="success" @click="handleSubmitAudit">提交审核</el-button>
          </div>
        </div>
      </template>

      <el-form :model="outbound" label-width="120px" :rules="rules" ref="formRef">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="退货单号">
              <el-input v-model="outbound.outStockNo" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="采购申请" prop="purchaseContractId">
              <el-select
                v-model="outbound.purchaseContractId"
                placeholder="选择采购申请"
                filterable
                clearable
                style="width:100%;"
                :disabled="isEdit"
                @change="handlePurchaseContractChange"
              >
                <el-option
                  v-for="c in purchaseContractList"
                  :key="c.id"
                  :label="c.requestNo + ' - ' + (c.supplierName || '未知供应商')"
                  :value="c.id"
                />
              </el-select>
              <el-text v-if="purchaseContractList.length === 0" type="warning" size="small">
                暂无可用采购申请，<el-link type="primary" @click="loadPurchaseContracts">点击刷新</el-link>
              </el-text>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="供应商">
              <el-input v-model="outbound.supplierName" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="退货日期" prop="outStockDate">
              <el-date-picker
                v-model="outbound.outStockDate"
                type="datetime"
                style="width:100%;"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="退货金额">
              <el-input :model-value="formatCurrency(totalAmount)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="备注">
              <el-input v-model="outbound.remark" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="退货签收时间">
              <el-date-picker
                v-model="outbound.returnSignTime"
                type="datetime"
                style="width:100%;"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择退货签收时间"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="退货回告时间">
              <el-date-picker
                v-model="outbound.returnNotifyTime"
                type="datetime"
                style="width:100%;"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择退货回告时间"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="附件上传">
              <el-upload
                v-model:file-list="attachmentList"
                action="/api/common/upload"
                :headers="uploadHeaders"
                :on-success="handleAttachmentSuccess"
                :on-remove="handleAttachmentRemove"
                :before-upload="beforeAttachmentUpload"
                multiple
                :limit="5"
              >
                <el-button type="primary">点击上传</el-button>
                <template #tip>
                  <div class="el-upload__tip">
                    支持图片格式（jpg/png/gif）和PDF格式，单个文件不超过10MB，最多5个文件
                  </div>
                </template>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- 导入采购退货明细按钮 -->
      <div style="margin: 16px 0;">
        <el-button type="primary" @click="openImportDialog" :disabled="!outbound.purchaseContractId">
          <el-icon><Plus /></el-icon>导入退货商品
        </el-button>
        <el-text v-if="!outbound.purchaseContractId" type="info" style="margin-left: 8px;">
          请先选择采购申请
        </el-text>
      </div>

      <!-- 退货明细表格 -->
      <el-table :data="details" border stripe v-loading="loading" style="width: 100%">
        <el-table-column type="index" width="50" align="center" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="productCode" label="商品编号" width="120" />
        <el-table-column prop="productSpec" label="规格型号" min-width="120" />
        <el-table-column prop="productUnit" label="单位" width="80" align="center" />
        <el-table-column label="退货数量" width="120" align="center">
          <template #default="scope">
            <el-input-number 
              v-model="scope.row.outQuantity" 
              :min="1" 
              :max="getMaxQty(scope.row.productCode)"
              style="width: 100px;"
              @change="(val: number) => handleQuantityChange(scope.row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="可退数量" width="100" align="center">
          <template #default="scope">
            <el-tag type="success">{{ getMaxQty(scope.row.productCode) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="salesPrice" label="单价" width="120" align="right">
          <template #default="scope">{{ formatCurrency(scope.row.salesPrice) }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template #default="scope">{{ formatCurrency(scope.row.amount) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="danger" @click="handleDeleteDetail(scope.row, scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 导入退货商品对话框 -->
    <el-dialog v-model="importDialogVisible" title="导入退货商品" width="900px" destroy-on-close>
      <el-alert type="info" :closable="false" style="margin-bottom: 16px;">
        <template #title>
          <span>提示：只能选择已入库的商品进行退货，退货数量不能超过可退数量</span>
        </template>
      </el-alert>

      <el-table 
        :data="inboundProducts" 
        style="width: 100%" 
        v-loading="inboundLoading" 
        border 
        row-key="productCode"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="productCode" label="商品编号" width="120" />
        <el-table-column prop="productSpec" label="规格型号" min-width="120" />
        <el-table-column prop="productUnit" label="单位" width="80" align="center" />
        <el-table-column prop="purchaseQty" label="采购数量" width="100" align="center" />
        <el-table-column prop="totalInboundQty" label="已入库数量" width="100" align="center" />
        <el-table-column prop="totalReturnedQty" label="已退货数量" width="100" align="center" />
        <el-table-column prop="remainQty" label="可退数量" width="100" align="center">
          <template #default="scope">
            <el-tag type="success">{{ scope.row.remainQty }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="退货数量" width="150" align="center">
          <template #default="scope">
            <el-input-number 
              v-model="scope.row.returnQty" 
              :min="1" 
              :max="scope.row.remainQty"
              style="width: 100px;"
              :disabled="!isSelected(scope.row)"
            />
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmImport" :disabled="selectedProducts.length === 0">
          导入选中商品 ({{ selectedProducts.length }})
        </el-button>
      </template>
    </el-dialog>

    <!-- 提交审核对话框 -->
    <el-dialog v-model="auditDialogVisible" title="提交审核" width="500px">
      <p>确定要提交审核吗？</p>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSubmitAudit" :loading="auditLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getOutboundById, 
  createOutbound, 
  updateOutbound,
  getPurchaseContractInbound
} from '@/api/outbound'
import { getPurchaseRequestList } from '@/api/purchase'

const route = useRoute()
const router = useRouter()

// 判断是编辑还是新建
const id = Number(route.params.id)
const isEdit = !isNaN(id) && id > 0

// 表单引用
const formRef = ref<any>(null)

// 表单数据
const outbound = reactive<any>({
  id: null,
  outStockNo: '',
  outStockType: 'PURCHASE_RETURN',
  outboundCategory: '采购退货',
  purchaseContractId: null,
  supplierId: null,
  supplierName: '',
  outStockDate: new Date().toISOString().slice(0, 19).replace('T', ' '),
  remark: '',
  auditStatus: 'PENDING',
  returnSignTime: null,
  returnNotifyTime: null,
  returnAttachments: null
})

// 明细数据
const details = ref<any[]>([])
const loading = ref(false)
const saving = ref(false)

// 采购申请列表
const purchaseContractList = ref<any[]>([])

// 导入对话框
const importDialogVisible = ref(false)
const inboundProducts = ref<any[]>([])
const inboundLoading = ref(false)
const selectedProducts = ref<any[]>([])

// 审核对话框
const auditDialogVisible = ref(false)
const auditLoading = ref(false)

// 附件上传相关
const attachmentList = ref<any[]>([])
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    Authorization: token ? `Bearer ${token}` : ''
  }
})

// 表单验证规则
const rules = {
  purchaseContractId: [{ required: true, message: '请选择采购申请', trigger: 'change' }],
  outStockDate: [{ required: true, message: '请选择退货日期', trigger: 'change' }]
}

// 计算总金额
const totalAmount = computed(() => {
  const total = details.value.reduce((sum, item) => {
    // 处理 BigDecimal 类型的字符串值
    const amount = item.amount ? parseFloat(String(item.amount)) : 0
    return sum + (isNaN(amount) ? 0 : amount)
  }, 0)
  return total
})

// 格式化货币
const formatCurrency = (value: number | undefined) => {
  if (value === undefined || value === null) return '¥0.00'
  return `¥${Number(value).toFixed(2)}`
}

// 加载数据
// 采购申请明细缓存（用于获取采购数量限制）
// key: productCode, value: { purchaseQty: 采购数量, remainQty: 可退数量 }
const purchaseItemMap = ref<Map<string, { purchaseQty: number, remainQty: number }>>(new Map())

const loadData = async () => {
  if (!isEdit) return

  loading.value = true
  try {
    const res: any = await getOutboundById(id)
    console.log('加载出库单数据:', res)
    Object.assign(outbound, res)
    // 使用返回的detailList作为明细数据
    details.value = res.detailList || []
    console.log('加载明细数据:', details.value)

    // 初始化附件列表
    initAttachmentList()

    // 加载采购申请明细，获取采购数量限制
    if (outbound.purchaseContractId) {
      await loadPurchaseItemLimits(outbound.purchaseContractId)
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 加载采购申请明细的数量限制
const loadPurchaseItemLimits = async (purchaseRequestId: number) => {
  try {
    // 编辑时传入当前单据ID，排除本单的退货数量
    const excludeId = isEdit ? id : undefined
    const res: any = await getPurchaseContractInbound(purchaseRequestId, excludeId)
    if (res && Array.isArray(res)) {
      res.forEach((item: any) => {
        if (item.productCode) {
          // 保存采购数量和可退数量
          purchaseItemMap.value.set(item.productCode, {
            purchaseQty: item.purchaseQty || 0,
            remainQty: item.remainQty !== undefined ? item.remainQty : (item.purchaseQty || 0)
          })
        }
      })
    }
  } catch (error) {
    console.error('加载采购数量限制失败:', error)
  }
}

// 加载采购申请列表
const loadPurchaseContracts = async () => {
  try {
    const res: any = await getPurchaseRequestList({ current: 1, size: 1000 })
    console.log('API响应:', res)
    const records = res.records || []
    console.log('解析后的记录:', records)
    console.log('第一条记录:', records[0])
    if (records[0]) {
      console.log('字段列表:', Object.keys(records[0]))
    }
    purchaseContractList.value = records
  } catch (error) {
    console.error('加载采购申请列表失败:', error)
    ElMessage.error('加载采购申请列表失败')
  }
}

// 采购申请变更
const handlePurchaseContractChange = async (contractId: number) => {
  const contract = purchaseContractList.value.find(c => c.id === contractId)
  if (contract) {
    outbound.supplierId = contract.supplierId
    outbound.supplierName = contract.supplierName
  }
  // 加载采购数量限制（新建时）
  if (contractId && !isEdit) {
    await loadPurchaseItemLimits(contractId)
  }
}

// 打开导入对话框
const openImportDialog = async () => {
  if (!outbound.purchaseContractId) {
    ElMessage.warning('请先选择采购申请')
    return
  }
  
  importDialogVisible.value = true
  inboundLoading.value = true
  
  try {
    const res: any = await getPurchaseContractInbound(outbound.purchaseContractId)
    console.log('导入对话框数据:', res)
    // 为每个商品添加退货数量字段，并确保采购数量有值
    inboundProducts.value = (res || []).map((item: any) => ({
      ...item,
      purchaseQty: item.purchaseQty || item.totalInboundQty || 0,
      returnQty: 1
    }))
  } catch (error) {
    console.error('加载已入库商品失败:', error)
    ElMessage.error('加载已入库商品失败')
  } finally {
    inboundLoading.value = false
  }
}

// 选择变更
const handleSelectionChange = (selection: any[]) => {
  selectedProducts.value = selection
}

// 判断是否选中
const isSelected = (row: any) => {
  return selectedProducts.value.some(p => p.productCode === row.productCode)
}

// 确认导入
const confirmImport = async () => {
  if (selectedProducts.value.length === 0) {
    ElMessage.warning('请选择要导入的商品')
    return
  }
  
  // 验证退货数量
  for (const product of selectedProducts.value) {
    if (!product.returnQty || product.returnQty < 1) {
      ElMessage.warning(`商品[${product.productName}]退货数量不能小于1`)
      return
    }
    if (product.returnQty > product.remainQty) {
      ElMessage.warning(`商品[${product.productName}]退货数量不能超过可退数量[${product.remainQty}]`)
      return
    }
  }
  
  // 添加到明细列表
  for (const product of selectedProducts.value) {
    const existingIndex = details.value.findIndex(d => d.productCode === product.productCode)
    if (existingIndex >= 0) {
      // 更新已有明细
      details.value[existingIndex].outQuantity = product.returnQty
      details.value[existingIndex].amount = product.unitPrice * product.returnQty
    } else {
      // 添加新明细
      const qty = Number(product.returnQty) || 1
      const price = Number(product.unitPrice) || 0
      details.value.push({
        productCode: product.productCode,
        productName: product.productName,
        productSpec: product.productSpec,
        productUnit: product.productUnit,
        outQuantity: qty,
        salesPrice: price,
        amount: price * qty,
        productQrCode: ''
      })
    }
  }
  
  importDialogVisible.value = false
  ElMessage.success('导入成功')
}

// 获取最大可退数量（剩余可退数量）
const getMaxQty = (productCode: string): number => {
  const item = purchaseItemMap.value.get(productCode)
  return item?.remainQty || 999999
}

// 数量变更时重新计算金额
const handleQuantityChange = (row: any, val: number) => {
  const item = purchaseItemMap.value.get(row.productCode)
  const maxQty = item?.remainQty
  // 如果超过可退数量，自动调整为可退数量
  if (maxQty && val > maxQty) {
    val = maxQty
    row.outQuantity = maxQty
    ElMessage.warning(`退货数量不能超过剩余可退数量${maxQty}，已自动调整为最大值`)
  } else {
    row.outQuantity = val
  }
  row.amount = row.salesPrice * val
}

// 删除明细
const handleDeleteDetail = (_row: any, index: number) => {
  ElMessageBox.confirm('确定要删除这条明细吗？', '提示', {
    type: 'warning'
  }).then(() => {
    details.value.splice(index, 1)
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// 保存
const handleSave = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  if (details.value.length === 0) {
    ElMessage.warning('请至少添加一条退货明细')
    return
  }
  
  saving.value = true
  try {
    const data = {
      ...outbound,
      totalSales: totalAmount.value,
      detailList: details.value
    }
    console.log('保存数据:', JSON.parse(JSON.stringify(data)))
    console.log('明细列表:', details.value)
    
    if (isEdit) {
      await updateOutbound(id, data)
    } else {
      const res: any = await createOutbound(data)
      console.log('创建退货单响应:', res)
      // 后端已经在create方法中保存了明细，不需要再调用importPurchaseReturn
      if (res) {
        outbound.id = res
      }
    }
    
    ElMessage.success('保存成功')
    // 保存成功后返回列表页面
    router.push('/scm/outbound/purchase-return')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 提交审核
const handleSubmitAudit = () => {
  auditDialogVisible.value = true
}

// 确认提交审核
const confirmSubmitAudit = async () => {
  auditLoading.value = true
  try {
    // 这里调用提交审核的API
    ElMessage.success('提交审核成功')
    auditDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('提交审核失败')
  } finally {
    auditLoading.value = false
  }
}

// 返回
const goBack = () => {
  router.push('/scm/outbound/purchase-return')
}

// 附件上传成功处理
const handleAttachmentSuccess = (response: any, file: any) => {
  if (response && response.code === 200) {
    const url = response.data
    // 将附件URL添加到列表
    const currentAttachments = outbound.returnAttachments ? JSON.parse(outbound.returnAttachments) : []
    currentAttachments.push({
      name: file.name,
      url: url,
      type: file.raw?.type || ''
    })
    outbound.returnAttachments = JSON.stringify(currentAttachments)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response?.message || '上传失败')
  }
}

// 附件删除处理
const handleAttachmentRemove = (file: any) => {
  const currentAttachments = outbound.returnAttachments ? JSON.parse(outbound.returnAttachments) : []
  const index = currentAttachments.findIndex((item: any) => item.url === file.url || item.url === file.response?.data)
  if (index > -1) {
    currentAttachments.splice(index, 1)
    outbound.returnAttachments = currentAttachments.length > 0 ? JSON.stringify(currentAttachments) : null
  }
}

// 附件上传前校验
const beforeAttachmentUpload = (file: any) => {
  const isImageOrPdf = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif' || file.type === 'application/pdf'
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImageOrPdf) {
    ElMessage.error('只支持图片格式（jpg/png/gif）和PDF格式!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  return true
}

// 初始化附件列表
const initAttachmentList = () => {
  if (outbound.returnAttachments) {
    try {
      const attachments = JSON.parse(outbound.returnAttachments)
      attachmentList.value = attachments.map((item: any, index: number) => ({
        name: item.name || `附件${index + 1}`,
        url: item.url,
        type: item.type || ''
      }))
    } catch (e) {
      console.error('解析附件列表失败:', e)
      attachmentList.value = []
    }
  } else {
    attachmentList.value = []
  }
}

onMounted(() => {
  loadData()
  loadPurchaseContracts()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
/* 确保下拉选择框有白色背景 */
:deep(.el-select) {
  background-color: #fff;
}
:deep(.el-select .el-input__wrapper) {
  background-color: #fff;
}
</style>
