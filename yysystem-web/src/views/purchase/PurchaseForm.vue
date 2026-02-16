<template>
  <el-dialog v-model="visible" :title="id ? '编辑采购单' : '新增采购单'" width="900px" @close="handleClose">
    <el-form :model="form" label-width="100px">
      <el-row>
        <el-col :span="12">
          <el-form-item label="供应商" required>
            <el-select v-model="form.supplierId" placeholder="请选择供应商" style="width: 100%" filterable>
              <el-option
                v-for="item in supplierList"
                :key="item.id"
                :label="item.supplierName"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="交货日期">
            <el-date-picker v-model="form.deliveryDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="付款方式">
            <el-select v-model="form.paymentMethod" placeholder="请选择付款方式" filterable allow-create default-first-option style="width: 100%">
              <el-option v-for="item in paymentMethodOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="付款金额">
            <el-input-number v-model="form.paymentAmount" :min="0" :precision="2" :step="0.1" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24">
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" />
          </el-form-item>
        </el-col>
      </el-row>

      <div class="detail-header">
        <span>商品明细</span>
        <el-button type="primary" link @click="addDetail">添加商品</el-button>
      </div>
      
      <el-table :data="form.detailList" border style="margin-top: 10px">
        <el-table-column label="商品名称" width="200">
          <template #default="scope">
            <el-select v-model="scope.row.productId" placeholder="选择商品" filterable @change="(val: number) => handleProductChange(val, scope.row)">
               <el-option
                v-for="item in productList"
                :key="item.id"
                :label="item.productName"
                :value="item.id"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="unitPrice" label="采购单价" width="150">
          <template #default="scope">
            <el-input-number v-model="scope.row.unitPrice" :precision="2" :step="0.1" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="采购数量" width="150">
          <template #default="scope">
            <el-input-number v-model="scope.row.quantity" :min="1" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="小计">
          <template #default="scope">
            {{ ((scope.row.unitPrice || 0) * (scope.row.quantity || 0)).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="scope">
            <el-button type="danger" link @click="removeDetail(scope.$index)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div style="margin-top: 10px; text-align: right">
        总金额: <span style="color: red; font-weight: bold">{{ totalAmount }}</span>
      </div>

    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { getPurchaseById, createPurchase, updatePurchase } from '@/api/purchase'
import { getProductList } from '@/api/product'
import { getSupplierList } from '@/api/supplier'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

interface Product {
  id: number
  productName: string
  productCode: string
  productSpec: string
  productUnit: string
  costPrice: number
  mainImage?: string
}

interface PurchaseDetail {
  id?: number
  productId?: number
  productName: string
  productCode: string
  productSpec?: string
  productUnit?: string
  productImage?: string
  unitPrice: number
  quantity: number
}

interface PurchaseForm {
  id?: number
  supplierId?: number
  deliveryDate?: string
  paymentMethod?: string
  paymentAmount?: number
  remark: string
  detailList: PurchaseDetail[]
}

const props = defineProps<{
  modelValue: boolean
  id?: number
}>()

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const form = reactive<PurchaseForm>({
  id: undefined,
  supplierId: undefined,
  deliveryDate: undefined,
  paymentMethod: '',
  paymentAmount: 0,
  remark: '',
  detailList: []
})

const productList = ref<Product[]>([])
const supplierList = ref<any[]>([])
const paymentMethodOptions = ref<string[]>([])

const totalAmount = computed(() => {
  return form.detailList.reduce((sum: number, item: PurchaseDetail) => {
    return sum + (item.unitPrice || 0) * (item.quantity || 0)
  }, 0).toFixed(2)
})

const fetchBaseData = async () => {
  const pRes: any = await getProductList({ size: 1000 })
  productList.value = pRes.records
  const sRes: any = await getSupplierList({ size: 1000 })
  supplierList.value = sRes.records
}

const fetchPaymentMethods = async () => {
  try {
    const res:any = await request({ url: '/system/config/list', method: 'get', params: { size: 100, current: 1 } })
    const list = res?.records || res || []
    const general = (list || []).find((item:any) => item.configKey === 'general_settings')
    let cfg:any = {}
    try { cfg = general?.configValue ? JSON.parse(general.configValue) : {} } catch { cfg = {} }
    const custom = cfg.customCategories || {}
    const keys = Object.keys(custom || {})
    const pickByName = (keyword:string) => {
      const k = keys.find(name => String(name || '').includes(keyword))
      return k ? (custom[k] || []) : []
    }
    let methods:string[] = Array.isArray(cfg.settlementMethods) ? cfg.settlementMethods : []
    if (!methods.length) {
      const bySettle = pickByName('结算方式')
      const byPay = pickByName('付款方式')
      methods = bySettle.length ? bySettle : byPay
    }
    paymentMethodOptions.value = methods || []
  } catch {
    paymentMethodOptions.value = []
  }
}

watch(() => props.id, async (newId) => {
  if (newId) {
    const res: any = await getPurchaseById(newId)
    // Map backend detail list to form structure if needed
    if (res.detailList) {
        res.detailList.forEach((d: any) => {
            const p = productList.value.find(prod => prod.productCode === d.productCode)
            if (p) {
                d.productId = p.id
            }
        })
    }
    Object.assign(form, res)
  } else {
    form.id = undefined
    form.supplierId = undefined
    form.deliveryDate = undefined
    form.remark = ''
    form.detailList = []
  }
})

const addDetail = () => {
  form.detailList.push({
    productId: undefined,
    productName: '',
    productCode: '',
    unitPrice: 0,
    quantity: 1
  })
}

const removeDetail = (index: number) => {
  form.detailList.splice(index, 1)
}

const handleProductChange = (productId: number, row: PurchaseDetail) => {
  const product = productList.value.find(p => p.id === productId)
  if (product) {
    row.productName = product.productName
    row.productCode = product.productCode
    row.unitPrice = product.costPrice || 0 // Default to cost price
    row.productSpec = product.productSpec
    row.productUnit = product.productUnit
    row.productImage = product.mainImage
  }
}

const handleSubmit = async () => {
  try {
    // Before submit, ensure data is clean
    if (form.id) {
      await updatePurchase(form)
    } else {
      await createPurchase(form)
    }
    ElMessage.success('操作成功')
    visible.value = false
    emit('success')
  } catch (e) {
    // handled
  }
}

const handleClose = () => {
  emit('update:modelValue', false)
}

onMounted(() => {
  fetchBaseData()
  fetchPaymentMethods()
})
</script>

<style scoped>
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}
</style>
