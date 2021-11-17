package com.swallowincense.order.vo;

import com.swallowincense.order.entity.OrderEntity;
import lombok.Data;

/**
 * 下单返回数据
 */
@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;//订单信息
    private Integer code;//错误状态码
}
