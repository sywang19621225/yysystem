<template>
  <div class="app-container" v-if="inbound">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2 style="margin: 0; font-size: 18px;">入库管理</h2>
          <div style="display: flex; gap: 8px;">
            <el-button @click="goBack" size="small">返回主表</el-button>
            <el-button type="primary" @click="saveMaster" size="small">保存单据</el-button>
          </div>
        </div>
      </template>
      <el-form :model="inbound" label-width="120px" class="master-form" :rules="formRules" ref="masterFormRef">
        <!-- 顶部：入库单信息 -->
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="入库编号"><el-input v-model="inbound.inboundNo" placeholder="系统自动生成" disabled /></el-form-item></el-col>
          <el-col :span="8">
            <el-form-item label="入库分类" required>
              <el-select v-model="inbound.inboundType" placeholder="请选择入库分类" style="width: 100%" @change="handleInboundTypeChange">
                <el-option label="采购入库（合肥仓库）" value="PURCHASE" />
                <el-option label="外转入库（外转仓库）" value="TRANSFER" />
                <el-option label="销售退货" value="SALES_RETURN" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="sourceLabel">
              <el-select v-model="sourceId" :placeholder="sourcePlaceholder" style="width: 100%" @change="handleSourceChange" filterable>
                <el-option
                  v-for="item in sourceList"
                  :key="item.id"
                  :label="item.label"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="供应商名称"><el-input v-model="inbound.supplierName" placeholder="请输入供应商名称" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="采购单编号"><el-input v-model="inbound.purchaseOrderNo" placeholder="采购申请表的采购编码" disabled /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="源单合计金额"><el-input :model-value="formatCurrency(sourceData?.totalAmount || sourceData?.totalSales || 0)" disabled /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="仓库"><el-select v-model="inbound.warehouseId" placeholder="请选择仓库" style="width: 100%">
            <el-option
              v-for="warehouse in warehouseList"
              :key="warehouse.id"
              :label="warehouse.warehouseName"
              :value="warehouse.id"
            />
          </el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="入库时间"><el-date-picker v-model="inbound.inboundTime" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm:ss" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="客户名称" v-if="inbound.inboundType === 'SALES_RETURN'"><el-input v-model="inbound.customerName" placeholder="销售退货的客户" disabled /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="inbound.remark" type="textarea" /></el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:16px;">
      <template #header>
        <div class="card-header">
          <span>入库明细</span>
          <el-button type="primary" size="small" @click="openAddDetail">新增明细</el-button>
        </div>
      </template>
      <el-table :data="details" border stripe v-loading="loading" style="width: 100%" :scrollbar-always-on="true" table-layout="fixed">
        <el-table-column prop="productName" label="商品名称" min-width="160" />
        <el-table-column prop="productCode" label="商品编号" width="140" />
        <el-table-column prop="productSpec" label="型号" min-width="120" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="costPrice" label="成本价" width="120">
          <template #default="scope">{{ formatCurrency(scope.row.costPrice) }}</template>
        </el-table-column>
        <el-table-column prop="costAmount" label="成本金额" width="120">
          <template #default="scope">{{ formatCurrency(scope.row.costAmount) }}</template>
        </el-table-column>
        <el-table-column prop="productUnit" label="单位" width="100" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="scope">
            <div style="display:flex; align-items:center; gap:10px; flex-wrap:nowrap; white-space:nowrap;">
              <el-button type="primary" link @click="openEditDetail(scope.row)">修改</el-button>
              <el-button type="danger" link @click="handleDeleteDetail(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
  <el-dialog v-model="detailDialogVisible" title="新增入库明细" width="600px">
    <el-form :model="detailForm" label-width="120px">
      <el-form-item label="商品编号"><el-input v-model="detailForm.productCode" /></el-form-item>
      <el-form-item label="商品名称"><el-input v-model="detailForm.productName" /></el-form-item>
      <el-form-item label="商品规格"><el-input v-model="detailForm.productSpec" /></el-form-item>
      <el-form-item label="数量"><el-input v-model="detailForm.quantity" type="number" /></el-form-item>
      <el-form-item label="成本价"><el-input v-model="detailForm.costPrice" type="number" placeholder="请输入成本价" /></el-form-item>
      <el-form-item label="单位"><el-input v-model="detailForm.productUnit" placeholder="请输入单位" /></el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="detailDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitAddDetail">确定</el-button>
      </span>
    </template>
  </el-dialog>
  <el-dialog v-model="editDetailDialogVisible" title="编辑入库明细" width="600px">
    <el-form :model="editDetailForm" label-width="120px">
      <el-form-item label="商品编号"><el-input v-model="editDetailForm.productCode" /></el-form-item>
      <el-form-item label="商品名称"><el-input v-model="editDetailForm.productName" /></el-form-item>
      <el-form-item label="商品规格"><el-input v-model="editDetailForm.productSpec" /></el-form-item>
      <el-form-item label="数量"><el-input v-model="editDetailForm.quantity" type="number" /></el-form-item>
      <el-form-item label="成本价"><el-input v-model="editDetailForm.costPrice" type="number" placeholder="请输入成本价" /></el-form-item>
      <el-form-item label="单位"><el-input v-model="editDetailForm.productUnit" placeholder="请输入单位" /></el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="editDetailDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitEditDetail">保存</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getInboundById, updateInbound, getInboundDetails, updateInboundDetail, addInboundDetail, deleteInboundDetail, createInbound } from '@/api/inbound'
