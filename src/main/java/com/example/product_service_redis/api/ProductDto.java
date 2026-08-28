package com.example.product_service_redis.api;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductDto(
        Long id,
        String name,
        BigDecimal price,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
