<template>
  <el-dialog v-model="visible" :title="id ? '编辑合同' : '新增合同'" width="1200px" :before-close="handleBeforeClose">
    <el-form :model="form" label-width="100px">
      <div style="display:flex;gap:16px;justify-content:flex-end;margin-bottom:8px;">
        <el-tag type="warning">成本合计：{{ formatCurrency(costTotalAmount) }}</el-tag>
        <el-tag type="primary">合同合计：{{ formatCurrency(totalAmount) }}</el-tag>
        <el-tag type="danger">预计毛利：{{ formatCurrency(Number(totalAmount) - Number(costTotalAmount) - Number(form.intermediaryFee || 0)) }}</el-tag>
        <el-tag v-if="isLocked" type="danger">金额已锁定（审核通过）</el-tag>
      </div>
      <el-row>
        <el-col :span="12">
          <el-form-item label="合同名称" required>
            <el-input v-model="form.contractName" placeholder="必须填写客户名称+合同名称" />
            <div style="color: #f56c6c; font-size: 12px; margin-top: 4px;">合同名称由校名+校区+项目名称</div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="合同编号">
            <el-input v-model="form.contractNo" placeholder="留空自动生成" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="客户" required>
            <el-select v-model="form.customerId" placeholder="请选择客户" filterable @change="onCustomerChange">
              <el-option v-for="item in customerList" :key="item.id" :label="item.customerName" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系人">
            <div style="display:flex;gap:8px;align-items:center;">
              <el-input v-model="form.linkmanName" style="flex:1" />
              <el-button size="small" @click="refreshContactInfo" title="刷新联系人信息">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
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
          <el-form-item label="居间人">
            <el-select v-model="form.intermediaryCustomerId" placeholder="选择居间人" filterable clearable>
              <el-option v-for="item in customerList" :key="item.id" :label="item.customerName" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="报价日期">
            <el-date-picker v-model="form.quoteDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="交货日期">
            <el-date-picker v-model="form.deliveryDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="合同签订日期">
            <el-date-picker v-model="form.signingDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="审核状态">
            <el-select v-model="form.auditStatus" placeholder="请选择">
              <el-option label="待审核" value="PENDING" />
              <el-option label="审核中" value="UNDER_REVIEW" />
              <el-option label="已通过" value="PASSED" />
              <el-option label="已驳回" value="REJECTED" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="质保（月）">
            <el-input-number v-model="form.warrantyPeriodMonths" :min="0" :step="1" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="预付款">
            <el-tag type="warning">{{ formatCurrency(form.advancePayment) }}</el-tag>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="中介客户">
            <el-select v-model="form.intermediaryCustomerId" placeholder="选择中介客户" filterable clearable>
              <el-option v-for="item in customerList" :key="item.id" :label="item.customerName" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="居间费">
            <el-input-number v-model="form.intermediaryFee" :precision="2" :step="100" style="width:100%" :disabled="isLocked" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="居间费支付">
            <el-select v-model="form.intermediaryFeeStatus" placeholder="请选择" :disabled="isLocked">
              <el-option label="未支付" value="UNPAID" />
              <el-option label="已支付" value="PAID" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="审核详情">
            <el-input v-model="form.auditDetail" type="textarea" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="终端客户" :required="isAgentCustomer">
            <el-select v-model="endCustomerId" placeholder="选择终端客户" filterable @change="onEndCustomerChange">
              <el-option v-for="item in customerList" :key="item.id" :label="item.customerName" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="收货人">
            <div style="display:flex;gap:8px;align-items:center;">
              <el-input v-model="form.receiver" style="flex:1" />
              <el-button size="small" @click="useCustomerDefaultContact">默认联系人</el-button>
              <el-button size="small" type="primary" @click="openContactDialog">新增联系人</el-button>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系方式">
            <el-input v-model="form.receiverPhone" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="收货地址">
            <el-input v-model="form.receiveAddress" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="业务员">
            <el-input v-model="salesmanName" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="合同附件">
            <el-upload
              action="/api/common/upload"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="onAttachmentSuccess"
              :before-upload="onAttachmentBeforeUpload"
              multiple
              accept="image/*,.pdf,.doc,.docx"
            >
              <el-button>上传附件</el-button>
            </el-upload>
            <div v-if="attachments.length" style="margin-top:8px; display:flex; flex-direction:column; gap:6px;">
              <div v-for="(att, idx) in attachments" :key="idx" style="display:flex; align-items:flex-start; gap:10px;">
                <a class="attachment-link" :href="att.url" :download="att.name" target="_blank">{{ att.name }}</a>
                <el-button size="small" type="danger" link @click="removeAttachment(idx)">移除</el-button>
              </div>
            </div>
          </el-form-item>
        </el-col>
      </el-row>

      <div class="detail-header">
        <span>商品明细（每条需选择商品并填写名称与编号）</span>
        <div style="display:flex;align-items:center;gap:12px;">
          <el-button type="primary" link @click="addDetail" :disabled="isLocked">添加明细</el-button>
          <el-button type="warning" link @click="openQuoteSelect" :disabled="isLocked">调用报价单</el-button>
        </div>
      </div>

      <el-table :data="paginatedDetailList" border style="margin-top: 10px" max-height="500">
        <el-table-column label="选择商品" width="220">
          <template #default="scope">
          <el-select v-model="scope.row.productId" placeholder="选择商品" filterable @change="(val: number) => handleProductChange(val, scope.row)" :disabled="isLocked">
            <el-option v-for="item in productList" :key="item.id" :label="item.productName" :value="item.id" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="productName" label="商品名称" min-width="180">
        <template #default="scope">
            <el-input v-model="scope.row.productName" placeholder="请输入商品名称" :disabled="isLocked" />
        </template>
      </el-table-column>
      <el-table-column prop="salesQuantity" label="数量" width="120">
        <template #default="scope">
            <el-input-number v-model="scope.row.salesQuantity" :min="1" size="small" :disabled="isLocked" />
        </template>
      </el-table-column>
      <el-table-column prop="salesPrice" label="单价" width="140">
        <template #default="scope">
            <el-input-number v-model="scope.row.salesPrice" :precision="2" :step="0.1" size="small" :disabled="isLocked" />
        </template>
      </el-table-column>
      <el-table-column label="小计" width="120">
        <template #default="scope">
            {{ (scope.row.salesPrice * scope.row.salesQuantity).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="customRemark" label="定制备注" min-width="180">
        <template #default="scope">
            <el-input v-model="scope.row.customRemark" placeholder="填写定制说明" :disabled="isLocked" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="scope">
            <el-button type="primary" link @click="openEditDetailDialog(scope.$index)" :disabled="isLocked">修改</el-button>
            <el-button type="danger" link @click="removeDetailByIndex(scope.$index)" :disabled="isLocked">移除</el-button>
        </template>
      </el-table-column>
      </el-table>

      <div class="detail-pagination">
        <el-pagination
          v-model:current-page="detailCurrentPage"
          v-model:page-size="detailPageSize"
          :page-sizes="[20, 50, 100]"
          :total="form.detailList?.length || 0"
          layout="total, sizes, prev, pager, next"
          @size-change="onDetailSizeChange"
          @current-change="onDetailCurrentChange"
        />
      </div>

      <div class="total-amount-container">
        合同金额: <span class="amount-text">¥{{ totalAmount }}</span>
      </div>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :disabled="isLocked || !detailsValid || saving" :loading="saving" @click="handleSubmit">确定</el-button>
      </span>
    </template>
    <ContactDialog v-model="contactDialogVisible" :customer-id="form.customerId || 0" />
    <el-dialog v-model="quoteSelectVisible" title="选择报价单" width="800px">
      <div style="margin-bottom:10px;display:flex;gap:12px;align-items:center;">
        <span>当前客户：{{ currentCustomerName }}</span>
        <el-button type="primary" @click="fetchQuoteList">刷新</el-button>
      </div>
      <el-table :data="quoteList" size="small" border>
        <el-table-column prop="quoteNo" label="报价单号" width="160" />
        <el-table-column prop="quoteDate" label="报价日期" width="140" />
        <el-table-column prop="customerName" label="客户名称" width="180" />
        <el-table-column prop="quoteAmount" label="总金额" width="120" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="primary" link @click="importFromQuote(scope.row.id)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container" style="margin-top:10px;">
        <el-pagination
          v-model:current-page="quotePage"
          v-model:page-size="quotePageSize"
          :total="quoteTotal"
          :page-sizes="[10,20,50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchQuoteList"
          @current-change="fetchQuoteList"
        />
      </div>
      <template #footer>
        <el-button @click="quoteSelectVisible=false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 编辑明细弹窗 -->
    <el-dialog v-model="editDetailVisible" title="编辑明细" width="700px">
      <el-form :model="editDetailForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="选择商品">
              <el-select v-model="editDetailForm.productId" placeholder="选择商品" filterable @change="(val: number) => onEditProductChange(val)" style="width: 100%">
                <el-option v-for="item in productList" :key="item.id" :label="item.productName" :value="item.id" />
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
          <el-col :span="24">
            <el-form-item label="参数">
              <el-input v-model="editDetailForm.productRemark" placeholder="投标参数/技术参数" type="textarea" :rows="3" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editDetailVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEditDetail">确定</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue'
import { getContractById, saveContract, updateContract } from '@/api/contract'
import { getProductList } from '@/api/product'
import { getCustomerList } from '@/api/customer'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import type { UploadProps, UploadFile } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getQuoteList, getQuoteById } from '@/api/quote'
import ContactDialog from '@/views/crm/customer/components/ContactDialog.vue'
import request from '@/utils/request'

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
  customerName: '',
  quoteDate: new Date().toISOString().split('T')[0],
  deliveryDate: '',
  signingDate: '',
  warrantyPeriodMonths: undefined,
  remark: '',
  contractName: '',
  contractNo: '',
  attachment: '',
  attachments: [],
  detailList: [],
  auditStatus: 'PENDING',
  auditDetail: '',
  linkmanId: undefined,
  linkmanName: '',
  phone: '',
  address: '',
  receiver: '',
  receiverPhone: '',
  receiveAddress: '',
  advancePayment: 0
  ,intermediaryFee: 0
  ,intermediaryCustomerId: undefined
  ,intermediaryCustomerName: ''
  ,intermediaryFeeStatus: 'UNPAID'
})