import { getPurchaseRequestById, getPurchaseRequestList } from '@/api/purchase'
import { getOutboundList, getOutboundById } from '@/api/outbound'
import { getWarehouseList } from '@/api/warehouse'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
let id = Number(route.query.id || route.params.id)
let purchaseRequestId = Number(route.query.purchaseRequestId || 0)
const loading = ref(false)
const inbound = ref<any>(null)
const details = ref<any[]>([])
const sourceData = ref<any>(null) // 源单数据（采购申请或销售出货）
const detailDialogVisible = ref(false)
const detailForm = ref<any>({ productCode: '', productName: '', productSpec: '', quantity: 1, costPrice: 0, productUnit: '' })
const editDetailDialogVisible = ref(false)
const editDetailForm = ref<any>({})
const isDirty = ref(false)
const initialSnapshot = ref<string>('')

// 仓库列表
const warehouseList = ref<any[]>([])
// 源单列表（采购申请表或销售出货单）
const sourceList = ref<any[]>([])
// 当前选择的源单ID
const sourceId = ref<number | undefined>(undefined)

// 计算属性：根据入库类型显示不同的标签
const sourceLabel = computed(() => {
  const type = inbound.value?.inboundType
  if (type === 'SALES_RETURN') return '选择销售出货单'
  return '选择采购申请表'
})

const sourcePlaceholder = computed(() => {
  const type = inbound.value?.inboundType
  if (type === 'SALES_RETURN') return '请选择销售出货单'
  return '请选择采购申请表'
})

const formRules = {
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  inboundTime: [{ required: true, message: '请选择入库时间', trigger: 'change' }]
}
const masterFormRef = ref()

// 获取仓库列表
const fetchWarehouseList = async () => {
  try {
    const res: any = await getWarehouseList({ current: 1, size: 100 })
    warehouseList.value = res.records || []
  } catch (error) {
    ElMessage.error('获取仓库列表失败')
    console.error('获取仓库列表失败:', error)
  }
}

// 获取采购申请表列表
const fetchPurchaseRequestList = async () => {
  try {
    const res: any = await getPurchaseRequestList({ current: 1, size: 100 })
    // 过滤出状态为到货的采购申请表（部分到货或全部到货）
    const filteredList = res.records.filter((item: any) => {
      // 检查采购申请表是否有商品明细，并且有商品已到货
      if (!item.itemList || item.itemList.length === 0) {
        return false
      }
      // 检查是否有商品的到货数量大于0
      return item.itemList.some((product: any) => {
        return product.arrivalQuantity !== null && product.arrivalQuantity > 0
      })
    })
    
    // 格式化列表数据
    sourceList.value = filteredList.map((item: any) => ({
      ...item,
      label: `${item.requestNo} - ${item.supplierName || '未知供应商'}`
    }))
  } catch (error) {
    ElMessage.error('获取采购申请表列表失败')
    console.error('获取采购申请表列表失败:', error)
  }
}

