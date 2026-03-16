package com.mawgod.e_commerce.dto.response;

import com.mawgod.e_commerce.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        String userEmail,
        OrderStatus status,
        BigDecimal totalAmount,
        ShippingAddressResponse shippingAddress,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
