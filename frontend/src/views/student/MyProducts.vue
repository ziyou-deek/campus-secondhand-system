<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">我的商品</h2>
      <el-button type="primary" @click="$router.push('/publish')">发布新商品</el-button>
    </div>
    <div v-loading="loading">
      <el-table v-if="list.length" :data="list" stripe>
        <el-table-column label="商品" min-width="220">
          <template #default="{ row }">
            <div class="row-item" @click="$router.push(`/products/${row.id}`)">
              <img :src="productCover(row)" class="thumb" />
              <span>{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="110">
          <template #default="{ row }">
            <span class="price">{{ formatPrice(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            {{ productStatusMap[row.status as any] || row.status || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/products/${row.id}`)">查看</el-button>
            <el-button v-if="![3, 5].includes(Number(row.status))" link type="primary" @click="$router.push(`/products/${row.id}/edit`)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="还没有发布商品" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteProductApi, getMyProductsApi } from '@/api/product'
import { normalizePage, productCover } from '@/utils/page'
import { formatDate, formatPrice, productStatusMap } from '@/utils/format'
import type { Product } from '@/utils/types'

const loading = ref(false)
const list = ref<Product[]>([])

async function load() {
  loading.value = true
  try {
    const data = await getMyProductsApi()
    list.value = normalizePage(data).list
  } catch {
    list.value = []
    ElMessage.warning('我的商品加载失败')
  } finally {
    loading.value = false
  }
}

async function remove(row: Product) {
  await ElMessageBox.confirm('确定删除该商品？', '提示', { type: 'warning' })
  try {
    await deleteProductApi(row.id)
    ElMessage.success('已删除')
    load()
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(load)
</script>

<style scoped>
.row-item {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}
.thumb {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 4px;
  background: #f2f3f5;
}
</style>
