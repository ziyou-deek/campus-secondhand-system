<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">个人中心</h2>
    </div>
    <el-row :gutter="20">
      <el-col :xs="24" :md="8">
        <div class="profile-card">
          <el-avatar :size="72" :src="userStore.userInfo?.avatar || undefined">{{ userStore.displayName.charAt(0) }}</el-avatar>
          <el-upload class="avatar-upload" :show-file-list="false" accept="image/jpeg,image/png" :http-request="uploadAvatar">
            <el-button size="small" :loading="uploadingAvatar">更换头像</el-button>
          </el-upload>
          <p class="avatar-hint">JPG/PNG，不超过 5 MB</p>
          <h3>{{ userStore.displayName }}</h3>
          <p>@{{ userStore.userInfo?.username }}</p>
          <el-tag :type="userStore.isAdmin ? 'danger' : 'success'">
            {{ userStore.isAdmin ? '管理员' : '学生' }}
          </el-tag>
          <div class="quick">
            <el-button @click="$router.push('/my-products')">我的商品</el-button>
            <el-button @click="$router.push('/orders')">我的订单</el-button>
            <el-button @click="$router.push('/favorites')">我的收藏</el-button>
            <el-button @click="$router.push('/messages')">我的私信</el-button>
            <el-button v-if="userStore.isAdmin" type="warning" @click="$router.push('/admin')">
              管理后台
            </el-button>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :md="16">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" />
          </el-form-item>
          <el-form-item label="学号">
            <el-input v-model="form.studentId" />
          </el-form-item>
          <el-form-item label="学院">
            <el-input v-model="form.college" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="save">保存资料</el-button>
          </el-form-item>
        </el-form>
        <el-divider />
        <h3>修改密码</h3>
        <el-form label-width="88px" @submit.prevent>
          <el-form-item label="当前密码">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password autocomplete="current-password" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="passwordForm.newPassword" type="password" show-password autocomplete="new-password" />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password autocomplete="new-password" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="changingPassword" @click="changePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules, UploadRequestOptions } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { changePasswordApi } from '@/api/auth'
import { uploadImageApi } from '@/api/upload'

const userStore = useUserStore()
const router = useRouter()
const formRef = ref<FormInstance>()
const saving = ref(false)
const changingPassword = ref(false)
const uploadingAvatar = ref(false)
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const form = reactive({
  nickname: '',
  phone: '',
  email: '',
  studentId: '',
  college: '',
})

const rules: FormRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
}

function syncForm() {
  const u = userStore.userInfo
  form.nickname = u?.nickname || ''
  form.phone = u?.phone || ''
  form.email = u?.email || ''
  form.studentId = u?.studentId || ''
  form.college = u?.college || ''
}

async function save() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await userStore.updateProfile({ ...form })
    ElMessage.success('资料已保存')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function uploadAvatar(options: UploadRequestOptions) {
  const file = options.file as File
  if (!['image/jpeg', 'image/png'].includes(file.type) || file.size > 5 * 1024 * 1024) {
    ElMessage.warning('只支持 5 MB 以内的 JPG/PNG 图片')
    return
  }
  uploadingAvatar.value = true
  try {
    const avatar = await uploadImageApi(file, 'avatars')
    await userStore.updateProfile({ avatar })
    ElMessage.success('头像已更新')
  } catch { ElMessage.error('头像上传失败') }
  finally { uploadingAvatar.value = false }
}

async function changePassword() {
  if (!passwordForm.oldPassword || passwordForm.newPassword.length < 8) {
    ElMessage.warning('请填写当前密码，新密码至少8位')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次新密码不一致')
    return
  }
  changingPassword.value = true
  try {
    await changePasswordApi({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
    userStore.logout()
    ElMessage.success('密码已修改，请重新登录')
    router.push('/login')
  } finally {
    changingPassword.value = false
  }
}

onMounted(async () => {
  await userStore.fetchProfile()
  syncForm()
})
</script>

<style scoped>
.profile-card {
  text-align: center;
  padding: 24px 12px;
  background: #f9fafc;
  border-radius: 8px;
}
.profile-card h3 {
  margin: 12px 0 4px;
}
.profile-card p {
  color: #909399;
  margin: 0 0 12px;
}
.avatar-upload { margin-top: 10px; }
.profile-card .avatar-hint { font-size: 12px; margin: 4px 0 0; }
.quick {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 20px;
}
</style>
