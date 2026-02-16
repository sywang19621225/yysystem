<template>
  <div class="app-container">
    <h2 class="page-title">角色管理</h2>
    <div class="header-actions">
      <el-button type="primary" @click="handleAdd">新增角色</el-button>
    </div>

    <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
      <el-table-column prop="roleName" label="角色名称" width="150" align="center" />
      <el-table-column prop="createTime" label="创建时间" width="200" align="center" />
      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑角色' : '新增角色'" width="1000px" :before-close="onDialogBeforeClose">
      <div class="dialog-top-actions">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit(formRef)">保存</el-button>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="权限勾选">
          <div style="width:100%;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
              <el-input v-model="permSearch" placeholder="搜索权限名称或编码" clearable style="width: 280px;" />
              <div style="display:flex; gap:8px;">
                <el-button size="small" @click="selectAllPerms">全选</el-button>
                <el-button size="small" @click="clearAllPerms">清空</el-button>
              </div>
            </div>
            
            <!-- 系统设置分组 -->
            <div class="perm-group perm-group-system">
              <div class="perm-group-header">
                <span class="perm-group-title">系统设置</span>
                <el-checkbox v-model="systemSettingsAll" @change="toggleSystemSettings">全选系统设置</el-checkbox>
              </div>
              <div class="perm-group-desc">包含：个人中心、用户管理、角色管理、权限管理、团队管理、部门管理、通告管理、编号规则、字典设置、操作日志</div>
            </div>
            
            <div v-for="g in groupedPermList" :key="g.resource" style="margin-bottom:12px;">
              <!-- 销售管理分组 - 显示在客户管理之前 -->
              <template v-if="g.resource === 'CM'">
                <div class="perm-group perm-group-sales">
                  <div class="perm-group-header">
                    <span class="perm-group-title">销售管理</span>
                    <el-checkbox v-model="salesManagementAll" @change="toggleSalesManagement">全选销售管理</el-checkbox>
                  </div>
                  <div class="perm-group-desc">包含：客户管理、客户联系人、项目管理、方案管理、报价管理、合同订单</div>
                </div>
              </template>
              
              <div style="display:flex; justify-content:space-between; align-items:center; margin:6px 0;">
                <span style="font-weight:600;">{{ g.name }}</span>
                <div style="display:flex; gap:8px;">
                  <el-button size="small" @click="selectModule(g.resource)">全选该模块</el-button>
                  <el-button size="small" @click="clearModule(g.resource)">清空该模块</el-button>
                </div>
              </div>
              <el-checkbox-group v-model="selectedPermissionIds" class="perm-checkbox-group">
                <el-checkbox
                  v-for="p in g.items"
                  :key="p.id"
                  :label="p.id"
                  class="perm-checkbox"
                >
                  {{ p.permName }}
                </el-checkbox>
              </el-checkbox-group>
              
              <!-- 财务管理分组 - 显示在合同订单之后 -->
              <template v-if="g.resource === 'CO'">
                <div class="perm-group perm-group-finance">
                  <div class="perm-group-header">
                    <span class="perm-group-title">财务管理</span>
                    <el-checkbox v-model="financeManagementAll" @change="toggleFinanceManagement">全选财务管理</el-checkbox>
                  </div>
                  <div class="perm-group-desc">包含：费用报销、收支管理、发票管理、保证金控、佣金管理</div>
                </div>
              </template>
              
              <!-- 采购管理分组 - 显示在佣金管理之后 -->
              <template v-if="g.resource === 'IFM'">
                <div class="perm-group perm-group-purchase">
                  <div class="perm-group-header">
                    <span class="perm-group-title">采购管理</span>
                    <el-checkbox v-model="purchaseManagementAll" @change="togglePurchaseManagement">全选采购管理</el-checkbox>
                  </div>
                  <div class="perm-group-desc">包含：供方管理、商品管理、商品采购、成本核算</div>
                </div>
              </template>
              
              <!-- 部署维护分组 - 显示在成本核算之后 -->
              <template v-if="g.resource === 'PCC'">
                <div class="perm-group perm-group-ops">
                  <div class="perm-group-header">
                    <span class="perm-group-title">部署维护</span>
                    <el-checkbox v-model="opsManagementAll" @change="toggleOpsManagement">全选部署维护</el-checkbox>
                  </div>
                  <div class="perm-group-desc">包含：设备安装、设备维修</div>
                </div>
              </template>
              
              <!-- 仓库管理分组 - 显示在设备维修之后 -->
              <template v-if="g.resource === 'EM'">
                <div class="perm-group perm-group-warehouse">
                  <div class="perm-group-header">
                    <span class="perm-group-title">仓库管理</span>
                    <el-checkbox v-model="warehouseManagementAll" @change="toggleWarehouseManagement">全选仓库管理</el-checkbox>
                  </div>
                  <div class="perm-group-desc">包含：入库管理、出库管理、库存管理、库存盘点、出入流水</div>
                </div>
              </template>
              
              <!-- 统计报表分组 - 显示在库存查询之后 -->
              <template v-if="g.resource === 'ST'">
                <div class="perm-group perm-group-report">
                  <div class="perm-group-header">
                    <span class="perm-group-title">统计报表</span>
                    <el-checkbox v-model="reportManagementAll" @change="toggleReportManagement">全选统计报表</el-checkbox>
                  </div>
                  <div class="perm-group-desc">包含：销售分析、采购分析、库存分析、财务分析、决策分析</div>
                </div>
              </template>
            </div>
          </div>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const permissionOptions = ref<any[]>([])
