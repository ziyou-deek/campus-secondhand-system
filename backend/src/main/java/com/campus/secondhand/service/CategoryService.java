package com.campus.secondhand.service;

import com.campus.secondhand.dto.CategoryRequest;
import com.campus.secondhand.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> listAll();
    Category create(CategoryRequest request);
    Category update(Long id, CategoryRequest request);
    void delete(Long id);
}
