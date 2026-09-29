<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">分类管理</h2>
      <el-button type="primary" @click="openEdit()">新增分类</el-button>
    </div>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="sort" label="排序" width="100" />
      <el-table-column prop="createTime" label="创建时间" min-width="160" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id ? '编辑分类' : '新增分类'" width="420px">
      <el-form label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAdminCategoryApi,
  deleteAdminCategoryApi,
  getAdminCategoriesApi,
  updateAdminCategoryApi,
} from '@/api/admin'
import type { Category } from '@/utils/types'

const loading = ref(false)
const saving = ref(false)
const list = ref<Category[]>([])
const visible = ref(false)
const form = reactive<{ id?: number | string; name: string; sort: number }>({ name: '', sort: 0 })

async function load() {
  loading.value = true
  try {
    const data = await getAdminCategoriesApi()
    list.value = Array.isArray(data) ? data : []
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

function openEdit(row?: Category) {
  form.id = row?.id
  form.name = row?.name || ''
  form.sort = Number(row?.sort ?? 0)
  visible.value = true
}

async function save() {
  if (!form.name.trim()) return ElMessage.warning('请输入名称')
  saving.value = true
  try {
    if (form.id) await updateAdminCategoryApi(form.id, { name: form.name, sort: form.sort })
    else await createAdminCategoryApi({ name: form.name, sort: form.sort })
    ElMessage.success('保存成功')
    visible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row: Category) {
  await ElMessageBox.confirm(`确认删除分类「${row.name}」？`, '提示', { type: 'warning' })
  await deleteAdminCategoryApi(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
