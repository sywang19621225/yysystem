<template>
  <el-dialog
    v-model="dialogVisible"
    title="选择商品"
    width="700px"
    :close-on-click-modal="false"
  >
    <el-form :model="searchForm" inline>
      <el-form-item label="商品编码">
        <el-input v-model="searchForm.productCode" placeholder="请输入商品编码" clearable />
      </el-form-item>
      <el-form-item label="商品名称">
        <el-input v-model="searchForm.productName" placeholder="请输入商品名称" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" v-loading="loading" border @row-click="handleRowClick">
      <el-table-column prop="productCode" label="商品编码" width="120" />
      <el-table-column prop="productName" label="商品名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="productSpec" label="规格" width="120" show-overflow-tooltip />
      <el-table-column prop="productUnit" label="单位" width="60" align="center" />
      <el-table-column prop="costPrice" label="成本价" width="100" align="right">
        <template #default="{ row }">
          {{ formatMoney(row.costPrice) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80" align="center">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click.stop="handleSelect(row)">选择</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      class="pagination"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getProductList } from '@/api/product'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'select', product: any): void
}>()

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const loading = ref(false)
const tableData = ref<any[]>([])
const searchForm = ref({
  productCode: '',
  productName: ''
})
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取列表数据
const fetchList = async () => {
  loading.value = true
  try {
    const params: any = {
      current: pageNum.value,
      size: pageSize.value
    }
    if (searchForm.value.productCode) params.productCode = searchForm.value.productCode
    if (searchForm.value.productName) params.productName = searchForm.value.productName

    const data: any = await getProductList(params)
    console.log('Product API response:', data)
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch (error) {
    console.error('Product API error:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  fetchList()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    productCode: '',
    productName: ''
  }
  pageNum.value = 1
  fetchList()
}

// 分页
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchList()
}

const handleCurrentChange = (val: number) => {
  pageNum.value = val
  fetchList()
}

// 选择商品
const handleSelect = (row: any) => {
  emit('select', row)
  dialogVisible.value = false
}

const handleRowClick = (row: any) => {
  emit('select', row)
  dialogVisible.value = false
}

const formatMoney = (value: number) => {
  if (!value && value !== 0) return '-'
  return '¥' + Number(value).toFixed(2)
}

// 监听弹窗显示
watch(() => props.visible, (val) => {
  if (val) {
    fetchList()
  }
})
</script>

<style scoped>
.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