const selectedPermissionIds = ref<any[]>([])
const permSearch = ref('')
const formRef = ref<FormInstance>()
const systemSettingsAll = ref(false)
const salesManagementAll = ref(false)
const financeManagementAll = ref(false)
const purchaseManagementAll = ref(false)
const opsManagementAll = ref(false)
const warehouseManagementAll = ref(false)
const reportManagementAll = ref(false)

// 系统设置包含的模块
const systemSettingsModules = ['PC', 'UM', 'RM', 'PM', 'TM', 'DM', 'NM', 'NR', 'GS', 'OL']

// 销售管理包含的模块
const salesManagementModules = ['CM', 'CC', 'PF', 'SOL', 'QM', 'CO']

// 财务管理包含的模块
const financeManagementModules = ['ER', 'IER', 'IM', 'DeM', 'IFM']

// 采购管理包含的模块
const purchaseManagementModules = ['SM', 'PDM', 'PA', 'PCC']

// 部署维护包含的模块
const opsManagementModules = ['PI', 'EM']

// 仓库管理包含的模块
const warehouseManagementModules = ['WHM', 'OM', 'WM', 'IC', 'IOF']

// 统计报表包含的模块
const reportManagementModules = ['SR', 'PR', 'IA', 'FA', 'DA']

// 系统设置全选/取消
const toggleSystemSettings = (val: boolean) => {
  const systemPermIds = permissionOptions.value
    .filter((p: any) => systemSettingsModules.includes(p.resource || String(p.permCode||'').split(':')[0]))
    .map((p: any) => p.id)
  
  if (val) {
    // 全选系统设置
    const set = new Set<number>([...selectedPermissionIds.value, ...systemPermIds])
    selectedPermissionIds.value = Array.from(set)
  } else {
    // 取消系统设置
    selectedPermissionIds.value = selectedPermissionIds.value.filter((id: number) => !systemPermIds.includes(id))
  }
}

