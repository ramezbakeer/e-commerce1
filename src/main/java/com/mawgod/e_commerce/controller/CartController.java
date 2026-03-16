package com.mawgod.e_commerce.controller;

import com.mawgod.e_commerce.dto.request.AddCartItemRequest;
import com.mawgod.e_commerce.dto.request.UpdateCartItemRequest;
import com.mawgod.e_commerce.dto.response.CartResponse;
import com.mawgod.e_commerce.security.SecurityUtils;
import com.mawgod.e_commerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Cart endpoints.
 *
 * Identity resolution (post Issue 8):
 *   - Authenticated users  → userId extracted from JWT via SecurityUtils
 *   - Anonymous guests     → pass X-Session-Id header (cart GET is public)
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * GET /api/v1/cart
     * Public: authenticated users get their user cart,
     * guests get/create a session cart via X-Session-Id.
     */
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            Authentication authentication,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {

        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(
                    cartService.getCartForUser(SecurityUtils.getCurrentUserId()));
        }
        return ResponseEntity.ok(
                cartService.getCartForSession(requireSession(sessionId)));
    }

    /**
     * POST /api/v1/cart/items
     * Requires authentication. Adds a product (or increments if already present).
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(
                cartService.addItem(SecurityUtils.getCurrentUserId(), null, request));
    }

    /**
     * PATCH /api/v1/cart/items/{itemId}
     * Requires authentication. Updates quantity of a line item.
     */
    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(
                cartService.updateItem(itemId, SecurityUtils.getCurrentUserId(), null, request));
    }

    /**
     * DELETE /api/v1/cart/items/{itemId}
     * Requires authentication. Removes a single line.
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(
                cartService.removeItem(itemId, SecurityUtils.getCurrentUserId(), null));
    }

    /**
     * DELETE /api/v1/cart
     * Requires authentication. Clears all items.
     */
    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart(SecurityUtils.getCurrentUserId(), null);
        return ResponseEntity.noContent().build();
    }

    // ---- helpers ----

    private String requireSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException(
                    "Unauthenticated requests must provide an X-Session-Id header");
        }
        return sessionId;
    }
}
