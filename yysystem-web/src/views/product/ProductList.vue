<template>
  <div class="app-container">
    <h2 class="page-title">商品管理</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #409EFF;">{{ statistics.totalCount || 0 }}</div>
            <div style="color: #909399; margin-top: 8px;">商品总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #67C23A;">{{ statistics.enabledCount || 0 }}</div>
            <div style="color: #909399; margin-top: 8px;">启用商品</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #E6A23C;">{{ statistics.totalStock || 0 }}</div>
            <div style="color: #909399; margin-top: 8px;">总库存</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold; color: #F56C6C;">{{ statistics.categoryCount || 0 }}</div>
            <div style="color: #909399; margin-top: 8px;">商品类别数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索栏 -->
    <div class="toolbar">
      <el-input v-model="queryParams.productName" placeholder="商品名称" clearable style="width: 200px; margin-right: 10px;" @keyup.enter="handleSearch" />
      <el-select v-model="queryParams.brand" placeholder="品牌" clearable filterable style="width: 150px; margin-right: 10px;">
        <el-option v-for="b in brandOptions" :key="b.value" :label="b.label" :value="b.value" />
      </el-select>
      <el-select v-model="queryParams.productCategory" placeholder="商品类别" clearable filterable style="width: 150px; margin-right: 10px;">
        <el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button v-if="canCreate" type="primary" @click="handleAdd">新增商品</el-button>
      <el-button v-if="canUpdate" type="warning" @click="openConfig">扩展字段设置</el-button>
    </div>

    <el-table :data="tableData" style="width: 100%" v-loading="loading" border>
      <el-table-column prop="productCode" label="商品编号" width="180" />
      <el-table-column prop="productName" label="商品名称" show-overflow-tooltip />
      <el-table-column prop="productCategory" label="类别" width="120" />
      <el-table-column prop="productUnit" label="单位" width="80" />
      <el-table-column prop="costPrice" label="成本价" width="100" />
      <el-table-column prop="warehouseName" label="仓库" width="120" />
      <el-table-column prop="stockQuantity" label="库存" width="100" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="scope">
          <el-button link :type="scope.row.remark ? 'primary' : 'info'" :disabled="!scope.row.remark" @click="handleViewParam(scope.row)">参数</el-button>
          <el-button link :type="scope.row.caseImages ? 'primary' : 'info'" :disabled="!scope.row.caseImages" @click="handleViewCase(scope.row)">案例</el-button>
          <el-button v-if="canUpdate" link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button v-if="canDelete" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="fetchData"
        @size-change="onMainSizeChange"
      />
    </div>

    <!-- Param Preview Dialog -->
    <el-dialog v-model="paramVisible" title="投标参数预览" width="600px">
      <div style="white-space: pre-wrap; word-break: break-all; min-height: 100px;">
        {{ currentRemark || '暂无参数信息' }}
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="handleCopy">复制</el-button>
          <el-button @click="paramVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Case Images Dialog -->
    <el-dialog v-model="caseVisible" title="案例图片" width="800px">
      <div v-if="currentCaseImages.length === 0" style="text-align: center; color: #999; padding: 20px;">暂无案例图片</div>
      <div v-else style="display: flex; flex-wrap: wrap; gap: 15px;">
        <div v-for="(img, index) in currentCaseImages" :key="index" style="width: 150px; text-align: center;">
          <el-image 
            :src="img.url" 
            :preview-src-list="currentCaseImages.map(i => i.url)"
            fit="cover"
            style="width: 150px; height: 150px; border-radius: 4px; border: 1px solid #eee;"
            :initial-index="index"
            hide-on-click-modal
          />
          <div style="margin-top: 5px;">
            <el-button link type="primary" size="small" @click="handleDownload({ url: img.url })">下载</el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新增商品'" width="800px" top="5vh" :before-close="onDialogBeforeClose">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <div class="image-side">
          <el-card class="image-card" shadow="never">
            <div class="image-box">
              <div class="image-label-vertical">上传真实商品的全图</div>
              <div class="image-uploader">
                <el-upload
                  class="avatar-uploader"
                  action="/api/common/upload"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :on-success="handleImageSuccess"
                  :before-upload="beforeImageUpload"
                >
                  <el-image v-if="form.productImage" :src="form.productImage" class="avatar" lazy fit="cover" :preview-src-list="[form.productImage]">
                    <template #placeholder>
                      <div class="image-placeholder">加载中...</div>
                    </template>
                    <template #error>
                      <div class="image-error">加载失败</div>
                    </template>
                  </el-image>
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
              </div>
            </div>
          </el-card>
        </div>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="商品编号">
              <div style="display:flex;gap:8px;">
                <el-input v-model="form.productCode" placeholder="自动生成" disabled class="code-input" />
                <el-button type="primary" link @click="generateProductCode">生成编号</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="商品名称" prop="productName">
              <el-input v-model="form.productName" placeholder="请按照商品的正式名称填写，不要自定义名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="英文名称" prop="productNameEn">
              <el-input v-model="form.productNameEn" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="规格/型号" prop="productSpec">
              <el-input v-model="form.productSpec" placeholder="请按照商品的正式型号填写，无型号的产品填写规格尺寸" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品系列" prop="productSeries">
              <el-select v-model="form.productSeries" placeholder="请选择" style="width: 100%" allow-create filterable default-first-option>
                <el-option v-for="item in seriesOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品单位" prop="productUnit">
              <el-select v-model="form.productUnit" placeholder="请选择" style="width: 100%" allow-create filterable default-first-option>
                <el-option v-for="item in unitOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="品牌" prop="brand">
              <el-select v-model="form.brand" placeholder="请选择或输入" style="width: 100%" filterable clearable allow-create default-first-option>
                <el-option v-for="b in brandOptions" :key="b.value" :label="b.label" :value="b.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>


        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品类别" prop="productCategory">
              <el-select v-model="form.productCategory" placeholder="请选择" style="width: 100%">
                <el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="form.supplierId" placeholder="自产的商品选择安徽公司，其它按实际供货单位选择" style="width: 100%" clearable filterable>
                <el-option v-for="s in supplierOptions" :key="s.id" :label="s.supplierName" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="成本单价" prop="costPrice">
              <el-input v-model.number="form.costPrice" type="number" placeholder="000.00" style="width: 100%" class="price-input" @blur="onCostPriceBlur" @input="onCostPriceInput">
                <template #prepend>￥</template>
              </el-input>
            </el-form-item>
            <div style="margin-top: 4px; margin-left: 80px;">
              <el-checkbox v-model="includeTax" @change="onTaxChange">含税（13%）</el-checkbox>
            </div>
          </el-col>
          <el-col :span="8">
            <el-form-item label="渠道单价" prop="channelPrice">
              <el-input v-model.number="form.channelPrice" type="number" placeholder="000.00" style="width: 100%" class="price-input" @blur="formatPrice('channelPrice')">
                <template #prepend>￥</template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="终端单价" prop="terminalPrice">
              <el-input v-model.number="form.terminalPrice" type="number" placeholder="000.00" style="width: 100%" class="price-input" @blur="formatPrice('terminalPrice')">
                <template #prepend>￥</template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="仓库" prop="warehouseId">
              <el-select v-model="form.warehouseId" placeholder="供应商直发客户的商品选择外转仓库" style="width: 100%" filterable>
                 <el-option v-for="w in warehouseOptions" :key="w.id" :label="w.warehouseName" :value="w.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存数量">
              <el-input-number v-model="form.stockQuantity" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品状态">
              <el-switch
                v-model="form.status"
                :active-value="1"
                :inactive-value="0"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="投标参数" prop="remark">
          <el-input type="textarea" v-model="form.remark" rows="6" placeholder="请确认参数无误后再加入，此参数将作为投标使用" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品相关文件">
               <el-upload
                 action="/api/common/upload"
                 :headers="uploadHeaders"
                 accept="image/jpeg,image/png,image/gif,image/bmp,image/webp,image/tiff,application/pdf,application/zip,application/x-zip-compressed,application/x-rar-compressed,application/x-rar,application/x-7z-compressed,application/x-tar,application/octet-stream"
                 :on-success="handleCertSuccess"
                 :on-remove="handleRemoveCert"
                 :before-upload="beforeCertUpload"
                 :file-list="certFileList"
                 :limit="10"
                 style="width: 100%;"
               >
                 <el-button type="primary" link>点击上传</el-button>
                 <template #tip>
                   <div class="el-upload__tip">支持各类图片、压缩包、文档；小于100MB</div>
                 </template>
                 <template #file="{ file }">
                   <div class="file-list-item">
                     <el-icon class="file-icon">
                       <Document v-if="file.name.toLowerCase().endsWith('.pdf')" />
                       <Picture v-else-if="['.jpg','.jpeg','.png'].some(ext => file.name.toLowerCase().endsWith(ext))" />
                       <Folder v-else-if="['.zip','.rar'].some(ext => file.name.toLowerCase().endsWith(ext))" />
                       <Document v-else />
                     </el-icon>
                     <span class="file-name" :title="file.name">{{ file.name }}</span>
                     <div class="file-actions">
                       <el-button link type="primary" size="small" @click="handleDownload(file)">下载</el-button>
                       <el-button link type="danger" size="small" @click="handleRemoveCertClick(file)">删除</el-button>
                     </div>
                   </div>
                 </template>
               </el-upload>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品配套软件">
               <el-upload
                 action="/api/common/upload"
                 :headers="uploadHeaders"
                 accept="image/jpeg,image/png,image/gif,image/bmp,image/webp,image/tiff,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation,application/zip,application/x-zip-compressed,application/x-rar-compressed,application/x-7z-compressed,application/x-tar,text/plain"
                 :on-success="handleSoftSuccess"
                 :on-remove="handleRemoveSoft"
                 :before-upload="beforeSoftUpload"
                 :file-list="softFileList"
                 :limit="5"
                 style="width: 100%;"
               >
                 <el-button type="primary" link>点击上传</el-button>
                 <template #tip>
                   <div class="el-upload__tip">支持各类图片、压缩包、文档；小于300MB</div>
                 </template>
                 <template #file="{ file }">
                   <div class="file-list-item">
                     <el-icon class="file-icon">
                       <Document v-if="file.name.toLowerCase().endsWith('.pdf')" />
                       <Folder v-else-if="['.zip','.rar'].some(ext => file.name.toLowerCase().endsWith(ext))" />
                       <Document v-else />
                     </el-icon>
                     <span class="file-name" :title="file.name">{{ file.name }}</span>
                     <div class="file-actions">
                       <el-button link type="primary" size="small" @click="handleDownloadSoft(file)">下载</el-button>
                       <el-button link type="danger" size="small" @click="handleRemoveSoftClick(file)">删除</el-button>
                     </div>
                   </div>
                 </template>
               </el-upload>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="上传本产品相关案例图片">
          <el-upload
            action="/api/common/upload"
            :headers="uploadHeaders"
            list-type="picture-card"
            :on-preview="handlePictureCardPreview"
            :on-remove="handleRemoveCaseImage"
            :on-success="handleCaseImageSuccess"
            :file-list="caseImagesFileList"
          >
            <el-icon><Plus /></el-icon>
            <template #file="{ file }">
              <div>
                <img class="el-upload-list__item-thumbnail" :src="file.url" alt="" />
                <span class="el-upload-list__item-actions">
                  <span class="el-upload-list__item-preview" @click="handlePictureCardPreview(file)">
                    <el-icon><ZoomIn /></el-icon>
                  </span>
                  <span class="el-upload-list__item-delete" @click="handleDownload(file)">
                    <el-icon><Download /></el-icon>
                  </span>
                  <span class="el-upload-list__item-delete" @click="handleRemoveCaseImage(file)">
                    <el-icon><Delete /></el-icon>
                  </span>
                </span>
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="商品销售说明">
          <el-input type="textarea" v-model="form.extends.salesNotes" rows="6" placeholder="请填写此商品销售中注意事项" style="width: 100%" />
        </el-form-item>

        <el-divider content-position="left" v-if="fieldConfigList.length > 0">扩展信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12" v-for="field in fieldConfigList" :key="field.prop">
            <el-form-item :label="field.label" :prop="'extends.' + field.prop" 
              :rules="field.required ? [{ required: true, message: '必填', trigger: 'blur' }] : []">
              <el-input v-if="field.type === 'input'" v-model="form.extends[field.prop]" />
              <el-input-number v-else-if="field.type === 'number'" v-model="form.extends[field.prop]" style="width: 100%" />
              <el-date-picker v-else-if="field.type === 'date'" v-model="form.extends[field.prop]" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="form.id">
           <el-col :span="12">
             <el-form-item label="创建人">
               <el-input v-model="form.createBy" disabled />
             </el-form-item>
           </el-col>
           <el-col :span="12">
             <el-form-item label="创建时间">
               <el-input v-model="form.createTime" disabled />
             </el-form-item>
           </el-col>
        </el-row>

      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit(formRef)">确定</el-button>
        </span>
      </template>
    </el-dialog>
    <el-dialog v-model="previewVisible">
      <img w-full :src="previewImageUrl" alt="Preview Image" style="width: 100%" />
    </el-dialog>

    <!-- Field Config Dialog -->
    <el-dialog v-model="configVisible" title="扩展字段配置" width="700px">
      <el-table :data="fieldConfigList" border>
        <el-table-column label="字段名" prop="label">
          <template #default="{ row }">
            <el-input v-model="row.label" placeholder="如：颜色" />
          </template>
        </el-table-column>
        <el-table-column label="字段Key" prop="prop">
          <template #default="{ row }">
            <el-input v-model="row.prop" placeholder="如：color" />
          </template>
        </el-table-column>
        <el-table-column label="类型" prop="type" width="120">
          <template #default="{ row }">
            <el-select v-model="row.type">
              <el-option label="文本" value="input" />
              <el-option label="数字" value="number" />
              <el-option label="日期" value="date" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="必填" prop="required" width="80">
          <template #default="{ row }">
            <el-switch v-model="row.required" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ $index }">
            <el-button type="danger" link @click="removeFieldConfig($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 10px;">
        <el-button type="primary" @click="addFieldConfig">添加字段</el-button>
      </div>
      <template #footer>
        <el-button @click="configVisible = false">取消</el-button>
        <el-button type="primary" @click="saveFieldConfig">保存配置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, reactive, computed } from 'vue'
