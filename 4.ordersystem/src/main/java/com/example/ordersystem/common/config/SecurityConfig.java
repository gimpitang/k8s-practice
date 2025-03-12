package com.example.ordersystem.common.config;

import com.example.ordersystem.common.auth.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableMethodSecurity //PreAuthorize 사용시 해당 어노테이션 필요
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
//                spring security에서 cors 정책 지정
                .csrf(AbstractHttpConfigurer::disable) //csrf 보안공격 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) //HTTP basic 보안 방식 비활성화
//                세션 로그인 방식 사용하지 않는다는 것을 의미
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authenticated() : 모든 요청에 대해서 Authentication 객체가 생성되기를 요구
                .authorizeHttpRequests(a -> a.requestMatchers("/member/create", "member/doLogin", "product/list", "/member/refresh-token")
                        .permitAll().anyRequest().authenticated())
                .addFilterBefore(new CorsFilter(corsConfigurationSource()), UsernamePasswordAuthenticationFilter.class)
                //                토큰을 검증하고, 토큰을 통해 Authentication 객체를 생성
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                
                .build();
    }

    private CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://www.junan.shop"));
        configuration.setAllowedMethods(Arrays.asList("*")); //모든 HTTP 메서드 허용
        configuration.setAllowedHeaders(Arrays.asList("*")); //모든 헤더값 허용
        configuration.setAllowCredentials(true); //자격 증명 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); //모든 url 패턴에 대해 cors 허용 설정
        return source;
    }

    @Bean
    public PasswordEncoder makePassword(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}