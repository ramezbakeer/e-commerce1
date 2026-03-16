package com.mawgod.e_commerce.mappers;

import com.mawgod.e_commerce.dto.response.CategoryResponse;
import com.mawgod.e_commerce.entity.Category;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return toResponse(category, true);
    }

    public CategoryResponse toResponse(Category category, boolean includeChildren) {
        List<CategoryResponse> children = null;
        if (includeChildren && category.getChildren() != null) {
            children = category.getChildren().stream()
                    .map(child -> toResponse(child, true))
                    .toList();
        }

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getParent() != null ? category.getParent().getId() : null,
                children,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    /** Flat response without children (for list views) */
    public CategoryResponse toFlatResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getParent() != null ? category.getParent().getId() : null,
                null,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
