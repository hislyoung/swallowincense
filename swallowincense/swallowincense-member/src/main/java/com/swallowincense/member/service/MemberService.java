package com.swallowincense.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.member.entity.MemberEntity;
import com.swallowincense.member.exception.EmailExistException;
import com.swallowincense.member.exception.PhoneExistException;
import com.swallowincense.member.exception.UsernameExistException;
import com.swallowincense.member.vo.MemberLoginVo;
import com.swallowincense.member.vo.MemberRegistVo;
import com.swallowincense.member.vo.OAuth2UserInfoVo;
import com.swallowincense.member.vo.SocialUserVo;

import java.util.Map;

/**
 * 会员
 *
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 22:54:01
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo vo);

    void checkEmailUnique(String email) throws EmailExistException;

    void checkUsernameUnique(String username) throws UsernameExistException;

    void checkPhoneUnique(String phone)throws PhoneExistException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SocialUserVo vo);
}

