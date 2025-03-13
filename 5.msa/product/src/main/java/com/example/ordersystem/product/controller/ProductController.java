package com.example.ordersystem.product.controller;

import com.example.ordersystem.product.dtos.ProductRegisterDto;
import com.example.ordersystem.product.dtos.ProductResDto;
import com.example.ordersystem.product.dtos.ProductSearchDto;
import com.example.ordersystem.product.dtos.ProductUpdateStockDto;
import com.example.ordersystem.product.entity.Product;
import com.example.ordersystem.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> productCreate( ProductRegisterDto dto) {
        Product product = productService.productCreate(dto);
        return new ResponseEntity<>(product.getId(), HttpStatus.CREATED);
    }

    @GetMapping("/list")
    //      paging과 list를 위해 매개변수 2개 필요함.
    public ResponseEntity<?> productList(Pageable pageable, ProductSearchDto dto) {
        Page<ProductResDto> productResDtos = productService.findAll(pageable,dto);

        return new ResponseEntity<>(productResDtos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> productDetail(@PathVariable Long id){
        ProductResDto productResDto = productService.productDetail(id);


        return new ResponseEntity<>(productResDto, HttpStatus.OK);
    }

    @PutMapping("/updatestock")
    //  한 건의 주문에 여러 아이템의 주문이 들어가는데 건건이 조회를 해야하는 로직이라 좀 비효율 적이긴함.
    public ResponseEntity<?> productStockUpdate (@RequestBody ProductUpdateStockDto dto){
        Product product = productService.updateStockQuantity(dto);
        return new ResponseEntity<>(product.getId(),HttpStatus.OK);
    }

}
