package com.mawgod.e_commerce.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        Long parentId,
        List<CategoryResponse> children,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
