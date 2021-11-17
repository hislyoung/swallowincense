package com.swallowincense.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/*
 * 1、开启服务的注册发现
 *
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SwallowincenseGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwallowincenseGatewayApplication.class, args);
    }

}
