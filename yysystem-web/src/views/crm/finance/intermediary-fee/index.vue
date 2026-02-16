<template>
  <div class="app-container">
    <!-- 页面标题 -->
    <div style="margin-bottom: 20px;">
      <h2 style="margin: 0; font-size: 20px; font-weight: 600; color: #303133;">居间费管理</h2>
    </div>

    <!-- 搜索栏 -->
    <el-card style="margin-bottom: 20px;">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="合同编号">
          <el-input v-model="queryParams.contractNo" placeholder="请输入合同编号" clearable />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="queryParams.customerName" placeholder="请输入客户名称" clearable />
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 150px;">
            <el-option label="待支付" value="UNPAID" />
            <el-option label="已支付" value="PAID" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card>
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="contractNo" label="合同编号" width="150" />
        <el-table-column prop="contractName" label="合同名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="customerName" label="客户名称" min-width="150" />
        <el-table-column prop="contractAmount" label="合同金额" width="120" align="right">
          <template #default="scope">
            ¥{{ scope.row.contractAmount?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="intermediaryFee" label="居间费金额" width="120" align="right">
          <template #default="scope">
            <span style="color: #F56C6C; font-weight: bold;">¥{{ scope.row.intermediaryFee?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="intermediaryCustomerName" label="居间人名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="intermediaryFeeStatus" label="支付状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.intermediaryFeeStatus === 'PAID' ? 'success' : 'warning'">
              {{ scope.row.intermediaryFeeStatus === 'PAID' ? '已支付' : '待支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="150" />
        <el-table-column prop="payMethod" label="支付方式" width="120" />
        <el-table-column prop="payRemark" label="支付备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handlePay(scope.row)">
              {{ scope.row.intermediaryFeeStatus === 'PAID' ? '查看' : '支付' }}
            </el-button>
            <el-button v-if="canUpdate" link type="warning" @click="handleEdit(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 支付弹窗 -->
    <el-dialog v-model="payDialogVisible" :title="currentRow?.intermediaryFeeStatus === 'PAID' ? '查看支付信息' : '居间费支付'" width="500px">
      <el-form ref="payFormRef" :model="payForm" :rules="payRules" label-width="100px">
        <el-form-item label="合同编号">
          <el-input v-model="currentRow.contractNo" disabled />
        </el-form-item>
        <el-form-item label="居间费金额">
          <el-input :model-value="'¥' + (currentRow.intermediaryFee?.toFixed(2) || '0.00')" disabled />
        </el-form-item>
        <el-form-item label="居间方">
          <el-input v-model="currentRow.intermediaryCustomerName" disabled />
        </el-form-item>
        <el-form-item label="支付金额" prop="payAmount" v-if="currentRow.intermediaryFeeStatus !== 'PAID'">
          <el-input-number v-model="payForm.payAmount" :precision="2" :min="0" :max="currentRow.intermediaryFee" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="支付方式" prop="payMethod" v-if="currentRow.intermediaryFeeStatus !== 'PAID'">
          <el-select v-model="payForm.payMethod" placeholder="请选择支付方式" style="width: 100%;">
            <el-option
              v-for="item in PAY_METHOD_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="支付备注" prop="payRemark" v-if="currentRow.intermediaryFeeStatus !== 'PAID'">
          <el-input v-model="payForm.payRemark" type="textarea" :rows="3" placeholder="请输入支付备注" />
        </el-form-item>
        <template v-if="currentRow.intermediaryFeeStatus === 'PAID'">
          <el-form-item label="支付金额">
            <el-input :model-value="'¥' + (currentRow.payAmount || currentRow.intermediaryFee?.toFixed(2) || '0.00')" disabled />
          </el-form-item>
          <el-form-item label="支付方式">
            <el-input v-model="currentRow.payMethod" disabled />
          </el-form-item>
          <el-form-item label="支付备注">
            <el-input v-model="currentRow.payRemark" type="textarea" :rows="3" disabled />
          </el-form-item>
          <el-form-item label="支付时间">
            <el-input v-model="currentRow.payTime" disabled />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="payDialogVisible = false">关闭</el-button>
          <el-button v-if="currentRow.intermediaryFeeStatus !== 'PAID'" type="primary" @click="handlePaySubmit">确认支付</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑居间费信息" width="500px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="合同编号">
          <el-input v-model="editForm.contractNo" disabled />
        </el-form-item>
        <el-form-item label="居间费金额" prop="intermediaryFee">
          <el-input-number v-model="editForm.intermediaryFee" :precision="2" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="居间人" prop="intermediaryCustomerName">
          <el-input v-model="editForm.intermediaryCustomerName" placeholder="请输入居间人名称" />
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="editForm.intermediaryFeeStatus" style="width: 100%;" disabled>
            <el-option label="待支付" value="UNPAID" />
            <el-option label="已支付" value="PAID" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付时间" prop="payTime">
          <el-date-picker
            v-model="editForm.payTime"
            type="datetime"
            placeholder="选择支付时间"
            style="width: 100%;"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="支付方式" prop="payMethod">
          <el-select v-model="editForm.payMethod" placeholder="请选择支付方式" style="width: 100%;">
            <el-option
              v-for="item in PAY_METHOD_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="支付备注" prop="payRemark">
          <el-input v-model="editForm.payRemark" type="textarea" :rows="3" placeholder="请输入支付备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleEditSubmit">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getIntermediaryFeeList, getIntermediaryFeeStatistics, updateIntermediaryFeeStatus } from '@/api/intermediary-fee'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canUpdate = computed(() => hasPermission('IFM:update'))

const PAY_METHOD_OPTIONS = ref<{ label: string; value: string }[]>([])

// 加载支付方式配置
const loadPayMethodOptions = async () => {
  try {
    const res: any = await request({ url: '/system/config/list', method: 'get', params: { size: 100 } })
    const general = (res.records || res || []).find((item: any) => item.configKey === 'general_settings')
    if (general && general.configValue) {
      const saved = JSON.parse(general.configValue)
      const methods = saved.intermediaryPayMethods || ['开票支付', '工资冲抵', '货款冲抵']
      PAY_METHOD_OPTIONS.value = methods.map((m: string) => ({ label: m, value: m }))
    } else {
      // 默认支付方式
      PAY_METHOD_OPTIONS.value = [
        { label: '开票支付', value: '开票支付' },
        { label: '工资冲抵', value: '工资冲抵' },
        { label: '货款冲抵', value: '货款冲抵' }
      ]
    }
  } catch (e) {
    console.error('加载支付方式配置失败', e)
    // 默认支付方式
    PAY_METHOD_OPTIONS.value = [
      { label: '开票支付', value: '开票支付' },
      { label: '工资冲抵', value: '工资冲抵' },
      { label: '货款冲抵', value: '货款冲抵' }
    ]
  }
}

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  contractNo: '',
  customerName: '',
  status: ''
})

// 统计数据
const statistics = reactive({
  totalFee: 0,
  paidFee: 0,
  unpaidFee: 0,
  totalCount: 0,
  paidCount: 0,
  unpaidCount: 0
})

// 表格数据
const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)

// 支付弹窗
const payDialogVisible = ref(false)
const currentRow = ref<any>({})
const payFormRef = ref<FormInstance>()
const payForm = reactive({
  payAmount: 0,
  payMethod: '',
  payRemark: ''
})

const payRules = reactive<FormRules>({
  payAmount: [{ required: true, message: '请输入支付金额', trigger: 'blur' }],
  payMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }],
  payRemark: [{ required: true, message: '请输入支付备注', trigger: 'blur' }]
})

