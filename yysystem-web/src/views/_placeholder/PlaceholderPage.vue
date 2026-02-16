<template>
  <div class="app-container">
    <div class="placeholder-page">
      <el-empty :description="`【${pageTitle}】功能开发中...`">
        <template #image>
          <div class="placeholder-icon">
            <el-icon :size="80" color="#909399"><Document /></el-icon>
          </div>
        </template>
        <template #description>
          <div class="placeholder-desc">
            <h3>【{{ pageTitle }}】</h3>
            <p>功能开发中，敬请期待...</p>
            <p class="placeholder-path">路径: {{ $route.path }}</p>
          </div>
        </template>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Document } from '@element-plus/icons-vue'

const route = useRoute()

const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/project/list': '项目列表',
    '/asm/install/on-site': '现场安装',
    '/asm/install/remote': '远程安装',
    '/asm/install/delegate': '委托安装',
    '/asm/repair/on-site': '现场维修',
    '/asm/repair/remote': '远程维护',
    '/asm/repair/delegate': '委托维修',
    '/report/sales/order-summary': '订单汇总',
    '/report/sales/delivery': '发货汇总',
    '/report/sales/return': '退货汇总',
    '/report/sales/receivable': '应收汇总',
    '/report/sales/receipt': '回款汇总',
    '/report/sales/profit': '毛利分析',
    '/report/sales/expense': '费用汇总',
    '/report/sales/ranking': '销售排行',
    '/report/purchase/order-summary': '采购订单汇总',
    '/report/purchase/inbound': '到货汇总',
    '/report/purchase/return': '采购退货汇总',
    '/report/purchase/payment': '付款汇总',
    '/report/purchase/supplier-statement': '供方对账',
    '/report/purchase/price-compare': '价格对比',
    '/report/inventory/in-out-summary': '收发存表',
    '/report/inventory/ledger': '库存台账',
    '/report/inventory/check': '盘点汇总',
    '/report/inventory/flow-detail': '出入汇总',
    '/report/finance/general-ledger': '总账明细',
    '/report/finance/balance': '科目余额',
    '/report/finance/voucher-summary': '凭证汇总',
    '/report/finance/aging-analysis': '收付账龄',
    '/report/finance/profit-statement': '利润分析',
    '/report/finance/balance-sheet': '资产负债',
    '/report/finance/cash-flow': '现金流量',
    '/report/finance/asset-depreciation': '资产折旧',
    '/report/finance/expense-detail': '费用明细',
    '/report/finance/dept-expense': '部门费用',
    '/report/management/daily': '经营日报',
    '/report/management/monthly': '经营月报',
    '/report/management/yearly': '经营年报',
    '/report/management/revenue-cost-profit': '收入成利',
    '/report/management/cash-flow-forecast': '现金预测',
    '/report/management/inventory-turnover': '库存周转',
    '/report/management/delivery-rate': '订单交付',
    '/scm/inventory/query': '库存查询'
  }
  return titles[route.path] || '新功能'
})
</script>

<style scoped>
.placeholder-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.placeholder-icon {
  margin-bottom: 20px;
}

.placeholder-desc {
  text-align: center;
}

.placeholder-desc h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 8px;
}

.placeholder-desc p {
  font-size: 14px;
  color: #909399;
  margin: 4px 0;
}

.placeholder-path {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 16px;
  padding: 8px 16px;
  background: #f5f7fa;
  border-radius: 4px;
  display: inline-block;
}
</style>