// 销售管理全选/取消
const toggleSalesManagement = (val: boolean) => {
  const salesPermIds = permissionOptions.value
    .filter((p: any) => salesManagementModules.includes(p.resource || String(p.permCode||'').split(':')[0]))
    .map((p: any) => p.id)
  
  if (val) {
    // 全选销售管理
    const set = new Set<number>([...selectedPermissionIds.value, ...salesPermIds])
    selectedPermissionIds.value = Array.from(set)
  } else {
    // 取消销售管理
    selectedPermissionIds.value = selectedPermissionIds.value.filter((id: number) => !salesPermIds.includes(id))
  }
}

// 财务管理全选/取消
const toggleFinanceManagement = (val: boolean) => {
  const financePermIds = permissionOptions.value
    .filter((p: any) => financeManagementModules.includes(p.resource || String(p.permCode||'').split(':')[0]))
    .map((p: any) => p.id)
  
  if (val) {
    // 全选财务管理
    const set = new Set<number>([...selectedPermissionIds.value, ...financePermIds])
    selectedPermissionIds.value = Array.from(set)
  } else {
    // 取消财务管理
    selectedPermissionIds.value = selectedPermissionIds.value.filter((id: number) => !financePermIds.includes(id))
  }
}

// 采购管理全选/取消
const togglePurchaseManagement = (val: boolean) => {
  const purchasePermIds = permissionOptions.value
    .filter((p: any) => purchaseManagementModules.includes(p.resource || String(p.permCode||'').split(':')[0]))
    .map((p: any) => p.id)
  
  if (val) {
    // 全选采购管理
    const set = new Set<number>([...selectedPermissionIds.value, ...purchasePermIds])
    selectedPermissionIds.value = Array.from(set)
  } else {
    // 取消采购管理
    selectedPermissionIds.value = selectedPermissionIds.value.filter((id: number) => !purchasePermIds.includes(id))
  }
}

// 部署维护全选/取消
const toggleOpsManagement = (val: boolean) => {
  const opsPermIds = permissionOptions.value
    .filter((p: any) => opsManagementModules.includes(p.resource || String(p.permCode||'').split(':')[0]))
    .map((p: any) => p.id)
  
  if (val) {
    // 全选部署维护
    const set = new Set<number>([...selectedPermissionIds.value, ...opsPermIds])
    selectedPermissionIds.value = Array.from(set)
  } else {
    // 取消部署维护
    selectedPermissionIds.value = selectedPermissionIds.value.filter((id: number) => !opsPermIds.includes(id))
  }
}

// 仓库管理全选/取消
const toggleWarehouseManagement = (val: boolean) => {
  const warehousePermIds = permissionOptions.value
    .filter((p: any) => warehouseManagementModules.includes(p.resource || String(p.permCode||'').split(':')[0]))
    .map((p: any) => p.id)
  
  if (val) {
    // 全选仓库管理
    const set = new Set<number>([...selectedPermissionIds.value, ...warehousePermIds])
    selectedPermissionIds.value = Array.from(set)
  } else {
    // 取消仓库管理
    selectedPermissionIds.value = selectedPermissionIds.value.filter((id: number) => !warehousePermIds.includes(id))
  }
}

// 统计报表全选/取消
const toggleReportManagement = (val: boolean) => {
  const reportPermIds = permissionOptions.value
    .filter((p: any) => reportManagementModules.includes(p.resource || String(p.permCode||'').split(':')[0]))
    .map((p: any) => p.id)
  
  if (val) {
    // 全选统计报表
    const set = new Set<number>([...selectedPermissionIds.value, ...reportPermIds])
    selectedPermissionIds.value = Array.from(set)
  } else {
    // 取消统计报表
    selectedPermissionIds.value = selectedPermissionIds.value.filter((id: number) => !reportPermIds.includes(id))
  }
}

const form = reactive<any>({
  id: undefined,
  roleName: '',
  permissions: ''
})
const originalSnapshot = ref<any>({})

const rules = reactive<FormRules>({
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
})

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await request({
      url: '/system/role/list',
      method: 'get',
      params: { current: currentPage.value, size: pageSize.value }
    })
    if (res && res.records) {
      tableData.value = res.records
      total.value = res.total
    } else {
      tableData.value = []
      total.value = 0
    }
  } finally {
    loading.value = false
  }
}