// 编辑弹窗
const editDialogVisible = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive({
  id: 0,
  contractNo: '',
  intermediaryFee: 0,
  intermediaryCustomerName: '',
  intermediaryFeeStatus: 'UNPAID',
  payTime: '',
  payMethod: '',
  payRemark: ''
})

const editRules = reactive<FormRules>({
  intermediaryFee: [{ required: true, message: '请输入居间费金额', trigger: 'blur' }],
  intermediaryCustomerName: [{ required: true, message: '请输入居间人名称', trigger: 'blur' }]
})

// 获取列表数据
const getList = async () => {
  loading.value = true
  try {
    const params = {
      current: queryParams.pageNum,
      size: queryParams.pageSize,
      contractNo: queryParams.contractNo,
      customerName: queryParams.customerName,
      status: queryParams.status
    }
    const res: any = await getIntermediaryFeeList(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取居间费列表失败', error)
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const getStatistics = async () => {
  try {
    const res: any = await getIntermediaryFeeStatistics()
    Object.assign(statistics, res)
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

// 查询
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.pageNum = 1
  queryParams.pageSize = 10
  queryParams.contractNo = ''
  queryParams.customerName = ''
  queryParams.status = ''
  getList()
}

// 分页
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  getList()
}

const handleCurrentChange = (val: number) => {
  queryParams.pageNum = val
  getList()
}

// 支付
const handlePay = (row: any) => {
  currentRow.value = row
  payForm.payAmount = row.intermediaryFee || 0
  payForm.payMethod = ''
  payForm.payRemark = ''
  payDialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  editForm.id = row.id
  editForm.contractNo = row.contractNo
  editForm.intermediaryFee = row.intermediaryFee || 0
  editForm.intermediaryCustomerName = row.intermediaryCustomerName || ''
  editForm.intermediaryFeeStatus = row.intermediaryFeeStatus || 'UNPAID'
  editForm.payTime = row.payTime || ''
  editForm.payMethod = row.payMethod || ''
  editForm.payRemark = row.payRemark || ''
  editDialogVisible.value = true
}

// 提交编辑
const handleEditSubmit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      await request({
        url: `/crm/contract/${editForm.id}/intermediary`,
        method: 'put',
        data: {
          intermediaryFee: editForm.intermediaryFee,
          intermediaryCustomerName: editForm.intermediaryCustomerName,
          payTime: editForm.payTime,
          payMethod: editForm.payMethod,
          payRemark: editForm.payRemark
        }
      })
      
      ElMessage.success('保存成功')
      editDialogVisible.value = false
      getList()
      getStatistics()
    } catch (error: any) {
      ElMessage.error(error.message || '保存失败')
    }
  })
}

// 提交支付
const handlePaySubmit = async () => {
  if (!payFormRef.value) return
  await payFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      await ElMessageBox.confirm('确认支付该笔居间费？', '提示', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      await updateIntermediaryFeeStatus(currentRow.value.id, {
        status: 'PAID',
        payAmount: payForm.payAmount,
        payMethod: payForm.payMethod,
        payRemark: payForm.payRemark
      })
      
      ElMessage.success('支付成功')
      payDialogVisible.value = false
      getList()
      getStatistics()
    } catch (error: any) {
      if (error !== 'cancel') {
        ElMessage.error(error.message || '支付失败')
      }
    }
  })
}

onMounted(() => {
  loadPayMethodOptions()
  getList()
  getStatistics()
})
</script>

<style scoped>
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>
