package com.example.ordersystem.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthFilter extends GenericFilter {
    @Value("${jwt.secretKey}")
    private String secretKey;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        //      token 검증
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        //      예외 처리를 위해서(캐치문을 위함임)
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String bearerToken = httpRequest.getHeader("Authorization"); //Authorization은 Header의 이름임.
        //      token 분해 후 Authentication 객체 생성
        //      token이 있는데 잘못된 경우 (), token이 없는 경우

        try {
            if (bearerToken != null) {
                //      Bearer 를 관례적으로 붙이는데, 안붙였을 때 에러 발생
                if (!bearerToken.substring(0, 7).equals("Bearer ")) {
                    throw new AuthenticationServiceException("Bearer 형식이 아닙니다.");
                }
                String token = bearerToken.substring(7);
                //      token 검증 및 claims 추출 (만료된 토큰을 넣으면 여기서 500 error을 출력함
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(secretKey) //        상단의 Value어노테이션
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                //      Authentication 객체 생성
//            SecurityContextHolder.getContext().setAuthentication(인증객체) 이걸 만들기 위함임.
//            Authentication authentication = new UsernamePasswordAuthenticationToken(이메일,비밀번호,롤);
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role")));
                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(claims.getSubject(), "password", authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }
            //      다시 filterchain으로 되돌아 가는 로직
            chain.doFilter(request, response);
        }catch (Exception e) {
            //      slf4j로 로그로 찍어야함. 근데 교육 목적 상 그냥 프린트 스택으로 함.
            e.printStackTrace();
            httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpResponse.getWriter().write("token is invalid");

        }
    }
}
