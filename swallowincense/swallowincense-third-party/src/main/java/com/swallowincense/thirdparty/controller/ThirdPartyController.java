package com.swallowincense.thirdparty.controller;

import com.swallowincense.common.utils.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ThirdPartyController {

    @RequestMapping(value = "/thirdPart/policy")
    public R policy(){
        //TODO 1、图片鉴权返回 token
        return R.ok();
    }


}
