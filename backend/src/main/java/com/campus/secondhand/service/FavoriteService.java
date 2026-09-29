package com.campus.secondhand.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.entity.Favorite;

public interface FavoriteService {
    void add(Long productId);
    void remove(Long productId);
    boolean isFavorited(Long productId);
    Page<Favorite> mine(int page, int size);
}
