<template>
  <div class="cost-calculation-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品成本核算</span>
          <el-button v-if="canCreate" type="primary" @click="handleAdd">新增核算</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="商品编码">
          <el-input v-model="searchForm.productCode" placeholder="请输入商品编码" clearable />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" :value="0" />
            <el-option label="已核算" :value="1" />
            <el-option label="已应用" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="productSeries" label="商品系列" width="120" show-overflow-tooltip />
        <el-table-column prop="productSpec" label="规格" width="120" show-overflow-tooltip />
        <el-table-column prop="suggestedPrice" label="内部价格" width="100" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.suggestedPrice) }}
          </template>
        </el-table-column>
        <el-table-column prop="channelPrice" label="渠道价格" width="100" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.channelPrice) }}
          </template>
        </el-table-column>
        <el-table-column prop="retailPrice" label="零售价格" width="100" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.retailPrice) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canUpdate" type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link size="small" @click="handleView(row)">查看</el-button>
            <el-button v-if="canApply && row.status !== 2" type="warning" link size="small" @click="handleApply(row)">应用</el-button>
            <el-button v-if="canDelete" type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <CostCalculationEdit
      v-model:visible="editVisible"
      :id="currentId"
      @success="handleSearch"
    />

    <!-- 查看弹窗 -->
    <CostCalculationView
      v-model:visible="viewVisible"
      :id="currentId"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/modules/user'
import CostCalculationEdit from './components/CostCalculationEdit.vue'
import CostCalculationView from './components/CostCalculationView.vue'

const userStore = useUserStore()

// 权限检查函数
const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canCreate = computed(() => hasPermission('PCC:create'))
const canUpdate = computed(() => hasPermission('PCC:update'))
const canDelete = computed(() => hasPermission('PCC:delete'))
const canApply = computed(() => hasPermission('PCC:apply'))

const loading = ref(false)
const tableData = ref([])
const searchForm = ref<{
  productCode: string
  productName: string
  status: number | undefined
}>({
  productCode: '',
  productName: '',
  status: undefined
})

const editVisible = ref(false)
const viewVisible = ref(false)
const currentId = ref<number | undefined>(undefined)

// 获取列表数据
const fetchList = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    if (searchForm.value.productCode) params.append('productCode', searchForm.value.productCode)
    if (searchForm.value.productName) params.append('productName', searchForm.value.productName)
    if (searchForm.value.status !== undefined) params.append('status', searchForm.value.status.toString())

    const response = await fetch(`/api/system/cost-calculation/list?${params.toString()}`)
    const result = await response.json()
    if (result.code === 200) {
      tableData.value = result.data
    } else {
      ElMessage.error(result.message || '获取数据失败')
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  fetchList()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    productCode: '',
    productName: '',
    status: undefined
  }
  fetchList()
}

// 新增
const handleAdd = () => {
  console.log('handleAdd called')
  currentId.value = undefined
  editVisible.value = true
  console.log('editVisible set to:', editVisible.value)
}

// 编辑
const handleEdit = (row: any) => {
  currentId.value = row.id
  editVisible.value = true
}

// 查看
const handleView = (row: any) => {
  currentId.value = row.id
  viewVisible.value = true
}

// 应用
const handleApply = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要应用此成本核算到商品吗？', '提示', {
      type: 'warning'
    })
    const response = await fetch(`/api/system/cost-calculation/apply/${row.id}`, {
      method: 'POST'
    })
    const result = await response.json()
    if (result.code === 200) {
      ElMessage.success('应用成功')
      fetchList()
    } else {
      ElMessage.error(result.message || '应用失败')
    }
  } catch (error) {
    // 取消操作
  }
}

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除此核算记录吗？', '提示', {
      type: 'warning'
    })
    const response = await fetch(`/api/system/cost-calculation/delete/${row.id}`, {
      method: 'DELETE'
    })
    const result = await response.json()
    if (result.code === 200) {
      ElMessage.success('删除成功')
      fetchList()
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error) {
    // 取消操作
  }
}

// 格式化金额
const formatMoney = (value: number) => {
  if (!value && value !== 0) return '-'
  return '¥' + Number(value).toFixed(2)
}

// 获取状态类型
const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'success'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    0: '草稿',
    1: '已核算',
    2: '已应用'
  }
  return texts[status] || '未知'
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.cost-calculation-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}
</style>
