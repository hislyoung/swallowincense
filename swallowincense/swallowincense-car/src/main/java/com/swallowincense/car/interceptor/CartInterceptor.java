package com.swallowincense.car.interceptor;

import com.swallowincense.car.to.UserInfoTo;
import com.swallowincense.common.constant.AuthConstant;
import com.swallowincense.common.constant.CartConstant;
import com.swallowincense.common.vo.MemberVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;


/**
 * 执行controller之前先判断用户登录状态，并封装
 */

public class CartInterceptor implements HandlerInterceptor {
    /**
     * 目标方法执行之前
     * @param request
     * @param response
     * @param handler
     * @return
     */
    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession();
        UserInfoTo userInfoTo = new UserInfoTo();
        MemberVo attribute = (MemberVo) session.getAttribute(AuthConstant.LOGIN_USER);
        if(attribute != null){
            //用户登录
            userInfoTo.setUserId(attribute.getId());
        }
        //用户没登录
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (name.equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                    break;
                }
            }
        }

        //没有临时用户，分配一个临时用户
        if(StringUtils.isBlank(userInfoTo.getUserKey())){
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }
        if(userInfoTo!=null){
            threadLocal.set(userInfoTo);
        }

        return true;
    }

    /**
     * 业务执行之后
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        UserInfoTo userInfoTo = threadLocal.get();
        if(!userInfoTo.isTempUser()){
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME,userInfoTo.getUserKey());
            cookie.setDomain("swallowincense.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }
}
