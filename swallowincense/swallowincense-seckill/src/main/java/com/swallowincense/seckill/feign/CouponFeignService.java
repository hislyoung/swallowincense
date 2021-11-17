package com.swallowincense.seckill.feign;

import com.swallowincense.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("swallowincense-coupon")
@Component
public interface CouponFeignService {
    @GetMapping("/coupon/seckillsession/last3DaySession")
    R getLast3DaySession();
}
