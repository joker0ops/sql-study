<template>
  <div v-loading="loading">
    <div v-if="course" class="page">
      <el-page-header :content="course.name" @back="router.push('/courses')" />

      <el-card class="overview-card" shadow="never">
        <p class="desc">{{ course.description }}</p>
        <div class="progress-row">
          <el-progress
            :percentage="percentage"
            :stroke-width="12"
            :color="'#2c5aa0'"
            style="flex: 1"
          />
          <span class="progress-text">已完成 {{ completedCount }} / {{ course.lessons.length }} 节</span>
        </div>
        <div class="source-tip">教程内容来源：<a href="https://www.runoob.com/" target="_blank" rel="noopener">菜鸟教程（runoob.com）</a></div>
      </el-card>

      <el-card shadow="never">
        <template #header>
          <div class="list-header">
            <span>章节列表</span>
            <el-checkbox
              :model-value="allCompleted"
              :indeterminate="someCompleted && !allCompleted"
              @change="toggleAll"
            >
              全部完成
            </el-checkbox>
          </div>
        </template>
        <div v-if="course.lessons.length === 0" class="empty">暂无章节</div>
        <div
          v-for="lesson in course.lessons"
          :key="lesson.id"
          class="lesson-item"
          :class="{ done: isCompleted(lesson.id) }"
        >
          <el-checkbox
            :model-value="isCompleted(lesson.id)"
            @change="() => toggle(lesson.id)"
          />
          <span class="lesson-index">{{ lesson.sortOrder }}</span>
          <span class="lesson-title">{{ lesson.title }}</span>
          <a
            v-if="lesson.url"
            :href="lesson.url"
            target="_blank"
            rel="noopener"
            class="lesson-link"
          >
            查看教程
          </a>
          <el-tag v-if="isCompleted(lesson.id)" size="small" type="success">已完成</el-tag>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCourse } from '../api/course'
import { getCourseProgress, toggleLesson } from '../api/progress'

const route = useRoute()
const router = useRouter()
const course = ref(null)
const completedIds = ref(new Set())
const loading = ref(false)

const courseId = computed(() => Number(route.params.id))

const percentage = computed(() => {
  if (!course.value || course.value.lessons.length === 0) return 0
  return Math.round((completedCount.value / course.value.lessons.length) * 100)
})

const completedCount = computed(() => {
  if (!course.value) return 0
  return course.value.lessons.filter((l) => completedIds.value.has(l.id)).length
})

const allCompleted = computed(() => {
  if (!course.value) return false
  return course.value.lessons.length > 0 && completedCount.value === course.value.lessons.length
})

const someCompleted = computed(() => completedCount.value > 0)

function isCompleted(lessonId) {
  return completedIds.value.has(lessonId)
}

async function toggle(lessonId) {
  try {
    const res = await toggleLesson(lessonId)
    const next = new Set(completedIds.value)
    if (res.data.completed) next.add(lessonId)
    else next.delete(lessonId)
    completedIds.value = next
  } catch (e) {
    // 错误已提示
  }
}

async function toggleAll(val) {
  const target = !allCompleted.value
  const lessons = course.value.lessons
  const pending = lessons.filter((l) => isCompleted(l.id) !== target)
  if (pending.length === 0) return
  try {
    // 逐个切换，最终状态一致
    for (const l of pending) {
      await toggleLesson(l.id)
    }
    await refreshProgress()
    ElMessage.success(target ? '已标记全部完成' : '已取消全部完成')
  } catch (e) {
    await refreshProgress()
  }
}

async function refreshProgress() {
  const res = await getCourseProgress(courseId.value)
  completedIds.value = new Set(res.data || [])
}

onMounted(async () => {
  loading.value = true
  try {
    const [courseRes, progressRes] = await Promise.all([
      getCourse(courseId.value),
      getCourseProgress(courseId.value)
    ])
    course.value = courseRes.data
    completedIds.value = new Set(progressRes.data || [])
  } catch (e) {
    // 错误已提示
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page {
  max-width: 860px;
  margin: 0 auto;
}
.overview-card {
  margin: 16px 0;
}
.desc {
  color: #6b7280;
  margin-bottom: 16px;
  line-height: 1.6;
}
.progress-row {
  display: flex;
  align-items: center;
  gap: 16px;
}
.progress-text {
  white-space: nowrap;
  color: #2c5aa0;
  font-size: 14px;
}
.source-tip {
  margin-top: 12px;
  color: #8a94a6;
  font-size: 13px;
}
.source-tip a {
  color: #2c5aa0;
}
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #1f3b73;
}
.lesson-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 8px;
  border-bottom: 1px solid #f0f2f5;
}
.lesson-item:last-child {
  border-bottom: none;
}
.lesson-item.done .lesson-title {
  color: #909399;
  text-decoration: line-through;
}
.lesson-index {
  min-width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #eef2f8;
  color: #2c5aa0;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.lesson-title {
  flex: 1;
  color: #303133;
}
.lesson-link {
  color: #2c5aa0;
  font-size: 13px;
  text-decoration: none;
}
.lesson-link:hover {
  text-decoration: underline;
}
.empty {
  text-align: center;
  color: #8a94a6;
  padding: 24px;
}
</style>
