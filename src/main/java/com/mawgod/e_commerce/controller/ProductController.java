package com.mawgod.e_commerce.controller;

import com.mawgod.e_commerce.dto.request.CreateProductRequest;
import com.mawgod.e_commerce.dto.request.UpdateProductRequest;
import com.mawgod.e_commerce.dto.response.PageResponse;
import com.mawgod.e_commerce.dto.response.ProductResponse;
import com.mawgod.e_commerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/v1/products
     * Returns paginated active products.
     *
     * Query params:
     *   page       – 0-based page index (default 0)
     *   size       – page size (default 20)
     *   sort       – field,direction  e.g. price,asc  or  name,desc  (default createdAt,desc)
     *   categoryId – filter by category
     */
    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "20")  int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) Long categoryId) {

        Pageable pageable = buildPageable(page, size, sort);

        PageResponse<ProductResponse> result = (categoryId != null)
                ? productService.getByCategory(categoryId, pageable)
                : productService.getActiveProducts(pageable);

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    /**
     * GET /api/v1/products/slug/{slug}
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getBySlug(slug));
    }

    /**
     * POST /api/v1/products
     */
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse created = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PATCH /api/v1/products/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    /**
     * DELETE /api/v1/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- helpers ----

    private Pageable buildPageable(int page, int size, String sort) {
        String[] parts = sort.split(",");
        String field     = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return PageRequest.of(page, Math.min(size, 100), Sort.by(dir, field));
    }
}
