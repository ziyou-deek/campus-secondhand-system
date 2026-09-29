package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.common.BusinessException;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.dto.OrderRequest;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.TradeOrder;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.TradeOrderMapper;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.security.SecurityUtils;
import com.campus.secondhand.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final TradeOrderMapper tradeOrderMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public TradeOrder create(OrderRequest request) {
        Product product = productMapper.selectById(request.getProductId());
        if (product == null) throw new BusinessException("商品不存在");
        if (product.getStatus() == null || product.getStatus() != Constants.PRODUCT_ON_SALE) {
            throw new BusinessException("商品不在售，无法下单");
        }
        Long buyerId = SecurityUtils.getUserId();
        if (Objects.equals(buyerId, product.getUserId())) throw new BusinessException("不能购买自己的商品");
        // 条件更新是商品的抢占点，并发请求只有一个能从“在售”改为“交易中”。
        int reserved = productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, product.getId())
                .eq(Product::getStatus, Constants.PRODUCT_ON_SALE)
                .set(Product::getStatus, Constants.PRODUCT_RESERVED));
        if (reserved != 1) throw new BusinessException("该商品已有进行中的订单");
        TradeOrder order = new TradeOrder();
        order.setOrderNo(genOrderNo());
        order.setProductId(product.getId());
        order.setBuyerId(buyerId);
        order.setSellerId(product.getUserId());
        order.setPrice(product.getPrice());
        order.setStatus(Constants.ORDER_PENDING);
        order.setBuyerConfirmed(false);
        order.setSellerConfirmed(false);
        order.setRemark(request.getRemark());
        tradeOrderMapper.insert(order);
        fillExtra(List.of(order));
        return order;
    }

    @Override
    public Page<TradeOrder> mine(int page, int size, String role, Integer status) {
        Long uid = SecurityUtils.getUserId();
        LambdaQueryWrapper<TradeOrder> qw = new LambdaQueryWrapper<>();
        if ("buyer".equalsIgnoreCase(role)) qw.eq(TradeOrder::getBuyerId, uid);
        else if ("seller".equalsIgnoreCase(role)) qw.eq(TradeOrder::getSellerId, uid);
        else qw.and(w -> w.eq(TradeOrder::getBuyerId, uid).or().eq(TradeOrder::getSellerId, uid));
        if (status != null) qw.eq(TradeOrder::getStatus, status);
        qw.orderByDesc(TradeOrder::getCreateTime);
        Page<TradeOrder> result = tradeOrderMapper.selectPage(new Page<>(page, size), qw);
        fillExtra(result.getRecords());
        return result;
    }

    @Override
    public TradeOrder getById(Long id) {
        TradeOrder order = tradeOrderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");
        Long uid = SecurityUtils.getUserId();
        if (!Objects.equals(uid, order.getBuyerId()) && !Objects.equals(uid, order.getSellerId()) && !SecurityUtils.isAdmin()) {
            throw new BusinessException(403, "无权查看该订单");
        }
        fillExtra(List.of(order));
        return order;
    }

    @Override
    @Transactional
    public TradeOrder updateStatus(Long id, Integer status) {
        TradeOrder order = tradeOrderMapper.selectForUpdate(id);
        if (order == null) throw new BusinessException("订单不存在");
        Long uid = SecurityUtils.getUserId();
        boolean isBuyer = Objects.equals(uid, order.getBuyerId());
        boolean isSeller = Objects.equals(uid, order.getSellerId());
        if (!isBuyer && !isSeller && !SecurityUtils.isAdmin()) {
            throw new BusinessException(403, "无权操作该订单");
        }
        int cur = order.getStatus();
        if (status == Constants.ORDER_CANCELLED) {
            if (cur != Constants.ORDER_PENDING && cur != Constants.ORDER_TRADING) {
                throw new BusinessException("当前状态不可取消");
            }
            int released = productMapper.update(null, new LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, order.getProductId())
                    .eq(Product::getStatus, Constants.PRODUCT_RESERVED)
                    .set(Product::getStatus, Constants.PRODUCT_ON_SALE));
            if (released != 1) throw new BusinessException("商品状态异常，请联系管理员");
            order.setStatus(Constants.ORDER_CANCELLED);
        } else if (status == Constants.ORDER_TRADING) {
            if (cur != Constants.ORDER_PENDING) throw new BusinessException("只有待交易订单可进入交易中");
            if (!isSeller && !SecurityUtils.isAdmin()) throw new BusinessException(403, "只有卖家可开始交易");
            order.setStatus(Constants.ORDER_TRADING);
        } else if (status == Constants.ORDER_DONE) {
            if (cur != Constants.ORDER_TRADING) throw new BusinessException("只有交易中订单可确认完成");
            if (isBuyer) {
                if (Boolean.TRUE.equals(order.getBuyerConfirmed())) throw new BusinessException("买家已确认，请等待卖家");
                order.setBuyerConfirmed(true);
            } else if (isSeller) {
                if (Boolean.TRUE.equals(order.getSellerConfirmed())) throw new BusinessException("卖家已确认，请等待买家");
                order.setSellerConfirmed(true);
            } else {
                throw new BusinessException(403, "请由交易双方确认完成");
            }
            // 双方确认后才结束交易；事务内锁住订单行，避免两端同时确认时丢失更新。
            if (Boolean.TRUE.equals(order.getBuyerConfirmed()) && Boolean.TRUE.equals(order.getSellerConfirmed())) {
                int sold = productMapper.update(null, new LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, order.getProductId())
                        .eq(Product::getStatus, Constants.PRODUCT_RESERVED)
                        .set(Product::getStatus, Constants.PRODUCT_SOLD));
                if (sold != 1) throw new BusinessException("商品状态异常，请联系管理员");
                order.setStatus(Constants.ORDER_DONE);
            }
        } else {
            throw new BusinessException("不支持的状态变更");
        }
        tradeOrderMapper.updateById(order);
        fillExtra(List.of(order));
        return order;
    }

    private String genOrderNo() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int r = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "ORD" + ts + r;
    }

    private void fillExtra(List<TradeOrder> list) {
        if (list == null || list.isEmpty()) return;
        Set<Long> pids = list.stream().map(TradeOrder::getProductId).collect(Collectors.toSet());
        Set<Long> uids = list.stream().flatMap(o -> java.util.stream.Stream.of(o.getBuyerId(), o.getSellerId())).collect(Collectors.toSet());
        Map<Long, Product> pmap = productMapper.selectBatchIds(pids).stream().collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
        Map<Long, User> umap = userMapper.selectBatchIds(uids).stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        for (TradeOrder o : list) {
            Product p = pmap.get(o.getProductId());
            if (p != null) {
                o.setProductTitle(p.getTitle());
                o.setProductImages(p.getImages());
            }
            User b = umap.get(o.getBuyerId());
            if (b != null) o.setBuyerNickname(b.getNickname());
            User s = umap.get(o.getSellerId());
            if (s != null) o.setSellerNickname(s.getNickname());
        }
    }
}
