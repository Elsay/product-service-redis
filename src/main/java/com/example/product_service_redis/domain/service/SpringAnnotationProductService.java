package com.example.product_service_redis.domain.service;

import com.example.product_service_redis.api.ProductCreateRequest;
import com.example.product_service_redis.api.ProductUpdateRequest;
import com.example.product_service_redis.domain.ProductService;
import com.example.product_service_redis.domain.db.ProductEntity;
import com.example.product_service_redis.domain.db.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpringAnnotationProductService implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductEntity create(ProductCreateRequest createRequest) {
        log.info("Creating product in DB: {}", createRequest.name());
        ProductEntity product = ProductEntity.builder()
                .name(createRequest.name())
                .price(createRequest.price())
                .description(createRequest.description())
                .build();
        return productRepository.save(product);
    }

    @Cacheable(
            value = "product",
            key = "#id"
    )
    @Override
    public ProductEntity getById(Long id) {
        log.info("Getting product from DB: id={}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
    }

    @CacheEvict(
            value = "product",
            key = "#id"
    )
    @Override
    public ProductEntity update(Long id, ProductUpdateRequest updateRequest) {
        log.info("Updating product in DB: id={}", id);

        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        if (updateRequest.price() != null) {
            product.setPrice(updateRequest.price());
        }

        if (updateRequest.description() != null) {
            product.setDescription(updateRequest.description());
        }
        return productRepository.save(product);
    }

    @CacheEvict(
            value = "product",
            key = "#id"
    )
    @Override
    public void delete(Long id) {
        log.info("Deleting product from DB: id={}", id);
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found: id=" + id);
        }
        productRepository.deleteById(id);
    }

}
