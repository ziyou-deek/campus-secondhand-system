package com.campus.secondhand.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.ProductRequest;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/api/products")
    public Result<Page<Product>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String sort) {
        return Result.ok(productService.page(page, size, keyword, categoryId, status, sort));
    }

    @GetMapping("/api/products/hot")
    public Result<List<Product>> hot(@RequestParam(defaultValue = "10") int limit) {
        return Result.ok(productService.hot(limit));
    }

    @GetMapping("/api/products/mine")
    public Result<Page<Product>> mine(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return Result.ok(productService.mine(page, size, status));
    }

    @GetMapping("/api/products/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.ok(productService.getById(id));
    }

    @PostMapping("/api/products")
    public Result<Product> create(@Validated @RequestBody ProductRequest request) {
        return Result.ok(productService.create(request));
    }

    @PutMapping("/api/products/{id}")
    public Result<Product> update(@PathVariable Long id, @RequestBody ProductRequest request) {
        return Result.ok(productService.update(id, request));
    }

    @DeleteMapping("/api/products/{id}")
    public Result<?> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.ok("已下架", null);
    }
}
