<template>
  <div class="heatmap">
    <div class="scroll">
      <div class="canvas" :style="{ width: canvasWidth + 'px' }">
        <div class="months">
          <span
            v-for="m in months"
            :key="m.key"
            class="month"
            :style="{ left: (m.col * CELL_STRIDE) + 'px' }"
          >{{ m.label }}</span>
        </div>
        <div class="body">
          <div class="day-labels">
            <span v-for="(d, i) in DAY_LABELS" :key="i">{{ d }}</span>
          </div>
          <div class="grid">
            <span
              v-for="cell in cells"
              :key="cell.date"
              class="cell"
              :style="{ background: cell.future ? 'transparent' : colorOf(cell.count) }"
              :title="cell.future ? '' : titleOf(cell)"
            ></span>
          </div>
        </div>
      </div>
    </div>
    <div class="legend">
      <span class="legend-text">少</span>
      <span v-for="(lv, i) in LEVELS" :key="i" class="cell legend-cell" :style="{ background: lv }"></span>
      <span class="legend-text">多</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: { type: Array, default: () => [] } // [{ date: 'yyyy-MM-dd', count: n }]
})

const CELL = 10
const GAP = 3
const CELL_STRIDE = CELL + GAP
const WEEK_COUNT = 53
const DAY_LABELS = ['一', '', '三', '', '五', '', '日']
const LEVELS = ['#ebedf0', '#9be9a8', '#40c463', '#30a14e', '#216e39']

function pad(n) {
  return String(n).padStart(2, '0')
}

function fmt(d) {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

function mondayOf(d) {
  const x = new Date(d)
  const diff = (x.getDay() + 6) % 7 // 距离周一的天数
  x.setDate(x.getDate() - diff)
  x.setHours(0, 0, 0, 0)
  return x
}

const cells = computed(() => {
  const countMap = {}
  props.data.forEach((d) => {
    countMap[d.date] = d.count
  })
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const start = mondayOf(today)
  start.setDate(start.getDate() - (WEEK_COUNT - 1) * 7)

  const result = []
  for (let w = 0; w < WEEK_COUNT; w++) {
    for (let d = 0; d < 7; d++) {
      const date = new Date(start)
      date.setDate(start.getDate() + w * 7 + d)
      const key = fmt(date)
      const future = date > today
      result.push({ date: key, count: countMap[key] || 0, future })
    }
  }
  return result
})

const months = computed(() => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const start = mondayOf(today)
  start.setDate(start.getDate() - (WEEK_COUNT - 1) * 7)

  const list = []
  let prevKey = ''
  for (let w = 0; w < WEEK_COUNT; w++) {
    const date = new Date(start)
    date.setDate(start.getDate() + w * 7)
    const key = `${date.getFullYear()}-${date.getMonth()}`
    if (key !== prevKey) {
      list.push({ key, col: w, label: `${date.getMonth() + 1}月` })
      prevKey = key
    }
  }
  return list
})

const canvasWidth = computed(() => WEEK_COUNT * CELL_STRIDE + 24)

function colorOf(count) {
  if (count <= 0) return LEVELS[0]
  if (count <= 2) return LEVELS[1]
  if (count <= 5) return LEVELS[2]
  if (count <= 9) return LEVELS[3]
  return LEVELS[4]
}

function titleOf(cell) {
  return cell.count ? `${cell.date}：${cell.count} 题` : `${cell.date}：无记录`
}
</script>

<style scoped>
.heatmap {
  font-size: 12px;
  color: #6b7280;
}
.scroll {
  overflow-x: auto;
  padding-bottom: 2px;
}
.canvas {
  position: relative;
}
.months {
  position: relative;
  height: 18px;
  margin-left: 30px;
}
.month {
  position: absolute;
  top: 0;
  font-size: 11px;
  color: #8a94a6;
  white-space: nowrap;
}
.body {
  display: flex;
}
.day-labels {
  display: flex;
  flex-direction: column;
  gap: 3px;
  width: 26px;
  margin-right: 4px;
  flex-shrink: 0;
}
.day-labels span {
  height: 10px;
  line-height: 10px;
  font-size: 10px;
  color: #8a94a6;
  text-align: right;
}
.grid {
  display: grid;
  grid-auto-flow: column;
  grid-template-rows: repeat(7, 10px);
  grid-auto-columns: 10px;
  gap: 3px;
}
.cell {
  width: 10px;
  height: 10px;
  border-radius: 2px;
}
.legend {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  margin-top: 8px;
  font-size: 11px;
}
.legend-cell {
  display: inline-block;
}
.legend-text {
  color: #8a94a6;
}
</style>
