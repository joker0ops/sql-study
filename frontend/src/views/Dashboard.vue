<template>
  <div>
    <el-card class="welcome-card" shadow="never">
      <h2>你好，{{ auth.user?.username }} 👋</h2>
      <p>欢迎回到关系型数据库学习网，继续你的学习之旅吧。</p>
      <div class="welcome-actions">
        <el-button type="primary" @click="router.push('/courses')">浏览课程</el-button>
        <el-button @click="router.push('/progress')">查看学习进度</el-button>
      </div>
    </el-card>

    <h3 class="section-title">学习概览</h3>
    <div v-loading="loading">
      <el-empty v-if="!loading && summary.length === 0" description="暂无课程数据" />
      <el-row :gutter="16">
        <el-col v-for="item in summary" :key="item.courseId" :xs="24" :sm="12" :md="8">
          <el-card class="summary-card" shadow="hover" @click="router.push(`/courses/${item.courseId}`)">
            <div class="summary-head">
              <span class="summary-name">{{ item.name }}</span>
              <span class="summary-pct">{{ item.percentage }}%</span>
            </div>
            <el-progress :percentage="item.percentage" :stroke-width="10" :color="progressColor(item.percentage)" />
            <div class="summary-meta">已完成 {{ item.completedLessons }} / {{ item.totalLessons }} 节</div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getSummary } from '../api/progress'

const router = useRouter()
const auth = useAuthStore()
const summary = ref([])
const loading = ref(false)

function progressColor(pct) {
  if (pct >= 80) return '#67c23a'
  if (pct >= 40) return '#409eff'
  return '#e6a23c'
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getSummary()
    summary.value = res.data || []
  } catch (e) {
    // 错误已提示
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.welcome-card {
  margin-bottom: 24px;
}
.welcome-card h2 {
  color: #1f3b73;
  margin-bottom: 8px;
}
.welcome-card p {
  color: #6b7280;
  margin-bottom: 16px;
}
.section-title {
  margin: 8px 0 16px;
  color: #1f3b73;
}
.summary-card {
  cursor: pointer;
  margin-bottom: 16px;
}
.summary-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.summary-name {
  font-weight: 600;
  color: #1f3b73;
}
.summary-pct {
  color: #2c5aa0;
  font-weight: 600;
}
.summary-meta {
  margin-top: 8px;
  color: #8a94a6;
  font-size: 13px;
}
</style>
