package com.example.ordersystem.ordering.service;

import com.example.ordersystem.common.dtos.StockRabbitDto;
import com.example.ordersystem.common.service.StockInventoryService;
import com.example.ordersystem.common.service.StockRabbitmqService;
import com.example.ordersystem.ordering.controller.SseController;
import com.example.ordersystem.ordering.dtos.*;
import com.example.ordersystem.ordering.dtos.OrderingListResDto;
import com.example.ordersystem.ordering.entity.OrderDetail;
import com.example.ordersystem.ordering.entity.OrderStatus;
import com.example.ordersystem.ordering.entity.Ordering;
import com.example.ordersystem.ordering.repository.OrderingDetailRepository;
import com.example.ordersystem.ordering.repository.OrderingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final OrderingDetailRepository orderingDetailRepository;
    private final StockInventoryService stockInventoryService;
    private final StockRabbitmqService stockRabbitmqService;
    private final SseController sseController;
    private final RestTemplate restTemplate;
    private final ProductFeign productFeign;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderingService(OrderingRepository orderingRepository, OrderingDetailRepository orderingDetailRepository, StockInventoryService stockInventoryService, StockRabbitmqService stockRabbitmqService, SseController sseController, RestTemplate restTemplate, ProductFeign productFeign, KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderingRepository = orderingRepository;
        this.orderingDetailRepository = orderingDetailRepository;
        //      재고 줄여주는 로직 사용하기 위해 의존성 주입
        this.stockInventoryService = stockInventoryService;
        this.stockRabbitmqService = stockRabbitmqService;
        this.sseController = sseController;
        this.restTemplate = restTemplate;
        this.productFeign = productFeign;
        this.kafkaTemplate = kafkaTemplate;
    }


    public Ordering orderCreate(List<OrderCreateDto> dtos) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Ordering ordering = Ordering.builder()
                .memberEmail(email)
                .build();

        for (OrderCreateDto dto : dtos) {
            //      product 서버에 api요청을 통해 product 객체를 받아와야함. --> 동기적 처리 필수
            String productGetUrl = "http://yj-msa-product-service/product/" + dto.getProductId();

            // response 에 넣을 토큰 생성
            String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();

            HttpHeaders headers = new HttpHeaders();
            //     베어러 토큰 형식의 token
            headers.set("Authorization", token);
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);

            // url, 요청형식, token, 응답 형태
            ResponseEntity<ProductDto> response = restTemplate.exchange(productGetUrl, HttpMethod.GET, httpEntity, ProductDto.class);

            ProductDto productDto = response.getBody();
            System.out.println("productDto 는" + productDto);
            int quantity = dto.getProductCount();
            //      동시성 이슈 고려 안한 코드
            if (productDto.getStockQuantity() < quantity) {
                throw new IllegalArgumentException("product not enough");
            } else {
                //재고 감소 api 요청을 product서버에 보내야함. -> 비동기 처리 가능.
                String productUpdateStockUrl = "http://yj-msa-product-service/product/updatestock";
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<ProductUpdateStockDto> updateEntity = new HttpEntity<>(
                        //      body부분
                        ProductUpdateStockDto.builder()
                                .productId(dto.getProductId())
                                .productQuantity(dto.getProductCount())
                                .build()
                        , headers
                );
                restTemplate.exchange(productUpdateStockUrl, HttpMethod.PUT, updateEntity, void.class);

                OrderDetail orderDetail = OrderDetail.builder()
                        .ordering(ordering)
                        // 받아온 product 객체를 통해 id 값 세팅
                        .productId(dto.getProductId())
                        .quantity(dto.getProductCount())
                        .build();
                ordering.getOrderDetails().add(orderDetail);
            }
            orderingRepository.save(ordering);
        }


            //----공통
            return ordering;
    }

    public Ordering orderFeignKafkaCreate(List<OrderCreateDto> dtos) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Ordering ordering = Ordering.builder()
                .memberEmail(email)
                .build();

        for (OrderCreateDto dto : dtos) {
            //      product 서버에 feign 클라이언트를 통한 api 요청 조회
            ProductDto productDto = productFeign.getProductById(dto.getProductId());



            int quantity = dto.getProductCount();
            //      동시성 이슈 고려 안한 코드
            if (productDto.getStockQuantity() < quantity) {
                throw new IllegalArgumentException("product not enough");
            } else {
                //재고 감소 api 요청을 product서버에 보내야함. -> kafka 에 메시지 발행.
//                productFeign.updateProductStock(ProductUpdateStockDto.builder().productId(dto.getProductId()).productQuantity(dto.getProductCount()).build());
                ProductUpdateStockDto updateDto = ProductUpdateStockDto.builder()
                        .productId(dto.getProductId()).productQuantity(dto.getProductCount())
                        .build();
                kafkaTemplate.send("update-stock-topic",updateDto);
            }
                OrderDetail orderDetail = OrderDetail.builder()
                        .ordering(ordering)
                        // 받아온 product 객체를 통해 id 값 세팅
                        .productId(dto.getProductId())
                        .quantity(dto.getProductCount())
                        .build();
                ordering.getOrderDetails().add(orderDetail);

            orderingRepository.save(ordering);
        }


        //----공통
        return ordering;
    }

    //      동시성 처리 전
