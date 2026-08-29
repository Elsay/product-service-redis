package com.example.product_service_redis.domain.service;

import com.example.product_service_redis.api.ProductCreateRequest;
import com.example.product_service_redis.api.ProductUpdateRequest;
import com.example.product_service_redis.domain.ProductService;
import com.example.product_service_redis.domain.db.ProductEntity;
import com.example.product_service_redis.domain.db.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManualCachingProductService implements ProductService {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, ProductEntity> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "product:";

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

    @Override
    public ProductEntity getById(Long id) {
        log.info("Getting product: id={}", id);
        String cacheKey = CACHE_KEY_PREFIX + id;

        ProductEntity productFromCache = redisTemplate.opsForValue().get(cacheKey);

        if (productFromCache != null) {
            log.info("Product found in cache: id={}", id);
            return productFromCache;
        }

        log.info("Product not found in cache: id={}", id);
        ProductEntity productFromDb = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));

        redisTemplate.opsForValue().set(cacheKey, productFromDb);
        log.info("Product cached: id={}", id);

        return productFromDb;
    }

    @Override
    public ProductEntity update(Long id, ProductUpdateRequest updateRequest) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: id=" + id));

        if (updateRequest.price() != null) {
            product.setPrice(updateRequest.price());
        }

        if (updateRequest.description() != null) {
            product.setDescription(updateRequest.description());
        }

        String cacheKey = CACHE_KEY_PREFIX + id;
        redisTemplate.delete(cacheKey);
        log.info("Cache invalidated for product being updated: id={}", id);

        log.info("Updating product in DB: id={}", id);
        return productRepository.save(product);
    }

    @Override
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found: id=" + id);
        }

        String cacheKey = CACHE_KEY_PREFIX + id;
        redisTemplate.delete(cacheKey);
        log.info("Cache invalidated for product being deleted: id={}", id);

        log.info("Deleting product from DB: id={}", id);
        productRepository.deleteById(id);
    }

}
