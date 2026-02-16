<template>
  <el-dialog v-model="visible" :title="id ? '编辑工单' : '新增工单'" width="800px" @close="handleClose">
    <el-form :model="form" label-width="100px">
      <el-row>
        <el-col :span="12">
          <el-form-item label="客户" required>
             <el-select v-model="form.customerId" placeholder="请选择客户" filterable @change="handleCustomerChange">
                <el-option
                  v-for="item in customerList"
                  :key="item.id"
                  :label="item.customerName"
                  :value="item.id"
                />
             </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="优先级">
            <el-select v-model="form.priority" placeholder="请选择">
              <el-option label="高" value="HIGH" />
              <el-option label="中" value="MEDIUM" />
              <el-option label="低" value="LOW" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="联系人">
            <el-input v-model="form.contactName" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话">
            <el-input v-model="form.contactPhone" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="地址">
        <el-input v-model="form.address" />
      </el-form-item>
      <el-row>
        <el-col :span="12">
          <el-form-item label="产品名称">
            <el-input v-model="form.productName" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="产品规格">
            <el-input v-model="form.productSpec" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="数量">
        <el-input-number v-model="form.quantity" :min="1" />
      </el-form-item>
      <el-form-item label="故障内容" required>
        <el-input v-model="form.problemDesc" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="图片附件">
        <el-input v-model="form.problemImages" placeholder="图片URL" />
      </el-form-item>
      <el-row>
        <el-col :span="12">
          <el-form-item label="上报日期">
            <el-date-picker v-model="form.reportDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="维修费用">
            <el-input-number v-model="form.cost" :precision="2" :step="0.1" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="状态">
        <el-select v-model="form.status" placeholder="请选择">
          <el-option label="待处理" value="PENDING" />
          <el-option label="处理中" value="PROCESSING" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </el-form-item>
      <el-form-item label="启用/禁用">
        <el-switch v-model="form.isEnabled" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input type="textarea" v-model="form.remark" />
      </el-form-item>
    </el-form>

    <div v-if="form.id" style="margin-top: 20px">
      <div class="detail-header">
        <span>维修记录</span>
        <el-button type="primary" link @click="handleAddRepair">添加记录</el-button>
      </div>
      <el-table :data="form.repairRecordList" border style="margin-top: 10px">
        <el-table-column prop="faultCause" label="故障原因" />
        <el-table-column prop="solution" label="解决方案" />
        <el-table-column prop="totalCost" label="总费用" width="100" />
        <el-table-column prop="repairStatus" label="状态" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
      </el-table>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </span>
    </template>

    <!-- Repair Record Dialog -->
    <el-dialog v-model="repairVisible" title="添加维修记录" width="500px" append-to-body>
      <el-form :model="repairForm" label-width="100px">
        <el-form-item label="故障原因" required>
          <el-input v-model="repairForm.faultCause" type="textarea" />
        </el-form-item>
        <el-form-item label="解决方案" required>
          <el-input v-model="repairForm.solution" type="textarea" />
        </el-form-item>
        <el-form-item label="维修人工费">
          <el-input-number v-model="repairForm.repairCost" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="材料费">
          <el-input-number v-model="repairForm.materialCost" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="维修状态">
          <el-select v-model="repairForm.repairStatus">
            <el-option label="已修复" value="FIXED" />
            <el-option label="待修复" value="PENDING" />
            <el-option label="无法修复" value="UNFIXABLE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="repairVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitRepair">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { getWorkOrderById, createWorkOrder, updateWorkOrder, createRepairRecord } from '@/api/asm'
import { getCustomerList } from '@/api/customer'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  modelValue: boolean
  id?: number
}>()

const emit = defineEmits(['update:modelValue', 'success'])

const customerList = ref<any[]>([])

const fetchCustomerList = async () => {
  const res: any = await getCustomerList({ size: 1000 })
  customerList.value = res.records
}

onMounted(() => {
  fetchCustomerList()
})

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const form = reactive<any>({
  id: undefined,
  customerId: undefined,
  contactName: '',
  contactPhone: '',
  address: '',
  productName: '',
  productSpec: '',
  quantity: 1,
  problemDesc: '',
  problemImages: '',
  priority: 'MEDIUM',
  status: 'PENDING',
  reportDate: '',
  cost: 0,
  isEnabled: 1,
  remark: '',
  repairRecordList: []
})

const handleCustomerChange = (customerId: number) => {
  const customer = customerList.value.find(c => c.id === customerId)
  if (customer) {
    form.contactName = customer.contactPerson
    form.contactPhone = customer.contactPhone
    form.address = customer.address
  }
}

const repairVisible = ref(false)
const repairForm = reactive<any>({
  workOrderId: undefined,
  faultCause: '',
  solution: '',
  repairCost: 0,
  materialCost: 0,
  repairStatus: 'FIXED'
})

watch(() => props.id, async (newId) => {
  if (newId) {
    const res: any = await getWorkOrderById(newId)
    Object.assign(form, res)
  } else {
    form.id = undefined
    form.customerId = undefined
    form.contactName = ''
    form.contactPhone = ''
    form.address = ''
    form.productName = ''
    form.productSpec = ''
    form.quantity = 1
    form.problemDesc = ''
    form.priority = 'MEDIUM'
    form.status = 'PENDING'
  form.isEnabled = 1
  form.reportDate = ''
  form.cost = 0
    form.remark = ''
    form.repairRecordList = []
  }
})

const handleSubmit = async () => {
  try {
    if (form.id) {
      await updateWorkOrder(form)
    } else {
      await createWorkOrder(form)
    }
    ElMessage.success('操作成功')
    visible.value = false
    emit('success')
  } catch (e) {
    // handled
  }
}

const handleAddRepair = () => {
  repairForm.workOrderId = form.id
  repairForm.faultCause = ''
  repairForm.solution = ''
  repairForm.repairCost = 0
  repairForm.materialCost = 0
  repairForm.repairStatus = 'FIXED'
  repairVisible.value = true
}

const handleSubmitRepair = async () => {
  try {
    await createRepairRecord(repairForm)
    ElMessage.success('添加维修记录成功')
    repairVisible.value = false
    // Refresh work order details to see new record
    const res: any = await getWorkOrderById(form.id)
    form.repairRecordList = res.repairRecordList
    form.status = res.status // Status might update
  } catch (e) {
    // handled
  }
}

const handleClose = () => {
  emit('update:modelValue', false)
}
</script>

<style scoped>
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}
</style>
