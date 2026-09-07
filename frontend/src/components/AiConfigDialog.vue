<template>
  <el-dialog v-model="visible" title="AI 配置" width="480px" :close-on-click-modal="false">
    <el-form :model="form" label-position="top">
      <el-form-item label="API 地址（Base URL）">
        <el-input v-model="form.apiUrl" placeholder="例如 https://api.deepseek.com/v1" />
      </el-form-item>
      <el-form-item label="API 密钥">
        <el-input
          v-model="form.apiKey"
          type="password"
          show-password
          placeholder="留空则保持不变"
        />
      </el-form-item>
      <el-form-item label="模型名称">
        <el-input v-model="form.modelName" placeholder="例如 deepseek-chat / gpt-4o-mini" />
      </el-form-item>
    </el-form>

    <el-alert
      v-if="testResult"
      :title="testResult"
      :type="testResultType"
      :closable="false"
      show-icon
      class="test-result"
    />

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="testing" @click="testConn">测试连接</el-button>
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getAiStatus, saveAiConfig, testAiConfig } from '../api/ai'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const form = reactive({ apiUrl: '', apiKey: '', modelName: '' })
const testing = ref(false)
const saving = ref(false)
const testResult = ref('')
const testResultType = ref('success')

watch(
  () => props.modelValue,
  async (v) => {
    if (!v) return
    testResult.value = ''
    form.apiKey = ''
    try {
      const res = await getAiStatus()
      if (res.data.configured) {
        form.apiUrl = res.data.apiUrl || ''
        form.modelName = res.data.modelName || ''
      }
    } catch {
      // 忽略加载失败
    }
  }
)

async function testConn() {
  if (!form.apiUrl || !form.modelName) {
    ElMessage.warning('请填写 API 地址和模型名称')
    return
  }
  testing.value = true
  testResult.value = ''
  try {
    await testAiConfig({ apiUrl: form.apiUrl, apiKey: form.apiKey, modelName: form.modelName })
    testResult.value = '连接成功，模型可正常响应'
    testResultType.value = 'success'
  } catch (e) {
    testResult.value = e.response?.data?.message || '连接失败，请检查配置'
    testResultType.value = 'error'
  } finally {
    testing.value = false
  }
}

async function save() {
  if (!form.apiUrl || !form.modelName) {
    ElMessage.warning('请填写 API 地址和模型名称')
    return
  }
  saving.value = true
  try {
    await saveAiConfig({ apiUrl: form.apiUrl, apiKey: form.apiKey, modelName: form.modelName })
    ElMessage.success('配置已保存')
    visible.value = false
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.test-result {
  margin-top: 4px;
}
</style>
