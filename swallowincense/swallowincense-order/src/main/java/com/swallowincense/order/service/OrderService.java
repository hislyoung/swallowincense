package com.swallowincense.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swallowincense.common.to.mq.SeckillOrderTo;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.order.entity.OrderEntity;
import com.swallowincense.order.vo.OrderConfirmVo;
import com.swallowincense.order.vo.OrderSubmitVo;
import com.swallowincense.order.vo.SubmitOrderResponseVo;

import java.util.Map;

/**
 * 订单
 *
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 23:02:16
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 订单确认页需要返回的数据
     * @return
     */
    OrderConfirmVo confirmOrder();

    /**
     * 下单操作
     * @param vo
     * @return
     */

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    /**
     * 根据订单号查询订单
     * @param orderSn
     * @return
     */
    OrderEntity getOrderByOrderSn(String orderSn);

    /**
     * 延时队列关闭订单
     * @param orderEntity
     */
    void closeOrder(OrderEntity orderEntity);

    /**
     * 监听队列创建秒杀单
     * @param seckillOrder
     */

    void createSeckillOrder(SeckillOrderTo seckillOrder);
}

