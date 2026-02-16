<template>
  <el-container class="layout-root">
    <el-aside width="200px" class="layout-aside">
      <div class="sidebar-title" @click="gotoHome" style="cursor:pointer;">{{ systemName }}</div>
      <el-menu
        active-text-color="#ffd04b"
        background-color="#545c64"
        class="el-menu-vertical-demo"
        :default-active="$route.path"
        text-color="#fff"
        router
        style="flex:1; overflow:auto;"
      >
        <!-- 一、个人中心 -->
        <el-menu-item index="/system/profile">个人中心</el-menu-item>

        <!-- 二、系统设置 -->
        <el-sub-menu index="/system">
          <template v-slot:title><span>系统设置</span></template>
          <el-menu-item v-if="hasRes('UM')" index="/system/user">用户管理</el-menu-item>
          <el-menu-item v-if="hasRes('RM')" index="/system/role">角色管理</el-menu-item>
          <el-menu-item v-if="hasRes('PM')" index="/system/permission">权限管理</el-menu-item>
          <el-menu-item v-if="hasRes('DM')" index="/system/dept">部门管理</el-menu-item>
          <el-menu-item v-if="hasRes('TM')" index="/system/team">团队管理</el-menu-item>
          <el-menu-item index="/system/notice">通告管理</el-menu-item>
          <el-menu-item v-if="hasRes('NR')" index="/system/code-rule">编号规则</el-menu-item>
          <el-menu-item v-if="hasRes('GS')" index="/system/config">字典设置</el-menu-item>
          <el-menu-item v-if="hasRes('OL')" index="/system/log">操作日志</el-menu-item>
        </el-sub-menu>

        <!-- 三、销售管理 -->
        <el-sub-menu index="/sales">
          <template v-slot:title><span>销售管理</span></template>
          <el-menu-item v-if="hasRes('CM')" index="/crm/customer">客户管理</el-menu-item>
          <el-menu-item v-if="hasRes('CC')" index="/crm/contact">客户联系人</el-menu-item>
          <el-menu-item v-if="hasRes('PF')" index="/crm/follow">项目管理</el-menu-item>
          <el-menu-item v-if="hasRes('SOL')" index="/crm/solution">方案管理</el-menu-item>
          <el-menu-item v-if="hasRes('QM')" index="/crm/quote">报价管理</el-menu-item>
          <el-menu-item v-if="hasRes('CO')" index="/crm/contract">合同订单</el-menu-item>
        </el-sub-menu>

        <!-- 四、财务管理 -->
        <el-sub-menu index="/finance">
          <template v-slot:title><span>财务管理</span></template>
          <el-menu-item v-if="hasRes('ER')" index="/crm/expense">费用报销</el-menu-item>
          <el-menu-item v-if="hasRes('IER')" index="/crm/finance">收支管理</el-menu-item>
          <el-menu-item v-if="hasRes('IM')" index="/crm/invoice">发票管理</el-menu-item>
          <el-menu-item v-if="hasRes('DeM')" index="/crm/finance/deposit">保证金控</el-menu-item>
          <el-menu-item v-if="hasRes('IFM')" index="/crm/finance/intermediary-fee">佣金管理</el-menu-item>
        </el-sub-menu>

        <!-- 五、采购管理 -->
        <el-sub-menu index="/purchase">
          <template v-slot:title><span>采购管理</span></template>
          <el-menu-item v-if="hasRes('SM')" index="/scm/supplier">供方管理</el-menu-item>
          <el-menu-item v-if="hasRes('PA')" index="/scm/purchase/request">商品采购</el-menu-item>
          <el-menu-item v-if="hasRes('PDM')" index="/pdm/product">商品管理</el-menu-item>
          <el-menu-item v-if="hasRes('PCC')" index="/system/cost-calculation">成本核算</el-menu-item>
        </el-sub-menu>

        <!-- 六、仓库管理 -->
        <el-sub-menu index="/warehouse">
          <template v-slot:title><span>仓库管理</span></template>
          <el-sub-menu index="/warehouse/inbound">
            <template v-slot:title><span>入库管理</span></template>
            <el-menu-item v-if="hasRes('WHM')" index="/scm/inbound/purchase-inbound">采购入库</el-menu-item>
            <el-menu-item v-if="hasRes('WHM')" index="/scm/inbound/other-inbound">外转入库</el-menu-item>
            <el-menu-item v-if="hasRes('WHM')" index="/scm/inbound/sales-return">销售退货</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="/warehouse/outbound">
            <template v-slot:title><span>出库管理</span></template>
            <el-menu-item v-if="hasRes('OM')" index="/scm/outbound/sales-outbound">销售出库</el-menu-item>
            <el-menu-item v-if="hasRes('OM')" index="/scm/outbound/purchase-return">采购退货</el-menu-item>
          </el-sub-menu>
          <el-menu-item v-if="hasRes('IOF')" index="/scm/inventory/flow">出入流水</el-menu-item>
          <el-sub-menu index="/warehouse/stock">
            <template v-slot:title><span>库存管理</span></template>
            <el-menu-item v-if="hasRes('WM')" index="/scm/inventory/warehouse-stock">仓库库存</el-menu-item>
            <el-menu-item v-if="hasRes('WM')" index="/scm/inventory/product-stock">商品库存</el-menu-item>
          </el-sub-menu>
          <el-menu-item v-if="hasRes('IC')" index="/scm/inventory/check">库存盘点</el-menu-item>
          <el-menu-item v-if="hasRes('ST')" index="/scm/inventory/query">库存查询</el-menu-item>
        </el-sub-menu>

        <!-- 七、部署维护 -->
        <el-sub-menu index="/ops">
          <template v-slot:title><span>部署维护</span></template>
          <el-sub-menu index="/ops/install">
            <template v-slot:title><span>设备安装</span></template>
            <el-menu-item v-if="hasRes('PI')" index="/asm/install/on-site">现场安装</el-menu-item>
            <el-menu-item v-if="hasRes('PI')" index="/asm/install/remote">远程安装</el-menu-item>
            <el-menu-item v-if="hasRes('PI')" index="/asm/install/delegate">委托安装</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="/ops/repair">
            <template v-slot:title><span>设备维修</span></template>
            <el-menu-item v-if="hasRes('EM')" index="/asm/repair/on-site">现场维修</el-menu-item>
            <el-menu-item v-if="hasRes('EM')" index="/asm/repair/remote">远程维护</el-menu-item>
            <el-menu-item v-if="hasRes('EM')" index="/asm/repair/delegate">委托维修</el-menu-item>
          </el-sub-menu>
        </el-sub-menu>

        <!-- 八、统计报表 -->
        <el-sub-menu index="/reports">
          <template v-slot:title><span>统计报表</span></template>
          <el-sub-menu index="/reports/sales">
            <template v-slot:title><span>销售分析</span></template>
            <el-menu-item v-if="hasRes('SR')" index="/report/sales/order-summary">订单汇总</el-menu-item>
            <el-menu-item v-if="hasRes('SR')" index="/report/sales/delivery">发货汇总</el-menu-item>
            <el-menu-item v-if="hasRes('SR')" index="/report/sales/return">退货汇总</el-menu-item>
            <el-menu-item v-if="hasRes('SR')" index="/report/sales/receivable">应收汇总</el-menu-item>
            <el-menu-item v-if="hasRes('SR')" index="/report/sales/receipt">回款汇总</el-menu-item>
            <el-menu-item v-if="hasRes('SR')" index="/report/sales/profit">毛利分析</el-menu-item>
            <el-menu-item v-if="hasRes('SR')" index="/report/sales/expense">费用汇总</el-menu-item>
            <el-menu-item v-if="hasRes('SR')" index="/report/sales/ranking">销售排行</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="/reports/purchase">
            <template v-slot:title><span>采购分析</span></template>
            <el-menu-item v-if="hasRes('PR')" index="/report/purchase/order-summary">订单汇总</el-menu-item>
            <el-menu-item v-if="hasRes('PR')" index="/report/purchase/inbound">到货汇总</el-menu-item>
            <el-menu-item v-if="hasRes('PR')" index="/report/purchase/return">退货汇总</el-menu-item>
            <el-menu-item v-if="hasRes('PR')" index="/report/purchase/payment">付款汇总</el-menu-item>
            <el-menu-item v-if="hasRes('PR')" index="/report/purchase/supplier-statement">供方对账</el-menu-item>
            <el-menu-item v-if="hasRes('PR')" index="/report/purchase/price-compare">价格对比</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="/reports/inventory">
            <template v-slot:title><span>库存分析</span></template>
            <el-menu-item v-if="hasRes('WM')" index="/report/inventory/in-out-summary">收发存表</el-menu-item>
            <el-menu-item v-if="hasRes('WM')" index="/report/inventory/ledger">库存台账</el-menu-item>
            <el-menu-item v-if="hasRes('IC')" index="/report/inventory/check">盘点汇总</el-menu-item>
            <el-menu-item v-if="hasRes('IOF')" index="/report/inventory/flow-detail">出入汇总</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="/reports/finance">
            <template v-slot:title><span>财务分析</span></template>
            <el-menu-item v-if="hasRes('IER')" index="/report/finance/general-ledger">总账明细</el-menu-item>
            <el-menu-item v-if="hasRes('IER')" index="/report/finance/balance">科目余额</el-menu-item>
            <el-menu-item v-if="hasRes('IER')" index="/report/finance/voucher-summary">凭证汇总</el-menu-item>
            <el-menu-item v-if="hasRes('IER')" index="/report/finance/aging-analysis">收付账龄</el-menu-item>
            <el-menu-item v-if="hasRes('IER')" index="/report/finance/profit-statement">利润分析</el-menu-item>
            <el-menu-item v-if="hasRes('IER')" index="/report/finance/balance-sheet">资产负债</el-menu-item>
            <el-menu-item v-if="hasRes('IER')" index="/report/finance/cash-flow">现金流量</el-menu-item>
            <el-menu-item v-if="hasRes('IER')" index="/report/finance/asset-depreciation">资产折旧</el-menu-item>
            <el-menu-item v-if="hasRes('IER')" index="/report/finance/expense-detail">费用明细</el-menu-item>
            <el-menu-item v-if="hasRes('IER')" index="/report/finance/dept-expense">部门费用</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="/reports/management">
            <template v-slot:title><span>决策分析</span></template>
            <el-menu-item v-if="hasRes('SR')" index="/report/management/daily">经营日报</el-menu-item>
            <el-menu-item v-if="hasRes('SR')" index="/report/management/monthly">经营月报</el-menu-item>
            <el-menu-item v-if="hasRes('SR')" index="/report/management/yearly">经营年报</el-menu-item>
            <el-menu-item v-if="hasRes('SR')" index="/report/management/revenue-cost-profit">收入成利</el-menu-item>
            <el-menu-item v-if="hasRes('IER')" index="/report/management/cash-flow-forecast">现金预测</el-menu-item>
            <el-menu-item v-if="hasRes('WM')" index="/report/management/inventory-turnover">库存周转</el-menu-item>
            <el-menu-item v-if="hasRes('SR')" index="/report/management/delivery-rate">订单交付</el-menu-item>
          </el-sub-menu>
        </el-sub-menu>

      </el-menu>
      <div class="sidebar-footer">© 原邑</div>
    </el-aside>
    <el-container>
      <el-header style="background-color: #fff; border-bottom: 1px solid #dcdfe6; display: flex; justify-content: space-between; align-items: center;">
        <div style="display:flex; align-items:center; gap:12px;">
          <el-badge v-if="isFinanceUser && pendingInvoiceCount > 0" :value="pendingInvoiceCount" class="item">
            <el-button type="warning" link @click="gotoPendingInvoices">待开票</el-button>
          </el-badge>
        </div>
        <el-dropdown @command="handleCommand">
          <span class="el-dropdown-link" style="cursor: pointer; display: flex; align-items: center;">
            {{ displayUserName }}
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
 </template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useUserStore } from '@/store/user'
