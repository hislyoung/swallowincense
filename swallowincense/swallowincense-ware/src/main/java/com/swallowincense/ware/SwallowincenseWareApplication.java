package com.swallowincense.ware;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
@EnableRabbit
@EnableFeignClients(basePackages = "com.swallowincense.ware.feign")
@SpringBootApplication
public class SwallowincenseWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwallowincenseWareApplication.class, args);
    }

}
