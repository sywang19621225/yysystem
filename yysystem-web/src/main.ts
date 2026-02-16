import { createApp } from 'vue'
import './style.css'
import './styles/attachment-fix.css'
import './styles/element-theme.css'  // Element Plus 自定义主题
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory, type RouteLocationNormalized, type NavigationGuardNext } from 'vue-router'
import { useUserStore } from './store/user'

import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)
dayjs.locale('zh-cn')

const pinia = createPinia()
const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/login', component: () => import('./views/login/Login.vue') },
        { 
            path: '/', 
            component: () => import('./layout/MainLayout2.vue'),
            redirect: '/dashboard',
            children: [
                { path: 'dashboard', component: () => import('./views/dashboard/Workbench.vue') },
                // (三) 商品管理
                { path: 'pdm/product', component: () => import('./views/product/ProductList.vue') },
                
                // (二) 客户管理
                { path: 'crm/customer', component: () => import('./views/crm/customer/CustomerList.vue') },
                { path: 'crm/public', component: () => import('./views/crm/public/PublicPool.vue') },
                { path: 'crm/contact', component: () => import('./views/crm/contact/index.vue') },
                { path: 'crm/info', component: () => import('./views/crm/info/index.vue') },
                { path: 'crm/follow', component: () => import('./views/crm/follow/index.vue') },
                { path: 'crm/quote', component: () => import('./views/quote/QuoteList.vue') },
                { path: 'crm/contract', component: () => import('./views/contract/ContractList.vue') },
                { path: 'crm/finance', component: () => import('./views/crm/finance/index.vue') },
                { path: 'crm/finance/deposit', component: () => import('./views/crm/finance/deposit/index.vue') },
                { path: 'crm/finance/intermediary-fee', component: () => import('./views/crm/finance/intermediary-fee/index.vue') },
                { path: 'crm/invoice', component: () => import('./views/crm/invoice/index.vue') },
                { path: 'crm/invoice/edit', component: () => import('./views/crm/invoice/EditPage.vue') },
                { path: 'crm/invoice/:id', component: () => import('./views/crm/invoice/EditPage.vue') },
                { path: 'crm/expense', component: () => import('./views/crm/expense/index.vue') },
                { path: 'crm/settings', component: () => import('./views/crm/settings/index.vue') },
                { path: 'crm/solution', component: () => import('./views/scheme/index.vue') },
                { path: 'scheme/detail/:id', component: () => import('./views/scheme/detail.vue') },

                // (四) 采购仓储
                { path: 'scm/inventory/warehouse-stock', component: () => import('./views/scm/inventory/warehouse-stock.vue') },
                { path: 'scm/inventory/product-stock', component: () => import('./views/inventory/InventoryList.vue') },
                
                { path: 'scm/purchase', component: () => import('./views/purchase/PurchaseRequestList.vue') },
                { path: 'scm/purchase/payment', component: () => import('./views/scm/purchase/payment.vue') },
                { path: 'scm/purchase/invoice', component: () => import('./views/scm/purchase/invoice.vue') },
                { path: 'scm/purchase/request', component: () => import('./views/purchase/PurchaseRequestList.vue') },

                { path: 'scm/inbound/purchase-inbound', component: () => import('./views/scm/inbound/purchase-inbound.vue') },
                { path: 'scm/inbound/purchase-inbound-edit', component: () => import('./views/scm/inbound/InboundEdit.vue') },
                { path: 'scm/inbound/sales-return', component: () => import('./views/scm/inbound/sales-return.vue') },
                { path: 'scm/inbound/other-inbound', component: () => import('./views/scm/inbound/other-inbound.vue') },

                { path: 'scm/outbound/sales-outbound', component: () => import('./views/outbound/OutboundList.vue') },
                { path: 'scm/outbound/sales-outbound-detail', component: () => import('./views/outbound/OutboundEdit.vue') },
                { path: 'scm/outbound/label-print', component: () => import('./views/outbound/LabelPrint.vue') },
                { path: 'scm/outbound/purchase-return', component: () => import('./views/scm/outbound/purchase-return.vue') },
                { path: 'scm/outbound/purchase-return/create', component: () => import('./views/scm/outbound/purchase-return-edit.vue') },
                { path: 'scm/outbound/purchase-return/edit/:id', component: () => import('./views/scm/outbound/purchase-return-edit.vue') },
                { path: 'scm/outbound/other-outbound', component: () => import('./views/scm/outbound/other-outbound.vue') },

                { path: 'scm/inventory/check', component: () => import('./views/scm/inventory/check/index.vue') },
                { path: 'scm/inventory/flow', component: () => import('./views/scm/inventory/flow/index.vue') },
                { path: 'scm/warehouse', component: () => import('./views/scm/warehouse/index.vue') },
                { path: 'scm/supplier', component: () => import('./views/scm/supplier/index.vue') },

                // (五) 售后管理
                { path: 'asm/work-order', component: () => import('./views/asm/WorkOrderList.vue') },
                { path: 'asm/maintenance', component: () => import('./views/asm/maintenance/index.vue') },

                // (一) 系统设置
                { path: 'system/user', component: () => import('./views/system/user/index.vue') },
                { path: 'system/role', component: () => import('./views/system/role/index.vue') },
                { path: 'system/dept', component: () => import('./views/system/dept/index.vue') },
                { path: 'system/team', component: () => import('./views/system/team/index.vue') },
                { path: 'system/profile', component: () => import('./views/system/profile/index.vue') },
                { path: 'system/log', component: () => import('./views/system/log/index.vue') },
                { path: 'system/config', component: () => import('./views/system/config/index.vue') },
                { path: 'system/permission', component: () => import('./views/system/permission/index.vue') },
                { path: 'system/code-rule', component: () => import('./views/system/code-rule/index.vue') },
                { path: 'system/cost-calculation', component: () => import('./views/system/cost-calculation/index.vue') },
                { path: 'system/notice', component: () => import('./views/system/notice/index.vue') },
                
                // (六) 项目管理 (使用原有的项目跟进页面)
                { path: 'project/list', component: () => import('./views/crm/follow/index.vue') },
                
                // (七) 部署维护 - 设备安装
                { path: 'asm/install/on-site', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'asm/install/remote', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'asm/install/delegate', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                
                // (七) 部署维护 - 设备维修
                { path: 'asm/repair/on-site', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'asm/repair/remote', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'asm/repair/delegate', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                
                // (八) 库存查询
                { path: 'scm/inventory/query', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                
                // (九) 统计报表 - 销售分析
                { path: 'report/sales/order-summary', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/sales/delivery', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/sales/return', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/sales/receivable', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/sales/receipt', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/sales/profit', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/sales/expense', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/sales/ranking', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                
                // (九) 统计报表 - 采购分析
                { path: 'report/purchase/order-summary', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/purchase/inbound', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/purchase/return', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/purchase/payment', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/purchase/supplier-statement', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/purchase/price-compare', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                
                // (九) 统计报表 - 库存分析
                { path: 'report/inventory/in-out-summary', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/inventory/ledger', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/inventory/check', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/inventory/flow-detail', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                
                // (九) 统计报表 - 财务分析
                { path: 'report/finance/general-ledger', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/finance/balance', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/finance/voucher-summary', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/finance/aging-analysis', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/finance/profit-statement', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/finance/balance-sheet', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/finance/cash-flow', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/finance/asset-depreciation', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/finance/expense-detail', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/finance/dept-expense', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                
                // (九) 统计报表 - 决策分析
                { path: 'report/management/daily', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/management/monthly', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/management/yearly', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/management/revenue-cost-profit', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/management/cash-flow-forecast', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/management/inventory-turnover', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                { path: 'report/management/delivery-rate', component: () => import('./views/_placeholder/PlaceholderPage.vue') },
                
                { path: 'workflow/tasks', component: () => import('./views/workflow/TaskList.vue') }
                
            ]
        }
    ]
})

router.beforeEach(async (to: RouteLocationNormalized, _from: RouteLocationNormalized, next: NavigationGuardNext) => {
    const token = localStorage.getItem('token')
    const resourceByPath: Record<string, string> = {
        '/system/user': 'UM',
        '/system/role': 'RM',
        '/system/permission': 'PM',
        '/system/team': 'TM',
        '/system/dept': 'DM',
        '/system/log': 'OL',
        '/system/config': 'GS',
        '/system/code-rule': 'NR',
        '/system/cost-calculation': 'PCC',
        '/crm/customer': 'CM',
        '/crm/contact': 'CC',
        '/crm/quote': 'QM',
        '/crm/follow': 'PF',
        '/project/list': 'PF',
        '/crm/contract': 'CO',
        '/crm/finance': 'IER',
        '/crm/finance/deposit': 'DeM',
        '/crm/finance/intermediary-fee': 'IFM',
        '/crm/invoice': 'IM',
        '/crm/expense': 'ER',
        '/crm/solution': 'SOL',
        '/scheme/detail': 'SOL',
        '/asm/maintenance': 'EM',
        '/asm/work-order': 'EM',
        '/scm/supplier': 'SM',
        '/pdm/product': 'PDM',
        '/scm/purchase': 'PCt',
        '/scm/purchase/request': 'PA',
        '/scm/warehouse': 'WM',
        '/scm/inbound/sales-return': 'WHM',
        '/scm/inbound/other-inbound': 'WHM',
        '/scm/outbound/purchase-return': 'OM',
        '/scm/outbound/sales-outbound': 'OM',
        '/scm/outbound/other-outbound': 'OM',
        '/scm/inventory/flow': 'IOF',
        '/scm/inventory/check': 'IC',
        '/scm/inventory/warehouse-stock': 'WM',
        '/scm/inventory/product-stock': 'WM',
        '/scm/inventory/query': 'ST',
        '/asm/install/on-site': 'PI',
        '/asm/install/remote': 'PI',
        '/asm/install/delegate': 'PI',
        '/asm/repair/on-site': 'EM',
        '/asm/repair/remote': 'EM',
        '/asm/repair/delegate': 'EM',
        '/report/sales/order-summary': 'SR',
        '/report/sales/delivery': 'SR',
        '/report/sales/return': 'SR',
        '/report/sales/receivable': 'SR',
        '/report/sales/receipt': 'SR',
        '/report/sales/profit': 'SR',
        '/report/sales/expense': 'SR',
        '/report/sales/ranking': 'SR',
        '/report/purchase/order-summary': 'PR',
        '/report/purchase/inbound': 'PR',
        '/report/purchase/return': 'PR',
        '/report/purchase/payment': 'PR',
        '/report/purchase/supplier-statement': 'PR',
        '/report/purchase/price-compare': 'PR',
        '/report/inventory/in-out-summary': 'WM',
        '/report/inventory/ledger': 'WM',
        '/report/inventory/check': 'IC',
        '/report/inventory/flow-detail': 'IOF',
        '/report/finance/general-ledger': 'IER',
        '/report/finance/balance': 'IER',
        '/report/finance/voucher-summary': 'IER',
        '/report/finance/aging-analysis': 'IER',
        '/report/finance/profit-statement': 'IER',
        '/report/finance/balance-sheet': 'IER',
        '/report/finance/cash-flow': 'IER',
        '/report/finance/asset-depreciation': 'IER',
        '/report/finance/expense-detail': 'IER',
        '/report/finance/dept-expense': 'IER',
        '/report/management/daily': 'SR',
        '/report/management/monthly': 'SR',
        '/report/management/yearly': 'SR',
        '/report/management/revenue-cost-profit': 'SR',
        '/report/management/cash-flow-forecast': 'IER',
        '/report/management/inventory-turnover': 'WM',
        '/report/management/delivery-rate': 'SR'
    }
    const needsRes = resourceByPath[to.path]
    const hasPerm = (perms?: string[], res?: string) => !!res ? (Array.isArray(perms) && perms.some(code => String(code||'').startsWith(res + ':'))) : true
    if (to.path === '/login') {
        if (token) {
            next('/')
        } else {
            next()
        }
    } else {
        if (token) {
            const store = useUserStore()
            // 如果用户信息为空，先获取用户信息
            if (!store.userInfo) {
                try {
                    await store.getInfo()
                } catch (error) {
                    // 获取用户信息失败，可能是token过期
                    store.logout()
                    next('/login')
                    return
                }
            }
            const ui:any = store.userInfo
            const perms:string[] = ui?.permissionCodes || []
            if (hasPerm(perms, needsRes) || !needsRes || to.path === '/dashboard' || to.path === '/') {
                next()
            } else {
                next('/dashboard')
            }
        } else {
            next('/login')
        }
    }
})

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(ElementPlus, { locale: zhCn })
app.use(pinia)
app.use(router)

app.mount('#app')
