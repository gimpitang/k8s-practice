package com.example.ordersystem.ordering.service;

//import com.example.ordersystem.common.dtos.StockRabbitDto;
import com.example.ordersystem.common.service.StockInventoryService;
//import com.example.ordersystem.common.service.StockRabbitmqService;
import com.example.ordersystem.ordering.controller.SseController;
import com.example.ordersystem.ordering.dtos.*;
import com.example.ordersystem.member.entity.Member;
import com.example.ordersystem.member.repository.MemberRepository;
import com.example.ordersystem.ordering.dtos.OrderingListResDto;
import com.example.ordersystem.ordering.entity.OrderDetail;
import com.example.ordersystem.ordering.entity.OrderStatus;
import com.example.ordersystem.ordering.entity.Ordering;
import com.example.ordersystem.ordering.repository.OrderingDetailRepository;
import com.example.ordersystem.ordering.repository.OrderingRepository;
import com.example.ordersystem.product.entity.Product;
import com.example.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final MemberRepository memberRepository;
    private final OrderingDetailRepository orderingDetailRepository;
    private final ProductRepository productRepository;
    private final StockInventoryService stockInventoryService;
//    private final StockRabbitmqService stockRabbitmqService;
    private final SseController sseController;

    public OrderingService(OrderingRepository orderingRepository, MemberRepository memberRepository, OrderingDetailRepository orderingDetailRepository, ProductRepository productRepository, StockInventoryService stockInventoryService, SseController sseController) {
        this.orderingRepository = orderingRepository;
        this.memberRepository = memberRepository;
        this.orderingDetailRepository = orderingDetailRepository;
        this.productRepository = productRepository;
        //      재고 줄여주는 로직 사용하기 위해 의존성 주입
        this.stockInventoryService = stockInventoryService;
//        this.stockRabbitmqService = stockRabbitmqService;
        this.sseController = sseController;
    }


    public Ordering orderCreate(List<OrderCreateDto> dtos){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("Member not found"));

        Ordering ordering = Ordering.builder()
                .member(member)
                .build();

        for (OrderCreateDto dto : dtos) {
            Product product = productRepository.findById(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("product not found"));
            int quantity = dto.getProductCount();
            if(product.getStockQuantity() < dto.getProductCount()){
                throw new IllegalArgumentException("product not enough");
            }else {
                product.updateStockQuantity(dto.getProductCount());
            }
            OrderDetail orderDetail = OrderDetail.builder()
                    .ordering(ordering)
                    .product(product)
                    .quantity(dto.getProductCount())
                    .build();
            ordering.getOrderDetails().add(orderDetail);
        }
        Ordering ordering1 = orderingRepository.save(ordering);

        //      sse를 통한 admin계정에 메시지 발송
        sseController.publishMessage(ordering1.fromEntity(),"admin@naver.com");

        //----공통
        return ordering;
    }

    public List<OrderingListResDto> orderList(){

        List<Ordering> orderings = orderingRepository.findAll();
        List<OrderingListResDto> orderListResDtos = new ArrayList<>();
        for(Ordering o : orderings){

            orderListResDtos.add(o.fromEntity());
        }
        return orderListResDtos;
    }

    public List<OrderingListResDto> myOrders(){

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("Member not found"));
        List<Ordering> orderings = member.getOrderingList();
        List<OrderingListResDto> orderListResDtos = new ArrayList<>();
        for(Ordering o : orderings){

            orderListResDtos.add(o.fromEntity());
        }
        return orderListResDtos;

    }

    public Ordering orderCancel(Long id){
        Ordering ordering = orderingRepository.findById(id).orElseThrow(()->new EntityNotFoundException("ordering not found"));
        ordering.cancelStatus();
        for(OrderDetail orderDetail : ordering.getOrderDetails()){
//            int count = orderDetail.getProduct().getStockQuantity() + orderDetail.getQuantity();
            orderDetail.getProduct().cancelOrder(orderDetail.getQuantity());
        }
        return ordering;
    }
}
