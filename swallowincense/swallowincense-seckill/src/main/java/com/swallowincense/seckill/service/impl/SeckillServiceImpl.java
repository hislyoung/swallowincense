package com.swallowincense.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.swallowincense.common.to.mq.SeckillOrderTo;
import com.swallowincense.common.utils.R;
import com.swallowincense.common.vo.MemberVo;
import com.swallowincense.seckill.feign.CouponFeignService;
import com.swallowincense.seckill.feign.ProductFeignService;
import com.swallowincense.seckill.interceptor.LoginUserInterceptor;
import com.swallowincense.seckill.service.SeckillService;
import com.swallowincense.seckill.to.SeckillSkuRedisTo;
import com.swallowincense.seckill.vo.SeckillSessionWithSkus;
import com.swallowincense.seckill.vo.SkuInfoVo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service("seckillService")
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    RabbitTemplate rabbitTemplate;
    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";

    private final String SKUKILL_CACHE_PREFIX = "seckill:skus";

    private final String SKU_STOCK_SEMAPHONE = "seckill:stock:";
    @Override
    public void uploadSeckillSkuLast3Days() {
        R last3DaySession = couponFeignService.getLast3DaySession();
        if (last3DaySession.getCode()==0) {
            //上架
            List<SeckillSessionWithSkus> data = last3DaySession.getData(new TypeReference<List<SeckillSessionWithSkus>>() {
            });
            //缓存活动信息
            saveSessionInfos(data);
            //缓存商品信息
            saveSessionSkuInfos(data);
        }
    }

    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        //确定当前时间属于那个场次
        long currentTime = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        for (String key : keys) {
            String[] start_end = key.replace(SESSIONS_CACHE_PREFIX, "").split("_");
            long startTime = Long.parseLong(start_end[0]);
            long endTime = Long.parseLong(start_end[1]);
            if (currentTime>=startTime&&currentTime<=endTime) {
                //获取当前场次需要的所有信息
                List<String> sessionValue = redisTemplate.opsForList().range(key, 0, -1);
                BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<String> skuStrings = ops.multiGet(sessionValue);
                if(skuStrings!=null&&skuStrings.size()>0){
                    return skuStrings.stream().map(sku ->
                         JSON.parseObject(sku, SeckillSkuRedisTo.class)
                    ).collect(Collectors.toList());
                }
                break;
            }

        }


        return null;
    }

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        //找到所有参与秒杀的key信息
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = operations.keys();
        if (keys!=null&&keys.size()>0) {
            String regx = "\\d_"+skuId;
            for (String key : keys) {
                if (Pattern.matches(regx,key)) {
                    String json = operations.get(key);
                    SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
                    if(redisTo!=null) {
                        Long current = new Date().getTime();
                        Long startTime = redisTo.getStartTime();
                        Long endTime = redisTo.getEndTime();
                        if(!(current>=startTime&&current<=endTime)){
                            redisTo.setRandomCode(null);
                        }
                    }
                    return redisTo;
                }
            }
        }
        return null;
    }
    @Override
    public String kill(String killId, String key, Integer num) {
        MemberVo memberVo = LoginUserInterceptor.loginUser.get();
        //获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String s = operations.get(killId);
        if(StringUtils.isBlank(s)){
            return null;
        }else {
            SeckillSkuRedisTo redis = JSON.parseObject(s, SeckillSkuRedisTo.class);
            //合法性校验
            Long current = new Date().getTime();
            Long startTime = redis.getStartTime();
            Long endTime = redis.getEndTime();
            Long passTime = endTime - current;
            if(current>=startTime&&current<=endTime){
                //校验随机码和商品Id
                String randomCode = redis.getRandomCode();
                String killKey =redis.getSeckillSkuVo().getPromotionSessionId() +"_"+ redis.getSkuInfoVo().getSkuId();
                if(randomCode.equals(key)&&killId.equals(killKey)){
                    //验证购物数量
                    if (num<=redis.getSeckillSkuVo().getSeckillLimit().intValue()) {
                        //验证这个人是否已经购买过，幂等性问题
                        //SETNX不存在才占位
                        String redisKey = memberVo.getId()+"_"+killId;
                        Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), passTime, TimeUnit.MILLISECONDS);
                        if(ifAbsent){
                            //占位成功已经购买
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHONE + randomCode);
                            boolean b = semaphore.tryAcquire(num);
                            if (b) {
                                //秒杀成功,快速下单，发送消息到MQ
                                String orderSn = IdWorker.getTimeId();
                                SeckillOrderTo orderTo = new SeckillOrderTo();
                                orderTo.setOrderSn(orderSn);
                                orderTo.setSkuId(redis.getSkuInfoVo().getSkuId());
                                orderTo.setPromotionSessionId(redis.getSeckillSkuVo().getPromotionSessionId());
                                orderTo.setSeckillPrice(redis.getSeckillSkuVo().getSeckillPrice());
                                orderTo.setNum(num);
                                orderTo.setMemberId(memberVo.getId());
                                //String orderJson = JSON.toJSONString(orderTo);
                                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
                                return orderSn;
                            } else {
                                return null;
                            }
                        }else {
                            //占位失败已经买过
                            return null;
                        }
                    }
                }else {
                    return null;
                }

            }else {
                return null;
            }
        }

        return null;
    }

    private void saveSessionInfos(List<SeckillSessionWithSkus> data){
        if(data!=null) {
            data.forEach(session -> {
                Long startTime = session.getStartTime().getTime();
                Long endTime = session.getEndTime().getTime();
                String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
                if (!redisTemplate.hasKey(key)) {
                    List<String> collect = session.getRelationEntities().stream().map(item ->
                            item.getPromotionSessionId() + "_" + item.getSkuId()
                    ).collect(Collectors.toList());
                    if (collect.size() > 0) {
                        redisTemplate.opsForList().leftPushAll(key, collect);
                    }
                }
            });
        }
    }
    private void saveSessionSkuInfos(List<SeckillSessionWithSkus> data){
        data.forEach(session->{
            //准备HASH操作的结构
            BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            session.getRelationEntities().forEach(seckillSkuVo -> {
                String skuRedisKey = seckillSkuVo.getPromotionSessionId() + "_" + seckillSkuVo.getSkuId();
                if (!operations.hasKey(skuRedisKey)) {
                    SeckillSkuRedisTo seckillSkuRedisTo = new SeckillSkuRedisTo();
                    //sku的秒杀信息
                    seckillSkuRedisTo.setSeckillSkuVo(seckillSkuVo);
                    //sku的基本信息
                    R info = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                    if (info.getCode()==0) {
                        SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        seckillSkuRedisTo.setSkuInfoVo(skuInfo);
                    }
                    //随机码、开始结束时间
                    seckillSkuRedisTo.setStartTime(session.getStartTime().getTime());
                    seckillSkuRedisTo.setEndTime(session.getEndTime().getTime());
                    //***防止刷单,开始才暴漏
                    String token = UUID.randomUUID().toString().replace("-", "");
                    seckillSkuRedisTo.setRandomCode(token);
                    String skuRedisValue = JSON.toJSONString(seckillSkuRedisTo);
                    operations.put(skuRedisKey,skuRedisValue);
                    //引入分布式锁-信号量作库存校验 限流
                    //解决不同场次相同商品不会重复上库存
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHONE + token);
                    semaphore.trySetPermits(seckillSkuVo.getSeckillCount().intValue());

                }
            });
        });
    }
}