import type { FormInstance, FormRules, UploadProps, UploadUserFile, UploadFile } from 'element-plus'
import { Plus, ZoomIn, Download, Delete, Document, Picture, Folder } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWarehouseList } from '@/api/warehouse'
import { getSupplierList } from '@/api/supplier'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

// 权限检查函数
const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo || {}
  const perms: string[] = ui?.permissionCodes || []
  return Array.isArray(perms) && perms.some((code: string) => code === permCode)
}

const canCreate = computed(() => hasPermission('PDM:create'))
const canUpdate = computed(() => hasPermission('PDM:update'))
const canDelete = computed(() => hasPermission('PDM:delete'))

const tableData = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const paramVisible = ref(false)
const currentRemark = ref('')
const caseVisible = ref(false)
const currentCaseImages = ref<any[]>([])
const formRef = ref<FormInstance>()

// 上传请求头（包含认证token）
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    Authorization: token ? `Bearer ${token}` : ''
  }
})

// 统计数据
const statistics = reactive({
  totalCount: 0,
  enabledCount: 0,
  totalStock: 0,
  categoryCount: 0
})

const unitOptions = ref<string[]>([])
const categoryOptions = ref<string[]>([])
const seriesOptions = ref<string[]>([])
const certFileList = ref<UploadUserFile[]>([])
const softFileList = ref<UploadUserFile[]>([])
const caseImagesFileList = ref<UploadUserFile[]>([])
const previewImageUrl = ref('')
const previewVisible = ref(false)
const configVisible = ref(false)
const fieldConfigList = ref<any[]>([])
const warehouseOptions = ref<any[]>([])
const supplierOptions = ref<any[]>([])
const brandOptions = ref<{ label: string; value: string }[]>([])
const submitLoading = ref(false)
const originalForm = ref<any>({})
const includeTax = ref(false)
const baseCostPrice = ref(0) // 记录原始成本价（不含税）
const isTaxIncluded = ref(false) // 记录当前价格是否已含税

