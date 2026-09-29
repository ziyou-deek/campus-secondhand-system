<template>
  <div v-loading="loading" class="page-card">
    <template v-if="product">
      <el-row :gutter="24">
        <el-col :xs="24" :md="10">
          <div class="gallery">
            <img :src="cover" :alt="product.title" />
          </div>
          <div v-if="images.length > 1" class="gallery-thumbs">
            <img v-for="url in images" :key="url" :src="url" :alt="product.title" @click="selectedImage = url" />
          </div>
        </el-col>
        <el-col :xs="24" :md="14">
          <h2 class="title">{{ product.title }}</h2>
          <div class="price-row">
            <span class="price">{{ formatPrice(product.price) }}</span>
            <span v-if="product.originalPrice" class="origin">
              原价 {{ formatPrice(product.originalPrice) }}
            </span>
          </div>
          <el-descriptions :column="1" border size="small" style="margin-top: 16px">
            <el-descriptions-item label="分类">
              {{ product.categoryName || '—' }}
            </el-descriptions-item>
            <el-descriptions-item label="成色">
              {{ product.condition || '—' }}
            </el-descriptions-item>
            <el-descriptions-item label="卖家">
              {{ product.sellerName || '—' }}
            </el-descriptions-item>
            <el-descriptions-item label="地点">
              {{ product.location || '—' }}
            </el-descriptions-item>
            <el-descriptions-item label="发布时间">
              {{ formatDate(product.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              {{ productStatusMap[product.status as any] || product.status || '—' }}
            </el-descriptions-item>
          </el-descriptions>
          <div class="actions">
            <el-button v-if="isOwner && ![3, 5].includes(Number(product.status))" @click="router.push(`/products/${product.id}/edit`)">编辑商品</el-button>
            <el-button v-if="!isOwner && Number(product.status) === 1" type="primary" :disabled="!userStore.isLoggedIn" @click="onOrder">
              立即下单
            </el-button>
            <el-button v-if="!isOwner && Number(product.status) === 1"
              :type="favorited ? 'warning' : 'default'"
              :disabled="!userStore.isLoggedIn"
              @click="toggleFavorite"
            >
              {{ favorited ? '已收藏' : '收藏' }}
            </el-button>
            <el-button v-if="!isOwner" :disabled="!userStore.isLoggedIn" @click="onChat">私信卖家</el-button>
          </div>
          <p v-if="!userStore.isLoggedIn" class="login-tip">
            登录后可下单、收藏与私信
            <router-link to="/login">去登录</router-link>
          </p>
        </el-col>
      </el-row>

      <el-divider />
      <h3>商品描述</h3>
      <p class="desc">{{ product.description || '暂无描述' }}</p>

      <el-divider />
      <div class="comment-header">
        <h3>评价</h3>
        <span class="review-tip">请在已完成订单详情中评价</span>
      </div>
      <div v-loading="commentLoading">
        <div v-if="comments.length" class="comments">
          <div v-for="c in comments" :key="c.id" class="comment-item">
            <div class="c-head">
              <strong>{{ c.userName || '匿名用户' }}</strong>
              <el-rate v-if="c.rating" :model-value="c.rating" disabled size="small" />
              <span class="time">{{ formatDate(c.createTime) }}</span>
            </div>
            <p>{{ c.content }}</p>
          </div>
        </div>
        <el-empty v-else description="暂无评价" :image-size="64" />
      </div>
    </template>
    <el-empty v-else-if="!loading" description="商品不存在或加载失败" />

  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductDetailApi } from '@/api/product'
import { addFavoriteApi, checkFavoriteApi, removeFavoriteApi } from '@/api/favorite'
import { createOrderApi } from '@/api/order'
import { getCommentsApi } from '@/api/comment'
import { startConversationApi } from '@/api/message'
import { useUserStore } from '@/stores/user'
import { normalizePage, productCover, productImages } from '@/utils/page'
import { formatDate, formatPrice, productStatusMap } from '@/utils/format'
import type { Comment, Product } from '@/utils/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const product = ref<Product | null>(null)
const favorited = ref(false)
const comments = ref<Comment[]>([])
const commentLoading = ref(false)
const selectedImage = ref('')

const id = computed(() => route.params.id as string)
const images = computed(() => (product.value ? productImages(product.value) : []))
const cover = computed(() => selectedImage.value || (product.value ? productCover(product.value) : ''))
const isOwner = computed(() => !!product.value && String(product.value.sellerId) === String(userStore.userInfo?.id))

async function loadDetail() {
  loading.value = true
  try {
    product.value = await getProductDetailApi(id.value)
  } catch {
    product.value = null
    ElMessage.error('商品详情加载失败')
  } finally {
    loading.value = false
  }
}

async function loadFavorite() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await checkFavoriteApi(id.value)
    favorited.value = !!(res && (res as any).favorited)
  } catch {
    // fallback: product detail may already include favorited
    favorited.value = !!(product.value as any)?.favorited
  }
}

