package com.swallowincense.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.swallowincense.search.feign")//开启远程调用
@SpringBootApplication
public class SwallowincenseSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwallowincenseSearchApplication.class, args);
    }

}
