package com.mawgod.e_commerce.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String slug,
        String description,
        BigDecimal price,
        String sku,
        Integer stockQuantity,
        Boolean active,
        Long categoryId,
        String categoryName,
        List<String> imageUrls,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
