package com.mawgod.e_commerce.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductRequest(

        @NotBlank(message = "Product name is required")
        @Size(max = 255)
        String name,

        @NotBlank(message = "Slug is required")
        @Size(max = 255)
        String slug,

        @Size(max = 5000)
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        @NotBlank(message = "SKU is required")
        @Size(max = 100)
        String sku,

        @Min(0)
        Integer stockQuantity,

        Long categoryId,

        List<String> imageUrls,

        Boolean active
) {}
