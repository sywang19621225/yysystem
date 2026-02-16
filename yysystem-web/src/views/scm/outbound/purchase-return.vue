<template>
  <div class="app-container">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>采购退货单</h2>
      <div class="header-actions">
        <el-button v-if="canCreate" type="primary" @click="handleCreate" icon="Plus">新建退货单</el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="退货单号">
              <el-input v-model="searchForm.outStockNo" placeholder="请输入退货单号" clearable @keyup.enter="handleSearch" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="供应商">
              <el-input v-model="searchForm.supplierName" placeholder="请输入供应商" clearable @keyup.enter="handleSearch" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="审核状态">
              <el-select v-model="searchForm.auditStatus" placeholder="请选择状态" clearable>
                <el-option label="待审核" value="PENDING" />
                <el-option label="已通过" value="PASSED" />
                <el-option label="已驳回" value="REJECTED" />
              </el-select>
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
      <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
        <el-table-column type="index" width="50" align="center" />
        <el-table-column prop="outStockNo" label="退货单号" width="180" align="center" />
        <el-table-column prop="purchaseContractNo" label="采购申请号" width="150" align="center" />
        <el-table-column prop="supplierName" label="供应商" min-width="150" />
        <el-table-column prop="outStockDate" label="退货日期" width="120" align="center" />
        <el-table-column prop="totalSales" label="退货金额" width="120" align="right">
          <template #default="scope">
            <span style="color: #f56c6c; font-weight: bold;">{{ formatCurrency(scope.row.totalSales) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审核状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.auditStatus)">{{ getStatusText(scope.row.auditStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="scope">
            <el-button v-if="scope.row.auditStatus === 'PENDING' && canAudit" link type="primary" size="small" @click="handleAudit(scope.row)" icon="Check">审核</el-button>
            <el-button v-else-if="scope.row.auditStatus === 'PASSED' && canAudit" link type="warning" size="small" @click="handleUnlock(scope.row)" icon="Unlock">解锁</el-button>
            <el-button v-else link type="info" size="small" disabled>已审核</el-button>
            <el-button link type="primary" size="small" @click="handleDetail(scope.row)" icon="View">详情</el-button>
            <el-button v-if="scope.row.auditStatus === 'PENDING' && canDelete" link type="danger" size="small" @click="handleDelete(scope.row)" icon="Delete">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditDialogVisible" title="审核采购退货单" width="500px">
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.status">
            <el-radio label="PASSED">通过</el-radio>
            <el-radio label="REJECTED">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见" v-if="auditForm.status === 'REJECTED'">
          <el-input v-model="auditForm.detail" type="textarea" :rows="3" placeholder="请输入驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAudit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOutboundList, auditOutbound, deleteOutbound, unlockOutbound } from '@/api/outbound'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

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
const canDelete = computed(() => hasPermission('OM:delete'))
const canAudit = computed(() => hasPermission('OM:audit') || isAdmin.value)

// 搜索表单
const searchForm = reactive({
  outStockNo: '',
  supplierName: '',
  auditStatus: ''
})

// 分页配置
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 表格数据
const tableData = ref<any[]>([])
const loading = ref(false)

// 审核对话框
const auditDialogVisible = ref(false)
const auditForm = reactive({
  id: 0,
  status: 'PASSED',
  detail: ''
})

// 获取列表数据
const fetchData = async () => {
  loading.value = true
  try {
    const params: any = {
      current: pagination.current,
      size: pagination.size,
      outStockType: 'PURCHASE_RETURN',
      ...searchForm
    }
    const res: any = await getOutboundList(params)
    tableData.value = res.records || []
    pagination.total = res.total || 0
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
  searchForm.supplierName = ''
  searchForm.auditStatus = ''
  handleSearch()
}

// 分页
const handleSizeChange = (size: number) => {
  pagination.size = size
  fetchData()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  fetchData()
}

// 格式化货币
const formatCurrency = (value: number | undefined) => {
  if (value === undefined || value === null) return '¥0.00'
  return `¥${Number(value).toFixed(2)}`
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

// 新建退货单
const handleCreate = () => {
  router.push('/scm/outbound/purchase-return/create')
}

// 审核
const handleAudit = (row: any) => {
  auditForm.id = row.id
  auditForm.status = 'PASSED'
  auditForm.detail = ''
  auditDialogVisible.value = true
}

// 确认审核
const confirmAudit = async () => {
  try {
    await auditOutbound(auditForm.id, auditForm.status, auditForm.detail)
    ElMessage.success('审核成功')
    auditDialogVisible.value = false
    fetchData()
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

// 详情/编辑
const handleDetail = (row: any) => {
  router.push(`/scm/outbound/purchase-return/edit/${row.id}`)
}

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该采购退货单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteOutbound(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 解锁
const handleUnlock = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      '解锁后将恢复库存数量，并将单据状态改为待审核，确定要解锁吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await unlockOutbound(row.id)
    ElMessage.success('解锁成功，库存已恢复，现在可以删除该单据')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('解锁失败')
    }
  }
}

// 初始化
fetchData()
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
