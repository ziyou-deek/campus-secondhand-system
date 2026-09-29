package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.secondhand.common.BusinessException;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.dto.ReviewRequest;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.Review;
import com.campus.secondhand.entity.TradeOrder;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.ReviewMapper;
import com.campus.secondhand.mapper.TradeOrderMapper;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.security.SecurityUtils;
import com.campus.secondhand.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final TradeOrderMapper tradeOrderMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;

    @Override
    public Review create(ReviewRequest request) {
        TradeOrder order = tradeOrderMapper.selectById(request.getOrderId());
        if (order == null) throw new BusinessException("订单不存在");
        if (order.getStatus() == null || order.getStatus() != Constants.ORDER_DONE) {
            throw new BusinessException("订单完成后才能评价");
        }
        Long uid = SecurityUtils.getUserId();
        if (!Objects.equals(uid, order.getBuyerId()) && !Objects.equals(uid, order.getSellerId())) {
            throw new BusinessException(403, "只有买卖双方可评价");
        }
        Long cnt = reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, order.getId()).eq(Review::getFromUserId, uid));
        if (cnt != null && cnt > 0) throw new BusinessException("您已评价过该订单");
        Long toUserId = Objects.equals(uid, order.getBuyerId()) ? order.getSellerId() : order.getBuyerId();
        Review review = new Review();
        review.setOrderId(order.getId());
        review.setProductId(order.getProductId());
        review.setFromUserId(uid);
        review.setToUserId(toUserId);
        review.setScore(request.getScore());
        review.setContent(request.getContent());
        reviewMapper.insert(review);
        fillExtra(List.of(review));
        return review;
    }

    @Override
    public List<Review> listByProduct(Long productId) {
        List<Review> list = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getProductId, productId).orderByDesc(Review::getCreateTime));
        fillExtra(list);
        return list;
    }

    @Override
    public List<Review> listByUser(Long userId) {
        List<Review> list = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getToUserId, userId).orderByDesc(Review::getCreateTime));
        fillExtra(list);
        return list;
    }

    private void fillExtra(List<Review> list) {
        if (list == null || list.isEmpty()) return;
        Set<Long> uids = list.stream().flatMap(r -> Stream.of(r.getFromUserId(), r.getToUserId())).collect(Collectors.toSet());
        Set<Long> pids = list.stream().map(Review::getProductId).collect(Collectors.toSet());
        Map<Long, User> umap = userMapper.selectBatchIds(uids).stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        Map<Long, Product> pmap = productMapper.selectBatchIds(pids).stream().collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
        for (Review r : list) {
            User f = umap.get(r.getFromUserId());
            if (f != null) r.setFromUserNickname(f.getNickname());
            User t = umap.get(r.getToUserId());
            if (t != null) r.setToUserNickname(t.getNickname());
            Product p = pmap.get(r.getProductId());
            if (p != null) r.setProductTitle(p.getTitle());
        }
    }
}
