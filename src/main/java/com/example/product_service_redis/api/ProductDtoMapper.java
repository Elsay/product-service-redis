package com.example.product_service_redis.api;

import com.example.product_service_redis.domain.db.ProductEntity;
import org.springframework.stereotype.Component;

@Component
class ProductDtoMapper {

    public ProductDto toProductDto(ProductEntity productEntity) {
        return new ProductDto(
                productEntity.getId(),
                productEntity.getName(),
                productEntity.getPrice(),
                productEntity.getDescription(),
                productEntity.getCreatedAt(),
                productEntity.getUpdatedAt()
        );
    }
}