// 获取销售出货单列表
const fetchOutboundList = async () => {
  try {
    const res: any = await getOutboundList({ current: 1, size: 100, auditStatus: 'PASSED' })
    // 格式化列表数据
    sourceList.value = (res.records || []).map((item: any) => ({
      ...item,
      label: `${item.outStockNo} - ${item.customerName || '未知客户'}`
    }))
  } catch (error) {
    ElMessage.error('获取销售出货单列表失败')
    console.error('获取销售出货单列表失败:', error)
  }
}

// 处理入库分类变更
const handleInboundTypeChange = async (type: string) => {
  // 清空源单选择
  sourceId.value = undefined
  sourceData.value = null
  inbound.value.supplierName = ''
  inbound.value.purchaseOrderNo = ''
  inbound.value.customerName = ''
  details.value = []
  
  // 根据入库类型加载不同的源单列表
  if (type === 'SALES_RETURN') {
    // 销售退货：加载销售出货单
    await fetchOutboundList()
  } else {
    // 采购入库和外转入库：加载采购申请表
    await fetchPurchaseRequestList()
  }
}

// 处理源单选择事件
const handleSourceChange = async (sourceId: number) => {
  if (!sourceId) {
    return
  }
  
  try {
    const type = inbound.value?.inboundType
    
    if (type === 'SALES_RETURN') {
      // 销售退货：获取销售出货单详情
      const res: any = await getOutboundById(sourceId)
      const outbound = res?.data || res
      if (outbound) {
        sourceData.value = outbound
        inbound.value.customerName = outbound.customerName || ''
        inbound.value.supplierName = outbound.supplierName || ''
        inbound.value.purchaseOrderNo = outbound.outStockNo || ''
        
        // 自动带入销售出货明细作为退货明细
        if (outbound.detailList && outbound.detailList.length > 0) {
          details.value = outbound.detailList.map((item: any) => ({
            productCode: item.productCode,
            productName: item.productName,
            productSpec: item.productSpec,
            productUnit: item.productUnit,
            quantity: item.outQuantity,
            costPrice: item.costPrice || 0,
            costAmount: (item.costPrice || 0) * (item.outQuantity || 0)
          }))
        }
      }
    } else {
      // 采购入库或外转入库：获取采购申请表详情
      const res: any = await getPurchaseRequestById(sourceId)
      const request = res?.data || res
      if (request) {
        sourceData.value = request
        inbound.value.supplierName = request.supplierName || ''
        inbound.value.purchaseOrderNo = request.requestNo || ''
        
        // 自动带入采购申请明细
        if (request.itemList && request.itemList.length > 0) {
          details.value = request.itemList.map((item: any) => ({
            productCode: item.productCode,
            productName: item.productName,
            productSpec: item.productSpec,
            productUnit: item.productUnit,
            quantity: item.purchaseQuantity,
            costPrice: item.purchasePrice || 0,
            costAmount: (item.purchasePrice || 0) * (item.purchaseQuantity || 0)
          }))
        }
      }
    }
  } catch (error) {
    ElMessage.error('获取源单详情失败')
    console.error('获取源单详情失败:', error)
  }
}

const formatCurrency = (v:any) => {
  const n = Number(v || 0)
  return `${n.toFixed(2)} 元`
}

