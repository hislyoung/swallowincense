package com.swallowincense.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.swallowincense.seckill.feign")
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class SwallowincenseSeckillApplication {
    public static void main(String[] args) {
        SpringApplication.run(SwallowincenseSeckillApplication.class, args);
    }

}
