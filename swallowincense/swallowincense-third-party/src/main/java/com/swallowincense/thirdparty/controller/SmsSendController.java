package com.swallowincense.thirdparty.controller;

import com.swallowincense.common.utils.R;
import com.swallowincense.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class SmsSendController {
    @Autowired
    SmsComponent smsComponent;
    @GetMapping("/sendcode")
    public R sendCode(@RequestParam("phone") String phone,@RequestParam("code") String code){
        try {
            smsComponent.sendCode(phone,code);
            return R.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return R.error();
        }

    }
}
