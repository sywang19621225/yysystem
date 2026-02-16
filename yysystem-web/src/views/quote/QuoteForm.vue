<template>
  <el-dialog v-model="visible" :title="id ? '编辑报价单' : '新增报价单'" width="800px" :before-close="onDialogBeforeClose" @close="handleClose">
    <el-form :model="form" label-width="100px">
      <div style="display:flex;gap:16px;justify-content:flex-end;margin-bottom:8px;">
        <el-tag type="info">成本合计：{{ formatCurrency(costTotalAmount) }}</el-tag>
        <el-tag type="primary">销售合计：{{ formatCurrency(totalAmount) }}</el-tag>
        <el-tag type="danger">预计毛利：{{ formatCurrency(Number(totalAmount) - Number(costTotalAmount)) }}</el-tag>
      </div>
      <el-row>
        <el-col :span="12">
          <el-form-item label="客户" required>
            <el-select v-model="form.customerId" placeholder="请选择客户" filterable @change="onCustomerChange">
              <el-option
                v-for="item in customerList"
                :key="item.id"
                :label="item.customerName"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="报价日期" required>
            <el-date-picker v-model="form.quoteDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="交付日期">
            <el-date-picker v-model="form.deliveryDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="报价状态">
            <el-select v-model="form.quoteStatus" placeholder="请选择">
              <el-option label="待确认" value="WAITING" />
              <el-option label="已确认" value="CONFIRMED" />
              <el-option label="已驳回" value="REJECTED" />
              <el-option label="草稿" value="DRAFT" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="审核状态">
            <el-select v-model="form.auditStatus" placeholder="请选择">
              <el-option label="待审" value="PENDING" />
              <el-option label="通过" value="PASSED" />
              <el-option label="驳回" value="REJECTED" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="审核详情">
            <el-input v-model="form.auditDetail" type="textarea" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系人">
            <el-select v-model="form.linkmanId" placeholder="选择联系人" filterable @change="onLinkmanChange">
              <el-option
                v-for="lk in linkmanList"
                :key="lk.id"
                :label="lk.name"
                :value="lk.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话">
            <el-input v-model="form.phone" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="地址">
            <el-input v-model="form.address" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="业务员">
            <el-input v-model="salesmanName" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="附件">
            <el-upload
              action="/api/common/upload"
              :headers="uploadHeaders"
              :file-list="attachFileList"
              :on-success="handleAttachSuccess"
              :on-remove="handleAttachRemove"
              :before-upload="beforeAttachUpload"
              :limit="10"
              multiple
            >
              <el-button type="primary" link>上传附件</el-button>
              <template #tip>
                <div class="el-upload__tip">最大 100MB，保存原文件名</div>
              </template>
            </el-upload>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" />
          </el-form-item>
        </el-col>
      </el-row>

      <div class="detail-header">
        <span>商品明细</span>
        <div style="display:flex;align-items:center;gap:12px;">
          <el-radio-group v-model="priceMode" @change="applyPriceMode">
            <el-radio label="CHANNEL">渠道单价</el-radio>
            <el-radio label="TERMINAL">终端单价</el-radio>
          </el-radio-group>
          <el-button type="primary" link @click="addDetail">添加商品</el-button>
        </div>
      </div>
      
      <el-table :data="form.detailList" border style="margin-top: 10px" max-height="500">
        <el-table-column label="选择商品" width="220">
          <template #default="scope">
            <el-select
              v-model="scope.row.productId"
              placeholder="请输入商品名称搜索"
              filterable
              remote
              :remote-method="searchProducts"
              :loading="productLoading"
              @change="(val: number) => handleProductChange(val, scope.row)"
              clearable
            >
               <el-option
                v-for="item in filteredProductList"
                :key="item.id"
                :label="item.productName + (item.productCode ? ' (' + item.productCode + ')' : '')"
                :value="item.id"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名称" min-width="180" />
        <el-table-column prop="salesQuantity" label="数量" width="140">
          <template #default="scope">
            <el-input-number v-model="scope.row.salesQuantity" :min="1" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="salesPrice" label="单价" width="160">
          <template #default="scope">
            <el-input-number v-model="scope.row.salesPrice" :precision="2" :step="0.1" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="productSpec" label="商品规格" min-width="160" />
        <el-table-column prop="productCode" label="商品编号" width="160" />
        <el-table-column prop="costPrice" label="成本单价" width="160">
          <template #default="scope">
            <el-input-number v-model="scope.row.costPrice" :precision="2" :step="0.1" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="customRemark" label="定制备注" min-width="180">
          <template #default="scope">
            <el-input v-model="scope.row.customRemark" placeholder="填写定制说明" />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template #default="scope">
            {{ (scope.row.salesPrice * scope.row.salesQuantity).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="openEditDetailDialog(scope.$index)">修改</el-button>
            <el-button type="danger" link @click="removeDetail(scope.$index)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="total-amount-container">
        总金额: <span class="amount-text">¥{{ totalAmount }}</span>
      </div>

    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </span>
    </template>
  </el-dialog>

  <!-- 编辑明细弹窗 -->
  <el-dialog v-model="editDetailVisible" title="编辑明细" width="700px">
    <el-form :model="editDetailForm" label-width="100px">
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="选择商品">
            <el-select
              v-model="editDetailForm.productId"
              placeholder="请输入商品名称搜索"
              filterable
              remote
              :remote-method="searchProducts"
              :loading="productLoading"
              @change="(val: number) => onEditProductChange(val)"
              style="width: 100%"
              clearable
            >
              <el-option
                v-for="item in filteredProductList"
                :key="item.id"
                :label="item.productName + (item.productCode ? ' (' + item.productCode + ')' : '')"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="商品名称">
            <el-input v-model="editDetailForm.productName" placeholder="请输入商品名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="商品编号">
            <el-input v-model="editDetailForm.productCode" placeholder="请输入商品编号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="数量">
            <el-input-number v-model="editDetailForm.salesQuantity" :min="1" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="单价">
            <el-input-number v-model="editDetailForm.salesPrice" :precision="2" :step="0.1" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="成本单价">
            <el-input-number v-model="editDetailForm.costPrice" :precision="2" :step="0.1" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="小计">
            <el-input :model-value="(editDetailForm.salesPrice * editDetailForm.salesQuantity).toFixed(2)" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="商品规格">
            <el-input v-model="editDetailForm.productSpec" placeholder="商品规格" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="定制备注">
            <el-input v-model="editDetailForm.customRemark" placeholder="填写定制说明" type="textarea" :rows="2" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="editDetailVisible = false">取消</el-button>
      <el-button type="primary" @click="saveEditDetail">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue'
import { getQuoteById, saveQuote, updateQuote } from '@/api/quote'
import { getProductList } from '@/api/product'
import { getCustomerList } from '@/api/customer'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import type { UploadProps, UploadUserFile } from 'element-plus'

const props = defineProps<{
  modelValue: boolean
  id?: number
}>()

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const form = reactive<any>({
  id: undefined,
  customerId: undefined,
  quoteDate: new Date().toISOString().split('T')[0],
  deliveryDate: '',
  remark: '',
  detailList: [],
  costTotalAmount: 0,
  channelTotalAmount: 0,
  terminalTotalAmount: 0,
  auditStatus: 'PENDING',
  auditDetail: '',
  quoteStatus: 'WAITING',
  linkmanName: '',
  phone: '',
  address: '',
  attachment: ''
})

const customerList = ref<any[]>([])
const productList = ref<any[]>([])
const filteredProductList = ref<any[]>([])
const linkmanList = ref<any[]>([])
const priceMode = ref<'CHANNEL' | 'TERMINAL'>('TERMINAL')
const attachFileList = ref<UploadUserFile[]>([])
const salesmanName = ref('')
const userStore = useUserStore()
const productLoading = ref(false)

// 上传请求头（包含认证token）
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    Authorization: token ? `Bearer ${token}` : ''
  }
})

