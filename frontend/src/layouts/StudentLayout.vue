<template>
  <el-container class="student-layout">
    <el-header class="header" height="60px">
      <div class="brand" @click="$router.push('/home')">
        <el-icon :size="22"><ShoppingBag /></el-icon>
        <span>校园二手交易</span>
      </div>
      <el-menu
        mode="horizontal"
        :ellipsis="false"
        :default-active="activeMenu"
        router
        class="nav-menu"
      >
        <el-menu-item index="/home">首页</el-menu-item>
        <el-menu-item index="/publish" v-if="userStore.isLoggedIn">发布</el-menu-item>
        <el-menu-item index="/favorites" v-if="userStore.isLoggedIn">收藏</el-menu-item>
        <el-menu-item index="/orders" v-if="userStore.isLoggedIn">订单</el-menu-item>
        <el-menu-item index="/messages" v-if="userStore.isLoggedIn">私信</el-menu-item>
        <el-menu-item index="/profile" v-if="userStore.isLoggedIn">我的</el-menu-item>
      </el-menu>
      <div class="header-right">
        <template v-if="userStore.isLoggedIn">
          <el-button v-if="userStore.isAdmin" type="warning" link @click="$router.push('/admin')">
            管理后台
          </el-button>
          <el-dropdown @command="onCommand">
            <span class="user-entry">
              <el-avatar :size="28" :src="userStore.userInfo?.avatar || undefined">{{ userStore.displayName.charAt(0) }}</el-avatar>
              <span class="name">{{ userStore.displayName }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="my-products">我的商品</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" link @click="$router.push('/login')">登录</el-button>
          <el-button type="primary" @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </el-header>
    <el-main class="main">
      <router-view />
    </el-main>
    <el-footer class="footer" height="48px">
      校园二手交易管理系统 © {{ year }}
    </el-footer>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const year = new Date().getFullYear()

const activeMenu = computed(() => {
  const p = route.path
  if (p.startsWith('/products')) return '/home'
  if (p.startsWith('/orders')) return '/orders'
  if (p.startsWith('/my-products')) return '/profile'
  return p
})

async function onCommand(cmd: string) {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' })
    userStore.logout()
    router.push('/login')
  } else if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'my-products') {
    router.push('/my-products')
  }
}
</script>

<style scoped>
.student-layout {
  min-height: 100vh;
}
.header {
  display: flex;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  padding: 0 24px;
  gap: 16px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #409eff;
  cursor: pointer;
  white-space: nowrap;
}
.nav-menu {
  flex: 1;
  border-bottom: none !important;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-entry {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.name {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.main {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}
.footer {
  text-align: center;
  color: #909399;
  font-size: 13px;
  line-height: 48px;
  background: #fff;
  border-top: 1px solid #ebeef5;
}
</style>
