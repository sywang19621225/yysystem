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
        <el-sub-menu index="/system">
          <template v-slot:title><span>系统设置</span></template>
          <el-menu-item index="/system/profile">个人中心</el-menu-item>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
          <el-menu-item index="/system/role">角色管理</el-menu-item>
          <el-menu-item index="/system/team">团队管理</el-menu-item>
          <el-menu-item index="/system/dept">部门管理</el-menu-item>
          <el-menu-item index="/system/log">操作日志</el-menu-item>
          <el-menu-item index="/system/config">通用设置</el-menu-item>
          <el-menu-item index="/system/code-rule">编号规则</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/crm/customer">客户管理</el-menu-item>

        <el-menu-item index="/crm/contact">客户联系人</el-menu-item>

        <el-sub-menu index="/project">
          <template v-slot:title><span>项目管理</span></template>
          <el-menu-item index="/project/list">项目列表</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/scheme">方案管理</el-menu-item>

        <el-sub-menu index="/quote">
          <template v-slot:title><span>报价管理</span></template>
          <el-menu-item index="/crm/quote">报价单</el-menu-item>
          <el-menu-item index="/crm/follow">报价跟进</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/crm/contract">合同订单</el-menu-item>

        <el-sub-menu index="/finance">
          <template v-slot:title><span>财务管理</span></template>
          <el-menu-item index="/crm/finance">收支管理</el-menu-item>
          <el-menu-item index="/crm/finance/deposit">保证金管理</el-menu-item>
          <el-menu-item index="/crm/finance/intermediary-fee">居间费管理</el-menu-item>
          <el-menu-item index="/crm/invoice">发票管理</el-menu-item>
          <el-menu-item index="/crm/expense">费用报销</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/pdm">
          <template v-slot:title><span>商品管理</span></template>
          <el-menu-item index="/pdm/product">商品列表</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/scm">
          <template v-slot:title><span>采购仓储</span></template>
          
          <el-sub-menu index="/scm/inventory-group">
            <template v-slot:title><span>库存管理</span></template>
            <el-menu-item index="/scm/inventory/warehouse-stock">仓库库存</el-menu-item>
            <el-menu-item index="/scm/inventory/product-stock">商品库存</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/scm/purchase-group">
             <template v-slot:title><span>采购管理</span></template>
             <el-menu-item index="/scm/purchase">采购单</el-menu-item>
             <el-menu-item index="/scm/purchase/payment">付款记录</el-menu-item>
             <el-menu-item index="/scm/purchase/invoice">发票管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/scm/inbound-group">
            <template v-slot:title><span>入库管理</span></template>
            <el-menu-item index="/scm/inbound/purchase-inbound">入库单</el-menu-item>
            <el-menu-item index="/scm/inbound/sales-return">销售退货单</el-menu-item>
            <el-menu-item index="/scm/inbound/other-inbound">其他入库单</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/scm/outbound-group">
             <template v-slot:title><span>出库管理</span></template>
             <el-menu-item index="/scm/outbound/sales-outbound">销售出库单</el-menu-item>
             <el-menu-item index="/scm/outbound/purchase-return">采购退货单</el-menu-item>
             <el-menu-item index="/scm/outbound/other-outbound">其他出库单</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/scm/inventory/check">库存盘点</el-menu-item>
          <el-menu-item index="/scm/inventory/flow">出入库流水</el-menu-item>
          <el-menu-item index="/scm/warehouse">仓库管理</el-menu-item>
          <el-menu-item index="/scm/supplier">供应商管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/asm">
          <template v-slot:title><span>售后管理</span></template>
          <el-menu-item index="/asm/maintenance">维保管理</el-menu-item>
          <el-menu-item index="/asm/work-order">维修工单</el-menu-item>
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
            {{ userStore.userInfo?.name || userStore.userInfo?.realName || userStore.userInfo?.nickname || userStore.userInfo?.username || '用户' }}
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

// 监听路由变化，刷新待开票数量
watch(() => route.path, () => {
  if (isFinanceUser.value) {
    fetchPendingInvoices()
  }
})

const handleCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
const gotoHome = () => {
  router.push('/')
}

if (!userStore.userInfo) {
  userStore.getInfo()
}
const gotoPendingInvoices = () => {
  router.push('/crm/invoice')
}
const fetchPendingInvoices = async () => {
  try {
    const res:any = await request({ url: '/crm/invoice/list', method: 'get', params: { current: 1, size: 1000 } })
    const list = res.records || []
    pendingInvoiceCount.value = list.filter((x:any) => String(x.invoiceStatus) === 'WAITING').length
  } catch (e) {
    pendingInvoiceCount.value = 0
  }
}
onMounted(async () => {
  if (!userStore.userInfo && userStore.getInfo) {
    await userStore.getInfo()
  }
  if (isFinanceUser.value) {
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
  background-color: #545c64;
  height: 100vh;
  display: flex;
  flex-direction: column;
}
.el-menu-vertical-demo {
  border-right: none;
}
.sidebar-title {
  color: #fff;
  font-weight: 600;
  font-size: 16px;
  padding: 14px 16px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.sidebar-footer {
  color: rgba(255,255,255,0.8);
  font-size: 12px;
  padding: 12px 16px;
  border-top: 1px solid rgba(255,255,255,0.1);
}
</style>
