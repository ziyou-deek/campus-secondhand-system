package com.campus.secondhand.controller;

import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.ReviewRequest;
import com.campus.secondhand.entity.Review;
import com.campus.secondhand.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 前端兼容路径 /api/comments（实际评价表为 review） */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final ReviewService reviewService;

    @PostMapping
    public Result<Review> create(@Validated @RequestBody ReviewRequest request) {
        return Result.ok(reviewService.create(request));
    }

    @GetMapping
    public Result<List<Review>> list(@RequestParam Long productId) {
        return Result.ok(reviewService.listByProduct(productId));
    }

    @GetMapping("/product/{productId}")
    public Result<List<Review>> byProduct(@PathVariable Long productId) {
        return Result.ok(reviewService.listByProduct(productId));
    }
}
