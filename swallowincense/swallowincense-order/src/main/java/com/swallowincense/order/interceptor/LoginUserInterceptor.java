package com.swallowincense.order.interceptor;

import com.swallowincense.common.constant.AuthConstant;
import com.swallowincense.common.vo.MemberVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberVo> loginUser = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        //拦截器执行会将请求拦截，丢失掉请求的用户信息会返回登录页
        //需要将请求放行
        String uri = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/order/order/status/**", uri);
        if(match){
            return true;
        }

        MemberVo attribute = (MemberVo) request.getSession().getAttribute(AuthConstant.LOGIN_USER);
        if(attribute!=null){
            loginUser.set(attribute);
            return true;
        }else {
            response.sendRedirect("http://auth.swallowincense.com/login.html");
            return false;
        }

    }
    /**
     * 在视图渲染完成之后执行，经常在完成方法中释放资源
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 调用删除方法，是必须选项。因为使用的是tomcat线程池，请求结束后，线程不会结束。
        // 如果不手动删除线程变量，可能会导致内存泄漏
        loginUser.remove();
    }

}
