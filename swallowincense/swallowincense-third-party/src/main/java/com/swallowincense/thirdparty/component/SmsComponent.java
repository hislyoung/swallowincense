package com.swallowincense.thirdparty.component;

import org.springframework.stereotype.Component;
import com.aliyun.dysmsapi20170525.models.*;
import com.aliyun.teaopenapi.models.*;

@Component
public class SmsComponent {
    public static com.aliyun.dysmsapi20170525.Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        Config config = new Config()
                // 您的AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 您的AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // 访问的域名
        config.endpoint = "dysmsapi.aliyuncs.com";
        return new com.aliyun.dysmsapi20170525.Client(config);
    }
    public void sendCode(String phoneNums, String sendCode) throws Exception {
        com.aliyun.dysmsapi20170525.Client client = SmsComponent.createClient("ak", "sk");
        // 1.发送短信
        SendSmsRequest sendReq = new SendSmsRequest()
                .setPhoneNumbers(phoneNums)
                .setSignName("一品国香测试")
                //SMS_223581081
                .setTemplateCode("SMS_223581081")
                .setTemplateParam("{\"code\":\""+sendCode+"\"}");
        SendSmsResponse sendResp = client.sendSms(sendReq);
        String code = sendResp.body.code;
        if (!com.aliyun.teautil.Common.equalString(code, "OK")) {
            com.aliyun.teaconsole.Client.log("错误信息: " + sendResp.body.message + "");
            return ;
        }

        String bizId = sendResp.body.bizId;
        // 2. 等待 10 秒后查询结果
        com.aliyun.teautil.Common.sleep(10000);
        // 3.查询结果
        java.util.List<String> phoneNumx = com.aliyun.darabonbastring.Client.split(phoneNums, ",", -1);
        for (String phoneNum : phoneNumx) {
            QuerySendDetailsRequest queryReq = new QuerySendDetailsRequest()
                    .setPhoneNumber(com.aliyun.teautil.Common.assertAsString(phoneNum))
                    .setBizId(bizId)
                    .setSendDate(com.aliyun.darabonbatime.Client.format("yyyyMMdd"))
                    .setPageSize(10L)
                    .setCurrentPage(1L);
            QuerySendDetailsResponse queryResp = client.querySendDetails(queryReq);
            java.util.List<QuerySendDetailsResponseBody.QuerySendDetailsResponseBodySmsSendDetailDTOsSmsSendDetailDTO> dtos = queryResp.body.smsSendDetailDTOs.smsSendDetailDTO;
            // 打印结果
            for (QuerySendDetailsResponseBody.QuerySendDetailsResponseBodySmsSendDetailDTOsSmsSendDetailDTO dto : dtos) {
                if (com.aliyun.teautil.Common.equalString("" + dto.sendStatus + "", "3")) {
                    com.aliyun.teaconsole.Client.log("" + dto.phoneNum + " 发送成功，接收时间: " + dto.receiveDate + "");
                } else if (com.aliyun.teautil.Common.equalString("" + dto.sendStatus + "", "2")) {
                    com.aliyun.teaconsole.Client.log("" + dto.phoneNum + " 发送失败");
                } else {
                    com.aliyun.teaconsole.Client.log("" + dto.phoneNum + " 正在发送中...");
                }

            }
        }
    }
}
