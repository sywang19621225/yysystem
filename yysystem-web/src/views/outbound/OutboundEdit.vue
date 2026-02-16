<template>
  <div class="app-container" v-if="outbound">
    <el-card>
      <template #header>
        <div class="card-header actions-right">
          <h2 style="margin: 0; font-size: 18px;">出库单</h2>
          <div style="display: flex; gap: 8px;">
            <el-button @click="goBack" size="small">返回主表</el-button>
            <el-button type="success" @click="openApplyContract" size="small">调用合同</el-button>
            <el-button type="primary" @click="saveMaster" size="small">保存单据</el-button>
          </div>
        </div>
      </template>
      <el-form :model="outbound" label-width="120px" class="master-form" :rules="formRules" ref="masterFormRef">
        <!-- 顶部三行：合同编号/名称/业务员；合同金额/本次发货金额；客户名称/客户联系人/联系电话 -->
        <el-row :gutter="12">
          <el-col :span="6"><el-form-item label="合同编号"><el-input :model-value="contract?.contractNo || '-'" disabled /></el-form-item></el-col>
          <el-col :span="10"><el-form-item label="合同名称"><el-input :model-value="contract?.contractName || '-'" disabled /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="业务员"><el-input :model-value="contractSalesmanName || salesmanName || '-'" disabled /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="合同金额"><el-input :model-value="formatCurrency(contract?.contractAmount)" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="本次发货金额"><el-input :model-value="formatCurrency(currentAmount)" disabled /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="客户名称"><el-input :model-value="customer?.customerName || contract?.customerName || '-'" disabled /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="客户联系人"><el-input :model-value="customer?.linkman || '-'" disabled /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="联系电话"><el-input :model-value="customer?.phone || '-'" disabled /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="24"><el-form-item label="客户办公地址"><el-input :model-value="customer?.address || '-'" disabled /></el-form-item></el-col>
        </el-row>
        <!-- 取消质保到期时间显示 -->
        <el-divider />
        <div v-if="contract && contract.endCustomerName">
          <el-row :gutter="12">
            <el-col :span="8"><el-form-item label="终端客户"><el-input :model-value="contract.endCustomerName" disabled /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="收货人"><el-input :model-value="contract.receiver" disabled /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="联系方式"><el-input :model-value="contract.receiverPhone" disabled /></el-form-item></el-col>
          </el-row>
          <el-form-item label="收货地址"><el-input :model-value="contract.receiveAddress || contract.address" disabled /></el-form-item>
        </div>
        <el-divider />
        <el-row :gutter="12">
          <el-col :span="6"><el-form-item label="运输方式" prop="transportMethod">
            <el-select v-model="outbound.transportMethod" placeholder="选择运输方式">
              <el-option v-for="m in transportMethods" :key="m" :label="m" :value="m" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="6"><el-form-item label="运输金额" prop="transportAmount">
            <el-input 
              v-model="outbound.transportAmount" 
              type="number" 
              step="0.01" 
              min="0" 
              placeholder="请输入运输金额"

            >
              <template #append>元</template>
            </el-input>
          </el-form-item></el-col>
          <el-col :span="6"><el-form-item label="运输单位" prop="transportCompany">
            <el-select 
              v-model="outbound.transportCompany" 
              placeholder="选择或输入运输单位"
              filterable
              allow-create
              default-first-option
            >
              <el-option 
                v-for="company in transportCompanyOptions" 
                :key="company" 
                :label="company" 
                :value="company" 
              />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="6"><el-form-item label="联系方式" prop="transportContactPhone"><el-input v-model="outbound.transportContactPhone" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="6"><el-form-item label="预定发货时间" prop="scheduledShipTime"><el-date-picker v-model="outbound.scheduledShipTime" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm:ss" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="预计到货时间" prop="expectedArrivalTime"><el-date-picker v-model="outbound.expectedArrivalTime" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm:ss" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="实际发货时间"><el-date-picker v-model="outbound.actualShipTime" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm:ss" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="实际到货时间"><el-date-picker v-model="outbound.actualArrivalTime" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm:ss" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="出库分类" prop="outboundCategory">
            <el-select v-model="outbound.outboundCategory" placeholder="请选择出库分类" filterable>
              <el-option v-for="category in outboundCategories" :key="category" :label="category" :value="category" />
            </el-select>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="到货验收备注"><el-input v-model="outbound.arrivalRemark" type="textarea" /></el-form-item>
        <el-form-item label="签收图片">
          <el-upload action="/api/common/upload" :headers="uploadHeaders" multiple :on-success="onArrivalImageUploadSuccess" :file-list="arrivalFileList" list-type="picture-card">
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:16px;">
      <template #header>
        <div class="card-header">
          <span>出库明细（逐台设备）</span>
          <el-button type="primary" size="small" @click="openAddDetail">新增明细</el-button>
        </div>
      </template>
      <el-table :data="paginatedDetails" border stripe v-loading="loading" style="width: 100%" :scrollbar-always-on="true" table-layout="fixed">
        <el-table-column prop="productName" label="商品名称" min-width="160" />
        <el-table-column prop="productCode" label="商品编号" width="140" />
        <el-table-column prop="productSpec" label="型号" min-width="120" />
        <el-table-column prop="outQuantity" label="数量" width="100" />
        <el-table-column prop="salesPrice" label="单价" width="120">
          <template #default="scope">{{ formatCurrency(scope.row.salesPrice) }}</template>
        </el-table-column>
        <el-table-column prop="productQrCode" label="二维码" min-width="180" />
        <el-table-column prop="productImage" label="缩略图" width="160">
          <template #default="scope">
            <div v-if="scope.row.productImage" style="width:60px;height:60px;display:flex;align-items:center;justify-content:center;">
              <img :src="normalizeImage(scope.row.productImage)" alt="" style="width:60px;height:60px;object-fit:cover;" @error="onThumbError" />
            </div>
            <div v-else style="width:60px;height:60px;display:flex;align-items:center;justify-content:center;background-color:#f2f3f5;color:#909399;font-size:12px;">
              无图片
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="parameters" label="参数" min-width="220">
          <template #default="scope"><el-input v-model="scope.row.parameters" type="textarea" /></template>
        </el-table-column>
        <el-table-column prop="deviceIp" label="设备IP" width="200">
          <template #default="scope">
            <el-tooltip :content="scope.row.deviceIp || '未设置'" placement="top">
              <el-input 
                v-model="scope.row.deviceIp" 
                placeholder="示例 192.168.1.10"
                :formatter="(val: string) => maskIp(val)"
              />
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="remoteMethod" label="远程方式" width="180">
          <template #default="scope">
            <el-select v-model="scope.row.remoteMethod" placeholder="选择">
              <el-option v-for="m in remoteMethods" :key="m" :label="m" :value="m" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="operatingSystem" label="操作系统" width="180">
          <template #default="scope">
            <el-select v-model="scope.row.operatingSystem" placeholder="选择">
              <el-option v-for="os in operatingSystems" :key="os" :label="os" :value="os" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="remoteAccount" label="远程账号" width="180">
          <template #default="scope"><el-input v-model="scope.row.remoteAccount" /></template>
        </el-table-column>
        <el-table-column prop="remotePassword" label="远程密码" width="180">
          <template #default="scope"><el-input v-model="scope.row.remotePassword" /></template>
        </el-table-column>
        <el-table-column prop="systemAccount" label="系统账号" width="180">
          <template #default="scope"><el-input v-model="scope.row.systemAccount" /></template>
        </el-table-column>
        <el-table-column prop="systemPassword" label="系统密码" width="180">
          <template #default="scope"><el-input v-model="scope.row.systemPassword" /></template>
        </el-table-column>
        <el-table-column prop="onsiteLocation" label="现场放置位置" min-width="200">
          <template #default="scope"><el-input v-model="scope.row.onsiteLocation" /></template>
        </el-table-column>
        <el-table-column prop="schedulePowerOnTime" label="定时开机时间" width="180">
          <template #default="scope"><el-input v-model="scope.row.schedulePowerOnTime" /></template>
        </el-table-column>
        <el-table-column prop="schedulePowerOffTime" label="定时关机时间" width="180">
          <template #default="scope"><el-input v-model="scope.row.schedulePowerOffTime" /></template>
        </el-table-column>
        <el-table-column prop="programBackup" label="定制程序备份" min-width="160">
          <template #default="scope"><el-input v-model="scope.row.programBackup" /></template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <div style="display:flex; align-items:center; gap:10px; flex-wrap:nowrap; white-space:nowrap;">
              <el-button type="primary" link @click="openEditDetail(scope.row)">修改</el-button>
              <el-button type="danger" link @click="handleDeleteDetail(scope.row)">删除</el-button>
              <el-button type="success" link @click="downloadQr(scope.row)">二维码</el-button>
              <el-button type="primary" link @click="previewParameters(scope.row)">参数</el-button>
              <el-button type="warning" link @click="printDetail(scope.row)">打印</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <!-- 明细分页 -->
      <div class="pagination-container" style="margin-top: 16px;">
        <el-pagination
          v-model:current-page="detailPagination.current"
          v-model:page-size="detailPagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="detailPagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleDetailSizeChange"
          @current-change="handleDetailCurrentChange"
        />
      </div>
    </el-card>
  </div>
  <el-dialog v-model="detailDialogVisible" title="新增出库明细" width="600px">
    <el-form :model="detailForm" label-width="120px">
      <el-form-item label="商品编号"><el-input v-model="detailForm.productCode" /></el-form-item>
      <el-form-item label="商品名称"><el-input v-model="detailForm.productName" /></el-form-item>
      <el-form-item label="商品规格"><el-input v-model="detailForm.productSpec" /></el-form-item>
      <el-form-item label="数量"><el-input v-model="detailForm.count" type="number" /></el-form-item>
      <el-form-item label="远程方式"><el-input v-model="detailForm.remoteMethod" /></el-form-item>
      <el-form-item label="现场放置位置"><el-input v-model="detailForm.onsiteLocation" /></el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="detailDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitAddDetail">确定</el-button>
      </span>
    </template>
  </el-dialog>
  <el-dialog v-model="applyDialogVisible" title="调用合同生成明细" width="900px">
    <el-form label-width="120px">
      <template v-if="hasFixedCustomerAndContract">
        <el-form-item label="客户">
          <el-input :model-value="fixedCustomerName || '-'" disabled />
        </el-form-item>
        <el-form-item label="合同">
          <el-input :model-value="fixedContractLabel || '-'" disabled />
        </el-form-item>
      </template>
      <template v-else>
        <el-form-item label="选择客户">
          <el-select v-model="selectedCustomerId" placeholder="选择客户" @change="loadContractsByCustomer" filterable style="width:100%;">
            <el-option v-for="c in customerList" :key="c.id" :label="c.customerName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择合同">
          <el-select v-model="selectedContractId" placeholder="选择合同" filterable style="width:100%;">
            <el-option
              v-for="ct in contractList"
              :key="ct.id"
              :label="contractOptionLabel(ct)"
              :value="ct.id"
            />
          </el-select>
        </el-form-item>
      </template>
    <!-- 提示信息 -->
    <el-alert type="info" :closable="false" style="margin: 8px 0;">
      <template #title>
        <div>提示：每次最多生成500个二维码</div>
        <div style="margin-top: 4px; color: #909399; font-size: 12px;">注：当你确认的客户未出库，但是不显示的话，检查订单是否已经锁单</div>
      </template>
    </el-alert>
    <el-table ref="contractTable" :data="paginatedContractDetails" border style="width:100%; margin-top: 8px" v-if="paginatedContractDetails.length" @selection-change="onSelectionChange" table-layout="auto" height="400">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="productName" label="名称" min-width="160" />
      <el-table-column prop="productSpec" label="型号" width="140" />
      <el-table-column prop="salesQuantity" label="合同数量" width="100" />
      <el-table-column label="导入数量" width="160">
        <template #default="scope">
          <el-input 
            v-model.number="scope.row.importQty" 
            type="number"
            :min="0" 
            :max="getInputMax(scope.row)"
            style="width: 100%"
          />
        </template>
      </el-table-column>
      <el-table-column prop="productCategory" label="分类" width="140" />
    </el-table>
    <!-- 合同明细分页 -->
    <div class="pagination-container" style="margin-top: 16px; display: flex; justify-content: flex-end;">
      <el-pagination
        v-model:current-page="contractDetailPagination.current"
        v-model:page-size="contractDetailPagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="contractDetailPagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleContractDetailSizeChange"
        @current-change="handleContractDetailCurrentChange"
      />
    </div>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="applyDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitApplyContract">导入选中明细</el-button>
      </span>
    </template>
  </el-dialog>
  <el-dialog v-model="editDetailDialogVisible" :title="editDetailTitle" width="1000px">
    <el-form :model="editDetailForm" label-width="120px">
      <el-form-item label="设备IP"><el-input v-model="editDetailForm.deviceIp" placeholder="示例 192.168.1.10" /></el-form-item>
      <el-row :gutter="12">
        <el-col :span="8"><el-form-item label="远程操作">
          <el-select v-model="editDetailForm.remoteMethod" placeholder="请选择设备预装远程工具">
            <el-option v-for="m in remoteMethods" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item></el-col>
        <el-col :span="8"><el-form-item label="远程账号"><el-input v-model="editDetailForm.remoteAccount" placeholder="请填写设备原装远程工具的账号" /></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="远程密码"><el-input v-model="editDetailForm.remotePassword" placeholder="请选择设备预装远程工具的密码" /></el-form-item></el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="8"><el-form-item label="操作系统">
          <el-select v-model="editDetailForm.operatingSystem" placeholder="请选择设备预装的操作系统">
            <el-option v-for="os in operatingSystems" :key="os" :label="os" :value="os" />
          </el-select>
        </el-form-item></el-col>
        <el-col :span="8"><el-form-item label="系统账号"><el-input v-model="editDetailForm.systemAccount" placeholder="请填写系统登录账号" /></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="系统密码"><el-input v-model="editDetailForm.systemPassword" placeholder="请填写系统登录密码" /></el-form-item></el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="8"><el-form-item label="开机时间"><el-time-picker v-model="editDetailForm.schedulePowerOnTime" format="HH:mm" placeholder="请填写BIOS设置的开机时间" /></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="关机时间"><el-time-picker v-model="editDetailForm.schedulePowerOffTime" format="HH:mm" placeholder="请填写系统设置的关机时间" /></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="放置位置"><el-input v-model="editDetailForm.onsiteLocation" placeholder="请至现场后再填写设备摆放的位置地点" /></el-form-item></el-col>
      </el-row>
    <el-form-item label="程序备份">
      <el-upload action="/api/common/upload" :headers="uploadHeaders" :on-success="onProgramUploadSuccess" :on-error="onProgramUploadError" accept=".zip,.rar,.7z" :before-upload="beforeProgramUpload" :show-file-list="false">
        <el-button type="primary">上传压缩文件</el-button>
        <div style="margin-top:6px; color:#909399; font-size:12px;">单个文件最大300MB</div>
      </el-upload>
      <div v-if="editDetailForm.programBackup" style="margin-top:6px; display:flex; align-items:center; gap:8px;">
        <a :href="editDetailForm.programBackup" target="_blank" style="word-break: break-all; white-space: normal; line-height: 1.4;">{{ fileNameFromUrl(editDetailForm.programBackup) }}</a>
        <el-button size="small" type="danger" @click="editDetailForm.programBackup=''">删除</el-button>
      </div>
    </el-form-item>
      <el-form-item label="设备图片">
        <el-upload 
          action="/api/common/upload"
          :headers="uploadHeaders"
          multiple 
          list-type="picture" 
          :file-list="editDetailImageList"
          :on-success="onDetailImageUploadSuccess"
          :on-remove="onDetailImageRemove"
        >
          <el-button>上传图片</el-button>
        </el-upload>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="editDetailDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitEditDetail">保存</el-button>
      </span>
    </template>
  </el-dialog>