import { useRouter, useRoute } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import request from '@/utils/request'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const pendingInvoiceCount = ref(0)
const systemName = ref('智汇原邑')
const isFinanceUser = computed(() => {
  const ui:any = userStore.userInfo
  const type = ui?.userType
  const roleName = ui?.roleName
  const roles = ui?.roles || []
  return type === 'finance'
    || String(roleName||'').includes('财务')
    || roles.some((r:any) => String(r.roleName||r.name||'').includes('财务') || String(r.roleKey||'').includes('finance'))
})

// 检查是否有发票管理权限
const hasInvoicePermission = computed(() => {
  const ui:any = userStore.userInfo
  const perms:string[] = ui?.permissionCodes || []
  return perms.includes('IM:list')
})

// 监听路由变化，刷新待开票数量
watch(() => route.path, () => {
  if (isFinanceUser.value && hasInvoicePermission.value) {
    fetchPendingInvoices()
  }
})

const displayUserName = computed<string>(() => {
  const ui:any = userStore.userInfo || {}
  return ui.name || ui.realName || ui.nickname || ui.username || '用户'
})
const hasRes = (res: string) => {
  const ui:any = userStore.userInfo || {}
  const perms:string[] = ui?.permissionCodes || []
  const has = Array.isArray(perms) && perms.some((code:string) => String(code||'').startsWith(res + ':'))
  console.log(`[hasRes] ${res}:`, has, 'perms:', perms?.filter((c:string) => c.startsWith(res + ':')))
  return has
}

const handleCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

if (!userStore.userInfo) {
  userStore.getInfo()
}
const gotoPendingInvoices = () => {
  router.push('/crm/invoice')
}
const gotoHome = () => {
  router.push('/')
}
const fetchPendingInvoices = async () => {
  try {
    const res:any = await request({ url: '/crm/invoice/list', method: 'get', params: { current: 1, size: 1000 } })
    const list = res.records || []
    const ui:any = userStore.userInfo
    const currentUserId = ui?.id
    
    // 根据开票单位筛选当前财务经理负责的待开票发票
    pendingInvoiceCount.value = list.filter((x:any) => {
      // 只统计待开票状态
      if (String(x.invoiceStatus) !== 'WAITING') return false
      
      // 如果没有开票单位信息，显示给所有财务经理
      if (!x.invoiceUnit) return true
      
      // 财务经理与开票单位映射关系
      const unitToFinanceManager: Record<string, number> = {
        '原邑智能科技（上海）有限公司': 2,  // 李珂
        '原邑信息科技（上海）有限公司': 2,  // 李珂
        '安徽维斯顿智能科技有限公司': 34    // 赵曼
      }
      
      // 只显示分配给当前财务经理的待开票发票
      const assignedManagerId = unitToFinanceManager[x.invoiceUnit]
      return assignedManagerId === currentUserId
    }).length
  } catch (e) {
    pendingInvoiceCount.value = 0
  }
}
onMounted(async () => {
  // 强制刷新用户信息以获取最新权限
  await userStore.getInfo()
  if (isFinanceUser.value && hasInvoicePermission.value) {
    await fetchPendingInvoices()
  }
  try {
    const res:any = await request({ url: '/system/config/list', method: 'get', params: { size: 100 } })
    const general = (res.records || res || []).find((item:any) => item.configKey === 'general_settings')
    if (general && general.configValue) {
      const cfg = JSON.parse(general.configValue || '{}')
      systemName.value = cfg.systemName || cfg.siteName || systemName.value
    }
  } catch (e) {}
})
</script>

