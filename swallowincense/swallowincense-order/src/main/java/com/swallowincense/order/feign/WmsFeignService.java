package com.swallowincense.order.feign;

import com.swallowincense.common.utils.R;
import com.swallowincense.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("swallowincense-ware")
@Component
public interface WmsFeignService {
    @PostMapping("/ware/waresku/hasStock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);

    @GetMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId")Long addrId);

    @PostMapping("/ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo vo);
}
