<template>
  <div class="app-container">
    <!-- 调试信息 -->
    <div v-if="false" style="background: #f0f0f0; padding: 10px; margin-bottom: 10px; font-size: 12px;">
      <pre>{{ JSON.stringify(debugInfo, null, 2) }}</pre>
    </div>
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>出库单</h2>
      <div class="header-actions">
        <el-button v-if="canCreate" type="primary" @click="handleCreate" icon="Plus">新建出库单</el-button>
        <el-button @click="handleExport" icon="Download">导出</el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="出库单号">
              <el-input 
                v-model="searchForm.outStockNo" 
                placeholder="请输入出库单号" 
                clearable 
                @keyup.enter="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="客户名称">
              <el-input 
                v-model="searchForm.customerName" 
                placeholder="请输入客户名称" 
                clearable 
                @keyup.enter="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="出库状态">
              <el-select v-model="searchForm.auditStatus" placeholder="请选择状态" clearable>
                <el-option label="待审核" value="PENDING" />
                <el-option label="已通过" value="PASSED" />
                <el-option label="已驳回" value="REJECTED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="出库日期">
              <el-date-picker
                v-model="searchForm.outStockDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="18">
            <el-form-item label="合同信息">
              <el-input 
                v-model="searchForm.contractInfo" 
                placeholder="请输入合同编号或合同名称" 
                clearable 
                @keyup.enter="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item>
              <el-button type="primary" @click="handleSearch" icon="Search">查询</el-button>
              <el-button @click="handleReset" icon="Refresh">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table 
        :data="tableData" 
        style="width: 100%" 
        v-loading="loading" 
        border 
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="outStockNo" label="出库单号" width="180" align="center" />
        <el-table-column prop="outStockType" label="出库类型" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.outStockType)">
              {{ getTypeText(scope.row.outStockType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contractNo" label="合同编号" width="150" align="center" />
        <el-table-column prop="customerName" label="客户名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="customerAddress" label="客户地址" min-width="300" show-overflow-tooltip />
        <el-table-column prop="outStockDate" label="出库日期" width="120" align="center" />
        <el-table-column prop="totalAmount" label="出库金额" width="120" align="right">
          <template #default="scope">
            <span style="color: #f56c6c; font-weight: bold;">
              {{ formatCurrency(scope.row.totalAmount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审核状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.auditStatus)">
              {{ getStatusText(scope.row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdByName" label="制单人" width="120" align="center" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <div class="operation-buttons">
              <template v-if="scope.row.auditStatus === 'PENDING' && canAudit">
                <el-button 
                  link 
                  type="primary" 
                  size="small" 
                  @click="handleAudit(scope.row)"
                  icon="Check"
                >
                  审核
                </el-button>
              </template>
              <template v-else-if="scope.row.auditStatus !== 'PENDING'">
                <el-button 
                  link 
                  type="success" 
                  size="small" 
                  disabled
                >
                  已审核
                </el-button>
              </template>
              <el-button 
                v-if="canUpdate"
                link 
                type="primary" 
                size="small" 
                @click="handleEdit(scope.row)"
                icon="Edit"
                :disabled="scope.row.auditStatus !== 'PENDING'"
              >
                修改
              </el-button>
              <el-button 
                v-if="canDelete && scope.row.auditStatus === 'PENDING'"
                link 
                type="danger" 
                size="small" 
                @click="handleDelete(scope.row)"
                icon="Delete"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOutboundList } from '@/api/outbound'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

// 确保用户信息已加载
onMounted(async () => {
  if (!userStore.userInfo && userStore.getInfo) {
    await userStore.getInfo()
  }
})

// 判断是否为管理员
const isAdmin = computed(() => {
  const ui: any = userStore.userInfo
  const type = ui?.userType
  const username = ui?.username
  const roleId = ui?.roleId
  const roleName = ui?.roleName
  return type === 'admin'
    || username === 'admin'
    || roleId === 1
    || String(roleName || '').includes('管理员')
})

// 判断是否有指定权限码
const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo
  if (!ui) return false
  const permissionCodes = ui?.permissionCodes || []
  return isAdmin.value || permissionCodes.includes(permCode)
}

// 权限控制
const canCreate = computed(() => hasPermission('OM:create'))
const canUpdate = computed(() => hasPermission('OM:update'))
const canDelete = computed(() => hasPermission('OM:delete'))
const canAudit = computed(() => hasPermission('OM:audit') || isAdmin.value)

// 调试信息
const debugInfo = computed(() => {
  const ui: any = userStore.userInfo
  return {
    userType: ui?.userType,
    roleName: ui?.roleName,
    permissionCodes: ui?.permissionCodes || [],
    isAdmin: isAdmin.value,
    canCreate: canCreate.value,
    canUpdate: canUpdate.value,
    canDelete: canDelete.value,
    canAudit: canAudit.value
  }
})

// 类型定义
interface OutboundRecord {
  id: number
  outStockNo: string
  customerName: string
  customerAddress: string
  outStockDate: string
  totalAmount: number
  auditStatus: string
  createdByName: string
  outStockType: string
}

interface OutboundQuery {
  current?: number
  size?: number
  outStockNo?: string
  customerName?: string
  auditStatus?: string
  outStockDateStart?: string
  outStockDateEnd?: string
  [key: string]: any
}

// 搜索表单
const searchForm = reactive({
  outStockNo: '',
  customerName: '',
  auditStatus: '',
  outStockDateRange: [],
  contractInfo: ''
})

// 分页配置
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 表格数据
const tableData = ref<OutboundRecord[]>([])
const loading = ref(false)
const selectedRows = ref<OutboundRecord[]>([])

// 获取列表数据
const fetchData = async () => {
  loading.value = true
  try {
    const params: OutboundQuery = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    
    // 处理日期范围
    if (searchForm.outStockDateRange && searchForm.outStockDateRange.length === 2) {
      params.outStockDateStart = searchForm.outStockDateRange[0]
      params.outStockDateEnd = searchForm.outStockDateRange[1]
    }
    
    const res = await getOutboundList(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

// 重置
const handleReset = () => {
  searchForm.outStockNo = ''
  searchForm.customerName = ''
  searchForm.auditStatus = ''
  searchForm.outStockDateRange = []
  searchForm.contractInfo = ''
  handleSearch()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.size = size
  fetchData()
}

// 当前页改变
const handleCurrentChange = (current: number) => {
  pagination.current = current
  fetchData()
}

// 表格选择
const handleSelectionChange = (rows: OutboundRecord[]) => {
  selectedRows.value = rows
}

// 获取类型标签
const getTypeTag = (type: string) => {
  switch (type) {
    case 'SALES': return 'success'
    case 'PURCHASE_RETURN': return 'warning'
    case 'OTHER': return 'info'
    default: return 'info'
  }
}

// 获取类型文本
const getTypeText = (type: string) => {
  switch (type) {
    case 'SALES': return '销售出库'
    case 'PURCHASE_RETURN': return '采购退货'
    case 'OTHER': return '其他出库'
    default: return type
  }
}

// 格式化货币
const formatCurrency = (value: number | undefined) => {
  if (value === undefined || value === null) return '¥0.00'
  return `¥${value.toFixed(2)}`
}

// 获取状态标签类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'PENDING': return 'warning'
    case 'PASSED': return 'success'
    case 'REJECTED': return 'danger'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'PENDING': return '待审核'
    case 'PASSED': return '已通过'
    case 'REJECTED': return '已驳回'
    default: return status
  }
}

// 新建出库单
const handleCreate = () => {
  router.push('/scm/outbound/create')
}

// 编辑出库单
const handleEdit = (row: OutboundRecord) => {
  router.push(`/scm/outbound/edit/${row.id}`)
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 审核
const handleAudit = (_row: OutboundRecord) => {
  ElMessage.info('审核功能开发中')
}

// 删除
const handleDelete = (_row: OutboundRecord) => {
  ElMessage.info('删除功能开发中')
}

// 初始化
fetchData()
</script>