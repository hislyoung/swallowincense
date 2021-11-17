package com.swallowincense.member.exception;

public class EmailExistException extends RuntimeException{
    public EmailExistException() {
        super("邮箱已存在");
    }
}
