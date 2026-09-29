<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">商品审核</h2>
      <el-select v-model="status" clearable placeholder="状态" style="width: 140px" @change="load">
        <el-option label="待审核" :value="0" />
        <el-option label="在售" :value="1" />
        <el-option label="已下架" :value="2" />
        <el-option label="已售出" :value="3" />
        <el-option label="已拒绝" :value="4" />
      </el-select>
      <el-input v-model="keyword" placeholder="关键词" clearable style="width: 200px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
    </div>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="sellerNickname" label="卖家" width="120" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ productStatusMap[row.status as any] || row.status }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" min-width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button link type="success" @click="audit(row, 1)">通过</el-button>
            <el-button link type="danger" @click="audit(row, 4)">拒绝</el-button>
          </template>
          <el-button link type="primary" @click="$router.push(`/products/${row.id}`)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      style="margin-top: 16px; justify-content: flex-end"
      background
      layout="total, prev, pager, next"
      :total="total"
      v-model:current-page="page"
      :page-size="size"
      @current-change="load"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditProductApi, getAdminProductsApi } from '@/api/admin'
import { normalizePage } from '@/utils/page'
import { productStatusMap } from '@/utils/format'
import type { Product } from '@/utils/types'

const loading = ref(false)
const list = ref<Product[]>([])
const keyword = ref('')
const status = ref<number | undefined>(0)
const page = ref(1)
const size = 10
const total = ref(0)

async function load() {
  loading.value = true
  try {
    const data = await getAdminProductsApi({
      page: page.value,
      size,
      keyword: keyword.value || undefined,
      status: status.value,
    })
    const p = normalizePage(data as any)
    list.value = p.list as any
    total.value = p.total
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function audit(row: Product, st: number) {
  let reason = ''
  if (st === 4) {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '审核拒绝', { inputPlaceholder: '原因' })
    reason = value || ''
  } else {
    await ElMessageBox.confirm('确认审核通过？', '提示')
  }
  await auditProductApi(row.id, { status: st, reason })
  ElMessage.success('已处理')
  load()
}

onMounted(load)
</script>
