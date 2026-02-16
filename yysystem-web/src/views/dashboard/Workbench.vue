<template>
  <div class="workbench">
    <!-- 统计卡片区域 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col v-if="hasPermission('QM:list')" :span="4">
        <el-card shadow="hover" class="stat-card" @click="goTodo('quote')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409EFF;">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.todoQuotes }}</div>
              <div class="stat-label">待确认报价</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col v-if="hasPermission('CO:list')" :span="4">
        <el-card shadow="hover" class="stat-card" @click="goTodo('contract')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67C23A;">
              <el-icon><DocumentChecked /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.todoContracts }}</div>
              <div class="stat-label">待审核合同</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col v-if="hasPermission('IM:list')" :span="4">
        <el-card shadow="hover" class="stat-card" @click="goTodo('invoice')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #E6A23C;">
              <el-icon><Ticket /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.todoInvoices }}</div>
              <div class="stat-label">待审核发票</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col v-if="hasPermission('ER:list')" :span="4">
        <el-card shadow="hover" class="stat-card" @click="goTodo('expense')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #F56C6C;">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.todoExpenses }}</div>
              <div class="stat-label">待审核报销</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col v-if="hasPermission('PA:list')" :span="4">
        <el-card shadow="hover" class="stat-card" @click="goTodo('purchase')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #909399;">
              <el-icon><ShoppingCart /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.todoPurchases }}</div>
              <div class="stat-label">待审核采购</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col v-if="hasPermission('WHM:list')" :span="4">
        <el-card shadow="hover" class="stat-card" @click="goTodo('inbound')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #8B5CF6;">
              <el-icon><Box /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.todoInbounds }}</div>
              <div class="stat-label">待审核入库</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col v-if="hasPermission('OM:list')" :span="4">
        <el-card shadow="hover" class="stat-card" @click="goTodo('outbound')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #FF6B6B;">
              <el-icon><Van /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.todoOutbounds }}</div>
              <div class="stat-label">待发货</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col v-if="hasPermission('CO:list')" :span="4">
        <el-card shadow="hover" class="stat-card" @click="goTodo('collection')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #4ECDC4;">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.todoCollections }}</div>
              <div class="stat-label">待回款</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col v-if="hasPermission('QM:list')" :span="4">
        <el-card shadow="hover" class="stat-card" @click="goSalesStats()">
          <div class="stat-content">
            <div class="stat-icon" style="background: #45B7D1;">
              <el-icon><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.monthSalesAmount }}</div>
              <div class="stat-label">本月销售额</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="grid">
      <!-- 快捷入口 -->
      <el-card class="card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>快捷入口</span>
          </div>
        </template>
        <div class="quick-actions">
          <div v-if="hasPermission('CM:create')" class="action-item" @click="goCustomerCreate">
            <div class="action-icon" style="background: #409EFF;">
              <el-icon :size="24"><User /></el-icon>
            </div>
            <div class="action-label">新增客户</div>
          </div>
          <div v-if="hasPermission('QM:create')" class="action-item" @click="openQuoteCreate">
            <div class="action-icon" style="background: #67C23A;">
              <el-icon :size="24"><Document /></el-icon>
            </div>
            <div class="action-label">新增报价单</div>
          </div>
          <div v-if="hasPermission('CO:create')" class="action-item" @click="openContractCreate">
            <div class="action-icon" style="background: #E6A23C;">
              <el-icon :size="24"><DocumentChecked /></el-icon>
            </div>
            <div class="action-label">新增合同</div>
          </div>
          <div v-if="hasPermission('ER:create')" class="action-item" @click="goExpense">
            <div class="action-icon" style="background: #F56C6C;">
              <el-icon :size="24"><Money /></el-icon>
            </div>
            <div class="action-label">费用报销</div>
          </div>
          <div v-if="hasPermission('PA:create')" class="action-item" @click="goPurchase">
            <div class="action-icon" style="background: #909399;">
              <el-icon :size="24"><ShoppingCart /></el-icon>
            </div>
            <div class="action-label">创建采购单</div>
          </div>
          <div v-if="hasPermission('OM:create')" class="action-item" @click="goOutbound">
            <div class="action-icon" style="background: #8B5CF6;">
              <el-icon :size="24"><Box /></el-icon>
            </div>
            <div class="action-label">销售出库单</div>
          </div>
        </div>
      </el-card>

      <!-- 公告 -->
      <el-card class="card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>公告通知</span>
            <el-link type="primary" :underline="false" @click="goNoticeManage">管理</el-link>
          </div>
        </template>
        <el-empty v-if="notices.length===0" description="暂无公告" />
        <ul v-else class="notice-list">
          <li v-for="n in notices" :key="n.id" class="notice-item" @click="showNoticeDetail(n)">
            <el-tag size="small" type="danger" v-if="isNew(n.publishTime)">NEW</el-tag>
            <span class="notice-title">{{ n.title }}</span>
            <span class="notice-time">{{ formatTime(n.publishTime) }}</span>
          </li>
        </ul>
      </el-card>

      <!-- 未读消息 -->
      <el-card class="card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>未读消息</span>
            <el-link type="primary" :underline="false" @click="markAllRead" v-if="messages.length > 0">全部已读</el-link>
          </div>
        </template>
        <el-empty v-if="messages.length===0" description="暂无未读消息" />
        <ul v-else class="message-list">
          <li v-for="m in messages" :key="m.id" class="message-item">
            <el-icon><Bell /></el-icon>
            <span class="message-content">{{ m.title || m.content || '' }}</span>
            <span class="message-time">{{ formatTime(m.createTime) }}</span>
          </li>
        </ul>
      </el-card>
    </div>

    <!-- 统计图表区域 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>销售统计分析</span>
              <el-radio-group v-model="selectedYear" size="small" @change="onYearChange">
                <el-radio-button v-for="year in availableYears" :key="year" :label="year">{{ year }}年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="salesChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>回款统计分析</span>
              <el-radio-group v-model="collectionYear" size="small" @change="onCollectionYearChange">
                <el-radio-button v-for="year in availableYears" :key="year" :label="year">{{ year }}年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="collectionChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <div class="grid">
      <!-- 最新客户 -->
      <el-card class="card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>最新新增客户</span>
            <el-link type="primary" :underline="false" @click="goCustomerList">查看更多</el-link>
          </div>
        </template>
        <el-table :data="latestCustomers" size="small" border>
          <el-table-column prop="customerName" label="客户" min-width="140" show-overflow-tooltip />
          <el-table-column prop="linkman" label="联系人" width="100" />
          <el-table-column prop="phone" label="联系电话" width="120" />
          <el-table-column prop="createTime" label="创建时间" width="140" />
        </el-table>
      </el-card>

      <!-- 最新报价 -->
      <el-card class="card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>最新新增报价单</span>
            <el-link type="primary" :underline="false" @click="goQuoteList">查看更多</el-link>
          </div>
        </template>
        <el-table :data="latestQuotes" size="small" border>
          <el-table-column prop="quoteNo" label="报价单号" width="130" />
          <el-table-column prop="customerName" label="客户" min-width="120" show-overflow-tooltip />
          <el-table-column prop="quoteAmount" label="报价金额" width="100" align="right">
            <template #default="scope">{{ formatCurrency(scope.row.quoteAmount) }}</template>
          </el-table-column>
          <el-table-column prop="quoteDate" label="日期" width="100" />
        </el-table>
      </el-card>

      <!-- 最新合同 -->
      <el-card class="card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>最新新增合同</span>
            <el-link type="primary" :underline="false" @click="goContractList">查看更多</el-link>
          </div>
        </template>
        <el-table :data="latestContracts" size="small" border>
          <el-table-column prop="contractNo" label="合同编号" width="130" />
          <el-table-column prop="customerName" label="客户" min-width="120" show-overflow-tooltip />
          <el-table-column prop="contractAmount" label="合同金额" width="100" align="right">
            <template #default="scope">{{ formatCurrency(scope.row.contractAmount) }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="140" />
        </el-table>
      </el-card>
    </div>

    <!-- 公告详情弹窗 -->
    <el-dialog v-model="noticeDialogVisible" title="公告详情" width="600px">
      <div v-if="currentNotice">
        <h3 style="margin-bottom: 16px;">{{ currentNotice.title }}</h3>
        <div style="color: #909399; margin-bottom: 16px;">
          发布时间：{{ currentNotice.publishTime }}
        </div>
        <div style="line-height: 1.8; white-space: pre-wrap;">{{ currentNotice.content }}</div>
      </div>
    </el-dialog>

    <QuoteForm v-model="quoteDialogVisible" @success="fetchLatest" />
    <ContractForm v-model="contractDialogVisible" @success="fetchLatest" />
  </div>
