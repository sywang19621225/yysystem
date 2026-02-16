<template>
  <div class="app-container">
    <div class="header-actions">
      <el-form :inline="true" :model="queryParams" class="search-form" @submit.prevent>
        <el-form-item label="商品名称">
          <el-input v-model="queryParams.productName" placeholder="请输入商品名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="商品编号">
          <el-input v-model="queryParams.productCode" placeholder="请输入商品编号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <span class="title">实时库存查询</span>
    </div>

    <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
      <el-table-column prop="productCode" label="商品编号" width="180" align="center" />
      <el-table-column prop="productName" label="商品名称" show-overflow-tooltip />
      <el-table-column prop="productSpec" label="规格" width="150" align="center" />
      <el-table-column prop="productUnit" label="单位" width="80" align="center" />
      <el-table-column prop="stockQuantity" label="当前库存" width="120" align="center">
        <template #default="scope">
          <span class="stock-num">{{ scope.row.stockQuantity }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="availableStock" label="可用库存" width="120" align="center">
        <template #default="scope">
          <span class="stock-num available">{{ scope.row.availableStock }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="totalStock" label="总库存" width="120" align="center" />
      <el-table-column label="预警状态" width="120" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.stockQuantity < (scope.row.stockLowerWarning || 0)" type="danger">库存不足</el-tag>
          <el-tag v-else-if="scope.row.stockQuantity > (scope.row.stockUpperWarning || 99999)" type="warning">库存积压</el-tag>
          <el-tag v-else type="success">正常</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getProductList } from '@/api/product'

const tableData = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const queryParams = reactive({
  productName: '',
  productCode: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      ...queryParams
    }
    const res: any = await getProductList(params)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  currentPage.value = 1
  fetchData()
}

const resetQuery = () => {
  queryParams.productName = ''
  queryParams.productCode = ''
  handleQuery()
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.search-form {
  margin-bottom: -18px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}
.stock-num {
  font-weight: bold;
}
.stock-num.available {
  color: #67c23a;
}
</style>
