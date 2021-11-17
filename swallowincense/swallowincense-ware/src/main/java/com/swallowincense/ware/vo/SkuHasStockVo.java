package com.swallowincense.ware.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SkuHasStockVo implements Serializable {
    private Long skuId;
    private Boolean hasStock;
}
