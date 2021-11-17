package com.swallowincense.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockVo {
    private String orderSn;//锁定订单
    private List<OrderItemVo> locks;//需要锁定的所有库存信息

}
