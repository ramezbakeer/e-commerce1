package com.mawgod.e_commerce.service;

import com.mawgod.e_commerce.dto.request.CreateCategoryRequest;
import com.mawgod.e_commerce.dto.response.CategoryResponse;
import com.mawgod.e_commerce.entity.Category;
import com.mawgod.e_commerce.exception.DuplicateResourceException;
import com.mawgod.e_commerce.exception.ResourceNotFoundException;
import com.mawgod.e_commerce.mappers.CategoryMapper;
import com.mawgod.e_commerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Returns all root categories with their full child tree.
     */
    public List<CategoryResponse> getCategoryTree() {
        return categoryRepository.findByParentIsNull().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    /**
     * Returns all categories as a flat list (no children populated).
     */
    public List<CategoryResponse> getAllFlat() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toFlatResponse)
                .toList();
    }

    public CategoryResponse getById(Long id) {
        Category category = findOrThrow(id);
        return categoryMapper.toResponse(category);
    }

    public CategoryResponse getBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new DuplicateResourceException("Category", "slug", request.slug());
        }

        Category.CategoryBuilder builder = Category.builder()
                .name(request.name())
                .slug(request.slug())
                .description(request.description());

        if (request.parentId() != null) {
            Category parent = findOrThrow(request.parentId());
            builder.parent(parent);
        }

        Category saved = categoryRepository.save(builder.build());
        return categoryMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", "id", id);
        }
        categoryRepository.deleteById(id);
    }

    // ---- helpers ----

    private Category findOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }
}