// 编辑明细弹窗
const editDetailVisible = ref(false)
const editDetailIndex = ref(-1)
const editDetailForm = reactive({
  id: undefined,
  productId: undefined,
  productName: '',
  productCode: '',
  productSpec: '',
  productUnit: '',
  salesQuantity: 1,
  salesPrice: 0,
  costPrice: 0,
  customRemark: ''
})
const originalSnapshot = ref<any>({})
const serializeSnapshot = () => {
  return JSON.stringify({
    form: form,
    files: attachFileList.value.map(f => ({ name: f.name, url: (f.response as any)?.data || f.url }))
  })
}
const isChanged = () => {
  const a = serializeSnapshot()
  const b = JSON.stringify(originalSnapshot.value || {})
  return a !== b
}

const totalAmount = computed(() => {
  if (!form.detailList) return '0.00'
  const total = form.detailList.reduce((sum: number, item: any) => {
    return sum + (item.salesPrice * item.salesQuantity)
  }, 0)
  return total.toFixed(2)
})
const costTotalAmount = computed(() => {
  if (!form.detailList) return '0.00'
  const total = form.detailList.reduce((sum: number, item: any) => {
    return sum + ((item.costPrice || 0) * item.salesQuantity)
  }, 0)
  return total.toFixed(2)
})
const channelTotalAmount = computed(() => {
  if (!form.detailList) return '0.00'
  const total = form.detailList.reduce((sum: number, item: any) => {
    const p = productList.value.find((x:any) => x.id === item.productId)
    const price = p?.channelPrice ?? p?.costPrice ?? 0
    return sum + (Number(price) * item.salesQuantity)
  }, 0)
  return total.toFixed(2)
})
const terminalTotalAmount = computed(() => {
  if (!form.detailList) return '0.00'
  const total = form.detailList.reduce((sum: number, item: any) => {
    const p = productList.value.find((x:any) => x.id === item.productId)
    const price = p?.terminalPrice ?? p?.costPrice ?? 0
    return sum + (Number(price) * item.salesQuantity)
  }, 0)
  return total.toFixed(2)
})

