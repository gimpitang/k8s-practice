package com.example.ordersystem.ordering.service;

import com.example.ordersystem.common.config.FeignTokenConfig;
import com.example.ordersystem.ordering.dtos.ProductDto;
import com.example.ordersystem.ordering.dtos.ProductUpdateStockDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

//로컬에서 테스트 할 때를 위해 유레카에서 쓰는 name을 남겨둠.
//name은 유레카의 서비스명이고, url 은 쿠버네티스의 서비스명임.
@FeignClient(name ="product-service", url="http://yj-msa-product-service", configuration = FeignTokenConfig.class)
public interface ProductFeign {

    @GetMapping(value = "/product/{id}")
    ProductDto getProductById(@PathVariable Long id);

    @PutMapping(value = "/product/updatestock")
        void updateProductStock(@RequestBody ProductUpdateStockDto dto);

}
