package com.mawgod.e_commerce.mappers;

import com.mawgod.e_commerce.dto.response.CartItemResponse;
import com.mawgod.e_commerce.dto.response.CartResponse;
import com.mawgod.e_commerce.entity.Cart;
import com.mawgod.e_commerce.entity.CartItem;
import com.mawgod.e_commerce.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CartMapper {

    public CartResponse toResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        BigDecimal total = itemResponses.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        return new CartResponse(
                cart.getId(),
                cart.getUser() != null ? cart.getUser().getId() : null,
                cart.getSessionId(),
                itemResponses,
                total,
                totalItems,
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }

    public CartItemResponse toItemResponse(CartItem item) {
        Product product = item.getProduct();
        BigDecimal subtotal = product.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        String imageUrl = (product.getImageUrls() != null && !product.getImageUrls().isEmpty())
                ? product.getImageUrls().get(0)
                : null;

        return new CartItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getPrice(),
                item.getQuantity(),
                subtotal,
                imageUrl,
                item.getAddedAt()
        );
    }
}