const formatCurrency = (value: number | string | undefined | null) => {
  const n = Number(value ?? 0)
  return `¥${n.toFixed(2)}`
}

const fetchCustomers = async () => {
  const res: any = await getCustomerList({ current: 1, size: 1000 })
  customerList.value = res.records
}

const searchProducts = async (query: string) => {
  if (!query || query.trim() === '') {
    filteredProductList.value = productList.value
    return
  }
  
  productLoading.value = true
  try {
    // 先按商品名称搜索
    const res: any = await getProductList({ 
      current: 1, 
      size: 50, // 限制搜索结果数量
      productName: query.trim()
    })
    const list = res.records || []
    
    // 如果按名称搜索没有结果，尝试按商品编号搜索
    if (list.length === 0) {
      const codeRes: any = await getProductList({ 
        current: 1, 
        size: 50,
        productCode: query.trim()
      })
      const codeList = codeRes.records || []
      filteredProductList.value = codeList
    } else {
      filteredProductList.value = list
    }
  } catch (error) {
    console.error('搜索商品失败:', error)
    // 如果API搜索失败，使用本地过滤
    const localFiltered = productList.value.filter((item: any) => {
      const nameMatch = item.productName && item.productName.toLowerCase().includes(query.toLowerCase())
      const codeMatch = item.productCode && item.productCode.toLowerCase().includes(query.toLowerCase())
      return nameMatch || codeMatch
    })
    filteredProductList.value = localFiltered
  } finally {
    productLoading.value = false
  }
}

