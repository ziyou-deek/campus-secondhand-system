<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">违规处理</h2>
      <el-alert type="info" :closable="false" title="展示审核拒绝/可下架商品，管理员可进一步下架处理。" />
    </div>
    <el-table v-loading="loading" :data="list" stripe style="margin-top: 12px">
      <el-table-column prop="id" label="商品ID" width="90" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="sellerNickname" label="发布者" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ productStatusMap[row.status as any] || row.status }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" min-width="160" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="danger" @click="offline(row)">强制下架</el-button>
          <el-button link type="primary" @click="$router.push(`/products/${row.id}`)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getViolationsApi, handleViolationApi } from '@/api/admin'
import { normalizePage } from '@/utils/page'
import { productStatusMap } from '@/utils/format'
import type { Product } from '@/utils/types'

const loading = ref(false)
const list = ref<Product[]>([])

async function load() {
  loading.value = true
  try {
    const data = await getViolationsApi({ status: 4, page: 1, size: 50 })
    list.value = normalizePage(data as any).list as any
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function offline(row: Product) {
  await ElMessageBox.confirm('确认强制下架该商品？', '提示', { type: 'warning' })
  await handleViolationApi(row.id, { status: 2, handleResult: '违规下架' })
  ElMessage.success('已下架')
  load()
}

onMounted(load)
</script>