<el-dialog v-model="paramPreviewDialogVisible" title="参数预览" width="600px">
  <pre style="white-space:pre-wrap;">{{ previewParametersText }}</pre>
  <template #footer>
    <span class="dialog-footer">
      <el-button @click="copyParameters">复制</el-button>
      <el-button type="success" @click="savePreviewParameters">保存到出库单</el-button>
      <el-button type="primary" @click="paramPreviewDialogVisible=false">关闭</el-button>
    </span>
  </template>
</el-dialog>

<!-- 打印弹窗 -->
<el-dialog v-model="printDialogVisible" title="打印出库明细" width="800px">
  <div id="print-content" style="padding: 20px;">
    <div style="text-align: center; margin-bottom: 20px;">
      <h2 style="margin: 0;">出库单明细</h2>
      <p style="color: #666; margin: 5px 0;">合同编号：{{ contract?.contractNo || '-' }} | 合同名称：{{ contract?.contractName || '-' }}</p>
    </div>
    <div v-if="printRow" style="border: 1px solid #ddd; padding: 20px; border-radius: 8px;">
      <div style="display: flex; gap: 20px; margin-bottom: 20px;">
        <div style="flex: 1;">
          <p><strong>商品名称：</strong>{{ printRow.productName }}</p>
          <p><strong>商品编号：</strong>{{ printRow.productCode }}</p>
          <p><strong>商品规格：</strong>{{ printRow.productSpec }}</p>
          <p><strong>数量：</strong>{{ printRow.outQuantity }}</p>
          <p><strong>单价：</strong>{{ formatCurrency(printRow.salesPrice) }}</p>
        </div>
        <div style="text-align: center;">
          <img v-if="printRow.productQrCode" :src="`https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${encodeURIComponent(printRow.productQrCode)}`" alt="二维码" style="width: 150px; height: 150px;" />
          <p style="margin-top: 8px; font-size: 12px; color: #666;">{{ printRow.productQrCode }}</p>
        </div>
      </div>
      <el-divider />
      <div style="font-size: 14px; color: #666;">
        <p><strong>设备信息</strong></p>
        <p>设备IP：{{ printRow.deviceIp || '-' }}</p>
        <p>远程方式：{{ printRow.remoteMethod || '-' }}</p>
        <p>远程账号：{{ printRow.remoteAccount || '-' }}</p>
        <p>操作系统：{{ printRow.operatingSystem || '-' }}</p>
        <p>系统账号：{{ printRow.systemAccount || '-' }}</p>
        <p>放置位置：{{ printRow.onsiteLocation || '-' }}</p>
        <p>参数：{{ printRow.parameters || '-' }}</p>
      </div>
    </div>
  </div>
  <template #footer>
    <span class="dialog-footer">
      <el-button @click="printDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handlePrint">打印</el-button>
    </span>
  </template>
