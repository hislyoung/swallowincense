package com.swallowincense.thirdparty;

import com.swallowincense.thirdparty.component.SmsComponent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SwallowincenseThirdPartyApplicationTests {
    @Autowired
    SmsComponent smsComponent;
    @Test
    void contextLoads() {
        try {
            smsComponent.sendCode("15598168665","3099");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