const customerList = ref<any[]>([])
const productList = ref<any[]>([])
const salesmanName = ref('')
const endCustomerId = ref<number | undefined>(undefined)
const userStore = useUserStore()
const quoteSelectVisible = ref(false)
const contactDialogVisible = ref(false)
const quoteList = ref<any[]>([])
const quotePage = ref(1)
const quotePageSize = ref(10)
const quoteTotal = ref(0)
const saving = ref(false)

// 上传请求头（包含认证token）
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    Authorization: token ? `Bearer ${token}` : ''
  }
})

// 明细分页
const detailCurrentPage = ref(1)
const detailPageSize = ref(20)

// 分页后的明细列表
const paginatedDetailList = computed(() => {
  if (!form.detailList || form.detailList.length === 0) return []
  const start = (detailCurrentPage.value - 1) * detailPageSize.value
  const end = start + detailPageSize.value
  return form.detailList.slice(start, end)
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
  customRemark: '',
  productRemark: ''
})
const currentCustomerName = computed(() => {
  const c = customerList.value.find((x:any) => x.id === form.customerId)
  // 尝试不同的字段名获取客户名称
  const name = c?.customerName || c?.name || c?.companyName || '未选择'
  console.log('当前客户名称计算:', {
    customerId: form.customerId,
    foundCustomer: c,
    usedName: name,
    allFields: {
      customerName: c?.customerName,
      name: c?.name,
      companyName: c?.companyName
    }
  })
  return name
})

