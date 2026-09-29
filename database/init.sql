-- Campus Secondhand Trading System Schema
-- Database: campus_secondhand

CREATE DATABASE IF NOT EXISTS campus_secondhand DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_secondhand;

DROP TABLE IF EXISTS `message`;
DROP TABLE IF EXISTS `review`;
DROP TABLE IF EXISTS `trade_order`;
DROP TABLE IF EXISTS `favorite`;
DROP TABLE IF EXISTS `product`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `username`    VARCHAR(50)  NOT NULL,
  `password`    VARCHAR(100) NOT NULL,
  `nickname`    VARCHAR(50)  DEFAULT NULL,
  `phone`       VARCHAR(20)  DEFAULT NULL,
  `email`       VARCHAR(100) DEFAULT NULL,
  `avatar`      VARCHAR(255) DEFAULT NULL,
  `role`        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT 'USER / ADMIN',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '0禁用 1正常',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `category` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(50) NOT NULL,
  `sort`        INT         NOT NULL DEFAULT 0,
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类';

CREATE TABLE `product` (
  `id`              BIGINT         NOT NULL AUTO_INCREMENT,
  `user_id`         BIGINT         NOT NULL,
  `category_id`     BIGINT         DEFAULT NULL,
  `title`           VARCHAR(100)   NOT NULL,
  `description`     TEXT,
  `price`           DECIMAL(10,2)  NOT NULL,
  `images`          VARCHAR(1000)  DEFAULT NULL COMMENT '逗号分隔URL或JSON',
  `condition_level` VARCHAR(20)    DEFAULT NULL COMMENT '成色',
  `status`          TINYINT        NOT NULL DEFAULT 0 COMMENT '0待审核 1在售 2已下架 3已售出 4审核拒绝 5交易中',
  `view_count`      INT            NOT NULL DEFAULT 0,
  `create_time`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

CREATE TABLE `favorite` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT   NOT NULL,
  `product_id`  BIGINT   NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

CREATE TABLE `trade_order` (
  `id`          BIGINT         NOT NULL AUTO_INCREMENT,
  `order_no`    VARCHAR(64)    NOT NULL,
  `product_id`  BIGINT         NOT NULL,
  `buyer_id`    BIGINT         NOT NULL,
  `seller_id`   BIGINT         NOT NULL,
  `price`       DECIMAL(10,2)  NOT NULL,
  `status`      TINYINT        NOT NULL DEFAULT 0 COMMENT '0待交易 1交易中 2已完成 3已取消',
  `buyer_confirmed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '买家确认交易完成',
  `seller_confirmed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '卖家确认交易完成',
  `remark`      VARCHAR(255)   DEFAULT NULL,
  `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单';

CREATE TABLE `review` (
  `id`           BIGINT   NOT NULL AUTO_INCREMENT,
  `order_id`     BIGINT   NOT NULL,
  `product_id`   BIGINT   NOT NULL,
  `from_user_id` BIGINT   NOT NULL,
  `to_user_id`   BIGINT   NOT NULL,
  `score`        TINYINT  NOT NULL COMMENT '1-5',
  `content`      VARCHAR(500) DEFAULT NULL,
  `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_to_user_id` (`to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

CREATE TABLE `message` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `from_user_id` BIGINT       NOT NULL,
  `to_user_id`   BIGINT       NOT NULL,
  `product_id`   BIGINT       DEFAULT NULL,
  `content`      VARCHAR(1000) NOT NULL,
  `is_read`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_from_user` (`from_user_id`),
  KEY `idx_to_user` (`to_user_id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息';
