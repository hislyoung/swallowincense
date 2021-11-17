package com.swallowincense.car.feign;

import com.swallowincense.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;

@FeignClient("swallowincense-product")
@Component
public interface ProductFeignService {
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R skuInfo(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skusaleattrvalue/stringList/{skuId}")
    List<String> getSkuSaleAttrValues(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skuinfo/{skuId}/price")
    BigDecimal getPrice(@PathVariable("skuId") Long skuId);
}
