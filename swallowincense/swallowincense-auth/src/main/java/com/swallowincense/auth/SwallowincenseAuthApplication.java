package com.swallowincense.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.swallowincense.auth.feign")
@SpringBootApplication
public class SwallowincenseAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwallowincenseAuthApplication.class, args);
    }

}
