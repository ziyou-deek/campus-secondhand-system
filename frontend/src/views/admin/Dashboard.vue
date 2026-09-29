<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">数据统计</h2>
      <el-button @click="load" :loading="loading">刷新</el-button>
    </div>
    <el-row :gutter="16" class="cards">
      <el-col :span="6" v-for="c in cards" :key="c.label">
        <el-card shadow="hover"><div class="num">{{ c.value }}</div><div class="label">{{ c.label }}</div></el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card><div ref="catRef" style="height: 320px"></div></el-card>
      </el-col>
      <el-col :span="12">
        <el-card><div ref="orderRef" style="height: 320px"></div></el-card>
      </el-col>
    </el-row>
    <el-card style="margin-top: 16px"><div ref="trendRef" style="height: 320px"></div></el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onBeforeUnmount, ref } from 'vue'
import * as echarts from 'echarts'
import { getAdminStatsApi } from '@/api/admin'
import type { StatsOverview } from '@/utils/types'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const stats = ref<StatsOverview>({})
const catRef = ref<HTMLDivElement>()
const orderRef = ref<HTMLDivElement>()
const trendRef = ref<HTMLDivElement>()
let charts: echarts.ECharts[] = []

const cards = computed(() => [
  { label: '用户数', value: stats.value.userCount ?? 0 },
  { label: '商品数', value: stats.value.productCount ?? 0 },
  { label: '订单数', value: stats.value.orderCount ?? 0 },
  { label: '分类类目', value: stats.value.categoryStats?.length ?? 0 },
])

function dispose() {
  charts.forEach((c) => c.dispose())
  charts = []
}

function render() {
  dispose()
  if (catRef.value) {
    const c = echarts.init(catRef.value)
    c.setOption({
      title: { text: '商品分类分布' },
      tooltip: { trigger: 'item' },
      series: [{ type: 'pie', radius: '60%', data: (stats.value as any).categoryStats || (stats.value as any).categoryDist || [] }],
    })
    charts.push(c)
  }
  if (orderRef.value) {
    const c = echarts.init(orderRef.value)
    const src = (stats.value as any).orderStatusStats || (stats.value as any).productStatusStats || []
    c.setOption({
      title: { text: '订单状态分布' },
      tooltip: { trigger: 'item' },
      series: [{ type: 'pie', radius: ['35%', '60%'], data: src }],
    })
    charts.push(c)
  }
  if (trendRef.value) {
    const c = echarts.init(trendRef.value)
    const trend = (stats.value as any).orderTrend || (stats.value as any).orderStatusStats || []
    const isDist = trend.length && trend[0].name !== undefined
    c.setOption({
      title: { text: isDist ? '订单状态' : '订单趋势' },
      tooltip: { trigger: isDist ? 'item' : 'axis' },
      ...(isDist
        ? { series: [{ type: 'pie', radius: '55%', data: trend }] }
        : {
            xAxis: { type: 'category', data: trend.map((i: any) => i.date) },
            yAxis: { type: 'value', minInterval: 1 },
            series: [{ type: 'line', smooth: true, data: trend.map((i: any) => i.count), areaStyle: {} }],
          }),
    })
    charts.push(c)
  }
}

async function load() {
  loading.value = true
  try {
    stats.value = (await getAdminStatsApi()) || {}
    await nextTick()
    render()
  } catch {
    ElMessage.error('统计数据加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
onBeforeUnmount(dispose)
</script>

<style scoped>
.cards .num { font-size: 28px; font-weight: 700; color: #409eff; }
.cards .label { color: #909399; margin-top: 6px; }
</style>
