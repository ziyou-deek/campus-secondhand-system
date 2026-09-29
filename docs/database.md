# 1.0 数据库说明

MySQL 8 数据库名 `campus_secondhand`，字符集 `utf8mb4`。主脚本 `database/init.sql` 用于**全新库**，会 DROP 同名表；演示数据 `database/demo-data.sql` 仅用于本地；从原框架已有数据库升级请先备份，再执行 `database/migrations/V1_0__order_reservation.sql` 一次。

| 表 | 用途 | 主要关联 |
| --- | --- | --- |
| `user` | 学生和管理员、角色与状态 | `id` 被商品、订单、评价、消息引用 |
| `category` | 商品分类 | `product.category_id` |
| `product` | 商品、价格、审核/交易状态 | `user_id` 卖家 |
| `favorite` | 用户收藏 | 用户与商品组合唯一 |
| `trade_order` | 订单、双方确认标志 | 商品、买家、卖家 |
| `review` | 完成订单后的评价 | 订单、商品、评价双方 |
| `message` | 简单站内消息 | 发送人、接收人、关联商品 |

交易一致性设计：下单时执行带 `product.status=1` 条件的更新，成功才插入订单，两个动作处于同一事务；取消或双方确认完成时，在锁定订单行后更新商品状态。`trade_order.buyer_confirmed` 和 `seller_confirmed` 保存双方各自的完成确认。SQL 中保留了原框架的逻辑关联与索引；尚未增加外键约束。商品图片目前存放在 `product.images` 的 URL 字符串中，后续图片上传模块可拆出 `goods_image` 表。独立用户举报表和处理流程仍待下一阶段。
