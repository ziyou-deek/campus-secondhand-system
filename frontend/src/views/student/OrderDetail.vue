<template>
  <div v-loading="loading" class="page-card">
    <div class="page-header">
      <h2 class="page-title">订单详情</h2>
      <el-button @click="$router.push('/orders')">返回列表</el-button>
    </div>
    <template v-if="order">
      <img v-if="order.productCover" class="product-image" :src="order.productCover" :alt="order.productTitle" />
      <el-descriptions title="基本信息" :column="2" border>
        <el-descriptions-item label="订单号">{{ order.orderNo || order.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          {{ orderStatusMap[order.status as any] || order.status }}
        </el-descriptions-item>
        <el-descriptions-item label="商品">{{ order.productTitle || order.productId }}</el-descriptions-item>
        <el-descriptions-item label="金额">
          <span class="price">{{ formatPrice(order.price) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="买家">{{ order.buyerName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="卖家">{{ order.sellerName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ formatDate(order.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ order.remark || '—' }}</el-descriptions-item>
        <el-descriptions-item label="双方确认">
          买家：{{ order.buyerConfirmed ? '已确认' : '未确认' }}；卖家：{{ order.sellerConfirmed ? '已确认' : '未确认' }}
        </el-descriptions-item>
      </el-descriptions>
      <div class="actions">
        <el-button
          v-if="canCancel"
          type="danger"
          plain
          :loading="acting"
          @click="cancel"
        >
          取消订单
        </el-button>
        <el-button
          v-if="canStartTrade"
          type="warning"
          :loading="acting"
          @click="startTrade"
        >
          开始交易
        </el-button>
        <el-button
          v-if="canComplete"
          type="primary"
          :loading="acting"
          @click="complete"
        >
          我已确认完成
        </el-button>
        <el-button v-if="canComment" type="success" plain @click="openComment">评价</el-button>
      </div>
    </template>
    <el-empty v-else-if="!loading" description="订单不存在" />

    <el-dialog v-model="showComment" title="订单评价" width="420px">
      <el-form label-position="top">
        <el-form-item label="评分">
          <el-rate v-model="commentForm.rating" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="commentForm.content" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showComment = false">取消</el-button>
        <el-button type="primary" :loading="acting" @click="submitComment">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelOrderApi, getOrderDetailApi, updateOrderStatusApi } from '@/api/order'
import { createCommentApi } from '@/api/comment'
import { formatDate, formatPrice, orderStatusMap } from '@/utils/format'
import type { Order } from '@/utils/types'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const acting = ref(false)
const order = ref<Order | null>(null)
const showComment = ref(false)
const commentForm = reactive({ content: '', rating: 5 })

const canCancel = computed(() => {
  const s = order.value?.status
  return s === 0 || s === 1 || s === 'PENDING' || s === 'TRADING'
})
const canStartTrade = computed(() => {
  const s = order.value?.status
  return (s === 0 || s === 'PENDING') && String(order.value?.sellerId) === String(userStore.userInfo?.id)
})
const canComplete = computed(() => {
  const s = order.value?.status
  if (s !== 1 && s !== 'TRADING') return false
  const uid = String(userStore.userInfo?.id)
  if (uid === String(order.value?.buyerId)) return !order.value?.buyerConfirmed
  if (uid === String(order.value?.sellerId)) return !order.value?.sellerConfirmed
  return false
})
const canComment = computed(() => {
  const s = order.value?.status
  return s === 2 || s === 'COMPLETED'
})

async function load() {
  loading.value = true
  try {
    order.value = await getOrderDetailApi(route.params.id as string)
  } catch {
    order.value = null
    ElMessage.error('订单详情加载失败')
  } finally {
    loading.value = false
  }
}

async function cancel() {
  await ElMessageBox.confirm('确定取消该订单？', '提示', { type: 'warning' })
  acting.value = true
  try {
    await cancelOrderApi(order.value!.id)
    ElMessage.success('已取消')
    load()
  } catch {
    ElMessage.error('取消失败')
  } finally {
    acting.value = false
  }
}

async function startTrade() {
  acting.value = true
  try {
    await updateOrderStatusApi(order.value!.id, 1)
    ElMessage.success('已开始交易')
    load()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    acting.value = false
  }
}

async function complete() {
  await ElMessageBox.confirm('确认交易完成？', '提示', { type: 'info' })
  acting.value = true
  try {
    await updateOrderStatusApi(order.value!.id, 2)
    ElMessage.success('已确认；双方都确认后订单完成')
    load()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    acting.value = false
  }
}

function openComment() {
  showComment.value = true
}

async function submitComment() {
  if (!commentForm.content.trim()) {
    ElMessage.warning('请输入评价')
    return
  }
  acting.value = true
  try {
    await createCommentApi({
      orderId: order.value!.id,
      productId: order.value!.productId,
      content: commentForm.content,
      rating: commentForm.rating,
    })
    ElMessage.success('评价成功')
    showComment.value = false
  } catch {
    ElMessage.warning('评价失败')
  } finally {
    acting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.product-image { width: 120px; height: 120px; object-fit: cover; border-radius: 8px; margin-bottom: 16px; }
.actions {
  margin-top: 20px;
  display: flex;
  gap: 8px;
}
</style>
