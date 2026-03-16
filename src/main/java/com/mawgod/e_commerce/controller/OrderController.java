package com.mawgod.e_commerce.controller;

import com.mawgod.e_commerce.dto.request.CheckoutRequest;
import com.mawgod.e_commerce.dto.response.OrderResponse;
import com.mawgod.e_commerce.dto.response.PageResponse;
import com.mawgod.e_commerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Order / checkout endpoints.
 *
 * Identity resolution (temporary — until Spring Security is enabled in Issue 8):
 *   - Pass X-User-Id header to identify the current user.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/v1/orders/checkout
     * Places an order from the caller's current cart.
     * Returns 201 Created with the new order representation.
     */
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @RequestHeader(value = "X-User-Id",    required = false) Long userId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @Valid @RequestBody CheckoutRequest request) {

        OrderResponse order = orderService.checkout(userId, sessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * GET /api/v1/orders
     * Lists all orders for the current user, newest first (paginated).
     *
     * Query params: page (default 0), size (default 10)
     */
    @GetMapping
    public ResponseEntity<PageResponse<OrderResponse>> getOrders(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        return ResponseEntity.ok(orderService.getOrdersForUser(userId, pageable));
    }

    /**
     * GET /api/v1/orders/{id}
     * Returns a single order detail including all line items.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(orderService.getOrderById(id, userId));
    }
}
