<template>
  <div class="public-pool">
    <div class="search-bar">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="客户名称">
          <el-input v-model="queryParams.customerName" placeholder="请输入客户名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="tableData" style="width: 100%" v-loading="loading" border>
      <el-table-column prop="customerCode" label="客户编号" width="150" />
      <el-table-column prop="customerName" label="客户名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="industry" label="行业" width="120" />
      <el-table-column prop="source" label="来源" width="100" />
      <el-table-column prop="level" label="等级" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleReceive(scope.row)">领取</el-button>
          <el-popover placement="top" :width="280" trigger="click">
            <template #reference>
              <el-button link type="warning">分配</el-button>
            </template>
            <el-select v-model="assignUserId" filterable placeholder="选择用户" style="width: 240px">
              <el-option v-for="u in userOptions" :key="u.id" :label="u.name || u.realName || u.nickname || u.username" :value="u.id" />
            </el-select>
            <div style="margin-top: 10px; text-align: right;">
              <el-button type="primary" size="small" @click="handleAssign(scope.row)">确定</el-button>
            </div>
          </el-popover>
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
  </div>
 </template>

 <script setup lang="ts">
 import { ref, reactive, onMounted } from 'vue'
 import request from '@/utils/request'
 import { ElMessage } from 'element-plus'

 const tableData = ref<any[]>([])
 const loading = ref(false)
 const currentPage = ref(1)
 const pageSize = ref(10)
 const total = ref(0)
 const assignUserId = ref<number | null>(null)
 const userOptions = ref<any[]>([])

 const queryParams = reactive({
   customerName: ''
 })

 const fetchData = async () => {
   loading.value = true
   try {
     const res: any = await request({
       url: '/crm/customer/public/list',
       method: 'get',
       params: {
         current: currentPage.value,
         size: pageSize.value,
         customerName: queryParams.customerName
       }
     })
     tableData.value = res.records
     total.value = res.total
   } finally {
     loading.value = false
   }
 }

 const handleSearch = () => {
   currentPage.value = 1
   fetchData()
 }

 const handleReset = () => {
   queryParams.customerName = ''
   handleSearch()
 }

 const handleReceive = async (row: any) => {
   await request({ url: `/crm/customer/receive/${row.id}`, method: 'post' })
   ElMessage.success('领取成功')
   fetchData()
 }

 const handleAssign = async (row: any) => {
  if (!assignUserId.value) {
    ElMessage.warning('请输入要分配的用户ID')
    return
  }
  await request({ url: `/crm/customer/assign/${row.id}`, method: 'post', params: { userId: assignUserId.value } })
  ElMessage.success('分配成功')
  assignUserId.value = null
  fetchData()
}

onMounted(() => {
  fetchData()
  request({ url: '/system/user/list', method: 'get', params: { size: 100 } }).then((res: any) => {
    userOptions.value = res.records || []
  })
})
 </script>

 <style scoped>
 .public-pool {
   padding: 20px;
 }
 .search-bar {
   margin-bottom: 20px;
 }
 .pagination {
   margin-top: 20px;
   display: flex;
   justify-content: flex-end;
 }
 </style>
