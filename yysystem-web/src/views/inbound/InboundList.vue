<template>
  <div class="inbound-list">
    <el-table :data="tableData" style="width: 100%" v-loading="loading">
      <el-table-column prop="inboundNo" label="入库单号" width="180" />
      <el-table-column prop="sourceType" label="来源类型" width="120">
        <template #default="scope">
            {{ scope.row.sourceType === 'PURCHASE' ? '采购入库' : scope.row.sourceType }}
        </template>
      </el-table-column>
      <el-table-column prop="inboundDate" label="入库日期" width="120" />
      <el-table-column prop="auditStatus" label="审核状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.auditStatus === 'PASSED' ? 'success' : (scope.row.auditStatus === 'REJECTED' ? 'danger' : 'warning')">
            {{ scope.row.auditStatus === 'PASSED' ? '已入库' : (scope.row.auditStatus === 'REJECTED' ? '已驳回' : '待入库') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button v-if="canUpdate && scope.row.auditStatus === 'PENDING'" link type="success" @click="handleAudit(scope.row)">确认入库</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getInboundList, auditInbound } from '@/api/inbound'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canUpdate = computed(() => hasPermission('WHM:update'))

const tableData = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getInboundList({ current: currentPage.value, size: pageSize.value })
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleAudit = (row: any) => {
  ElMessageBox.confirm('确认执行入库操作吗？这将增加库存。', '提示', { type: 'warning' }).then(async () => {
    await auditInbound(row.id!, 'PASSED')
    ElMessage.success('入库成功')
    fetchData()
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.inbound-list {
  padding: 20px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
