package com.example.ordersystem.ordering.dtos;

import com.example.ordersystem.ordering.entity.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class OrderingListResDto {
    private Long orderId;
    private String memberEmail;
    private String orderStatus;
    private List<OrderDetailResDto> orderDetails;


}
