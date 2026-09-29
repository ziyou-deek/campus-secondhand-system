# 校园二手交易管理系统 · 前端

基于 **Vue 3 + Vite + TypeScript + Element Plus + Pinia + Vue Router + Axios + ECharts** 的校园二手交易前端。

## 环境要求

- Node.js 18+（推荐 20）
- npm 9+

## 安装与启动

```bash
cd frontend
npm install
npm run dev
```

- 开发地址：**http://localhost:5173**
- 默认端口：`5173`（见 `vite.config.ts`）

### 常用脚本

| 命令 | 说明 |
|------|------|
| `npm run dev` | 启动开发服务器（热更新） |
| `npm run build` | 类型检查 + 生产构建，产物在 `dist/` |
| `npm run preview` | 预览生产构建 |

## 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `VITE_API_BASE` | 后端 API 基址；**留空则走 Vite 代理** `/api` → `8080` | 开发态为空 |

配置文件：

- `.env.development`：`VITE_API_BASE=`（空，推荐，走 `vite.config.ts` 中的 `/api` 代理）
- 若需直连后端，可设为 `http://localhost:8080`（需后端 CORS）

请求头约定：登录后自动附加 `Authorization: Bearer <token>`（token 存于 `localStorage`）。

## 与后端联调

1. 启动后端，确认监听 `http://localhost:8080`。
2. 确认前端 `VITE_API_BASE` 指向该地址。
3. 接口路径前缀约定：

| 前缀 | 说明 |
|------|------|
| `/api/auth` | 登录、注册、当前用户、资料 |
| `/api/products` | 商品列表/搜索/详情/发布/我的 |
| `/api/categories` | 分类 |
| `/api/favorites` | 收藏 |
| `/api/orders` | 订单 |
| `/api/comments` | 评价 |
| `/api/messages` | 私信会话与消息 |
| `/api/admin/*` | 用户、商品审核、分类、违规、统计 |

4. 响应建议兼容：`{ code: 0|200, data, message }` 或直接返回业务数据；分页可用 `{ list|records, total }`。
5. 登录/注册依赖真实后端；失败时会展示后端错误，不再使用本地假登录。

联调账号见根目录 README（如 `admin/admin123`、`student1/123456`）。

## 页面 / 路由清单

### 学生端（`StudentLayout`）

| Path | 名称 | 说明 |
|------|------|------|
| `/login` | 登录 | 公开 |
| `/register` | 注册 | 公开 |
| `/home` | 商品列表 | 搜索、分类、分页 |
| `/products/:id` | 商品详情 | 下单/收藏/私信；评价仅展示列表 |
| `/publish` | 发布商品 | 需登录 |
| `/favorites` | 我的收藏 | 需登录 |
| `/orders` | 我的订单 | 需登录 |
| `/orders/:id` | 订单详情 | 需登录 |
| `/messages` | 私信 | 会话列表 + 聊天，需登录 |
| `/profile` | 个人中心 | 资料与入口，需登录 |
| `/my-products` | 我的商品 | 需登录 |

### 管理后台（`AdminLayout`，需 `role=admin`）

| Path | 名称 |
|------|------|
| `/admin/dashboard` | 数据统计（ECharts） |
| `/admin/users` | 用户管理 |
| `/admin/products` | 商品审核 |
| `/admin/categories` | 分类管理 |
| `/admin/violations` | 违规处理 |

### 路由守卫

- 需登录页面：未登录跳转 `/login`（带回跳 `redirect`）
- `/admin/*`：非 `admin` 角色禁止进入

## 工程结构

```
src/
  api/          # Axios 实例与各模块 API
  stores/       # Pinia（user）
  router/       # 路由与守卫
  layouts/      # StudentLayout / AdminLayout
  views/student/
  views/admin/
  components/
  utils/        # auth、format、types、分页归一化
  assets/
```

## 技术说明

- Element Plus 全量引入 + 中文 `zh-cn` locale
- Pinia 管理登录态；token / 用户信息持久化到 `localStorage`
- ECharts 用于管理端统计看板（无数据时占位图）