<style scoped>
.layout-root {
  height: 100vh;
}
.layout-aside {
  background: linear-gradient(180deg, #3a3f47 0%, #2f343b 50%, #2a2f35 100%);
  height: 100vh;
  display: flex;
  flex-direction: column;
}
.el-menu-vertical-demo {
  border-right: none;
  background: transparent;
}
.sidebar-title {
  color: #fff;
  font-weight: 600;
  font-size: 16px;
  padding: 14px 16px;
  text-align: center;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.sidebar-footer {
  color: rgba(255,255,255,0.8);
  font-size: 10px;
  padding: 12px 16px;
  border-top: 1px solid rgba(255,255,255,0.1);
}
.el-menu-vertical-demo :deep(.el-menu-item),
.el-menu-vertical-demo :deep(.el-sub-menu__title) {
  height: 40px;
  line-height: 40px;
  margin: 4px 8px;
  border-radius: 8px;
  padding-left: 12px;
}
.el-menu-vertical-demo :deep(.el-menu-item:hover),
.el-menu-vertical-demo :deep(.el-sub-menu__title:hover) {
  background: rgba(255,255,255,0.08);
}
.el-menu-vertical-demo :deep(.el-menu-item.is-active) {
  background: rgba(255,208,75,0.18);
  color: #ffd04b !important;
}
.el-menu-vertical-demo :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: #ffd04b !important;
}
.el-menu-vertical-demo :deep(.el-menu-item) {
  transition: background 0.2s ease, color 0.2s ease;
}
</style>
