package com.swallowincense.product.exception;

import com.swallowincense.common.exception.BizCodeEnum;
import com.swallowincense.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

//@ControllerAdvice(basePackages = "com.swallowincense.product.controller")
//@ResponseBody
@Slf4j
@RestControllerAdvice(basePackages = "com.swallowincense.product.controller")
public class ProductExceptionControllerAdvice {
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        log.error("错误的信息{},错误的类型{}", e.getMessage(), e.getClass());
        BindingResult result = e.getBindingResult();
        Map<String, String> stringMap = new HashMap<>();
        if (result.hasErrors()) {
            result.getFieldErrors().forEach((error) -> {
                stringMap.put(error.getField(), error.getDefaultMessage());
            });
        }
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMsg()).put("data", stringMap);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleThrowable(Throwable throwable){
        log.error("错误：",throwable);
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(),BizCodeEnum.UNKNOW_EXCEPTION.getMsg());
    }
}
