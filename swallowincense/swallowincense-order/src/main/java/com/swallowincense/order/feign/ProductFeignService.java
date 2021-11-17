package com.swallowincense.order.feign;

import com.swallowincense.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient("swallowincense-product")
@Component
public interface ProductFeignService {
    @GetMapping("/product/spuinfo/skuId/{id}")
    R getSpuInfoBySku(@PathVariable("id")Long id);
}
