package com.swallowincense.order.to;

import com.swallowincense.order.entity.OrderEntity;
import com.swallowincense.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建订单对象
 */
@Data
public class OrderCreateTo {
    //订单对象
    private OrderEntity order;
    //购物项列表
    private List<OrderItemEntity> orderItems;
    //重新计算订单价格  价格校验
    private BigDecimal payPrice;
    //运费
    private BigDecimal fare;
}
