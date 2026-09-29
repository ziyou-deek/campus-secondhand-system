<template>
  <div>
    <div class="page-card search-bar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索商品标题 / 描述"
        clearable
        style="max-width: 360px"
        @keyup.enter="search"
      >
        <template #append>
          <el-button :icon="Search" @click="search" />
        </template>
      </el-input>
      <el-select
        v-model="query.categoryId"
        clearable
        placeholder="全部分类"
        style="width: 180px"
        @change="search"
      >
        <el-option
          v-for="c in categories"
          :key="c.id"
          :label="c.name"
          :value="c.id"
        />
      </el-select>
      <el-button type="primary" @click="search">搜索</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <div v-loading="loading" class="list-wrap">
      <el-row v-if="list.length" :gutter="16">
        <el-col
          v-for="item in list"
          :key="item.id"
          :xs="12"
          :sm="8"
          :md="6"
          style="margin-bottom: 16px"
        >
          <el-card
            shadow="hover"
            class="product-card"
            :body-style="{ padding: '12px' }"
            @click="$router.push(`/products/${item.id}`)"
          >
            <div class="cover">
              <img :src="productCover(item)" :alt="item.title" />
            </div>
            <div class="title" :title="item.title">{{ item.title }}</div>
            <div class="meta">
              <span class="price">{{ formatPrice(item.price) }}</span>
              <el-tag size="small" type="info">{{ item.categoryName || '未分类' }}</el-tag>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-else description="暂无商品，试试调整筛选或稍后刷新" />
    </div>

    <div class="pager" v-if="total > 0">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="query.size"
        :current-page="query.page"
        @current-change="onPage"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getProductsApi } from '@/api/product'
import { getCategoriesApi } from '@/api/category'
import { normalizePage, productCover } from '@/utils/page'
import { formatPrice } from '@/utils/format'
import type { Category, Product } from '@/utils/types'

const loading = ref(false)
const list = ref<Product[]>([])
const total = ref(0)
const categories = ref<Category[]>([])

const query = reactive({
  keyword: '',
  categoryId: undefined as string | number | undefined,
  page: 1,
  size: 12,
})

async function loadCategories() {
  try {
    const data = await getCategoriesApi()
    categories.value = Array.isArray(data) ? data : []
  } catch {
    categories.value = [
      { id: 1, name: '数码电子' },
      { id: 2, name: '图书教材' },
      { id: 3, name: '生活用品' },
      { id: 4, name: '服饰鞋包' },
      { id: 5, name: '其他' },
    ]
  }
}

async function loadList() {
  loading.value = true
  try {
    const data = await getProductsApi({ ...query })
    const page = normalizePage(data)
    list.value = page.list
    total.value = page.total
  } catch {
    list.value = []
    total.value = 0
    ElMessage.warning('商品列表加载失败，请确认后端是否已启动')
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  loadList()
}

function reset() {
  query.keyword = ''
  query.categoryId = undefined
  query.page = 1
  loadList()
}

function onPage(p: number) {
  query.page = p
  loadList()
}

onMounted(() => {
  loadCategories()
  loadList()
})
</script>

<style scoped>
.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}
.product-card {
  cursor: pointer;
  height: 100%;
}
.cover {
  height: 140px;
  overflow: hidden;
  border-radius: 4px;
  background: #f2f3f5;
}
.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.title {
  margin-top: 8px;
  font-size: 14px;
  height: 40px;
  line-height: 20px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.meta {
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
