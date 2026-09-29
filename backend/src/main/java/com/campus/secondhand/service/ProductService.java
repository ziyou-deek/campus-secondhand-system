package com.campus.secondhand.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.dto.AuditRequest;
import com.campus.secondhand.dto.ProductRequest;
import com.campus.secondhand.entity.Product;

import java.util.List;

public interface ProductService {
    Page<Product> page(int page, int size, String keyword, Long categoryId, Integer status, String sort);
    Product getById(Long id);
    List<Product> hot(int limit);
    Product create(ProductRequest request);
    Product update(Long id, ProductRequest request);
    void delete(Long id);
    Page<Product> mine(int page, int size, Integer status);
    void audit(Long id, AuditRequest request);
    Page<Product> adminPage(int page, int size, String keyword, Integer status);
    void setStatus(Long id, Integer status);
}
