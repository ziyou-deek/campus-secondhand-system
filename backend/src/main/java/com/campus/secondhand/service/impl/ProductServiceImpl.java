package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.common.BusinessException;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.dto.AuditRequest;
import com.campus.secondhand.dto.ProductRequest;
import com.campus.secondhand.entity.Category;
import com.campus.secondhand.entity.Favorite;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.CategoryMapper;
import com.campus.secondhand.mapper.FavoriteMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.security.SecurityUtils;
import com.campus.secondhand.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final FavoriteMapper favoriteMapper;

    @Override
    public Page<Product> page(int page, int size, String keyword, Long categoryId, Integer status, String sort) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<>();
        qw.eq(Product::getStatus, Constants.PRODUCT_ON_SALE);
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(Product::getTitle, keyword).or().like(Product::getDescription, keyword));
        }
        if (categoryId != null) {
            qw.eq(Product::getCategoryId, categoryId);
        }
        if ("price_asc".equals(sort)) {
            qw.orderByAsc(Product::getPrice);
        } else if ("price_desc".equals(sort)) {
            qw.orderByDesc(Product::getPrice);
        } else if ("view".equals(sort) || "hot".equals(sort)) {
            qw.orderByDesc(Product::getViewCount);
        } else {
            qw.orderByDesc(Product::getCreateTime);
        }
        Page<Product> result = productMapper.selectPage(new Page<>(page, size), qw);
        fillExtra(result.getRecords());
        return result;
    }

    @Override
    public Product getById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStatus() == null || product.getStatus() != Constants.PRODUCT_ON_SALE) {
            Long currentUid = null;
            try { currentUid = SecurityUtils.getUserId(); } catch (BusinessException ignored) { }
            if (!Objects.equals(currentUid, product.getUserId()) && !SecurityUtils.isAdmin()) {
                throw new BusinessException(403, "商品不可查看");
            }
        }
        product.setViewCount(product.getViewCount() == null ? 1 : product.getViewCount() + 1);
        Product upd = new Product();
        upd.setId(id);
        upd.setViewCount(product.getViewCount());
        productMapper.updateById(upd);
        fillExtra(List.of(product));
        return product;
    }

    @Override
    public List<Product> hot(int limit) {
        List<Product> list = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, Constants.PRODUCT_ON_SALE)
                .orderByDesc(Product::getViewCount)
                .last("LIMIT " + Math.max(1, Math.min(limit, 50))));
        fillExtra(list);
        return list;
    }

    @Override
    public Product create(ProductRequest request) {
        Product p = new Product();
        p.setUserId(SecurityUtils.getUserId());
        p.setCategoryId(request.getCategoryId());
        p.setTitle(request.getTitle());
        p.setDescription(request.getDescription());
        p.setPrice(request.getPrice());
        p.setImages(request.getImages());
        p.setConditionLevel(request.getConditionLevel());
        p.setStatus(Constants.PRODUCT_PENDING);
        p.setViewCount(0);
        productMapper.insert(p);
        return p;
    }

    @Override
    public Product update(Long id, ProductRequest request) {
        Product p = requireOwner(id);
        if (p.getStatus() != null && (p.getStatus() == Constants.PRODUCT_RESERVED || p.getStatus() == Constants.PRODUCT_SOLD)) {
            throw new BusinessException("交易中或已售商品不能编辑");
        }
        if (request.getCategoryId() != null) p.setCategoryId(request.getCategoryId());
        if (request.getTitle() != null) p.setTitle(request.getTitle());
        if (request.getDescription() != null) p.setDescription(request.getDescription());
        if (request.getPrice() != null) p.setPrice(request.getPrice());
        if (request.getImages() != null) p.setImages(request.getImages());
        if (request.getConditionLevel() != null) p.setConditionLevel(request.getConditionLevel());
        // 内容有变更后统一重新审核；审核拒绝或已下架的商品也可修改后重新提交。
        p.setStatus(Constants.PRODUCT_PENDING);
        productMapper.updateById(p);
        return productMapper.selectById(id);
    }

    @Override
    public void delete(Long id) {
        Product p = requireOwner(id);
        if (p.getStatus() != null && p.getStatus() == Constants.PRODUCT_RESERVED) {
            throw new BusinessException("交易中的商品不能下架，请先取消订单");
        }
        p.setStatus(Constants.PRODUCT_OFF);
        productMapper.updateById(p);
    }

    @Override
    public Page<Product> mine(int page, int size, Integer status) {
        Long uid = SecurityUtils.getUserId();
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<Product>().eq(Product::getUserId, uid);
        if (status != null) {
            qw.eq(Product::getStatus, status);
        }
        qw.orderByDesc(Product::getCreateTime);
        Page<Product> result = productMapper.selectPage(new Page<>(page, size), qw);
        fillExtra(result.getRecords());
        return result;
    }

    @Override
    public void audit(Long id, AuditRequest request) {
        Product p = productMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("商品不存在");
        }
        Integer st = request.getStatus();
        if (st == null || (st != Constants.PRODUCT_ON_SALE && st != Constants.PRODUCT_REJECTED)
                || p.getStatus() == null || p.getStatus() != Constants.PRODUCT_PENDING) {
            throw new BusinessException("非法商品状态");
        }
        p.setStatus(st);
        productMapper.updateById(p);
    }

    @Override
    public Page<Product> adminPage(int page, int size, String keyword, Integer status) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<>();
        if (status != null) {
            qw.eq(Product::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            qw.like(Product::getTitle, keyword);
        }
        qw.orderByDesc(Product::getCreateTime);
        Page<Product> result = productMapper.selectPage(new Page<>(page, size), qw);
        fillExtra(result.getRecords());
        return result;
    }

    private Product requireOwner(Long id) {
        Product p = productMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("商品不存在");
        }
        if (!Objects.equals(p.getUserId(), SecurityUtils.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new BusinessException(403, "无权操作该商品");
        }
        return p;
    }

    private void fillExtra(List<Product> list) {
        if (list == null || list.isEmpty()) return;
        Set<Long> userIds = list.stream().map(Product::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> catIds = list.stream().map(Product::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        Map<Long, Category> catMap = catIds.isEmpty() ? Map.of() :
                categoryMapper.selectBatchIds(catIds).stream().collect(Collectors.toMap(Category::getId, c -> c, (a, b) -> a));
        Long currentUid = null;
        try {
            currentUid = SecurityUtils.getUserId();
        } catch (Exception ignored) {
        }
        Set<Long> favIds = Set.of();
        if (currentUid != null) {
            List<Long> pids = list.stream().map(Product::getId).collect(Collectors.toList());
            favIds = favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>()
                            .eq(Favorite::getUserId, currentUid).in(Favorite::getProductId, pids))
                    .stream().map(Favorite::getProductId).collect(Collectors.toSet());
        }
        for (Product p : list) {
            User u = userMap.get(p.getUserId());
            if (u != null) p.setSellerNickname(u.getNickname());
            Category c = p.getCategoryId() == null ? null : catMap.get(p.getCategoryId());
            if (c != null) p.setCategoryName(c.getName());
            p.setFavorited(favIds.contains(p.getId()));
        }
    }

    @Override
    public void setStatus(Long id, Integer status) {
        Product p = productMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("商品不存在");
        }
        if (status == null || status != Constants.PRODUCT_OFF || p.getStatus() == Constants.PRODUCT_RESERVED) {
            throw new BusinessException("只能下架非交易中的商品");
        }
        p.setStatus(status);
        productMapper.updateById(p);
    }

}
