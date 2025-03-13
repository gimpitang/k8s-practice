package com.example.ordersystem.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;



    //      redis 에 접근하기 위한 접근(connection) 객체(몇 번 redis db를 쓸건지)
    @Bean
    @Qualifier("rtdb")
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();

        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(0);

        return new LettuceConnectionFactory(configuration);
    }

    //      redis 에 저장할 key, value의 타입 지정한 template 생성
    //      redisTemplate 이라는 메서드가 config 전체에 1개는 있어야함.

    @Bean
    @Qualifier("rtdb")
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("rtdb") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        //      redisTemplate의 object에 json 형태로 넣어야하므로 직렬화를 할 수 있는 탬플릿을 만들어야함.(물론 매번 직렬화 해도됨)
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }


    //      redis 에 접근하기 위한 접근(connection) 객체
    @Bean
    @Qualifier("stockinventory")
    public RedisConnectionFactory stockRedisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();

        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(1);

        return new LettuceConnectionFactory(configuration);
    }

    //      redis 에 저장할 key, value의 타입 지정한 template 생성

    @Bean
    @Qualifier("stockinventory")
    public RedisTemplate<String, String> stockRedisTemplate(@Qualifier("stockinventory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        //      redisTemplate의 object에 json 형태로 넣어야하므로 직렬화를 할 수 있는 탬플릿을 만들어야함.(물론 매번 직렬화 해도됨)
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
}