</el-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOutboundById, updateOutbound, getOutboundDetails, updateOutboundDetail, addOutboundDetail, deleteOutboundDetail, createOutbound, importContractDetails } from '@/api/outbound'
import { getProductList } from '@/api/product'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { Plus } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
let id = Number(route.query.id || route.params.id)
const loading = ref(false)
const outbound = ref<any>(null)
const details = ref<any[]>([])

// 明细分页
const detailPagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 当前页显示的明细数据
const paginatedDetails = computed(() => {
  const start = (detailPagination.current - 1) * detailPagination.size
  const end = start + detailPagination.size
  return details.value.slice(start, end)
})

// 上传请求头（包含认证token）
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    Authorization: token ? `Bearer ${token}` : ''
  }
})

// 获取当前出库单中已导入的合同明细ID和数量
const getImportedContractDetailInfo = () => {
  const importedMap: Record<number, number> = {}
  if (Array.isArray(details.value)) {
    details.value.forEach((row: any) => {
      if (row.contractDetailId != null) {
        const k = Number(row.contractDetailId)
        importedMap[k] = (importedMap[k] || 0) + Number(row.outQuantity || 0)
      }
    })
  }
  return importedMap
}

// 分页后的合同明细（排除已完全导入的）
const paginatedContractDetails = computed(() => {
  // 获取当前已导入的合同明细信息
  const importedMap = getImportedContractDetailInfo()
  
  // 过滤：只显示还有剩余可导入数量的明细
  const filtered = contractDetailsInDialog.value.filter((d: any) => {
    const contractDetailId = Number(d.id)
    const contractQty = Number(d.salesQuantity || 0)
    const shippedQty = Number(d.shippedQuantity || 0)
    const importedQty = importedMap[contractDetailId] || 0
    
    // 实际剩余 = 合同数量 - 已发货数量 - 当前单已导入数量
    const actualRemain = Math.max(0, contractQty - shippedQty - importedQty)
    
    // 更新显示的剩余数量
    d.baseRemainQuantity = actualRemain
    // 默认导入数量为全部剩余数量，用户可以修改
    d.importQty = actualRemain
    d.maxRemainQuantity = actualRemain
    
    return actualRemain > 0
  })
  
  // 更新分页总数
  contractDetailPagination.total = filtered.length
  
  // 返回当前页的数据
  const start = (contractDetailPagination.current - 1) * contractDetailPagination.size
  const end = start + contractDetailPagination.size
  return filtered.slice(start, end)
})

const remoteMethods = ref<string[]>([])
const operatingSystems = ref<string[]>([])
const transportMethods = ref<string[]>([])
const outboundCategories = ref<string[]>([])
const detailDialogVisible = ref(false)
const detailForm = ref<any>({ productCode: '', productName: '', productSpec: '', count: 1, remoteMethod: '', onsiteLocation: '' })
const applyDialogVisible = ref(false)
const contractTable = ref<any>(null)

// 合同明细导入对话框分页
const contractDetailPagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

const selectedCustomerId = ref<number | null>(null)
const selectedContractId = ref<number | null>(null)
const customerList = ref<any[]>([])
const contractList = ref<any[]>([])
const contract = ref<any>(null)
const contractDetailsInDialog = ref<any[]>([])
const selectedDetails = ref<any[]>([])
const productMapByCode = ref<Record<string, any>>({})
const hasFixedCustomerAndContract = computed(() => Boolean(outbound.value?.customerId) && Boolean(outbound.value?.contractId))
const fixedCustomerName = computed(() => customer.value?.customerName || contract.value?.customerName || '')
const fixedContractLabel = computed(() => {
  const ct = contract.value || {}
  return ct?.contractName ? `${ct.contractNo || '-'}（${ct.contractName}）` : (ct?.contractNo || '-')
})
const contractSalesmanName = ref<string>('')
const salesmanName = ref<string>('')
const currentAmount = ref<number>(0)
const arrivalFileList = ref<any[]>([])
const customer = ref<any>(null)
const editDetailDialogVisible = ref(false)
const editDetailForm = ref<any>({})
const editDetailTitle = computed(() => {
  const name = editDetailForm.value?.productName || ''
  const code = editDetailForm.value?.productCode || ''
  const displayName = name || code || '未命名设备'
  return `设备出厂预装信息 - ${displayName}`
})
const editDetailImageList = ref<any[]>([])
const paramPreviewDialogVisible = ref(false)
const previewParametersText = ref<string>('')
const previewDetailId = ref<number | null>(null)
const isDirty = ref(false)
const initialSnapshot = ref<string>('') // 初始状态快照，用于判断是否真的修改

