package com.mawgod.e_commerce.controller;

import com.mawgod.e_commerce.dto.request.AddCartItemRequest;
import com.mawgod.e_commerce.dto.request.UpdateCartItemRequest;
import com.mawgod.e_commerce.dto.response.CartResponse;
import com.mawgod.e_commerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Cart endpoints.
 *
 * Identity resolution (temporary — until Spring Security is enabled in Issue 8):
 *   - Authenticated users  →  pass X-User-Id header
 *   - Guest / anonymous    →  pass X-Session-Id header
 *
 * After Issue 8, replace header extraction with SecurityContextHolder.
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * GET /api/v1/cart
     * Returns the current cart for the caller.
     */
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestHeader(value = "X-User-Id",    required = false) Long userId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {

        CartResponse cart = (userId != null)
                ? cartService.getCartForUser(userId)
                : cartService.getCartForSession(requireSession(sessionId));

        return ResponseEntity.ok(cart);
    }

    /**
     * POST /api/v1/cart/items
     * Adds a product to the cart (or increments quantity if already present).
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @RequestHeader(value = "X-User-Id",    required = false) Long userId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @Valid @RequestBody AddCartItemRequest request) {

        return ResponseEntity.ok(cartService.addItem(userId, sessionId, request));
    }

    /**
     * PATCH /api/v1/cart/items/{itemId}
     * Updates the quantity of an existing cart line.
     */
    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            @PathVariable Long itemId,
            @RequestHeader(value = "X-User-Id",    required = false) Long userId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        return ResponseEntity.ok(cartService.updateItem(itemId, userId, sessionId, request));
    }

    /**
     * DELETE /api/v1/cart/items/{itemId}
     * Removes a single line from the cart.
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable Long itemId,
            @RequestHeader(value = "X-User-Id",    required = false) Long userId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {

        return ResponseEntity.ok(cartService.removeItem(itemId, userId, sessionId));
    }

    /**
     * DELETE /api/v1/cart
     * Clears all items from the cart.
     */
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestHeader(value = "X-User-Id",    required = false) Long userId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {

        cartService.clearCart(userId, sessionId);
        return ResponseEntity.noContent().build();
    }

    // ---- helpers ----

    private String requireSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException(
                    "Either X-User-Id or X-Session-Id header must be provided");
        }
        return sessionId;
    }
}
