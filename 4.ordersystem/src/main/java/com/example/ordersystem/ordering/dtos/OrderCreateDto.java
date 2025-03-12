package com.example.ordersystem.ordering.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
//@Builder 필요 없을 수 있음
public class OrderCreateDto {
    private Long productId;         // 제품 번호
    private int productCount;       // 주문 수량
}
