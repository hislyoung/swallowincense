package com.swallowincense.member.feign;

import com.swallowincense.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
@FeignClient("swallowincense-coupon")
@Component
public interface CouponFeignService {
    @RequestMapping("coupon/coupon/member/list")
    public R memberCoupons();
}
