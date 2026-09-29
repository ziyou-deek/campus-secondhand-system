<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">我的收藏</h2>
    </div>
    <div v-loading="loading">
      <el-table v-if="list.length" :data="list" stripe>
        <el-table-column label="商品" min-width="220">
          <template #default="{ row }">
            <div class="row-item" @click="goProduct(row)">
              <img :src="coverOf(row)" class="thumb" />
              <span>{{ productOf(row)?.title || '—' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="120">
          <template #default="{ row }">
            <span class="price">{{ formatPrice(productOf(row)?.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            {{ productOf(row)?.categoryName || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goProduct(row)">查看</el-button>
            <el-button link type="danger" @click="remove(row)">取消收藏</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无收藏" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFavoritesApi, removeFavoriteApi, type FavoriteItem } from '@/api/favorite'
import { normalizePage, productCover } from '@/utils/page'
import { formatPrice } from '@/utils/format'
import type { Product } from '@/utils/types'

const router = useRouter()
const loading = ref(false)
const list = ref<FavoriteItem[]>([])

function productOf(row: FavoriteItem): Product | undefined {
  return row.product
}

function productIdOf(row: FavoriteItem): number | string | undefined {
  return row.productId ?? row.product?.id
}

function coverOf(row: FavoriteItem) {
  const p = productOf(row)
  return p ? productCover(p) : ''
}

function goProduct(row: FavoriteItem) {
  const pid = productIdOf(row)
  if (pid == null) return
  router.push(`/products/${pid}`)
}

async function load() {
  loading.value = true
  try {
    const data = await getFavoritesApi()
    list.value = normalizePage(data).list
  } catch {
    list.value = []
    ElMessage.warning('收藏列表加载失败')
  } finally {
    loading.value = false
  }
}

async function remove(row: FavoriteItem) {
  const pid = productIdOf(row)
  if (pid == null) {
    ElMessage.error('无法获取商品ID')
    return
  }
  await ElMessageBox.confirm('确定取消收藏？', '提示', { type: 'warning' })
  try {
    await removeFavoriteApi(pid)
    ElMessage.success('已取消')
    load()
  } catch {
    ElMessage.error('操作失败')
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
