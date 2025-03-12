package com.example.ordersystem.ordering.controller;

import com.example.ordersystem.ordering.dtos.OrderCreateDto;
import com.example.ordersystem.ordering.dtos.OrderingListResDto;
import com.example.ordersystem.ordering.entity.Ordering;
import com.example.ordersystem.ordering.service.OrderingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordering")
public class OrderingController {
    private final OrderingService orderingService;


    public OrderingController(OrderingService orderingService) {
        this.orderingService = orderingService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> orderCreate(@RequestBody List<OrderCreateDto> dtos) {
        Ordering ordering= orderingService.orderCreate(dtos);
        return new ResponseEntity<>(ordering.getId(), HttpStatus.OK);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> orderList() {
        List<OrderingListResDto> orderingListDtoList = orderingService.orderList();
        return new ResponseEntity<>(orderingListDtoList, HttpStatus.OK);

    }
    @GetMapping("/myorders")
    public ResponseEntity<?> orderMyOrders() {
        List<OrderingListResDto> orderingListResDtos = orderingService.myOrders();
        return new ResponseEntity<>(orderingListResDtos, HttpStatus.OK);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> orderCancel(@PathVariable Long id) {//     여기서 id는 ordering의 id 값임
        Ordering ordering = orderingService.orderCancel(id);
        return new ResponseEntity<>(ordering.getId(), HttpStatus.OK);
    }


}
