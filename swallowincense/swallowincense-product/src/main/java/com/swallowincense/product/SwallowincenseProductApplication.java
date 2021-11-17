package com.swallowincense.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.swallowincense.product.feign")
@MapperScan(value = "com.swallowincense.product.dao")
@SpringBootApplication
public class SwallowincenseProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwallowincenseProductApplication.class, args);
    }

}
