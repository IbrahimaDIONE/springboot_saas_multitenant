package com.example.saas.mapper;

import com.example.saas.domain.Product;
import com.example.saas.dto.ProductResponse;

import org.springframework.stereotype.Component;

/** SRP : mapping domaine -> API sans accès à la base ni règle métier. */
@Component
public class ProductMapper {
    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getImageUrl(),
                new com.example.saas.dto.CategoryResponse(
                        product.getCategory().getId(), product.getCategory().getName()),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
