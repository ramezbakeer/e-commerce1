package com.mawgod.e_commerce.repository;

import com.mawgod.e_commerce.entity.Cart;
import com.mawgod.e_commerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCart(Cart cart);

    List<CartItem> findByCartId(Long cartId);

    void deleteByCartId(Long cartId);
}
