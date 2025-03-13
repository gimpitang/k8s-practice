package com.example.ordersystem.product.service;

import com.example.ordersystem.common.service.StockInventoryService;
import com.example.ordersystem.product.dtos.ProductRegisterDto;
import com.example.ordersystem.product.dtos.ProductResDto;
import com.example.ordersystem.product.dtos.ProductSearchDto;
import com.example.ordersystem.product.dtos.ProductUpdateStockDto;
import com.example.ordersystem.product.entity.Product;
import com.example.ordersystem.product.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;


@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final StockInventoryService stockInventoryService;


    public ProductService(ProductRepository productRepository,  StockInventoryService stockInventoryService) {
        this.productRepository = productRepository;
        this.stockInventoryService = stockInventoryService;
    }

    public Product productCreate(ProductRegisterDto dto) {
        // try {

        //     //      member 조회
        //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //     //rdb 에 재고 값 추가
        //     Product product = productRepository.save(dto.toEntity(authentication.getName()));
        //     //redis에도 재고 값 추가!
        //     stockInventoryService.increaseStock(product.getId(),dto.getStockQuantity());

        //     //      aws에 image 저장 후에 url 추출
        //     //      aws에 s3 접근 가능한  iam(새끼계정)계정 생성 iam계정을 통해 aws에 접근 가능한 접근 객체 생성(config에 AwsS3Config)
        //     MultipartFile image = dto.getProductImage();
        //     byte[] bytes = image.getBytes();
        //     String fileName = product.getId() + "_" + image.getOriginalFilename();
        //     //      먼저 local에 저장
        //     Path path = Paths.get("C:/Users/Playdata/Desktop/tmp" , fileName);
        //     Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        //     return product;
        // }catch (IOException e){
        //     //      redis는 트랜잭션의 대상이 아니므로, 에러시 별도의 decrease 작업이 필요함.
        //     throw new RuntimeException("이미지 저장 실패");
        // }
        return null;
    }
    public Page<ProductResDto> findAll(Pageable pageable, ProductSearchDto searchDto) {

        //      검색을 위해 Specification 객체 사용
        //      Specification 객체는 복잡한 쿼리를 명세를 이용하여 정의하는 방식으로, 쿼리를 쉽게 생성
        Specification<Product> specification = new Specification<Product>() { // 검색처리를 위함

            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                //      root: entity의 속성을 접근하기 위한 객체.
                //      criteriaBuilder: query를 생성하기 위한 객체
                List<Predicate> predicates = new ArrayList<>(); //Predicate에 쿼리를 하나씩 담을 예정임
                if(searchDto.getCategory() != null){
                    predicates.add(criteriaBuilder.equal(root.get("category"), searchDto.getCategory()));
                }
                if(searchDto.getProductName() != null){
                    //      root.get("name")은 컬럼명(엔티티), getProductName()은 dto에 있는 값
                    predicates.add(criteriaBuilder.like(root.get("name"), "%" + searchDto.getProductName() + "%"));
                }
                Predicate[] predicateArr = new Predicate[predicates.size()];
                for(int i=0; i<predicates.size(); i++){
                    predicateArr[i] = predicates.get(i);
                }
                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };
        Page<Product> productList = productRepository.findAll(specification, pageable);
        return productList.map(p->p.fromEntity());
    }

    public ProductResDto productDetail(Long id){
        Product product = productRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("없는 아이디 입니다."));
        return product.fromEntity();
    }

    public Product updateStockQuantity(ProductUpdateStockDto dto){
        Product product = productRepository.findById(dto.getProductId()).orElseThrow(()-> new EntityNotFoundException("없는 상품"));
        product.updateStockQuantity(dto.getProductQuantity());
        return product;
    }

    @KafkaListener(topics = "update-stock-topic", groupId = "product-group", containerFactory = "kafkaListener")
    public void productConsumer(String message){
        System.out.println("Consumer message received : " + message);
        ObjectMapper objectMapper = new ObjectMapper();

        try {

            ProductUpdateStockDto dto = objectMapper.readValue(message, ProductUpdateStockDto.class);
            this.updateStockQuantity(dto);

        }catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }
    }
}