const queryParams = reactive({
  productName: '',
  brand: '',
  productCategory: ''
})

const form = reactive<any>({
  id: undefined,
  productCode: '',
  productName: '',
  productNameEn: '',
  productSeries: '',
  productSpec: '',
  brand: '',
  productUnit: '',
  productCategory: '',
  productImage: '',
  productSource: 'PURCHASE',
  costPrice: 0,
  channelPrice: 0,
  terminalPrice: 0,
  warehouseId: undefined,
  stockQuantity: 0,
  remark: '',
  certificates: '',
  softwareAttachment: '',
  caseImages: '',
  extendFields: '',
  extends: {} as Record<string, any>,
  createBy: '',
  createTime: '',
  status: 1
})

const rules = reactive<FormRules>({
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  productUnit: [{ required: true, message: '请选择单位', trigger: 'change' }],
  productCategory: [{ required: true, message: '请选择类别', trigger: 'change' }],
  productSeries: [{ required: true, message: '请选择商品系列', trigger: 'change' }],
  costPrice: [{ required: true, message: '请输入成本单价', trigger: 'blur' }],
  channelPrice: [{ required: true, message: '请输入渠道单价', trigger: 'blur' }],
  terminalPrice: [{ required: true, message: '请输入终端单价', trigger: 'blur' }],
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  remark: [{ required: true, message: '请输入投标参数', trigger: 'blur' }]
})

