package com.example.ordersystem.ordering.entity;

import com.example.ordersystem.common.entity.BaseTimeEntity;
import com.example.ordersystem.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Builder
public class OrderDetail extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name ="ordering_id")
    private Ordering ordering;

    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;



}
