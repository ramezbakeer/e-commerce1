package com.mawgod.e_commerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(

        @NotBlank(message = "Category name is required")
        @Size(max = 255)
        String name,

        @NotBlank(message = "Slug is required")
        @Size(max = 255)
        String slug,

        @Size(max = 1000)
        String description,

        Long parentId
) {}
