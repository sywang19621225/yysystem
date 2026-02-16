<template>
  <el-dialog
    v-model="dialogVisible"
    title="查看成本核算"
    width="800px"
    :close-on-click-modal="false"
  >
    <div v-if="data" class="view-content">
      <!-- 商品信息 -->
      <el-descriptions title="商品信息" :column="2" border>
        <el-descriptions-item label="商品编码">{{ data.calculation.productCode }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ data.calculation.productName }}</el-descriptions-item>
      </el-descriptions>

      <!-- 部件明细 -->
      <div class="section-title">部件明细（硬件成本）</div>
      <el-table :data="data.details" border size="small">
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
        <el-table-column prop="unitPrice" label="单价" width="100" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.unitPrice) }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" align="right" />
        <el-table-column prop="subtotal" label="小计" width="100" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.subtotal) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 其他成本 -->
      <div class="section-title">其他成本</div>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="安装成本">{{ formatMoney(data.calculation.installCost) }}</el-descriptions-item>
        <el-descriptions-item label="人工成本">{{ formatMoney(data.calculation.laborCost) }}</el-descriptions-item>
        <el-descriptions-item label="软件成本">{{ formatMoney(data.calculation.testCost) }}</el-descriptions-item>
        <el-descriptions-item label="损耗成本">{{ formatMoney(data.calculation.lossCost) }}</el-descriptions-item>
        <el-descriptions-item label="毛利率">{{ data.calculation.profitMargin }}%</el-descriptions-item>
      </el-descriptions>

      <!-- 成本汇总 -->
      <div class="section-title">成本汇总</div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="硬件成本">{{ formatMoney(data.calculation.hardwareCost) }}</el-descriptions-item>
        <el-descriptions-item label="其他成本">
          {{ formatMoney((data.calculation.installCost || 0) + (data.calculation.laborCost || 0) + (data.calculation.testCost || 0) + (data.calculation.lossCost || 0)) }}
        </el-descriptions-item>
        <el-descriptions-item label="总成本" label-class-name="highlight-label">
          <span class="highlight-value">{{ formatMoney(data.calculation.totalCost) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="毛利金额">{{ formatMoney(data.calculation.profitAmount) }}</el-descriptions-item>
        <el-descriptions-item label="内部价格" label-class-name="highlight-label">
          <span class="highlight-value">{{ formatMoney(data.calculation.suggestedPrice) }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 备注 -->
      <div class="section-title">备注</div>
      <div class="remark-content">{{ data.calculation.remark || '无' }}</div>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  visible: boolean
  id?: number
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const data = ref<any>(null)

// 加载数据
const loadData = async () => {
  if (!props.id) return
  try {
    const response = await fetch(`/api/system/cost-calculation/detail/${props.id}`)
    const result = await response.json()
    if (result.code === 200) {
      data.value = result.data
    } else {
      ElMessage.error(result.message || '加载数据失败')
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

// 监听id变化
watch(() => props.id, (newId) => {
  if (newId && props.visible) {
    loadData()
  }
}, { immediate: true })

// 监听弹窗显示
watch(() => props.visible, (val) => {
  if (val && props.id) {
    loadData()
  }
})

const formatMoney = (value: number) => {
  if (!value && value !== 0) return '¥0.00'
  return '¥' + Number(value).toFixed(2)
}
</script>

<style scoped>
.view-content {
  padding: 0 10px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin: 20px 0 10px;
  padding-left: 10px;
  border-left: 4px solid #409eff;
}

.remark-content {
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  min-height: 60px;
  color: #606266;
}

:deep(.highlight-label) {
  background-color: #ecf5ff !important;
  font-weight: bold;
}

.highlight-value {
  color: #409eff;
  font-size: 16px;
  font-weight: bold;
}
</style>
