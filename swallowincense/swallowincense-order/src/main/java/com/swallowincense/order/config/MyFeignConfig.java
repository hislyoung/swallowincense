package com.swallowincense.order.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class MyFeignConfig {
    @Bean
     public RequestInterceptor requestInterceptor(){
        return template -> {
            //RequestContextHolder 上下文环境保持器
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if(requestAttributes!=null){
                HttpServletRequest request = requestAttributes.getRequest();
                //同步请求头数据
                String cookie = request.getHeader("Cookie");
                //将老请求的请求头同步到新请求
                template.header("Cookie", cookie);
            }
        };
    }
}
