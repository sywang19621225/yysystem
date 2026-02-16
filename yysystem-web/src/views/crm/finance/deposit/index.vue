<template>
  <div class="app-container">
    <h2 class="page-title">保证金管理</h2>
    
    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #409EFF;">¥{{ statistics.totalDeposit?.toFixed(2) || '0.00' }}</div>
            <div style="color: #909399; margin-top: 8px;">保证金总额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #67C23A;">¥{{ statistics.totalRefund?.toFixed(2) || '0.00' }}</div>
            <div style="color: #909399; margin-top: 8px;">已退回金额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #E6A23C;">¥{{ statistics.totalPending?.toFixed(2) || '0.00' }}</div>
            <div style="color: #909399; margin-top: 8px;">待退回金额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #F56C6C;">{{ statistics.pendingCount || 0 }}</div>
            <div style="color: #909399; margin-top: 8px;">待处理笔数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索栏 -->
    <div class="toolbar">
      <el-input v-model="searchKeyword" placeholder="搜索客户名称" style="width: 300px; margin-right: 10px;" clearable @keyup.enter="fetchList" />
      <el-select v-model="searchType" placeholder="保证金类型" clearable style="width: 150px; margin-right: 10px;">
        <el-option label="投标保证金" value="投标保证金" />
        <el-option label="履约保证金" value="履约保证金" />
        <el-option label="质量保证金" value="质量保证金" />
      </el-select>
      <el-select v-model="searchStatus" placeholder="状态" clearable style="width: 150px; margin-right: 10px;">
        <el-option label="待退回" value="PENDING" />
        <el-option label="已退回" value="REFUNDED" />
        <el-option label="已转代理费" value="TRANSFERRED" />
      </el-select>
      <el-button @click="fetchList">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button v-if="canCreate" type="primary" @click="handleAdd">新增保证金</el-button>
      <el-button v-if="canCreate" type="warning" @click="handleConvert">货款转保证金</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table :data="tableData" border stripe v-loading="loading" style="margin-top: 16px;">
      <el-table-column type="index" width="50" align="center" />
      <el-table-column prop="contractNo" label="合同编号" min-width="150">
        <template #default="scope">
          <el-link type="primary" @click="viewContract(scope.row.contractId)">{{ scope.row.contractNo || '-' }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="contractName" label="合同名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="customerName" label="客户名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="companyName" label="公司名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="depositType" label="保证金类型" width="120">
        <template #default="scope">
          <el-tag :type="getDepositTypeType(scope.row.depositType)">{{ scope.row.depositType || '其他' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="depositSourceType" label="来源类型" width="120">
        <template #default="scope">
          <el-tag :type="scope.row.depositSourceType === 'CONVERT' ? 'warning' : 'info'">
            {{ scope.row.depositSourceType === 'CONVERT' ? '货款转保证金' : '主动支付' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="refundDueDate" label="到期退还日期" width="140" align="center">
        <template #default="scope">
          <span :class="{ 'text-danger': isOverdue(scope.row.refundDueDate) && !scope.row.actualRefundDate }">
            {{ scope.row.refundDueDate || '-' }}
            <el-tag v-if="isOverdue(scope.row.refundDueDate) && !scope.row.actualRefundDate" type="danger" size="small">已到期</el-tag>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="actualRefundDate" label="实际退还日期" width="140" align="center">
        <template #default="scope">
          <span :class="{ 'text-success': scope.row.actualRefundDate }">
            {{ scope.row.actualRefundDate || '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="amount" label="保证金金额" width="140" align="right">
        <template #default="scope">
          <span :style="{ color: Number(scope.row.amount || 0) === 0 ? '#F56C6C' : '' }">
            ¥{{ scope.row.amount?.toFixed(2) || '0.00' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="auditStatus" label="审核状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="getAuditStatusType(scope.row.auditStatus)">{{ getAuditStatusText(scope.row.auditStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="退回状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="refundAmount" label="已退回/转代理费" width="140" align="right">
        <template #default="scope">
          ¥{{ scope.row.refundAmount?.toFixed(2) || '0.00' }}
        </template>
      </el-table-column>
      <el-table-column prop="payTime" label="支付时间" width="160">
        <template #default="scope">
          <span>{{ scope.row.payTime || '-' }}</span>
          <el-tag v-if="scope.row.depositSourceType === 'CONVERT'" type="warning" size="small" style="margin-left: 4px;">货款转付</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="payMethod" label="支付方式" width="120">
        <template #default="scope">
          <span>{{ scope.row.payMethod || (scope.row.depositSourceType === 'CONVERT' ? '货款转付' : '-') }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleView(scope.row)">查看</el-button>
          <el-button v-if="scope.row.auditStatus === 'PENDING' && canUpdate" link type="success" @click="handleAudit(scope.row)">审核</el-button>
          <el-button v-if="scope.row.auditStatus !== 'PENDING' && canUpdate" link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button v-if="scope.row.auditStatus === 'PASSED' && scope.row.status !== 'REFUNDED' && scope.row.status !== 'TRANSFERRED' && canUpdate" link type="success" @click="handleRefund(scope.row)">保证金退回</el-button>
          <el-button v-if="scope.row.auditStatus === 'PASSED' && scope.row.status !== 'REFUNDED' && scope.row.status !== 'TRANSFERRED' && canUpdate" link type="warning" @click="handleTransfer(scope.row)">转代理费</el-button>
          <el-button v-if="(scope.row.status === 'REFUNDED' || scope.row.status === 'TRANSFERRED') && canDelete" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchList"
        @current-change="fetchList"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="合同" prop="contractId">
              <el-select v-model="form.contractId" filterable clearable style="width: 100%;" @change="onContractChange">
                <el-option v-for="c in contractOptions" :key="c.id" :label="c.contractNo" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户">
              <el-input v-model="form.customerName" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属公司" prop="companyId">
              <el-select v-model="form.companyId" filterable style="width: 100%;" @change="handleCompanyChange">
                <el-option v-for="c in companyOptions" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="保证金类型" prop="depositType">
              <el-select v-model="form.depositType" style="width: 100%;" :disabled="!!form.id">
                <el-option label="投标保证金" value="投标保证金" />
                <el-option label="履约保证金" value="履约保证金" />
                <el-option label="质量保证金" value="质量保证金" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="保证金金额" prop="amount">
              <el-input-number v-model="form.amount" :precision="2" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="支付时间" prop="payTime">
              <el-date-picker v-model="form.payTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="支付方式">
              <el-select v-model="form.payMethod" style="width: 100%;" clearable>
                <el-option 
                  v-for="method in paymentMethods" 
                  :key="method" 
                  :label="method" 
                  :value="method" 
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="到期退还日期" prop="refundDueDate">
              <el-date-picker v-model="form.refundDueDate" type="date" value-format="YYYY-MM-DD" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际退还日期" prop="actualRefundDate">
              <el-date-picker v-model="form.actualRefundDate" type="date" value-format="YYYY-MM-DD" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditDialogVisible" title="保证金审核" width="500px">
      <el-form ref="auditFormRef" :model="auditForm" :rules="auditRules" label-width="120px">
        <el-form-item label="合同编号">
          <el-input :model-value="currentRow?.contractNo" disabled />
        </el-form-item>
        <el-form-item label="保证金类型">
          <el-input :model-value="currentRow?.depositType" disabled />
        </el-form-item>
        <el-form-item label="申请金额">
          <el-input :model-value="currentRow?.amount?.toFixed(2) || '0.00'" disabled />
        </el-form-item>
        <el-form-item label="支付金额" prop="amount">
          <el-input-number v-model="auditForm.amount" :precision="2" :min="0" style="width: 100%;" placeholder="请输入实际支付金额" />
        </el-form-item>
        <el-form-item label="支付时间" prop="payTime">
          <el-date-picker v-model="auditForm.payTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%;" placeholder="请选择支付时间" />
        </el-form-item>
        <el-form-item label="支付方式" prop="payMethod">
          <el-select v-model="auditForm.payMethod" style="width: 100%;" clearable placeholder="请选择支付方式">
            <el-option 
              v-for="method in paymentMethods" 
              :key="method" 
              :label="method" 
              :value="method" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input v-model="auditForm.remark" type="textarea" :rows="3" placeholder="审核备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleAuditReject">驳回</el-button>
        <el-button type="success" @click="handleAuditPass">通过</el-button>
      </template>
    </el-dialog>

    <!-- 退回对话框 -->
    <el-dialog v-model="refundDialogVisible" title="保证金退回" width="500px">
      <el-form ref="refundFormRef" :model="refundForm" :rules="refundRules" label-width="120px">
        <el-form-item label="保证金金额">
          <el-input :model-value="currentRow?.amount?.toFixed(2)" disabled />
        </el-form-item>
        <el-form-item label="退回金额" prop="refundAmount">
          <el-input-number v-model="refundForm.refundAmount" :precision="2" :min="0" :max="currentRow?.amount" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="实际退还日期" prop="actualRefundDate">
          <el-date-picker v-model="refundForm.actualRefundDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%;" placeholder="请选择实际退还日期" />
        </el-form-item>
        <el-form-item label="退回备注">
          <el-input v-model="refundForm.refundRemark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRefundSubmit">确认退回</el-button>
      </template>
    </el-dialog>

    <!-- 转代理费对话框 -->
    <el-dialog v-model="transferDialogVisible" title="转为投标代理费" width="500px">
      <el-form ref="transferFormRef" :model="transferForm" :rules="transferRules" label-width="120px">
        <el-form-item label="保证金金额">
          <el-input :model-value="currentRow?.amount?.toFixed(2)" disabled />
        </el-form-item>
        <el-form-item label="转代理费金额" prop="transferAmount">
          <el-input-number v-model="transferForm.transferAmount" :precision="2" :min="0" :max="currentRow?.amount" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="转换时间" prop="transferTime">
          <el-date-picker v-model="transferForm.transferTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="transferForm.transferRemark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTransferSubmit">确认转换</el-button>
      </template>
    </el-dialog>

    <!-- 货款转保证金对话框 -->
    <el-dialog v-model="convertDialogVisible" title="货款转保证金" width="600px">
      <el-form ref="convertFormRef" :model="convertForm" :rules="convertRules" label-width="120px">
        <el-form-item label="合同" prop="contractId">
          <el-select v-model="convertForm.contractId" filterable style="width: 100%;" @change="onConvertContractChange">
            <el-option v-for="c in contractOptions" :key="c.id" :label="c.contractNo" :value="c.id">
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span>{{ c.contractNo }}</span>
                <span style="color: #909399; font-size: 12px;">{{ c.contractName || c.customerName }}</span>
                <el-tag type="warning" size="small">剩余: ¥{{ (c.contractAmount - (c.totalCollection || 0))?.toFixed(2) }}</el-tag>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="客户">
          <el-input v-model="convertForm.customerName" disabled />
        </el-form-item>
        <el-form-item label="合同金额">
          <el-input :model-value="convertForm.contractAmount?.toFixed(2)" disabled />
        </el-form-item>
        <el-form-item label="已收金额">
          <el-input :model-value="convertForm.collectedAmount?.toFixed(2)" disabled />
        </el-form-item>
        <el-form-item label="可转金额">
          <el-tag type="warning">¥{{ (convertForm.contractAmount - convertForm.collectedAmount)?.toFixed(2) }}</el-tag>
        </el-form-item>
        <el-form-item label="转保证金金额" prop="amount">
          <el-input-number v-model="convertForm.amount" :precision="2" :min="0" :max="convertForm.contractAmount - convertForm.collectedAmount" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="保证金类型" prop="depositType">
          <el-select v-model="convertForm.depositType" style="width: 100%;">
            <el-option label="投标保证金" value="投标保证金" />
            <el-option label="履约保证金" value="履约保证金" />
            <el-option label="质量保证金" value="质量保证金" />
          </el-select>
        </el-form-item>
        <el-form-item label="到期退还日期" prop="refundDueDate">
          <el-date-picker v-model="convertForm.refundDueDate" type="date" value-format="YYYY-MM-DD" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="convertForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="convertDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConvertSubmit">确认转换</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="保证金详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="合同编号">{{ currentRow?.contractNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ currentRow?.customerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="保证金类型">{{ currentRow?.depositType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="来源类型">{{ currentRow?.depositSourceType === 'CONVERT' ? '货款转保证金' : '主动支付' }}</el-descriptions-item>
        <el-descriptions-item label="保证金金额">¥{{ currentRow?.amount?.toFixed(2) || '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="到期退还日期">{{ currentRow?.refundDueDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="实际退还日期">{{ currentRow?.actualRefundDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ currentRow?.payTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ currentRow?.payMethod || (currentRow?.depositSourceType === 'CONVERT' ? '货款转付' : '-') }}</el-descriptions-item>
        <el-descriptions-item label="状态" :span="currentRow?.depositSourceType === 'CONVERT' ? 1 : 2">
          <el-tag :type="getStatusType(currentRow?.status)">{{ getStatusText(currentRow?.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="已退回/转代理费" :span="2">¥{{ currentRow?.refundAmount?.toFixed(2) || '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRow?.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRow?.refundRemark" label="退回备注" :span="2">{{ currentRow.refundRemark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'
import { getContractList } from '@/api/contract'
import { useUserStore } from '@/store/user'
import { getCustomerList } from '@/api/customer'

const router = useRouter()
const loading = ref(false)
const tableData = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const searchType = ref('')
const searchStatus = ref('')
const statistics = ref<any>({})
const contractOptions = ref<any[]>([])
const companyOptions = ref<{id: number, name: string}[]>([])
const paymentMethods = ref<string[]>([]) // 保证金支付方式列表

// 用户store
const userStore = useUserStore()

const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canCreate = computed(() => hasPermission('DeM:create'))
const canUpdate = computed(() => hasPermission('DeM:update'))
const canDelete = computed(() => hasPermission('DeM:delete'))

const loadCompanyOptions = async () => {
  try {
    const res: any = await getCustomerList({ current: 1, size: 1000 })
    const list = res.records || []
    companyOptions.value = list
      .filter((item: any) => {
        const name = item.customerName || ''
        return name.includes('原邑智能') || name.includes('维斯顿') || name.includes('原邑信息科技')
      })
      .map((item: any) => ({
        id: item.id,
        name: item.customerName
      }))
  } catch {
    companyOptions.value = []
  }
}

// 加载保证金支付方式
const loadPaymentMethods = async () => {
  try {
    const res: any = await request({
      url: '/system/config/deposit-payment-methods',
      method: 'get'
    })
    paymentMethods.value = res || []
  } catch {
    paymentMethods.value = []
  }
}

// 获取用户所属公司默认值
const getDefaultCompany = () => {
  const userInfo = userStore.userInfo || {}
  if (userInfo.companyId) {
    const company = companyOptions.value.find(c => c.id === userInfo.companyId)
    if (company) {
      return { id: company.id, name: company.name }
    }
  }
  if (companyOptions.value.length > 0) {
    return { id: companyOptions.value[0].id, name: companyOptions.value[0].name }
  }
  return { id: undefined, name: '' }
}

// 公司选择变化
const handleCompanyChange = (companyId: number) => {
  const company = companyOptions.value.find(c => c.id === companyId)
  form.companyName = company ? company.name : ''
}

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增保证金')
const formRef = ref<FormInstance>()
const form = reactive<any>({
  id: undefined,
  contractId: undefined,
  customerId: undefined,
  customerName: '',
  companyId: undefined,
  companyName: '',
  depositType: '投标保证金',
  amount: 0,
  payTime: '',
  payMethod: '',
  refundDueDate: '',
  actualRefundDate: '',
  remark: ''
})

const rules = reactive<FormRules>({
  contractId: [{ required: true, message: '请选择合同', trigger: 'change' }],
  depositType: [{ required: true, message: '请选择保证金类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入保证金金额', trigger: 'blur' }],
  payTime: [{ required: true, message: '请选择支付时间', trigger: 'change' }]
})

// 审核对话框
const auditDialogVisible = ref(false)
const auditFormRef = ref<FormInstance>()
const auditForm = reactive<any>({
  amount: 0,
  payTime: '',
  payMethod: '',
  remark: ''
})
const auditRules = reactive<FormRules>({
  amount: [{ required: true, message: '请输入支付金额', trigger: 'blur' }],
  payTime: [{ required: true, message: '请选择支付时间', trigger: 'change' }],
  payMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
})

// 退回对话框
const refundDialogVisible = ref(false)
const refundFormRef = ref<FormInstance>()
const refundForm = reactive<any>({
  refundAmount: 0,
  actualRefundDate: '',
  refundRemark: ''
})
const refundRules = reactive<FormRules>({
  refundAmount: [{ required: true, message: '请输入退回金额', trigger: 'blur' }],
  actualRefundDate: [{ required: true, message: '请选择实际退还日期', trigger: 'change' }]
})

// 转代理费对话框
const transferDialogVisible = ref(false)
const transferFormRef = ref<FormInstance>()
const transferForm = reactive<any>({
  transferAmount: 0,
  transferTime: '',
  transferRemark: ''
})
const transferRules = reactive<FormRules>({
  transferAmount: [{ required: true, message: '请输入转代理费金额', trigger: 'blur' }],
  transferTime: [{ required: true, message: '请选择转换时间', trigger: 'change' }]
})

// 货款转保证金对话框
const convertDialogVisible = ref(false)
const convertFormRef = ref<FormInstance>()
const convertForm = reactive<any>({
  contractId: undefined,
  customerId: undefined,
  customerName: '',
  companyId: undefined,
  companyName: '',
  contractAmount: 0,
  collectedAmount: 0,
  amount: 0,
  depositType: '质量保证金',
  refundDueDate: '',
  remark: ''
})
const convertRules = reactive<FormRules>({
  contractId: [{ required: true, message: '请选择合同', trigger: 'change' }],
  amount: [{ required: true, message: '请输入转保证金金额', trigger: 'blur' }],
  depositType: [{ required: true, message: '请选择保证金类型', trigger: 'change' }],
  refundDueDate: [{ required: true, message: '请选择到期退还日期', trigger: 'change' }]
})

// 查看详情
const viewDialogVisible = ref(false)
const currentRow = ref<any>(null)

// 获取保证金列表
const fetchList = async () => {
  loading.value = true
  try {
    const params: any = {
      current: currentPage.value,
      size: pageSize.value
    }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (searchType.value) params.depositType = searchType.value
    if (searchStatus.value) params.status = searchStatus.value
    
    const res: any = await request({
      url: '/crm/deposit/list',
      method: 'get',
      params
    })
    
    // 确保合同列表已加载
    if (contractOptions.value.length === 0) {
      await fetchContracts()
    }
    
    // 处理数据，解析extendFields
    const records = res.records || []
    tableData.value = records.map((item: any) => {
      const extendFields = parseExtendFields(item.extendFields)
      return {
        ...item,
        // 使用后端返回的合同编号和客户名称
        contractNo: item.contractNo || '-',
        customerName: item.customerName || '-',
        // 将arrivalTime映射为payTime（支付时间）
        payTime: item.arrivalTime || item.payTime || item.paymentTime || '-',
        // 支付方式
        payMethod: item.paymentMethod || item.payMethod || '',
        depositType: extractDepositType(item.remark),
        refundAmount: extendFields.depositRefundAmount || extendFields.transferAmount || 0,
        refundRemark: extendFields.depositRefundRemark || extendFields.transferRemark,
        status: calculateStatus(item.amount, extendFields, item.actualRefundDate)
      }
    })
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

// 获取统计
const fetchStatistics = async () => {
  try {
    const res: any = await request({
      url: '/crm/deposit/statistics',
      method: 'get'
    })
    statistics.value = res || {}
  } catch (e) {
    console.error('获取统计失败', e)
  }
}

// 解析extendFields
const parseExtendFields = (extendFields: string): any => {
  if (!extendFields) return {}
  try {
    return JSON.parse(extendFields)
  } catch {
    return {}
  }
}

// 提取保证金类型
const extractDepositType = (remark: string): string => {
  if (!remark) return '其他'
  if (remark.includes('投标')) return '投标保证金'
  if (remark.includes('履约')) return '履约保证金'
  if (remark.includes('质保') || remark.includes('质量')) return '质量保证金'
  return '其他'
}

// 计算状态
const calculateStatus = (amount: number, extendFields: any, actualRefundDate?: string): string => {
  const refundAmount = extendFields.depositRefundAmount || extendFields.transferAmount || 0
  const transferAmount = extendFields.transferAmount || 0
  const totalProcessed = Number(refundAmount) + Number(transferAmount)
  
  if (extendFields.transferToAgencyFee) return 'TRANSFERRED'
  if (totalProcessed >= amount) return 'REFUNDED'
  // 如果有实际退还日期，且退回金额大于0，视为已退回
  if (actualRefundDate && totalProcessed > 0) return 'REFUNDED'
  if (totalProcessed > 0) return 'PARTIAL'
  return 'PENDING'
}

// 获取合同列表
const fetchContracts = async () => {
  const res: any = await getContractList({ current: 1, size: 1000 })
  contractOptions.value = res.records || []
}

// 合同变更
const onContractChange = (contractId: number) => {
  const contract = contractOptions.value.find(c => c.id === contractId)
  if (contract) {
    form.customerId = contract.customerId
    form.customerName = contract.customerName
    // 设置默认公司
    const defaultCompany = getDefaultCompany()
    form.companyId = defaultCompany.id
    form.companyName = defaultCompany.name
  }
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  searchType.value = ''
  searchStatus.value = ''
  currentPage.value = 1
  fetchList()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增保证金'
  form.id = undefined
  form.contractId = undefined
  form.customerId = undefined
  form.customerName = ''
  form.companyId = undefined
  form.companyName = ''
  form.depositType = '投标保证金'
  form.amount = 0
  form.payTime = ''
  form.payMethod = ''
  form.refundDueDate = ''
  form.actualRefundDate = ''
  form.remark = ''
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑保证金'
  Object.assign(form, row)
  // 将arrivalTime映射为payTime（支付时间）
  form.payTime = row.arrivalTime || row.payTime || ''
  // 加载实际退还日期
  form.actualRefundDate = row.actualRefundDate || ''
  // 如果没有公司信息，设置默认值
  if (!form.companyId && companyOptions.value.length > 0) {
    const defaultCompany = getDefaultCompany()
    form.companyId = defaultCompany.id
    form.companyName = defaultCompany.name
  }
  dialogVisible.value = true
}

// 查看
const handleView = (row: any) => {
  const extendFields = parseExtendFields(row.extendFields)
  currentRow.value = {
    ...row,
    refundRemark: extendFields.depositRefundRemark || extendFields.transferRemark,
    refundAmount: extendFields.depositRefundAmount || extendFields.transferAmount || 0
  }
  viewDialogVisible.value = true
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    const data = {
      ...form,
      // 将payTime映射回arrivalTime（后端字段名），空值或'-'时设为null
      arrivalTime: form.payTime && form.payTime !== '-' ? form.payTime : null,
      // 将payMethod映射回paymentMethod（后端字段名）
      paymentMethod: form.payMethod || null,
      // 将actualRefundDate映射回actualRefundDate（后端字段名）
      actualRefundDate: form.actualRefundDate && form.actualRefundDate !== '-' ? form.actualRefundDate : null,
      remark: form.depositType + ' - ' + form.remark
    }
    
    try {
      if (form.id) {
        await request({
          url: `/crm/deposit/${form.id}`,
          method: 'put',
          data
        })
        ElMessage.success('更新成功')
      } else {
        await request({
          url: '/crm/deposit',
          method: 'post',
          data
        })
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchList()
      fetchStatistics()
    } catch (e: any) {
      ElMessage.error(e.message || '操作失败')
    }
  })
}

// 审核
const handleAudit = (row: any) => {
  currentRow.value = row
  auditForm.amount = row.amount || 0
  auditForm.payTime = ''
  auditForm.payMethod = ''
  auditForm.remark = ''
  auditDialogVisible.value = true
}

// 审核通过
const handleAuditPass = async () => {
  if (!auditFormRef.value) return
  await auditFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await request({
        url: `/crm/deposit/${currentRow.value.id}/audit`,
        method: 'post',
        data: {
          auditStatus: 'PASSED',
          amount: auditForm.amount,
          arrivalAmount: auditForm.amount,
          arrivalTime: auditForm.payTime,
          paymentTime: auditForm.payTime,
          paymentMethod: auditForm.payMethod,
          remark: currentRow.value.remark + (auditForm.remark ? ' | 审核备注: ' + auditForm.remark : '')
        }
      })
      ElMessage.success('审核通过')
      auditDialogVisible.value = false
      fetchList()
      fetchStatistics()
    } catch (e: any) {
      ElMessage.error(e.message || '操作失败')
    }
  })
}

// 审核驳回
const handleAuditReject = async () => {
  try {
    await request({
      url: `/crm/deposit/${currentRow.value.id}/audit`,
      method: 'post',
      data: {
        auditStatus: 'REJECTED',
        remark: currentRow.value.remark + (auditForm.remark ? ' | 驳回原因: ' + auditForm.remark : '')
      }
    })
    ElMessage.success('已驳回')
    auditDialogVisible.value = false
    fetchList()
    fetchStatistics()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该保证金记录？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request({
        url: `/crm/deposit/${row.id}`,
        method: 'delete'
      })
      ElMessage.success('删除成功')
      fetchList()
      fetchStatistics()
    } catch (e: any) {
      ElMessage.error(e.message || '删除失败')
    }
  })
}

// 退回
const handleRefund = (row: any) => {
  currentRow.value = row
  refundForm.refundAmount = row.amount
  // 如果已有实际退还日期则显示，否则清空让用户填写
  refundForm.actualRefundDate = row.actualRefundDate || ''
  refundForm.refundRemark = ''
  refundDialogVisible.value = true
}

// 提交退回
const handleRefundSubmit = async () => {
  if (!refundFormRef.value) return
  await refundFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      // 从日期时间中提取日期部分
      const actualRefundDate = refundForm.actualRefundDate ? refundForm.actualRefundDate.split(' ')[0] : ''
      
      await request({
        url: `/crm/deposit/${currentRow.value.id}/refund`,
        method: 'post',
        data: {
          refundAmount: refundForm.refundAmount,
          actualRefundDate,
          refundRemark: refundForm.refundRemark
        }
      })
      ElMessage.success('退回成功')
      refundDialogVisible.value = false
      fetchList()
      fetchStatistics()
    } catch (e: any) {
      ElMessage.error(e.message || '操作失败')
    }
  })
}

// 转代理费
const handleTransfer = (row: any) => {
  currentRow.value = row
  transferForm.transferAmount = row.amount
  transferForm.transferTime = ''
  transferForm.transferRemark = ''
  transferDialogVisible.value = true
}

// 提交转代理费
const handleTransferSubmit = async () => {
  if (!transferFormRef.value) return
  await transferFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      await request({
        url: `/crm/deposit/${currentRow.value.id}/transfer`,
        method: 'post',
        data: transferForm
      })
      ElMessage.success('转换成功')
      transferDialogVisible.value = false
      fetchList()
      fetchStatistics()
    } catch (e: any) {
      ElMessage.error(e.message || '操作失败')
    }
  })
}

// 查看合同
const viewContract = (contractId: number) => {
  if (contractId) {
    router.push(`/crm/contract`)
  }
}

// 获取类型标签样式
const getDepositTypeType = (type: string) => {
  if (type === '投标保证金') return 'primary'
  if (type === '履约保证金') return 'success'
  if (type === '质量保证金') return 'warning'
  return 'info'
}

// 获取状态标签样式
const getStatusType = (status: string) => {
  if (status === 'PENDING') return 'warning'
  if (status === 'REFUNDED') return 'success'
  if (status === 'TRANSFERRED') return 'info'
  if (status === 'PARTIAL') return 'primary'
  return 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  if (status === 'PENDING') return '待退回'
  if (status === 'REFUNDED') return '已退回'
  if (status === 'TRANSFERRED') return '已转代理费'
  if (status === 'PARTIAL') return '部分退回'
  return '未知'
}

// 获取审核状态标签样式
const getAuditStatusType = (status: string) => {
  if (status === 'PENDING') return 'warning'
  if (status === 'PASSED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'info'
}

// 获取审核状态文本
const getAuditStatusText = (status: string) => {
  if (status === 'PENDING') return '待审核'
  if (status === 'PASSED') return '已通过'
  if (status === 'REJECTED') return '已驳回'
  return '未知'
}

// 判断是否到期
const isOverdue = (date: string) => {
  if (!date) return false
  const dueDate = new Date(date)
  const today = new Date()
  return dueDate < today
}

// 货款转保证金
const handleConvert = () => {
  convertForm.contractId = undefined
  convertForm.customerId = undefined
  convertForm.customerName = ''
  convertForm.companyId = undefined
  convertForm.companyName = ''
  convertForm.contractAmount = 0
  convertForm.collectedAmount = 0
  convertForm.amount = 0
  convertForm.depositType = '质量保证金'
  convertForm.refundDueDate = ''
  convertForm.remark = ''
  convertDialogVisible.value = true
}

// 货款转保证金 - 合同选择变化
const onConvertContractChange = (contractId: number) => {
  const contract = contractOptions.value.find(c => c.id === contractId)
  if (contract) {
    convertForm.customerId = contract.customerId
    convertForm.customerName = contract.customerName
    convertForm.contractAmount = Number(contract.contractAmount || 0)
    // 从合同的收款记录计算已收金额
    convertForm.collectedAmount = Number(contract.totalCollection || 0)
    // 设置默认公司
    const defaultCompany = getDefaultCompany()
    convertForm.companyId = defaultCompany.id
    convertForm.companyName = defaultCompany.name
  }
}

// 提交货款转保证金
const handleConvertSubmit = async () => {
  if (!convertFormRef.value) return
  await convertFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      await request({
        url: '/crm/deposit/convert-from-payment',
        method: 'post',
        data: {
          contractId: convertForm.contractId,
          customerId: convertForm.customerId,
          companyId: convertForm.companyId,
          companyName: convertForm.companyName,
          amount: convertForm.amount,
          remark: convertForm.depositType + ' - ' + convertForm.remark,
          refundDueDate: convertForm.refundDueDate
        }
      })
      ElMessage.success('货款转保证金成功')
      convertDialogVisible.value = false
      fetchList()
      fetchStatistics()
    } catch (e: any) {
      ElMessage.error(e.message || '操作失败')
    }
  })
}

onMounted(() => {
  fetchList()
  fetchStatistics()
  fetchContracts()
  loadCompanyOptions()
  loadPaymentMethods()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
.pagination-container {
  margin-top: 16px;
  text-align: right;
}
</style>
