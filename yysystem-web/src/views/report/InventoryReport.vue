<template>
  <div class="app-container">
    <el-table :data="tableData" style="width: 100%" v-loading="loading" show-summary border stripe>
      <el-table-column prop="productCode" label="商品编号" width="150" align="center" />
      <el-table-column prop="productName" label="商品名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="stockQuantity" label="库存数量" width="120" sortable align="center" />
      <el-table-column prop="costPrice" label="成本价" width="120" align="right">
        <template #default="scope">
          {{ formatCurrency(scope.row.costPrice) }}
        </template>
      </el-table-column>
      <el-table-column prop="totalValue" label="库存总值" sortable align="right">
        <template #default="scope">
          {{ formatCurrency(scope.row.totalValue) }}
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getInventoryReport } from '@/api/report'

const tableData = ref<any[]>([])
const loading = ref(false)

const formatCurrency = (value: number | undefined) => {
  if (value === undefined || value === null) return '¥0.00'
  return `¥${value.toFixed(2)}`
}

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getInventoryReport()
    tableData.value = res
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
</style>
