package com.swallowincense.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

/*
 * 注册不用@EnableDiscoveryClient注解也可以
 * 只需要配置服务地址和服务名称
 *
 * 1、想要远程调用别的服务
 * 1）、引入Open-Feign依赖
 * 2）、编写一个统一的接口，告诉spring-cloud这个接口需要远程调用
 *      1、声明接口的每一个方法都是调用的那个远程服务的那个请求
 *      2、开启远程调用功能
 *      @EnableFeignClients(basePackages = "com.swallowincense.member.feign")
 */
@EnableCaching
@EnableFeignClients(basePackages = "com.swallowincense.member.feign")
@SpringBootApplication
public class SwallowincenseMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwallowincenseMemberApplication.class, args);
    }

}
