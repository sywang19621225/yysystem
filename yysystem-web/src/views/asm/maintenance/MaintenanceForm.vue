<template>
  <el-dialog v-model="visible" :title="form.id ? '编辑维保' : '新增维保'" width="800px" @close="handleClose">
    <el-form :model="form" label-width="100px">
      <el-row>
        <el-col :span="12">
          <el-form-item label="维保名称" required>
            <el-input v-model="form.maintenanceName" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="客户" required>
            <el-select v-model="form.customerId" placeholder="选择客户" filterable clearable @change="handleCustomerChange">
              <el-option v-for="c in customerList" :key="c.id" :label="c.customerName" :value="c.id" />
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
      <el-form-item label="质保到期">
        <el-date-picker v-model="form.warrantyExpirationTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
      </el-form-item>
      <el-row>
        <el-col :span="12">
          <el-form-item label="预定时间">
            <el-date-picker v-model="form.scheduledTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="维保人">
            <el-select v-model="form.maintainerId" placeholder="选择维保人" filterable clearable>
              <el-option v-for="u in userList" :key="u.id" :label="u.name" :value="u.id" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="设备名称">
            <el-input v-model="form.deviceName" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="数量">
            <el-input-number v-model="form.quantity" :min="1" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="维保内容">
        <el-input type="textarea" v-model="form.content" />
      </el-form-item>
      <el-form-item label="执行记录">
        <el-input type="textarea" v-model="form.executionRecord" />
      </el-form-item>
      <el-form-item label="图片附件">
        <el-input v-model="form.images" placeholder="图片URL" />
      </el-form-item>
      <el-row>
        <el-col :span="12">
          <el-form-item label="维保费用">
            <el-input-number v-model="form.cost" :precision="2" :step="0.1" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="备注">
        <el-input type="textarea" v-model="form.remark" />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import request from '@/utils/request'
import { getCustomerList } from '@/api/customer'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  modelValue: boolean
  id?: number
}>()

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const form = reactive<any>({
  id: undefined,
  maintenanceName: '',
  customerId: undefined,
  contactName: '',
  contactPhone: '',
  address: '',
  warrantyExpirationTime: '',
  scheduledTime: '',
  maintainerId: undefined,
  deviceName: '',
  quantity: 1,
  content: '',
  executionRecord: '',
  images: '',
  cost: 0,
  status: 1,
  remark: ''
})

const customerList = ref<any[]>([])
const userList = ref<any[]>([])

const fetchCustomers = async () => {
  const res: any = await getCustomerList({ current: 1, size: 1000 })
  customerList.value = res.records
}

const fetchUsers = async () => {
  const res: any = await request({
    url: '/system/user/list',
    method: 'get',
    params: { current: 1, size: 1000 }
  })
  userList.value = res.records
}

const handleCustomerChange = (customerId: number) => {
  const customer = customerList.value.find(c => c.id === customerId)
  if (customer) {
    form.contactName = customer.contactPerson
    form.contactPhone = customer.contactPhone
    form.address = customer.address
  }
}

const handleSubmit = async () => {
  try {
    if (form.id) {
      await request({
        url: '/asm/maintenance',
        method: 'put',
        data: form
      })
      ElMessage.success('更新成功')
    } else {
      await request({
        url: '/asm/maintenance',
        method: 'post',
        data: form
      })
      ElMessage.success('创建成功')
    }
    visible.value = false
    emit('success')
  } catch (e) {
  }
}

const handleClose = () => {
  form.id = undefined
  Object.keys(form).forEach(key => {
    if (key !== 'id' && key !== 'status' && key !== 'quantity' && key !== 'cost') form[key] = ''
  })
  form.warrantyExpirationTime = ''
  form.status = 1
  form.quantity = 1
  form.cost = 0
}

watch(() => props.id, async (val) => {
  if (val) {
    const res: any = await request({ url: `/asm/maintenance/${val}`, method: 'get' })
    if (res) {
      Object.assign(form, res)
      if (!form.quantity) form.quantity = 1
      if (form.status == null) form.status = 1
    }
  } else {
    handleClose()
  }
})

onMounted(() => {
  fetchCustomers()
  fetchUsers()
})
</script>
