<template>
  <div class="app-container">
    <div class="header-actions">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="合同编号">
          <el-input v-model="queryParams.contractNo" placeholder="请输入合同编号" clearable @keyup.enter="fetchData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="openEdit()">新增合同</el-button>
    </div>
    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="contractNo" label="合同编号" width="180" />
      <el-table-column prop="contractName" label="合同名称" width="200" />
      <el-table-column prop="supplierId" label="供应商" width="160">
        <template #default="scope">
          {{ getSupplierName(scope.row.supplierId) }}
        </template>
      </el-table-column>
      <el-table-column prop="contractAmount" label="合同金额" width="140">
        <template #default="scope">¥{{ Number(scope.row.contractAmount||0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="auditStatus" label="审核状态" width="120">
        <template #default="scope">{{ auditStatusText(scope.row.auditStatus) }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openEdit(scope.row.id)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
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
    <el-dialog v-model="dialogVisible" :title="editId ? '编辑采购合同' : '新增采购合同'" width="900px">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="合同编号">
              <el-input v-model="form.contractNo" placeholder="系统自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同名称">
              <el-input v-model="form.contractName" placeholder="请输入合同名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商" required>
              <el-select v-model="form.supplierId" filterable style="width: 100%">
                <el-option v-for="s in supplierList" :key="s.id" :label="s.supplierName || s.name || s.companyName" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联申请">
              <el-select v-model="form.requestId" placeholder="选择采购申请" filterable style="width: 100%" @change="onRequestChange">
                <el-option v-for="r in requestList" :key="r.id" :label="r.requestNo" :value="r.id" />
              </el-select>
              <el-button link type="primary" @click="createFromRequest">从申请生成</el-button>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
        <div class="detail-header">
          <span>合同明细</span>
          <el-button type="primary" link @click="addItem">添加商品</el-button>
        </div>
        <el-table :data="form.detailList" border>
          <el-table-column label="商品" width="240">
            <template #default="scope">
              <el-input v-model="scope.row.productName" placeholder="商品名称" />
            </template>
          </el-table-column>
          <el-table-column prop="productCode" label="编号" width="140" />
          <el-table-column prop="productSpec" label="规格" width="160" />
          <el-table-column label="数量" width="120">
            <template #default="scope">
              <el-input-number v-model="scope.row.quantity" :min="1" />
            </template>
          </el-table-column>
          <el-table-column label="单价" width="140">
            <template #default="scope">
              <el-input-number v-model="scope.row.unitPrice" :min="0" :precision="2" :step="0.1" />
            </template>
          </el-table-column>
          <el-table-column label="金额" width="140">
            <template #default="scope">
              ¥{{ calcRowAmount(scope.row) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button link type="danger" @click="removeItem(scope.$index)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="total-amount">合同金额：¥{{ calcTotalAmount }}</div>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPurchaseContractList, getPurchaseContractById, savePurchaseContract, updatePurchaseContract, deletePurchaseContract, createPurchaseContractFromRequest } from '@/api/purchase'
import { getSupplierList } from '@/api/supplier'
import { getPurchaseRequestList, getPurchaseRequestById } from '@/api/purchase'

const loading = ref(false)
const tableData = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const editId = ref<number|undefined>(undefined)
const queryParams = reactive({ contractNo: '' })
const form = reactive<any>({ contractNo: '', contractName: '', supplierId: undefined, requestId: undefined, remark: '', detailList: [], contractAmount: 0 })
const supplierList = ref<any[]>([])
const requestList = ref<any[]>([])

const fetchSuppliers = async () => {
  const res:any = await getSupplierList({ current: 1, size: 1000 })
  supplierList.value = res.records || res || []
}
const fetchRequests = async () => {
  const res:any = await getPurchaseRequestList({ current: 1, size: 1000 })
  requestList.value = res.records || res || []
}
const getSupplierName = (id?: number) => {
  if (!id) return ''
  const s = (supplierList.value || []).find((x:any)=>x.id === id)
  return s?.supplierName || s?.name || s?.companyName || id
}
const auditStatusText = (val: any) => {
  const v = String(val || '').toUpperCase()
  const map: Record<string, string> = { PENDING: '待审核', UNDER_REVIEW: '审核中', PASSED: '已通过', REJECTED: '已驳回' }
  return map[v] || v || '-'
}
const fetchData = async () => {
  loading.value = true
  try {
    const res:any = await getPurchaseContractList({ current: currentPage.value, size: pageSize.value, contractNo: queryParams.contractNo })
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}
const resetQuery = () => {
  queryParams.contractNo = ''
  currentPage.value = 1
  fetchData()
}
const openEdit = async (id?: number) => {
  editId.value = id
  form.contractNo = ''
  form.contractName = ''
  form.supplierId = undefined
  form.requestId = undefined
  form.remark = ''
  form.detailList = []
  form.contractAmount = 0
  if (id) {
    const res:any = await getPurchaseContractById(id)
    Object.assign(form, res || {})
    if (!Array.isArray(form.detailList)) form.detailList = res?.detailList || []
  }
  dialogVisible.value = true
}
const addItem = () => {
  form.detailList.push({ productName: '', productCode: '', productSpec: '', quantity: 1, unitPrice: 0 })
}
const removeItem = (index:number) => {
  form.detailList.splice(index, 1)
}
const calcRowAmount = (row:any) => {
  const n = Number(row.unitPrice || 0) * Number(row.quantity || 0)
  return n.toFixed(2)
}
const calcTotalAmount = computed(() => {
  const total = (form.detailList || []).reduce((sum:number, it:any) => sum + Number(it.unitPrice || 0) * Number(it.quantity || 0), 0)
  form.contractAmount = Number(total.toFixed(2))
  return form.contractAmount.toFixed(2)
})
const onRequestChange = async (id:number) => {
  if (!id) return
  const req:any = await getPurchaseRequestById(id)
  form.supplierId = req?.supplierId || form.supplierId
  const items:any[] = req?.itemList || []
  form.detailList = items.map((it:any) => ({
    requestItemId: it.id,
    productName: it.productName,
    productCode: it.productCode,
    productSpec: it.productSpec,
    quantity: it.purchaseQuantity || 1,
    unitPrice: it.purchasePrice || 0
  }))
}
const createFromRequest = async () => {
  if (!form.requestId) { ElMessage.warning('请选择采购申请'); return }
  await createPurchaseContractFromRequest(Number(form.requestId))
  dialogVisible.value = false
  ElMessage.success('已从采购申请生成合同')
  fetchData()
}
const handleSubmit = async () => {
  if (!form.supplierId) { ElMessage.warning('请选择供应商'); return }
  if (!form.contractName) { ElMessage.warning('请输入合同名称'); return }
  if (!form.detailList || form.detailList.length === 0) { ElMessage.warning('请添加合同明细'); return }
  try {
    if (editId.value) {
      await updatePurchaseContract({ id: editId.value, contractNo: form.contractNo, contractName: form.contractName, supplierId: form.supplierId, requestId: form.requestId, remark: form.remark, contractAmount: form.contractAmount, detailList: form.detailList })
      ElMessage.success('更新成功')
    } else {
      await savePurchaseContract({ contractNo: form.contractNo, contractName: form.contractName, supplierId: form.supplierId, requestId: form.requestId, remark: form.remark, contractAmount: form.contractAmount, detailList: form.detailList })
      ElMessage.success('保存成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e:any) {}
}
const handleDelete = (row:any) => {
  ElMessageBox.confirm('确认删除该合同吗？', '提示', { type: 'warning' }).then(async () => {
    await deletePurchaseContract(row.id)
    ElMessage.success('删除成功')
    fetchData()
  })
}
onMounted(async () => {
  await fetchSuppliers()
  await fetchRequests()
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
