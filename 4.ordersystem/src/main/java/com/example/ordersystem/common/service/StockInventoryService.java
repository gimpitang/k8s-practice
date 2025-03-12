package com.example.ordersystem.common.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;



@Service
//@Transactional --> rdb에 접근하는 경우
public class StockInventoryService {
    @Qualifier("stockinventory")
    private final RedisTemplate<String, String> redisTemplate;

    public StockInventoryService(@Qualifier("stockinventory") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    //      상품 등록,취소 시 increaseStock
    public int increaseStock(Long productId, int quantity) {
        String remainsObject = redisTemplate.opsForValue().get(String.valueOf(productId));
        if(remainsObject != null) {
            int remains = Integer.parseInt(remainsObject);
            if(remains < 0) {
                redisTemplate.opsForValue().set(String.valueOf(productId), "0");
            }

        }

            Long newRemains= redisTemplate.opsForValue().increment(String.valueOf(productId), quantity);
        return newRemains.intValue();
    }

    //      주문 시 decreaseStock

    public int decreaseStock(Long productId, int quantity) {
        //      먼저 조회 후에 재고 감소가 가능할 때 decrease
        String remainsObject = redisTemplate.opsForValue().get(String.valueOf(productId));
        int remains = Integer.parseInt(remainsObject);
        if(remains < quantity) {
            return -1;
        }else {
            Long finalRemains = redisTemplate.opsForValue().decrement(String.valueOf(productId),quantity);
        return finalRemains.intValue();
        }
    }

}
