USE campus_secondhand;

-- admin / admin123
-- student passwords: 123456
INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `phone`, `email`, `avatar`, `role`, `status`, `create_time`, `update_time`) VALUES
(1, 'admin', '$2a$10$0UAucscMKM8/HYXHpOhmGexj/8XpjEr8bdkT5J2C1MO0ubExc.0bu', '系统管理员', '13800000000', 'admin@campus.edu', NULL, 'ADMIN', 1, NOW(), NOW()),
(2, 'student1', '$2a$10$55AMYJUObB2mdhcOxwU4p.kmbSX0eQdl//skjod/ytL/v.HlXeckC', '张三', '13800000001', 'zhangsan@campus.edu', NULL, 'USER', 1, NOW(), NOW()),
(3, 'student2', '$2a$10$55AMYJUObB2mdhcOxwU4p.kmbSX0eQdl//skjod/ytL/v.HlXeckC', '李四', '13800000002', 'lisi@campus.edu', NULL, 'USER', 1, NOW(), NOW()),
(4, 'student3', '$2a$10$55AMYJUObB2mdhcOxwU4p.kmbSX0eQdl//skjod/ytL/v.HlXeckC', '王五', '13800000003', 'wangwu@campus.edu', NULL, 'USER', 1, NOW(), NOW());

INSERT INTO `category` (`id`, `name`, `sort`, `create_time`) VALUES
(1, '教材', 1, NOW()),
(2, '电子产品', 2, NOW()),
(3, '生活用品', 3, NOW()),
(4, '体育用品', 4, NOW()),
(5, '其他', 5, NOW());

INSERT INTO `product` (`id`, `user_id`, `category_id`, `title`, `description`, `price`, `images`, `condition_level`, `status`, `view_count`, `create_time`, `update_time`) VALUES
(1, 2, 1, '高等数学上下册', '同济第七版，几乎全新，无笔记', 35.00, 'https://picsum.photos/400/300?random=1', '九成新', 5, 128, NOW(), NOW()),
(2, 2, 2, '二手笔记本电脑', 'i5/8G/256G，日常学习够用', 1800.00, 'https://picsum.photos/400/300?random=2', '八成新', 1, 256, NOW(), NOW()),
(3, 3, 3, '宿舍小台灯', '护眼LED台灯，可调光', 25.00, 'https://picsum.photos/400/300?random=3', '全新', 1, 45, NOW(), NOW()),
(4, 3, 4, '羽毛球拍一对', '含球三只，毕业甩卖', 60.00, 'https://picsum.photos/400/300?random=4', '七成新', 5, 89, NOW(), NOW()),
(5, 2, 5, '考研资料全套', '政治+英语+专业课笔记', 80.00, 'https://picsum.photos/400/300?random=5', '九成新', 0, 12, NOW(), NOW()),
(6, 3, 2, '机械键盘', '青轴，RGB背光', 120.00, 'https://picsum.photos/400/300?random=6', '八成新', 2, 67, NOW(), NOW()),
(7, 2, 1, '大学英语四级真题', '近五年真题含解析', 20.00, 'https://picsum.photos/400/300?random=7', '九成新', 3, 200, NOW(), NOW()),
(8, 4, 3, '电热水壶', '1.5L，自动断电', 30.00, 'https://picsum.photos/400/300?random=8', '八成新', 4, 5, NOW(), NOW());

INSERT INTO `favorite` (`id`, `user_id`, `product_id`, `create_time`) VALUES
(1, 3, 1, NOW()),
(2, 3, 2, NOW()),
(3, 2, 3, NOW()),
(4, 4, 1, NOW()),
(5, 4, 4, NOW());

INSERT INTO `trade_order` (`id`, `order_no`, `product_id`, `buyer_id`, `seller_id`, `price`, `status`, `remark`, `create_time`, `update_time`) VALUES
(1, 'ORD202609270001', 7, 3, 2, 20.00, 2, '校园当面交易', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 'ORD202609270002', 1, 4, 2, 35.00, 0, '图书馆门口见面', NOW(), NOW()),
(3, 'ORD202609270003', 4, 2, 3, 60.00, 1, '体育馆交易', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW());

INSERT INTO `review` (`id`, `order_id`, `product_id`, `from_user_id`, `to_user_id`, `score`, `content`, `create_time`) VALUES
(1, 1, 7, 3, 2, 5, '书很新，卖家很靠谱，推荐！', DATE_SUB(NOW(), INTERVAL 3 DAY));

INSERT INTO `message` (`id`, `from_user_id`, `to_user_id`, `product_id`, `content`, `is_read`, `create_time`) VALUES
(1, 3, 2, 1, '高数还在吗？可以便宜点吗？', 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 2, 3, 1, '可以，32块钱吧，图书馆门口见', 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 4, 2, 2, '笔记本配置详细点可以吗？', 0, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(4, 2, 3, 4, '羽毛球拍今天方便交易吗？', 0, NOW());
