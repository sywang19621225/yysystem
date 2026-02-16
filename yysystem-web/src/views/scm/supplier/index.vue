<template>
  <div class="app-container">
    <h2 class="page-title">供应商管理</h2>
    <div class="header-actions">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="供应商名称">
          <el-input v-model="queryParams.supplierName" placeholder="请输入供应商名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button v-if="canCreate" type="primary" @click="handleAdd">新增供应商</el-button>
    </div>

    <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
      <el-table-column prop="supplierNo" label="供应商编号" width="180" align="center" />
      <el-table-column prop="supplierName" label="供应商名称" show-overflow-tooltip />
      <el-table-column prop="contactName" label="联系人" width="120" align="center" />
      <el-table-column prop="contactPhone" label="联系电话" width="150" align="center" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <el-button v-if="canUpdate" link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button v-if="canDelete" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑供应商' : '新增供应商'" width="600px">
      <el-form :model="form" label-width="100px" ref="formRef" :rules="rules">
        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="form.supplierName" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" />
        </el-form-item>
        <el-form-item label="联系邮箱" prop="contactEmail">
          <el-input v-model="form.contactEmail" />
        </el-form-item>
        <el-form-item label="个人微信号" prop="wechatId">
          <el-input v-model="form.wechatId" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="单位网址" prop="website">
          <el-input v-model="form.website" />
        </el-form-item>
        <el-form-item label="开户行" prop="bankName">
          <el-input v-model="form.bankName" />
        </el-form-item>
        <el-form-item label="银行账号" prop="bankAccount">
          <el-input v-model="form.bankAccount" />
        </el-form-item>
        <el-form-item label="税号" prop="creditCode">
          <el-input v-model="form.creditCode" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
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
import { ref, reactive, onMounted, computed } from 'vue'
import { getSupplierList, createSupplier, updateSupplier, deleteSupplier } from '@/api/supplier'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canCreate = computed(() => hasPermission('SM:create'))
const canUpdate = computed(() => hasPermission('SM:update'))
const canDelete = computed(() => hasPermission('SM:delete'))

const tableData = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()

const queryParams = reactive({
  supplierName: ''
})

const form = reactive<any>({
  id: undefined,
  supplierName: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  wechatId: '',
  address: '',
  website: '',
  bankName: '',
  bankAccount: '',
  creditCode: '',
  status: 1,
  remark: ''
})

const rules = {
  supplierName: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      ...queryParams
    }
    const res: any = await getSupplierList(params)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  currentPage.value = 1
  fetchData()
}

const resetQuery = () => {
  queryParams.supplierName = ''
  handleQuery()
}

const handleAdd = () => {
  form.id = undefined
  form.supplierName = ''
  form.contactName = ''
  form.contactPhone = ''
  form.contactEmail = ''
  form.wechatId = ''
  form.address = ''
  form.website = ''
  form.bankName = ''
  form.bankAccount = ''
  form.creditCode = ''
  form.status = 1
  form.remark = ''
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该供应商吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteSupplier(row.id)
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      if (form.id) {
        await updateSupplier(form)
        ElMessage.success('更新成功')
      } else {
        await createSupplier(form)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchData()
    }
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.search-form {
  margin-bottom: -18px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
