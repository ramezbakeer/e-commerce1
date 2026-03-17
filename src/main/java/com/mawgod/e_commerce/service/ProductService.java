package com.mawgod.e_commerce.service;

import com.mawgod.e_commerce.dto.request.CreateProductRequest;
import com.mawgod.e_commerce.dto.request.UpdateProductRequest;
import com.mawgod.e_commerce.dto.response.PageResponse;
import com.mawgod.e_commerce.dto.response.ProductResponse;
import com.mawgod.e_commerce.entity.Category;
import com.mawgod.e_commerce.entity.Product;
import com.mawgod.e_commerce.exception.DuplicateResourceException;
import com.mawgod.e_commerce.exception.ResourceNotFoundException;
import com.mawgod.e_commerce.mappers.ProductMapper;
import com.mawgod.e_commerce.repository.CategoryRepository;
import com.mawgod.e_commerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public PageResponse<ProductResponse> getFilteredProducts(
            Long categoryId,
            String categorySlug,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String search,
            Pageable pageable) {

        // Normalise: empty string → null so JPQL :param IS NULL checks work
        String searchTerm = (search != null && !search.isBlank()) ? search.trim() : null;

        Page<Product> page;

        if (categorySlug != null && !categorySlug.isBlank()) {
            page = productRepository.findByCategorySlugWithFilters(
                    categorySlug, minPrice, maxPrice, searchTerm, pageable);
        } else {
            // categoryId may be null — the query handles that
            if (categoryId != null && !categoryRepository.existsById(categoryId)) {
                throw new ResourceNotFoundException("Category", "id", categoryId);
            }
            page = productRepository.findWithFilters(
                    categoryId, minPrice, maxPrice, searchTerm, pageable);
        }

        return toPageResponse(page);
    }

    public ProductResponse getById(Long id) {
        return productMapper.toResponse(findOrThrow(id));
    }

    public ProductResponse getBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
        return productMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        if (productRepository.existsBySlug(request.slug())) {
            throw new DuplicateResourceException("Product", "slug", request.slug());
        }
        if (productRepository.existsBySku(request.sku())) {
            throw new DuplicateResourceException("Product", "sku", request.sku());
        }

        Product.ProductBuilder builder = Product.builder()
                .name(request.name())
                .slug(request.slug())
                .description(request.description())
                .price(request.price())
                .sku(request.sku())
                .stockQuantity(request.stockQuantity() != null ? request.stockQuantity() : 0)
                .active(request.active() != null ? request.active() : true)
                .imageUrls(request.imageUrls() != null ? new ArrayList<>(request.imageUrls()) : new ArrayList<>());

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.categoryId()));
            builder.category(category);
        }

        return productMapper.toResponse(productRepository.save(builder.build()));
    }

    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = findOrThrow(id);

        if (request.name() != null)          product.setName(request.name());
        if (request.description() != null)   product.setDescription(request.description());
        if (request.price() != null)         product.setPrice(request.price());
        if (request.stockQuantity() != null) product.setStockQuantity(request.stockQuantity());
        if (request.active() != null)        product.setActive(request.active());
        if (request.imageUrls() != null)     product.setImageUrls(new ArrayList<>(request.imageUrls()));

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.categoryId()));
            product.setCategory(category);
        }

        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        productRepository.deleteById(id);
    }

    // ---- helpers ----

    private Product findOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    private PageResponse<ProductResponse> toPageResponse(Page<Product> page) {
        return new PageResponse<>(
                page.getContent().stream().map(productMapper::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
