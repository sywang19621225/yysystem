<template>
  <div class="app-container">
    <h2 class="page-title">报价管理</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="8">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #409EFF;">{{ formatCurrency(summary.salesTotal) }}</div>
            <div style="color: #909399; margin-top: 8px;">销售合计</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #67C23A;">{{ formatCurrency(summary.costTotal) }}</div>
            <div style="color: #909399; margin-top: 8px;">成本合计</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #F56C6C;">{{ formatCurrency(summary.profitTotal) }}</div>
            <div style="color: #909399; margin-top: 8px;">预计毛利</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索栏 -->
    <div class="toolbar">
      <el-input v-model="queryParams.quoteNo" placeholder="报价单号" clearable style="width: 200px; margin-right: 10px;" @keyup.enter="handleQuery" />
      <el-input v-model="queryParams.customerName" placeholder="客户名称" clearable style="width: 200px; margin-right: 10px;" @keyup.enter="handleQuery" />
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
      <el-button v-if="canCreate" type="primary" @click="handleAdd">新增报价单</el-button>
    </div>

    <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe @expand-change="onExpandChange" row-key="id" :expand-row-keys="expandedRowKeys">
      <el-table-column type="expand">
        <template #default="props">
          <div style="padding: 10px;">
            <el-table :data="detailMap[props.row.id] || []" size="small" border style="width: 100%;">
              <el-table-column :width="detailColWidths.productName" prop="productName" label="商品名称">
                <template #header>
                  <div class="header-resize">商品名称<span class="col-resizer" @mousedown="(e:any)=>startResizeDetail('productName', e)"></span></div>
                </template>
              </el-table-column>
              <el-table-column :width="detailColWidths.productSpec" prop="productSpec" label="规格">
                <template #header>
                  <div class="header-resize">规格<span class="col-resizer" @mousedown="(e:any)=>startResizeDetail('productSpec', e)"></span></div>
                </template>
              </el-table-column>
              <el-table-column :width="detailColWidths.salesPrice" prop="salesPrice" label="单价" align="right">
                <template #default="s">
                  {{ formatCurrency(s.row.salesPrice) }}
                </template>
                <template #header>
                  <div class="header-resize">单价<span class="col-resizer" @mousedown="(e:any)=>startResizeDetail('salesPrice', e)"></span></div>
                </template>
              </el-table-column>
              <el-table-column :width="detailColWidths.salesQuantity" prop="salesQuantity" label="数量" align="center">
                <template #header>
                  <div class="header-resize">数量<span class="col-resizer" @mousedown="(e:any)=>startResizeDetail('salesQuantity', e)"></span></div>
                </template>
              </el-table-column>
              <el-table-column :width="detailColWidths.subtotal" label="小计" align="right">
                <template #default="s">
                  {{ formatCurrency((Number(s.row.salesPrice||0) * Number(s.row.salesQuantity||0))) }}
                </template>
                <template #header>
                  <div class="header-resize">小计<span class="col-resizer" @mousedown="(e:any)=>startResizeDetail('subtotal', e)"></span></div>
                </template>
              </el-table-column>
              <el-table-column :width="detailColWidths.productCode" prop="productCode" label="商品编号">
                <template #header>
                  <div class="header-resize">商品编号<span class="col-resizer" @mousedown="(e:any)=>startResizeDetail('productCode', e)"></span></div>
                </template>
              </el-table-column>
              <el-table-column :width="detailColWidths.costPrice" prop="costPrice" label="成本单价" align="right">
                <template #default="s">
                  {{ formatCurrency(s.row.costPrice) }}
                </template>
                <template #header>
                  <div class="header-resize">成本单价<span class="col-resizer" @mousedown="(e:any)=>startResizeDetail('costPrice', e)"></span></div>
                </template>
              </el-table-column>
            </el-table>
            
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="quoteNo" label="报价单号" width="180" align="center" />
      <el-table-column prop="customerName" label="客户名称" width="180" align="center" />
      <el-table-column prop="quoteDate" label="报价日期" width="120" align="center" />
      <el-table-column prop="costTotalAmount" label="成本合计" width="140" align="right">
        <template #default="scope">
          {{ formatCurrency(scope.row.costTotalAmount) }}
        </template>
      </el-table-column>
      <el-table-column prop="channelTotalAmount" label="渠道合计" width="140" align="right">
        <template #default="scope">
          {{ formatCurrency(scope.row.channelTotalAmount) }}
        </template>
      </el-table-column>
      <el-table-column prop="terminalTotalAmount" label="终端合计" width="140" align="right">
        <template #default="scope">
          {{ formatCurrency(scope.row.terminalTotalAmount) }}
        </template>
      </el-table-column>
      <el-table-column prop="quoteAmount" label="总金额" width="150" align="right">
        <template #default="scope">
          {{ formatCurrency(scope.row.quoteAmount) }}
        </template>
      </el-table-column>
      <el-table-column label="毛利" width="140" align="right">
        <template #default="scope">
          {{ formatCurrency((Number(scope.row.quoteAmount||0) - Number(scope.row.costTotalAmount||0))) }}
        </template>
      </el-table-column>
      <el-table-column prop="productTotal" label="商品数" width="100" align="center" />
      <el-table-column prop="quoteStatus" label="状态" width="120" align="center">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.quoteStatus)">
            {{ getStatusText(scope.row.quoteStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="280" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handlePreview(scope.row)">预览</el-button>
          <el-button v-if="canUpdate" link type="warning" @click="handleEdit(scope.row)">修改明细</el-button>
          <el-button v-if="canCreate" link type="success" @click="handleToContract(scope.row)" v-show="scope.row.quoteStatus !== 'CONFIRMED'">转合同</el-button>
          <el-button v-if="canDelete" link type="danger" @click="handleDelete(scope.row)" :disabled="scope.row.quoteStatus === 'CONFIRMED'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container" style="gap:12px;align-items:center;">
      <span>共 {{ total }} 条、每页 {{ pageSize }} 条</span>
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="onMainSizeChange"
        @current-change="fetchData"
      />
    </div>

    <QuoteForm v-model="dialogVisible" :id="currentId" @success="fetchData" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getQuoteList, deleteQuote, getQuoteById, type Quote } from '@/api/quote'
import { createContractFromQuote } from '@/api/contract'
import { ElMessage, ElMessageBox } from 'element-plus'
import QuoteForm from './QuoteForm.vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canCreate = computed(() => hasPermission('QM:create'))
const canUpdate = computed(() => hasPermission('QM:update'))
const canDelete = computed(() => hasPermission('QM:delete'))

const router = useRouter()
const tableData = ref<Quote[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const currentId = ref<number | undefined>(undefined)
const route = useRoute()
const summary = reactive({ costTotal: 0, salesTotal: 0, profitTotal: 0, unconfirmedCount: 0, unconfirmedSalesTotal: 0 })
const detailMap = ref<Record<number, any[]>>({})
const expandedRowKeys = ref<number[]>([])
const detailColWidths = reactive<Record<string, number>>({
  productName: 180,
  productSpec: 160,
  salesPrice: 140,
  salesQuantity: 120,
  subtotal: 140,
  productCode: 160,
  costPrice: 140
})
const resizeDetailState = reactive<{ key: string; startX: number; startWidth: number }>({ key: '', startX: 0, startWidth: 0 })
const startResizeDetail = (key: string, e: MouseEvent) => {
  resizeDetailState.key = key
  resizeDetailState.startX = e.clientX
  resizeDetailState.startWidth = detailColWidths[key]
  document.addEventListener('mousemove', onDetailMouseMove)
  document.addEventListener('mouseup', stopDetailResize)
}
const onDetailMouseMove = (e: MouseEvent) => {
  if (!resizeDetailState.key) return
  const delta = e.clientX - resizeDetailState.startX
  const next = Math.max(60, resizeDetailState.startWidth + delta)
  detailColWidths[resizeDetailState.key] = next
}
const stopDetailResize = () => {
  resizeDetailState.key = ''
  document.removeEventListener('mousemove', onDetailMouseMove)
  document.removeEventListener('mouseup', stopDetailResize)
}

const queryParams = reactive({
  quoteNo: '',
  customerName: ''
})

const formatCurrency = (value: number | undefined) => {
  if (value === undefined || value === null) return '¥0.00'
  return `¥${value.toFixed(2)}`
}

const getStatusType = (status: string | undefined) => {
  if (status === 'CONFIRMED') return 'success'
  if (status === 'WAITING') return 'warning'
  if (status === 'REJECTED') return 'danger'
  if (status === 'DRAFT') return 'info'
  return 'primary'
}

const getStatusText = (status: string | undefined) => {
  if (status === 'CONFIRMED') return '已确认'
  if (status === 'WAITING') return '待确认'
  if (status === 'REJECTED') return '已驳回'
  if (status === 'DRAFT') return '草稿'
  return '未知'
}

const fetchData = async () => {
  loading.value = true
  try {
    const prevExpanded = [...expandedRowKeys.value]
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      ...queryParams
    }
    const res: any = await getQuoteList(params)
    const allRows:any[] = res.records || []
    const byNo = String(queryParams.quoteNo || '').trim()
    const byName = String(queryParams.customerName || '').trim()
    let rows = allRows
    if (byNo) rows = rows.filter(x => String(x.quoteNo || '').includes(byNo))
    if (byName) rows = rows.filter(x => String(x.customerName || '').includes(byName))
    tableData.value = rows
    total.value = res.total ?? rows.length
    await fetchSummary()
    if (prevExpanded.length > 0) {
      const exist = tableData.value.find(x => x.id === prevExpanded[0])
      expandedRowKeys.value = exist ? prevExpanded : []
    }
  } finally {
    loading.value = false
  }
}
const fetchSummary = async () => {
  const rows = tableData.value || []
  const sum = (arr: any[], key: string) => arr.reduce((acc, x) => acc + Number(x?.[key] || 0), 0)
  summary.costTotal = Number(sum(rows, 'costTotalAmount').toFixed(2))
  summary.salesTotal = Number(sum(rows, 'quoteAmount').toFixed(2))
  summary.profitTotal = Number((summary.salesTotal - summary.costTotal).toFixed(2))
  const unconfirmed = rows.filter((x:any) => String(x.quoteStatus) !== 'CONFIRMED')
  summary.unconfirmedCount = unconfirmed.length
  summary.unconfirmedSalesTotal = Number(sum(unconfirmed, 'quoteAmount').toFixed(2))
}
const handleQuery = () => {
  currentPage.value = 1
  fetchData()
}

  const resetQuery = () => {
    queryParams.quoteNo = ''
    queryParams.customerName = ''
    handleQuery()
  }

const handleAdd = () => {
  currentId.value = undefined
  dialogVisible.value = true
}

const handleEdit = (row: Quote) => {
  currentId.value = row.id
  dialogVisible.value = true
}

const handleDelete = (row: Quote) => {
  ElMessageBox.confirm('确认删除该报价单吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteQuote(row.id!)
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleToContract = (row: Quote) => {
  ElMessageBox.confirm('确认将该报价单转为合同吗？', '提示', { type: 'info' }).then(async () => {
    await createContractFromQuote(row.id!)
    ElMessage.success('生成合同成功')
    router.push('/contract')
  })
}

const onExpandChange = async (row: Quote) => {
  if (!row.id) return
  const res:any = await getQuoteById(row.id)
  detailMap.value[row.id] = res.detailList || []
  
}

const handlePreview = async (row: Quote) => {
  if (!row.id) return
  expandedRowKeys.value = [row.id]
  await onExpandChange(row)
}

onMounted(() => {
  fetchData()
  if (String(route.query.create || '') === '1') {
    currentId.value = undefined
    dialogVisible.value = true
  }
})
onUnmounted(() => {
  document.removeEventListener('mousemove', onDetailMouseMove)
  document.removeEventListener('mouseup', stopDetailResize)
})

const onMainSizeChange = (val: number) => {
  const oldSize = pageSize.value
  const oldPage = currentPage.value
  pageSize.value = val
  const startIndex = (oldPage - 1) * oldSize
  currentPage.value = Math.floor(startIndex / val) + 1
  fetchData()
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.summary-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.search-form {
  margin-bottom: -18px; /* Align with button */
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.header-resize {
  position: relative;
  padding-right: 8px;
}
.col-resizer {
  position: absolute;
  right: 0;
  top: 0;
  width: 6px;
  height: 100%;
  cursor: col-resize;
  display: inline-block;
}
</style>