const fetchData = async () => {
  loading.value = true
  try {
    // 商品列表接口返回的是分页数据，request工具返回的是res.data，即分页对象
    console.log('查询参数:', queryParams)
    const params: any = {
        current: currentPage.value,
        size: pageSize.value,
        productName: queryParams.productName,
        brand: queryParams.brand
    }
    if (queryParams.productCategory) {
        params.productCategory = queryParams.productCategory
    }
    const productPage: any = await request({
        url: '/pdm/product/list',
        method: 'get',
        params
    })
    
    // 获取仓库列表，用于映射仓库名称
    // 注意：getWarehouseList返回的是分页数据，request工具返回的是res.data，即分页对象
    const warehousePage: any = await getWarehouseList({ current: 1, size: 1000 })
    const warehouseMap = new Map()
    const warehouses = warehousePage.records || []
    warehouses.forEach((warehouse: any) => {
      warehouseMap.set(warehouse.id, warehouse.warehouseName)
    })
    
    // 为商品数据添加仓库名称
    tableData.value = (productPage.records || []).map((product: any) => ({
      ...product,
      warehouseName: warehouseMap.get(product.warehouseId) || ''
    }))
    total.value = productPage.total
    
    // 获取统计数据（查询所有数据）
    await fetchStatistics()
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const res: any = await request({
      url: '/pdm/product/list',
      method: 'get',
      params: { current: 1, size: 10000 }
    })
    const allProducts = res?.records || []
    
    statistics.totalCount = allProducts.length
    statistics.enabledCount = allProducts.filter((p: any) => p.status === 1).length
    statistics.totalStock = allProducts.reduce((sum: number, p: any) => sum + (Number(p.stockQuantity) || 0), 0)
    statistics.categoryCount = new Set(allProducts.map((p: any) => p.productCategory).filter(Boolean)).size
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleReset = () => {
  queryParams.productName = ''
  queryParams.brand = ''
  queryParams.productCategory = ''
  handleSearch()
}

const handleViewParam = (row: any) => {
  currentRemark.value = row.remark
  paramVisible.value = true
}

const handleViewCase = (row: any) => {
  currentCaseImages.value = row.caseImages ? row.caseImages.split(',').map((url: string) => ({ url: normalizeUrl(url) })) : []
  caseVisible.value = true
}

const handleCopy = async () => {
  if (!currentRemark.value) {
    ElMessage.warning('暂无内容可复制')
    return
  }
  try {
    await navigator.clipboard.writeText(currentRemark.value)
    ElMessage.success('复制成功')
  } catch (err) {
    ElMessage.error('复制失败，请手动复制')
  }
}

const openConfig = async () => {
  const res: any = await request({ url: '/system/config/list', params: { size: 100 } })
  const config = res.records.find((item: any) => item.configKey === 'product_extend_field_config')
  if (config) {
    fieldConfigList.value = JSON.parse(config.configValue || '[]')
  } else {
    fieldConfigList.value = []
  }
  configVisible.value = true
}

const saveFieldConfig = async () => {
  await request({
    url: '/system/config',
    method: 'post',
    data: {
      configKey: 'product_extend_field_config',
      configName: '商品扩展字段配置',
      configValue: JSON.stringify(fieldConfigList.value)
    }
  })
  ElMessage.success('配置已保存')
  configVisible.value = false
  fetchConfig()
}

const addFieldConfig = () => {
  fieldConfigList.value.push({ label: '', prop: '', type: 'input', required: false })
}

const removeFieldConfig = (index: number) => {
  fieldConfigList.value.splice(index, 1)
}

const fetchConfig = async () => {
  try {
    const res: any = await request({ url: '/system/config/list', method: 'get', params: { size: 100 } })
    const general = (res.records || res || []).find((item: any) => item.configKey === 'general_settings')
    if (general && general.configValue) {
      const config = JSON.parse(general.configValue)
      const custom = config.customCategories || {}
      const keys = Object.keys(custom || {})
      const pickByName = (keyword: string) => {
        const key = keys.find(k => k.includes(keyword))
        return key ? (custom[key] || []) : []
      }
      // 商品单位来自“产品分类”
      unitOptions.value = Array.isArray(config.productCategories) && config.productCategories.length > 0
        ? config.productCategories
        : (pickByName('产品分类').length > 0 ? pickByName('产品分类') : (pickByName('商品分类').length > 0 ? pickByName('商品分类') : pickByName('分类')))
      // 商品类别来自“设备分类”
      categoryOptions.value = Array.isArray(config.deviceCategories) && config.deviceCategories.length > 0
        ? config.deviceCategories
        : (pickByName('设备分类').length > 0 ? pickByName('设备分类') : pickByName('类别'))
      seriesOptions.value = Array.isArray(config.productSeries) && config.productSeries.length > 0
        ? config.productSeries
        : (pickByName('商品系列').length > 0 ? pickByName('商品系列') : pickByName('系列'))
    }
    
    const extendConfig = (res.records || []).find((item: any) => item.configKey === 'product_extend_field_config')
    if (extendConfig && extendConfig.configValue) {
      fieldConfigList.value = JSON.parse(extendConfig.configValue)
    }
  } catch (e) {
    console.error('Fetch config failed', e)
  }
}

const handleAdd = () => {
  form.id = undefined
  form.productCode = ''
  form.productName = ''
  form.productNameEn = ''
  form.productSeries = ''
  form.productSpec = ''
  form.brand = ''
  form.productUnit = '台'
  form.productCategory = ''
  form.productImage = ''
  form.productSource = 'PURCHASE'
  form.costPrice = 0
  form.channelPrice = 0
  form.terminalPrice = 0
  form.warehouseId = undefined
  form.supplierId = undefined
  form.stockQuantity = 0
  form.remark = ''
  form.certificates = ''
  form.softwareAttachment = ''
  form.caseImages = ''
  form.extendFields = ''
  form.extends = {}
  form.createBy = ''
  form.createTime = ''
  certFileList.value = []
  softFileList.value = []
  caseImagesFileList.value = []
  includeTax.value = false
  baseCostPrice.value = 0
  isTaxIncluded.value = false
  originalForm.value = JSON.parse(JSON.stringify(form))
  dialogVisible.value = true
  fetchWarehouses()
  fetchSuppliers()
}

const getFileName = (url: string) => {
    return url.substring(url.lastIndexOf('/') + 1)
}

const handleEdit = (row: any) => {
  Object.assign(form, row)
  try {
    form.extends = row.extendFields ? JSON.parse(row.extendFields) : {}
  } catch (e) {
    form.extends = {}
  }
  certFileList.value = row.certificates ? row.certificates.split(',').map((url: string) => ({ name: getFileName(url), url })) : []
  softFileList.value = row.softwareAttachment ? row.softwareAttachment.split(',').map((url: string) => ({ name: getFileName(url), url })) : []
  caseImagesFileList.value = row.caseImages ? row.caseImages.split(',').map((url: string) => ({ name: getFileName(url), url })) : []
  includeTax.value = false
  baseCostPrice.value = Number(row.costPrice) || 0
  isTaxIncluded.value = false
  originalForm.value = JSON.parse(JSON.stringify(form))
  dialogVisible.value = true
  fetchWarehouses()
  fetchSuppliers()
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该商品吗？', '提示', { type: 'warning' }).then(async () => {
    await request({ url: `/pdm/product/${row.id}`, method: 'delete' })
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleSubmit = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  if (submitLoading.value) return

  // 保存前提醒确认价格是否含税
  try {
    await ElMessageBox.confirm(
      `你填写的成本价格${isTaxIncluded.value ? '已' : '未'}含税，是否继续保存？`,
      '价格确认',
      {
        confirmButtonText: '确认保存',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    // 用户取消保存
    return
  }

  submitLoading.value = true
  // 提交前再次同步文件列表，防止上传回调遗漏
  syncFilesToForm(certFileList.value, 'certificates')
  syncFilesToForm(softFileList.value, 'softwareAttachment')
  syncFilesToForm(caseImagesFileList.value, 'caseImages')

  // 序列化扩展字段
  form.extendFields = JSON.stringify(form.extends)

  await formEl.validate(async (valid) => {
    if (valid) {
      const payload = { ...form }
      // 后端不识别的字段，提交时移除
      delete (payload as any).extends
      delete (payload as any).warehouseName
      // 自动推断商品来源：有供应商则外购，否则自产
      if (!payload.productSource) {
        payload.productSource = payload.supplierId ? 'PURCHASE' : 'SELF'
      }
      // 预检：组合唯一性（名称+规格+单位）
      const duplicate = await checkDuplicateCombo(payload)
      if (duplicate) {
        ElMessage.closeAll()
        ElMessage.error('保存失败：同名商品（含规格/单位）已存在，请调整后再试')
        submitLoading.value = false
        return
      }
      // 若创建且未填写商品编号，自动生成
      if (!payload.productCode) {
        payload.productCode = await generateProductCodeOrFallback()
      }
      try {
        if (form.id) {
          await request({ url: `/pdm/product/${form.id}`, method: 'put', data: payload })
          ElMessage.closeAll()
          ElMessage.success('更新成功')
          ensureBrandOption(payload.brand as string)
        } else {
          await request({ url: '/pdm/product', method: 'post', data: payload })
          ElMessage.closeAll()
          ElMessage.success('创建成功')
          ensureBrandOption(payload.brand as string)
        }
        dialogVisible.value = false
        fetchData()
        submitLoading.value = false
      } catch (e: any) {
        const msg = String(e?.message || e || '')
        const isCodeConflict = msg.includes('编号') || msg.includes('商品编号已存在')
        if (!form.id && isCodeConflict) {
          // 编号冲突时自动重新生成一次并重试
          payload.productCode = await generateProductCodeOrFallback()
          // 确保重试时也删除不识别的字段
          delete (payload as any).extends
          delete (payload as any).warehouseName
          try {
            await request({ url: '/pdm/product', method: 'post', data: payload })
            ElMessage.closeAll()
            ElMessage.success('创建成功')
            dialogVisible.value = false
            fetchData()
            submitLoading.value = false
            return
          } catch (e2: any) {
            ElMessage.closeAll()
            ElMessage.error(String(e2?.message || '保存失败，请检查唯一性后重试'))
            submitLoading.value = false
          }
        } else {
          ElMessage.closeAll()
          ElMessage.error(msg || '保存失败，请检查唯一性后重试')
          submitLoading.value = false
        }
      }
    } else {
      submitLoading.value = false
    }
  })
}

const checkDuplicateCombo = async (payload: any) => {
  try {
    const res:any = await request({ url: '/pdm/product/list', method: 'get', params: { current: 1, size: 1000, productName: payload.productName } })
    const list = res.records || res || []
    const target = (list || []).find((x:any) => {
      if (payload.id && x.id === payload.id) return false
      const sameName = String(x.productName || '') === String(payload.productName || '')
      const sameSpec = String(x.productSpec || '') === String(payload.productSpec || '')
      const sameUnit = String(x.productUnit || '') === String(payload.productUnit || '')
      // 仅比较非空参与的字段：若提交了规格/单位，则匹配相同；若为空，则不严格比较该项
      const specOk = String(payload.productSpec || '') ? sameSpec : true
      const unitOk = String(payload.productUnit || '') ? sameUnit : true
      return sameName && specOk && unitOk
    })
    return !!target
  } catch {
    return false
  }
}

const normalizeUrl = (u?: string) => {
  if (!u) return ''
  if (String(u).startsWith('http')) return u as string
  const path = String(u).startsWith('/files/') ? String(u) : (`/files/${String(u).replace(/^\/+/, '')}`)
  return `${window.location.origin}${path}`
}
const handleImageSuccess: UploadProps['onSuccess'] = (res) => {
  if (res.code === 200) form.productImage = normalizeUrl(res.data)
}

const beforeImageUpload: UploadProps['beforeUpload'] = (rawFile) => {
  if (rawFile.size / 1024 / 1024 > 10) {
    ElMessage.error('图片大小不能超过 10MB!')
    return false
  }
  return true
}

const syncFilesToForm = (fileList: UploadUserFile[], field: 'certificates' | 'softwareAttachment' | 'caseImages') => {
    const urls = fileList.map(f => (f.response as any)?.data || f.url).filter(Boolean)
    form[field] = urls.join(',')
}

const handleCertSuccess: UploadProps['onSuccess'] = (res, uploadFile, uploadFiles) => {
    if (res.code === 200) {
        ElMessage.success('上传成功')
        certFileList.value = uploadFiles
        syncFilesToForm(uploadFiles, 'certificates')
    } else {
        ElMessage.error(res.message || '上传失败')
        const index = certFileList.value.indexOf(uploadFile)
        if (index !== -1) certFileList.value.splice(index, 1)
    }
}

const beforeCertUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const name = (rawFile.name || '').toLowerCase()
  const type = (rawFile.type || '').toLowerCase()
  
  // 允许的文件扩展名
  const allowedExt = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.tiff', '.pdf', '.zip', '.rar', '.7z', '.tar']
  
  // 允许的MIME类型
  const allowedMime = [
    'image/jpeg', 
    'image/png',
    'image/gif',
    'image/bmp',
    'image/webp',
    'image/tiff',
    'application/pdf',
    'application/zip',
    'application/x-zip-compressed',
    'application/x-rar-compressed',
    'application/x-rar',
    'application/x-7z-compressed',
    'application/x-tar',
    'application/octet-stream'
  ]
  
  // 检查文件扩展名或MIME类型
  const isExtensionValid = allowedExt.some(ext => name.endsWith(ext))
  const isMimeTypeValid = allowedMime.includes(type)
  
  if (!isExtensionValid && !isMimeTypeValid) {
    ElMessage.error('只能上传图片、压缩包、PDF格式的文件!')
    return false
  }
  if (rawFile.size / 1024 / 1024 > 100) {
    ElMessage.error('文件大小不能超过 100MB!')
    return false
  }
  return true
}

const handleRemoveCert: UploadProps['onRemove'] = (_uploadFile, uploadFiles) => {
    certFileList.value = uploadFiles
    syncFilesToForm(uploadFiles, 'certificates')
}
const handleRemoveCertClick = (file: any) => {
  const nextList = (certFileList.value || []).filter((x: any) => x.uid !== file.uid)
  handleRemoveCert(file, nextList as any)
}

const handleSoftSuccess: UploadProps['onSuccess'] = (res, uploadFile, uploadFiles) => {
    if (res.code === 200) {
        ElMessage.success('上传成功')
        softFileList.value = uploadFiles
        syncFilesToForm(uploadFiles, 'softwareAttachment')
    } else {
        ElMessage.error(res.message || '上传失败')
        const index = softFileList.value.indexOf(uploadFile)
        if (index !== -1) softFileList.value.splice(index, 1)
    }
}

const beforeSoftUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const name = (rawFile.name || '').toLowerCase()
  const type = (rawFile.type || '').toLowerCase()
  // 图片格式
  const imageExt = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.tiff']
  const imageMime = ['image/jpeg', 'image/png', 'image/gif', 'image/bmp', 'image/webp', 'image/tiff']
  // 压缩格式
  const zipExt = ['.zip', '.rar', '.7z', '.tar']
  const zipMime = ['application/zip', 'application/x-zip-compressed', 'application/x-rar-compressed', 'application/vnd.rar', 'application/x-7z-compressed', 'application/x-tar']
  // 文档格式
  const docExt = ['.pdf', '.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx', '.txt']
  const docMime = [
    'application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'application/vnd.ms-powerpoint', 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
    'text/plain'
  ]
  
  const allowedExt = [...imageExt, ...zipExt, ...docExt]
  const allowedMime = [...imageMime, ...zipMime, ...docMime]
  
  const ok = allowedExt.some(ext => name.endsWith(ext)) || allowedMime.includes(type)
  if (!ok) {
    ElMessage.error('仅支持图片、压缩包及常见文档格式')
    return false
  }
  if (rawFile.size / 1024 / 1024 > 300) {
    ElMessage.error('文件大小不能超过 300MB!')
    return false
  }
  return true
}

const handleRemoveSoft: UploadProps['onRemove'] = (_uploadFile, uploadFiles) => {
    softFileList.value = uploadFiles
    syncFilesToForm(uploadFiles, 'softwareAttachment')
}

const handleRemoveSoftClick = (file: any) => {
  const nextList = (softFileList.value || []).filter((x: any) => x.uid !== file.uid)
  handleRemoveSoft(file, nextList as any)
}

const handleDownloadSoft = (file: any) => {
  const url = file.response?.data || file.url
  if (url) {
    const link = document.createElement('a')
    link.href = url
    link.download = file.name || url.split('/').pop() || 'download'
    link.target = '_blank'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } else {
    ElMessage.warning('文件链接不存在')
  }
}

const handleCaseImageSuccess: UploadProps['onSuccess'] = (res, uploadFile, uploadFiles) => {
    if (res.code === 200) {
        ElMessage.success('上传成功')
        caseImagesFileList.value = uploadFiles
        syncFilesToForm(uploadFiles, 'caseImages')
    } else {
        ElMessage.error(res.message || '上传失败')
        const index = caseImagesFileList.value.indexOf(uploadFile)
        if (index !== -1) caseImagesFileList.value.splice(index, 1)
    }
}

const handleRemoveCaseImage = (uploadFile: UploadFile) => {
    const index = caseImagesFileList.value.indexOf(uploadFile)
    if (index !== -1) caseImagesFileList.value.splice(index, 1)
    syncFilesToForm(caseImagesFileList.value, 'caseImages')
}

const handlePictureCardPreview = (uploadFile: UploadFile) => {
  previewImageUrl.value = uploadFile.url!
  previewVisible.value = true
}

const handleDownload = (file: any) => {
  const url = file.response?.data || file.url
  if (url) {
    const link = document.createElement('a')
    link.href = url
    link.download = file.name || url.split('/').pop() || 'download'
    link.target = '_blank'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } else {
    ElMessage.warning('文件链接不存在')
  }
}

onMounted(() => {
  fetchData()
  fetchConfig()
  fetchWarehouses()
  fetchSuppliers()
  fetchBrands()
})

const fetchWarehouses = async () => {
  try {
    const res:any = await getWarehouseList({ current: 1, size: 1000 })
    warehouseOptions.value = res.records || res || []
  } catch (e) {}
}

const fetchSuppliers = async () => {
  try {
    const res:any = await getSupplierList({ current: 1, size: 1000 })
    const list = res.records || res || []
    // 按供应商名称拼音排序
    list.sort((a: any, b: any) => {
      const nameA = a.supplierName || ''
      const nameB = b.supplierName || ''
      return nameA.localeCompare(nameB, 'zh-CN')
    })
    supplierOptions.value = list
  } catch (e) {}
}
const fetchBrands = async () => {
  try {
    const res:any = await request({ url: '/pdm/product/list', method: 'get', params: { current: 1, size: 1000 } })
    const list = res.records || res || []
    const normalize = (s:string) => String(s || '').trim().replace(/\s+/g, ' ').toLowerCase()
    const map = new Map<string, string>()
    for (const p of list) {
      const raw = String(p.brand || '').trim()
      if (!raw) continue
      const key = normalize(raw)
      if (!map.has(key)) map.set(key, raw)
    }
    const uniq = Array.from(map.values()).filter(Boolean).sort((a, b) => a.localeCompare(b))
    brandOptions.value = uniq.map(v => ({ label: v, value: v }))
  } catch (e) {
    brandOptions.value = []
  }
}

const ensureBrandOption = (v:string) => {
  const value = String(v || '').trim()
  if (!value) return
  const exists = brandOptions.value.some(o => String(o.value) === value)
  if (!exists) {
    brandOptions.value.push({ label: value, value })
  }
}
const generateProductCode = async () => {
  try {
    const res:any = await request({ url: '/sys/code-rule/generate/PRODUCT_CODE', method: 'get' })
    form.productCode = (res?.data || res)
    if (!form.productCode) {
      ElMessage.error('生成编号失败')
    }
  } catch (e) {
    try {
      const res2:any = await request({ url: '/sys/code-rule/generate/PRODUCT_CODE', method: 'get' })
      form.productCode = (res2?.data || res2)
      if (!form.productCode) {
        ElMessage.error('生成编号失败')
      }
    } catch (_e) {
      ElMessage.error('生成编号失败')
    }
  }
}

const generateProductCodeOrFallback = async (): Promise<string> => {
  try {
    const res:any = await request({ url: '/sys/code-rule/generate/PRODUCT_CODE', method: 'get' })
    const code = (res?.data || res)
    if (code) return String(code)
  } catch {}
  // 回退：基于时间戳的临时编号（避免再次冲突），最终建议用户稍后修改为规范编号
  return 'TMP-' + Date.now()
}

const onMainSizeChange = (val: number) => {
  const oldSize = pageSize.value
  const oldPage = currentPage.value
  pageSize.value = val
  const startIndex = (oldPage - 1) * oldSize
  currentPage.value = Math.floor(startIndex / val) + 1
  fetchData()
}

const formatPrice = (field: 'costPrice' | 'channelPrice' | 'terminalPrice') => {
  const v = Number(form[field] ?? 0)
  if (isNaN(v)) {
    form[field] = 0
    return
  }
  form[field] = Number(v.toFixed(2))
}

// 成本价失去焦点时处理
const onCostPriceBlur = () => {
  formatPrice('costPrice')
}

// 成本价输入时记录原始值
const onCostPriceInput = (value: number) => {
  const costPrice = Number(value) || 0
  // 记录原始成本价（不含税）
  if (!isTaxIncluded.value) {
    baseCostPrice.value = costPrice
  }
  // 自动计算渠道和终端价格
  if (costPrice > 0) {
    form.channelPrice = Number((costPrice * 1.5).toFixed(2))
    form.terminalPrice = Number((costPrice * 1.8).toFixed(2))
  }
}

// 含税勾选框变化处理
const onTaxChange = (checked: boolean) => {
  if (checked && !isTaxIncluded.value) {
    // 只有在未含税的情况下才加税
    const basePrice = baseCostPrice.value || Number(form.costPrice) || 0
    if (basePrice > 0) {
      // 含税价格 = 成本价 * (1 + 13%)
      form.costPrice = Number((basePrice * 1.13).toFixed(2))
      // 重新计算渠道和终端价格
      form.channelPrice = Number((form.costPrice * 1.5).toFixed(2))
      form.terminalPrice = Number((form.costPrice * 1.8).toFixed(2))
      isTaxIncluded.value = true
    }
  }
}

const isFormChanged = () => {
  const a = JSON.stringify(form)
  const b = JSON.stringify(originalForm.value || {})
  return a !== b
}

const onDialogBeforeClose = async (done: () => void) => {
  if (!isFormChanged()) {
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
  if (dialogVisible.value && isFormChanged()) {
    e.preventDefault()
    e.returnValue = ''
  }
}

onMounted(() => {
  window.addEventListener('beforeunload', beforeUnloadHandler)
})
onUnmounted(() => {
  window.removeEventListener('beforeunload', beforeUnloadHandler)
})
</script>

<style scoped>
.product-list {
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
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 140px;
  height: 140px;
  background-color: #fafafa;
}
.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}
.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 140px;
  height: 140px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}
.avatar {
  width: 140px;
  height: 140px;
  display: block;
  object-fit: contain;
}
.image-placeholder,
.image-error {
  width: 140px;
  height: 140px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #999;
  font-size: 12px;
  background-color: #f5f5f5;
}
.code-input {
  width: 240px;
}
@media (max-width: 900px) {
  .code-input {
    width: 100%;
  }
}
.image-side {
  float: right;
  width: 300px;
  margin-left: 16px;
}
.image-card {
  border: 1px solid var(--el-border-color);
}
.image-box {
  display: flex;
  align-items: center;
  gap: 12px;
}
.image-label-vertical {
  writing-mode: vertical-rl;
  text-orientation: mixed;
  font-size: 12px;
  color: #606266;
  letter-spacing: 2px;
}
.image-uploader {
  display: flex;
  align-items: center;
  justify-content: center;
}
@media (max-width: 900px) {
  .image-side {
    float: none;
    width: 100%;
    margin-left: 0;
    margin-bottom: 12px;
  }
}
.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e4e7ed;
}

/* 案例图片自适应显示 - 保持原始比例 */
:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: auto !important;
  height: auto !important;
  max-width: 200px;
  max-height: 200px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin: 0 8px 8px 0;
}

:deep(.el-upload-list--picture-card .el-upload-list__item-thumbnail) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  aspect-ratio: unset !important;
}

/* 文件列表项样式 */
.file-list-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  width: 100%;
  box-sizing: border-box;
}

.file-list-item .file-icon {
  margin-right: 8px;
  flex-shrink: 0;
}

.file-list-item .file-name {
  flex: 1;
  margin-right: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
  max-width: 120px;
}

.file-list-item .file-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}
</style>
