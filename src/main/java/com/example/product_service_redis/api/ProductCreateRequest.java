package com.example.product_service_redis.api;

import java.math.BigDecimal;

public record ProductCreateRequest(
        String name,
        BigDecimal price,
        String description
) {
}