const loadData = async () => {
  loading.value = true
  try {
    // 获取仓库列表
    await fetchWarehouseList()
    
    if (!Number.isFinite(id) && Number.isFinite(purchaseRequestId)) {
      // 从采购申请表创建入库单
      const pr:any = await getPurchaseRequestById(purchaseRequestId)
      sourceData.value = pr?.data || pr
      
      // 初始化入库单
      inbound.value = {
        inboundType: 'PURCHASE',
        inboundNo: '', // 入库编号，由后端生成
        warehouseId: '',
        totalCost: 0,
        inboundTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
        auditStatus: 'PENDING',
        remark: '',
        supplierName: sourceData.value?.supplierName || '', // 自动带入供应商名称
        purchaseOrderNo: sourceData.value?.purchaseOrderNo || '' // 调用采购申请表的采购编码
      }
      sourceId.value = purchaseRequestId
      details.value = []
      
      // 加载采购申请表列表
      await fetchPurchaseRequestList()
      return
    }
    
    if (!Number.isFinite(id)) {
      // 新建状态，设置合理的初始值
      inbound.value = {
        inboundType: 'PURCHASE',
        inboundNo: '', // 入库编号，由后端生成
        warehouseId: '',
        totalCost: 0,
        inboundTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
        auditStatus: 'PENDING',
        remark: '',
        supplierName: '',
        purchaseOrderNo: '',
        customerName: ''
      }
      details.value = []
      sourceData.value = null
      sourceId.value = undefined
      
      // 默认加载采购申请表列表
      await fetchPurchaseRequestList()
      return
    }
    
    const master:any = await getInboundById(id)
    inbound.value = master?.data || master
    const det:any = await getInboundDetails(id)
    details.value = Array.isArray(det) ? det : (det?.records || det?.data || [])
    
    // 根据入库类型加载对应的源单列表
    if (inbound.value?.inboundType === 'SALES_RETURN') {
      await fetchOutboundList()
      // 如果有源单ID，加载源单信息
      if (inbound.value?.purchaseRequestId) {
        sourceId.value = inbound.value.purchaseRequestId
        const pr:any = await getOutboundById(inbound.value.purchaseRequestId)
        sourceData.value = pr?.data || pr
        // 如果当前源单不在列表中，添加到列表
        const exists = sourceList.value.some((item: any) => item.id === sourceId.value)
        if (!exists && sourceData.value) {
          sourceList.value.unshift({
            ...sourceData.value,
            label: `${sourceData.value.outStockNo} - ${sourceData.value.customerName || '未知客户'}`
          })
        }
      }
    } else {
      await fetchPurchaseRequestList()
      // 如果有源单ID，加载源单信息
      if (inbound.value?.purchaseRequestId) {
        sourceId.value = inbound.value.purchaseRequestId
        const pr:any = await getPurchaseRequestById(inbound.value.purchaseRequestId)
        sourceData.value = pr?.data || pr
        // 如果当前源单不在列表中，添加到列表
        const exists = sourceList.value.some((item: any) => item.id === sourceId.value)
        if (!exists && sourceData.value) {
          sourceList.value.unshift({
            ...sourceData.value,
            label: `${sourceData.value.requestNo} - ${sourceData.value.supplierName || '未知供应商'}`
          })
        }
      }
    }
  } finally {
    loading.value = false
    isDirty.value = false
    createSnapshot()
  }
}

// 创建当前状态快照
const createSnapshot = () => {
  const state = {
    inbound: JSON.parse(JSON.stringify(inbound.value || {})),
    details: JSON.parse(JSON.stringify(details.value || []))
  }
  initialSnapshot.value = JSON.stringify(state)
}

// 检查是否真的修改了内容
const checkIfReallyDirty = () => {
  if (!initialSnapshot.value) return false
  const currentState = {
    inbound: JSON.parse(JSON.stringify(inbound.value || {})),
    details: JSON.parse(JSON.stringify(details.value || []))
  }
  const currentSnapshot = JSON.stringify(currentState)
  return currentSnapshot !== initialSnapshot.value
}

const saveMaster = async () => {
  // 表单验证
  if (!masterFormRef.value) {
    ElMessage.error('表单验证失败')
    return
  }
  
  try {
    await masterFormRef.value.validate()
  } catch (error) {
    ElMessage.error('请填写所有必填项')
    return
  }

  const backPath = getBackPath()
  if (!Number.isFinite(id)) {
    // 新建状态
    const res:any = await createInbound({
      ...inbound.value,
      purchaseRequestId: sourceId.value,
      detailList: details.value
    })
    const newId = res?.id || res?.data || res
    id = Number(newId)
    ElMessage.success('已保存并返回列表')
    isDirty.value = false
    createSnapshot() // 保存成功后更新快照
    router.push(backPath)
  } else {
    // 编辑状态
    await updateInbound(id, {
      ...inbound.value,
      detailList: details.value
    })
    ElMessage.success('已保存并返回列表')
    isDirty.value = false
    createSnapshot() // 保存成功后更新快照
    router.push(backPath)
  }
}

