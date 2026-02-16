<template>
  <div class="app-container">
    <h2 class="page-title">通用设置管理</h2>
    <el-tabs type="border-card">
      <el-tab-pane label="通用设置">
        <el-form ref="generalFormRef" :model="config" :rules="generalRules" label-width="120px" style="max-width: 800px">
          <el-form-item label="产品单位" v-if="config.productUnits && config.productUnits.length > 0">
             <div style="display:flex; align-items:center; gap:10px; margin-bottom:6px;">
               <el-tag type="info">产品单位</el-tag>
               <el-button v-if="canEdit" size="small" type="danger" @click="removeProductUnitsGroup">删除组</el-button>
             </div>
             <div class="tag-group">
                <el-tag
                  v-for="tag in config.productUnits"
                  :key="tag"
                  :closable="canEdit"
                  :disable-transitions="false"
                  @close="handleClose(tag, 'productUnits')"
                >
                  {{ tag }}
                </el-tag>
                <el-input
                  v-if="canEdit && inputVisible.productUnits"
                  ref="productUnitsInputRef"
                  v-model="inputValue.productUnits"
                  class="input-new-tag"
                  size="small"
                  @keyup.enter="handleInputConfirm('productUnits')"
                  @blur="handleInputConfirm('productUnits')"
                />
                <el-button v-if="canEdit && !inputVisible.productUnits" class="button-new-tag" size="small" @click="showInput('productUnits')">
                  + 添加
                </el-button>
             </div>
          </el-form-item>
          
          <!-- 新增的自定义分类组展示：放在客户分类下方、添加分类组上方（不显示额外标题） -->
          <div v-for="group in Object.keys(config.customCategories)" :key="group" style="margin-bottom: 12px;">
            <div style="display:flex; align-items:center; gap:10px; margin-bottom:6px;">
              <el-tag type="warning">{{ group }}</el-tag>
              <el-button v-if="canEdit" size="small" type="danger" @click="removeCategoryGroup(group)">删除组</el-button>
            </div>
            <div class="tag-group">
              <el-tag
                v-for="tag in config.customCategories[group]"
                :key="group + '_' + tag"
                :closable="canEdit"
                :disable-transitions="false"
                @close="handleGroupTagClose(group, tag)"
              >
                {{ tag }}
              </el-tag>
              <el-input
                v-if="canEdit && groupInputVisible[group]"
                v-model="groupInputValue[group]"
                class="input-new-tag"
                size="small"
                @keyup.enter="handleGroupInputConfirm(group)"
                @blur="handleGroupInputConfirm(group)"
              />
              <el-button v-if="canEdit && !groupInputVisible[group]" class="button-new-tag" size="small" @click="showGroupInput(group)">
                + 添加
              </el-button>
            </div>
          </div>
          <el-form-item label="设备分类" v-if="config.deviceCategories && config.deviceCategories.length > 0">
             <div style="display:flex; align-items:center; gap:10px; margin-bottom:6px;">
               <el-tag type="info">设备分类</el-tag>
               <el-button v-if="canEdit" size="small" type="danger" @click="removeDeviceCategoriesGroup">删除组</el-button>
             </div>
             <div class="tag-group">
                <el-tag
                  v-for="tag in config.deviceCategories"
                  :key="tag"
                  :closable="canEdit"
                  :disable-transitions="false"
                  @close="handleClose(tag, 'deviceCategories')"
                >
                  {{ tag }}
                </el-tag>
                <el-input
                  v-if="canEdit && inputVisible.deviceCategories"
                  ref="deviceCategoriesInputRef"
                  v-model="inputValue.deviceCategories"
                  class="input-new-tag"
                  size="small"
                  @keyup.enter="handleInputConfirm('deviceCategories')"
                  @blur="handleInputConfirm('deviceCategories')"
                />
                <el-button v-if="canEdit && !inputVisible.deviceCategories" class="button-new-tag" size="small" @click="showInput('deviceCategories')">
                  + 添加
                </el-button>
             </div>
          </el-form-item>
          <el-form-item label="客户标签" v-if="config.customerTags && config.customerTags.length > 0">
             <div style="display:flex; align-items:center; gap:10px; margin-bottom:6px;">
               <el-tag type="info">客户标签</el-tag>
               <el-button v-if="canEdit" size="small" type="danger" @click="removeCustomerTagsGroup">删除组</el-button>
             </div>
             <div class="tag-group">
                <el-tag
                  v-for="tag in config.customerTags"
                  :key="tag"
                  :closable="canEdit"
                  :disable-transitions="false"
                  @close="handleClose(tag, 'customerTags')"
                >
                  {{ tag }}
                </el-tag>
                <el-input
                  v-if="canEdit && inputVisible.customerTags"
                  ref="customerTagsInputRef"
                  v-model="inputValue.customerTags"
                  class="input-new-tag"
                  size="small"
                  @keyup.enter="handleInputConfirm('customerTags')"
                  @blur="handleInputConfirm('customerTags')"
                />
                <el-button v-if="canEdit && !inputVisible.customerTags" class="button-new-tag" size="small" @click="showInput('customerTags')">
                  + 添加
                </el-button>
             </div>
          </el-form-item>
          <el-form-item label="客户分类" v-if="config.customerCategories && config.customerCategories.length > 0">
             <div style="display:flex; align-items:center; gap:10px; margin-bottom:6px;">
               <el-tag type="info">客户分类</el-tag>
               <el-button v-if="canEdit" size="small" type="danger" @click="removeCustomerCategoriesGroup">删除组</el-button>
             </div>
             <div class="tag-group">
                <el-tag
                  v-for="tag in config.customerCategories"
                  :key="tag"
                  :closable="canEdit"
                  :disable-transitions="false"
                  @close="handleClose(tag, 'customerCategories')"
                >
                  {{ tag }}
                </el-tag>
                <el-input
                  v-if="canEdit && inputVisible.customerCategories"
                  ref="customerCategoriesInputRef"
                  v-model="inputValue.customerCategories"
                  class="input-new-tag"
                  size="small"
                  @keyup.enter="handleInputConfirm('customerCategories')"
                  @blur="handleInputConfirm('customerCategories')"
                />
                <el-button v-if="canEdit && !inputVisible.customerCategories" class="button-new-tag" size="small" @click="showInput('customerCategories')">
                  + 添加
                </el-button>
             </div>
          </el-form-item>
          <el-form-item label="自定义分类组（新增）" v-if="canEdit">
             <div style="display:flex; gap:10px; align-items:center; margin-bottom:10px;">
               <el-input v-model="newGroupName" placeholder="分类组名称" style="width:220px" />
               <el-button type="primary" size="small" @click="addCategoryGroup">添加分类组</el-button>
             </div>
          </el-form-item>
          <el-form-item v-if="canEdit">
            <el-button type="primary" @click="handleSave('general', generalFormRef)">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="环境诊断">
        <div class="diag">
          <el-card shadow="never" class="diag-card">
            <template #header>数据库连接信息</template>
            <div v-if="dbInfo">
              <div>数据库：{{ dbInfo.database }}</div>
              <div>地址：{{ dbInfo.host }}:{{ dbInfo.port }}</div>
              <div>JDBC：{{ dbInfo.jdbcUrl }}</div>
              <div>用户：{{ dbInfo.username }}</div>
              <div>产品：{{ dbInfo.product }} {{ dbInfo.version }}</div>
            </div>
            <div v-else>加载中...</div>
          </el-card>
          <el-card shadow="never" class="diag-card">
            <template #header>表结构检查（crm_customer_tech_info）</template>
            <div style="margin-bottom:8px;">
              <el-button type="primary" size="small" @click="loadTechInfoColumns">刷新表结构</el-button>
              <el-button size="small" @click="checkTechInfoTable">检查必需列</el-button>
            </div>
            <div v-if="columns.length > 0">
              <el-table :data="columns" size="small" style="width:100%;" border>
                <el-table-column prop="COLUMN_NAME" label="列名" width="200" />
                <el-table-column prop="DATA_TYPE" label="类型" width="120" />
                <el-table-column prop="IS_NULLABLE" label="可空" width="100" />
                <el-table-column prop="COLUMN_DEFAULT" label="默认值" />
              </el-table>
            </div>
            <div v-else>尚未加载</div>
            <div v-if="requiredStatus">
              <div style="margin-top:10px; font-weight:600;">必需列状态：</div>
              <el-tag
                v-for="(present, name) in requiredStatus"
                :key="name"
                :type="present ? 'success' : 'danger'"
                style="margin-right:6px; margin-top:6px;"
              >
                {{ name }}：{{ present ? '存在' : '缺失' }}
              </el-tag>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      <el-tab-pane label="AI大模型配置">
        <el-form ref="aiConfigFormRef" :model="aiConfig" :rules="aiConfigRules" label-width="150px" style="max-width: 700px">
          <el-form-item label="启用AI功能">
            <el-switch v-model="aiConfig.enabled" active-text="启用" inactive-text="禁用" />
          </el-form-item>
          <el-form-item label="API Key" prop="apiKey">
            <el-input 
              v-model="aiConfig.apiKey" 
              type="password" 
              show-password
              placeholder="请输入 API Key"
            />
            <span class="tips">硅基流动在 <a href="https://cloud.siliconflow.cn/account/ak" target="_blank">https://cloud.siliconflow.cn/account/ak</a> 获取</span>
          </el-form-item>
          <el-form-item label="API 地址" prop="baseUrl">
            <el-input v-model="aiConfig.baseUrl" placeholder="https://api.siliconflow.cn/v1/chat/completions" />
            <span class="tips">默认使用硅基流动，可修改为其他兼容 OpenAI 格式的 API 地址</span>
          </el-form-item>
          <el-form-item label="模型名称" prop="model">
            <el-input v-model="aiConfig.model" placeholder="deepseek-ai/DeepSeek-V3.2" />
            <span class="tips">模型格式：厂商/模型名，如 deepseek-ai/DeepSeek-V3.2</span>
          </el-form-item>
          <el-form-item label="Temperature">
            <el-slider v-model="aiConfig.temperature" :min="0" :max="2" :step="0.1" show-stops />
            <span class="tips">值越大，生成结果越随机（推荐0.7）</span>
          </el-form-item>
          <el-form-item label="Max Tokens">
            <el-input-number v-model="aiConfig.maxTokens" :min="100" :max="8192" :step="100" />
            <span class="tips" style="margin-left: 10px">生成文本的最大长度</span>
          </el-form-item>
          <el-form-item label="系统提示词">
            <el-input 
              v-model="aiConfig.systemPrompt" 
              type="textarea" 
              :rows="4"
              placeholder="设置AI的系统提示词，用于定义AI的角色和行为"
            />
          </el-form-item>
          <el-form-item v-if="canEdit">
            <el-button type="primary" @click="handleSave('ai', aiConfigFormRef)">保存配置</el-button>
            <el-button @click="testAIConnection" :loading="testing">测试连接</el-button>
          </el-form-item>
        </el-form>
        
        <el-divider />
        
        <div class="ai-config-help">
          <h3>常用配置参考</h3>
          
          <el-card class="config-card">
            <template #header>
              <span>火山引擎</span>
            </template>
            <p><strong>API Key：</strong>6f98887b-b48b-4219-8587-e667dfb62d2f</p>
            <p><strong>API 地址：</strong>https://ark.cn-beijing.volces.com/api/v3/chat/completions</p>
            <p><strong>模型名称：</strong></p>
            <ul>
              <li>doubao-seed-2-0-mini-260215：面向低时延、高并发与成本敏感场景，强调快速响应与灵活推理部署，支持四档位思考与多模态理解能力。</li>
              <li>doubao-seed-2-0-pro-260215：侧重长链路推理能力与复杂任务稳定性，适配真实业务中的复杂场景</li>
              <li>doubao-seedance-1-5-pro-251215：支持文生音画、首帧图生音画、首尾帧图生音画</li>
              <li>doubao-seed-2-0-code-preview-260215：Seed 2.0 的编程加强版，更适合 Agentic Coding</li>
            </ul>
          </el-card>
          
          <el-card class="config-card" style="margin-top: 15px;">
            <template #header>
              <span>硅基流动</span>
            </template>
            <p><strong>API Key：</strong>sk-obzrmcqpfyyypxsdtflvykefqbkuuoagitwyikfwwgjoglcr</p>
            <p><strong>API 地址：</strong>https://api.siliconflow.cn/v1/chat/completions</p>
            <p><strong>模型名称：</strong>deepseek-ai/DeepSeek-V3.2</p>
            <ul>
              <li>Pro/MiniMaxAI/MiniMax-M2.5：MiniMax 推出的最新大语言模型，通过在数十万个复杂真实环境中进行大规模强化学习训练。</li>
              <li>Pro/zai-org/GLM-5：GLM-5 是智谱推出的新一代大语言模型，专注于复杂系统工程和长周期 Agent 任务。</li>
              <li>Pro/moonshotai/Kimi-K2.5：Kimi K2.5 是一款开源的原生多模态智能体模型，基于 Kimi-K2-Base，通过约 15 万亿混合视觉与文本 tokens 持续预训练而成。</li>
            </ul>
          </el-card>
        </div>
      </el-tab-pane>
      <el-tab-pane label="企业信息设置">
        <el-form ref="enterpriseFormRef" :model="enterprise" :rules="enterpriseRules" label-width="120px" style="max-width: 600px">
          <el-form-item label="企业名称" prop="name">
            <el-input v-model="enterprise.name" />
          </el-form-item>
          <el-form-item label="企业Logo">
            <el-upload
              class="avatar-uploader"
              action="/api/common/upload"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleLogoSuccess"
            >
              <img v-if="enterprise.logo" :src="enterprise.logo" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>
          <el-form-item label="支付宝收款码">
            <el-upload
              class="avatar-uploader"
              action="/api/common/upload"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleAlipaySuccess"
            >
              <img v-if="enterprise.alipayQr" :src="enterprise.alipayQr" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>
          <el-form-item label="微信收款码">
            <el-upload
              class="avatar-uploader"
              action="/api/common/upload"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleWechatSuccess"
            >
              <img v-if="enterprise.wechatQr" :src="enterprise.wechatQr" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>
          <el-form-item v-if="canEdit">
            <el-button type="primary" @click="handleSave('enterprise', enterpriseFormRef)">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed, watch } from 'vue'
