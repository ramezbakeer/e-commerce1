package com.mawgod.e_commerce.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CartResponse(
        Long id,
        Long userId,
        String sessionId,
        List<CartItemResponse> items,
        BigDecimal totalAmount,
        int totalItems,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
