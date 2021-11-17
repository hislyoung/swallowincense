package com.swallowincense.product.vo;

import lombok.Data;

@Data
public class SeckillInfoVo {
    private Long startTime;
    private Long endTime;
    private String randomCode;//防止攻击，工具刷单
    private SeckillSkuVo seckillSkuVo;
    private SkuInfoVo skuInfoVo;
}
