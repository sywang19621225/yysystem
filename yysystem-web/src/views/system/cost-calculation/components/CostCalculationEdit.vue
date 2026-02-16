<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑成本核算' : '新增成本核算'"
    width="900px"
    :close-on-click-modal="false"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <!-- 商品选择 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <span>商品信息</span>
        </template>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="选择商品" prop="productId">
              <el-select
                v-model="form.productId"
                filterable
                placeholder="请选择商品"
                :loading="productLoading"
                style="width: 100%"
                @visible-change="handleSelectVisibleChange"
                @change="handleProductChange"
              >
                <el-option
                  v-for="item in productList"
                  :key="item.id"
                  :label="`${item.productCode} - ${item.productName}`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品编码">
              <el-input v-model="form.productCode" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名称">
              <el-input v-model="form.productName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格">
              <el-input v-model="form.productSpec" disabled />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <!-- 部件明细 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <div class="card-header">
            <span>部件明细（硬件成本）</span>
            <div>
              <el-button type="primary" size="small" @click="handleAddFromStock">从库存选择</el-button>
              <el-button size="small" @click="handleAddCustom">添加自定义</el-button>
            </div>
          </div>
        </template>
        <el-table :data="form.details" border size="small">
          <el-table-column type="index" label="序号" width="50" align="center" />
          <el-table-column label="类型" width="80">
            <template #default="{ row }">
              <el-tag :type="row.itemType === 1 ? 'success' : 'info'" size="small">
                {{ row.itemType === 1 ? '库存' : '自定义' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="itemCode" label="物料编码" width="120" />
          <el-table-column prop="itemName" label="物料名称" min-width="150" show-overflow-tooltip />
          <el-table-column prop="itemSpec" label="规格" width="120" show-overflow-tooltip />
          <el-table-column prop="itemUnit" label="单位" width="60" align="center" />
          <el-table-column label="单价" width="100" align="right">
            <template #default="{ row }">
              <el-input-number v-model="row.unitPrice" :min="0" :precision="2" :controls="false" size="small" style="width: 80px" @change="calcSubtotal(row)" />
            </template>
          </el-table-column>
          <el-table-column label="数量" width="100" align="right">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="0" :precision="2" :controls="false" size="small" style="width: 80px" @change="calcSubtotal(row)" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="100" align="right">
            <template #default="{ row }">
              {{ formatMoney(row.subtotal) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ $index }">
              <el-button type="danger" link size="small" @click="handleDeleteDetail($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="summary-row">
          <span>硬件成本合计：</span>
          <span class="amount">{{ formatMoney(hardwareCost) }}</span>
        </div>
      </el-card>

      <!-- 其他成本 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <span>其他成本</span>
        </template>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="装配费用">
              <el-input-number v-model="form.installCost" :min="0" :precision="2" style="width: 100%" @change="calcTotal" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="现场安装">
              <el-input-number v-model="form.laborCost" :min="0" :precision="2" style="width: 100%" @change="calcTotal" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="软件成本">
              <el-input-number v-model="form.testCost" :min="0" :precision="2" style="width: 100%" @change="calcTotal" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="损耗成本">
              <el-input-number v-model="form.lossCost" :min="0" :precision="2" style="width: 100%" @change="calcTotal" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="认证费用">
              <el-input-number v-model="form.certCost" :min="0" :precision="2" style="width: 100%" @change="calcTotal" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="仓储包装">
              <el-input-number v-model="form.packageCost" :min="0" :precision="2" style="width: 100%" @change="calcTotal" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="运输成本">
              <el-input-number v-model="form.transportCost" :min="0" :precision="2" style="width: 100%" @change="calcTotal" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="毛利率(%)">
              <el-input-number v-model="form.profitMargin" :min="0" :precision="2" style="width: 100%" @change="calcTotal" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <!-- 成本汇总 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <span>成本汇总</span>
        </template>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="cost-item">
              <span class="label">硬件成本：</span>
              <span class="value">{{ formatMoney(hardwareCost) }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="cost-item">
              <span class="label">其他成本：</span>
              <span class="value">{{ formatMoney(otherCost) }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="cost-item highlight">
              <span class="label">总成本：</span>
              <span class="value">{{ formatMoney(totalCost) }}</span>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 15px;">
          <el-col :span="8">
            <div class="cost-item">
              <span class="label">毛利金额：</span>
              <span class="value">{{ formatMoney(profitAmount) }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="cost-item highlight">
              <span class="label">内部价格：</span>
              <span class="value">{{ formatMoney(suggestedPrice) }}</span>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 15px;">
          <el-col :span="8">
            <div class="cost-item">
              <span class="label">渠道价格：</span>
              <el-input-number v-model="form.channelPrice" :min="0" :precision="2" size="small" style="width: 120px" />
            </div>
          </el-col>
          <el-col :span="8">
            <div class="cost-item">
              <span class="label">零售价格：</span>
              <el-input-number v-model="form.retailPrice" :min="0" :precision="2" size="small" style="width: 120px" />
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 备注 -->
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </template>

    <!-- 选择商品弹窗 -->
    <ProductSelectDialog
      v-model:visible="productSelectVisible"
      @select="handleSelectProduct"
    />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import ProductSelectDialog from './ProductSelectDialog.vue'

const props = defineProps<{
  visible: boolean
  id?: number
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const isEdit = computed(() => !!props.id)

const formRef = ref()
const form = ref({
  productId: undefined as number | undefined,
  productCode: '',
  productName: '',
  productSpec: '',
  installCost: 0,
  laborCost: 0,
  testCost: 0,
  lossCost: 0,
  certCost: 0,
  packageCost: 0,
  transportCost: 0,
  profitMargin: 20,
  suggestedPrice: 0,
  channelPrice: 0,
  retailPrice: 0,
  remark: '',
  details: [] as any[]
})

const rules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }]
}

// 计算属性
const hardwareCost = computed(() => {
  return form.value.details.reduce((sum, item) => sum + (item.subtotal || 0), 0)
})

const otherCost = computed(() => {
  return (form.value.installCost || 0) +
         (form.value.laborCost || 0) +
         (form.value.testCost || 0) +
         (form.value.lossCost || 0) +
         (form.value.certCost || 0) +
         (form.value.packageCost || 0) +
         (form.value.transportCost || 0)
})

const totalCost = computed(() => {
  return hardwareCost.value + otherCost.value
})

const profitAmount = computed(() => {
  return totalCost.value * (form.value.profitMargin || 0) / 100
})

const suggestedPrice = computed(() => {
  return totalCost.value + profitAmount.value
})

// 商品选择
const productLoading = ref(false)
const productList = ref<any[]>([])
const productSelectVisible = ref(false)

// 加载所有商品列表
// 加载自产商品和自产软件（用于上部选择商品）
const loadAllProducts = async () => {
  productLoading.value = true
  try {
    console.log('开始加载自产商品列表...')
    const token = localStorage.getItem('token')
    // 加载自产商品
    const response1 = await fetch(`/api/pdm/product/list?current=1&size=1000&productCategory=${encodeURIComponent('自产商品')}`, {
      headers: {
        'Authorization': 'Bearer ' + token
      }
    })
    const result1 = await response1.json()
    // 加载自产软件
    const response2 = await fetch(`/api/pdm/product/list?current=1&size=1000&productCategory=${encodeURIComponent('自产软件')}`, {
      headers: {
        'Authorization': 'Bearer ' + token
      }
    })
    const result2 = await response2.json()
    
    const list1 = result1.code === 200 ? (result1.data.records || []) : []
    const list2 = result2.code === 200 ? (result2.data.records || []) : []
    productList.value = [...list1, ...list2]
    console.log('加载到的自产商品数量:', productList.value.length)
  } catch (error) {
    console.error('加载商品列表失败:', error)
  } finally {
    productLoading.value = false
  }
}

const handleSelectVisibleChange = (visible: boolean) => {
  if (visible && productList.value.length === 0) {
    loadAllProducts()
  }
}

const handleProductChange = (productId: number) => {
  const product = productList.value.find(p => p.id === productId)
  if (product) {
    form.value.productCode = product.productCode
    form.value.productName = product.productName
    form.value.productSpec = product.productSpec
  }
}

// 添加明细
const handleAddFromStock = () => {
  productSelectVisible.value = true
}

const handleSelectProduct = (product: any) => {
  form.value.details.push({
    itemType: 1,
    itemId: product.id,
    itemCode: product.productCode,
    itemName: product.productName,
    itemSpec: product.productSpec,
    itemUnit: product.productUnit,
    unitPrice: product.costPrice || 0,
    quantity: 1,
    subtotal: product.costPrice || 0
  })
}

const handleAddCustom = () => {
  form.value.details.push({
    itemType: 2,
    itemCode: '',
    itemName: '',
    itemSpec: '',
    itemUnit: '个',
    unitPrice: 0,
    quantity: 1,
    subtotal: 0
  })
}

const handleDeleteDetail = (index: number) => {
  form.value.details.splice(index, 1)
}

const calcSubtotal = (row: any) => {
  row.subtotal = (row.unitPrice || 0) * (row.quantity || 0)
}

const calcTotal = () => {
  // 自动计算，无需额外操作
}

// 保存
const handleSave = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (form.value.details.length === 0) {
    ElMessage.warning('请至少添加一个部件')
    return
  }

  const data = {
    ...form.value,
    hardwareCost: hardwareCost.value,
    totalCost: totalCost.value,
    profitAmount: profitAmount.value,
    suggestedPrice: suggestedPrice.value
  }

  try {
    const url = isEdit.value
      ? `/api/system/cost-calculation/update/${props.id}`
      : '/api/system/cost-calculation/save'
    const method = isEdit.value ? 'POST' : 'POST'

    const response = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    })
    const result = await response.json()
    if (result.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '保存成功')
      dialogVisible.value = false
      emit('success')
    } else {
      ElMessage.error(result.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 加载数据
const loadData = async () => {
  if (!props.id) return
  try {
    const response = await fetch(`/api/system/cost-calculation/detail/${props.id}`)
    const result = await response.json()
    if (result.code === 200) {
      const { calculation, details } = result.data
      form.value = {
        productId: calculation.productId,
        productCode: calculation.productCode,
        productName: calculation.productName,
        productSpec: '',
        installCost: calculation.installCost,
        laborCost: calculation.laborCost,
        testCost: calculation.testCost,
        lossCost: calculation.lossCost,
        certCost: calculation.certCost || 0,
        packageCost: calculation.packageCost || 0,
        transportCost: calculation.transportCost || 0,
        profitMargin: calculation.profitMargin,
        suggestedPrice: calculation.suggestedPrice || 0,
        channelPrice: calculation.channelPrice || 0,
        retailPrice: calculation.retailPrice || 0,
        remark: calculation.remark,
        details: details || []
      }
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

// 监听id变化
watch(() => props.id, (newId) => {
  if (newId) {
    loadData()
  } else {
    form.value = {
      productId: undefined,
      productCode: '',
      productName: '',
      productSpec: '',
      installCost: 0,
      laborCost: 0,
      testCost: 0,
      lossCost: 0,
      certCost: 0,
      packageCost: 0,
      transportCost: 0,
      profitMargin: 20,
      suggestedPrice: 0,
      channelPrice: 0,
      retailPrice: 0,
      remark: '',
      details: []
    }
  }
}, { immediate: true })

// 监听visible变化，打开对话框时加载商品列表
watch(() => props.visible, (val, oldVal) => {
  console.log('visible changed:', val, 'old:', oldVal)
  if (val) {
    loadAllProducts()
  }
}, { immediate: true })

// 组件挂载时，如果对话框可见，加载商品列表
onMounted(() => {
  console.log('CostCalculationEdit mounted, visible:', props.visible)
  if (props.visible) {
    loadAllProducts()
  }
})

const formatMoney = (value: number) => {
  if (!value && value !== 0) return '¥0.00'
  return '¥' + Number(value).toFixed(2)
}
</script>

<style scoped>
.section-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-row {
  text-align: right;
  padding: 10px;
  background: #f5f7fa;
  margin-top: 10px;
  font-weight: bold;
}

.summary-row .amount {
  color: #f56c6c;
  font-size: 18px;
  margin-left: 10px;
}

.cost-item {
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}

.cost-item .label {
  color: #606266;
}

.cost-item .value {
  font-weight: bold;
  margin-left: 10px;
}

.cost-item.highlight {
  background: #ecf5ff;
}

.cost-item.highlight .value {
  color: #409eff;
  font-size: 16px;
}
</style>
