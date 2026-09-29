package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.common.BusinessException;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.entity.Favorite;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.mapper.FavoriteMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.security.SecurityUtils;
import com.campus.secondhand.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;

    @Override
    public void add(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() == null || product.getStatus() != Constants.PRODUCT_ON_SALE) {
            throw new BusinessException("商品不在售，无法收藏");
        }
        Long uid = SecurityUtils.getUserId();
        Long cnt = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, uid).eq(Favorite::getProductId, productId));
        if (cnt != null && cnt > 0) return;
        Favorite f = new Favorite();
        f.setUserId(uid);
        f.setProductId(productId);
        favoriteMapper.insert(f);
    }

    @Override
    public void remove(Long productId) {
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, SecurityUtils.getUserId())
                .eq(Favorite::getProductId, productId));
    }

    @Override
    public boolean isFavorited(Long productId) {
        Long cnt = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, SecurityUtils.getUserId())
                .eq(Favorite::getProductId, productId));
        return cnt != null && cnt > 0;
    }

    @Override
    public Page<Favorite> mine(int page, int size) {
        Page<Favorite> result = favoriteMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, SecurityUtils.getUserId())
                        .orderByDesc(Favorite::getCreateTime));
        for (Favorite f : result.getRecords()) {
            Product product = productMapper.selectById(f.getProductId());
            if (product != null && product.getStatus() != null && product.getStatus() == Constants.PRODUCT_ON_SALE) {
                f.setProduct(product);
            }
        }
        return result;
    }
}
