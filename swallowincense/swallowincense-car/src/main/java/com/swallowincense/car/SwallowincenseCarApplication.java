package com.swallowincense.car;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableFeignClients(basePackages = "com.swallowincense.car.feign")
@EnableRedisHttpSession
@SpringBootApplication
public class SwallowincenseCarApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwallowincenseCarApplication.class, args);
    }

}
