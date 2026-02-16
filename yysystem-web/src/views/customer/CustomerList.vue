<template>
  <div class="customer-list">
    <div class="header">
      <el-button type="primary" @click="handleAdd">新增客户</el-button>
    </div>

    <el-table :data="tableData" style="width: 100%" v-loading="loading">
      <el-table-column prop="customerCode" label="客户编号" width="180" />
      <el-table-column prop="customerName" label="客户名称" />
      <el-table-column prop="customerType" label="客户类型" width="120" />
      <el-table-column prop="linkman" label="联系人" width="120" />
      <el-table-column prop="phone" label="电话" width="150" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchData"
      />
    </div>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑客户' : '新增客户'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="客户名称" required>
          <el-input v-model="form.customerName" />
        </el-form-item>
        <el-form-item label="客户标签">
          <el-input v-model="form.customerTag" />
        </el-form-item>
        <el-form-item label="客户类型" required>
          <el-select v-model="form.customerType" placeholder="请选择">
            <el-option label="终端客户" value="END_USER" />
            <el-option label="代理商" value="AGENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.linkman" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="税号">
          <el-input v-model="form.creditCode" />
        </el-form-item>
        <el-form-item label="开户行">
          <el-input v-model="form.bankName" />
        </el-form-item>
        <el-form-item label="银行账号">
          <el-input v-model="form.bankAccount" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input type="textarea" v-model="form.companyIntro" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { getCustomerList, saveCustomer, updateCustomer, deleteCustomer, type Customer } from '@/api/customer'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref<Customer[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const form = reactive<any>({
  id: undefined,
  customerName: '',
  customerTag: '',
  customerType: 'END_USER',
  linkman: '',
  phone: '',
  address: '',
  creditCode: '',
  bankName: '',
  bankAccount: '',
  companyIntro: '',
  status: 1
})

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getCustomerList({ current: currentPage.value, size: pageSize.value })
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  form.id = undefined
  form.customerName = ''
  form.customerTag = ''
  form.customerType = 'END_USER'
  form.linkman = ''
  form.phone = ''
  form.address = ''
  form.creditCode = ''
  form.bankName = ''
  form.bankAccount = ''
  form.companyIntro = ''
  form.status = 1
  dialogVisible.value = true
}

const handleEdit = (row: Customer) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row: Customer) => {
  ElMessageBox.confirm('确认删除该客户吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteCustomer(row.id!)
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleSubmit = async () => {
  try {
    if (form.id) {
      await updateCustomer(form.id, form)
    } else {
      await saveCustomer(form)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    // handled in request.ts
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.customer-list {
  padding: 20px;
}
.header {
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
