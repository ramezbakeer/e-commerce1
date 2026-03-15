package com.mawgod.e_commerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

/**
 * Line item of an order. Stores a snapshot of product name and price at order
 * time; does not reference Product so historical orders remain correct if
 * products change.
 */
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_order", columnList = "order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull
    @Size(max = 255)
    @Column(name = "product_name", nullable = false)
    private String productName;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 10, fraction = 2)
    @Column(name = "price_at_order", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAtOrder;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 12, fraction = 2)
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;
}
