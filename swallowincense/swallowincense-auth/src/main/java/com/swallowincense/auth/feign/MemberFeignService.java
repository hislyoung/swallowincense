package com.swallowincense.auth.feign;

import com.swallowincense.auth.vo.OAuth2UserInfoVo;
import com.swallowincense.auth.vo.SocialUserVo;
import com.swallowincense.auth.vo.UserLoginVo;
import com.swallowincense.auth.vo.UserRegistVo;
import com.swallowincense.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(name ="swallowincense-member")
@Component
public interface MemberFeignService {

    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo vo );

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    @ResponseBody
    R oauthLogin(@RequestBody SocialUserVo vo);
}
