package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.secondhand.entity.*;
import com.campus.secondhand.mapper.*;
import com.campus.secondhand.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final TradeOrderMapper tradeOrderMapper;
    private final CategoryMapper categoryMapper;
    private final ReviewMapper reviewMapper;
    private final MessageMapper messageMapper;

    @Override
    public Map<String, Object> stats() {
        Map<String, Object> data = new LinkedHashMap<>();
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        long userCount = Optional.ofNullable(userMapper.selectCount(null)).orElse(0L);
        long productCount = Optional.ofNullable(productMapper.selectCount(null)).orElse(0L);
        long orderCount = Optional.ofNullable(tradeOrderMapper.selectCount(null)).orElse(0L);
        long reviewCount = Optional.ofNullable(reviewMapper.selectCount(null)).orElse(0L);
        long messageCount = Optional.ofNullable(messageMapper.selectCount(null)).orElse(0L);

        long todayUsers = Optional.ofNullable(userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreateTime, todayStart))).orElse(0L);
        long todayProducts = Optional.ofNullable(productMapper.selectCount(
                new LambdaQueryWrapper<Product>().ge(Product::getCreateTime, todayStart))).orElse(0L);
        long todayOrders = Optional.ofNullable(tradeOrderMapper.selectCount(
                new LambdaQueryWrapper<TradeOrder>().ge(TradeOrder::getCreateTime, todayStart))).orElse(0L);

        data.put("userCount", userCount);
        data.put("productCount", productCount);
        data.put("orderCount", orderCount);
        data.put("reviewCount", reviewCount);
        data.put("messageCount", messageCount);
        data.put("todayUsers", todayUsers);
        data.put("todayProducts", todayProducts);
        data.put("todayOrders", todayOrders);

        Map<String, Long> productStatus = new LinkedHashMap<>();
        String[] statusNames = {"待审核", "在售", "已下架", "已售出", "审核拒绝"};
        for (int i = 0; i <= 4; i++) {
            long c = Optional.ofNullable(productMapper.selectCount(
                    new LambdaQueryWrapper<Product>().eq(Product::getStatus, i))).orElse(0L);
            productStatus.put(statusNames[i], c);
        }
        data.put("productStatusDist", productStatus);

        Map<String, Long> orderStatus = new LinkedHashMap<>();
        String[] orderNames = {"待交易", "交易中", "已完成", "已取消"};
        for (int i = 0; i <= 3; i++) {
            long c = Optional.ofNullable(tradeOrderMapper.selectCount(
                    new LambdaQueryWrapper<TradeOrder>().eq(TradeOrder::getStatus, i))).orElse(0L);
            orderStatus.put(orderNames[i], c);
        }
        data.put("orderStatusDist", orderStatus);

        List<Category> categories = categoryMapper.selectList(null);
        List<Map<String, Object>> categoryDist = new ArrayList<>();
        for (Category cat : categories) {
            long c = Optional.ofNullable(productMapper.selectCount(
                    new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, cat.getId()))).orElse(0L);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", cat.getName());
            item.put("value", c);
            categoryDist.add(item);
        }
        data.put("categoryDist", categoryDist);

        // 前端 ECharts 兼容字段
        data.put("categoryStats", categoryDist);
        List<Map<String, Object>> productStatusStats = new ArrayList<>();
        for (Map.Entry<String, Long> e : productStatus.entrySet()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", e.getKey());
            m.put("value", e.getValue());
            productStatusStats.add(m);
        }
        data.put("productStatusStats", productStatusStats);
        List<Map<String, Object>> orderStatusStats = new ArrayList<>();
        for (Map.Entry<String, Long> e : orderStatus.entrySet()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", e.getKey());
            m.put("value", e.getValue());
            orderStatusStats.add(m);
        }
        data.put("orderStatusStats", orderStatusStats);
        data.put("orderTrend", orderStatusStats);

                data.put("categoryNames", categories.stream().map(Category::getName).collect(Collectors.toList()));
        data.put("categoryValues", categories.stream().map(cat ->
                Optional.ofNullable(productMapper.selectCount(
                        new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, cat.getId()))).orElse(0L)
        ).collect(Collectors.toList()));

        return data;
    }
}
