package com.mawgod.e_commerce.service;

import com.mawgod.e_commerce.dto.request.CheckoutRequest;
import com.mawgod.e_commerce.dto.response.OrderResponse;
import com.mawgod.e_commerce.dto.response.PageResponse;
import com.mawgod.e_commerce.entity.*;
import com.mawgod.e_commerce.exception.EmptyCartException;
import com.mawgod.e_commerce.exception.InsufficientStockException;
import com.mawgod.e_commerce.exception.ResourceNotFoundException;
import com.mawgod.e_commerce.mappers.OrderMapper;
import com.mawgod.e_commerce.repository.CartRepository;
import com.mawgod.e_commerce.repository.OrderRepository;
import com.mawgod.e_commerce.repository.ProductRepository;
import com.mawgod.e_commerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final CartService cartService;

    // ------------------------------------------------------------------ //
    //  Checkout                                                            //
    // ------------------------------------------------------------------ //

    /**
     * Creates an order from the current cart, deducts stock, and clears the cart.
     * Runs in a single transaction so partial failures roll back completely.
     */
    @Transactional
    public OrderResponse checkout(Long userId, String sessionId, CheckoutRequest request) {
        // 1. Resolve cart
        Cart cart = cartService.resolveCart(userId, sessionId);

        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException();
        }

        // 2. Resolve user (required for an order)
        User user = resolveUser(userId, cart);

        // 3. Validate stock and build order items (snapshot prices)
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            // Re-fetch for up-to-date stock inside the transaction
            product = productRepository.findById(product.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", product.getId()));

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                        product.getName(), cartItem.getQuantity(), product.getStockQuantity());
            }

            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            orderItems.add(OrderItem.builder()
                    .productName(product.getName())
                    .priceAtOrder(product.getPrice())
                    .quantity(cartItem.getQuantity())
                    .subtotal(subtotal)
                    .build());

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            total = total.add(subtotal);
        }

        // 4. Build and persist order
        ShippingAddress shippingAddress = ShippingAddress.builder()
                .street(request.street())
                .city(request.city())
                .state(request.state())
                .postalCode(request.postalCode())
                .country(request.country())
                .build();

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(total)
                .shippingAddress(shippingAddress)
                .items(new ArrayList<>())
                .build();

        Order savedOrder = orderRepository.save(order);

        // 5. Attach order items (need order id first)
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
        }
        savedOrder.getItems().addAll(orderItems);
        savedOrder = orderRepository.save(savedOrder);

        // 6. Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        return orderMapper.toResponse(savedOrder);
    }

    // ------------------------------------------------------------------ //
    //  Query orders                                                        //
    // ------------------------------------------------------------------ //

    public PageResponse<OrderResponse> getOrdersForUser(Long userId, Pageable pageable) {
        ensureUserExists(userId);
        Page<Order> page = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return toPageResponse(page);
    }

    public OrderResponse getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Ensure order belongs to requesting user
        if (!order.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Order", "id", orderId);
        }

        return orderMapper.toResponse(order);
    }

    // ------------------------------------------------------------------ //
    //  Internal helpers                                                    //
    // ------------------------------------------------------------------ //

    /**
     * Returns the user associated with the userId, or — for guest carts —
     * the user linked to the cart (if any). Throws if no user can be resolved.
     */
    private User resolveUser(Long userId, Cart cart) {
        if (userId != null) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        }
        if (cart.getUser() != null) {
            return cart.getUser();
        }
        throw new IllegalStateException(
                "Guest checkout requires a registered user. Please log in before placing an order.");
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
    }

    private PageResponse<OrderResponse> toPageResponse(Page<Order> page) {
        return new PageResponse<>(
                page.getContent().stream().map(orderMapper::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