const totalAmount = computed(() => {
  if (!form.detailList) return '0.00'
  const total = form.detailList.reduce((sum: number, item: any) => {
    return sum + (Number(item.salesPrice || 0) * Number(item.salesQuantity || 0))
  }, 0)
  return total.toFixed(2)
})
const costTotalAmount = computed(() => {
  if (!form.detailList) return '0.00'
  const total = form.detailList.reduce((sum: number, item: any) => {
    return sum + (Number(item.costPrice || 0) * Number(item.salesQuantity || 0))
  }, 0)
  return total.toFixed(2)
})
const detailsValid = computed(() => {
  if (!form.detailList || form.detailList.length === 0) return false
  return form.detailList.every((row:any) => !!row.productName && !!row.productCode)
})
const initialAuditStatus = ref<string>('PENDING')
const isLocked = computed(() => String(initialAuditStatus.value) === 'PASSED')

// 表单原始数据，用于检测是否有修改
const initialFormData = ref<string>('')
const getFormSnapshot = () => {
  // 获取表单快照，包含所有字段
  return {
    customerId: form.customerId,
    customerName: form.customerName,
    contractName: form.contractName,
    contractNo: form.contractNo,
    quoteDate: form.quoteDate,
    deliveryDate: form.deliveryDate,
    signingDate: form.signingDate,
    warrantyPeriodMonths: form.warrantyPeriodMonths,
    remark: form.remark,
    auditStatus: form.auditStatus,
    auditDetail: form.auditDetail,
    intermediaryFee: form.intermediaryFee,
    intermediaryFeeStatus: form.intermediaryFeeStatus,
    intermediaryCustomerId: form.intermediaryCustomerId,
    intermediaryCustomerName: form.intermediaryCustomerName,
    linkmanId: form.linkmanId,
    linkmanName: form.linkmanName,
    phone: form.phone,
    address: form.address,
    receiver: form.receiver,
    receiverPhone: form.receiverPhone,
    receiveAddress: form.receiveAddress,
    endCustomerName: form.endCustomerName,
    advancePayment: form.advancePayment,
    attachment: form.attachment,
    detailList: form.detailList?.map((d: any) => ({
      id: d.id,
      productId: d.productId,
      productName: d.productName,
      productCode: d.productCode,
      productSpec: d.productSpec,
      productCategory: d.productCategory,
      productUnit: d.productUnit,
      salesPrice: d.salesPrice,
      salesQuantity: d.salesQuantity,
      productRemark: d.productRemark,
      customRemark: d.customRemark,
      costPrice: d.costPrice
    }))
  }
}
const isFormDirty = computed(() => {
  return JSON.stringify(getFormSnapshot()) !== initialFormData.value
})
const currentCustomer = computed(() => customerList.value.find((x:any) => x.id === form.customerId))
const isAgentCustomer = computed(() => {
  const c:any = currentCustomer.value || {}
  const t = [c.customerType, c.customerCategory, c.customerTag].map(x => String(x || '')).join(' ')
  return t.includes('代理')
})