// 打印弹窗
const printDialogVisible = ref(false)
const printRow = ref<any>(null)

// 运输单位选项
const transportCompanyOptions = ref<string[]>([
  '顺丰速运',
  '圆通速递',
  '中通快递',
  '申通快递',
  '韵达快递',
  '百世快递',
  '德邦物流',
  '京东物流',
  '菜鸟网络',
  '邮政EMS',
  '跨越速运',
  '安能物流',
  '天地华宇',
  '佳吉快运',
  '中铁快运',
  '民航快递',
  'DHL',
  'FedEx',
  'UPS',
  'TNT'
])

// 验证到货时间不能早于发货时间
const validateArrivalTime = (_rule: any, value: any, callback: any) => {
  if (!value) {
    callback(new Error('请选择预计到货时间'))
    return
  }
  
  const shipTime = outbound.value.scheduledShipTime || outbound.value.actualShipTime
  if (shipTime && value < shipTime) {
    callback(new Error('到货时间不能早于发货时间'))
    return
  }
  
  callback()
}

// 验证实际到货时间不能早于实际发货时间
const validateActualArrivalTime = (_rule: any, value: any, callback: any) => {
  if (!value) {
    callback() // 实际时间为可选，不验证
    return
  }
  
  const actualShipTime = outbound.value.actualShipTime
  if (actualShipTime && value < actualShipTime) {
    callback(new Error('实际到货时间不能早于实际发货时间'))
    return
  }
  
  callback()
}

// 表单验证规则
const formRules = {
  transportMethod: [{ required: true, message: '请选择运输方式', trigger: 'change' }],
  transportAmount: [
    { required: true, message: '请输入运输金额', trigger: 'blur' },
    { pattern: /^\d+(\.\d{1,2})?$/, message: '请输入正确的金额格式', trigger: 'blur' }
  ],
  transportCompany: [{ required: true, message: '请选择或输入运输单位', trigger: 'change' }],
  transportContactPhone: [{ required: true, message: '请输入联系方式', trigger: 'blur' }],
  scheduledShipTime: [{ required: true, message: '请选择预计发货时间', trigger: 'change' }],
  expectedArrivalTime: [
    { required: true, message: '请选择预计到货时间', trigger: 'change' },
    { validator: validateArrivalTime, trigger: 'change' }
  ],
  outboundCategory: [{ required: true, message: '请选择出库分类', trigger: 'change' }],
  actualArrivalTime: [
    { validator: validateActualArrivalTime, trigger: 'change' }
  ]
}
const masterFormRef = ref()

const fileNameFromUrl = (u:string) => {
  if (!u) return ''
  const s = String(u)
  const idx = Math.max(s.lastIndexOf('/'), s.lastIndexOf('\\'))
  return idx >= 0 ? s.slice(idx + 1) : s
}

const loadRemoteMethods = async () => {
  try {
    const res:any = await request({ url: '/system/config/list', method: 'get', params: { size: 100, current: 1 } })
    const list = res.records || res || []
    const general = list.find((item:any) => item.configKey === 'general_settings')
    let cfg:any = {}
    try { cfg = general?.configValue ? JSON.parse(general.configValue) : {} } catch { cfg = {} }
    const custom = cfg.customCategories || {}
    const keys = Object.keys(custom || {})
    const pickByName = (keyword:string) => {
      const k = keys.find(name => String(name||'').includes(keyword))
      return k ? (Array.isArray(custom[k]) ? custom[k] : []) : []
    }
    let tList:string[] = Array.isArray(cfg.transportMethods) ? cfg.transportMethods : pickByName('运输方式')
    let osList:string[] = Array.isArray(cfg.operatingSystems) ? cfg.operatingSystems : pickByName('操作系统')
    let rmList:string[] = Array.isArray(cfg.remoteMethods) ? cfg.remoteMethods : pickByName('远程方式')
    let ocList:string[] = Array.isArray(cfg.outboundCategories) ? cfg.outboundCategories : pickByName('出库分类')
    if (!tList.length) tList = ['快递','物流','自提']
    if (!osList.length) osList = ['Windows','Linux','Android']
    if (!rmList.length) rmList = ['TeamViewer','AnyDesk','RDP','VNC']
    if (!ocList.length) ocList = ['销售出库','采购退货','其他出库']
    transportMethods.value = tList
    operatingSystems.value = osList
    remoteMethods.value = rmList
    outboundCategories.value = ocList
    
    // 加载运输单位选项
    if (cfg.transportCompanies && Array.isArray(cfg.transportCompanies)) {
      transportCompanyOptions.value = cfg.transportCompanies
    }
  } catch (e) {
    remoteMethods.value = []
    transportMethods.value = []
    operatingSystems.value = []
  }
}

const loadData = async () => {
  loading.value = true
  try {
    if (!Number.isFinite(id)) {
      // 新建状态，设置合理的初始值，避免表单绑定导致的脏数据问题
      outbound.value = {
        transportMethod: '',
        transportAmount: '',
        transportCompany: '',
        transportContactPhone: '',
        scheduledShipTime: '',
        expectedArrivalTime: '',
        actualShipTime: '',
        actualArrivalTime: '',
        arrivalRemark: '',
        customerAddress: ''
      }
      details.value = []
      detailPagination.total = 0
      detailPagination.current = 1
      contract.value = null
      customer.value = null
      contractSalesmanName.value = ''
      return
    }
    const master:any = await getOutboundById(id)
    outbound.value = master?.data || master
    const det:any = await getOutboundDetails(id)
    details.value = Array.isArray(det) ? det : (det?.records || det?.data || [])
    // 更新分页总数
    detailPagination.total = details.value.length
    detailPagination.current = 1
    currentAmount.value = (details.value || []).reduce((sum:number, d:any) => sum + Number(d.salesPrice || 0) * Number(d.outQuantity || 0), 0)
    if (outbound.value?.arrivalImages) {
      const urls = String(outbound.value.arrivalImages).split(',').map((u:string) => u.trim()).filter(Boolean)
      arrivalFileList.value = urls.map((u:string) => ({ name: u.substring(u.lastIndexOf('/')+1), url: u }))
    } else {
      arrivalFileList.value = []
    }
    if (outbound.value?.contractId) {
      const ct:any = await request({ url: `/crm/contract/${outbound.value.contractId}`, method: 'get' })
      contract.value = ct?.data || ct
      if (contract.value?.salesmanId) {
        try {
          const user:any = await request({ url: `/system/user/${contract.value.salesmanId}`, method: 'get' })
          const u = user?.data || user
          contractSalesmanName.value = u?.name || u?.realName || u?.nickname || u?.username || ''
        } catch {}
      }
    }
    if (outbound.value?.customerId) {
      try {
        const c:any = await request({ url: `/crm/customer/${outbound.value.customerId}`, method: 'get' })
        customer.value = c?.data || c
      } catch { customer.value = null }
    }
    if (!contractSalesmanName.value && outbound.value?.salesmanId) {
      try {
        const user2:any = await request({ url: `/system/user/${outbound.value.salesmanId}`, method: 'get' })
        const u2 = user2?.data || user2
        salesmanName.value = u2?.name || u2?.realName || u2?.nickname || u2?.username || ''
      } catch {}
    }
  } finally {
    loading.value = false
    isDirty.value = false
    // 创建初始状态快照
    createSnapshot()
  }
}

