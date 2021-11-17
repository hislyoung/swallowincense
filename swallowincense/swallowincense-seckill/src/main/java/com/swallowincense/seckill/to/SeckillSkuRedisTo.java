package com.swallowincense.seckill.to;

import com.swallowincense.seckill.vo.SeckillSkuVo;
import com.swallowincense.seckill.vo.SkuInfoVo;
import lombok.Data;

@Data
public class SeckillSkuRedisTo {
    private Long startTime;
    private Long endTime;
    private String randomCode;//防止攻击，工具刷单
    private SeckillSkuVo seckillSkuVo;
    private SkuInfoVo skuInfoVo;
}
