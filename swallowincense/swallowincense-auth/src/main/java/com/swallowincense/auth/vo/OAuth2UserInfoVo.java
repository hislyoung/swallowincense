package com.swallowincense.auth.vo;

import lombok.Data;

@Data
public class OAuth2UserInfoVo {
    private Long id;
    private String login;
    private String name;
    private String email;
}