const handleProductChange = (productId: number, row: any) => {
  // 先从搜索列表中查找，再从完整列表中查找
  let product = filteredProductList.value.find(p => p.id === productId)
  if (!product) {
    product = productList.value.find(p => p.id === productId)
  }
  if (product) {
    row.productName = product.productName
    row.productCode = product.productCode
    row.costPrice = Number(product.costPrice || 0)
    row.salesPrice = Number(priceMode.value === 'CHANNEL' ? (product.channelPrice || product.costPrice || 0) : (product.terminalPrice || product.costPrice || 0))
    row.productSpec = product.productSpec
    row.productUnit = product.productUnit
  }
}

const addDetail = () => {
  form.detailList.push({
    costPrice: 0,
    salesPrice: 0,
    salesQuantity: 1
  })
}

const removeDetail = (index: number) => {
  form.detailList.splice(index, 1)
}

// 打开编辑明细弹窗
const openEditDetailDialog = (index: number) => {
  const row = form.detailList[index]
  if (!row) return

  editDetailIndex.value = index
  Object.assign(editDetailForm, {
    id: row.id,
    productId: row.productId,
    productName: row.productName,
    productCode: row.productCode,
    productSpec: row.productSpec,
    productUnit: row.productUnit,
    salesQuantity: row.salesQuantity || 1,
    salesPrice: row.salesPrice || 0,
    costPrice: row.costPrice || 0,
    customRemark: row.customRemark || ''
  })
  editDetailVisible.value = true
}

// 编辑弹窗中商品选择变化
const onEditProductChange = (productId: number) => {
  let product = filteredProductList.value.find(p => p.id === productId)
  if (!product) {
    product = productList.value.find(p => p.id === productId)
  }
  if (product) {
    editDetailForm.productName = product.productName
    editDetailForm.productCode = product.productCode
    editDetailForm.productSpec = product.productSpec
    editDetailForm.productUnit = product.productUnit
    editDetailForm.costPrice = Number(product.costPrice || 0)
    editDetailForm.salesPrice = Number(priceMode.value === 'CHANNEL' ? (product.channelPrice || product.costPrice || 0) : (product.terminalPrice || product.costPrice || 0))
  }
}

// 保存编辑的明细
const saveEditDetail = () => {
  if (editDetailIndex.value < 0 || editDetailIndex.value >= form.detailList.length) {
    ElMessage.error('保存失败：明细索引错误')
    return
  }

  const row = form.detailList[editDetailIndex.value]
  Object.assign(row, {
    id: editDetailForm.id,
    productId: editDetailForm.productId,
    productName: editDetailForm.productName,
    productCode: editDetailForm.productCode,
    productSpec: editDetailForm.productSpec,
    productUnit: editDetailForm.productUnit,
    salesQuantity: editDetailForm.salesQuantity,
    salesPrice: editDetailForm.salesPrice,
    costPrice: editDetailForm.costPrice,
    customRemark: editDetailForm.customRemark
  })

  editDetailVisible.value = false
  ElMessage.success('修改成功')
}