async function loadComments() {
  commentLoading.value = true
  try {
    const data = await getCommentsApi({ productId: id.value })
    comments.value = normalizePage(data).list
  } catch {
    comments.value = []
  } finally {
    commentLoading.value = false
  }
}

async function toggleFavorite() {
  try {
    if (favorited.value) {
      await removeFavoriteApi(id.value)
      favorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavoriteApi(id.value)
      favorited.value = true
      ElMessage.success('收藏成功')
    }
  } catch {
    ElMessage.warning('收藏操作失败，请稍后重试')
  }
}

async function onOrder() {
  try {
    await ElMessageBox.confirm('确认下单购买该商品？', '下单确认', { type: 'info' })
    const order = await createOrderApi({ productId: id.value })
    ElMessage.success('下单成功')
    router.push(`/orders/${order.id || ''}`)
  } catch (e: any) {
    if (e === 'cancel' || e?.toString?.().includes('cancel')) return
    ElMessage.warning(e?.message || '下单失败，请确认后端服务')
  }
}

async function onChat() {
  const sellerId = product.value?.sellerId ?? (product.value as any)?.userId
  if (!sellerId) {
    ElMessage.warning('无法获取卖家信息')
    return
  }
  const peerName = product.value?.sellerName || ''
  try {
    const conv = await startConversationApi(sellerId)
    const conversationId = conv?.id ?? sellerId
    const peer = conv?.peerId ?? sellerId
    router.push({
      path: '/messages',
      query: {
        conversationId: String(conversationId),
        peerId: String(peer),
        peerName,
      },
    })
  } catch {
    router.push({
      path: '/messages',
      query: { peerId: String(sellerId), peerName },
    })
  }
}


onMounted(() => {
  loadDetail()
  loadFavorite()
  loadComments()
})
</script>

<style scoped>
.gallery {
  background: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
  min-height: 260px;
}
.gallery img {
  width: 100%;
  display: block;
  object-fit: cover;
  max-height: 360px;
}
.gallery-thumbs { display: flex; gap: 8px; margin-top: 8px; flex-wrap: wrap; }
.gallery-thumbs img { width: 64px; height: 64px; object-fit: cover; border-radius: 4px; cursor: pointer; }
.title {
  margin: 0 0 8px;
}
.price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.price {
  font-size: 28px;
}
.origin {
  color: #909399;
  text-decoration: line-through;
}
.actions {
  margin-top: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.login-tip {
  color: #909399;
  font-size: 13px;
}
.login-tip a {
  color: #409eff;
}
.desc {
  white-space: pre-wrap;
  color: #606266;
  line-height: 1.7;
}
.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.review-tip {
  color: #909399;
  font-size: 13px;
}
.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}
.c-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}
.time {
  color: #909399;
  font-size: 12px;
  margin-left: auto;
}
</style>