import type { FormInstance, FormRules, InputInstance, UploadProps } from 'element-plus'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const isAdmin = computed(() => {
  const ui: any = userStore.userInfo
  const type = ui?.userType
  const username = ui?.username
  const roleId = ui?.roleId
  const roleName = ui?.roleName
  return type === 'admin'
    || username === 'admin'
    || roleId === 1
    || String(roleName || '').includes('管理员')
})

// 判断是否有指定权限码
const hasPermission = (permCode: string) => {
  const ui: any = userStore.userInfo
  const permissionCodes = ui?.permissionCodes || []
  return isAdmin.value || permissionCodes.includes(permCode)
}

const canEdit = computed(() => hasPermission('system:config:edit'))

const generalFormRef = ref<FormInstance>()
const enterpriseFormRef = ref<FormInstance>()
const aiConfigFormRef = ref<FormInstance>()
const customerCategoriesInputRef = ref<InputInstance>()
const productUnitsInputRef = ref<InputInstance>()
const deviceCategoriesInputRef = ref<InputInstance>()
const customerTagsInputRef = ref<InputInstance>()
const newGroupName = ref('')
const testing = ref(false)

const config = reactive<{
    productUnits: string[],
    deviceCategories: string[],
    customerTags: string[],
    customerCategories: string[],
    intermediaryPayMethods: string[],
    customCategories: Record<string, string[]>
}>({
  productUnits: ['台', '张', '套', '个', '件'],
  deviceCategories: ['自产整机', '自产部件', '外购部件', '外购整机'],
  customerTags: ['VIP', '新客户', '重点客户'],
  customerCategories: ['企业', '个人', '代理商'],
  intermediaryPayMethods: ['开票支付', '工资冲抵', '货款冲抵'],
  customCategories: {}
})

