package com.campus.secondhand.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.OrderRequest;
import com.campus.secondhand.dto.OrderStatusRequest;
import com.campus.secondhand.entity.TradeOrder;
import com.campus.secondhand.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<TradeOrder> create(@Validated @RequestBody OrderRequest request) {
        return Result.ok(orderService.create(request));
    }

    @GetMapping("/mine")
    public Result<Page<TradeOrder>> mine(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        return Result.ok(orderService.mine(page, size, role, status));
    }

    @GetMapping("/{id}")
    public Result<TradeOrder> detail(@PathVariable Long id) {
        return Result.ok(orderService.getById(id));
    }

    @PutMapping("/{id}/status")
    public Result<TradeOrder> updateStatus(@PathVariable Long id, @Validated @RequestBody OrderStatusRequest request) {
        return Result.ok(orderService.updateStatus(id, request.getStatus()));
    }
}
