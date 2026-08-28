package com.example.saas.mapper;

import com.example.saas.domain.Category;
import com.example.saas.dto.CategoryResponse;

import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName());
    }
}
