package com.example.product_service_redis.api;

import com.example.product_service_redis.domain.CacheMode;
import com.example.product_service_redis.domain.ProductService;
import com.example.product_service_redis.domain.db.ProductEntity;
import com.example.product_service_redis.domain.service.DbProductService;
import com.example.product_service_redis.domain.service.ManualCachingProductService;
import com.example.product_service_redis.domain.service.SpringAnnotationProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final DbProductService dbProductService;
    private final ManualCachingProductService manualCachingProductService;
    private final SpringAnnotationProductService springAnnotationProductService;
    private final ProductDtoMapper mapper;

    @PostMapping
    public ResponseEntity<ProductDto> create(
            @RequestBody ProductCreateRequest request,
            @RequestParam(value = "cacheMode", defaultValue = "NONE_CACHE") CacheMode cacheMode
    ) {
        log.info("Creating product with cacheMode={}", cacheMode);

        ProductService service = resolveProductService(cacheMode);

        ProductEntity productEntity = service.create(request);
        ProductDto productDto = mapper.toProductDto(productEntity);

        return ResponseEntity.status(HttpStatus.CREATED).body(productDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getById(
            @PathVariable Long id,
            @RequestParam(value = "cacheMode", defaultValue = "NONE_CACHE") CacheMode cacheMode
    ) {
        log.info("Getting product {} with cacheMode={} ", id, cacheMode);

        ProductService service = resolveProductService(cacheMode);

        ProductEntity productEntity = service.getById(id);
        ProductDto productDto = mapper.toProductDto(productEntity);

        return ResponseEntity.ok(productDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(
            @PathVariable Long id,
            @RequestBody ProductUpdateRequest request,
            @RequestParam(value = "cacheMode", defaultValue = "NONE_CACHE") CacheMode cacheMode
    ) {
        log.info("Updating product {} with cacheMode={}", id, cacheMode);

        ProductService service = resolveProductService(cacheMode);

        ProductEntity productEntity = service.update(id, request);
        ProductDto productDto = mapper.toProductDto(productEntity);

        return ResponseEntity.ok(productDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam(value = "cacheMode", defaultValue = "NONE_CACHE") CacheMode cacheMode
    ) {
        log.info("Deleting product {} with cacheMode={}", id, cacheMode);

        ProductService service = resolveProductService(cacheMode);

        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ProductService resolveProductService(CacheMode cacheMode) {
        return switch (cacheMode) {
            case NONE_CACHE -> dbProductService;
            case MANUAL -> manualCachingProductService;
            case SPRING -> springAnnotationProductService;
        };
    }

}
