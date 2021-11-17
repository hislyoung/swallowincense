package com.swallowincense.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillOrderTo {
    /**
     * 订单号
     */
    private String orderSn;
    /**
     * 商品Id
     */
    private Long skuId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 购买数量
     */
    private Integer num;
    /**
     * 购买人
     */
    private Long memberId;
}