const fetchPermissionOptions = async () => {
  const res: any = await request({ url: '/system/permission/list', method: 'get', params: { current: 1, size: 1000 } })
  const list = res?.records || res || []
  permissionOptions.value = list
}
const filteredPermissionOptions = computed(() => {
  const kw = permSearch.value.trim().toLowerCase()
  if (!kw) return permissionOptions.value
  return permissionOptions.value.filter((p:any) => String(p.permName||'').toLowerCase().includes(kw) || String(p.permCode||'').toLowerCase().includes(kw))
})
const resourceNameMap: Record<string, string> = {
  PC: '个人中心',
  UM: '用户管理',
  RM: '角色管理',
  PM: '权限管理',
  PDM: '商品管理',
  TM: '团队管理',
  DM: '部门管理',
  NM: '通告管理',
  NR: '编号规则',
  GS: '字典设置',
  PCC: '成本核算',
  CM: '客户管理',
  CC: '客户联系人',
  PF: '项目管理',
  SOL: '方案管理',
  QM: '报价管理',
  CO: '合同订单',
  ER: '费用报销',
  IER: '收支管理',
  IM: '发票管理',
  DeM: '保证金控',
  IFM: '佣金管理',
  SM: '供方管理',
  PDM: '商品管理',
  PA: '商品采购',
  PCC: '成本核算',
  WM: '库存管理',
  WHM: '入库管理',
  OM: '出库管理',
  IOF: '出入流水',
  IC: '库存盘点',
  PI: '设备安装',
  EM: '设备维修',
  SR: '销售分析',
  PR: '采购分析',
  IA: '库存分析',
  FA: '财务分析',
  DA: '决策分析',
  ST: '库存查询',
  OL: '操作日志',
  system: '系统管理'
}
const resourceOrder = ['PC','UM','RM','PM','TM','DM','NM','NR','GS','OL','CM','CC','PF','SOL','QM','CO','ER','IER','IM','DeM','IFM','SM','PDM','PA','PCC','PI','EM','WHM','OM','WM','IC','ST','IOF','SR','PR','IA','FA','DA','system']
const normalizeResource = (res: string) => {
  if (!res) return res
  if (res === 'system/user') return 'UM'
  if (res === 'system/permission') return 'PM'
  return res
}
const groupedPermList = computed(() => {
  const list = filteredPermissionOptions.value || []
  const groups: Record<string, any[]> = {}
  list.forEach((p:any) => {
    const resRaw = p.resource || String(p.permCode||'').split(':')[0]
    const res = normalizeResource(resRaw)
    if (!groups[res]) groups[res] = []
    groups[res].push(p)
  })
  const result = Object.keys(groups).map(res => ({
    resource: res,
    name: resourceNameMap[res] || res,
    items: groups[res]
  }))
  result.sort((a,b) => {
    const ai = resourceOrder.indexOf(a.resource)
    const bi = resourceOrder.indexOf(b.resource)
    if (ai === -1 && bi === -1) return a.name.localeCompare(b.name, 'zh')
    if (ai === -1) return 1
    if (bi === -1) return -1
    return ai - bi
  })
  return result
})
const selectAllPerms = () => {
  selectedPermissionIds.value = permissionOptions.value.map((p:any) => p.id)
}
const clearAllPerms = () => {
  selectedPermissionIds.value = []
}
const selectModule = (res: string) => {
  const ids = (permissionOptions.value || []).filter((p:any) => (p.resource || String(p.permCode||'').split(':')[0]) === res).map((p:any) => p.id)
  const set = new Set<number>([...selectedPermissionIds.value, ...ids])
  selectedPermissionIds.value = Array.from(set)
}
const clearModule = (res: string) => {
  const ids = new Set<number>((permissionOptions.value || []).filter((p:any) => (p.resource || String(p.permCode||'').split(':')[0]) === res).map((p:any) => p.id))
  selectedPermissionIds.value = (selectedPermissionIds.value || []).filter((id:number) => !ids.has(id))
}

