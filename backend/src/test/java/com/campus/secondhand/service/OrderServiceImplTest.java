package com.campus.secondhand.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.campus.secondhand.common.BusinessException;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.dto.OrderRequest;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.TradeOrder;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.TradeOrderMapper;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.security.LoginUser;
import com.campus.secondhand.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    @Mock private TradeOrderMapper orderMapper;
    @Mock private ProductMapper productMapper;
    @Mock private UserMapper userMapper;
    private OrderServiceImpl service;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), "test"), Product.class);
        service = new OrderServiceImpl(orderMapper, productMapper, userMapper);
        User buyer = new User();
        buyer.setId(2L);
        buyer.setRole(Constants.ROLE_USER);
        buyer.setStatus(Constants.USER_STATUS_NORMAL);
        LoginUser principal = new LoginUser(buyer);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void concurrentReservationFailureDoesNotCreateAnotherOrder() {
        Product product = new Product();
        product.setId(10L);
        product.setUserId(1L);
        product.setStatus(Constants.PRODUCT_ON_SALE);
        product.setPrice(new BigDecimal("25.00"));
        when(productMapper.selectById(10L)).thenReturn(product);
        when(productMapper.update(isNull(), any(Wrapper.class))).thenReturn(0);
        OrderRequest request = new OrderRequest();
        request.setProductId(10L);

        assertThrows(BusinessException.class, () -> service.create(request));
        verify(orderMapper, never()).insert(any(TradeOrder.class));
    }

    @Test
    void buyerConfirmationKeepsOrderTradingUntilSellerConfirms() {
        TradeOrder order = new TradeOrder();
        order.setId(8L);
        order.setProductId(10L);
        order.setBuyerId(2L);
        order.setSellerId(1L);
        order.setStatus(Constants.ORDER_TRADING);
        order.setBuyerConfirmed(false);
        order.setSellerConfirmed(false);
        when(orderMapper.selectForUpdate(8L)).thenReturn(order);
        when(productMapper.selectBatchIds(any())).thenReturn(Collections.emptyList());
        when(userMapper.selectBatchIds(any())).thenReturn(Collections.emptyList());

        TradeOrder result = service.updateStatus(8L, Constants.ORDER_DONE);

        assertEquals(Constants.ORDER_TRADING, result.getStatus());
        assertTrue(result.getBuyerConfirmed());
        verify(productMapper, never()).update(isNull(), any(Wrapper.class));
    }
}
