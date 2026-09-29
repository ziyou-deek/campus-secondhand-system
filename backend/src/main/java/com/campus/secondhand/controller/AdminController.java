package com.campus.secondhand.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.AuditRequest;
import com.campus.secondhand.dto.CategoryRequest;
import com.campus.secondhand.dto.StatusRequest;
import com.campus.secondhand.dto.PasswordResetResponse;
import com.campus.secondhand.entity.Category;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.service.CategoryService;
import com.campus.secondhand.service.ProductService;
import com.campus.secondhand.service.StatsService;
import com.campus.secondhand.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final StatsService statsService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.ok(statsService.stats());
    }

    @GetMapping("/users")
    public Result<Page<User>> users(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(userService.pageUsers(page, size, keyword));
    }

    @PutMapping("/users/{id}/status")
    public Result<?> userStatus(@PathVariable Long id, @Validated @RequestBody StatusRequest req) {
        userService.updateStatus(id, req.getStatus());
        return Result.ok("更新成功", null);
    }

    @PutMapping("/users/{id}/reset-password")
    public Result<PasswordResetResponse> resetPassword(@PathVariable Long id) {
        // 临时密码只在本次响应中返回；日志及数据库不保存明文。
        return Result.ok(userService.resetPassword(id));
    }

    @GetMapping("/products")
    public Result<Page<Product>> products(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(productService.adminPage(page, size, keyword, status));
    }

    @PutMapping("/products/{id}/audit")
    public Result<?> auditPut(@PathVariable Long id, @Validated @RequestBody AuditRequest req) {
        productService.audit(id, req);
        return Result.ok("审核完成", null);
    }

    @PostMapping("/products/{id}/audit")
    public Result<?> auditPost(@PathVariable Long id, @Validated @RequestBody AuditRequest req) {
        productService.audit(id, req);
        return Result.ok("审核完成", null);
    }

    @GetMapping("/categories")
    public Result<List<Category>> categories() {
        return Result.ok(categoryService.listAll());
    }

    @PostMapping("/categories")
    public Result<Category> createCategory(@Validated @RequestBody CategoryRequest req) {
        return Result.ok(categoryService.create(req));
    }

    @PutMapping("/categories/{id}")
    public Result<Category> updateCategory(@PathVariable Long id, @RequestBody CategoryRequest req) {
        return Result.ok(categoryService.update(id, req));
    }

    @DeleteMapping("/categories/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.ok("删除成功", null);
    }

    @GetMapping("/violations")
    public Result<Page<Product>> violations(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Integer st = status != null ? status : 4;
        return Result.ok(productService.adminPage(page, size, null, st));
    }

    @PutMapping("/violations/{id}")
    public Result<?> handleViolation(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object st = body.get("status");
        int status = st == null ? 2 : Integer.valueOf(st.toString());
        productService.setStatus(id, status);
        return Result.ok("处理完成", null);
    }
}
