package com.swallowincense.common.to.mq;

import lombok.Data;


@Data
public class StockLockedTo {
    private Long id;//库存工作单Id
    private StockDetailTo detail;//工作单Id,方便回溯
}
