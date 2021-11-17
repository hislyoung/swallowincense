package com.swallowincense.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单提交的数据
 */
@Data
public class OrderSubmitVo {
    private Long addrId; //收货地址
    private Integer payType; //支付方式
    //购物项会重新查询
    //优惠发票物流等信息
    //用户相关信息在session中
    private BigDecimal payPrice;//价格校验
    private String orderToken;//验证令牌

}