const inputVisible = reactive<any>({
    productUnits: false,
    deviceCategories: false,
    customerTags: false,
    customerCategories: false
})
const inputValue = reactive<any>({
    productUnits: '',
    deviceCategories: '',
    customerTags: '',
    customerCategories: ''
})
const groupInputVisible = reactive<Record<string, boolean>>({})
const groupInputValue = reactive<Record<string, string>>({})

const handleClose = (tag: string, type: 'productUnits' | 'deviceCategories' | 'customerTags' | 'customerCategories') => {
  config[type].splice(config[type].indexOf(tag), 1)
}

const showInput = (type: string) => {
  inputVisible[type] = true
  nextTick(() => {
    if (type === 'productUnits') {
        productUnitsInputRef.value?.input?.focus()
    } else if (type === 'deviceCategories') {
        deviceCategoriesInputRef.value?.input?.focus()
    } else if (type === 'customerTags') {
        customerTagsInputRef.value?.input?.focus()
    } else if (type === 'customerCategories') {
        customerCategoriesInputRef.value?.input?.focus()
    }
  })
}

const handleInputConfirm = (type: 'productUnits' | 'deviceCategories' | 'customerTags' | 'customerCategories') => {
  if (inputValue[type]) {
    config[type].push(inputValue[type])
  }
  inputVisible[type] = false
  inputValue[type] = ''
}

