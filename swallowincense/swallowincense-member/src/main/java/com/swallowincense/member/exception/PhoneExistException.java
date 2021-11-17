package com.swallowincense.member.exception;

public class PhoneExistException extends RuntimeException{
    public PhoneExistException() {
        super("手机号已存在");
    }
}
