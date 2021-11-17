package com.swallowincense.seckill.scheduled;

import com.swallowincense.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @EnableScheduling开启定时任务
 * @Scheduled 开启一个定时任务
 *  TaskSchedulingAutoConfiguration
 * @EnableAsync开启异步任务
 * @Async标注到异步执行的任务上，指定方法
 *  TaskExecutionAutoConfiguration
 *
 */
@Slf4j
@Component
public class StockScheduled {
    /**
     * spring中不支持年
     * 完成定时任务不阻塞，默认是阻塞的
     *      将业务以异步方式运行
     *          CompletableFuture
     *      使用定时任务线程池,有的好使，有的不好使
     *          TaskSchedulingProperties ,默认只有一个线程
     *      让定时任务异步执行
     *          TaskExecutionProperties
     *          @EnableAsync
     *          @Async
     *
     */
    @Autowired
    SeckillService seckillService;
    @Autowired
    RedissonClient redissonClient;

    private final String upload_lock = "seckill:upload:lock";
    /**
     * 幂等性处理
     */
    @Async
    @Scheduled(cron = "*/5 * * * * ?")
    public void stockUpScheduled()  {
        //防止分布式情况下重复提交，需要上分布式锁
        RLock lock = redissonClient.getLock(upload_lock);
        try{
            lock.lock(10, TimeUnit.SECONDS);
            seckillService.uploadSeckillSkuLast3Days();
        }finally{
            lock.unlock();
        }

    }
}