const addCategoryGroup = () => {
  const name = newGroupName.value?.trim()
  if (!name) return
  if (!config.customCategories[name]) {
    config.customCategories[name] = []
    groupInputVisible[name] = false
    groupInputValue[name] = ''
  }
  newGroupName.value = ''
}

const removeCategoryGroup = (group: string) => {
  delete config.customCategories[group]
  delete groupInputVisible[group]
  delete groupInputValue[group]
}

const showGroupInput = (group: string) => {
  groupInputVisible[group] = true
}

const handleGroupInputConfirm = (group: string) => {
  const val = groupInputValue[group]?.trim()
  if (val) {
    config.customCategories[group].push(val)
  }
  groupInputVisible[group] = false
  groupInputValue[group] = ''
}

const handleGroupTagClose = (group: string, tag: string) => {
  const arr = config.customCategories[group]
  const idx = arr.indexOf(tag)
  if (idx !== -1) arr.splice(idx, 1)
}

const removeProductUnitsGroup = () => { config.productUnits = [] }
const removeDeviceCategoriesGroup = () => { config.deviceCategories = [] }
const removeCustomerTagsGroup = () => { config.customerTags = [] }
const removeCustomerCategoriesGroup = () => { config.customerCategories = [] }


const enterprise = reactive({
  name: '原邑销售管理平台',
  logo: '',
  alipayQr: '',
  wechatQr: ''
})

