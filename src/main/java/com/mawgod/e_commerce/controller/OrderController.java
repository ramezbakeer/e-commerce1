package com.mawgod.e_commerce.controller;

import com.mawgod.e_commerce.dto.request.CheckoutRequest;
import com.mawgod.e_commerce.dto.response.OrderResponse;
import com.mawgod.e_commerce.dto.response.PageResponse;
import com.mawgod.e_commerce.security.SecurityUtils;
import com.mawgod.e_commerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Order / checkout endpoints. All routes require authentication (enforced by SecurityConfig).
 * User identity is resolved from the JWT via SecurityUtils — no manual headers needed.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Checkout and order history — requires auth")
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/v1/orders/checkout
     * Places an order from the caller's cart. Returns 201 Created.
     */
    @PostMapping("/checkout")
    @Operation(summary = "Place an order from the current cart", security = @SecurityRequirement(name = "Bearer Auth"))
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        OrderResponse order = orderService.checkout(userId, null, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * GET /api/v1/orders
     * Returns paginated order history for the current user (newest first).
     * Query params: page (default 0), size (default 10, max 50).
     */
    @GetMapping
    @Operation(summary = "List order history (paginated)", security = @SecurityRequirement(name = "Bearer Auth"))
    public ResponseEntity<PageResponse<OrderResponse>> getOrders(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(
                page, Math.min(size, 50), Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(
                orderService.getOrdersForUser(SecurityUtils.getCurrentUserId(), pageable));
    }

    /**
     * GET /api/v1/orders/{id}
     * Returns a single order with all line items. 404 if not owned by the caller.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a single order by ID", security = @SecurityRequirement(name = "Bearer Auth"))
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(
                orderService.getOrderById(id, SecurityUtils.getCurrentUserId()));
    }
}
