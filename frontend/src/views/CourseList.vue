<template>
  <div>
    <div class="page-head">
      <h2>全部课程</h2>
      <p>包含 SQL、MySQL、PostgreSQL 三大关系型数据库教程，内容来源于菜鸟教程。</p>
    </div>

    <div v-loading="loading">
      <el-empty v-if="!loading && courses.length === 0" description="暂无课程" />
      <el-row :gutter="20">
        <el-col v-for="course in courses" :key="course.id" :xs="24" :sm="12" :md="8">
          <el-card class="course-card" shadow="hover" @click="router.push(`/courses/${course.id}`)">
            <div class="course-badge" :class="course.code">{{ course.code.toUpperCase() }}</div>
            <h3 class="course-name">{{ course.name }}</h3>
            <p class="course-desc">{{ course.description }}</p>
            <div class="course-footer">
              <span>{{ course.lessonCount }} 个章节</span>
              <el-progress
                :percentage="progressOf(course.id)"
                :stroke-width="8"
                :color="'#2c5aa0'"
                style="width: 100px"
              />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listCourses } from '../api/course'
import { getSummary } from '../api/progress'

const router = useRouter()
const courses = ref([])
const summary = ref([])
const loading = ref(false)

const summaryMap = computed(() => {
  const map = {}
  for (const s of summary.value) map[s.courseId] = s
  return map
})

function progressOf(courseId) {
  return summaryMap.value[courseId]?.percentage ?? 0
}

onMounted(async () => {
  loading.value = true
  try {
    const [courseRes, summaryRes] = await Promise.all([listCourses(), getSummary()])
    courses.value = courseRes.data || []
    summary.value = summaryRes.data || []
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
.course-card {
  cursor: pointer;
  margin-bottom: 20px;
  min-height: 220px;
  display: flex;
  flex-direction: column;
}
.course-badge {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  color: #fff;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
}
.course-badge.sql {
  background: linear-gradient(135deg, #f56c6c, #f78989);
}
.course-badge.mysql {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}
.course-badge.postgresql {
  background: linear-gradient(135deg, #67c23a, #95d475);
}
.course-name {
  color: #1f3b73;
  margin-bottom: 8px;
}
.course-desc {
  color: #6b7280;
  font-size: 13px;
  line-height: 1.6;
  flex: 1;
}
.course-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
  color: #8a94a6;
  font-size: 13px;
}
</style>
