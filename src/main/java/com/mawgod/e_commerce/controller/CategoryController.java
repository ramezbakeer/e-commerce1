package com.mawgod.e_commerce.controller;

import com.mawgod.e_commerce.dto.request.CreateCategoryRequest;
import com.mawgod.e_commerce.dto.response.CategoryResponse;
import com.mawgod.e_commerce.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category tree — public reads, admin writes")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * GET /api/v1/categories
     * Returns the full category tree (root categories with their children).
     *
     * Use ?flat=true to get a flat list (no nesting).
     */
    @GetMapping
    @Operation(summary = "Get category tree or flat list")
    public ResponseEntity<List<CategoryResponse>> getCategories(
            @RequestParam(defaultValue = "false") boolean flat) {

        List<CategoryResponse> result = flat
                ? categoryService.getAllFlat()
                : categoryService.getCategoryTree();

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/categories/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<CategoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    /**
     * GET /api/v1/categories/slug/{slug}
     */
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug")
    public ResponseEntity<CategoryResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getBySlug(slug));
    }

    /**
     * POST /api/v1/categories
     */
    @PostMapping
    @Operation(summary = "Create a category", security = @SecurityRequirement(name = "Bearer Auth"))
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse created = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * DELETE /api/v1/categories/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category", security = @SecurityRequirement(name = "Bearer Auth"))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
