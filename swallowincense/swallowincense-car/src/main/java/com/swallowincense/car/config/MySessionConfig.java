package com.swallowincense.car.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class MySessionConfig {
    @Bean
    public CookieSerializer cookieSerializer(){
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setDomainName("swallowincense.com");
        serializer.setCookieName("JSESSIONID");
        serializer.setCookiePath("/");
        return serializer;
    }
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer(){
        return new GenericFastJsonRedisSerializer();
    }
}
