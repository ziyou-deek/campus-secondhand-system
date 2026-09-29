package com.campus.secondhand.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.entity.Favorite;
import com.campus.secondhand.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{productId}")
    public Result<?> add(@PathVariable Long productId) {
        favoriteService.add(productId);
        return Result.ok("收藏成功", null);
    }

    @DeleteMapping("/{productId}")
    public Result<?> remove(@PathVariable Long productId) {
        favoriteService.remove(productId);
        return Result.ok("取消收藏", null);
    }

    @GetMapping("/check/{productId}")
    public Result<Map<String, Boolean>> check(@PathVariable Long productId) {
        return Result.ok(Map.of("favorited", favoriteService.isFavorited(productId)));
    }

    @GetMapping
    public Result<Page<Favorite>> mine(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(favoriteService.mine(page, size));
    }
}
