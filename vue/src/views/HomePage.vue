<template>
  <div class="home-page">
    <section class="hero">
      <p class="hero-eyebrow">E-BOOK READING PLATFORM</p>
      <h1 class="hero-title display">在数字世界里，<br />读一本好书。</h1>
      <p class="hero-sub">从左侧分类挑选感兴趣的电子书，点击即可在线阅读；下方是平台阅读数据一览。</p>
    </section>

    <section v-if="stat" class="stats-grid">
      <article class="stat-card stat-primary">
        <span class="stat-label">总阅读量</span>
        <span class="stat-value display">{{ stat.totalViewCount }}</span>
        <span class="stat-foot">累计阅读次数</span>
      </article>
      <article class="stat-card stat-primary">
        <span class="stat-label">总点赞量</span>
        <span class="stat-value display">{{ stat.totalVoteCount }}</span>
        <span class="stat-foot">累计点赞次数</span>
      </article>
      <article class="stat-card stat-primary">
        <span class="stat-label">点赞率</span>
        <span class="stat-value display">{{ stat.voteRate }}<small>%</small></span>
        <span class="stat-foot">点赞 / 阅读</span>
      </article>
      <article class="stat-card">
        <span class="stat-label">今日阅读</span>
        <span class="stat-value display">{{ stat.todayViewCount }}</span>
      </article>
      <article class="stat-card">
        <span class="stat-label">今日点赞</span>
        <span class="stat-value display">{{ stat.todayVoteCount }}</span>
      </article>
      <article class="stat-card">
        <span class="stat-label">预计今日阅读</span>
        <span class="stat-value display">{{ stat.estimatedTodayView }}</span>
        <span class="stat-foot">按当前进度推算</span>
      </article>
      <article class="stat-card">
        <span class="stat-label">预计阅读增长率</span>
        <span class="stat-value display growth" :class="stat.estimatedGrowth >= 0 ? 'up' : 'down'">
          {{ stat.estimatedGrowth >= 0 ? '▲' : '▼' }} {{ Math.abs(stat.estimatedGrowth) }}<small>%</small>
        </span>
        <span class="stat-foot">对比昨日</span>
      </article>
    </section>

    <section class="panel chart-panel">
      <div class="chart-head">
        <h3 class="display">近 30 天趋势</h3>
        <p>阅读增量与点赞增量 · 数据来自每日快照</p>
      </div>
      <div ref="chartRef" class="chart"></div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { get30Statistic, getStatistic, type StatisticResp } from '../api/stat'

const stat = ref<StatisticResp | null>(null)
const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

onMounted(async () => {
  try {
    const [statData, days] = await Promise.all([getStatistic(), get30Statistic()])
    stat.value = statData
    renderChart(days)
  } catch {
    // 加载失败时保持空态
  }
})

onBeforeUnmount(() => {
  chart?.dispose()
})

function renderChart(days: { date: string; viewIncrease: number; voteIncrease: number }[]) {
  if (!chartRef.value) return
  chart = echarts.init(chartRef.value)
  chart.setOption({
    color: ['#b03a2e', '#2f6b5f'],
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#2d2a24',
      borderWidth: 0,
      textStyle: { color: '#f6f1e5' }
    },
    toolbox: {
      right: 20,
      itemSize: 16,
      iconStyle: { borderColor: '#6f675a' },
      emphasis: { iconStyle: { borderColor: '#b03a2e' } },
      feature: {
        saveAsImage: { title: '保存为图片', name: '近30天趋势' }
      }
    },
    legend: {
      data: ['阅读增量', '点赞增量'],
      textStyle: { color: '#6f675a' },
      top: 0
    },
    grid: { left: 40, right: 20, top: 40, bottom: 30 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: days.map((d) => d.date.slice(5)),
      axisLine: { lineStyle: { color: '#e4dcc9' } },
      axisLabel: { color: '#6f675a' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#efe8d8' } },
      axisLabel: { color: '#6f675a' }
    },
    series: [
      {
        name: '阅读增量',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        data: days.map((d) => d.viewIncrease),
        areaStyle: { opacity: 0.08 }
      },
      {
        name: '点赞增量',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        data: days.map((d) => d.voteIncrease)
      }
    ]
  })
}
</script>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.hero {
  padding: 28px 8px 8px;
  animation: page-fade 0.6s ease both;
}

.hero-eyebrow {
  margin: 0 0 10px;
  font-size: 12px;
  letter-spacing: 0.28em;
  color: var(--accent);
  font-weight: 600;
}

.hero-title {
  margin: 0;
  font-size: 42px;
  line-height: 1.25;
  font-weight: 700;
  color: var(--ink);
}

.hero-sub {
  margin: 14px 0 0;
  color: var(--ink-soft);
  font-size: 15px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.stat-card {
  background: var(--paper-light);
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 18px 20px;
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
  gap: 6px;
  animation: page-fade 0.5s ease both;
}

.stat-card:nth-child(1) { animation-delay: 0.05s; }
.stat-card:nth-child(2) { animation-delay: 0.1s; }
.stat-card:nth-child(3) { animation-delay: 0.15s; }
.stat-card:nth-child(4) { animation-delay: 0.2s; }
.stat-card:nth-child(5) { animation-delay: 0.25s; }
.stat-card:nth-child(6) { animation-delay: 0.3s; }
.stat-card:nth-child(7) { animation-delay: 0.35s; }

.stat-primary {
  background:
    linear-gradient(160deg, rgba(176, 58, 46, 0.06), transparent 55%),
    var(--paper-light);
}

.stat-label {
  font-size: 13px;
  color: var(--ink-soft);
  letter-spacing: 0.05em;
}

.stat-value {
  font-size: 34px;
  font-weight: 700;
  color: var(--ink);
  line-height: 1.1;
}

.stat-value small {
  font-size: 18px;
}

.stat-value.growth {
  font-size: 26px;
}

.stat-value.growth.up { color: var(--accent); }
.stat-value.growth.down { color: var(--teal); }

.stat-foot {
  font-size: 12px;
  color: #9a9182;
}

.chart-panel {
  animation: page-fade 0.6s ease 0.2s both;
}

.chart-head h3 {
  margin: 0 0 4px;
  font-size: 20px;
}

.chart-head p {
  margin: 0;
  color: var(--ink-soft);
  font-size: 13px;
}

.chart {
  height: 320px;
  margin-top: 8px;
}

@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .hero-title {
    font-size: 32px;
  }
}
</style>