const handleAdd = () => {
  form.id = undefined
  form.roleName = ''
  form.permissions = ''
  dialogVisible.value = true
  nextTick(() => {
    selectedPermissionIds.value = []
    originalSnapshot.value = { form: JSON.parse(JSON.stringify(form)), selected: JSON.parse(JSON.stringify(selectedPermissionIds.value)) }
  })
}

  const handleEdit = (row: any) => {
    Object.assign(form, row)
    dialogVisible.value = true
    nextTick(() => {
      let ids: any[] = []
      try {
        ids = row.permissions ? JSON.parse(row.permissions) : []
      } catch (e) {
        ids = row.permissions ? String(row.permissions).split(',').map((x:string)=>Number(x)).filter(Boolean) : []
      }
      selectedPermissionIds.value = ids
      originalSnapshot.value = { form: JSON.parse(JSON.stringify(form)), selected: JSON.parse(JSON.stringify(selectedPermissionIds.value)) }
    })
  }
  
  const handleDelete = (row: any) => {
    ElMessageBox.confirm('确认删除该角色吗？', '提示', { type: 'warning' }).then(async () => {
      await request({
        url: `/system/role/${row.id}`,
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
        form.permissions = JSON.stringify(selectedPermissionIds.value || [])
      
        if (form.id) {
          await request({
            url: '/system/role',
            method: 'put',
            data: form
          })
          ElMessage.success('更新成功')
        } else {
          await request({
            url: '/system/role',
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
  fetchPermissionOptions()
  window.addEventListener('beforeunload', beforeUnloadHandler)
})
onUnmounted(() => {
  window.removeEventListener('beforeunload', beforeUnloadHandler)
})

const isChanged = () => {
  const a = JSON.stringify({ form, selected: selectedPermissionIds.value })
  const b = JSON.stringify(originalSnapshot.value || {})
  return a !== b
}
const onDialogBeforeClose = async (done: () => void) => {
  if (!isChanged()) {
    done()
    return
  }
  try {
    await ElMessageBox.confirm('内容已修改，是否保存？', '提示', { confirmButtonText: '保存并关闭', cancelButtonText: '直接关闭' })
    await handleSubmit(formRef.value)
    done()
  } catch {
    done()
  }
}
const beforeUnloadHandler = (e: BeforeUnloadEvent) => {
  if (dialogVisible.value && isChanged()) {
    e.preventDefault()
    e.returnValue = ''
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.header-actions {
  margin-bottom: 20px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.dialog-top-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-bottom: 10px;
}
.perm-checkbox-group {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.perm-checkbox {
  margin: 0;
  height: auto;
  line-height: 1.5;
}

/* 权限分组统一样式 */
.perm-group {
  margin-bottom: 16px;
  padding: 12px;
  border-radius: 4px;
  border-left: 4px solid;
}

.perm-group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.perm-group-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.perm-group-desc {
  font-size: 12px;
  color: #909399;
}

/* 各分组颜色 */
.perm-group-system {
  background: #f5f7fa;
  border-left-color: #909399;
}

.perm-group-sales {
  background: #ecf5ff;
  border-left-color: #409eff;
}

.perm-group-finance {
  background: #f0f9ff;
  border-left-color: #67c23a;
}

.perm-group-purchase {
  background: #f6ffed;
  border-left-color: #52c41a;
}

.perm-group-ops {
  background: #fff7e6;
  border-left-color: #fa8c16;
}

.perm-group-warehouse {
  background: #e6fffb;
  border-left-color: #13c2c6;
}

.perm-group-report {
  background: #f9f0ff;
  border-left-color: #722ed1;
}

@media (max-width: 768px) {
  .perm-checkbox-group {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
