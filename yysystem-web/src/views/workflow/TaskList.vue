<template>
  <div class="app-container">
    <div class="toolbar">
      <el-button type="primary" @click="fetchTasks">刷新</el-button>
    </div>
    <el-table :data="tasks" border v-loading="loading">
      <el-table-column prop="module" label="业务模块" width="120" />
      <el-table-column prop="businessTitle" label="摘要" min-width="180" />
      <el-table-column prop="businessAmount" label="金额" width="120" />
      <el-table-column prop="assigneeRole" label="审批角色" width="140" />
      <el-table-column prop="stepIndex" label="步骤" width="80" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="scope">
          <el-button type="primary" link @click="onApprove(scope.row)">同意</el-button>
          <el-button type="danger" link @click="onReject(scope.row)">驳回</el-button>
          <el-button type="warning" link @click="onAddSign(scope.row)">加签</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
  <el-dialog v-model="signDialogVisible" title="加签" width="420px">
    <el-form :model="signForm" label-width="120px">
      <el-form-item label="加签角色">
        <el-select v-model="signForm.roleName" filterable clearable style="width:100%">
          <el-option label="部门主管" value="部门主管" />
          <el-option label="财务经理" value="财务经理" />
          <el-option label="分管副总" value="分管副总" />
          <el-option label="总经理" value="总经理" />
        </el-select>
      </el-form-item>
      <el-form-item label="指定用户ID">
        <el-input v-model="signForm.userId" placeholder="可选，不填按角色自动指派" />
      </el-form-item>
    </el-form>
    <template #footer>
      <span style="display:flex;gap:10px;justify-content:flex-end;">
        <el-button @click="signDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitAddSign">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyTasks, approveTask, rejectTask, addSignTask } from '@/api/workflow'

const tasks = ref<any[]>([])
const loading = ref(false)
const signDialogVisible = ref(false)
const signForm = ref<{ roleName: string, userId?: number, taskId?: number }>({ roleName: '' })

const fetchTasks = async () => {
  loading.value = true
  try {
    const res:any = await getMyTasks()
    tasks.value = Array.isArray(res) ? res : (res?.records || [])
  } finally {
    loading.value = false
  }
}
const onApprove = async (row:any) => {
  const remark = await ElMessageBox.prompt('输入审批备注（可选）', '同意', { inputValue: '', confirmButtonText: '确定', cancelButtonText: '取消' }).then(x => x.value).catch(() => '')
  await approveTask(row.id, remark || '')
  ElMessage.success('已同意')
  fetchTasks()
}
const onReject = async (row:any) => {
  const remark = await ElMessageBox.prompt('输入驳回原因（必填）', '驳回', { inputValue: '', confirmButtonText: '确定', cancelButtonText: '取消' }).then(x => x.value).catch(() => '')
  if (!remark) { ElMessage.warning('请输入驳回原因'); return }
  await rejectTask(row.id, remark)
  ElMessage.success('已驳回')
  fetchTasks()
}
const onAddSign = (row:any) => {
  signForm.value = { roleName: '', taskId: row.id }
  signDialogVisible.value = true
}
const submitAddSign = async () => {
  if (!signForm.value.taskId) return
  await addSignTask(signForm.value.taskId, signForm.value.roleName || '', signForm.value.userId)
  ElMessage.success('已加签')
  signDialogVisible.value = false
  fetchTasks()
}
onMounted(() => {
  fetchTasks()
})
</script>
<style scoped>
.app-container { padding: 20px; }
.toolbar { display:flex; gap:10px; align-items:center; margin-bottom: 12px; }
</style>
