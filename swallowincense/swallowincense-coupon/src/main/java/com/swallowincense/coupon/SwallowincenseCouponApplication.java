package com.swallowincense.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@EnableDiscoveryClient
@MapperScan(value = "com.swallowincense.coupon.dao")
@SpringBootApplication
public class SwallowincenseCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwallowincenseCouponApplication.class, args);
    }

}