const formatCurrency = (value: number | string | undefined | null) => {
  const n = Number(value ?? 0)
  return `¥${n.toFixed(2)}`
}

const fetchCustomers = async () => {
  console.log('开始加载客户列表...')
  const res: any = await getCustomerList({ current: 1, size: 1000 })
  const list = res.records || []
  console.log('加载到的客户列表:', list.map((x:any) => ({id: x.id, customerName: x.customerName, name: x.name, companyName: x.companyName})))
  try {
    const collator = new Intl.Collator(['zh-Hans-u-co-pinyin', 'zh'], { usage: 'sort', sensitivity: 'base' })
    list.sort((a:any, b:any) => collator.compare(String(a.customerName || ''), String(b.customerName || '')))
  } catch {
    list.sort((a:any, b:any) => String(a.customerName || '').localeCompare(String(b.customerName || ''), 'zh'))
  }
  customerList.value = list
  console.log('排序后的客户列表:', list.map((x:any) => ({id: x.id, customerName: x.customerName, name: x.name, companyName: x.companyName})))
}

const fetchProducts = async () => {
  const res: any = await getProductList({ current: 1, size: 1000 })
  productList.value = res.records
}

const handleProductChange = (productId: number, row: any) => {
  const product = productList.value.find(p => p.id === productId)
  if (product) {
    row.productName = product.productName
    row.productCode = product.productCode
    row.costPrice = Number(product.costPrice || 0)
    row.salesPrice = Number(product.terminalPrice || product.channelPrice || product.costPrice || 0)
    row.productSpec = product.productSpec
    row.productUnit = product.productUnit
    row.productRemark = product.remark || row.productRemark
  }
}


const addDetail = () => {
  const newRow = {
    productId: undefined,
    productName: '',
    productCode: '',
    productSpec: '',
    productUnit: '',
    costPrice: 0,
    salesPrice: 0,
    salesQuantity: 1,
    salesAmount: 0,
    productRemark: ''
  }
  form.detailList.push(newRow)
  // 添加后跳转到最后一页
  const totalPages = Math.ceil(form.detailList.length / detailPageSize.value)
  detailCurrentPage.value = totalPages
}

// 根据分页后的索引删除明细
const removeDetailByIndex = (pageIndex: number) => {
  const realIndex = (detailCurrentPage.value - 1) * detailPageSize.value + pageIndex
  form.detailList.splice(realIndex, 1)
  // 如果删除后当前页没有数据了，回到上一页
  const totalPages = Math.ceil((form.detailList.length || 0) / detailPageSize.value)
  if (detailCurrentPage.value > totalPages && detailCurrentPage.value > 1) {
    detailCurrentPage.value = totalPages || 1
  }
}

// 明细分页事件
const onDetailSizeChange = (val: number) => {
  detailPageSize.value = val
  detailCurrentPage.value = 1
}

const onDetailCurrentChange = (val: number) => {
  detailCurrentPage.value = val
}

// 打开编辑明细弹窗
const openEditDetailDialog = (pageIndex: number) => {
  const realIndex = (detailCurrentPage.value - 1) * detailPageSize.value + pageIndex
  const row = form.detailList[realIndex]
  if (!row) return

  editDetailIndex.value = realIndex
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
    customRemark: row.customRemark || '',
    productRemark: row.productRemark || ''
  })
  editDetailVisible.value = true
}

// 编辑弹窗中商品选择变化
const onEditProductChange = (productId: number) => {
  const product = productList.value.find(p => p.id === productId)
  if (product) {
    editDetailForm.productName = product.productName
    editDetailForm.productCode = product.productCode
    editDetailForm.productSpec = product.productSpec
    editDetailForm.productUnit = product.productUnit
    editDetailForm.costPrice = Number(product.costPrice || 0)
    editDetailForm.salesPrice = Number(product.terminalPrice || product.channelPrice || product.costPrice || 0)
    editDetailForm.productRemark = product.remark || editDetailForm.productRemark
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
    customRemark: editDetailForm.customRemark,
    productRemark: editDetailForm.productRemark
  })

  editDetailVisible.value = false
  ElMessage.success('修改成功')
}

