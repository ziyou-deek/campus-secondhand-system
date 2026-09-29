package com.campus.secondhand.service;

import com.campus.secondhand.common.BusinessException;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.dto.ProductRequest;
import com.campus.secondhand.mapper.CategoryMapper;
import com.campus.secondhand.mapper.FavoriteMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.campus.secondhand.security.LoginUser;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    @Mock private ProductMapper productMapper;
    @Mock private UserMapper userMapper;
    @Mock private CategoryMapper categoryMapper;
    @Mock private FavoriteMapper favoriteMapper;

    @Test
    void anonymousVisitorCannotReadPendingProductById() {
        SecurityContextHolder.clearContext();
        Product product = new Product();
        product.setId(10L);
        product.setStatus(Constants.PRODUCT_PENDING);
        product.setUserId(1L);
        product.setCategoryId(1L);
        when(productMapper.selectById(10L)).thenReturn(product);
        ProductServiceImpl service = new ProductServiceImpl(productMapper, userMapper, categoryMapper, favoriteMapper);

        assertThrows(BusinessException.class, () -> service.getById(10L));
        verify(productMapper, never()).updateById(any(Product.class));
    }

    @Test
    void rejectedProductReturnsToPendingAfterEdit() {
        Product product = new Product();
        product.setId(11L);
        product.setUserId(2L);
        product.setStatus(Constants.PRODUCT_REJECTED);
        when(productMapper.selectById(11L)).thenReturn(product);
        User seller = new User();
        seller.setId(2L);
        seller.setUsername("seller");
        seller.setRole("USER");
        seller.setStatus(1);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new LoginUser(seller), null, Collections.emptyList()));
        try {
            ProductRequest request = new ProductRequest();
            request.setTitle("修改后的商品");
            new ProductServiceImpl(productMapper, userMapper, categoryMapper, favoriteMapper).update(11L, request);
            assertEquals(Constants.PRODUCT_PENDING, product.getStatus());
            verify(productMapper).updateById(product);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
