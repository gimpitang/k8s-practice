package com.example.ordersystem.ordering.entity;

import com.example.ordersystem.common.entity.BaseTimeEntity;
import com.example.ordersystem.member.entity.Member;
import com.example.ordersystem.ordering.dtos.OrderDetailResDto;
import com.example.ordersystem.ordering.dtos.OrderingListResDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Builder

public class Ordering extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus= OrderStatus.ORDERED;

//    @OneToMany(mappedBy = "ordering", cascade = CascadeType.PERSIST)
//    private List<OrderDetail> orderDetails ;

    @OneToMany(mappedBy = "ordering", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<OrderDetail> orderDetails = new ArrayList<>();

    public OrderingListResDto fromEntity() {
        List<OrderDetailResDto> orderDetailResDtos = new ArrayList<>();
        for(OrderDetail od : this.getOrderDetails()){
            OrderDetailResDto orderDetailResDto = OrderDetailResDto.builder()
                    .detailId(od.getId())
                    .productName(od.getProduct().getName())
                    .count(od.getQuantity())
                    .build();
            orderDetailResDtos.add(orderDetailResDto);
        }
        OrderingListResDto orderDto = OrderingListResDto
                .builder()
                .orderId(this.getId())
                .memberEmail(this.getMember().getEmail())
                .orderStatus(this.getOrderStatus().toString())
                .orderDetails(orderDetailResDtos)
                .build();
        return orderDto;
    }

    public void cancelStatus() {
        this.orderStatus = OrderStatus.CANCELED;
    }



}
