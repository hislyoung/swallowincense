package com.swallowincense.seckill.service;

import com.swallowincense.seckill.to.SeckillSkuRedisTo;

import java.util.List;

public interface SeckillService {
    /**
     * 上架最近三天的秒杀商品及场次
     */
    void uploadSeckillSkuLast3Days();
    /**
     * 查询当前秒杀的商品信息
     * @return
     */
    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    /**
     * 商品服务查询秒杀信息
     * @param skuId
     * @return
     */
    SeckillSkuRedisTo getSkuSeckillInfo(Long skuId);

    /**
     * 用户进行秒杀
     * @param killId
     * @param key
     * @param num
     * @return
     */
    String kill(String killId, String key, Integer num);
}
