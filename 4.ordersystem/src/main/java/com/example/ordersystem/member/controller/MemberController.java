package com.example.ordersystem.member.controller;

import com.example.ordersystem.common.auth.JwtTokenProvider;
import com.example.ordersystem.member.dtos.LoginDto;
import com.example.ordersystem.member.dtos.MemberRefreshDto;
import com.example.ordersystem.member.dtos.MemberResDto;
import com.example.ordersystem.member.dtos.MemberSaveReqDto;
import com.example.ordersystem.member.entity.Member;
import com.example.ordersystem.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @Qualifier("rtdb")
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider,@Qualifier("rtdb") RedisTemplate<String, Object> redisTemplate) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/create")
    public ResponseEntity<?> memberCreate(@Valid @RequestBody MemberSaveReqDto memberSaveReqDto) {
        Long memberId= memberService.save(memberSaveReqDto);
        return new ResponseEntity<>(memberId,HttpStatus.CREATED);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')") //        가장 쉽고 간단한 편한 방법.ROLE_ADMIN 할 필요 없이 ADMIN 만 쓰면 됨.
    //  단점: 필터레벨에서 예외처리를 해서 로그나 메시지를 내 마음대로 하기 힘듦.
    public ResponseEntity<?> list() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if(!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))){
//            throw new AccessDeniedException("권한 없음");
//        }
        List<MemberResDto> memberResDto = memberService.findAll();
        return new ResponseEntity<>(memberResDto, HttpStatus.OK);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody LoginDto dto) {
        //      email, password 검증
        Member member = memberService.login(dto);
        //      토큰 생성 및 return
        String token = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(), member.getRole().toString());

        //      redis 에 rt 저장(상단에서 redisTemplate 주입함)
        redisTemplate.opsForValue().set(member.getEmail(), refreshToken, 200, TimeUnit.DAYS); // 200일 TTL

        //      사용자에게 at, rt 지급
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id",member.getId());
        loginInfo.put("token",token);
        loginInfo.put("refreshToken",refreshToken);
        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }


    @GetMapping("/myinfo")
    public ResponseEntity<?> myInfo() {
        MemberResDto dto =  memberService.myInfo();
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> generateNewAt(@RequestBody MemberRefreshDto dto) {
        //      rt 를 디코딩 후 email 추출
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKeyRt) //        상단의 Value어노테이션
                .build()
                .parseClaimsJws(dto.getRefreshToken())
                .getBody();

        //      rt 를 redis의 rt와 비교하는 로직
        Object rt = redisTemplate.opsForValue().get(claims.getSubject());
        if(rt == null || !rt.toString().equals(dto.getRefreshToken())) {
            return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);
        }

        //      at 생성하여 지급
        String token = jwtTokenProvider.createToken(claims.getSubject(), claims.get("role").toString());
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("token",token);
        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }

}
