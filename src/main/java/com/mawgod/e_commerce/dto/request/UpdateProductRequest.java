package com.mawgod.e_commerce.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record UpdateProductRequest(

        @Size(max = 255)
        String name,

        @Size(max = 5000)
        String description,

        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        @Min(0)
        Integer stockQuantity,

        Long categoryId,

        List<String> imageUrls,

        Boolean active
) {}
