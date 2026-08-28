package com.example.product_service_redis.api;

import java.math.BigDecimal;

public record ProductUpdateRequest(
        BigDecimal price,
        String description
) {
}