const handleSubmit = async () => {
  if (!form.customerId) {
    ElMessage.warning('请选择客户')
    return
  }
  if (!form.quoteDate) {
    ElMessage.warning('请选择报价日期')
    return
  }
  if (!form.detailList || form.detailList.length === 0) {
    ElMessage.warning('请添加商品明细')
    return
  }
  for (const row of form.detailList) {
    if (!row.productName || !row.productCode) {
      const p = productList.value.find((x:any) => x.id === row.productId)
      if (p) {
        row.productName = p.productName
        row.productCode = p.productCode
        row.productSpec = p.productSpec
        row.productUnit = p.productUnit
        row.costPrice = Number(p.costPrice || 0)
      }
    }
    if (!row.productName || !row.productCode) {
      ElMessage.warning('每条明细需选择商品，填写商品名称与编号')
      return
    }
  }

  // Calculate quoteAmount
  form.quoteAmount = Number(totalAmount.value)
  form.productTotal = form.detailList.length
  form.costTotalAmount = Number(costTotalAmount.value)
  form.channelTotalAmount = Number(channelTotalAmount.value)
  form.terminalTotalAmount = Number(terminalTotalAmount.value)

  const payload:any = {
    id: form.id,
    customerId: form.customerId,
    linkmanId: form.linkmanId,
    phone: form.phone,
    address: form.address,
    quoteDate: form.quoteDate,
    deliveryDate: form.deliveryDate,
    remark: form.remark,
    auditStatus: form.auditStatus,
    auditDetail: form.auditDetail,
    quoteStatus: form.quoteStatus,
    quoteAmount: form.quoteAmount,
    productTotal: form.productTotal,
    costTotalAmount: form.costTotalAmount,
    channelTotalAmount: form.channelTotalAmount,
    terminalTotalAmount: form.terminalTotalAmount,
    attachment: form.attachment,
    detailList: form.detailList.map((row:any) => ({
      productName: row.productName,
      productCode: row.productCode,
      productSpec: row.productSpec,
      productCategory: row.productCategory,
      productImage: row.productImage,
      salesPrice: row.salesPrice,
      salesQuantity: row.salesQuantity,
      productRemark: row.productRemark,
      customRemark: row.customRemark
    }))
  }
  try {
    if (form.id) {
      await updateQuote(form.id, payload)
      ElMessage.success('更新成功')
    } else {
      await saveQuote(payload)
      ElMessage.success('创建成功')
    }
    visible.value = false
    emit('success')
  } catch (e) {
    // error handled by request.ts
  }
}

const handleClose = () => {
  form.id = undefined
  form.customerId = undefined
  form.quoteDate = new Date().toISOString().split('T')[0]
  form.detailList = []
  form.auditStatus = 'PENDING'
  form.auditDetail = ''
  form.quoteStatus = 'WAITING'
  form.linkmanName = ''
  form.phone = ''
  form.address = ''
  form.attachment = ''
  attachFileList.value = []
}

watch(() => props.id, async (val) => {
  if (val) {
    const res: any = await getQuoteById(val)
    Object.assign(form, res)
    // 如果有商品明细，按需加载相关商品信息
    if (form.detailList && form.detailList.length > 0) {
      // 收集需要匹配的商品名称或编号
      const productNames = form.detailList.map((row: any) => row.productName).filter(Boolean)
      const productCodes = form.detailList.map((row: any) => row.productCode).filter(Boolean)
      
      if (productNames.length > 0 || productCodes.length > 0) {
        // 搜索相关商品
        for (const name of productNames) {
          await searchProducts(name)
          if (filteredProductList.value.length > 0) break
        }
        
        // 匹配商品ID
        (form.detailList || []).forEach((row:any) => {
          if (!row.productId) {
            const found = filteredProductList.value.find((p:any) => 
              p.productCode === row.productCode || p.productName === row.productName
            )
            if (found) {
              row.productId = found.id
            }
          }
        })
      }
    }
    onCustomerChange()
    originalSnapshot.value = JSON.parse(serializeSnapshot())
  } else {
    handleClose()
  }
})

const loadQuoteIfVisible = async () => {
  if (visible.value && props.id) {
    const res: any = await getQuoteById(props.id)
    Object.assign(form, res)
    // 如果有商品明细，按需加载相关商品信息
    if (form.detailList && form.detailList.length > 0) {
      // 收集需要匹配的商品名称或编号
      const productNames = form.detailList.map((row: any) => row.productName).filter(Boolean)
      const productCodes = form.detailList.map((row: any) => row.productCode).filter(Boolean)
      
      if (productNames.length > 0 || productCodes.length > 0) {
        // 搜索相关商品
        for (const name of productNames) {
          await searchProducts(name)
          if (filteredProductList.value.length > 0) break
        }
        
        // 匹配商品ID
        (form.detailList || []).forEach((row:any) => {
          if (!row.productId) {
            const found = filteredProductList.value.find((p:any) => 
              p.productCode === row.productCode || p.productName === row.productName
            )
            if (found) {
              row.productId = found.id
            }
          }
        })
      }
    }
    onCustomerChange()
    originalSnapshot.value = JSON.parse(serializeSnapshot())
  }
}

