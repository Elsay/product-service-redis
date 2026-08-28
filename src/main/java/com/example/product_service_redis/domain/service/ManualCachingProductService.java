package com.example.product_service_redis.domain.service;

import com.example.product_service_redis.api.ProductCreateRequest;
import com.example.product_service_redis.api.ProductUpdateRequest;
import com.example.product_service_redis.domain.ProductService;
import com.example.product_service_redis.domain.db.ProductEntity;
import com.example.product_service_redis.domain.db.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManualCachingProductService implements ProductService {

    private final ProductRepository productRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_KEY_PREFIX = "product:";

    @Override
    public ProductEntity create(ProductCreateRequest createRequest) {
        log.info("Creating product in DB: {}", createRequest);
        ProductEntity product = ProductEntity.builder()
                .name(createRequest.name())
                .price(createRequest.price())
                .description(createRequest.description())
                .build();
        return productRepository.save(product);
    }

    @Override
    public ProductEntity getById(Long id) {
        log.info("Getting product: id={}", id);
        var cacheKey = CACHE_KEY_PREFIX + id;

        String objectFromCache = stringRedisTemplate.opsForValue().get(cacheKey);

        if (objectFromCache != null) {
            log.info("Product found in cache: id={}", id);
            return objectMapper.readValue(objectFromCache, ProductEntity.class);
        }

        log.info("Product not found in cache: id={}", id);
        ProductEntity entityFromDb = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));

        stringRedisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(entityFromDb));
        log.info("Product cached: id={}", id);

        return entityFromDb;
    }

    @Override
    public ProductEntity update(Long id, ProductUpdateRequest updateRequest) {
        log.info("Updating product in DB: {}", id);

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

    @Override
    public void delete(Long id) {
        log.info("Deleting product {} in DB", id);
        productRepository.deleteById(id);
    }

}
