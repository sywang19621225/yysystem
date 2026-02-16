<template>
  <div class="print-page">
    <div class="print-header no-print">
      <h2>标签打印</h2>
      <div class="print-actions">
        <el-button @click="goBack">返回</el-button>
        <el-button type="primary" @click="handlePrint">打印</el-button>
      </div>
    </div>

    <div class="print-info no-print">
      <el-alert type="info" :closable="false">
        <template #title>
          <div>共 {{ labels.length }} 个标签，每页A4纸打印8个标签</div>
          <div style="margin-top: 4px; color: #909399;">请使用A4不干胶标签纸打印，建议打印前进行打印预览</div>
        </template>
      </el-alert>
    </div>

    <!-- 打印设置 -->
    <div class="margin-settings no-print">
      <el-card shadow="never">
        <template #header>
          <div class="setting-header">
            <span>打印设置（单位：mm）</span>
            <div>
              <el-select v-model="selectedTemplate" placeholder="选择保存的参数" size="small" style="width: 180px; margin-right: 10px;">
                <el-option
                  v-for="item in savedTemplates"
                  :key="item.name"
                  :label="item.name"
                  :value="item.name"
                />
              </el-select>
              <el-button type="primary" size="small" @click="loadTemplate">加载</el-button>
              <el-button size="small" @click="showSaveDialog = true">保存参数</el-button>
            </div>
          </div>
        </template>

        <!-- 标签大小设置 -->
        <div class="setting-section">
          <div class="section-title">标签大小</div>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="标签宽度">
                <el-input-number v-model="labelWidth" :min="20" :max="100" :step="1" size="small" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="标签高度">
                <el-input-number v-model="labelHeight" :min="20" :max="100" :step="1" size="small" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 排列设置 -->
        <div class="setting-section">
          <div class="section-title">排列设置</div>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="横排数量">
                <el-input-number v-model="colsPerPage" :min="1" :max="4" :step="1" size="small" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="竖排数量">
                <el-input-number v-model="rowsPerPage" :min="1" :max="10" :step="1" size="small" />
              </el-form-item>
            </el-col>
            <el-col :span="12" style="padding-top: 28px;">
              <el-tag type="info">每页 {{ labelsPerPage }} 个标签</el-tag>
            </el-col>
          </el-row>
        </div>

        <!-- 边距设置 -->
        <div class="setting-section">
          <div class="section-title">边距设置</div>
          <el-row :gutter="20">
            <el-col :span="4">
              <el-form-item label="上边距">
                <el-input-number v-model="marginTop" :min="0" :max="30" :step="0.5" size="small" />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="下边距">
                <el-input-number v-model="marginBottom" :min="0" :max="30" :step="0.5" size="small" />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="左边距">
                <el-input-number v-model="marginLeft" :min="0" :max="30" :step="0.5" size="small" />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="右边距">
                <el-input-number v-model="marginRight" :min="0" :max="30" :step="0.5" size="small" />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="上下间距">
                <el-input-number v-model="itemGapVertical" :min="0" :max="20" :step="0.5" size="small" />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="左右间距">
                <el-input-number v-model="itemGapHorizontal" :min="0" :max="20" :step="0.5" size="small" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="setting-actions">
          <el-button type="primary" size="small" @click="applySettings">应用设置</el-button>
          <el-button size="small" @click="resetSettings">重置默认</el-button>
        </div>
      </el-card>
    </div>

    <!-- 保存参数对话框 -->
    <el-dialog v-model="showSaveDialog" title="保存参数" width="400px">
      <el-form>
        <el-form-item label="参数名称">
          <el-input v-model="newTemplateName" placeholder="请输入参数名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSaveDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTemplate">保存</el-button>
      </template>
    </el-dialog>

    <div id="print-content">
      <!-- 按每8个标签分组，每组一个独立的 label-container -->
      <div v-for="(group, groupIndex) in labelGroups" :key="groupIndex" class="label-container" :style="containerStyle">
        <div v-for="(item, index) in group" :key="index" class="label-item" :style="itemStyle">
          <div class="label-content">
            <div class="label-info">
              <p class="product-name">{{ item.productName }}</p>
              <p><span class="label-text">规格：</span>{{ item.productSpec }}</p>
              <p><span class="label-text">出厂日期：</span>{{ item.manufactureDate || '-' }}</p>
              <p><span class="label-text">制造商：</span>{{ item.manufacturer || '-' }}</p>
              <p><span class="label-text">联系电话：</span>{{ item.contactPhone || '-' }}</p>
              <p><span class="label-text">网址：</span>{{ item.website || '-' }}</p>
            </div>
            <div class="label-qr">
              <img :src="`https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(item.productQrCode)}`" alt="二维码" />
              <div class="label-qr-code">编号：{{ item.productQrCode }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const labels = ref<any[]>([])

// 标签大小设置
const labelWidth = ref(96)
const labelHeight = ref(68)

// 排列设置
const colsPerPage = ref(2)
const rowsPerPage = ref(4)
const labelsPerPage = computed(() => colsPerPage.value * rowsPerPage.value)

// 边距设置
const marginTop = ref(3.4)
const marginBottom = ref(0)
const marginLeft = ref(6)
const marginRight = ref(6)
const itemGapVertical = ref(3.4)
const itemGapHorizontal = ref(6)

// 计算样式
const containerStyle = computed(() => {
  return {
    padding: `${marginTop.value}mm ${marginRight.value}mm ${marginBottom.value}mm ${marginLeft.value}mm`
  }
})

const itemStyle = computed(() => {
  return {
    width: `${labelWidth.value}mm`,
    height: `${labelHeight.value}mm`,
    marginBottom: `${itemGapVertical.value}mm`,
    marginRight: `${itemGapHorizontal.value}mm`
  }
})

// 将标签按每页分组
const labelGroups = computed(() => {
  const groups: any[][] = []
  const pageSize = labelsPerPage.value
  for (let i = 0; i < labels.value.length; i += pageSize) {
    groups.push(labels.value.slice(i, i + pageSize))
  }
  return groups
})

// 参数模板管理
const savedTemplates = ref<any[]>([])
const selectedTemplate = ref('')
const showSaveDialog = ref(false)
const newTemplateName = ref('')

// 从localStorage加载保存的模板
const loadSavedTemplates = () => {
  const stored = localStorage.getItem('labelPrintTemplates')
  if (stored) {
    try {
      savedTemplates.value = JSON.parse(stored)
    } catch (e) {
      console.error('加载模板失败:', e)
    }
  }
}

// 保存模板
const saveTemplate = () => {
  if (!newTemplateName.value.trim()) {
    ElMessage.warning('请输入参数名称')
    return
  }

  const template = {
    name: newTemplateName.value.trim(),
    labelWidth: labelWidth.value,
    labelHeight: labelHeight.value,
    colsPerPage: colsPerPage.value,
    rowsPerPage: rowsPerPage.value,
    marginTop: marginTop.value,
    marginBottom: marginBottom.value,
    marginLeft: marginLeft.value,
    marginRight: marginRight.value,
    itemGapVertical: itemGapVertical.value,
    itemGapHorizontal: itemGapHorizontal.value
  }

  // 检查是否已存在同名模板
  const existingIndex = savedTemplates.value.findIndex(t => t.name === template.name)
  if (existingIndex >= 0) {
    savedTemplates.value[existingIndex] = template
  } else {
    savedTemplates.value.push(template)
  }

  // 保存到localStorage
  localStorage.setItem('labelPrintTemplates', JSON.stringify(savedTemplates.value))
  showSaveDialog.value = false
  newTemplateName.value = ''
  ElMessage.success('参数已保存')
}

// 加载模板
const loadTemplate = () => {
  if (!selectedTemplate.value) {
    ElMessage.warning('请选择要加载的参数')
    return
  }

  const template = savedTemplates.value.find(t => t.name === selectedTemplate.value)
  if (template) {
    labelWidth.value = template.labelWidth
    labelHeight.value = template.labelHeight
    colsPerPage.value = template.colsPerPage
    rowsPerPage.value = template.rowsPerPage
    marginTop.value = template.marginTop
    marginBottom.value = template.marginBottom
    marginLeft.value = template.marginLeft
    marginRight.value = template.marginRight
    itemGapVertical.value = template.itemGapVertical
    itemGapHorizontal.value = template.itemGapHorizontal
    ElMessage.success('参数已加载')
  }
}

// 应用设置
const applySettings = () => {
  ElMessage.success('设置已应用')
}

// 重置设置
const resetSettings = () => {
  labelWidth.value = 96
  labelHeight.value = 68
  colsPerPage.value = 2
  rowsPerPage.value = 4
  marginTop.value = 3.4
  marginBottom.value = 0
  marginLeft.value = 6
  marginRight.value = 6
  itemGapVertical.value = 3.4
  itemGapHorizontal.value = 6
  ElMessage.success('设置已重置为默认值')
}

onMounted(() => {
  // 加载保存的模板
  loadSavedTemplates()

  // 从路由参数或本地存储获取标签数据
  const data = route.query.data
  if (data) {
    try {
      labels.value = JSON.parse(decodeURIComponent(data as string))
    } catch (e) {
      ElMessage.error('标签数据解析失败')
    }
  } else {
    // 尝试从 sessionStorage 获取
    const stored = sessionStorage.getItem('printLabels')
    if (stored) {
      try {
        labels.value = JSON.parse(stored)
      } catch (e) {
        ElMessage.error('标签数据解析失败')
      }
    }
  }

  if (!labels.value.length) {
    ElMessage.warning('没有可打印的标签')
  }
})

const goBack = () => {
  // 如果是新打开的窗口，直接关闭；否则返回列表页
  if (window.history.length <= 1) {
    window.close()
  } else {
    router.push('/scm/outbound/sales-outbound')
  }
}

const handlePrint = () => {
  // 创建打印专用的iframe，只打印标签内容
  const printIframe = document.createElement('iframe')
  printIframe.style.position = 'absolute'
  printIframe.style.top = '-9999px'
  printIframe.style.left = '-9999px'
  printIframe.style.width = '210mm'
  printIframe.style.height = '297mm'
  document.body.appendChild(printIframe)

  const printContent = document.getElementById('print-content')
  if (!printContent || !printIframe.contentWindow) {
    ElMessage.error('打印内容不存在')
    return
  }

  // 获取当前设置值
  const currentLabelWidth = labelWidth.value
  const currentLabelHeight = labelHeight.value
  const currentColsPerPage = colsPerPage.value
  const currentMarginTop = marginTop.value
  const currentMarginBottom = marginBottom.value
  const currentMarginLeft = marginLeft.value
  const currentMarginRight = marginRight.value
  const currentItemGapVertical = itemGapVertical.value
  const currentItemGapHorizontal = itemGapHorizontal.value

  const iframeDoc = printIframe.contentWindow.document
  iframeDoc.open()
  iframeDoc.write(`
    <!DOCTYPE html>
    <html>
    <head>
      <title>标签打印</title>
      <style>
        * {
          margin: 0;
          padding: 0;
          box-sizing: border-box;
        }
        body {
          font-family: Arial, sans-serif;
          background: white;
        }
        .label-container {
          width: 210mm;
          min-height: 297mm;
          padding: ${currentMarginTop}mm ${currentMarginRight}mm ${currentMarginBottom}mm ${currentMarginLeft}mm;
          background: white;
          box-sizing: border-box;
          page-break-after: always;
          display: flex;
          flex-wrap: wrap;
          align-content: flex-start;
          margin-bottom: 0;
        }
        /* 最后一个容器不需要分页 */
        .label-container:last-child {
          page-break-after: auto;
        }
        .label-item {
          border: 1px solid #000;
          padding: 2mm;
          display: flex;
          flex-direction: column;
          justify-content: center;
          box-sizing: border-box;
          width: ${currentLabelWidth}mm;
          height: ${currentLabelHeight}mm;
          background: white;
          page-break-inside: avoid;
          margin-right: ${currentItemGapHorizontal}mm;
          margin-bottom: ${currentItemGapVertical}mm;
        }
        /* 每行最后一个去除右边距 */
        .label-item:nth-child(${currentColsPerPage}n) {
          margin-right: 0;
        }
        .label-content {
          display: flex;
          gap: 3mm;
          align-items: center;
          height: 100%;
        }
        .label-info {
          flex: 1;
          font-size: 13px;
          line-height: 1.5;
        }
        .label-info p {
          margin: 3px 0;
        }
        .label-info .product-name {
          font-size: 20px;
          font-weight: bold;
          margin-bottom: 8px;
          color: #000;
          line-height: 1.3;
        }
        .label-text {
          font-weight: 500;
          color: #333;
        }
        .label-qr {
          text-align: center;
          width: 40mm;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
        }
        .label-qr img {
          width: 35mm;
          height: 35mm;
        }
        .label-qr-code {
          font-size: 12px;
          color: #000;
          margin-top: 4px;
          word-break: break-all;
          font-weight: 600;
        }
        @page {
          size: A4;
          margin: 0;
        }
        @media print {
          body {
            margin: 0;
            padding: 0;
            width: 210mm;
            height: 297mm;
          }
          .label-container {
            width: 210mm;
            min-height: 297mm;
            padding: ${currentMarginTop}mm ${currentMarginRight}mm ${currentMarginBottom}mm ${currentMarginLeft}mm;
            margin: 0;
            page-break-after: always;
            display: flex !important;
            flex-wrap: wrap !important;
            align-content: flex-start !important;
            box-sizing: border-box;
          }
          /* 最后一个容器不需要分页 */
          .label-container:last-child {
            page-break-after: auto;
          }
          .label-item {
            border: 1px solid #000 !important;
            width: ${currentLabelWidth}mm !important;
            height: ${currentLabelHeight}mm !important;
            margin-right: ${currentItemGapHorizontal}mm !important;
            margin-bottom: ${currentItemGapVertical}mm !important;
            box-sizing: border-box !important;
            page-break-inside: avoid !important;
            -webkit-print-color-adjust: exact;
            print-color-adjust: exact;
            overflow: hidden;
          }
          /* 每行最后一个去除右边距 */
          .label-item:nth-child(${currentColsPerPage}n) {
            margin-right: 0 !important;
          }
          .label-content {
            display: flex !important;
            gap: 3mm !important;
            align-items: center !important;
            height: 100% !important;
          }
          .label-info {
            flex: 1 !important;
            font-size: 11px !important;
            line-height: 1.4 !important;
            overflow: hidden;
          }
          .label-info p {
            margin: 2px 0 !important;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
          .label-info .product-name {
            font-size: 16px !important;
            font-weight: bold !important;
            margin-bottom: 6px !important;
            color: #000 !important;
            line-height: 1.2 !important;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
          .label-text {
            font-weight: 500 !important;
            color: #333 !important;
          }
          .label-qr {
            text-align: center !important;
            width: 32mm !important;
            display: flex !important;
            flex-direction: column !important;
            align-items: center !important;
            justify-content: center !important;
            flex-shrink: 0;
          }
          .label-qr img {
            width: 28mm !important;
            height: 28mm !important;
            display: block !important;
          }
          .label-qr-code {
            font-size: 10px !important;
            color: #000 !important;
            margin-top: 2px !important;
            word-break: break-all !important;
            font-weight: 600 !important;
            line-height: 1.2 !important;
          }
        }
      </style>
    </head>
    <body>
       ${printContent.innerHTML}
     </body>
    </html>
  `)
  iframeDoc.close()

  // 等待所有图片加载完成后打印
  const waitForImages = () => {
    const images = iframeDoc.querySelectorAll('img')
    const totalImages = images.length
    let loadedImages = 0

    if (totalImages === 0) {
      // 没有图片，直接打印
      setTimeout(() => {
        printIframe.contentWindow?.focus()
        printIframe.contentWindow?.print()
        setTimeout(() => {
          document.body.removeChild(printIframe)
        }, 1000)
      }, 300)
      return
    }

    images.forEach((img) => {
      if (img.complete) {
        loadedImages++
      } else {
        img.onload = () => {
          loadedImages++
          if (loadedImages === totalImages) {
            // 所有图片加载完成，开始打印
            setTimeout(() => {
              printIframe.contentWindow?.focus()
              printIframe.contentWindow?.print()
              setTimeout(() => {
                document.body.removeChild(printIframe)
              }, 1000)
            }, 300)
          }
        }
        img.onerror = () => {
          loadedImages++
          console.warn('图片加载失败:', img.src)
          if (loadedImages === totalImages) {
            setTimeout(() => {
              printIframe.contentWindow?.focus()
              printIframe.contentWindow?.print()
              setTimeout(() => {
                document.body.removeChild(printIframe)
              }, 1000)
            }, 300)
          }
        }
      }
    })

    // 如果所有图片都已经加载完成
    if (loadedImages === totalImages) {
      setTimeout(() => {
        printIframe.contentWindow?.focus()
        printIframe.contentWindow?.print()
        setTimeout(() => {
          document.body.removeChild(printIframe)
        }, 1000)
      }, 300)
    }
  }

  // 延迟一点等待DOM渲染
  setTimeout(waitForImages, 200)
}
</script>

<style scoped>
.print-page {
  padding: 20px;
  background: #f0f2f5;
  min-height: 100vh;
}

.print-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.print-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.print-actions {
  display: flex;
  gap: 12px;
}

.print-info {
  margin-bottom: 20px;
}

.margin-settings {
  margin-bottom: 20px;
}

.margin-settings :deep(.el-card__header) {
  padding: 12px 20px;
  font-weight: 500;
}

.margin-settings :deep(.el-card__body) {
  padding: 20px;
}

.setting-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.setting-section {
  margin-bottom: 20px;
}

.setting-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.setting-actions {
  text-align: right;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

/* 打印区域样式 */
.label-container {
  width: 210mm;
  height: 297mm;
  margin: 0 auto 20px;
  padding: 3.4mm 6mm;
  background: white;
  box-sizing: border-box;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  justify-content: flex-start;
  page-break-after: always;
  overflow: hidden;
}

/* 最后一个容器不需要分页 */
.label-container:last-child {
  page-break-after: auto;
  margin-bottom: 0;
}

.label-item {
  border: 1px solid #000;
  padding: 3mm;
  display: flex;
  flex-direction: column;
  justify-content: center;
  box-sizing: border-box;
  width: 96mm;
  height: 70mm;
  background: white;
  page-break-inside: avoid;
  margin-right: 6mm;
  margin-bottom: 3.4mm;
}

/* 每行第2个（偶数）去除右边距 */
.label-item:nth-child(2n) {
  margin-right: 0;
}

/* 最后一行（第7、8个）去除底部间距 */
.label-item:nth-child(7),
.label-item:nth-child(8) {
  margin-bottom: 0;
}

.label-content {
  display: flex;
  gap: 4mm;
  align-items: center;
  height: 100%;
}

.label-info {
  flex: 1;
  font-size: 14px;
  line-height: 1.6;
}

.label-info p {
  margin: 4px 0;
}

.label-info .product-name {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #000;
  line-height: 1.3;
}

.label-text {
  font-weight: 500;
  color: #333;
}

.label-qr {
  text-align: center;
  width: 42mm;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.label-qr img {
  width: 38mm;
  height: 38mm;
}

.label-qr-code {
  font-size: 13px;
  color: #000;
  margin-top: 6px;
  word-break: break-all;
  font-weight: 600;
}

/* 打印样式 */
@media print {
  .no-print {
    display: none !important;
  }

  .print-page {
    padding: 0;
    background: white;
  }

  .label-container {
    width: 210mm;
    padding: 3.4mm 6mm;
    margin: 0;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
  }

  .label-item {
    border: 2px solid #000 !important;
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
    width: 96mm;
    height: 70mm;
    margin-right: 6mm;
    margin-bottom: 3.4mm;
    page-break-inside: avoid;
  }

  .label-item:nth-child(2n) {
    margin-right: 0;
  }

  @page {
    size: A4;
    margin: 0;
  }
}
</style>