const handleSubmit = async () => {
  if (isLocked.value) {
    ElMessage.warning('审核通过的合同不可修改')
    return
  }
  if (!form.customerId) {
    ElMessage.warning('请选择客户')
    return
  }
  if (!form.quoteDate) {
    ElMessage.warning('请选择报价日期')
    return
  }
  if (isAgentCustomer.value && !endCustomerId.value) {
    ElMessage.warning('代理商客户需选择终端客户')
    return
  }

  // 调试日志：打印提交前的数据
  console.log('=== handleSubmit 调试开始 ===')
  console.log('合同ID:', form.id)
  console.log('合同金额:', form.contractAmount)
  console.log('明细数量:', form.detailList.length)
  form.detailList.forEach((row: any, index: number) => {
    console.log(`明细${index} - ID: ${row.id}, 商品: ${row.productName}, 价格: ${row.salesPrice}, 数量: ${row.salesQuantity}`)
  })
  if (!form.detailList || form.detailList.length === 0) {
    ElMessage.warning('请调用报价单导入商品明细')
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

  if (!isLocked.value) {
    form.contractAmount = Number(totalAmount.value)
    form.productTotal = form.detailList.reduce((sum:number, x:any) => sum + Number(x.salesQuantity||0), 0)
  }

  if (endCustomerId.value && !form.endCustomerName) {
    const c = customerList.value.find((x:any) => x.id === endCustomerId.value)
    form.endCustomerName = c?.customerName || form.endCustomerName
  }
  const payload:any = {
    id: form.id,
    customerId: form.customerId,
    linkmanId: form.linkmanId,
    contractName: form.contractName,
    contractNo: form.contractNo,
    phone: form.phone,
    address: form.address,
    receiver: form.receiver,
    receiverPhone: form.receiverPhone,
    receiveAddress: form.receiveAddress,
    endCustomerName: form.endCustomerName,
    attachment: Array.isArray(attachments) && attachments.length ? attachments[0].url : form.attachment,
    extendFields: JSON.stringify({ attachments, endCustomerId: endCustomerId.value || undefined, endCustomerName: form.endCustomerName || undefined }),
    quoteDate: form.quoteDate,
    deliveryDate: form.deliveryDate,
    signingDate: form.signingDate,
    warrantyPeriodMonths: form.warrantyPeriodMonths,
    remark: form.remark,
    auditStatus: form.auditStatus,
    auditDetail: form.auditDetail
  }
  payload.intermediaryFee = Number(form.intermediaryFee || 0)
  payload.intermediaryFeeStatus = form.intermediaryFeeStatus || 'UNPAID'
  if (form.intermediaryCustomerId) {
    const ic = customerList.value.find((x:any) => x.id === form.intermediaryCustomerId)
    payload.intermediaryCustomerId = form.intermediaryCustomerId
    payload.intermediaryCustomerName = ic?.customerName || form.intermediaryCustomerName || ''
  }
  if (!isLocked.value) {
    payload.contractAmount = form.contractAmount
    payload.productTotal = form.productTotal
    payload.detailList = form.detailList.map((row:any) => ({
      id: row.id, // 添加id字段，用于后端识别更新还是新增
      productName: row.productName,
      productCode: row.productCode,
      productSpec: row.productSpec,
      productCategory: row.productCategory,
      productImage: row.productImage,
      salesPrice: row.salesPrice,
      salesQuantity: row.salesQuantity,
      productRemark: row.productRemark,
      customRemark: row.customRemark,
      costPrice: row.costPrice
    }))
  }
  try {
    if (saving.value) return
    saving.value = true
    if (!String(form.contractName || '').trim()) {
      ElMessage.warning('请填写合同名称（包含客户名称+合同名称）')
      saving.value = false
      return
    }
    const custName = currentCustomerName.value
    console.log('验证合同名称:', {
      contractName: form.contractName,
      customerName: custName,
      customerId: form.customerId,
      currentCustomer: currentCustomer.value
    })
    
    if (custName && custName !== '未选择') {
      // 验证合同名称是否包含客户名称（忽略大小写和空格）
      const contractName = String(form.contractName).toLowerCase().replace(/\s/g, '')
      const customerName = custName.toLowerCase().replace(/\s/g, '')
      
      // 获取终端客户名称（如果是代理商客户）
      let endCustomerName = ''
      if (isAgentCustomer.value && endCustomerId.value) {
        const endCustomer = customerList.value.find((c: any) => c.id === endCustomerId.value)
        endCustomerName = (endCustomer?.customerName || endCustomer?.name || endCustomer?.companyName || '').toLowerCase().replace(/\s/g, '')
      }
      
      console.log('验证结果:', {
        originalContractName: form.contractName,
        originalCustomerName: custName,
        isAgentCustomer: isAgentCustomer.value,
        endCustomerId: endCustomerId.value,
        endCustomerName: endCustomerName,
        contractNameLower: contractName,
        customerNameLower: customerName,
        includesCustomer: contractName.includes(customerName),
        includesEndCustomer: endCustomerName && contractName.includes(endCustomerName),
        contractLength: contractName.length,
        customerLength: customerName.length
      })
      
      // 验证逻辑：
      // 1. 普通客户：合同名称必须包含签约客户名称
      // 2. 代理商客户：合同名称可以包含签约客户名称或终端客户名称
      let isValid = false
      let validationMessage = ''
      
      if (isAgentCustomer.value) {
        // 代理商客户：检查是否包含签约客户或终端客户
        const includesCustomer = contractName.includes(customerName)
        const includesEndCustomer = !!endCustomerName && contractName.includes(endCustomerName)
        
        isValid = includesCustomer || includesEndCustomer
        
        if (!isValid) {
          validationMessage = `合同名称必须包含客户名称"${custName}"`
          if (endCustomerName) {
            validationMessage += `或终端客户名称"${endCustomerName}"`
          }
        }
      } else {
        // 普通客户：必须包含签约客户名称
        isValid = contractName.includes(customerName)
        if (!isValid) {
          validationMessage = `合同名称必须包含客户名称"${custName}"`
        }
      }
      
      if (!isValid) {
        // 检查是否有其他客户可能匹配这个合同名称
        const possibleCustomers = customerList.value.filter((c: any) => {
          const name = (c.customerName || c.name || c.companyName || '').toLowerCase().replace(/\s/g, '')
          return contractName.includes(name) && name.length > 0
        })
        
        console.log('可能的客户匹配:', possibleCustomers.map((c: any) => ({
          id: c.id,
          name: c.customerName || c.name || c.companyName
        })))
        
        let message = validationMessage + `，当前合同名称为"${form.contractName}"`
        if (possibleCustomers.length > 0) {
          const suggestedNames = possibleCustomers.map((c: any) => c.customerName || c.name || c.companyName).join('、')
          message += `。\n\n注意：当前合同名称可能更适合以下客户：${suggestedNames}`
        }
        
        ElMessage.warning(message)
        saving.value = false
        return
      }
    }
    if (form.id) {
      await updateContract(form.id, payload)
      ElMessage.success('更新成功')
    } else {
      await saveContract(payload)
      ElMessage.success('创建成功')
    }
    // 保存成功后更新初始表单数据，避免再次提示保存
    initialFormData.value = JSON.stringify(getFormSnapshot())
    // 关闭对话框并通知父组件
    visible.value = false
    emit('success')
  } catch (e) {
    // handled by request.ts
  } finally {
    saving.value = false
  }
}

const resetForm = () => {
  form.id = undefined
  form.customerId = undefined
  form.customerName = ''
  form.quoteDate = new Date().toISOString().split('T')[0]
  form.deliveryDate = ''
  form.signingDate = ''
  form.warrantyPeriodMonths = undefined
  form.remark = ''
  form.contractName = ''
  form.contractNo = ''
  form.attachment = ''
  form.detailList = []
  form.auditStatus = 'PENDING'
  form.auditDetail = ''
  form.linkmanId = undefined
  form.linkmanName = ''
  form.phone = ''
  form.address = ''
  form.receiver = ''
  form.receiverPhone = ''
  form.receiveAddress = ''
  form.advancePayment = 0
  form.intermediaryFee = 0
  form.intermediaryCustomerId = undefined
  form.intermediaryCustomerName = ''
  form.intermediaryFeeStatus = 'UNPAID'
  form.endCustomerName = ''
  initialAuditStatus.value = 'PENDING'
  attachments.splice(0, attachments.length)
  initialFormData.value = ''
  endCustomerId.value = undefined
}

const handleBeforeClose = async (done: () => void) => {
  // 如果有修改，提示用户是否保存
  if (isFormDirty.value) {
    try {
      await ElMessageBox.confirm('合同内容已修改，是否保存？', '提示', {
        confirmButtonText: '保存',
        cancelButtonText: '不保存',
        distinguishCancelAndClose: true,
        type: 'warning'
      })
      // 用户点击保存，执行保存操作
      await handleSubmit()
      // handleSubmit 中会关闭对话框，这里不需要调用 done()
    } catch (action: any) {
      if (action === 'cancel') {
        // 用户点击不保存，继续关闭
        resetForm()
        done()
      }
      // 其他情况（点击关闭按钮或按 ESC，或保存失败），取消关闭操作
    }
  } else {
    // 没有修改，直接关闭
    resetForm()
    done()
  }
}

const handleClose = () => {
  resetForm()
  visible.value = false
}

watch(() => props.id, async (val) => {
  if (val) {
    const res: any = await getContractById(val)
    Object.assign(form, res)
    initialAuditStatus.value = String(res?.auditStatus || 'PENDING')
    try {
      const ext = res?.extendFields ? JSON.parse(res.extendFields) : {}
      const list = Array.isArray(ext?.attachments) ? ext.attachments : []
      attachments.splice(0, attachments.length, ...list)
      if (!form.attachment && attachments.length) form.attachment = attachments[0].url
    } catch {}
    if (form.endCustomerName) {
      const match = customerList.value.find((x:any) => x.customerName === form.endCustomerName)
      endCustomerId.value = match?.id
    }
    if (!productList.value || productList.value.length === 0) {
      await fetchProducts()
    }
    (form.detailList || []).forEach((row:any) => {
      if (!row.productId) {
        const found = productList.value.find((p:any) => p.productCode === row.productCode || p.productName === row.productName)
        if (found) {
          row.productId = found.id
        } else {
          // 商品已被删除，保持原有数据，不强制关联商品ID
          console.warn(`商品不存在: code=${row.productCode}, name=${row.productName}`)
        }
      }
    })
    onCustomerChange()
    // 保存初始表单数据用于比较
    await nextTick()
    initialFormData.value = JSON.stringify(getFormSnapshot())
  } else {
    // 新增合同时，重置表单但不关闭对话框
    resetForm()
    initialFormData.value = ''
  }
})

watch(() => visible.value, async (v) => {
  if (v) {
    if (props.id) {
      const res: any = await getContractById(props.id)
      Object.assign(form, res)
      initialAuditStatus.value = String(res?.auditStatus || 'PENDING')
      try {
        const ext = res?.extendFields ? JSON.parse(res.extendFields) : {}
        const list = Array.isArray(ext?.attachments) ? ext.attachments : []
        attachments.splice(0, attachments.length, ...list)
        if (!form.attachment && attachments.length) form.attachment = attachments[0].url
      } catch {}
      if (form.endCustomerName) {
        const match = customerList.value.find((x:any) => x.customerName === form.endCustomerName)
        endCustomerId.value = match?.id
      }
    } else {
      // 新增合同时，重置表单
      resetForm()
      initialFormData.value = ''
    }
  }
})

onMounted(() => {
  fetchCustomers()
  fetchProducts()
  userStore.getInfo().then(() => {
    const ui:any = userStore.userInfo || {}
    salesmanName.value = ui.name || ui.realName || ui.nickname || ui.username || ''
  })
})

const onCustomerChange = () => {
  console.log('客户选择变化:', {
    customerId: form.customerId,
    customerList: customerList.value.map((x:any) => ({id: x.id, name: x.customerName || x.name || x.companyName}))
  })
  
  const c = customerList.value.find((x:any) => x.id === form.customerId)
  console.log('找到的客户对象:', c)
  
  if (c) {
    form.address = c.address || ''
    
    // 设置客户名称
    const customerName = c?.customerName || c?.name || c?.companyName || ''
    console.log('设置客户名称:', customerName)
    form.customerName = customerName
    
    // 如果合同名称为空，自动填充客户名称
    if (!form.contractName || form.contractName === '') {
      if (customerName) {
        form.contractName = customerName
      }
    }
    
    request({ url: '/crm/contact/list', method: 'get', params: { customerId: form.customerId } }).then((res:any) => {
      const list = res || res?.data || []
      console.log('获取联系人列表:', {
        customerId: form.customerId,
        response: res,
        contactList: list,
        listLength: list.length
      })
      
      const primary = list.find((x:any) => Number(x.isPrimary) === 1)
      const pick = primary || list[0]
      
      console.log('选择的联系人:', {
        primaryContact: primary,
        selectedContact: pick,
        hasPrimary: !!primary,
        hasAnyContact: list.length > 0
      })
      
      if (pick) {
        form.linkmanId = pick.id
        form.linkmanName = pick.name || ''
        console.log('设置联系人信息:', {
          linkmanId: pick.id,
          linkmanName: pick.name,
          phone: pick.phone
        })
      } else {
        form.linkmanId = undefined
        form.linkmanName = c.linkman || ''
        console.log('使用客户默认联系人:', {
          customerLinkman: c.linkman,
          customerPhone: c.phone
        })
      }
      form.phone = (pick?.phone || c.phone || '')
    })
    if (isAgentCustomer.value) {
      endCustomerId.value = undefined
      form.endCustomerName = ''
    } else {
      endCustomerId.value = form.customerId
      onEndCustomerChange(form.customerId as number)
    }
  }
}

const onEndCustomerChange = (id: number) => {
  console.log('终端客户变更:', { id })
  const c = customerList.value.find((x:any) => x.id === id)
  console.log('终端客户对象:', c)
  form.endCustomerName = c?.customerName || ''
  if (c) {
    request({ url: `/crm/customer/${id}`, method: 'get' }).then((full:any) => {
      const cust = full?.data || full || {}
      form.receiveAddress = cust?.receiveAddress || cust?.address || form.receiveAddress
    }).catch(() => {
      form.receiveAddress = c?.address || form.receiveAddress
    })
    request({ url: '/crm/contact/list', method: 'get', params: { customerId: id } }).then((res:any) => {
      const list = res || res?.data || []
      console.log('终端客户联系人列表:', { list, length: list.length })
      const primary = list.find((x:any) => Number(x.isPrimary) === 1)
      const pick = primary || list[0]
      console.log('选择的终端客户联系人:', { primary, pick })
      form.receiver = (pick?.name || c.linkman || '')
      form.receiverPhone = (pick?.phone || c.phone || '')
      console.log('设置终端客户收货人信息:', {
        receiver: form.receiver,
        receiverPhone: form.receiverPhone
      })
    })
  } else {
    console.log('未找到终端客户，清空收货人信息')
    form.receiver = ''
    form.receiverPhone = ''
  }
}
const attachments = reactive<Array<{ name: string, url: string }>>([])
const onAttachmentSuccess: UploadProps['onSuccess'] = (res:any, file: UploadFile) => {
  const url = res?.data
  const name = file?.name || '附件'
  if (url) {
    attachments.push({ name, url })
    if (!form.attachment) form.attachment = url
  }
}
const onAttachmentBeforeUpload: UploadProps['beforeUpload'] = (file) => {
  const name = file.name.toLowerCase()
  const isImage = file.type?.startsWith('image/')
  const okExt = ['.pdf', '.doc', '.docx'].some(ext => name.endsWith(ext))
  if (!isImage && !okExt) {
    ElMessage.error('仅支持图片、PDF、DOC/DOCX 格式')
    return false
  }
  return true
}
const removeAttachment = (idx: number) => {
  attachments.splice(idx, 1)
  form.attachment = attachments[0]?.url || ''
}

const refreshContactInfo = () => {
  if (!form.customerId) {
    ElMessage.warning('请先选择客户')
    return
  }
  
  console.log('手动刷新联系人信息:', { customerId: form.customerId })
  
  request({ url: '/crm/contact/list', method: 'get', params: { customerId: form.customerId } }).then((res:any) => {
    const list = res || res?.data || []
    console.log('手动获取联系人列表:', { list, length: list.length })
    
    const primary = list.find((x:any) => Number(x.isPrimary) === 1)
    const pick = primary || list[0]
    
    if (pick) {
      form.linkmanId = pick.id
      form.linkmanName = pick.name || ''
      form.phone = pick.phone || ''
      
      ElMessage.success(`联系人信息已更新: ${pick.name || '未知'}`)
      
      console.log('手动设置联系人信息:', {
        linkmanId: pick.id,
        linkmanName: pick.name,
        phone: pick.phone
      })
    } else {
      // 如果没有联系人，使用客户的默认信息
      const c = customerList.value.find((x:any) => x.id === form.customerId)
      if (c) {
        form.linkmanId = undefined
        form.linkmanName = c.linkman || ''
        form.phone = c.phone || ''
        
        ElMessage.info('使用客户默认联系信息')
        
        console.log('手动使用客户默认联系信息:', {
          customerLinkman: c.linkman,
          customerPhone: c.phone
        })
      }
    }
  }).catch((error) => {
    console.error('获取联系人信息失败:', error)
    ElMessage.error('获取联系人信息失败')
  })
}

const useCustomerDefaultContact = () => {
  if (!endCustomerId.value) {
    ElMessage.warning('请先选择终端客户')
    return
  }
  const c = customerList.value.find((x:any) => x.id === endCustomerId.value)
  if (c) {
    form.receiver = c.linkman || ''
    form.receiverPhone = c.phone || ''
  } else {
    ElMessage.info('未找到终端客户信息')
  }
}
const openContactDialog = () => {
  if (!form.customerId) {
    ElMessage.warning('请先选择客户')
    return
  }
  contactDialogVisible.value = true
}

const openQuoteSelect = () => {
  if (!form.customerId) {
    ElMessage.warning('请先选择客户再调用报价单')
    return
  }
  quoteSelectVisible.value = true
  fetchQuoteList()
}
const fetchQuoteList = async () => {
  const res:any = await getQuoteList({ current: quotePage.value, size: quotePageSize.value, customerId: form.customerId })
  quoteList.value = res.records || []
  quoteTotal.value = res.total || 0
}
const importFromQuote = async (id: number) => {
  const res:any = await getQuoteById(id)
  if (!res) return
  form.quoteDate = res.quoteDate || form.quoteDate
  form.customerId = res.customerId || form.customerId
  if (form.customerId) {
    const c = customerList.value.find((x:any) => x.id === form.customerId)
    const t = [c?.customerType, c?.customerCategory, c?.customerTag].map(x => String(x || '')).join(' ')
    if (t.includes('代理')) {
      endCustomerId.value = undefined
      form.endCustomerName = ''
    } else {
      endCustomerId.value = form.customerId
      onEndCustomerChange(form.customerId as number)
    }
  }
  if (!productList.value || productList.value.length === 0) {
    await fetchProducts()
  }
  form.detailList = (res.detailList || []).map((d:any) => {
    const found = productList.value.find((p:any) => p.productCode === d.productCode || p.productName === d.productName)
    return {
      id: d.id, // 保留明细ID，用于后端识别更新还是新增
      productName: d.productName,
      productCode: d.productCode,
      productSpec: d.productSpec,
      salesPrice: Number(d.salesPrice || 0),
      salesQuantity: Number(d.salesQuantity || 1),
      costPrice: Number(d.costPrice || 0),
      productRemark: (found?.remark ?? d.productRemark ?? ''),
      // 如果商品存在则设置productId，不存在则保持undefined
      productId: found?.id
    }
  })
  // 导入后重置到第一页
  detailCurrentPage.value = 1
  quoteSelectVisible.value = false
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
.detail-pagination {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
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
