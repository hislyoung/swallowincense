package com.swallowincense.member.vo;

import lombok.Data;

/**
 * 社交登录Token
 */
@Data
public class SocialUserVo {
    private String access_token;
    private String token_type;
    private Long expires_in;
    private String refresh_token;
    private String scope;
    private Long created_at;

    private Long id;
    private String login;
    private String name;
    private String email;
}
