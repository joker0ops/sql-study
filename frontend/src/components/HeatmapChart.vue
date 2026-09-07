<template>
  <div ref="chartRef" class="chart"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: { type: Array, default: () => [] } // [{ date: 'yyyy-MM-dd', count: n }]
})

const chartRef = ref()
let chart = null

function render() {
  if (!chart) return
  const data = props.data.map((d) => [d.date, d.count])
  const max = Math.max(1, ...props.data.map((d) => d.count))
  chart.setOption({
    tooltip: {
      formatter: (p) => `${p.value[0]}：${p.value[1]} 题`
    },
    visualMap: {
      min: 0,
      max,
      calculable: false,
      orient: 'horizontal',
      left: 'center',
      bottom: 0,
      inRange: { color: ['#ebedf0', '#c6e48b', '#7bc96f', '#239a3b', '#196127'] }
    },
    calendar: {
      top: 10,
      left: 30,
      right: 10,
      cellSize: ['auto', 13],
      range: rangeOfYear(),
      itemStyle: { borderWidth: 2, borderColor: '#fff' },
      splitLine: { show: false },
      dayLabel: { firstDay: 1, nameMap: 'ZH' },
      monthLabel: { nameMap: 'ZH' },
      yearLabel: { show: false }
    },
    series: [
      {
        type: 'heatmap',
        coordinateSystem: 'calendar',
        data
      }
    ]
  })
}

function rangeOfYear() {
  const end = new Date()
  const start = new Date()
  start.setFullYear(start.getFullYear() - 1)
  const fmt = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  return [fmt(start), fmt(end)]
}

function onResize() {
  if (chart) chart.resize()
}

onMounted(() => {
  chart = echarts.init(chartRef.value)
  render()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  if (chart) {
    chart.dispose()
    chart = null
  }
})

watch(() => props.data, render, { deep: true })
</script>

<style scoped>
.chart {
  width: 100%;
  height: 170px;
}
</style>
