<template>
  <div v-loading="!loaded" class="ai-page">
    <!-- 未配置 API -->
    <el-card v-if="loaded && !status.configured" class="setup-card">
      <h2>配置 AI 服务</h2>
      <p class="setup-tip">
        首次使用需配置远程大模型接口（OpenAI 兼容协议，DeepSeek / Kimi / Qwen / OpenAI / Ollama 均可）。
        配置会保存到你的账号，之后无需重复填写。
      </p>
      <el-form :model="form" label-position="top" @submit.prevent>
        <el-form-item label="API 地址（Base URL）">
          <el-input v-model="form.apiUrl" placeholder="例如 https://api.deepseek.com/v1" />
        </el-form-item>
        <el-form-item label="API 密钥">
          <el-input v-model="form.apiKey" type="password" show-password placeholder="sk-..." />
        </el-form-item>
        <el-form-item label="模型名称">
          <el-input v-model="form.modelName" placeholder="例如 deepseek-chat / gpt-4o-mini" />
        </el-form-item>
        <el-button type="primary" :loading="saving" @click="saveConfig">保存并继续</el-button>
      </el-form>
    </el-card>

    <!-- 已配置但未建库 -->
    <el-card v-else-if="loaded && !status.databaseCreated" class="setup-card">
      <h2>初始化专属数据库</h2>
      <p class="setup-tip">
        将为你创建独立的练习数据库（8 张相关表、数万条数据），你可以在其中自由编写并执行 SQL。
      </p>
      <el-button type="primary" :loading="initializing" @click="initDb">初始化数据库</el-button>
    </el-card>

    <!-- 就绪：练习界面 -->
    <div v-else-if="loaded" class="practice-layout">
      <div class="main-col">
        <el-card class="block" shadow="never">
          <div class="card-head">
            <span class="card-title">当前题目</span>
            <div class="head-actions">
              <el-tag v-if="question.difficulty" size="small" class="mr8">{{ question.difficulty }}</el-tag>
              <el-button size="small" type="primary" :loading="questionLoading" @click="nextQuestion">
                下一题
              </el-button>
            </div>
          </div>
          <div v-if="question.text" class="question-text">{{ question.text }}</div>
          <div v-else class="question-empty">点击右上角「下一题」让 AI 出题</div>
          <div v-if="question.hint" class="hint">提示：{{ question.hint }}</div>
          <div v-if="question.tables && question.tables.length" class="tables">
            涉及表：
            <el-tag v-for="t in question.tables" :key="t" size="small" type="info" class="mr8">{{ t }}</el-tag>
          </div>
        </el-card>

        <el-card class="block" shadow="never">
          <div class="card-head"><span class="card-title">编写 SQL</span></div>
          <el-input
            v-model="sql"
            type="textarea"
            :rows="6"
            class="sql-editor"
            placeholder="在此输入 SQL 语句，例如：SELECT * FROM customers LIMIT 10;"
          />
          <div class="btn-row">
            <el-button type="primary" :loading="executing" :disabled="!question.text" @click="run">
              执行并检查
            </el-button>
            <el-button @click="confirmReset">重置数据库</el-button>
          </div>
        </el-card>

        <el-card v-if="result" class="block" shadow="never">
          <div class="card-head"><span class="card-title">执行结果</span></div>
          <el-alert v-if="result.error" :title="result.error" type="error" :closable="false" show-icon />
          <template v-else>
            <div v-if="!result.columns.length" class="rowcount">影响 {{ result.rowCount }} 行</div>
            <div v-else class="table-wrap">
              <table class="result-table">
                <thead>
                  <tr>
                    <th v-for="c in result.columns" :key="c">{{ c }}</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(row, ri) in result.rows" :key="ri">
                    <td v-for="(cell, ci) in row" :key="ci">{{ cell }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>
        </el-card>

        <el-card v-if="feedback" class="block" shadow="never">
          <div class="card-head">
            <span class="card-title">AI 反馈</span>
            <el-tag v-if="feedback.correct === true" type="success">正确</el-tag>
            <el-tag v-else-if="feedback.correct === false" type="danger">有误</el-tag>
            <el-tag v-else type="info">未知</el-tag>
          </div>
          <div class="feedback-text">{{ feedback.feedback }}</div>
          <div v-if="feedback.suggestions" class="suggestions">改进建议：{{ feedback.suggestions }}</div>
          <div v-if="feedback.referenceSql" class="reference">
            参考 SQL：
            <pre>{{ feedback.referenceSql }}</pre>
          </div>
        </el-card>
      </div>

      <div class="side-col">
        <el-card class="block" shadow="never">
          <div class="card-head"><span class="card-title">做题打卡</span></div>
          <HeatmapChart :data="stats.heatmap" />
        </el-card>
        <el-card class="block" shadow="never">
          <div class="card-head"><span class="card-title">正确率</span></div>
          <div class="overall">
            <span class="overall-num">{{ stats.accuracy.overall }}%</span>
            <span class="overall-meta">正确 {{ stats.accuracy.correct }} / 共 {{ stats.accuracy.total }} 题</span>
          </div>
          <AccuracyChart :data="stats.accuracy.series" />
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAiStatus,
  saveAiConfig,
  initAiDb,
  resetAiDb,
  genQuestion,
  executeSql,
  getAiStats
} from '../api/ai'
import HeatmapChart from '../components/HeatmapChart.vue'
import AccuracyChart from '../components/AccuracyChart.vue'

