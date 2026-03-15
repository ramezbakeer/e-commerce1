package com.mawgod.e_commerce.repository;

import com.mawgod.e_commerce.entity.Order;
import com.mawgod.e_commerce.entity.OrderStatus;
import com.mawgod.e_commerce.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Order> findByStatus(OrderStatus status);
}
