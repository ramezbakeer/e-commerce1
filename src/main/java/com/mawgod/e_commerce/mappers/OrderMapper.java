package com.mawgod.e_commerce.mappers;

import com.mawgod.e_commerce.dto.response.OrderItemResponse;
import com.mawgod.e_commerce.dto.response.OrderResponse;
import com.mawgod.e_commerce.dto.response.ShippingAddressResponse;
import com.mawgod.e_commerce.entity.Order;
import com.mawgod.e_commerce.entity.OrderItem;
import com.mawgod.e_commerce.entity.ShippingAddress;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getEmail(),
                order.getStatus(),
                order.getTotalAmount(),
                toAddressResponse(order.getShippingAddress()),
                itemResponses,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductName(),
                item.getPriceAtOrder(),
                item.getQuantity(),
                item.getSubtotal()
        );
    }

    private ShippingAddressResponse toAddressResponse(ShippingAddress address) {
        if (address == null) return null;
        return new ShippingAddressResponse(
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry()
        );
    }
}
