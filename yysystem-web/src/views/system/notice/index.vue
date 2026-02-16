<template>
  <div class="app-container">
    <h2 class="page-title">通告管理</h2>

    <!-- 搜索栏 -->
    <div class="toolbar">
      <el-input v-model="queryParams.title" placeholder="通告标题" clearable style="width: 200px; margin-right: 10px;" @keyup.enter="handleQuery" />
      <el-select v-model="queryParams.status" placeholder="发布状态" clearable style="width: 150px; margin-right: 10px;">
        <el-option label="已发布" value="PUBLISHED" />
        <el-option label="草稿" value="DRAFT" />
      </el-select>
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
      <el-button v-if="canCreate" type="primary" @click="handleAdd">新增通告</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="title" label="通告标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="content" label="通告内容" min-width="300" show-overflow-tooltip />
      <el-table-column prop="publisherName" label="发布人" width="120" align="center" />
      <el-table-column prop="publishTime" label="发布时间" width="180" align="center" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'PUBLISHED' ? 'success' : 'info'">
            {{ scope.row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button v-if="canUpdate" link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button v-if="canDelete" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入通告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入通告内容" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio label="PUBLISHED">立即发布</el-radio>
            <el-radio label="DRAFT">保存草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

// 权限检查函数
const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canCreate = computed(() => hasPermission('NM:create'))
const canUpdate = computed(() => hasPermission('NM:update'))
const canDelete = computed(() => hasPermission('NM:delete'))

const loading = ref(false)
const tableData = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增通告')
const formRef = ref<FormInstance>()

const queryParams = reactive({
  title: '',
  status: ''
})

const form = reactive({
  id: undefined as number | undefined,
  title: '',
  content: '',
  status: 'PUBLISHED'
})

const rules = reactive<FormRules>({
  title: [{ required: true, message: '请输入通告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入通告内容', trigger: 'blur' }]
})

const fetchData = async () => {
  loading.value = true
  try {
    const params: any = {
      current: currentPage.value,
      size: pageSize.value,
      ...queryParams
    }
    const res: any = await request({ url: '/system/notice/page', method: 'get', params })
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取通告列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  currentPage.value = 1
  fetchData()
}

const resetQuery = () => {
  queryParams.title = ''
  queryParams.status = ''
  handleQuery()
}

const handleAdd = () => {
  dialogTitle.value = '新增通告'
  form.id = undefined
  form.title = ''
  form.content = ''
  form.status = 'PUBLISHED'
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑通告'
  form.id = row.id
  form.title = row.title
  form.content = row.content
  form.status = row.status || 'DRAFT'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const url = form.id ? '/system/notice/update' : '/system/notice/create'
        const method = form.id ? 'put' : 'post'
        await request({ url, method, data: form })
        ElMessage.success(form.id ? '修改成功' : '新增成功')
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该通告吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request({ url: `/system/notice/delete/${row.id}`, method: 'delete' })
    ElMessage.success('删除成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
.pagination-container {
  margin-top: 16px;
  text-align: right;
}
</style>
