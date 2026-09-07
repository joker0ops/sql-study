<template>
  <div>
    <div class="page-head">
      <h2>我的学习进度</h2>
      <p>跟踪你在 SQL、MySQL、PostgreSQL 三大课程中的学习完成情况。</p>
    </div>

    <div v-loading="loading">
      <el-empty v-if="!loading && summary.length === 0" description="暂无学习记录" />
      <el-row :gutter="20">
        <el-col v-for="item in summary" :key="item.courseId" :xs="24" :sm="12" :md="8">
          <el-card class="progress-card" shadow="hover">
            <div class="card-head">
              <span class="card-name">{{ item.name }}</span>
              <el-button link type="primary" @click="router.push(`/courses/${item.courseId}`)">
                去学习
              </el-button>
            </div>
            <el-progress
              type="circle"
              :percentage="item.percentage"
              :width="120"
              :color="circleColor(item.percentage)"
            />
            <div class="card-meta">
              <div>已完成 {{ item.completedLessons }} 节</div>
              <div>共 {{ item.totalLessons }} 节</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card v-if="summary.length > 0" class="overall-card" shadow="never">
        <div class="overall-head">
          <span>总体进度</span>
          <span class="overall-pct">{{ overallPercentage }}%</span>
        </div>
        <el-progress :percentage="overallPercentage" :stroke-width="14" :color="'#2c5aa0'" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getSummary } from '../api/progress'

const router = useRouter()
const summary = ref([])
const loading = ref(false)

const overallPercentage = computed(() => {
  const total = summary.value.reduce((acc, s) => acc + s.totalLessons, 0)
  const done = summary.value.reduce((acc, s) => acc + s.completedLessons, 0)
  if (total === 0) return 0
  return Math.round((done / total) * 100)
})

function circleColor(pct) {
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
.page-head {
  margin-bottom: 24px;
}
.page-head h2 {
  color: #1f3b73;
  margin-bottom: 6px;
}
.page-head p {
  color: #8a94a6;
  font-size: 14px;
}
.progress-card {
  text-align: center;
  margin-bottom: 20px;
}
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.card-name {
  font-weight: 600;
  color: #1f3b73;
}
.card-meta {
  margin-top: 16px;
  color: #8a94a6;
  font-size: 13px;
  line-height: 1.8;
}
.overall-card {
  margin-top: 8px;
}
.overall-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-weight: 600;
  color: #1f3b73;
}
.overall-pct {
  color: #2c5aa0;
}
</style>