// 上传请求头（包含认证token）
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    Authorization: token ? `Bearer ${token}` : ''
  }
})

// AI 配置
const aiConfig = reactive({
  enabled: false,
  apiKey: '',
  baseUrl: 'https://api.siliconflow.cn/v1/chat/completions',
  model: 'deepseek-ai/DeepSeek-V3.2',
  temperature: 0.7,
  maxTokens: 4096,
  systemPrompt: '你是一名专业的图书馆整体规划方案撰写专家，请根据客户需求撰写专业、详细的方案。'
})

const handleLogoSuccess: UploadProps['onSuccess'] = (res) => { enterprise.logo = res.data }
const handleAlipaySuccess: UploadProps['onSuccess'] = (res) => { enterprise.alipayQr = res.data }
const handleWechatSuccess: UploadProps['onSuccess'] = (res) => { enterprise.wechatQr = res.data }

const generalRules = reactive<FormRules>({})
const enterpriseRules = reactive<FormRules>({
  name: [{ required: true, message: '请输入企业名称', trigger: 'blur' }]
})
const aiConfigRules = reactive<FormRules>({
  provider: [{ required: true, message: '请选择AI提供商', trigger: 'change' }],
  apiKey: [{ required: true, message: '请输入API Key', trigger: 'blur' }],
  model: [{ required: true, message: '请输入模型名称', trigger: 'blur' }]
})

const dbInfo = ref<any>(null)
const columns = ref<any[]>([])
const requiredStatus = ref<Record<string, boolean> | null>(null)

