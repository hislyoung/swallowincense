package com.swallowincense.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.fastjson.JSON;
import com.swallowincense.common.exception.BizCodeEnum;
import com.swallowincense.common.utils.R;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * webflux mono响应式编程
 */
@Configuration
public class SentinelGatewayConfig {
    public SentinelGatewayConfig(){
        GatewayCallbackManager.setBlockHandler((serverWebExchange, throwable) -> {
            R error = R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMsg());
            String errJson = JSON.toJSONString(error);
            return ServerResponse.ok().body(Mono.just(errJson),String.class);
        });
    }
}
