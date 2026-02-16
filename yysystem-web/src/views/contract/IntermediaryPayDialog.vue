<template>
  <el-dialog v-model="visible" title="中支记录" width="640px">
    <el-form :model="form" label-width="120px">
      <el-form-item label="居间费金额">
        <el-tag type="warning">{{ formatCurrency(intermediaryFee) }}</el-tag>
      </el-form-item>
      <el-form-item label="支付状态">
        <el-tag v-if="intermediaryFee <= 0" type="info">无居间费</el-tag>
        <el-tag v-else :type="String(feeStatus).toUpperCase() === 'PAID' ? 'success' : 'warning'">
          {{ String(feeStatus).toUpperCase() === 'PAID' ? '已支付' : '未支付' }}
        </el-tag>
      </el-form-item>
      <el-form-item label="本次支付说明">
        <el-input v-model="form.payInfo" type="textarea" placeholder="例如：支付方式/时间/凭证号/备注等" />
      </el-form-item>
    </el-form>
    <el-divider content-position="left">历史记录</el-divider>
    <el-table :data="historyList" size="small" border height="260" v-loading="loading">
      <el-table-column prop="time" label="时间" width="200" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="info" label="说明" show-overflow-tooltip />
      <el-table-column prop="operator" label="操作人" width="120" />
    </el-table>
    <template #footer>
      <span class="dialog-footer">
        <el-button :disabled="submitting" @click="visible = false">关闭</el-button>
        <el-button type="primary" :loading="submitting" :disabled="submitting || intermediaryFee <= 0" @click="handleMarkPaid">登记为已支付</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getContractById } from '@/api/contract'
import request from '@/utils/request'

const props = defineProps<{
  modelValue: boolean
  contractId?: number
}>()
const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const loading = ref(false)
const submitting = ref(false)
const intermediaryFee = ref<number>(0)
const feeStatus = ref<string>('UNPAID')
const historyList = ref<any[]>([])

const form = reactive({
  payInfo: ''
})

const formatCurrency = (value: number | undefined | null) => {
  const n = Number(value ?? 0)
  return `¥${n.toFixed(2)}`
}

const parseExtend = (extendFields: any) => {
  try {
    const obj = typeof extendFields === 'string' ? JSON.parse(extendFields) : (extendFields || {})
    return obj && typeof obj === 'object' ? obj : {}
  } catch {
    return {}
  }
}

const load = async () => {
  if (!props.contractId) return
  loading.value = true
  try {
    const res: any = await getContractById(props.contractId)
    const contract = res?.data || res
    intermediaryFee.value = Number(contract?.intermediaryFee || 0)
    feeStatus.value = String(contract?.intermediaryFeeStatus || 'UNPAID')
    const ext = parseExtend(contract?.extendFields)
    const raw = Array.isArray(ext.intermediaryPayHistory) ? ext.intermediaryPayHistory : []
    historyList.value = raw.map((x: any) => ({
      time: String(x?.time || ''),
      status: String(x?.status || ''),
      info: String(x?.info || ''),
      operator: String(x?.operator || '')
    })).reverse()
  } catch {
    historyList.value = []
  } finally {
    loading.value = false
  }
}

const handleMarkPaid = async () => {
  if (!props.contractId) return
  if (intermediaryFee.value <= 0) {
    ElMessage.info('无居间费支付金额')
    return
  }
  if (submitting.value) return
  submitting.value = true
  try {
    await request({
      url: `/crm/contract/${props.contractId}/fee-status`,
      method: 'put',
      data: { intermediaryFeeStatus: 'PAID', intermediaryPayInfo: String(form.payInfo || '') }
    })
    ElMessage.success('已保存')
    emit('success')
    await load()
  } finally {
    submitting.value = false
  }
}

watch(() => props.modelValue, (v) => {
  if (v) {
    form.payInfo = ''
    load()
  }
})
</script>