const fetchConfig = async () => {
  try {
    const res: any = await request({ url: '/system/config/list', method: 'get', params: { size: 100 } })
    const general = (res.records || res || []).find((item: any) => item.configKey === 'general_settings')
    if (general && general.configValue) {
      const saved = JSON.parse(general.configValue)
      config.productUnits = saved.productUnits ?? []
      config.deviceCategories = saved.deviceCategories ?? []
      config.customerTags = saved.customerTags ?? []
      config.customerCategories = saved.customerCategories ?? []
      config.intermediaryPayMethods = saved.intermediaryPayMethods ?? ['开票支付', '工资冲抵', '货款冲抵']
      config.customCategories = saved.customCategories ?? {}
    }
    const enterpriseCfg = (res.records || res || []).find((item: any) => item.configKey === 'enterprise_info')
    if (enterpriseCfg && enterpriseCfg.configValue) {
      const savedEnt = JSON.parse(enterpriseCfg.configValue)
      enterprise.name = savedEnt.name ?? enterprise.name
      enterprise.logo = savedEnt.logo ?? ''
      enterprise.alipayQr = savedEnt.alipayQr ?? ''
      enterprise.wechatQr = savedEnt.wechatQr ?? ''
    }
    const aiCfg = (res.records || res || []).find((item: any) => item.configKey === 'ai_model_config')
    if (aiCfg && aiCfg.configValue) {
      const savedAi = JSON.parse(aiCfg.configValue)
      aiConfig.enabled = savedAi.enabled ?? false
      aiConfig.apiKey = savedAi.apiKey ?? ''
      aiConfig.baseUrl = savedAi.baseUrl ?? 'https://api.siliconflow.cn/v1/chat/completions'
      aiConfig.model = savedAi.model ?? 'deepseek-ai/DeepSeek-V3.2'
      aiConfig.temperature = savedAi.temperature ?? 0.7
      aiConfig.maxTokens = savedAi.maxTokens ?? 4096
      aiConfig.systemPrompt = savedAi.systemPrompt ?? ''
    }
  } catch (e) {
    console.error('Fetch general config failed', e)
  }
  try {
    dbInfo.value = await request({ url: '/system/diagnostics/db-info', method: 'get' })
  } catch (e) {
    dbInfo.value = null
  }
}

const loadTechInfoColumns = async () => {
  try {
    columns.value = await request({ url: '/system/diagnostics/table-columns', method: 'get', params: { table: 'crm_customer_tech_info' } })
  } catch (e) {
    columns.value = []
  }
}

const checkTechInfoTable = async () => {
  try {
    const res: any = await request({ url: '/system/diagnostics/check-tech-info-table', method: 'get' })
    requiredStatus.value = res?.requiredStatus || null
    if (res?.columns) {
      columns.value = res.columns
    }
  } catch (e) {
    requiredStatus.value = null
  }
}

const handleSave = async (type: string, formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      if (type === 'general') {
        await request({
          url: '/system/config',
          method: 'post',
          data: { configKey: 'general_settings', configValue: JSON.stringify(config), configName: '通用设置' }
        })
      } else if (type === 'ai') {
        const saveData = {
          enabled: aiConfig.enabled,
          apiKey: aiConfig.apiKey,
          baseUrl: aiConfig.baseUrl,
          model: aiConfig.model,
          temperature: aiConfig.temperature,
          maxTokens: aiConfig.maxTokens,
          systemPrompt: aiConfig.systemPrompt
        }
        
        await request({
          url: '/system/config',
          method: 'post',
          data: { configKey: 'ai_model_config', configValue: JSON.stringify(saveData), configName: 'AI大模型配置' }
        })
      } else {
        await request({
          url: '/system/config',
          method: 'post',
          data: { configKey: 'enterprise_info', configValue: JSON.stringify(enterprise), configName: '企业信息' }
        })
      }
      ElMessage.success('保存成功')
    }
  })
}

// 测试AI连接
const testAIConnection = async () => {
  testing.value = true
  try {
    // 如果没有填写 baseUrl，使用默认值
    const testData = {
      ...aiConfig,
      baseUrl: aiConfig.baseUrl || 'https://api.siliconflow.cn/v1/chat/completions'
    }
    const res: any = await request({
      url: '/system/config/ai-test',
      method: 'post',
      data: testData,
      timeout: 30000
    })
    if (res === true) {
      ElMessage.success('连接测试成功！')
    } else {
      ElMessage.error('连接测试失败')
    }
  } catch (error) {
    ElMessage.error('连接测试失败')
  } finally {
    testing.value = false
  }
}


onMounted(() => {
  fetchConfig()
  loadTechInfoColumns()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.diag {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}
.diag-card {
  padding-bottom: 8px;
}
.tips {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}
.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}
.input-new-tag {
  width: 120px;
}

.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 100px;
  height: 100px;
  background-color: #fafafa;
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 24px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: contain;
}

.ai-config-help {
  margin-top: 20px;
}

.ai-config-help h3 {
  margin-bottom: 15px;
  color: #303133;
}

.config-card {
  margin-bottom: 15px;
}

.config-card p {
  margin: 8px 0;
  line-height: 1.6;
}

.config-card ul {
  margin: 10px 0;
  padding-left: 20px;
}

.config-card li {
  margin: 5px 0;
  line-height: 1.5;
  color: #606266;
}
</style>
