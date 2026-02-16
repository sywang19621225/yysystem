<template>
  <div class="app-container">
    <div class="header-actions">
      <el-radio-group v-model="type" @change="fetchData">
        <el-radio-button label="day">按日统计</el-radio-button>
        <el-radio-button label="month">按月统计</el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
      <el-table-column prop="date" label="日期" align="center" />
      <el-table-column prop="amount" label="销售额" align="right">
        <template #default="scope">
          {{ formatCurrency(scope.row.amount) }}
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getSalesReport } from '@/api/report'

const type = ref('day')
const tableData = ref<any[]>([])
const loading = ref(false)

const formatCurrency = (value: number | undefined) => {
  if (value === undefined || value === null) return '¥0.00'
  return `¥${value.toFixed(2)}`
}

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getSalesReport({ type: type.value })
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
.header-actions {
  margin-bottom: 20px;
}
</style>
