package com.mawgod.e_commerce.service;

import com.mawgod.e_commerce.dto.request.AddCartItemRequest;
import com.mawgod.e_commerce.dto.request.UpdateCartItemRequest;
import com.mawgod.e_commerce.dto.response.CartResponse;
import com.mawgod.e_commerce.entity.Cart;
import com.mawgod.e_commerce.entity.CartItem;
import com.mawgod.e_commerce.entity.Product;
import com.mawgod.e_commerce.entity.User;
import com.mawgod.e_commerce.exception.InsufficientStockException;
import com.mawgod.e_commerce.exception.ResourceNotFoundException;
import com.mawgod.e_commerce.mappers.CartMapper;
import com.mawgod.e_commerce.repository.CartItemRepository;
import com.mawgod.e_commerce.repository.CartRepository;
import com.mawgod.e_commerce.repository.ProductRepository;
import com.mawgod.e_commerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    // ------------------------------------------------------------------ //
    //  Get cart                                                            //
    // ------------------------------------------------------------------ //

    public CartResponse getCartForUser(Long userId) {
        Cart cart = getOrCreateCartForUser(userId);
        return cartMapper.toResponse(cart);
    }

    public CartResponse getCartForSession(String sessionId) {
        Cart cart = getOrCreateCartForSession(sessionId);
        return cartMapper.toResponse(cart);
    }

    // ------------------------------------------------------------------ //
    //  Add item                                                            //
    // ------------------------------------------------------------------ //

    @Transactional
    public CartResponse addItem(Long userId, String sessionId, AddCartItemRequest request) {
        Cart cart = resolveCart(userId, sessionId);
        Product product = findProductOrThrow(request.productId());

        validateStock(product, request.quantity());

        // If product already in cart → increment quantity
        cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> {
                            int newQty = existing.getQuantity() + request.quantity();
                            validateStock(product, newQty);
                            existing.setQuantity(newQty);
                            cartItemRepository.save(existing);
                        },
                        () -> {
                            CartItem newItem = CartItem.builder()
                                    .cart(cart)
                                    .product(product)
                                    .quantity(request.quantity())
                                    .build();
                            cart.getItems().add(newItem);
                            cartItemRepository.save(newItem);
                        }
                );

        return cartMapper.toResponse(cartRepository.save(cart));
    }

    // ------------------------------------------------------------------ //
    //  Update item quantity                                                //
    // ------------------------------------------------------------------ //

    @Transactional
    public CartResponse updateItem(Long cartItemId, Long userId, String sessionId,
                                   UpdateCartItemRequest request) {
        CartItem item = findCartItemOrThrow(cartItemId);
        verifyCartOwnership(item.getCart(), userId, sessionId);
        validateStock(item.getProduct(), request.quantity());

        item.setQuantity(request.quantity());
        cartItemRepository.save(item);

        return cartMapper.toResponse(item.getCart());
    }

    // ------------------------------------------------------------------ //
    //  Remove item                                                         //
    // ------------------------------------------------------------------ //

    @Transactional
    public CartResponse removeItem(Long cartItemId, Long userId, String sessionId) {
        CartItem item = findCartItemOrThrow(cartItemId);
        Cart cart = item.getCart();
        verifyCartOwnership(cart, userId, sessionId);

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        return cartMapper.toResponse(cartRepository.save(cart));
    }

    // ------------------------------------------------------------------ //
    //  Clear cart                                                          //
    // ------------------------------------------------------------------ //

    @Transactional
    public void clearCart(Long userId, String sessionId) {
        Cart cart = resolveCart(userId, sessionId);
        cart.getItems().clear();
        cartItemRepository.deleteByCartId(cart.getId());
        cartRepository.save(cart);
    }

    // ------------------------------------------------------------------ //
    //  Internal helpers                                                    //
    // ------------------------------------------------------------------ //

    /** Resolves a cart from user id OR session id — user takes precedence. */
    Cart resolveCart(Long userId, String sessionId) {
        if (userId != null) {
            return getOrCreateCartForUser(userId);
        }
        if (sessionId != null && !sessionId.isBlank()) {
            return getOrCreateCartForSession(sessionId);
        }
        throw new IllegalArgumentException("Either userId or sessionId must be provided");
    }

    private Cart getOrCreateCartForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().user(user).build()));
    }

    private Cart getOrCreateCartForSession(String sessionId) {
        return cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().sessionId(sessionId).build()));
    }

    private CartItem findCartItemOrThrow(Long cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));
    }

    private Product findProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }

    private void validateStock(Product product, int requestedQty) {
        if (product.getStockQuantity() < requestedQty) {
            throw new InsufficientStockException(
                    product.getName(), requestedQty, product.getStockQuantity());
        }
    }

    private void verifyCartOwnership(Cart cart, Long userId, String sessionId) {
        boolean ownerUser    = userId != null
                && cart.getUser() != null
                && cart.getUser().getId().equals(userId);
        boolean ownerSession = sessionId != null
                && sessionId.equals(cart.getSessionId());
        if (!ownerUser && !ownerSession) {
            throw new ResourceNotFoundException("CartItem", "id", "not found in your cart");
        }
    }
}
