<template>
  <div class="page-card" style="max-width: 720px; margin: 0 auto" v-loading="initializing">
    <div class="page-header"><h2 class="page-title">{{ editing ? '编辑商品' : '发布商品' }}</h2></div>
    <el-alert v-if="editing" title="保存修改后商品将重新进入审核，审核通过前不会出现在交易大厅。" type="info" :closable="false" style="margin-bottom: 20px" />
    <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
      <el-form-item label="标题" prop="title"><el-input v-model="form.title" maxlength="50" show-word-limit placeholder="简洁描述商品" /></el-form-item>
      <el-form-item label="分类" prop="categoryId"><el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%"><el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" /></el-select></el-form-item>
      <el-form-item label="售价" prop="price"><el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 100%" /></el-form-item>
      <el-form-item label="成色" prop="condition"><el-select v-model="form.condition" placeholder="请选择" style="width: 100%"><el-option label="全新" value="全新" /><el-option label="几乎全新" value="几乎全新" /><el-option label="轻微使用痕迹" value="轻微使用痕迹" /><el-option label="明显使用痕迹" value="明显使用痕迹" /></el-select></el-form-item>
      <el-form-item label="商品图片">
        <div class="image-field">
          <div class="image-list"><div v-for="(url, index) in imageUrls" :key="url" class="image-item"><el-image :src="url" fit="cover" /><el-button size="small" type="danger" @click="imageUrls.splice(index, 1)">移除</el-button></div></div>
          <el-upload :show-file-list="false" accept="image/jpeg,image/png" :http-request="uploadProductImage" :disabled="imageUrls.length >= 4 || uploading"><el-button :loading="uploading" :disabled="imageUrls.length >= 4">选择图片</el-button></el-upload>
          <span class="hint">最多 4 张 JPG/PNG，每张不超过 5 MB；第一张作为封面。</span>
        </div>
      </el-form-item>
      <el-form-item label="描述" prop="description"><el-input v-model="form.description" type="textarea" :rows="5" maxlength="1000" show-word-limit placeholder="详细描述商品状况、交易地点等" /></el-form-item>
      <el-form-item><el-button type="primary" :loading="loading" :disabled="uploading" @click="onSubmit">{{ editing ? '保存并提交审核' : '提交发布' }}</el-button><el-button @click="router.push('/my-products')">取消</el-button></el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules, UploadRequestOptions } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createProductApi, getProductDetailApi, updateProductApi } from '@/api/product'
import { getCategoriesApi } from '@/api/category'
import { uploadImageApi } from '@/api/upload'
import { productImages } from '@/utils/page'
import { useUserStore } from '@/stores/user'
import type { Category } from '@/utils/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const editing = computed(() => Boolean(route.params.id))
const formRef = ref<FormInstance>()
const loading = ref(false)
const initializing = ref(false)
const uploading = ref(false)
const categories = ref<Category[]>([])
const imageUrls = ref<string[]>([])
const form = reactive({ title: '', categoryId: undefined as number | string | undefined, price: 0, condition: '几乎全新', description: '' })
const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }],
  condition: [{ required: true, message: '请选择成色', trigger: 'change' }],
  description: [{ required: true, message: '请填写描述', trigger: 'blur' }],
}

async function uploadProductImage(options: UploadRequestOptions) {
  const file = options.file as File
  if (!['image/jpeg', 'image/png'].includes(file.type) || file.size > 5 * 1024 * 1024) {
    ElMessage.warning('只支持 5 MB 以内的 JPG/PNG 图片')
    return
  }
  uploading.value = true
  try {
    imageUrls.value.push(await uploadImageApi(file, 'products'))
    ElMessage.success('图片上传成功')
  } catch { ElMessage.error('图片上传失败') }
  finally { uploading.value = false }
}

async function initialize() {
  initializing.value = true
  try {
    categories.value = await getCategoriesApi()
    if (editing.value) {
      const product = await getProductDetailApi(String(route.params.id))
      if (String(product.sellerId) !== String(userStore.userInfo?.id)) {
        ElMessage.error('只能编辑自己发布的商品')
        router.push('/my-products')
        return
      }
      if ([3, 5].includes(Number(product.status))) {
        ElMessage.warning('交易中或已售商品不能编辑')
        router.push('/my-products')
        return
      }
      form.title = product.title
      form.categoryId = product.categoryId
      form.price = Number(product.price)
      form.condition = product.condition || '几乎全新'
      form.description = product.description || ''
      imageUrls.value = productImages(product).slice(0, 4)
    }
  } catch {
    ElMessage.error('商品信息加载失败')
    if (editing.value) router.push('/my-products')
  } finally { initializing.value = false }
}

async function onSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || uploading.value) return
  loading.value = true
  try {
    const payload = { title: form.title.trim(), categoryId: form.categoryId, price: form.price, description: form.description.trim(), condition: form.condition, images: imageUrls.value.join(',') }
    if (editing.value) {
      await updateProductApi(String(route.params.id), payload)
      ElMessage.success('修改已提交，等待管理员审核')
    } else {
      await createProductApi(payload)
      ElMessage.success('发布成功，等待管理员审核')
    }
    router.push('/my-products')
  } catch { ElMessage.error(editing.value ? '保存失败' : '发布失败') }
  finally { loading.value = false }
}

onMounted(initialize)
</script>

<style scoped>
.image-field { width: 100%; }
.image-list { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 8px; }
.image-item { width: 100px; display: flex; flex-direction: column; gap: 4px; }
.image-item .el-image { width: 100px; height: 100px; border-radius: 6px; background: #f2f3f5; }
.hint { color: #909399; font-size: 12px; }
</style>
