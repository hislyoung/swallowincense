package com.swallowincense.order.feign;

import com.swallowincense.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
@FeignClient("swallowincense-car")
@Component
public interface CartFeignService {
    @GetMapping("/currentUserItems")
    @ResponseBody
    List<OrderItemVo> getCurrentUserItems();
}
