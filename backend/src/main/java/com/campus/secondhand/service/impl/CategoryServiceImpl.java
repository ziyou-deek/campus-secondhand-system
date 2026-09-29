package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.secondhand.common.BusinessException;
import com.campus.secondhand.dto.CategoryRequest;
import com.campus.secondhand.entity.Category;
import com.campus.secondhand.mapper.CategoryMapper;
import com.campus.secondhand.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<Category> listAll() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort).orderByAsc(Category::getId));
    }

    @Override
    public Category create(CategoryRequest request) {
        Category c = new Category();
        c.setName(request.getName());
        c.setSort(request.getSort() != null ? request.getSort() : 0);
        categoryMapper.insert(c);
        return c;
    }

    @Override
    public Category update(Long id, CategoryRequest request) {
        Category c = categoryMapper.selectById(id);
        if (c == null) {
            throw new BusinessException("分类不存在");
        }
        if (request.getName() != null) {
            c.setName(request.getName());
        }
        if (request.getSort() != null) {
            c.setSort(request.getSort());
        }
        categoryMapper.updateById(c);
        return c;
    }

    @Override
    public void delete(Long id) {
        categoryMapper.deleteById(id);
    }
}
