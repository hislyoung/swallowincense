package com.swallowincense.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.concurrent.*;
//配置类放入到spring容器中,可以去掉此配置
//@EnableConfigurationProperties(value = ThreadPoolConfigProperties.class)
@Configuration
public class MyThreadConfig {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties pool){
        return new ThreadPoolExecutor(pool.getCoreSize(),pool.getMaxSize(),pool.getKeepAliveTime(),
                TimeUnit.SECONDS,new LinkedBlockingDeque<>(10000),
                Executors.defaultThreadFactory(),new ThreadPoolExecutor.AbortPolicy());
    }
}
