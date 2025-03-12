package com.example.ordersystem.ordering.controller;

import com.example.ordersystem.ordering.dtos.OrderingListResDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class SseController {
    //      사용자 연결정보를 변수로 관리
    //      ConcurrentHashMap은 Thread-safe한 HashMap(동시성 이슈 발생 x)
    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    //      사용자의 서버 연결요청을 통해 연결 정보에 등록.
    @GetMapping("/subscribe")
    public SseEmitter subscribe(){

        //      연결 객체 생성
        SseEmitter sseEmitter = new SseEmitter(14400*60*100L); //10일정도 emitter 유효시간 설정

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        emitterMap.put(email, sseEmitter);

//        sseEmitter.onTimeout(()->emitterMap.remove(email));
//        sseEmitter.onCompletion(()->emitterMap.remove(email));
        try {
            sseEmitter.send(SseEmitter.event().name("connect").data("연결 완료"));
        }catch (IOException e){
            e.printStackTrace();
        }

        return sseEmitter;
    }

    //      특정 사용자에게 message 발송
    //      orderingService에 --> 여기 최하단에 있음     public Ordering orderCreate(List<OrderCreateDto> dtos){
    public void publishMessage(OrderingListResDto dto, String email){
        SseEmitter sseEmitter = emitterMap.get(email);
        try {
            sseEmitter.send(SseEmitter.event().name("ordered").data(dto));
        }catch (IOException e){
            e.printStackTrace();
        }

    }
    @GetMapping("/unsubscribe")
    public void unSubscribe(){
        // 연결객체 생성
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        emitterMap.remove(email);
        System.out.println(emitterMap);

    }

}
