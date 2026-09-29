package com.campus.secondhand.controller;

import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.ReviewRequest;
import com.campus.secondhand.entity.Review;
import com.campus.secondhand.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Result<Review> create(@Validated @RequestBody ReviewRequest request) {
        return Result.ok(reviewService.create(request));
    }

    @GetMapping("/product/{productId}")
    public Result<List<Review>> byProduct(@PathVariable Long productId) {
        return Result.ok(reviewService.listByProduct(productId));
    }

    @GetMapping("/user/{userId}")
    public Result<List<Review>> byUser(@PathVariable Long userId) {
        return Result.ok(reviewService.listByUser(userId));
    }
}