const loaded = ref(false)
const status = reactive({ configured: false, apiUrl: '', maskedApiKey: '', modelName: '', databaseCreated: false, schema: [] })
const form = reactive({ apiUrl: '', apiKey: '', modelName: '' })
const saving = ref(false)
const initializing = ref(false)
const questionLoading = ref(false)
const executing = ref(false)

const question = reactive({ text: '', difficulty: '', hint: '', tables: [] })
const sql = ref('')
const result = ref(null)
const feedback = ref(null)
const stats = reactive({ heatmap: [], accuracy: { overall: 0, total: 0, correct: 0, series: [] } })

async function loadStatus() {
  const res = await getAiStatus()
  Object.assign(status, res.data)
  loaded.value = true
}

async function loadStats() {
  const res = await getAiStats()
  stats.heatmap = res.data.heatmap || []
  stats.accuracy = res.data.accuracy || { overall: 0, total: 0, correct: 0, series: [] }
}

async function saveConfig() {
  if (!form.apiUrl || !form.apiKey || !form.modelName) {
    ElMessage.warning('请填写完整的 API 信息')
    return
  }
  saving.value = true
  try {
    await saveAiConfig({ apiUrl: form.apiUrl, apiKey: form.apiKey, modelName: form.modelName })
    ElMessage.success('配置已保存')
    await loadStatus()
  } finally {
    saving.value = false
  }
}

async function initDb() {
  initializing.value = true
  try {
    await initAiDb()
    ElMessage.success('专属数据库已初始化')
    await loadStatus()
  } finally {
    initializing.value = false
  }
}

async function nextQuestion() {
  questionLoading.value = true
  try {
    const res = await genQuestion()
    question.text = res.data.question
    question.difficulty = res.data.difficulty || ''
    question.hint = res.data.hint || ''
    question.tables = res.data.tables || []
    result.value = null
    feedback.value = null
  } finally {
    questionLoading.value = false
  }
}

async function run() {
  if (!sql.value.trim()) {
    ElMessage.warning('请先输入 SQL')
    return
  }
  executing.value = true
  try {
    const res = await executeSql({ question: question.text, userSql: sql.value })
    result.value = res.data.result
    feedback.value = {
      correct: res.data.correct,
      feedback: res.data.feedback,
      suggestions: res.data.suggestions,
      referenceSql: res.data.referenceSql
    }
    await loadStats()
  } finally {
    executing.value = false
  }
}

async function confirmReset() {
  try {
    await ElMessageBox.confirm('重置将清空并重建你的练习数据库，确定吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await resetAiDb()
  ElMessage.success('数据库已重置')
}

onMounted(async () => {
  try {
    await Promise.all([loadStatus(), loadStats()])
  } catch {
    loaded.value = true
  }
})
</script>

<style scoped>
.ai-page {
  max-width: 1200px;
  margin: 0 auto;
}
.setup-card {
  max-width: 560px;
  margin: 40px auto;
  padding: 8px 8px 16px;
}
.setup-card h2 {
  color: #1f3b73;
  margin-bottom: 12px;
}
.setup-tip {
  color: #6b7280;
  font-size: 14px;
  line-height: 1.7;
  margin-bottom: 16px;
}
.practice-layout {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.main-col {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.side-col {
  width: 340px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.block .card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.card-title {
  font-weight: 600;
  color: #1f3b73;
}
.head-actions {
  display: flex;
  align-items: center;
}
.mr8 {
  margin-right: 8px;
}
.question-text {
  font-size: 16px;
  color: #303133;
  line-height: 1.7;
}
.question-empty {
  color: #a0a6b0;
}
.hint {
  margin-top: 10px;
  color: #8a94a6;
  font-size: 13px;
}
.tables {
  margin-top: 10px;
  font-size: 13px;
  color: #6b7280;
}
.sql-editor :deep(textarea) {
  font-family: 'JetBrains Mono', Consolas, 'Courier New', monospace;
  font-size: 14px;
}
.btn-row {
  margin-top: 12px;
}
.rowcount {
  color: #6b7280;
}
.table-wrap {
  overflow-x: auto;
}
.result-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.result-table th,
.result-table td {
  border: 1px solid #e6e8eb;
  padding: 6px 10px;
  text-align: left;
  white-space: nowrap;
}
.result-table th {
  background: #f5f7fa;
  color: #1f3b73;
}
.feedback-text {
  color: #303133;
  line-height: 1.7;
  white-space: pre-wrap;
}
.suggestions {
  margin-top: 10px;
  color: #8a6d00;
  background: #fdf6ec;
  padding: 10px 12px;
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.6;
}
.reference {
  margin-top: 10px;
  color: #6b7280;
  font-size: 13px;
}
.reference pre {
  background: #f5f7fa;
  padding: 10px 12px;
  border-radius: 6px;
  overflow-x: auto;
  font-family: 'JetBrains Mono', Consolas, 'Courier New', monospace;
  font-size: 13px;
  white-space: pre-wrap;
}
.overall {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 8px;
}
.overall-num {
  font-size: 28px;
  font-weight: 700;
  color: #2c5aa0;
}
.overall-meta {
  color: #8a94a6;
  font-size: 13px;
}
@media (max-width: 900px) {
  .practice-layout {
    flex-direction: column;
  }
  .side-col {
    width: 100%;
  }
}
</style>
