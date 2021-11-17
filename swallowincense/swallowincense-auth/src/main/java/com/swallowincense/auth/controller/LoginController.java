package com.swallowincense.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.swallowincense.auth.feign.MemberFeignService;
import com.swallowincense.auth.feign.ThirdPartFeignService;
import com.swallowincense.auth.vo.UserLoginVo;
import com.swallowincense.auth.vo.UserRegistVo;
import com.swallowincense.common.constant.AuthConstant;
import com.swallowincense.common.exception.BizCodeEnum;
import com.swallowincense.common.utils.R;
import com.swallowincense.common.vo.MemberVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {
    @Autowired
    ThirdPartFeignService thirdPartFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    MemberFeignService memberFeignService;
    @GetMapping("/sms/sendcode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone){
        //TODO 接口暴漏防刷
        String s = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + phone);
        if(StringUtils.isNotBlank(s)){
            Long split = Long.parseLong(s.split("_")[1]);
            if(System.currentTimeMillis()-split<60000){
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(),BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        String code = UUID.randomUUID().toString().substring(0, 5);
        String redisCode = code+"_"+System.currentTimeMillis();
        //再次校验,防止手机号在60S内再次发送
        redisTemplate.opsForValue().set(AuthConstant.SMS_CODE_CACHE_PREFIX+phone,redisCode,100, TimeUnit.SECONDS);
        thirdPartFeignService.sendCode(phone,code);
        return R.ok();
    }
    @PostMapping("regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes model){
        if(result.hasErrors()){
            Map<String, String> collect = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            //重定向带参数,利用session原理
            model.addFlashAttribute("errors",collect);
            //校验出错转发到注册页
            return "redirect:http://auth.swallowincense.com/reg.html";
        }
        //用户注册->/regist[post]->转发reg.html（路径映射默认是get请求）
        String code = vo.getCode();
        String s = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if(StringUtils.isNotBlank(s)){
            if(code.equals(s.split("_")[0])){
                redisTemplate.delete(AuthConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                //远程服务注册
                R regist = memberFeignService.regist(vo);
                if(regist.getCode()==0){
                    return "redirect:http://auth.swallowincense.com/login.html";
                }else{
                    Map<String, String> collect = new HashMap<>();
                    collect.put("msg",regist.getData(new TypeReference<String>(){}));
                    model.addFlashAttribute("errors",collect);
                    return "redirect:http://auth.swallowincense.com/reg.html";
                }

            }else {
                Map<String, String> collect = new HashMap<>();
                collect.put("code","验证码错误");
                model.addFlashAttribute("errors",collect);
                return "redirect:http://auth.swallowincense.com/reg.html";
            }
        }else{
            Map<String, String> collect = new HashMap<>();
            collect.put("code","验证码失效，请重新发送！");
            model.addFlashAttribute("errors",collect);
            return "redirect:http://auth.swallowincense.com/reg.html";
        }
    }
    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes model, HttpSession session){
        //远程登录
        R login = memberFeignService.login(vo);
        if(login.getCode()==0){
            MemberVo data = login.getData(new TypeReference<MemberVo>() {
            });
            session.setAttribute(AuthConstant.LOGIN_USER,data);
            return "redirect:http://swallowincense.com";
        }else{
            Map<String, String> collect = new HashMap<>();
            collect.put("msg",login.getData(new TypeReference<String>(){}));
            model.addFlashAttribute("errors",collect);
            return "redirect:http://auth.swallowincense.com/login.html";
        }

    }
}