//    public Ordering orderCreate(List<OrderCreateDto> dtos){
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("Member not found"));
////        //      1. cascading 없이 db 저장 (orderingDetailRepository 필요함
////        //      Ordering 객체 생성 및 save
////        Ordering ordering = Ordering.builder()
////                .member(member)
////                .build();
////        orderingRepository.save(ordering);
////
////        //      OrderingDetail 객체 생성 및 save
////        for (OrderCreateDto dto : dtos) {
////            Product product = productRepository.findById(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("product not found"));
////            if(product.getStockQuantity() < dto.getProductCount()){
////                throw new IllegalArgumentException("product not enough");
////            }else {
////                //재고 감소 로직
////                product.updateStockQuantity(dto.getProductCount());
////            }
////            OrderDetail orderDetail = OrderDetail.builder()
////                    .ordering(ordering)
////                    .product(product)
////                    .quantity(dto.getProductCount())
////                    .build();
////            orderingDetailRepository.save(orderDetail);
////        }
//
//
//
//        //      2. cascading 사용하여 db 저장
//        //      Ordering 객체 생성하면서 OrderingDetail
//        Ordering ordering = Ordering.builder()
//                .member(member)
//                .build();
//
//        for (OrderCreateDto dto : dtos) {
//            Product product = productRepository.findById(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("product not found"));
//            if(product.getStockQuantity() < dto.getProductCount()){
//                throw new IllegalArgumentException("product not enough");
//            }else {
//                //재고 감소 로직
//                product.updateStockQuantity(dto.getProductCount());
//            }
//            OrderDetail orderDetail = OrderDetail.builder()
//                    .ordering(ordering)
//                    .product(product)
//                    .quantity(dto.getProductCount())
//                    .build();
//            ordering.getOrderDetails().add(orderDetail);
//        }
//        orderingRepository.save(ordering);
//
//        //----공통
//        return ordering;
//    }

    public List<OrderingListResDto> orderList(){

        // 나 혼자 했덩거----------------------------------------------------------
//        List<Ordering> orderings = orderingRepository.findAll();
//        List<OrderingListResDto> orderingList = new ArrayList<>();
//        for(int i =0; i< orderings.size(); i++){
//
//            OrderingListResDto orderingListDto = new OrderingListResDto();
//            orderingListDto = OrderingListResDto.builder()
//                    .orderId(orderings.get(i).getId())
//                    .memberEmail(orderings.get(i).getMember().getEmail())
//                    .orderStatus(orderings.get(i).getOrderStatus().toString())
//                    .orderDetails(orderings.get(i).)
//                    .build();
//        }
        // 나 혼자 했덩거----------------------------------------------------------
        List<Ordering> orderings = orderingRepository.findAll();
        List<OrderingListResDto> orderListResDtos = new ArrayList<>();
        for(Ordering o : orderings){

            orderListResDtos.add(o.fromEntity());
        }
        return orderListResDtos;
    }

    public List<OrderingListResDto> myOrders(){

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        List<OrderingListResDto> orderListResDtos = new ArrayList<>();
        for(Ordering o : orderingRepository.findByMemberEmail(email)){

            orderListResDtos.add(o.fromEntity());
        }
        return orderListResDtos;
    }

    public Ordering orderCancel(Long id){
        Ordering ordering = orderingRepository.findById(id).orElseThrow(()->new EntityNotFoundException("ordering not found"));
        ordering.cancelStatus();

        return ordering;
    }
}
