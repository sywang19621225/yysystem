<template>
  <el-dialog v-model="visible" title="保证金退还登记" width="560px">
    <el-form :model="form" label-width="110px">
      <el-form-item label="保证金支出记录" required>
        <el-select v-model="form.financeId" filterable placeholder="请选择保证金支出" style="width: 100%" @change="handleSelect">
          <el-option
            v-for="item in depositExpenseList"
            :key="item.id"
            :label="item.label"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="支出金额">
        <el-tag type="warning">{{ formatCurrency(selected.amount) }}</el-tag>
      </el-form-item>
      <el-form-item label="已退还">
        <el-tag type="info">{{ formatCurrency(selected.refundedAmount) }}</el-tag>
      </el-form-item>
      <el-form-item label="本次退还金额" required>
        <el-input-number v-model="form.refundAmount" :precision="2" :min="0" :max="selected.maxRefundable" style="width: 100%" />
      </el-form-item>
      <el-form-item label="退还时间" required>
        <el-date-picker v-model="form.refundTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
      </el-form-item>
      <el-form-item label="退还说明">
        <el-input v-model="form.refundRemark" type="textarea" placeholder="可填写退还原因/凭证号/备注等" />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button :disabled="submitting" @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" :disabled="submitting" @click="handleSubmit">保存</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getFinanceList, getFinanceById, updateFinance } from '@/api/finance'

const props = defineProps<{
  modelValue: boolean
  contractId?: number
}>()
const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const submitting = ref(false)
const depositExpenseList = ref<any[]>([])
const selected = reactive({
  id: undefined as number | undefined,
  amount: 0,
  refundedAmount: 0,
  maxRefundable: 0
})

const form = reactive({
  financeId: undefined as number | undefined,
  refundAmount: 0,
  refundTime: '',
  refundRemark: ''
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

const isDepositExpense = (x: any) => {
  const category = String(x?.category || '')
  const remark = String(x?.remark || '')
  const ext = parseExtend(x?.extendFields)
  const refunded = Number(ext.depositRefundAmount || 0)
  return category.includes('保证金') || remark.includes('保证金') || refunded > 0
}

const buildDepositList = async () => {
  if (!props.contractId) return
  try {
    const res: any = await getFinanceList({ current: 1, size: 1000 })
    const list = (res?.records || res || []).filter((x: any) => x.contractId === props.contractId && String(x.type) === 'OUT' && isDepositExpense(x))
    const mapped = list.map((x: any) => {
      const ext = parseExtend(x.extendFields)
      const refundedAmount = Number(ext.depositRefundAmount || 0)
      const amount = Number(x.arrivalAmount || x.amount || 0)
      const maxRefundable = Math.max(0, Number((amount - refundedAmount).toFixed(2)))
      const label = `${x.category || '保证金支出'}｜支出${formatCurrency(amount)}｜已退还${formatCurrency(refundedAmount)}｜${x.createTime || ''}`
      return { id: x.id, label, amount, refundedAmount, maxRefundable }
    }).filter((x: any) => x.maxRefundable > 0)
    depositExpenseList.value = mapped
    if (!mapped.length) {
      ElMessage.info('未找到可登记退还的保证金支出记录')
      visible.value = false
      return
    }
    if (!form.financeId || !mapped.some((x: any) => x.id === form.financeId)) {
      form.financeId = mapped[0].id
      await handleSelect(form.financeId as number)
    }
  } catch {
    depositExpenseList.value = []
  }
}

const handleSelect = async (id: number) => {
  if (!id) return
  const item = depositExpenseList.value.find((x: any) => x.id === id)
  selected.id = id
  selected.amount = Number(item?.amount || 0)
  selected.refundedAmount = Number(item?.refundedAmount || 0)
  selected.maxRefundable = Number(item?.maxRefundable || 0)
  form.refundAmount = selected.maxRefundable
}

const handleSubmit = async () => {
  if (!props.contractId) return
  if (!form.financeId) {
    ElMessage.error('请选择保证金支出记录')
    return
  }
  if (Number(form.refundAmount || 0) <= 0) {
    ElMessage.error('请输入本次退还金额')
    return
  }
  if (!String(form.refundTime || '').trim()) {
    ElMessage.error('请选择退还时间')
    return
  }
  if (Number(form.refundAmount) > Number(selected.maxRefundable || 0)) {
    ElMessage.error('退还金额不能超过可退还金额')
    return
  }
  if (submitting.value) return
  submitting.value = true
  try {
    const fin: any = await getFinanceById(form.financeId)
    const ext = parseExtend(fin?.extendFields)
    const oldRefund = Number(ext.depositRefundAmount || 0)
    const nextRefund = Number((oldRefund + Number(form.refundAmount || 0)).toFixed(2))
    ext.depositRefundAmount = nextRefund
    ext.depositRefundTime = String(form.refundTime || '')
    ext.depositRefundRemark = String(form.refundRemark || '')
    const payload: any = {
      contractId: fin.contractId,
      customerId: fin.customerId,
      type: fin.type,
      amount: fin.amount,
      arrivalAmount: fin.arrivalAmount,
      arrivalTime: fin.arrivalTime,
      category: fin.category,
      remark: fin.remark,
      auditStatus: fin.auditStatus,
      extendFields: JSON.stringify(ext)
    }
    const ok = await updateFinance(form.financeId, payload)
    if (ok) {
      ElMessage.success('退还信息已保存')
      visible.value = false
      emit('success')
    }
  } finally {
    submitting.value = false
  }
}

watch(() => props.modelValue, (v) => {
  if (v) {
    form.financeId = undefined
    form.refundAmount = 0
    form.refundTime = ''
    form.refundRemark = ''
    selected.id = undefined
    selected.amount = 0
    selected.refundedAmount = 0
    selected.maxRefundable = 0
    buildDepositList()
  }
})
</script>
