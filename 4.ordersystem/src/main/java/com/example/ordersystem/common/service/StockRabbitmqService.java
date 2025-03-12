//package com.example.ordersystem.common.service;
//
//import com.example.ordersystem.common.config.RabbitmqConfig;
//import com.example.ordersystem.common.dtos.StockRabbitDto;
//import com.example.ordersystem.product.entity.Product;
//import com.example.ordersystem.product.repository.ProductRepository;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.persistence.EntityNotFoundException;
//import jakarta.persistence.Transient;
//import jakarta.transaction.Transactional;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//public class StockRabbitmqService {
//
//    private final RabbitTemplate template;
//    private final ProductRepository productRepository;
//
//    public StockRabbitmqService(RabbitTemplate template, ProductRepository productRepository) {
//        this.template = template;
//        this.productRepository = productRepository;
//    }
//
//    //      mq에 rdb 동기화 관련 메시지를 발행하는 코드(발행하는코드)
//    public void publish(StockRabbitDto dto){
//        template.convertAndSend(RabbitmqConfig.STOCK_DECREASE_QUEUE, dto);
//    }
//
//    //      mq에 저장된 메시지를 소비하여 rdb에 동기화 (읽어주는 코드, queue를 계속 지켜보는 코드)
//    //      listener는 publish와는 독립적으로 동작하기 때문에, 비동기적으로 실행.
//    //      한 트랜젝션이 완료된 이후에 그 다음 메시지를 수신하므로, 동시이슈가 발생하지 않는다.
//    @RabbitListener(queues = RabbitmqConfig.STOCK_DECREASE_QUEUE)
//    @Transactional      // 비동기적으로 처리하기 위함
//    public void subscribe(Message message) throws JsonProcessingException {
//        //      {"productId:1, "productCount":3"}
//        String messageBody = new String(message.getBody());
//        ObjectMapper objectMapper = new ObjectMapper();
//        StockRabbitDto dto = objectMapper.readValue(messageBody, StockRabbitDto.class);
//        //      아래 product에서 updateStockQuantity 메서드를 꺼내기 위함.
//        Product product = productRepository.findById(dto.getProductId())
//                .orElseThrow(()-> new EntityNotFoundException("product not found"));
//        //      더티체킹을 위해 메서드단에서 @Transactional진행
//        product.updateStockQuantity(dto.getProductCount());
//    }
//}
