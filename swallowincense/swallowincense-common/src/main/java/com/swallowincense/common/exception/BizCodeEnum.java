package com.swallowincense.common.exception;

/**
 * 10通用
 * 11商品
 * 12订单
 * 13购物车
 * 14物流
 * 15用户
 * 21库存
 */
public enum BizCodeEnum {
    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002, "验证码获取频率太高，请稍后..."),
    TOO_MANY_REQUEST(10003, "请求流量过大！"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常"),
    USER_EXIST_EXCEPTION(15001,"用户名已存在"),
    PHONE_EXIST_EXCEPTION(15002,"手机号已存在"),
    EMAIL_EXIST_EXCEPTION(15003,"邮箱已存在"),
    NO_STOCK_EXCEPTION(21000,"没有商品库存"),
    LOGINACCT_PASSWORD_INVAILD_EXCEPTION(15004,"账号或密码错误");
    private Integer code;
    private String msg;
    BizCodeEnum(int code, String msg){
        this.code = code;
        this.msg = msg;
    }
    public Integer getCode(){
        return this.code;
    }
    public String getMsg(){
        return this.msg;
    }
}