watch(() => visible.value, async (v) => {
  if (v) {
    await loadQuoteIfVisible()
  }
})

onMounted(() => {
  fetchCustomers()
  // 不再预加载所有商品，改为按需搜索加载
  // fetchProducts()
  userStore.getInfo().then(() => {
    const ui:any = userStore.userInfo || {}
    salesmanName.value = ui.name || ui.realName || ui.nickname || ui.username || ''
  })
  window.addEventListener('beforeunload', beforeUnloadHandler)
})
onUnmounted(() => {
  window.removeEventListener('beforeunload', beforeUnloadHandler)
})
const beforeUnloadHandler = (e: BeforeUnloadEvent) => {
  if (visible.value && isChanged()) {
    e.preventDefault()
    e.returnValue = ''
  }
}

const applyPriceMode = () => {
  form.detailList = (form.detailList || []).map((row: any) => {
    const product = productList.value.find(p => p.id === row.productId)
    if (product) {
      if (row.salesPrice === undefined || row.salesPrice === null) {
        row.salesPrice = Number(priceMode.value === 'CHANNEL' ? (product.channelPrice || product.costPrice || 0) : (product.terminalPrice || product.costPrice || 0))
      }
      row.costPrice = Number(product.costPrice || 0)
    }
    return row
  })
}

const onCustomerChange = () => {
  const c = customerList.value.find((x:any) => x.id === form.customerId)
  if (c) {
    form.linkmanId = undefined
    form.phone = c.phone || ''
    form.address = c.address || ''
    const isAgent = (c.customerType === 'AGENT') || (c.customerCategory === 'AGENT') || (String(c.customerTag || '').includes('代理'))
    priceMode.value = isAgent ? 'CHANNEL' : 'TERMINAL'
    request({ url: '/crm/contact/list', method: 'get', params: { customerId: form.customerId } }).then((res:any) => {
      const list = Array.isArray(res) ? res : (res?.records || res?.data || [])
      linkmanList.value = list
      const primary = list.find((x:any) => Number(x.isPrimary) === 1)
      const pick = primary || list[0]
      form.linkmanId = pick?.id || undefined
      form.phone = pick?.phone || c.phone || form.phone
    })
  }
}
const onLinkmanChange = (id: number) => {
  const lk = linkmanList.value.find((x:any) => x.id === id)
  if (lk) {
    form.phone = lk.phone || form.phone
    form.address = form.address
  }
}

const handleAttachSuccess: UploadProps['onSuccess'] = (res, _uploadFile, uploadFiles) => {
  if ((res as any)?.code === 200) {
    attachFileList.value = uploadFiles
    const pairs = uploadFiles.map(f => `${f.name}@${(f.response as any)?.data || f.url}`).filter(Boolean)
    form.attachment = pairs.join(',')
    ElMessage.success('上传成功')
  } else {
    ElMessage.error((res as any)?.message || '上传失败')
  }
}
const handleAttachRemove: UploadProps['onRemove'] = (_uploadFile, uploadFiles) => {
  attachFileList.value = uploadFiles
  const pairs = uploadFiles.map(f => `${f.name}@${(f.response as any)?.data || f.url}`).filter(Boolean)
  form.attachment = pairs.join(',')
}
const beforeAttachUpload: UploadProps['beforeUpload'] = (rawFile) => {
  if (rawFile.size / 1024 / 1024 > 100) {
    ElMessage.error('文件大小不能超过 100MB!')
    return false
  }
  return true
}
const onDialogBeforeClose = async (done: () => void) => {
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
</script>

<style scoped>
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-weight: bold;
}
.total-amount-container {
  margin-top: 20px;
  text-align: right;
  font-size: 16px;
}
.amount-text {
  color: #f56c6c;
  font-weight: bold;
  font-size: 20px;
  margin-left: 10px;
}
</style>
