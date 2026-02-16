<template>
  <div class="app-container">
    <h2 class="page-title">编号规则管理</h2>
    <div class="header-actions">
      <el-button v-if="canCreate" type="primary" @click="handleAdd">新增规则</el-button>
    </div>

    <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
      <el-table-column prop="codeName" label="编号名称" width="150" align="center" />
      <el-table-column prop="element1" label="元素1" width="120" align="center" />
      <el-table-column prop="separator" label="分隔符" width="80" align="center" />
      <el-table-column prop="element2" label="元素2" width="120" align="center" />
      <el-table-column prop="preview" label="编号预览" align="center" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑编号规则' : '新增编号规则'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="规则编码" prop="code">
          <el-input v-model="form.code" placeholder="唯一标识，如：CUST_CODE" />
        </el-form-item>
        <el-form-item label="编号名称" prop="codeName">
          <el-input v-model="form.codeName" placeholder="如：客户编码" />
        </el-form-item>
        <el-form-item label="元素1" prop="element1">
           <el-input v-model="form.element1" placeholder="数字或英文，最多9位" maxlength="9" />
        </el-form-item>
        <el-form-item label="分隔符" prop="separator">
           <el-input v-model="form.separator" placeholder="如：-" maxlength="1" />
        </el-form-item>
        <el-form-item label="元素2" prop="element2">
           <el-input v-model="form.element2" placeholder="数字或英文，最多9位" maxlength="9" />
        </el-form-item>
        <el-form-item label="预览">
           <el-input :value="previewCode" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit(formRef)">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

// 权限检查函数
const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canCreate = computed(() => hasPermission('NR:create'))
const canUpdate = computed(() => hasPermission('NR:update'))
const canDelete = computed(() => hasPermission('NR:delete'))

const tableData = ref<any[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const formRef = ref<FormInstance>()
const form = reactive<any>({
  id: undefined,
  code: '',
  codeName: '',
  element1: '',
  separator: '',
  element2: '',
  rule: '',
  preview: ''
})

const rules = reactive<FormRules>({
  code: [{ required: true, message: '请输入规则编码', trigger: 'blur' }],
  codeName: [{ required: true, message: '请输入编号名称', trigger: 'blur' }]
})

const previewCode = computed(() => {
  return `${form.element1 || ''}${form.separator || ''}${form.element2 || ''}`
})

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await request({
      url: '/sys/code-rule/list',
      method: 'get',
      params: { current: currentPage.value, size: pageSize.value }
    })
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  form.id = undefined
  form.code = ''
  form.codeName = ''
  form.element1 = ''
  form.separator = ''
  form.element2 = ''
  form.rule = ''
  form.preview = ''
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该规则吗？', '提示', { type: 'warning' }).then(async () => {
    await request({
      url: `/sys/code-rule/${row.id}`,
      method: 'delete'
    })
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleSubmit = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      // 构造 rule 和 preview
      form.preview = previewCode.value
      form.rule = previewCode.value // 简单逻辑：rule 等于预览字符串
      
      if (form.id) {
        await request({
          url: `/sys/code-rule/${form.id}`,
          method: 'put',
          data: form
        })
        ElMessage.success('更新成功')
      } else {
        await request({
          url: '/sys/code-rule',
          method: 'post',
          data: form
        })
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
  margin-bottom: 20px;
}
.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
