# 1.0 接口摘要

接口基址：本地 `http://localhost:8080`，线上 `https://121.43.101.242:8443`。JSON 响应统一为 `{ "code": 200, "msg": "success", "data": ... }`；业务错误的 `code` 为 400/401/403/500。除公开接口外，请求头需要 `Authorization: Bearer <JWT>`。

| 功能 | 方法与路径 | 说明 |
| --- | --- | --- |
| 注册、登录 | `POST /api/auth/register`、`POST /api/auth/login` | 登录返回 token 与用户资料 |
| 当前用户 | `GET /api/auth/me` | 需要 JWT |
| 修改资料 | `PUT /api/auth/profile` | 可传 `avatar` 图片地址；个人中心上传后自动保存 |
| 上传图片 | `POST /api/uploads/images` | 需要 JWT；`multipart/form-data` 含 `file` 和 `kind=products/avatars`，返回 `{url}`；JPG/PNG，最多 5 MB |
| 访问图片 | `GET /uploads/{kind}/{name}` | 公开访问，供列表、详情、头像显示 |
| 修改密码 | `PUT /api/auth/password` | 提交 `oldPassword`、`newPassword`；新密码至少 8 位 |
| 商品列表、详情、热门 | `GET /api/products`、`GET /api/products/{id}`、`GET /api/products/hot` | 公开列表只显示在售商品；非公开详情仅卖家/管理员可见 |
| 发布、编辑、下架 | `POST /api/products`、`PUT /api/products/{id}`、`DELETE /api/products/{id}` | 卖家操作，交易中或已售不可编辑；编辑后重新待审核。`images` 存逗号分隔的图片地址 |
| 分类 | `GET /api/categories` | 公开 |
| 收藏 | `/api/favorites` 相关接口 | 在售商品可收藏，收藏列表需登录 |
| 订单 | `POST /api/orders`、`GET /api/orders/mine`、`GET /api/orders/{id}` | 商品从在售原子转交易中，防重复订单 |
| 订单状态 | `PUT /api/orders/{id}/status` | 请求体 `{ "status": 1|2|3 }`：卖家开始、双方分别确认、取消 |
| 评价与消息 | `/api/reviews`、`/api/messages` 相关接口 | 完成订单后评价，登录后可留言 |
| 管理 | `/api/admin/users`、`/api/admin/products`、`/api/admin/categories`、`/api/admin/stats` | ADMIN 角色 |
| 管理员重置用户密码 | `PUT /api/admin/users/{id}/reset-password` | 仅 ADMIN 可调用，只能重置普通用户；响应 `data` 含 `username`、`temporaryPassword`，临时密码只展示这一次 |

订单状态 0 待交易、1 交易中、2 已完成、3 已取消。请求 status=2 表示**当前用户确认**；只有买卖双方都确认，状态才变为 2。商品状态 0 待审核、1 在售、2 已下架、3 已售出、4 审核拒绝、5 交易中。

示例：`POST /api/auth/login`，请求体 `{ "username": "你的用户名", "password": "你的密码" }`。管理员审核通过商品：`PUT /api/admin/products/{id}/audit`，请求体 `{ "status": 1 }`。

忘记密码流程：用户线下向管理员证明身份，管理员在“用户管理”点击“重置密码”并确认，将一次性显示的随机临时密码私下交给该用户。用户用临时密码登录后，在个人中心通过 `PUT /api/auth/password` 修改密码。重置和修改密码都会让该用户先前的 JWT 失效。系统暂不提供短信或邮件自助找回，也不自动强制首次登录修改临时密码。管理员账户不能通过此接口重置。临时密码不应写入工单、截图或公开聊天。
