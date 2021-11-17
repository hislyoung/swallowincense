package com.swallowincense.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rabbitmq.client.Channel;
import com.swallowincense.common.to.mq.StockLockedTo;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.ware.entity.WareSkuEntity;
import com.swallowincense.ware.vo.LockStockResult;
import com.swallowincense.ware.vo.OrderVo;
import com.swallowincense.ware.vo.SkuHasStockVo;
import com.swallowincense.ware.vo.WareSkuLockVo;
import org.springframework.amqp.core.Message;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 23:09:29
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    /**
     * 为订单锁库存
     * @param vo
     * @return
     */
    Boolean orderLockStock(WareSkuLockVo vo);

    /**
     * 解锁库存
     * @param to
     */

    void unlockStock(StockLockedTo to);

    /**
     * 订单卡顿，防止消息被提前消费，重复发送一个消息
     * @param to
     */
    void unlockStock(OrderVo to);
}

