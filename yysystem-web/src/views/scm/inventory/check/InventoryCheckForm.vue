<template>
  <el-dialog
    v-model="visible"
    :title="id ? '编辑盘点单' : '新增盘点单'"
    width="800px"
    @close="handleClose"
  >
    <el-form :model="form" label-width="100px" ref="formRef" :rules="rules">
      <el-form-item label="仓库" prop="warehouseId">
        <el-select v-model="form.warehouseId" placeholder="请选择仓库">
          <el-option
            v-for="item in warehouseList"
            :key="item.id"
            :label="item.warehouseName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="盘点日期" prop="checkDate">
        <el-date-picker v-model="form.checkDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" />
      </el-form-item>

      <div class="detail-list">
        <div class="detail-header">
          <span>盘点明细</span>
          <el-button type="primary" link @click="handleAddDetail">添加商品</el-button>
        </div>
        <el-table :data="form.detailList" border stripe>
          <el-table-column label="商品编号" prop="productCode">
            <template #default="scope">
              <el-input v-model="scope.row.productCode" placeholder="商品编号" />
            </template>
          </el-table-column>
          <el-table-column label="账面数量" prop="bookQuantity" width="120">
             <template #default="scope">
              <el-input-number v-model="scope.row.bookQuantity" :min="0" />
            </template>
          </el-table-column>
          <el-table-column label="盘点数量" prop="checkQuantity" width="120">
            <template #default="scope">
              <el-input-number v-model="scope.row.checkQuantity" :min="0" />
            </template>
          </el-table-column>
           <el-table-column label="差异数量" width="100" align="center">
            <template #default="scope">
              {{ (scope.row.checkQuantity || 0) - (scope.row.bookQuantity || 0) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="scope">
              <el-button link type="danger" @click="handleRemoveDetail(scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
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
import { ref, reactive, computed, watch } from 'vue'
import { getCheckById, createCheck, updateCheck } from '@/api/inventory'
import { getWarehouseList } from '@/api/warehouse'
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

const formRef = ref()
const warehouseList = ref<any[]>([])

const form = reactive<any>({
  id: undefined,
  warehouseId: undefined,
  checkDate: '',
  remark: '',
  status: 'DRAFT',
  detailList: []
})

const rules = {
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  checkDate: [{ required: true, message: '请选择盘点日期', trigger: 'change' }]
}

const fetchWarehouses = async () => {
    const res: any = await getWarehouseList({ current: 1, size: 100 })
    warehouseList.value = res.records
}

const handleClose = () => {
  formRef.value?.resetFields()
  form.detailList = []
  form.id = undefined
}

const handleAddDetail = () => {
  form.detailList.push({
    productCode: '',
    bookQuantity: 0,
    checkQuantity: 0,
    remark: ''
  })
}

const handleRemoveDetail = (index: number) => {
  form.detailList.splice(index, 1)
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      // Calculate diff
      form.detailList.forEach((item: any) => {
          item.diffQuantity = (item.checkQuantity || 0) - (item.bookQuantity || 0)
      })

      if (form.id) {
        await updateCheck(form)
        ElMessage.success('更新成功')
      } else {
        await createCheck(form)
        ElMessage.success('创建成功')
      }
      visible.value = false
      emit('success')
    }
  })
}

watch(() => props.modelValue, async (val) => {
  if (val) {
      await fetchWarehouses()
      if (props.id) {
          const res: any = await getCheckById(props.id)
          Object.assign(form, res)
      } else {
          form.id = undefined
          form.warehouseId = undefined
          form.checkDate = new Date().toISOString().split('T')[0]
          form.remark = ''
          form.status = 'DRAFT'
          form.detailList = []
      }
  }
})
</script>

<style scoped>
.detail-list {
  margin-top: 20px;
}
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-weight: bold;
}
</style>
