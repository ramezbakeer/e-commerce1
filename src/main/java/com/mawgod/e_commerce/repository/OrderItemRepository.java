package com.mawgod.e_commerce.repository;

import com.mawgod.e_commerce.entity.Order;
import com.mawgod.e_commerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder(Order order);

    List<OrderItem> findByOrderId(Long orderId);
}
