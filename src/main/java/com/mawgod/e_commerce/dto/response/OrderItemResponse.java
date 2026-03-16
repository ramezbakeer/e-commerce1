package com.mawgod.e_commerce.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        String productName,
        BigDecimal priceAtOrder,
        Integer quantity,
        BigDecimal subtotal
) {}
