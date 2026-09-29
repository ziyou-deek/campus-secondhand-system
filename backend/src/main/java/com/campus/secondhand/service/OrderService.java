package com.campus.secondhand.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.dto.OrderRequest;
import com.campus.secondhand.entity.TradeOrder;

public interface OrderService {
    TradeOrder create(OrderRequest request);
    Page<TradeOrder> mine(int page, int size, String role, Integer status);
    TradeOrder getById(Long id);
    TradeOrder updateStatus(Long id, Integer status);
}