const openAddDetail = () => {
  detailDialogVisible.value = true
}

const submitAddDetail = async () => {
  const payload = {
    ...detailForm.value,
    inStockId: id || null, // 新建状态，ID为null
    costAmount: Number(detailForm.value.costPrice || 0) * Number(detailForm.value.quantity || 0)
  }
  
  // 新建状态，直接添加到本地列表
  if (!Number.isFinite(id)) {
    details.value.push(payload)
    detailDialogVisible.value = false
    ElMessage.success('新增成功')
    isDirty.value = checkIfReallyDirty()
    return
  }
  
  // 编辑状态，调用后端API
  await addInboundDetail(payload)
  detailDialogVisible.value = false
  await loadData()
  ElMessage.success('新增成功')
}

const openEditDetail = (row:any) => {
  editDetailForm.value = { ...row }
  editDetailDialogVisible.value = true
}

const submitEditDetail = async () => {
  // 重新计算成本金额
  editDetailForm.value.costAmount = Number(editDetailForm.value.costPrice || 0) * Number(editDetailForm.value.quantity || 0)
  
  // 新建状态下，直接更新本地数据
  if (!Number.isFinite(id)) {
    const index = details.value.findIndex((d: any) => d.productCode === editDetailForm.value.productCode)
    if (index !== -1) {
      details.value[index] = { ...editDetailForm.value }
    }
    editDetailDialogVisible.value = false
    ElMessage.success('保存成功')
    isDirty.value = checkIfReallyDirty()
    return
  }
  
  // 编辑状态下，调用后端API
  await updateInboundDetail(editDetailForm.value.id, editDetailForm.value)
  editDetailDialogVisible.value = false
  await loadData()
  ElMessage.success('保存成功')
}

const handleDeleteDetail = (row:any) => {
  // 新建状态下，直接从本地数据中删除
  if (!Number.isFinite(id)) {
    const index = details.value.findIndex((d: any) => d.productCode === row.productCode)
    if (index !== -1) {
      details.value.splice(index, 1)
    }
    ElMessage.success('已删除')
    isDirty.value = checkIfReallyDirty()
    return
  }
  
  // 编辑状态下，调用后端API
  deleteInboundDetail(row.id).then(async () => {
    await loadData()
    ElMessage.success('已删除')
  })
}

// 根据入库类型获取返回路径
const getBackPath = () => {
  const type = inbound.value?.inboundType
  if (type === 'TRANSFER') {
    return '/scm/inbound/other-inbound'
  } else if (type === 'SALES_RETURN') {
    return '/scm/inbound/sales-return'
  } else {
    return '/scm/inbound/purchase-inbound'
  }
}

const goBack = () => {
  const backPath = getBackPath()
  if (isDirty.value) {
    ElMessageBox.confirm('存在未保存的修改，是否取消保存并返回列表？', '提示', { type: 'warning', confirmButtonText: '是', cancelButtonText: '否' })
      .then(() => { router.push(backPath) })
      .catch(() => {})
  } else {
    router.push(backPath)
  }
}

onMounted(async () => {
  await loadData()
})

watch(inbound, () => {
  if (!loading.value) {
    isDirty.value = checkIfReallyDirty()
  }
}, { deep: true })

watch(details, () => {
  if (!loading.value) {
    isDirty.value = checkIfReallyDirty()
  }
}, { deep: true })
</script>

<style scoped>
.app-container { padding: 20px; }
.card-header { display:flex; justify-content:space-between; align-items:center; }
.actions-right { justify-content: flex-end; gap: 8px; }
.master-form { margin-top: 8px; }
</style>