// 创建当前状态快照
const createSnapshot = () => {
  const state = {
    outbound: JSON.parse(JSON.stringify(outbound.value || {})),
    details: JSON.parse(JSON.stringify(details.value || []))
  }
  initialSnapshot.value = JSON.stringify(state)
}

// 检查是否真的修改了内容
const checkIfReallyDirty = () => {
  if (!initialSnapshot.value) return false
  const currentState = {
    outbound: JSON.parse(JSON.stringify(outbound.value || {})),
    details: JSON.parse(JSON.stringify(details.value || []))
  }
  const currentSnapshot = JSON.stringify(currentState)
  return currentSnapshot !== initialSnapshot.value
}
const saveMaster = async () => {
  // 表单验证
  if (!masterFormRef.value) {
    ElMessage.error('表单验证失败')
    return
  }

  try {
    await masterFormRef.value.validate()
  } catch (error) {
    ElMessage.error('请填写所有必填项')
    return
  }

  if (!Number.isFinite(id)) {
    // 确保 customerAddress 被正确设置
    if (!outbound.value.customerAddress && customer.value?.address) {
      outbound.value.customerAddress = customer.value.address
    }
    const res:any = await createOutbound(outbound.value)
    const newId = res?.id || res?.data || res
    id = Number(newId)
    
    // 保存明细数据（如果有）
    if (details.value && details.value.length > 0) {
      try {
        // 按 contractDetailId 合并数量
        const itemMap: Record<number, number> = {}
        details.value.forEach((d: any) => {
          if (d.contractDetailId) {
            itemMap[d.contractDetailId] = (itemMap[d.contractDetailId] || 0) + (d.outQuantity || 1)
          }
        })
        
        const items = Object.entries(itemMap).map(([contractDetailId, quantity]) => ({
          contractDetailId: Number(contractDetailId),
          quantity
        }))
        
        if (items.length > 0) {
          await importContractDetails(id, outbound.value.contractId, items)
        }
      } catch (e) {
        ElMessage.error('保存明细失败，请稍后重试')
      }
    }
    
    ElMessage.success('已保存并返回列表')
    isDirty.value = false
    createSnapshot() // 保存成功后更新快照
    router.push('/scm/outbound/sales-outbound')
  } else {
    await updateOutbound(id, outbound.value)
    ElMessage.success('已保存并返回列表')
    isDirty.value = false
    createSnapshot() // 保存成功后更新快照
    router.push('/scm/outbound/sales-outbound')
  }
}
const openAddDetail = () => {
  if (!Number.isFinite(id)) {
    ElMessage.warning('请先保存出库单再新增明细')
    return
  }
  detailDialogVisible.value = true
}
const submitAddDetail = async () => {
  const payload = {
    outStockId: id,
    productCode: detailForm.value.productCode,
    productName: detailForm.value.productName,
    productSpec: detailForm.value.productSpec,
    remoteMethod: detailForm.value.remoteMethod,
    onsiteLocation: detailForm.value.onsiteLocation
  }
  const count = Number(detailForm.value.count || 1)
  await addOutboundDetail(payload, count)
  detailDialogVisible.value = false
  await loadData()
  ElMessage.success('新增成功')
}

onMounted(async () => {
  await loadRemoteMethods()
  await loadData()
})
watch(outbound, () => {
  if (!loading.value) {
    // 使用智能检测判断是否真的有修改
    isDirty.value = checkIfReallyDirty()
  }
}, { deep: true })
watch(details, () => {
  if (!loading.value) {
    // 使用智能检测判断是否真的有修改
    isDirty.value = checkIfReallyDirty()
  }
}, { deep: true })
const openApplyContract = async () => {
  // 允许新建状态直接调用合同，无需先保存出库单
  // 只有在编辑状态下才强制刷新数据，新建状态下保留已导入的明细
  if (Number.isFinite(id)) {
    await loadData()
  }
  
  // 如果没有选择过客户/合同，则使用出库单上的客户/合同
  if (!selectedCustomerId.value) {
    selectedCustomerId.value = outbound.value?.customerId || null
  }
  if (!selectedContractId.value) {
    selectedContractId.value = outbound.value?.contractId || null
  }
  
  // 清空之前的选择，但保留客户和合同选择
  selectedDetails.value = []
  
  applyDialogVisible.value = true
  if (!selectedCustomerId.value || !selectedContractId.value) {
    await loadCustomers()
  }
  
  // 如果已经选择了合同，重新触发加载逻辑
  if (selectedContractId.value) {
    // 先重置为null再设置，触发watch
    const tempId = selectedContractId.value
    selectedContractId.value = null
    // 使用nextTick确保watch被触发
    setTimeout(() => {
      selectedContractId.value = tempId
    }, 0)
    
    try {
      const ct:any = await request({ url: `/crm/contract/${tempId}`, method: 'get' })
      contract.value = ct?.data || ct || {}
    } catch {}
  }
}
const loadCustomers = async () => {
  try {
    const res:any = await request({ url: '/crm/customer/list', method: 'get', params: { size: 1000, current: 1 } })
    const list = res.records || res || []
    // 按客户名称拼音排序
    customerList.value = list.sort((a: any, b: any) => {
      const nameA = a.customerName || ''
      const nameB = b.customerName || ''
      return nameA.localeCompare(nameB, 'zh-CN')
    })
  } catch { customerList.value = [] }
}
const loadContractsByCustomer = async () => {
  contractList.value = []
  selectedContractId.value = null
  if (!selectedCustomerId.value) return
  try {
    const res:any = await request({ url: `/crm/contract/by-customer/${selectedCustomerId.value}`, method: 'get' })
    const list = res?.data || res || []
    contractList.value = list.filter((ct: any) => ct.auditStatus === 'PASSED' && ct.outboundStatus !== 'COMPLETED')
    if (!selectedContractId.value && contractList.value.length) {
      selectedContractId.value = contractList.value[0].id
    }
  } catch { contractList.value = [] }
}
watch(selectedContractId, async (val) => {
  contractDetailsInDialog.value = []
  if (!val) return
  try {
    const ct:any = await request({ url: `/crm/contract/${val}`, method: 'get' })
    const data = ct?.data || ct || {}
    // 从合同中获取客户ID并设置
    if (data?.customerId && !selectedCustomerId.value) {
      selectedCustomerId.value = data.customerId
    }
    const details = data?.detailList || []
    contractDetailsInDialog.value = (details || []).map((d:any) => {
      const qty = Number(d.salesQuantity || 0)
      return { 
        ...d, 
        salesQuantity: qty, 
        baseRemainQuantity: qty, 
        importQty: qty,
        contractQuantity: qty // 初始化contractQuantity，确保getInputMax可用
      }
    })
    try {
      const res:any = await getProductList({ current: 1, size: 1000 })
      const list = res?.records || res || []
      productMapByCode.value = {}
      list.forEach((p:any) => { if (p?.productCode) productMapByCode.value[String(p.productCode)] = p })
      contractDetailsInDialog.value = (contractDetailsInDialog.value || []).map((d:any) => {
        const code = String(d.productCode || '')
        const prod = productMapByCode.value[code]
        const cat = d.productCategory || prod?.productCategory || ''
        // 只使用合同明细中的图片，如果没有则显示暂无图片
        const img = d.productImage || ''
        return { ...d, productCategory: cat, productImage: img }
      })
    } catch {}
    try {
      const remain:any = await request({ url: `/scm/outbound/contract-remaining/${val}`, method: 'get' })
      const arr = remain?.data || remain || []
      const map: Record<number, any> = {}
      ;(arr || []).forEach((x:any) => { map[Number(x.contractDetailId)] = x })
      const existingMap: Record<number, number> = {}
      ;(details.value || []).forEach((row:any) => {
        if (row.contractDetailId != null) {
          const k = Number(row.contractDetailId)
          existingMap[k] = (existingMap[k] || 0) + Number(row.outQuantity || 0)
        }
      })
      contractDetailsInDialog.value = (contractDetailsInDialog.value || []).map((d:any) => {
        const m = map[Number(d.id)] || {}
        const contractQuantity = Number(m.salesQuantity ?? d.salesQuantity ?? 0)
        // 注意：这里我们不再减去shippedQuantity（后端记录的已发货数量），因为这可能包含当前单据之前保存的数量
        // 我们只关注当前合同总数减去所有出库单中已存在的数量（包括当前单据中的数量）
        // 实际上后端返回的contract-remaining接口数据中的shippedQuantity是该合同明细在所有已审核出库单中的发货总数
        // 为了准确计算，我们应该用：合同数量 - (所有已保存出库单的数量 + 当前未保存的变更)
        // 但简单起见，且符合用户要求"剩余数量=合同数量-已经导入数量"，我们这里理解为：
        // 剩余可导入 = 合同数量 - (数据库中已记录的该合同明细占用数量 + 当前页面details列表中已存在的数量)
        // 如果shippedQuantity包含了当前单据已保存的数量，那么逻辑会比较复杂。
        // 假设shippedQuantity是所有已出库数量（含当前单据已审核部分），而details是当前单据的明细。
        // 用户的需求是简单的：剩余 = 合同 - 已导入。这里的"已导入"在当前上下文是指"当前出库单中已有的" + "其他出库单已有的"。
        
        const shippedQuantity = Number(m.shippedQuantity ?? 0)
        const existingInCurrent = existingMap[Number(d.id)] || 0
        
        // 修正逻辑：剩余数量 = 合同数量 - 已发货数量 - 当前单已导入数量
        // 注意：如果当前单据是编辑模式，且部分明细已保存，那么这些明细的数量可能已经计入shippedQuantity（取决于后端逻辑）。
        // 通常后端shippedQuantity只统计"已审核"或"已完成"的出库单。如果当前单据未审核，则不计入。
        // 假设当前单据未审核，则shippedQuantity不包含当前单据数量。
        
        const initialRemain = Math.max(0, contractQuantity - shippedQuantity - existingInCurrent)
        
        return { 
          ...d, 
          salesQuantity: contractQuantity,
          shippedQuantity: shippedQuantity,
          baseRemainQuantity: initialRemain,
          importQty: initialRemain, // 默认导入数量为剩余可导入数量
          contractQuantity: contractQuantity,
          maxRemainQuantity: initialRemain // 新增字段存储最大剩余（作为计算基准）
        }
      })
    } catch {}
  } catch { contractDetailsInDialog.value = [] }
})
// 用于防止 selection-change 重复触发的标志
let isProcessingSelection = false

