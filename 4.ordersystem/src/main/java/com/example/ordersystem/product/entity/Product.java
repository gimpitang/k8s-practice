package com.example.ordersystem.product.entity;

import com.example.ordersystem.common.entity.BaseTimeEntity;
import com.example.ordersystem.member.dtos.MemberResDto;
import com.example.ordersystem.member.entity.Member;
import com.example.ordersystem.product.dtos.ProductResDto;
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
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private Integer price;
    private Integer stockQuantity;
    private String imagePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;


    public ProductResDto fromEntity() {
        return ProductResDto.builder()
                .id(this.id)
                .name(this.name)
                .category(this.category)
                .price(this.price)
                .stockQuantity(this.stockQuantity)
                .imagePath(this.imagePath)
                .build();
    }

    public void updateImagePath(String imagePath) {
        this.imagePath = imagePath;

    }

    public void updateStockQuantity(int stockQuantity) {
        this.stockQuantity = this.stockQuantity-stockQuantity;
    }

    public void cancelOrder(int stockQuantity) {
        this.stockQuantity = this.stockQuantity + stockQuantity;
    }
}
