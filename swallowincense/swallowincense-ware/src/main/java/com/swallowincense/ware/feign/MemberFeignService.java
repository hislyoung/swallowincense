package com.swallowincense.ware.feign;

import com.swallowincense.common.utils.R;
import com.swallowincense.ware.vo.MemberReceiveAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("swallowincense-member")
@Component
public interface MemberFeignService {

    @RequestMapping("/member/memberreceiveaddress/info/{id}")
    R addrInfo(@PathVariable("id") Long id);

    @RequestMapping("/member/memberreceiveaddress/update")
    R updateDef(@RequestBody MemberReceiveAddressVo memberReceiveAddress);

    @RequestMapping("/member/memberreceiveaddress/updateInBatch")
    R  updateInBatchByMemberId(@RequestParam("addrId")Long addrId);
}
