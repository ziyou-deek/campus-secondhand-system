<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">我的订单</h2>
      <el-radio-group v-model="status" @change="load">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button :label="0">待交易</el-radio-button>
        <el-radio-button :label="1">交易中</el-radio-button>
        <el-radio-button :label="2">已完成</el-radio-button>
        <el-radio-button :label="3">已取消</el-radio-button>
      </el-radio-group>
    </div>
    <div v-loading="loading">
      <el-table v-if="list.length" :data="list" stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="140">
          <template #default="{ row }">{{ row.orderNo || row.id }}</template>
        </el-table-column>
        <el-table-column label="商品" min-width="180">
          <template #default="{ row }"><div class="product-cell"><img v-if="row.productCover" :src="row.productCover" :alt="row.productTitle" /><span>{{ row.productTitle || row.productId }}</span></div></template>
        </el-table-column>
        <el-table-column label="金额" width="100">
          <template #default="{ row }">
            <span class="price">{{ formatPrice(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            {{ orderStatusMap[row.status as any] || row.status }}
          </template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/orders/${row.id}`)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无订单" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getOrdersApi } from '@/api/order'
import { normalizePage } from '@/utils/page'
import { formatDate, formatPrice, orderStatusMap } from '@/utils/format'
import type { Order } from '@/utils/types'

const loading = ref(false)
const list = ref<Order[]>([])
const status = ref<number | string>('')

async function load() {
  loading.value = true
  try {
    const data = await getOrdersApi({
      status: status.value === '' ? undefined : status.value,
    })
    list.value = normalizePage(data).list
  } catch {
    list.value = []
    ElMessage.warning('订单列表加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.product-cell { display: flex; align-items: center; gap: 10px; }
.product-cell img { width: 44px; height: 44px; object-fit: cover; border-radius: 5px; }
</style>