const onSelectionChange = (rows:any[]) => {
  // 防止重复触发
  if (isProcessingSelection) {
    return
  }
  
  // 检查是否超过导入限制
  const MAX_TOTAL_IMPORT = 500
  let totalCount = 0
  
  // 计算当前已导入的数量
  const existingCount = details.value.length
  
  // 计算即将导入的数量
  for (const row of rows) {
    const category = row.productCategory || ''
    const needSplit = category === '自产商品' || category === '外购商品'
                    || category === '自产软件' || category === '外购软件'

    if (needSplit) {
      // 需要拆分的类别：按数量生成多条记录
      // 使用用户设置的导入数量，如果没有设置则使用剩余数量
      const importQty = Number(row.importQty) || Number(row.baseRemainQuantity) || 0
      totalCount += importQty
    } else {
      // 不需要拆分的类别：只生成一条记录
      totalCount += 1
    }
  }
  
  // 检查是否超过限制
  if (existingCount + totalCount > MAX_TOTAL_IMPORT) {
    ElMessage.warning(`本次导入后将超过 ${MAX_TOTAL_IMPORT} 条限制（当前已有 ${existingCount} 条，即将导入 ${totalCount} 条），请减少选择`)
    
    // 设置标志防止重复触发
    isProcessingSelection = true
    
    // 取消勾选最后一条
    if (rows.length > 0 && contractTable.value) {
      const lastRow = rows[rows.length - 1]
      contractTable.value.toggleRowSelection(lastRow, false)
    }
    
    // 延迟重置标志
    setTimeout(() => {
      isProcessingSelection = false
    }, 100)
    
    return
  }
  
  selectedDetails.value = rows || []
}

const getInputMax = (row: any) => {
  // 最大可导入数量 = 合同数量 - 已发货数量 - 当前单已存在数量
  const contractQuantity = Number(row.contractQuantity ?? row.salesQuantity ?? 0)
  const shippedQuantity = Number(row.shippedQuantity || 0)
  
  const existingMap:Record<number, number> = {}
  if (Array.isArray(details.value)) {
    details.value.forEach((r:any) => {
      if (r && r.contractDetailId != null) {
        const k = Number(r.contractDetailId)
        existingMap[k] = (existingMap[k] || 0) + Number(r.outQuantity || 0)
      }
    })
  }
  const existingInCurrent = existingMap[Number(row.id)] || 0
  
  return Math.max(0, contractQuantity - shippedQuantity - existingInCurrent)
}
const formatCurrency = (v:any) => {
  const n = Number(v || 0)
  return `¥${n.toFixed(2)}`
}
const contractOptionLabel = (ct:any) => {
  const amount = formatCurrency(ct.contractAmount)
  const contractNo = ct.contractNo || '-'
  const contractName = ct.contractName || '-'
  return `${contractNo} | ${contractName} | ${amount}`
}

