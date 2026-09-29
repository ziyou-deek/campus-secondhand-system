import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { getToken, getStoredUser } from '@/utils/auth'
import type { UserInfo } from '@/utils/types'
import { ElMessage } from 'element-plus'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/student/Login.vue'),
    meta: { title: '登录', public: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/student/Register.vue'),
    meta: { title: '注册', public: true },
  },
  {
    path: '/',
    component: () => import('@/layouts/StudentLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/student/Home.vue'),
        meta: { title: '商品列表' },
      },
      {
        path: 'products/:id',
        name: 'ProductDetail',
        component: () => import('@/views/student/ProductDetail.vue'),
        meta: { title: '商品详情' },
      },
      {
        path: 'publish',
        name: 'Publish',
        component: () => import('@/views/student/Publish.vue'),
        meta: { title: '发布商品', auth: true },
      },
      {
        path: 'products/:id/edit',
        name: 'EditProduct',
        component: () => import('@/views/student/Publish.vue'),
        meta: { title: '编辑商品', auth: true },
      },
      {
        path: 'favorites',
        name: 'Favorites',
        component: () => import('@/views/student/Favorites.vue'),
        meta: { title: '我的收藏', auth: true },
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/student/Orders.vue'),
        meta: { title: '我的订单', auth: true },
      },
      {
        path: 'orders/:id',
        name: 'OrderDetail',
        component: () => import('@/views/student/OrderDetail.vue'),
        meta: { title: '订单详情', auth: true },
      },
      {
        path: 'messages',
        name: 'Messages',
        component: () => import('@/views/student/Messages.vue'),
        meta: { title: '私信', auth: true },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/student/Profile.vue'),
        meta: { title: '个人中心', auth: true },
      },
      {
        path: 'my-products',
        name: 'MyProducts',
        component: () => import('@/views/student/MyProducts.vue'),
        meta: { title: '我的商品', auth: true },
      },
    ],
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { auth: true, admin: true },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '数据统计', auth: true, admin: true },
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/Users.vue'),
        meta: { title: '用户管理', auth: true, admin: true },
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('@/views/admin/Products.vue'),
        meta: { title: '商品审核', auth: true, admin: true },
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/Categories.vue'),
        meta: { title: '分类管理', auth: true, admin: true },
      },
      {
        path: 'violations',
        name: 'AdminViolations',
        component: () => import('@/views/admin/Violations.vue'),
        meta: { title: '违规处理', auth: true, admin: true },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/student/NotFound.vue'),
    meta: { title: '页面不存在', public: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to, _from, next) => {
  document.title = `${(to.meta.title as string) || '校园二手'} - 校园二手交易`
  const token = getToken()
  const user = getStoredUser<UserInfo>()
  const needAuth = to.matched.some((r) => r.meta.auth)
  const needAdmin = to.matched.some((r) => r.meta.admin)
  const isPublic = to.meta.public

  if (needAuth && !token) {
    ElMessage.warning('请先登录')
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  if (needAdmin && String(user?.role || '').toUpperCase() !== 'ADMIN') {
    ElMessage.error('无权访问管理后台')
    next('/home')
    return
  }
  if ((to.path === '/login' || to.path === '/register') && token) {
    next(String(user?.role || '').toUpperCase() === 'ADMIN' ? '/admin' : '/home')
    return
  }
  if (!isPublic && !needAuth && !token && to.path !== '/home' && !to.path.startsWith('/products')) {
    // 商品列表与详情允许访客浏览（home 已在学生布局下，不强制）
  }
  next()
})

export default router
