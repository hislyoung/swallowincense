package com.swallowincense.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class UserRegistVo {
    @NotNull(message = "用户名必须提交")
    @Length(min = 6,max = 10,message = "用户名必须是6-10位字符")
    private String username;
    @NotNull(message = "密码必须提交")
    @Length(min = 6,max = 10,message = "密码必须是6-10位字符")
    private String password;
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$",message = "手机号格式不正确")
    private String phone;
    @NotNull(message = "验证码必须提交")
    private String code;
}
