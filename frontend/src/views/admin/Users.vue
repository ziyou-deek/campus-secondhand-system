<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
      <el-input v-model="keyword" placeholder="搜索用户名/昵称" clearable style="width: 240px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
    </div>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="nickname" label="昵称" width="140" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="role" label="角色" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" min-width="160" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.role !== 'ADMIN'"
            link
            :type="row.status === 1 ? 'danger' : 'success'"
            @click="toggle(row)"
          >{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
          <el-button v-if="row.role !== 'ADMIN'" link type="warning" :loading="resettingId === row.id" @click="resetPassword(row)">重置密码</el-button>
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
    <el-dialog v-model="resetDialogVisible" title="临时密码" width="440px" :close-on-click-modal="false" @closed="temporaryPassword = ''">
      <el-alert type="warning" :closable="false" title="仅显示这一次，请先复制并私下告知用户。用户登录后应立即在个人中心修改密码。" />
      <p>用户名：{{ resetUsername }}</p>
      <el-input v-model="temporaryPassword" readonly>
        <template #append><el-button @click="copyPassword">复制</el-button></template>
      </el-input>
      <template #footer><el-button type="primary" @click="resetDialogVisible = false">已记录，关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminUsersApi, updateUserStatusApi, resetUserPasswordApi } from '@/api/admin'
import { normalizePage } from '@/utils/page'
import type { UserInfo } from '@/utils/types'

const loading = ref(false)
const list = ref<UserInfo[]>([])
const keyword = ref('')
const page = ref(1)
const size = 10
const total = ref(0)
const resettingId = ref<number | string | null>(null)
const resetDialogVisible = ref(false)
const resetUsername = ref('')
const temporaryPassword = ref('')

async function load() {
  loading.value = true
  try {
    const data = await getAdminUsersApi({ page: page.value, size, keyword: keyword.value || undefined })
    const p = normalizePage(data as any)
    list.value = p.list as any
    total.value = p.total
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function toggle(row: UserInfo) {
  const next = row.status === 1 ? 0 : 1
  await ElMessageBox.confirm(`确认${next === 0 ? '禁用' : '启用'}用户 ${row.username}？`, '提示')
  await updateUserStatusApi(row.id, next)
  ElMessage.success('已更新')
  load()
}

async function resetPassword(row: UserInfo) {
  try {
    await ElMessageBox.confirm(`确定重置用户 ${row.username} 的密码？该用户现有登录状态将失效。`, '重置密码', { type: 'warning', confirmButtonText: '确认重置' })
  } catch { return }
  resettingId.value = row.id
  try {
    const result = await resetUserPasswordApi(row.id)
    resetUsername.value = result.username
    temporaryPassword.value = result.temporaryPassword
    resetDialogVisible.value = true
  } catch { ElMessage.error('重置失败，请稍后重试') }
  finally { resettingId.value = null }
}

async function copyPassword() {
  try {
    await navigator.clipboard.writeText(temporaryPassword.value)
    ElMessage.success('临时密码已复制')
  } catch { ElMessage.warning('复制失败，请手动选中密码复制') }
}

onMounted(load)
</script>
