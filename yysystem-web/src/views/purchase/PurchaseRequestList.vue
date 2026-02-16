<template>
  <div class="app-container">
    <h2 class="page-title">商品采购</h2>
    <div class="header-actions">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="申请单号">
          <el-input v-model="queryParams.requestNo" placeholder="请输入申请单号" clearable @keyup.enter="fetchData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="openEdit()">新增申请</el-button>
    </div>
    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="requestNo" label="申请单号" width="160" />
      <el-table-column prop="requestDate" label="申请日期" width="140" />
      <el-table-column prop="requesterName" label="申请人" width="140" />
      <el-table-column prop="purchaseCategory" label="采购分类" width="140" />
      <el-table-column prop="customerId" label="客户" width="160" v-if="showCustomerColumn">
        <template #default="scope">
          {{ getCustomerName(scope.row.customerId) }}
        </template>
      </el-table-column>
      <el-table-column prop="contractId" label="合同订单" width="180" v-if="showContractColumn">
        <template #default="scope">
          {{ getContractName(scope.row.contractId) }}
        </template>
      </el-table-column>
      <el-table-column prop="supplierId" label="供应商" width="160">
        <template #default="scope">
          {{ getSupplierName(scope.row.supplierId) }}
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="总金额" width="120" />
      <el-table-column prop="purchaseStatus" label="采购状态" width="120">
        <template #default="scope">
          <el-tag :type="getPurchaseStatusType(scope.row.purchaseStatus)" size="small">
            {{ purchaseStatusText(scope.row.purchaseStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          {{ statusText(scope.row.status) }}
        </template>
      </el-table-column>
      <el-table-column prop="auditStatus" label="审核状态" width="120">
        <template #default="scope">
          {{ auditStatusText(scope.row.auditStatus) }}
        </template>
      </el-table-column>
      <el-table-column prop="lockStatus" label="锁单状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.lockStatus === '1' ? 'danger' : 'success'" size="small">
            {{ lockStatusText(scope.row.lockStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openEdit(scope.row.id)" :disabled="scope.row.lockStatus === '1'">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)" :disabled="scope.row.lockStatus === '1'">删除</el-button>
          <el-button link type="warning" @click="handleLock(scope.row)" v-if="scope.row.lockStatus !== '1'">锁单</el-button>
          <el-button link type="success" @click="handleUnlock(scope.row)" v-if="scope.row.lockStatus === '1' && isAdmin">解锁</el-button>
          <el-button link type="primary" @click="handleAudit(scope.row)" v-if="scope.row.auditStatus !== 'PASSED' && scope.row.auditStatus !== 'REJECTED'">审核</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>
    <el-dialog v-model="dialogVisible" :title="editId ? '编辑采购申请' : '新增采购申请'" width="820px">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="申请单号">
              <el-input v-model="form.requestNo" placeholder="系统自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申请日期">
              <el-date-picker v-model="form.requestDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="采购状态">
              <el-select v-model="form.purchaseStatus" placeholder="请选择采购状态" style="width: 100%">
                <el-option label="待采购" value="待采购" />
                <el-option label="采购付款" value="采购付款" />
                <el-option label="部分发货" value="部分发货" />
                <el-option label="全部发货" value="全部发货" />
                <el-option label="部分到库" value="部分到库" />
                <el-option label="全部到库" value="全部到库" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="申请人">
              <el-input v-model="form.requesterName" placeholder="自动带出登录人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" required>
              <el-select v-model="form.supplierId" placeholder="请选择供应商" filterable style="width: 100%">
                <el-option v-for="s in supplierList" :key="s.id" :label="s.supplierName || s.name || s.companyName" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="采购分类">
              <el-select v-model="form.purchaseCategory" placeholder="请选择采购分类" style="width: 100%" @change="onPurchaseCategoryChange">
                <el-option v-for="item in purchaseCategoryList" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同附件">
              <div style="display:flex; gap:10px; align-items:center; flex-wrap:wrap;">
                <el-upload
                  action="/api/common/upload"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :before-upload="beforeContractUpload"
                  :on-success="onContractUploadSuccess"
                  :on-error="onContractUploadError"
                >
                  <el-button size="small">上传合同</el-button>
                </el-upload>
                <div v-for="(f, index) in contractAttachments" :key="f.url" style="display:flex;align-items:center;gap:6px;">
                  <el-link :href="f.url" target="_blank">{{ f.name }}</el-link>
                  <el-button link type="danger" size="small" @click="removeContractAttachment(index)" :icon="Delete" />
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 项目采购专用字段 -->
        <el-row :gutter="20" v-if="form.purchaseCategory === '项目采购'">
          <el-col :span="12">
            <el-form-item label="客户" required>
              <el-select v-model="form.customerId" placeholder="请选择客户" filterable style="width: 100%" @change="onCustomerChange">
                <el-option v-for="c in customerList" :key="c.id" :label="c.customerName" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同订单" required>
              <el-select v-model="form.contractId" placeholder="请选择合同订单" filterable style="width: 100%" @change="onContractChange" :disabled="!form.customerId">
                <el-option v-for="c in contractList" :key="c.id" :label="c.contractName || c.contractNo" :value="c.id">
                  <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span>{{ c.contractName || c.contractNo }}</span>
                    <span style="color: #f56c6c; font-weight: bold;">¥{{ formatCurrency(c.contractAmount) }}</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <div style="text-align: right; margin-bottom: 10px; font-weight: bold; color: #f56c6c;">
              合计金额：¥{{ calcTotalAmount }}
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="付款时间">
              <el-date-picker 
                v-model="form.paymentTime" 
                type="datetime" 
                value-format="YYYY-MM-DDTHH:mm:ss" 
                style="width: 100%"
                clearable
                :default-time="null"
                :editable="false"
                placeholder="请选择付款时间"
                @change="(val: Date) => { if (!val) form.paymentTime = '' }"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="付款凭证">
              <div style="display:flex; gap:10px; align-items:center; flex-wrap:wrap;">
                <el-upload
                  action="/api/common/upload"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :before-upload="beforePaymentUpload"
                  :on-success="onPaymentUploadSuccess"
                  :on-error="onPaymentUploadError"
                >
                  <el-button size="small">上传凭证</el-button>
                </el-upload>
                <div v-for="(f, index) in paymentVoucherList" :key="f.url" style="display:flex;align-items:center;gap:6px;">
                  <el-link :href="f.url" target="_blank">{{ f.name }}</el-link>
                  <el-button link type="danger" size="small" @click="removePaymentVoucher(index)" :icon="Delete" />
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 验收时间和验收备注暂时隐藏
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="验收时间">
              <el-date-picker 
                v-model="form.acceptanceTime" 
                type="datetime" 
                value-format="YYYY-MM-DDTHH:mm:ss" 
                style="width: 100%"
                clearable
                :default-time="null"
                placeholder="请选择验收时间"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="验收备注">
              <el-input v-model="form.acceptanceRemark" placeholder="验收情况备注" />
            </el-form-item>
          </el-col>
        </el-row>
        -->
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
        <div class="detail-header">
          <span>申请明细</span>
          <el-button type="primary" link @click="addItem">添加商品</el-button>
        </div>
        <el-table :data="form.itemList" border>
          <el-table-column prop="productCode" label="编号" width="140" />
          <el-table-column prop="productName" label="名称" width="160" />
          <el-table-column prop="purchaseQuantity" label="数量" width="100" />
          <el-table-column prop="purchasePrice" label="采购价格" width="120" />
          <!-- 到货数量和验收数量暂时隐藏
          <el-table-column prop="arrivalQuantity" label="到货数量" width="100" />
          <el-table-column prop="acceptQuantity" label="验收数量" width="100" />
          -->
          <el-table-column prop="shipDate" label="发货日期" width="120" />
          <el-table-column prop="expectedDate" label="到货日期" width="120" />
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button link type="primary" @click="openItemDialog(scope.$index)">编辑</el-button>
              <el-button link type="danger" @click="removeItem(scope.$index)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="total-amount">总金额：¥{{ calcTotalAmount }}</div>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="info" @click="handleSave">保存</el-button>
          <el-button type="primary" @click="handleSubmit">提交申请</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="itemDialogVisible" :title="editingItemIndex >= 0 ? '编辑商品' : '添加商品'" width="560px">
      <el-form :model="itemForm" label-width="100px">
        <el-form-item label="商品" required>
          <el-select v-model="itemForm.productId" placeholder="选择商品" filterable style="width: 100%" @change="(val:number)=>onProductChange(val, itemForm)">
            <el-option v-for="p in productList" :key="p.id" :label="p.productName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="itemForm.productSpec" disabled />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="数量" required>
              <el-input-number v-model="itemForm.purchaseQuantity" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="采购价格" required>
              <el-input-number v-model="itemForm.purchasePrice" :min="0" :precision="2" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="成本价格">
              <el-input-number v-model="itemForm.unitPrice" :min="0" :precision="2" :step="0.1" style="width: 100%" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12"></el-col>
        </el-row>
        <!-- 到货数量和验收数量暂时隐藏
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="到货数量">
              <el-input-number v-model="itemForm.arrivalQuantity" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="验收数量">
              <el-input-number v-model="itemForm.acceptQuantity" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="发货日期">
              <el-date-picker 
                v-model="itemForm.shipDate" 
                type="date" 
                value-format="YYYY-MM-DD" 
                style="width: 100%"
                clearable
                placeholder="请选择发货日期"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="到货日期">
              <el-date-picker 
                v-model="itemForm.expectedDate" 
                type="date" 
                value-format="YYYY-MM-DD" 
                style="width: 100%"
                clearable
                placeholder="请选择到货日期"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="itemDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmItem">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 审核弹窗 -->
    <el-dialog v-model="auditDialogVisible" title="采购申请审核" width="500px">
      <div v-if="currentAuditRow" style="margin-bottom: 20px;">
        <p><strong>申请单号：</strong>{{ currentAuditRow.requestNo }}</p>
        <p><strong>供应商：</strong>{{ getSupplierName(currentAuditRow.supplierId) }}</p>
        <p><strong>总金额：</strong>¥{{ formatCurrency(currentAuditRow.totalAmount) }}</p>
      </div>
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="审核结果" required>
          <el-radio-group v-model="auditForm.status">
            <el-radio label="PASSED">通过</el-radio>
            <el-radio label="REJECTED">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input v-model="auditForm.detail" type="textarea" :rows="3" placeholder="请输入审核意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="auditDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAudit" :loading="auditLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { getProductList } from '@/api/product'
import { getPurchaseRequestList, getPurchaseRequestById, savePurchaseRequest, updatePurchaseRequest, deletePurchaseRequest, lockPurchaseRequest, unlockPurchaseRequest, auditPurchaseRequest } from '@/api/purchase'
import { getSupplierList } from '@/api/supplier'
import { getCustomerList } from '@/api/customer'
import { getContractList, getContractById, getContractListByCustomer } from '@/api/contract'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const loading = ref(false)
const tableData = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 格式化货币
const formatCurrency = (value: number | undefined) => {
  if (value === undefined || value === null) return '0.00'
  return value.toFixed(2)
}
const dialogVisible = ref(false)
const editId = ref<number|undefined>(undefined)
const itemDialogVisible = ref(false)
const editingItemIndex = ref(-1)
const queryParams = reactive({ requestNo: '' })

// 审核相关变量
const auditDialogVisible = ref(false)
const auditLoading = ref(false)
const currentAuditRow = ref<any>(null)
const auditForm = reactive({
  status: 'PASSED',
  detail: ''
})
const form = reactive<any>({ 
  requestNo: '', 
  requesterId: undefined,
  requesterName: '', 
  requestDate: '', 
  deptId: undefined,
  supplierId: undefined,
  supplierName: '', // 供应商名称
  purchaseCategory: '', 
  customerId: undefined,
  customerName: '',
  contractId: undefined,
  contractNo: '',
  contractName: '',
  purchaseStatus: '待采购',
  paymentTime: '',
  acceptanceTime: '',
  acceptanceRemark: '',
  remark: '', 
  itemList: [], 
  totalAmount: 0 
})
const productList = ref<any[]>([])
const purchaseCategoryList = ref<string[]>([])
const supplierList = ref<any[]>([])
const customerList = ref<any[]>([])
const contractList = ref<any[]>([])
const supplierWatchDisabled = ref(false)
const uploadHeaders = computed(()=>({ Authorization: `Bearer ${localStorage.getItem('token') || ''}` }))
const contractAttachments = ref<any[]>([])
const paymentVoucherList = ref<any[]>([])
const onContractUploadSuccess = (res:any, file:any) => {
  const url = res?.data
  if (url) {
    contractAttachments.value.push({ name: file.name, url })
    ElMessage.success('合同已上传')
  } else {
    ElMessage.error(res?.message || '上传失败')
  }
}
const onContractUploadError = () => { ElMessage.error('合同上传失败') }
const beforeContractUpload = (file:File) => {
  const max = 300 * 1024 * 1024
  if (file.size > max) { ElMessage.error('文件超过300MB限制'); return false }
  return true
}
const onPaymentUploadSuccess = (res:any, file:any) => {
  const url = res?.data
  if (url) { paymentVoucherList.value.push({ name: file.name, url }); ElMessage.success('凭证已上传') } else { ElMessage.error(res?.message || '上传失败') }
}
const onPaymentUploadError = () => { ElMessage.error('凭证上传失败') }
const beforePaymentUpload = (file:File) => {
  const max = 300 * 1024 * 1024
  if (file.size > max) { ElMessage.error('文件超过300MB限制'); return false }
  return true
}

// 删除合同附件
const removeContractAttachment = (index: number) => {
  ElMessageBox.confirm('确认删除该合同附件吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    contractAttachments.value.splice(index, 1)
    ElMessage.success('合同附件已删除')
  }).catch(() => {})
}

// 删除付款凭证
const removePaymentVoucher = (index: number) => {
  ElMessageBox.confirm('确认删除该付款凭证吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    paymentVoucherList.value.splice(index, 1)
    ElMessage.success('付款凭证已删除')
  }).catch(() => {})
}

const itemForm = reactive<any>({
  productId: undefined,
  productName: '',
  productCode: '',
  productSpec: '',
  purchaseQuantity: 1,
  unitPrice: 0,
  purchasePrice: 0,
  arrivalQuantity: 0,
  acceptQuantity: 0,
  shipDate: '',
  expectedDate: ''
})

const userStore = useUserStore()

// 检查是否为管理员
const isAdmin = computed(() => {
  const userInfo = userStore.userInfo
  return userInfo && (userInfo.role === 'admin' || (userInfo.roles && userInfo.roles.includes('admin')))
})

const fetchProducts = async (supplierId?: any) => {
  if (!supplierId) {
    productList.value = []
    return
  }
  const res:any = await getProductList({ current: 1, size: 1000, supplierId: Number(supplierId) })
  productList.value = res.records || []
}

const fetchSuppliers = async () => {
  const res: any = await getSupplierList({ current: 1, size: 1000 })
  let suppliers = res.records || res || []
  
  // 按供应商名称拼音排序
  suppliers.sort((a: any, b: any) => {
    const nameA = (a.supplierName || a.name || a.companyName || '').toString()
    const nameB = (b.supplierName || b.name || b.companyName || '').toString()
    
    // 使用拼音排序，支持中文
    return nameA.localeCompare(nameB, 'zh-CN', { sensitivity: 'accent' })
  })
  
  supplierList.value = suppliers
}

// 是否显示客户列（项目采购时显示）
const showCustomerColumn = computed(() => {
  // 如果表格中有任何一行是项目采购，就显示客户列
  return tableData.value.some((row: any) => row.purchaseCategory === '项目采购')
})

// 是否显示合同列（项目采购时显示）
const showContractColumn = computed(() => {
  // 如果表格中有任何一行是项目采购，就显示合同列
  return tableData.value.some((row: any) => row.purchaseCategory === '项目采购')
})

// 获取客户名称
const getCustomerName = (customerId: number) => {
  if (!customerId) return '-'
  const customer = customerList.value.find(c => c.id === customerId)
  return customer?.customerName || '-'
}

// 获取合同名称
const getContractName = (contractId: number) => {
  if (!contractId) return '-'
  const contract = contractList.value.find(c => c.id === contractId)
  return contract?.contractName || '-'
}

const fetchCustomers = async () => {
  // 编辑模式下，form.purchaseCategory 已经有值
  // 新增模式下，如果还没选择采购分类，先加载所有客户
  const isProjectPurchase = form.purchaseCategory === '项目采购'
  
  if (isProjectPurchase) {
    // 项目采购：只获取有合同的客户
    try {
      // 先获取所有合同
      const contractRes: any = await getContractList({ current: 1, size: 1000 })
      const contracts = contractRes.records || contractRes || []
      
      // 提取合同中的客户ID
      const customerIds = [...new Set(contracts.map((c: any) => c.customerId).filter(Boolean))]
      
      if (customerIds.length > 0) {
        // 获取所有客户，然后过滤出有合同的客户
        const customerRes: any = await getCustomerList({ current: 1, size: 1000 })
        const allCustomers = customerRes.records || customerRes || []
        customerList.value = allCustomers.filter((customer: any) => customerIds.includes(customer.id))
      } else {
        customerList.value = []
      }
    } catch (error) {
      console.error('获取有合同的客户失败:', error)
      customerList.value = []
    }
  } else {
    // 普通采购或新增模式：显示所有客户
    const res: any = await getCustomerList({ current: 1, size: 1000 })
    customerList.value = res.records || res || []
  }
}

const fetchContracts = async (customerId?: number) => {
  console.log('fetchContracts被调用, customerId:', customerId)
  if (!customerId) {
    console.log('customerId为空，清空合同列表')
    contractList.value = []
    return
  }
  console.log('调用getContractListByCustomer API, 参数:', customerId)
  const res: any = await getContractListByCustomer(customerId)
  console.log('getContractListByCustomer API返回结果:', res)
  contractList.value = res.records || res || []
  console.log('合同列表已更新:', contractList.value)
}

const onPurchaseCategoryChange = (value: string) => {
  // 当选择项目采购时，清空之前的选择
  if (value === '项目采购') {
    form.customerId = undefined
    form.customerName = ''
    form.contractId = undefined
    form.contractNo = ''
    form.contractName = ''
    form.supplierId = undefined
    form.supplierName = '' // 供应商名称
    contractList.value = []
  }
  // 重新获取客户列表（根据采购类型过滤）
  fetchCustomers()
}

const onCustomerChange = (customerId: number) => {
  console.log('客户选择变化:', customerId)
  const customer = customerList.value.find(c => c.id === customerId)
  form.customerName = customer?.customerName || ''
  form.contractId = undefined
  form.contractNo = ''
  form.contractName = ''
  console.log('准备获取客户合同, 客户ID:', customerId)
  fetchContracts(customerId)
}

const onContractChange = async (contractId: number) => {
  const contract = contractList.value.find(c => c.id === contractId)
  form.contractNo = contract?.contractNo || ''
  form.contractName = contract?.contractName || ''
  
  // 获取合同详细信息，包括明细
  if (contractId) {
    try {
      const contractDetail = await getContractById(contractId)
      const contractData = contractDetail.data
      
      // 注释掉自动设置供应商的逻辑，让用户自由选择
      // if (contractData?.supplierId) {
      //   form.supplierId = contractData.supplierId
      // }
      
      // 加载合同明细到采购申请明细中
      if (contractData?.detailList) {
        form.itemList = contractData.detailList.map((item: any) => ({
          productId: item.productId,
          productName: item.productName,
          productCode: item.productCode,
          productSpec: item.productSpec,
          purchaseQuantity: item.quantity || 1,
          unitPrice: item.unitPrice || 0,
          purchasePrice: item.unitPrice || 0,
          shipDate: '',
          expectedDate: ''
        }))
      }
    } catch (error) {
      console.error('获取合同明细失败:', error)
      ElMessage.error('获取合同明细失败')
    }
  }
}

const getSupplierName = (id: any) => {
  const sid = Number(id)
  if (!sid) return ''
  const s: any = (supplierList.value || []).find((x: any) => Number(x.id) === sid)
  return s?.supplierName || s?.name || s?.companyName || sid
}

// 申请状态文本转换函数
const statusText = (val: any) => {
  const v = String(val || '').toUpperCase()
  const map: Record<string, string> = {
    DRAFT: '草稿',
    SUBMITTED: '已提交',
    CANCELLED: '已取消',
    DONE: '已完成'
  }
  return map[v] || (v ? v : '-')
}

// 审核状态文本转换函数
const auditStatusText = (val: any) => {
  const v = String(val || '').toUpperCase()
  const map: Record<string, string> = {
    PENDING: '待审核',
    UNDER_REVIEW: '审核中',
    PASSED: '已通过',
    REJECTED: '已驳回',
    APPROVED: '已通过',
    REFUSED: '已驳回'
  }
  return map[v] || (v ? v : '-')
}

// 锁单状态文本转换函数
const lockStatusText = (val: any) => {
  const v = String(val || '0')
  const map: Record<string, string> = {
    '0': '未锁单',
    '1': '已锁单'
  }
  return map[v] || '未锁单'
}

// 采购状态文本转换函数
const purchaseStatusText = (val: any) => {
  const v = String(val || '')
  const map: Record<string, string> = {
    '待采购': '待采购',      // 初始状态，等待采购
    '采购付款': '采购付款',  // 已付款，准备采购
    '部分发货': '部分发货',  // 部分商品已发货
    '全部发货': '全部发货',  // 所有商品已发货
    '部分到库': '部分到库',  // 部分商品已到库
    '全部到库': '全部到库'   // 所有商品已到库
  }
  return map[v] || (v ? v : '待采购')
}

// 采购状态标签类型映射函数
const getPurchaseStatusType = (val: any) => {
  const v = String(val || '')
  const map: Record<string, string> = {
    '待采购': 'info',      // 蓝色标签，表示等待处理
    '采购付款': 'warning', // 黄色标签，表示已付款
    '部分发货': 'warning', // 黄色标签，表示发货进行中
    '全部发货': 'success', // 绿色标签，表示发货完成
    '部分到库': 'primary', // 主要标签，表示到库进行中
    '全部到库': 'success'  // 绿色标签，表示到库完成
  }
  return map[v] || 'info'
}

const fetchPurchaseCategories = async () => {
  try {
    const res: any = await request({ url: '/system/config/list', method: 'get', params: { size: 100, current: 1 } })
    const list = res?.records || res || []
    const general = (list || []).find((item: any) => item.configKey === 'general_settings')
    let cfg: any = {}
    try { cfg = general?.configValue ? JSON.parse(general.configValue) : {} } catch { cfg = {} }
    const custom = cfg.customCategories || {}
    const keys = Object.keys(custom || {})
    const pickByName = (keyword: string) => {
      const key = keys.find(k => String(k || '').includes(keyword))
      return key ? (Array.isArray(custom[key]) ? custom[key] : []) : []
    }
    const direct = Array.isArray(cfg.purchaseCategories) ? cfg.purchaseCategories : []
    const byName = pickByName('采购分类')
    const fallback = keys.length > 0 ? (Array.isArray(custom[keys[0]]) ? custom[keys[0]] : []) : []
    const result = (direct.length > 0 ? direct : (byName.length > 0 ? byName : fallback))
    purchaseCategoryList.value = (result || []).map((x: any) => String(x || '')).filter((x: string) => x)
  } catch {
    purchaseCategoryList.value = []
  }
}
const fetchData = async () => {
  loading.value = true
  try {
    const res:any = await getPurchaseRequestList({ current: currentPage.value, size: pageSize.value, requestNo: queryParams.requestNo })
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}
const resetQuery = () => {
  queryParams.requestNo = ''
  currentPage.value = 1
  fetchData()
}
const openEdit = async (id?: number) => {
    editId.value = id
    supplierWatchDisabled.value = true
    form.requestNo = ''
    form.requesterId = undefined
    form.requesterName = ''
    form.requestDate = ''
    form.deptId = undefined
    form.supplierId = undefined
    form.supplierName = '' // 供应商名称
    form.purchaseCategory = ''
    form.customerId = undefined
    form.customerName = ''
    form.contractId = undefined
    form.contractNo = ''
    form.contractName = ''
    form.purchaseStatus = '待采购'
    form.paymentTime = ''
    form.acceptanceTime = ''
    form.acceptanceRemark = ''
    form.remark = ''
    form.itemList = []
    form.totalAmount = 0
    
    if (id) {
      const res:any = await getPurchaseRequestById(id)
      // 保存合同信息
      const originalContractInfo = {
        contractId: res?.contractId,
        contractNo: res?.contractNo,
        contractName: res?.contractName
      }
      
      Object.assign(form, res || {})
      
      // 先加载客户列表（如果是项目采购需要）
      await fetchCustomers()
      // 如果后端没有返回supplierName，根据supplierId设置
      if (!form.supplierName && form.supplierId) {
        const supplier = supplierList.value.find(s => s.id === form.supplierId)
        form.supplierName = supplier?.supplierName || supplier?.name || supplier?.companyName || ''
      }
      // 将后端的status字段映射到前端的purchaseStatus字段
      if (res?.status) {
        form.purchaseStatus = res.status
      }
      // 确保付款时间为空时不会被自动填充
      if (!res?.paymentTime) {
        form.paymentTime = ''
      }
      if (!res?.acceptanceTime) {
        form.acceptanceTime = ''
      }
      if (!Array.isArray(form.itemList)) form.itemList = res?.itemList || []
      
      // 如果是项目采购，加载对应的合同
      if (form.purchaseCategory === '项目采购') {
        console.log('编辑模式 - 项目采购，客户ID:', form.customerId)
        if (form.customerId) {
          console.log('准备获取合同列表')
          await fetchContracts(form.customerId)
          console.log('合同列表获取完成:', contractList.value)
          
          // 检查合同列表中是否包含原始合同ID
          const contractExists = contractList.value.some(c => c.id === originalContractInfo.contractId)
          if (!contractExists && originalContractInfo.contractId) {
            // 如果不存在，尝试获取合同详情并添加到合同列表中
            try {
              const contractDetail = await getContractById(originalContractInfo.contractId)
              const contractData = contractDetail.data
              if (contractData) {
                // 添加到合同列表中
                contractList.value.push(contractData)
                console.log('合同已添加到列表:', contractData)
              }
            } catch (error) {
              console.error('获取合同详情失败:', error)
            }
          }
          
          // 重新设置合同信息，确保下拉框显示名称而不是ID
          form.contractId = originalContractInfo.contractId
          form.contractNo = originalContractInfo.contractNo
          form.contractName = originalContractInfo.contractName
        }
      }
      await fetchProducts(form.supplierId)
      try {
        const parsed = res?.contractAttachments ? JSON.parse(res.contractAttachments) : []
        contractAttachments.value = Array.isArray(parsed) ? parsed : []
      } catch { contractAttachments.value = [] }
      try {
        const pv = res?.paymentVoucher ? JSON.parse(res.paymentVoucher) : []
        paymentVoucherList.value = Array.isArray(pv) ? pv : []
      } catch { paymentVoucherList.value = [] }
    } else {
      form.requestDate = new Date().toISOString().split('T')[0]
      if (!userStore.userInfo) {
        try { await userStore.getInfo() } catch {}
      }
      const u: any = userStore.userInfo || {}
      form.requesterId = u.id
      form.requesterName = u.name || u.realName || u.nickname || u.username || ''
      form.deptId = u.deptId
      contractAttachments.value = []
      paymentVoucherList.value = []
      // 新增模式下也加载客户列表（加载所有客户）
      await fetchCustomers()
    }
    supplierWatchDisabled.value = false
    dialogVisible.value = true
  }
const onProductChange = (productId:number, row:any) => {
  const p = productList.value.find((x:any)=>x.id === productId)
  if (p) {
    row.productName = p.productName
    row.productCode = p.productCode
    row.productSpec = p.productSpec
    row.unitPrice = Number(p.costPrice || 0)
    if (row.purchasePrice == null) row.purchasePrice = Number(p.costPrice || 0)
    if (row.purchaseQuantity == null) row.purchaseQuantity = 1
  }
}

const openItemDialog = (index?: number) => {
  if (!form.supplierId) {
    ElMessage.warning('请先选择供应商')
    return
  }
  editingItemIndex.value = (typeof index === 'number') ? index : -1
  const src = (editingItemIndex.value >= 0) ? (form.itemList[editingItemIndex.value] || {}) : {}
  itemForm.productId = src.productId
  itemForm.productName = src.productName || ''
  itemForm.productCode = src.productCode || ''
  itemForm.productSpec = src.productSpec || ''
  itemForm.purchaseQuantity = src.purchaseQuantity ?? 1
  itemForm.unitPrice = src.unitPrice ?? 0
  itemForm.purchasePrice = src.purchasePrice ?? 0
  // itemForm.arrivalQuantity = src.arrivalQuantity ?? 0  // 暂时隐藏
  // itemForm.acceptQuantity = src.acceptQuantity ?? 0    // 暂时隐藏
  itemForm.shipDate = src.shipDate || ''
  itemForm.expectedDate = src.expectedDate || ''
  itemDialogVisible.value = true
}

const confirmItem = () => {
    if (!itemForm.productId) {
      ElMessage.warning('请选择商品')
      return
    }
    // 验证数量不能为0或负数
    if (!itemForm.purchaseQuantity || Number(itemForm.purchaseQuantity) <= 0) {
      ElMessage.warning('数量必须大于0')
      return
    }
    // 验证采购价格不能为0或负数
    if (Number(itemForm.purchasePrice) <= 0) {
      ElMessage.warning('采购价格必须大于0')
      return
    }
    // 验证到货日期不能早于发货日期
    if (itemForm.shipDate && itemForm.expectedDate) {
      const shipDate = new Date(itemForm.shipDate)
      const expectedDate = new Date(itemForm.expectedDate)
      if (expectedDate < shipDate) {
        ElMessage.warning('到货日期不能早于发货日期')
        return
      }
    }
    // 到货数量和验收数量验证暂时隐藏
    /*
    // 验证到货数量不能为负数
    if (itemForm.arrivalQuantity < 0) {
      ElMessage.warning('到货数量不能为负数')
      return
    }
    // 验证验收数量不能为负数
    if (itemForm.acceptQuantity < 0) {
      ElMessage.warning('验收数量不能为负数')
      return
    }
    // 验证验收数量不能超过到货数量
    if (itemForm.acceptQuantity > itemForm.arrivalQuantity) {
      ElMessage.warning('验收数量不能超过到货数量')
      return
    }
    */
    const row = {
      productId: itemForm.productId,
      productName: itemForm.productName,
      productCode: itemForm.productCode,
      productSpec: itemForm.productSpec,
      purchaseQuantity: itemForm.purchaseQuantity,
      unitPrice: itemForm.unitPrice,
      purchasePrice: itemForm.purchasePrice,
      // arrivalQuantity: itemForm.arrivalQuantity,  // 暂时隐藏
      // acceptQuantity: itemForm.acceptQuantity,    // 暂时隐藏
      shipDate: itemForm.shipDate,
      expectedDate: itemForm.expectedDate
    }
    if (editingItemIndex.value >= 0) {
      form.itemList.splice(editingItemIndex.value, 1, row)
    } else {
      form.itemList.push(row)
    }
    itemDialogVisible.value = false
  }

const addItem = () => {
  openItemDialog()
}

const removeItem = (index:number) => {
  form.itemList.splice(index, 1)
}
const calcTotalAmount = computed(() => {
  const total = (form.itemList || []).reduce((sum:number, it:any) => {
    const price = Number(it.purchasePrice || 0)
    return sum + price * Number(it.purchaseQuantity || 0)
  }, 0)
  form.totalAmount = Number(total.toFixed(2))
  return form.totalAmount.toFixed(2)
})

// 保存按钮处理函数 - 只保存数据，不修改状态
const handleSave = async () => {
  if (!form.requesterName) { ElMessage.warning('申请人不能为空'); return }
  if (!form.itemList || form.itemList.length === 0) { ElMessage.warning('请添加申请明细'); return }
  for (const it of form.itemList) {
    if (!it.productName || !it.productCode) {
      const p = productList.value.find((x:any)=>x.id === it.productId)
      if (p) {
        it.productName = p.productName
        it.productCode = p.productCode
        it.productSpec = p.productSpec
      }
    }
  }
  try {
    if (editId.value) {
      // 更新时保持原有状态不变
      // 确保付款时间和验收时间为空时传递null而不是空字符串
      const saveData = {
        id: editId.value,
        requestNo: form.requestNo,
        requesterId: form.requesterId,
        requesterName: form.requesterName,
        deptId: form.deptId,
        requestDate: form.requestDate,
        supplierId: form.supplierId,
        supplierName: form.supplierName, // 供应商名称
        purchaseCategory: form.purchaseCategory,
        customerId: form.customerId,
        customerName: form.customerName,
        contractId: form.contractId,
        contractNo: form.contractNo,
        contractName: form.contractName,
        status: form.purchaseStatus,
        remark: form.remark,
        totalAmount: form.totalAmount,
        itemList: form.itemList,
        contractAttachments: JSON.stringify(contractAttachments.value),
        paymentTime: form.paymentTime || null,
        paymentVoucher: JSON.stringify(paymentVoucherList.value),
        acceptanceTime: form.acceptanceTime || null,
        acceptanceRemark: form.acceptanceRemark
      }
      await updatePurchaseRequest(saveData)
      ElMessage.success('保存成功')
    } else {
      // 新增时排除到货数量、验收数量和采购状态字段，让后端设置默认值
      const cleanItemList = form.itemList.map((item: any) => {
        const { arrivalQuantity, acceptQuantity, ...cleanItem } = item
        return cleanItem
      })
      
      // 确保付款时间和验收时间为空时传递null而不是空字符串
      const saveData = {
        requesterId: form.requesterId,
        requesterName: form.requesterName,
        deptId: form.deptId,
        requestDate: form.requestDate,
        supplierId: form.supplierId,
        supplierName: form.supplierName, // 供应商名称
        purchaseCategory: form.purchaseCategory,
        customerId: form.customerId,
        customerName: form.customerName,
        contractId: form.contractId,
        contractNo: form.contractNo,
        contractName: form.contractName,
        remark: form.remark,
        totalAmount: form.totalAmount,
        itemList: cleanItemList,
        contractAttachments: JSON.stringify(contractAttachments.value),
        paymentTime: form.paymentTime || null,
        paymentVoucher: JSON.stringify(paymentVoucherList.value),
        acceptanceTime: form.acceptanceTime || null,
        acceptanceRemark: form.acceptanceRemark
      }
      
      await savePurchaseRequest(saveData)
      ElMessage.success('保存成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e:any) {}
}

// 提交按钮处理函数 - 根据数据变化自动判断状态
const handleSubmit = async () => {
    if (!form.supplierId) { ElMessage.warning('请选择供应商'); return }
    // 项目采购验证
    if (form.purchaseCategory === '项目采购') {
      if (!form.customerId) { ElMessage.warning('请选择客户'); return }
      if (!form.contractId) { ElMessage.warning('请选择合同订单'); return }
    }
    if (!form.requesterName) { ElMessage.warning('申请人不能为空'); return }
    if (!form.itemList || form.itemList.length === 0) { ElMessage.warning('请添加申请明细'); return }
    for (const it of form.itemList) {
      if (!it.productName || !it.productCode) {
        const p = productList.value.find((x:any)=>x.id === it.productId)
        if (p) {
          it.productName = p.productName
          it.productCode = p.productCode
          it.productSpec = p.productSpec
        }
      }
    }
    
    // 自动判断采购状态
    let autoPurchaseStatus = form.purchaseStatus || '待采购'
    
    // 1. 根据付款时间判断状态
    if (form.paymentTime) {
      autoPurchaseStatus = '采购付款'
    }
    
    // 2. 根据发货日期判断状态
    const hasShipDateCount = form.itemList.filter((item: any) => item.shipDate).length
    if (hasShipDateCount > 0) {
      if (hasShipDateCount === form.itemList.length) {
        autoPurchaseStatus = '全部发货'
      } else {
        autoPurchaseStatus = '部分发货'
      }
    }
    
    // 3. 根据到货日期判断状态
    const hasExpectedDateCount = form.itemList.filter((item: any) => item.expectedDate).length
    if (hasExpectedDateCount > 0) {
      if (hasExpectedDateCount === form.itemList.length) {
        autoPurchaseStatus = '全部到库'
      } else {
        autoPurchaseStatus = '部分到库'
      }
    }
    
    try {
      if (editId.value) {
        // 编辑时排除到货数量、验收数量字段，避免后端报错
        const cleanItemList = form.itemList.map((item: any) => {
          const { arrivalQuantity, acceptQuantity, ...cleanItem } = item
          return cleanItem
        })
        
        // 确保付款时间和验收时间为空时传递null而不是空字符串
        const submitData = {
          id: editId.value,
          requestNo: form.requestNo,
          requesterId: form.requesterId,
          requesterName: form.requesterName,
          deptId: form.deptId,
          requestDate: form.requestDate,
          supplierId: form.supplierId,
          supplierName: form.supplierName, // 供应商名称
          purchaseCategory: form.purchaseCategory,
          customerId: form.customerId,
          customerName: form.customerName,
          contractId: form.contractId,
          contractNo: form.contractNo,
          contractName: form.contractName,
          status: autoPurchaseStatus,
          remark: form.remark,
          totalAmount: form.totalAmount,
          itemList: cleanItemList,
          contractAttachments: JSON.stringify(contractAttachments.value),
          paymentTime: form.paymentTime || null,
          paymentVoucher: JSON.stringify(paymentVoucherList.value),
          acceptanceTime: form.acceptanceTime || null,
          acceptanceRemark: form.acceptanceRemark
        }
        
        await updatePurchaseRequest(submitData)
        ElMessage.success('提交成功')
      } else {
        // 新增时排除到货数量、验收数量和采购状态字段，让后端设置默认值
        const cleanItemList = form.itemList.map((item: any) => {
          const { arrivalQuantity, acceptQuantity, ...cleanItem } = item
          return cleanItem
        })
        
        // 确保付款时间和验收时间为空时传递null而不是空字符串
        const submitData = {
          requesterId: form.requesterId,
          requesterName: form.requesterName,
          deptId: form.deptId,
          requestDate: form.requestDate,
          supplierId: form.supplierId,
          supplierName: form.supplierName, // 供应商名称
          purchaseCategory: form.purchaseCategory,
          customerId: form.customerId,
          customerName: form.customerName,
          contractId: form.contractId,
          contractNo: form.contractNo,
          contractName: form.contractName,
          status: autoPurchaseStatus,
          remark: form.remark,
          totalAmount: form.totalAmount,
          itemList: cleanItemList,
          contractAttachments: JSON.stringify(contractAttachments.value),
          paymentTime: form.paymentTime || null,
          paymentVoucher: JSON.stringify(paymentVoucherList.value),
          acceptanceTime: form.acceptanceTime || null,
          acceptanceRemark: form.acceptanceRemark
        }
        
        await savePurchaseRequest(submitData)
        ElMessage.success('提交成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (e:any) {}
  }
const handleDelete = (row:any) => {
  ElMessageBox.confirm('确认删除该申请吗？', '提示', { type: 'warning' }).then(async () => {
    await deletePurchaseRequest(row.id)
    ElMessage.success('删除成功')
    fetchData()
  })
}

// 锁单功能
const handleLock = (row: any) => {
  ElMessageBox.confirm('确认要锁单吗？锁单后将无法修改和删除。', '提示', { type: 'warning' }).then(async () => {
    try {
      await lockPurchaseRequest(row.id)
      ElMessage.success('锁单成功')
      fetchData()
    } catch (error: any) {
      ElMessage.error(error?.message || '锁单失败')
    }
  })
}

// 解锁功能
const handleUnlock = (row: any) => {
  ElMessageBox.confirm('确认要解锁吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await unlockPurchaseRequest(row.id)
      ElMessage.success('解锁成功')
      fetchData()
    } catch (error: any) {
      ElMessage.error(error?.message || '解锁失败')
    }
  })
}

// 审核功能
const handleAudit = (row: any) => {
  currentAuditRow.value = row
  auditForm.status = 'PASSED'
  auditForm.detail = ''
  auditDialogVisible.value = true
}

// 确认审核
const confirmAudit = async () => {
  if (!auditForm.status) {
    ElMessage.warning('请选择审核结果')
    return
  }
  
  auditLoading.value = true
  try {
    await auditPurchaseRequest(currentAuditRow.value.id, auditForm.status, auditForm.detail)
    ElMessage.success(auditForm.status === 'PASSED' ? '审核通过' : '审核驳回')
    auditDialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error?.message || '审核失败')
  } finally {
    auditLoading.value = false
  }
}
watch(() => form.supplierId, async (val, oldVal) => {
  if (supplierWatchDisabled.value) return
  if (val === oldVal) return
  form.itemList = []
  await fetchProducts(val)
  
  // 根据供应商ID设置供应商名称
  if (val) {
    const supplier = supplierList.value.find(s => s.id === val)
    form.supplierName = supplier?.supplierName || supplier?.name || supplier?.companyName || ''
  } else {
    form.supplierName = ''
  }
})

// removed contract linking logic

onMounted(async () => {
  await fetchSuppliers()
  await fetchCustomers()
  await fetchPurchaseCategories()
  fetchData()
})
</script>
<style scoped>
.app-container { padding: 20px; }
.header-actions { display:flex; justify-content: space-between; align-items:center; margin-bottom: 12px; }
.pagination-container { margin-top: 12px; display:flex; justify-content:flex-end; }
.detail-header { display:flex; justify-content:space-between; align-items:center; font-weight: 600; margin: 10px 0; }
.total-amount { text-align: right; margin-top: 8px; font-weight: 600; }
</style>
