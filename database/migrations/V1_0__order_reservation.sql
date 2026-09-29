-- 仅针对原框架已建库执行一次。先备份数据库；不要重新导入会 DROP 表的 schema.sql。
USE campus_secondhand;

ALTER TABLE trade_order
  ADD COLUMN buyer_confirmed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '买家确认交易完成' AFTER status,
  ADD COLUMN seller_confirmed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '卖家确认交易完成' AFTER buyer_confirmed;

-- 原数据中有效订单占用的商品转为交易中；若有重复订单，应先人工核对。
UPDATE product p
JOIN trade_order o ON o.product_id = p.id AND o.status IN (0, 1)
SET p.status = 5
WHERE p.status = 1;

-- 历史完成订单视为双方已确认，便于详情页显示。
UPDATE trade_order SET buyer_confirmed = 1, seller_confirmed = 1 WHERE status = 2;
