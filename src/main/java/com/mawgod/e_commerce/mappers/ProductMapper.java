package com.mawgod.e_commerce.mappers;

import com.mawgod.e_commerce.dto.response.ProductResponse;
import com.mawgod.e_commerce.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getPrice(),
                product.getSku(),
                product.getStockQuantity(),
                product.getActive(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getImageUrls(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
