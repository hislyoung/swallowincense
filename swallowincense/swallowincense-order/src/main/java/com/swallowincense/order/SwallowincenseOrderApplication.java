package com.swallowincense.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
@EnableFeignClients(basePackages = "com.swallowincense.order.feign")
@EnableRabbit
@EnableRedisHttpSession
@SpringBootApplication
public class SwallowincenseOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwallowincenseOrderApplication.class, args);
    }

}
