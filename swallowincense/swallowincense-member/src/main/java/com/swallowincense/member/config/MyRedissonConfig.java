package com.swallowincense.member.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyRedissonConfig {
    /**
     * 所有操作都是通过redis-cli
     * @return
     * @throws Exception
     */
    @Bean(destroyMethod = "shutdown")
    //Redis url should start with redis:// or rediss:// (for SSL connection)
    RedissonClient redissonClient()throws Exception{
        Config config = new Config();
        //集群模式
        //config.useClusterServers().addNodeAddress("192.168.159.128:6379");
        //单节点模式
        config.useSingleServer().setAddress("redis://192.168.159.128:6379").setPassword("liuyang");
        return Redisson.create(config);
    }

}
