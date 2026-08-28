package com.example.product_service_redis.domain;

import com.example.product_service_redis.api.ProductCreateRequest;
import com.example.product_service_redis.api.ProductUpdateRequest;
import com.example.product_service_redis.domain.db.ProductEntity;

public interface ProductService
{
    ProductEntity create(ProductCreateRequest createRequest);
    ProductEntity getById(Long id);
    ProductEntity update(Long id, ProductUpdateRequest updateRequest);
    void delete(Long id);
}