</template>

  <script setup lang="ts">
  import { ref, reactive, onMounted, nextTick, computed } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import request from '@/utils/request'
  import QuoteForm from '@/views/quote/QuoteForm.vue'
  import ContractForm from '@/views/contract/ContractForm.vue'
  import { User, Document, DocumentChecked, Money, ShoppingCart, Box, Bell, Ticket, Van, DataLine } from '@element-plus/icons-vue'
  import * as echarts from 'echarts'
  import { useUserStore } from '@/store/user'
  
  const router = useRouter()
  const userStore = useUserStore()
  
  // 权限检查函数
  const hasPermission = (permCode: string) => {
    const ui: any = userStore.userInfo || {}
    const perms: string[] = ui?.permissionCodes || []
    return perms.includes(permCode)
  }
  const quoteDialogVisible = ref(false)
  const contractDialogVisible = ref(false)
  const noticeDialogVisible = ref(false)
  const currentNotice = ref<any>(null)
  const messages = ref<any[]>([])
  const notices = ref<any[]>([])
  const latestCustomers = ref<any[]>([])
  const latestQuotes = ref<any[]>([])
  const latestContracts = ref<any[]>([])
  const summary = reactive({ todoQuotes: 0, todoContracts: 0, todoInvoices: 0, todoExpenses: 0, todoPurchases: 0, todoInbounds: 0, todoOutbounds: 0, todoCollections: 0, monthSalesAmount: 0 })
  
  // 图表相关
  const salesChartRef = ref<HTMLElement | null>(null)
  let salesChart: echarts.ECharts | null = null
  
  // 销售统计图表数据
  const selectedYear = ref<number>(new Date().getFullYear())
  const availableYears = ref<number[]>([])
  const salesDataByYear = ref<Record<number, { months: string[], sales: number[], collections: number[] }>>({})
  
  // 回款统计图表数据
  const collectionYear = ref<number>(new Date().getFullYear())
  const collectionDataByYear = ref<Record<number, { months: string[], planned: number[], actual: number[] }>>({})
  
  // 生成年份选项（从2016年到当前年份）
  const generateYearOptions = () => {
    const currentYear = new Date().getFullYear()
    const years: number[] = []
    for (let year = 2016; year <= currentYear; year++) {
      years.push(year)
    }
    availableYears.value = years
    if (!years.includes(selectedYear.value)) {
      selectedYear.value = currentYear
    }
    if (!years.includes(collectionYear.value)) {
      collectionYear.value = currentYear
    }
  }
  
  // 年份切换
  const onYearChange = () => {
    updateSalesChart()
  }
  
  // 回款年份切换
  const onCollectionYearChange = () => {
    updateCollectionChart()
  }
  
  // 更新销售统计图表
  const updateSalesChart = () => {
    if (!salesChartRef.value) return
    
    const yearData = salesDataByYear.value[selectedYear.value] || {
      months: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      sales: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
      collections: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    }
    
    if (!salesChart) {
      salesChart = echarts.init(salesChartRef.value)
    }
    
    const option = {
      tooltip: { 
        trigger: 'axis',
        formatter: (params: any[]) => {
          let result = params[0].name + '<br/>'
          params.forEach((param: any) => {
            const value = Number(param.value || 0)
            let valueStr = ''
            if (value >= 10000) {
              valueStr = (value / 10000).toFixed(2) + '万'
            } else {
              valueStr = value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
            }
            result += param.marker + param.seriesName + ': ¥' + valueStr + '<br/>'
          })
          return result
        }
      },
      legend: { data: ['销售额', '回款额'], bottom: 0 },
      grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
      xAxis: {
        type: 'category',
        data: yearData.months
      },
      yAxis: { 
        type: 'value', 
        axisLabel: { 
          formatter: (value: number) => {
            if (value >= 10000) {
              return (value / 10000).toFixed(0) + '万'
            }
            return value
          }
        }
      },
      series: [
        {
          name: '销售额',
          type: 'bar',
          data: yearData.sales,
          itemStyle: { color: '#409EFF' },
          label: {
            show: true,
            position: 'top',
            formatter: (params: any) => {
              if (params.value > 0) {
                return params.value >= 10000 ? (params.value / 10000).toFixed(1) + '万' : params.value
              }
              return ''
            }
          }
        },
        {
          name: '回款额',
          type: 'bar',
          data: yearData.collections,
          itemStyle: { color: '#67C23A' },
          label: {
            show: true,
            position: 'top',
            formatter: (params: any) => {
              if (params.value > 0) {
                return params.value >= 10000 ? (params.value / 10000).toFixed(1) + '万' : params.value
              }
              return ''
            }
          }
        }
      ]
    }
    
    salesChart.setOption(option, true)
  }
  
  // 回款统计图表
  const collectionChartRef = ref<HTMLElement | null>(null)
  let collectionChart: echarts.ECharts | null = null
  
  // 更新回款统计图表
  const updateCollectionChart = () => {
    if (!collectionChartRef.value) return
    
    const yearData = collectionDataByYear.value[collectionYear.value] || {
      months: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      planned: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
      actual: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    }
    
    if (!collectionChart) {
      collectionChart = echarts.init(collectionChartRef.value)
    }
    
    const option = {
      tooltip: { 
        trigger: 'axis',
        formatter: (params: any[]) => {
          let result = params[0].name + '<br/>'
          params.forEach((param: any) => {
            const value = Number(param.value || 0)
            let valueStr = ''
            if (value >= 10000) {
              valueStr = (value / 10000).toFixed(2) + '万'
            } else {
              valueStr = value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
            }
            result += param.marker + param.seriesName + ': ¥' + valueStr + '<br/>'
          })
          return result
        }
      },
      legend: { data: ['计划回款', '实际回款'], bottom: 0 },
      grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
      xAxis: {
        type: 'category',
        data: yearData.months
      },
      yAxis: { 
        type: 'value', 
        axisLabel: { 
          formatter: (value: number) => {
            if (value >= 10000) {
              return (value / 10000).toFixed(0) + '万'
            }
            return value
          }
        }
      },
      series: [
        {
          name: '计划回款',
          type: 'bar',
          data: yearData.planned,
          itemStyle: { color: '#E6A23C' },
          label: {
            show: true,
            position: 'top',
            formatter: (params: any) => {
              if (params.value > 0) {
                return params.value >= 10000 ? (params.value / 10000).toFixed(1) + '万' : params.value
              }
              return ''
            }
          }
        },
        {
          name: '实际回款',
          type: 'bar',
          data: yearData.actual,
          itemStyle: { color: '#67C23A' },
          label: {
            show: true,
            position: 'top',
            formatter: (params: any) => {
              if (params.value > 0) {
                return params.value >= 10000 ? (params.value / 10000).toFixed(1) + '万' : params.value
              }
              return ''
            }
          }
        }
      ]
    }
    
    collectionChart.setOption(option, true)
  }
  
  const formatCurrency = (value: number | undefined) => {
    const n = Number(value || 0)
    return '¥' + n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
  }
  
  const formatAmount = (value: number | undefined) => {
    const n = Number(value || 0)
    if (n >= 10000) {
      return `${(n / 10000).toFixed(2)}万`
    }
    return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
  }
  
  const formatTime = (time: string) => {
    if (!time) return ''
    return time.substring(0, 10)
  }
  
  const isNew = (time: string) => {
    if (!time) return false
    const publishDate = new Date(time)
    const now = new Date()
    const diffDays = (now.getTime() - publishDate.getTime()) / (1000 * 60 * 60 * 24)
    return diffDays <= 3
  }
  
  const goCustomerCreate = () => {
    router.push({ path: '/crm/customer', query: { create: 1 } })
  }
  const openQuoteCreate = () => {
    quoteDialogVisible.value = true
  }
  const openContractCreate = () => {
    contractDialogVisible.value = true
  }
  const goExpense = () => {
    router.push('/crm/expense')
  }
  const goPurchase = () => {
    router.push('/scm/purchase')
  }
  const goOutbound = () => {
    router.push('/scm/outbound/sales-outbound')
  }
  
  // 待办跳转
  const goTodo = (type: string) => {
    const routes: Record<string, string> = {
      quote: '/crm/quote',
      contract: '/contract/list',
      invoice: '/crm/invoice',
      expense: '/crm/expense',
      purchase: '/scm/purchase',
      inbound: '/scm/inbound/purchase-inbound',
      outbound: '/scm/outbound/sales-outbound',
      collection: '/crm/finance'
    }
    router.push(routes[type] || '/')
  }

  // 销售统计跳转
  const goSalesStats = () => {
    router.push('/report/sales')
  }
  
  // 列表跳转
  const goCustomerList = () => router.push('/crm/customer')
  const goQuoteList = () => router.push('/crm/quote')
  const goContractList = () => router.push('/contract/list')
  const goNoticeManage = () => router.push('/system/notice')
  
  // 显示公告详情
  const showNoticeDetail = (notice: any) => {
    currentNotice.value = notice
    noticeDialogVisible.value = true
  }
  
  // 标记全部已读
  const markAllRead = async () => {
    try {
      await request({ url: '/system/message/read-all', method: 'post' })
      ElMessage.success('已全部标记为已读')
      fetchMessages()
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  
  const fetchMessages = async () => {
    try {
      const res:any = await request({ url: '/system/message/unread', method: 'get', params: { size: 10 } })
      messages.value = res.records || res || []
    } catch {
      messages.value = []
    }
  }
  const fetchNotices = async () => {
    try {
      const res:any = await request({ url: '/system/notice/list', method: 'get', params: { size: 10 } })
      notices.value = res.records || res || []
    } catch {
      notices.value = []
    }
  }
  const fetchTodo = async () => {
    try {
      const quotes:any = await request({ url: '/crm/quote/list', method: 'get', params: { size: 100 } })
      const qrows = quotes.records || quotes || []
      summary.todoQuotes = (qrows || []).filter((x:any) => String(x.quoteStatus) !== 'CONFIRMED').length
    } catch {}
    try {
      const contracts:any = await request({ url: '/crm/contract/list', method: 'get', params: { size: 100 } })
      const crows = contracts.records || contracts || []
      summary.todoContracts = (crows || []).filter((x:any) => String(x.auditStatus) === 'PENDING').length
    } catch {}
    // 检查发票管理权限
    if (hasPermission('IM:list')) {
      try {
        const invoices:any = await request({ url: '/crm/invoice/list', method: 'get', params: { size: 100 } })
        const irows = invoices.records || invoices || []
        summary.todoInvoices = (irows || []).filter((x:any) => String(x.invoiceStatus) === 'WAITING').length
      } catch {}
    }
    try {
      const expenses:any = await request({ url: '/crm/expense/list', method: 'get', params: { size: 100 } })
      const erows = expenses.records || expenses || []
      summary.todoExpenses = (erows || []).filter((x:any) => String(x.auditStatus) === 'PENDING').length
    } catch {}
    // 检查采购管理权限
    if (hasPermission('PA:list')) {
      try {
        const purchases:any = await request({ url: '/scm/purchase-request/list', method: 'get', params: { size: 100 } })
        const prows = purchases.records || purchases || []
        summary.todoPurchases = (prows || []).filter((x:any) => {
          const audit = String(x.auditStatus || '')
          const status = String(x.status || '')
          return audit === 'PENDING' || status === 'PENDING'
        }).length
      } catch {}
    }
    // 检查入库管理权限
    if (hasPermission('WHM:list')) {
      try {
        const inbounds:any = await request({ url: '/scm/inbound/list', method: 'get', params: { size: 100 } })
        const irows = inbounds.records || inbounds || []
        summary.todoInbounds = (irows || []).filter((x:any) => String(x.auditStatus) === 'PENDING').length
      } catch {}
    }
  }
  
  const fetchLatest = async () => {
    try {
      const res:any = await request({ url: '/crm/customer/list', method: 'get', params: { size: 5, current: 1 } })
      latestCustomers.value = res.records || res || []
    } catch {}
    try {
      const res:any = await request({ url: '/crm/quote/list', method: 'get', params: { size: 5, current: 1 } })
      latestQuotes.value = res.records || res || []
    } catch {}
    try {
      const res:any = await request({ url: '/crm/contract/list', method: 'get', params: { size: 5, current: 1 } })
      latestContracts.value = res.records || res || []
    } catch {}
  }

  // 处理销售数据，按年份和月份分组（按照合同签订时间 signingDate 统计）
  const processSalesData = (contracts: any[]) => {
    const dataByYear: Record<number, { months: string[], sales: number[], collections: number[] }> = {}
    
    // 初始化所有年份的数据
    availableYears.value.forEach(year => {
      dataByYear[year] = {
        months: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        sales: new Array(12).fill(0),
        collections: new Array(12).fill(0)
      }
    })
    
    contracts.forEach((contract: any) => {
      // 按照合同签订时间 signingDate 统计
      const signDate = contract.signingDate || contract.signDate || contract.contractDate
      if (signDate) {
        const date = new Date(signDate)
        const year = date.getFullYear()
        const month = date.getMonth() // 0-11
        
        if (dataByYear[year] && month >= 0 && month < 12) {
          // 累加销售额（合同金额）
          const amount = Number(contract.contractAmount || 0)
          dataByYear[year].sales[month] += amount
          
          // 累加回款额（已回款金额）
          const received = Number(contract.receivedAmount || contract.totalCollection || 0)
          dataByYear[year].collections[month] += received
        }
      }
    })
    
    return dataByYear
  }
  
  // 处理回款数据，按年份和月份分组（按照回款时间统计）
  const processCollectionData = (contracts: any[]) => {
    const dataByYear: Record<number, { months: string[], planned: number[], actual: number[] }> = {}
    
    // 初始化所有年份的数据
    availableYears.value.forEach(year => {
      dataByYear[year] = {
        months: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        planned: new Array(12).fill(0),
        actual: new Array(12).fill(0)
      }
    })
    
    // 从合同中统计计划回款（合同金额）和实际回款（已回款金额）
    contracts.forEach((contract: any) => {
      // 按照合同签订时间统计计划回款
      const signDate = contract.signingDate || contract.signDate || contract.contractDate
      if (signDate) {
        const date = new Date(signDate)
        const year = date.getFullYear()
        const month = date.getMonth() // 0-11
        
        if (dataByYear[year] && month >= 0 && month < 12) {
          // 计划回款（合同金额）
          const amount = Number(contract.contractAmount || 0)
          dataByYear[year].planned[month] += amount
          
          // 实际回款（已回款金额）
          const received = Number(contract.receivedAmount || contract.totalCollection || 0)
          dataByYear[year].actual[month] += received
        }
      }
    })
    
    return dataByYear
  }

  // 获取图表数据
  const fetchChartData = async () => {
    // 生成年份选项
    generateYearOptions()
    
    try {
      // 获取所有合同数据
      const res: any = await request({ 
        url: '/crm/contract/list', 
        method: 'get', 
        params: { size: 10000, current: 1 } 
      })
      const contracts = res.records || res || []
      
      // 处理数据按年份分组
      salesDataByYear.value = processSalesData(contracts)
      collectionDataByYear.value = processCollectionData(contracts)
      
      // 更新图表
      await nextTick()
      updateSalesChart()
      updateCollectionChart()
    } catch (error) {
      console.error('获取图表数据失败:', error)
      // 使用空数据初始化
      await nextTick()
      updateSalesChart()
      updateCollectionChart()
    }
  }

  // 窗口大小改变时重新调整图表
  const handleResize = () => {
    salesChart?.resize()
    collectionChart?.resize()
  }

  onMounted(() => {
    fetchMessages()
    fetchNotices()
    fetchTodo()
    fetchLatest()
    fetchChartData()
    window.addEventListener('resize', handleResize)
  })
  </script>
  
  <style scoped>
  .workbench { padding: 20px; }
  
  /* 统计卡片样式 */
  .stat-card { cursor: pointer; transition: all 0.3s; }
  .stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
  .stat-content { display: flex; align-items: center; gap: 12px; }
  .stat-icon { 
    width: 48px; height: 48px; border-radius: 8px; 
    display: flex; align-items: center; justify-content: center;
    color: white; font-size: 24px;
  }
  .stat-info { flex: 1; }
  .stat-value { font-size: 24px; font-weight: bold; color: #303133; }
  .stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
  
  /* 卡片头部样式 */
  .card-header { display: flex; justify-content: space-between; align-items: center; }

  /* 图表卡片样式 */
  .chart-card { height: 380px; }
  .chart-card :deep(.el-card__body) { padding: 10px; height: calc(100% - 50px); }
  
  /* 快捷入口样式 */
  .quick-actions { 
    display: flex; 
    flex-wrap: wrap; 
    gap: 16px; 
    justify-content: space-around;
    padding: 10px 0;
  }
  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    transition: all 0.3s;
    padding: 10px;
    border-radius: 8px;
  }
  .action-item:hover {
    background: #f5f7fa;
    transform: translateY(-2px);
  }
  .action-icon {
    width: 64px;
    height: 64px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    box-shadow: 0 2px 8px rgba(0,0,0,0.15);
    transition: all 0.3s;
  }
  .action-item:hover .action-icon {
    box-shadow: 0 4px 12px rgba(0,0,0,0.2);
    transform: scale(1.05);
  }
  .action-label {
    font-size: 14px;
    color: #606266;
    font-weight: 500;
  }
  
  /* 公告列表样式 */
  .notice-list { list-style: none; padding: 0; margin: 0; }
  .notice-item { 
    display: flex; align-items: center; gap: 8px; 
    padding: 10px 0; border-bottom: 1px solid #ebeef5;
    cursor: pointer; transition: background 0.2s;
  }
  .notice-item:hover { background: #f5f7fa; }
  .notice-item:last-child { border-bottom: none; }
  .notice-title { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .notice-time { color: #909399; font-size: 12px; }
  
  /* 消息列表样式 */
  .message-list { list-style: none; padding: 0; margin: 0; }
  .message-item { 
    display: flex; align-items: center; gap: 8px; 
    padding: 10px 0; border-bottom: 1px solid #ebeef5;
  }
  .message-item:last-child { border-bottom: none; }
  .message-content { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .message-time { color: #909399; font-size: 12px; }
  
  /* 网格布局 */
  .grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 12px; }
  .card { min-height: 180px; }
  
  @media (max-width: 1200px) {
    .grid { grid-template-columns: repeat(2, 1fr); }
  }
  @media (max-width: 768px) {
    .grid { grid-template-columns: 1fr; }
  }
  </style>
