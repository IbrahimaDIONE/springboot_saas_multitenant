package com.example.saas.service;

import com.example.saas.dto.*;

import java.util.*;

public interface CategoryService {
    List<CategoryResponse> findAll();

    CategoryResponse create(CategoryRequest r);

    CategoryResponse update(UUID id, CategoryRequest r);

    void delete(UUID id);
}
