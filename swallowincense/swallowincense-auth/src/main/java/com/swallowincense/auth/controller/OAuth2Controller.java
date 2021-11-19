package com.swallowincense.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.swallowincense.auth.feign.MemberFeignService;
import com.swallowincense.auth.utils.HttpClientHelper;
import com.swallowincense.common.constant.AuthConstant;
import com.swallowincense.common.vo.MemberVo;
import com.swallowincense.auth.vo.SocialUserVo;
import com.swallowincense.common.utils.R;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller
@RequestMapping("/OAuth2")
public class OAuth2Controller {
    @Autowired
    MemberFeignService memberFeignService;
    @RequestMapping("/success")
    public String gitee(@RequestParam("code")String code, HttpSession session,HttpServletResponse servletResponse){
        //发送请求，用code换取token
        String url = "https://gitee.com/oauth/token?grant_type=authorization_code&code="+code+"&client_id=ik&redirect_uri=http://auth.swallowincense.com/OAuth2/success&client_secret=sk";
        try {
            String s = HttpClientHelper.sendPost(url);
            if(StringUtils.isNotBlank(s)){
                SocialUserVo userVo = JSON.parseObject(s, SocialUserVo.class);
                //当前用户第一次登录，自动注册（社交账号对应系统）
                String getUserInfoUrl = "https://gitee.com/api/v5/user?access_token="+userVo.getAccess_token();
                String userInfo = HttpClientHelper.sendGet(getUserInfoUrl);
                if(StringUtils.isNotBlank(userInfo)) {
                    JSONObject jsonObject = JSONObject.parseObject(userInfo);
                    userVo.setId(Long.parseLong(jsonObject.get("id").toString()));
                    userVo.setLogin((String) jsonObject.get("login"));
                    userVo.setName((String) jsonObject.get("name"));
                    userVo.setEmail((String) jsonObject.get("email"));
                    R r = memberFeignService.oauthLogin(userVo);
                    if(r.getCode()==0){
                        MemberVo data = r.getData(new TypeReference<MemberVo>() {
                        });
                        session.setAttribute(AuthConstant.LOGIN_USER,data);
                        System.out.println(data);
                        /*Cookie cookie = new Cookie("jsessionid", "xxx");
                        cookie.setDomain("swallowincense.com");
                        servletResponse.addCookie(cookie);*/

                        return "redirect:http://swallowincense.com";
                    }else {
                        return "redirect:http://auth.swallowincense.com/login.html";
                    }
                }else {
                    return "redirect:http://auth.swallowincense.com/login.html";
                }
            }else{
                return "redirect:http://auth.swallowincense.com/login.html";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:http://auth.swallowincense.com/login.html";
        }
    }
}
