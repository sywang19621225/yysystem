<template>
  <div class="app-container">
    <h2 class="page-title">仓库管理</h2>
    <div class="header-actions">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="仓库名称">
          <el-input v-model="queryParams.warehouseName" placeholder="请输入仓库名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button v-if="canCreate" type="primary" @click="handleAdd">新增仓库</el-button>
    </div>

    <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
      <el-table-column prop="warehouseNo" label="仓库编号" width="180" align="center" />
      <el-table-column prop="warehouseName" label="仓库名称" show-overflow-tooltip />
      <el-table-column prop="managerName" label="负责人" width="120" align="center" />
      <el-table-column prop="managerPhone" label="联系电话" width="150" align="center" />
      <el-table-column prop="address" label="地址" show-overflow-tooltip />
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
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑仓库' : '新增仓库'" width="600px">
      <el-form :model="form" label-width="100px" ref="formRef" :rules="rules">
        <el-form-item label="仓库名称" prop="warehouseName">
          <el-input v-model="form.warehouseName" />
        </el-form-item>
        <el-form-item label="负责人" prop="managerName">
          <el-select v-model="selectedManagerId" placeholder="请选择负责人" filterable @change="onManagerChange">
            <el-option
              v-for="u in userOptions"
              :key="u.id"
              :label="u.name || u.realName || u.nickname || u.username"
              :value="u.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话" prop="managerPhone">
          <el-input v-model="form.managerPhone" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" />
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
import { getWarehouseList, createWarehouse, updateWarehouse, deleteWarehouse } from '@/api/warehouse'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canCreate = computed(() => hasPermission('WM:create'))
const canUpdate = computed(() => hasPermission('WM:update'))
const canDelete = computed(() => hasPermission('WM:delete'))

const tableData = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()
const userOptions = ref<any[]>([])
const selectedManagerId = ref<number | undefined>(undefined)

const queryParams = reactive({
  warehouseName: ''
})

const form = reactive<any>({
  id: undefined,
  warehouseName: '',
  managerName: '',
  managerPhone: '',
  address: '',
  status: 1,
  remark: ''
})

const rules = {
  warehouseName: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      ...queryParams
    }
    const res: any = await getWarehouseList(params)
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
  queryParams.warehouseName = ''
  handleQuery()
}

const handleAdd = () => {
  form.id = undefined
  form.warehouseName = ''
  form.managerName = ''
  form.managerPhone = ''
  form.address = ''
  form.status = 1
  form.remark = ''
  dialogVisible.value = true
  fetchUsers()
  selectedManagerId.value = undefined
}

const handleEdit = (row: any) => {
  Object.assign(form, row)
  dialogVisible.value = true
  fetchUsers().then(() => {
    const matched = userOptions.value.find((u:any) => (u.name || u.realName || u.nickname || u.username) === form.managerName)
    selectedManagerId.value = matched?.id
    if (matched?.phone && !form.managerPhone) {
      form.managerPhone = matched.phone
    }
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该仓库吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteWarehouse(row.id)
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      if (form.id) {
        await updateWarehouse(form)
        ElMessage.success('更新成功')
      } else {
        await createWarehouse(form)
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

const fetchUsers = async () => {
  const res: any = await request({ url: '/system/user/list', method: 'get', params: { size: 200 } })
  userOptions.value = res.records || res || []
}

const onManagerChange = (id: number) => {
  const u = userOptions.value.find((x:any) => x.id === id)
  form.managerName = u?.name || u?.realName || u?.nickname || u?.username || ''
  form.managerPhone = u?.phone || form.managerPhone
}
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
