<template>
  <div ref="chartRef" class="chart"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: { type: Array, default: () => [] } // [{ date: 'yyyy-MM-dd', pct: n }]
})

const chartRef = ref()
let chart = null

function render() {
  if (!chart) return
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const p = params[0]
        return `${p.axisValue}<br/>正确率：${p.value}%`
      }
    },
    grid: { left: 40, right: 16, top: 20, bottom: 26 },
    xAxis: {
      type: 'category',
      data: props.data.map((d) => d.date),
      axisLabel: { fontSize: 10, rotate: 30 }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLabel: { formatter: '{value}%', fontSize: 10 }
    },
    series: [
      {
        type: 'line',
        data: props.data.map((d) => d.pct),
        smooth: true,
        areaStyle: { opacity: 0.15 },
        lineStyle: { color: '#2c5aa0' },
        itemStyle: { color: '#2c5aa0' }
      }
    ]
  })
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
  height: 200px;
}
</style>