const submitApplyContract = async () => {
  if (!selectedContractId.value) return
  
  // 从勾选的行中筛选出有导入数量的
  const selectedRows = selectedDetails.value || []
  if (selectedRows.length === 0) {
    ElMessage.warning('请勾选需要导入的商品');
    return
  }
  
  const items = selectedRows.filter((d:any) => {
    const qty = Number(d.importQty) || 0
    return qty > 0
  }).map((d:any) => ({ contractDetailId: d.id, quantity: Number(d.importQty) }))
  
  if (!items.length) { 
    ElMessage.warning('请设置导入数量（导入数量必须大于0）'); 
    return 
  }
  
  // 如果是新建状态（没有ID），直接填充合同信息到表单，不调用后端API
  if (!Number.isFinite(id)) {
    // 设置合同信息
    outbound.value.contractId = selectedContractId.value
    
    // 加载合同详细信息
    let contractCustomerId = null
    try {
      const ct:any = await request({ url: `/crm/contract/${selectedContractId.value}`, method: 'get' })
      contract.value = ct?.data || ct
      // 从合同中获取客户ID
      contractCustomerId = contract.value?.customerId || selectedCustomerId.value
      if (contract.value?.salesmanId) {
        try {
          const user:any = await request({ url: `/system/user/${contract.value.salesmanId}`, method: 'get' })
          const u = user?.data || user
          contractSalesmanName.value = u?.name || u?.realName || u?.nickname || u?.username || ''
        } catch {}
      }
    } catch {}
    
    // 设置客户ID
    if (contractCustomerId) {
      outbound.value.customerId = contractCustomerId
    } else if (selectedCustomerId.value) {
      outbound.value.customerId = selectedCustomerId.value
    }
    
    // 加载客户信息并设置地址
    const customerIdToLoad = contractCustomerId || selectedCustomerId.value
    if (customerIdToLoad) {
      try {
        const c:any = await request({ url: `/crm/customer/${customerIdToLoad}`, method: 'get' })
        customer.value = c?.data || c
        // 设置客户地址到出库单
        if (customer.value?.address) {
          outbound.value.customerAddress = customer.value.address
        }
      } catch { customer.value = null }
    }
    
    // 将选中的合同明细添加到出库单明细中
    // 规则：只有外购商品、外购软件、自产商品、自产软件需要按数量生成单条记录和二维码
    // 其他类别直接导入，不拆分，不生成二维码
    let totalCount = 0

    const newDetails: any[] = []

    for (const item of items) {
      const contractDetail = contractDetailsInDialog.value.find((d: any) => d.id === item.contractDetailId)
      const category = contractDetail?.productCategory || ''

      // 判断是否需要拆分（指定类别：自产商品、外购商品、自产软件、外购软件）
      const needSplit = category === '自产商品' || category === '外购商品'
                      || category === '自产软件' || category === '外购软件'

      if (needSplit) {
        // 需要拆分的类别：按数量生成多条记录
        let quantity = item.quantity || 1
        totalCount += quantity
        
        // 生成quantity条记录，每条都有二维码
        for (let index = 0; index < quantity; index++) {
          const productCode = contractDetail?.productCode || 'PRODUCT'
          const dateStr = new Date().toISOString().slice(0, 10).replace(/-/g, '')
          const seqNum = String(index + 1).padStart(2, '0')
          const qrCode = productCode + dateStr + seqNum
          
          newDetails.push({
            productCode: contractDetail?.productCode || '',
            productName: contractDetail?.productName || '',
            productSpec: contractDetail?.productSpec || '',
            productCategory: contractDetail?.productCategory || '',
            productImage: contractDetail?.productImage || '',
            outQuantity: 1,
            contractDetailId: item.contractDetailId,
            outStockId: null,
            salesPrice: contractDetail?.salesPrice || 0,
            amount: contractDetail?.salesPrice || 0,
            productUnit: contractDetail?.productUnit || '',
            productQrCode: qrCode,
            parameters: contractDetail?.parameters || '',
            serialNumbers: '',
            remark: '',
            deviceIp: '',
            remoteMethod: '',
            remoteAccount: '',
            remotePassword: '',
            operatingSystem: '',
            systemAccount: '',
            systemPassword: '',
            schedulePowerOnTime: '',
            schedulePowerOffTime: '',
            onsiteLocation: '',
            programBackup: '',
            onsitePhotos: ''
          })
        }
      } else {
        // 不需要拆分的类别：直接导入一条记录，数量为导入数量，不生成二维码
        totalCount += 1
        
        newDetails.push({
          productCode: contractDetail?.productCode || '',
          productName: contractDetail?.productName || '',
          productSpec: contractDetail?.productSpec || '',
          productCategory: contractDetail?.productCategory || '',
          productImage: contractDetail?.productImage || '',
          outQuantity: item.quantity || 1, // 使用导入数量
          contractDetailId: item.contractDetailId,
          outStockId: null,
          salesPrice: contractDetail?.salesPrice || 0,
          amount: (contractDetail?.salesPrice || 0) * (item.quantity || 1), // 总价
          productUnit: contractDetail?.productUnit || '',
          productQrCode: '', // 不生成二维码
          parameters: contractDetail?.parameters || '',
          serialNumbers: '',
          remark: '',
          deviceIp: '',
          remoteMethod: '',
          remoteAccount: '',
          remotePassword: '',
          operatingSystem: '',
          systemAccount: '',
          systemPassword: '',
          schedulePowerOnTime: '',
          schedulePowerOffTime: '',
          onsiteLocation: '',
          programBackup: '',
          onsitePhotos: ''
        })
      }
    }
    
    // 添加到明细列表
    details.value = [...details.value, ...newDetails]
    // 更新分页总数
    detailPagination.total = details.value.length
    
    applyDialogVisible.value = false
    ElMessage.success('已导入选中明细')
    // 使用智能检测更新脏数据状态
    isDirty.value = checkIfReallyDirty()
  } else {
    // 编辑状态，调用后端API
    try {
      await importContractDetails(id, selectedContractId.value, items)
      outbound.value.contractId = selectedContractId.value
      applyDialogVisible.value = false
      await loadData()
      ElMessage.success('已导入选中明细')
    } catch (e) {}
  }
}
const goBack = () => {
  if (isDirty.value) {
    ElMessageBox.confirm('存在未保存的修改，是否取消保存并返回列表？', '提示', { type: 'warning', confirmButtonText: '是', cancelButtonText: '否' })
      .then(() => { router.push('/scm/outbound/sales-outbound') })
      .catch(() => {})
  } else {
    router.push('/scm/outbound/sales-outbound')
  }
}
const onArrivalImageUploadSuccess = (res:any) => {
  if (res?.code === 200) {
    const url = res.data
    arrivalFileList.value.push({ name: url.substring(url.lastIndexOf('/')+1), url })
    outbound.value.arrivalImages = (outbound.value.arrivalImages ? outbound.value.arrivalImages + ',' : '') + url
  }
}
const openEditDetail = (row:any) => {
  editDetailForm.value = { ...row }
  // 初始化设备图片列表
  const photos = row.onsitePhotos || ''
  editDetailImageList.value = photos.split(',').filter((url: string) => url.trim()).map((url: string, index: number) => ({
    name: url.substring(url.lastIndexOf('/') + 1) || `image_${index}`,
    url: url.trim()
  }))
  editDetailDialogVisible.value = true
}
const submitEditDetail = async () => {
  const ip = String(editDetailForm.value.deviceIp || '').trim()
  const ipv4 = '(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)'
  // 只验证IPv4格式，不验证端口
  const ipRegex = new RegExp(`^(${ipv4}\\.${ipv4}\\.${ipv4}\\.${ipv4})$`)
  if (!ipRegex.test(ip)) {
    ElMessage.error('设备IP格式错误，应为 IPv4，如 192.168.1.10')
    return
  }
  
  // 新建状态下，直接更新本地数据
  if (!Number.isFinite(id)) {
    const index = details.value.findIndex((d: any) => d.productCode === editDetailForm.value.productCode)
    if (index !== -1) {
      details.value[index] = { ...editDetailForm.value }
    }
    // 更新分页总数
    detailPagination.total = details.value.length
    editDetailDialogVisible.value = false
    ElMessage.success('保存成功')
    return
  }
  
  // 编辑状态下，调用后端API
  await updateOutboundDetail(editDetailForm.value.id, editDetailForm.value)
  editDetailDialogVisible.value = false
  await loadData()
  ElMessage.success('保存成功')
}
const onProgramUploadSuccess = (res:any) => {
  if (res?.code === 200) {
    editDetailForm.value.programBackup = res.data
    ElMessage.success('上传成功')
  }
}
const beforeProgramUpload = (file:any) => {
  const max = 300 * 1024 * 1024
  if (file && file.size > max) {
    ElMessage.error('文件大小超过300MB')
    return false
  }
  return true
}
const onProgramUploadError = (_err:any) => {
  ElMessage.error('上传失败，请检查登录状态或稍后重试')
}
const onDetailImageUploadSuccess = (res:any) => {
  if (res?.code === 200) {
    const url = res.data
    editDetailForm.value.onsitePhotos = (editDetailForm.value.onsitePhotos ? editDetailForm.value.onsitePhotos + ',' : '') + url
    // 添加到图片列表
    editDetailImageList.value.push({
      name: url.substring(url.lastIndexOf('/') + 1),
      url: url
    })
  }
}
const onDetailImageRemove = (file: any) => {
  // 从 onsitePhotos 中移除对应的图片URL
  const url = file.url || (file.response?.data)
  if (url && editDetailForm.value.onsitePhotos) {
    const photos = editDetailForm.value.onsitePhotos.split(',').filter((u: string) => u.trim() !== url)
    editDetailForm.value.onsitePhotos = photos.join(',')
  }
}
const downloadQr = async (row:any) => {
  // 检查二维码是否存在
  if (!row.productQrCode) {
    ElMessage.warning('该记录没有二维码')
    return
  }
  const data = encodeURIComponent(String(row.productQrCode))
  const url = `https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=${data}`
  // 使用二维码内容作为文件名
  const base = String(row.productQrCode).replace(/[\\/:*?"<>|]+/g, '_')
  const name = base + '.png'
  try {
    const resp = await fetch(url)
    if (!resp.ok) {
      throw new Error('生成二维码失败')
    }
    const blob = await resp.blob()
    const objectUrl = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = objectUrl
    a.download = name
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(objectUrl)
    ElMessage.success('二维码下载成功')
  } catch (e) {
    ElMessage.error('二维码下载失败，请重试')
  }
}
const previewParameters = (row:any) => {
  const local = String(row.parameters || '').trim()
  
  // 新建状态下，直接显示本地参数，不需要ID
  if (!Number.isFinite(id)) {
    previewParametersText.value = local
    paramPreviewDialogVisible.value = true
    return
  }
  
  // 编辑状态下，需要ID来保存参数
  previewDetailId.value = row.id
  if (local) {
    previewParametersText.value = local
    paramPreviewDialogVisible.value = true
    return
  }
  const code = String(row.productCode || '').trim()
  if (!code) {
    previewParametersText.value = ''
    paramPreviewDialogVisible.value = true
    return
  }
  request({ url: `/scm/outbound/${id}/param-preview`, method: 'get', params: { productCode: code } })
    .then((res:any) => {
      const val = (typeof res === 'string') ? res : (res?.data ?? '')
      previewParametersText.value = String(val || '').trim()
      paramPreviewDialogVisible.value = true
    })
    .catch(() => {
      previewParametersText.value = ''
      paramPreviewDialogVisible.value = true
    })
}
const savePreviewParameters = async () => {
  // 新建状态下，直接更新本地数据
  if (!Number.isFinite(id)) {
    // 找到当前编辑的行并更新参数
    const row = details.value.find((d: any) => d.productCode === editDetailForm.value.productCode)
    if (row) {
      row.parameters = previewParametersText.value
    }
    paramPreviewDialogVisible.value = false
    ElMessage.success('已保存参数')
    return
  }

  // 编辑状态下，需要调用后端API
  if (!previewDetailId.value) {
    paramPreviewDialogVisible.value = false
    return
  }
  await updateOutboundDetail(previewDetailId.value, { parameters: previewParametersText.value })
  paramPreviewDialogVisible.value = false
  await loadData()
  ElMessage.success('已保存参数')
}

// 打开打印弹窗
const printDetail = (row: any) => {
  printRow.value = { ...row }
  printDialogVisible.value = true
}

// 执行打印
const handlePrint = () => {
  const printContent = document.getElementById('print-content')
  if (!printContent) {
    ElMessage.error('打印内容不存在')
    return
  }

  const printWindow = window.open('', '_blank')
  if (!printWindow) {
    ElMessage.error('请允许弹出窗口以进行打印')
    return
  }

  printWindow.document.write(`
    <!DOCTYPE html>
    <html>
    <head>
      <title>出库单明细</title>
      <style>
        body { font-family: Arial, sans-serif; padding: 20px; }
        .header { text-align: center; margin-bottom: 20px; }
        .header h2 { margin: 0; }
        .header p { color: #666; margin: 5px 0; }
        .content { border: 1px solid #ddd; padding: 20px; border-radius: 8px; }
        .info-row { display: flex; gap: 20px; margin-bottom: 20px; }
        .info-col { flex: 1; }
        .info-col p { margin: 8px 0; }
        .qr-code { text-align: center; }
        .qr-code img { width: 150px; height: 150px; }
        .qr-code p { margin-top: 8px; font-size: 12px; color: #666; }
        .divider { border-top: 1px solid #ddd; margin: 20px 0; }
        .device-info { font-size: 14px; color: #666; }
        .device-info p { margin: 6px 0; }
        @media print {
          body { padding: 0; }
          .no-print { display: none; }
        }
      </style>
    </head>
    <body>
      ${printContent.innerHTML}
    </body>
    </html>
  `)

  printWindow.document.close()
  printWindow.focus()

  // 等待图片加载完成后打印
  setTimeout(() => {
    printWindow.print()
    printWindow.close()
  }, 500)

  printDialogVisible.value = false
  ElMessage.success('打印已发起')
}
const copyParameters = () => {
  navigator.clipboard.writeText(previewParametersText.value || '')
  ElMessage.success('已复制')
}
const handleDeleteDetail = async (row:any) => {
  // 新建状态下，直接从本地数据中删除
  if (!Number.isFinite(id)) {
    const index = details.value.findIndex((d: any) => d.productCode === row.productCode)
    if (index !== -1) {
      details.value.splice(index, 1)
    }
    // 更新分页总数
    detailPagination.total = details.value.length
    ElMessage.success('已删除')
    return
  }
  
  // 编辑状态下，调用后端API
  try {
    await deleteOutboundDetail(row.id)
    await loadData()
    ElMessage.success('已删除')
  } catch (error: any) {
    if (error?.message?.includes('Network Error')) {
      ElMessage.error('网络错误，请检查后端服务是否正常运行')
    } else {
      ElMessage.error(error?.message || '删除失败')
    }
  }
}
const normalizeImage = (u:string) => {
  if (!u) return ''
  const first = String(u).split(',')[0].trim()
  if (!first) return ''
  
  // 处理完整的http(s) URL，提取路径部分
  if (first.startsWith('http://') || first.startsWith('https://')) {
    try {
      const url = new URL(first)
      const pathname = url.pathname
      // 如果是 /files/ 开头，转换为 /api/files/
      if (pathname.startsWith('/files/')) {
        return '/api' + pathname
      }
      // 其他路径直接返回
      return pathname
    } catch {
      // URL解析失败，返回原值
      return first
    }
  }
  
  // 关键修正：必须加上 /api 前缀才能正确代理到后端
  if (first.startsWith('/api/')) return first
  if (first.startsWith('api/')) return '/' + first

  // 如果已经是 /files/ 开头，加上 /api 前缀
  if (first.startsWith('/files/')) return '/api' + first
  if (first.startsWith('files/')) return '/api/' + first
  
  const cleaned = first.replace(/^\/+/, '')
  return `/api/files/${cleaned}`
}
const onThumbError = (e:any) => {
  try {
    const svg = encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="60" height="60"><rect width="100%" height="100%" fill="#f2f3f5"/><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" font-size="10" fill="#909399">无图片</text></svg>')
    e.target.src = `data:image/svg+xml;charset=UTF-8,${svg}`
  } catch {}
}

// IP地址掩码处理函数
const maskIp = (val: string): string => {
  if (!val) return ''
  // 匹配 IPv4 格式
  const match = val.match(/^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/)
  if (!match) return val
  // 返回掩码格式
  return '***.***.***.***'
}

// 明细分页大小改变
const handleDetailSizeChange = (size: number) => {
  detailPagination.size = size
  detailPagination.current = 1
}

// 明细分页当前页改变
const handleDetailCurrentChange = (current: number) => {
  detailPagination.current = current
}

// 合同明细导入对话框分页大小改变
const handleContractDetailSizeChange = (size: number) => {
  contractDetailPagination.size = size
  contractDetailPagination.current = 1
}

// 合同明细导入对话框分页当前页改变
const handleContractDetailCurrentChange = (current: number) => {
  contractDetailPagination.current = current
}
</script>

<style scoped>
.app-container { padding: 20px; }
.card-header { display:flex; justify-content:space-between; align-items:center; }
.actions-right { justify-content: flex-end; gap: 8px; }
.master-form { margin-top: 8px; }
</style>
