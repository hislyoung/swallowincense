package com.swallowincense.member.controller;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import com.swallowincense.common.exception.BizCodeEnum;
import com.swallowincense.member.exception.EmailExistException;
import com.swallowincense.member.exception.PhoneExistException;
import com.swallowincense.member.exception.UsernameExistException;
import com.swallowincense.member.feign.CouponFeignService;
import com.swallowincense.member.vo.MemberLoginVo;
import com.swallowincense.member.vo.MemberRegistVo;
import com.swallowincense.member.vo.SocialUserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.swallowincense.member.entity.MemberEntity;
import com.swallowincense.member.service.MemberService;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.common.utils.R;



/**
 * 会员
 *
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 22:54:01
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    @PostMapping("/oauth2/login")
    @ResponseBody
    public R oauthLogin(@RequestBody SocialUserVo vo){
        MemberEntity entity= memberService.login(vo);
        if(entity!=null){
            return R.ok().setData(entity);
        }else {
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getCode(),BizCodeEnum.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getMsg());
        }

    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo){
        MemberEntity entity= memberService.login(vo);
        if(entity!=null){
            return R.ok().setData(entity);
        }else {
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getCode(),BizCodeEnum.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getMsg());
        }

    }

    @PostMapping("regist")
    public R regist(@RequestBody MemberRegistVo vo ){
        try {
            memberService.regist(vo);
        }catch (EmailExistException e){
            R.error(BizCodeEnum.EMAIL_EXIST_EXCEPTION.getCode(),BizCodeEnum.EMAIL_EXIST_EXCEPTION.getMsg());
        }catch (PhoneExistException e){
            R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        }catch (UsernameExistException e){
            R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(),BizCodeEnum.USER_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    @RequestMapping("/coupons")
    public R test(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("LiuYang");
        R memberCoupons = couponFeignService.memberCoupons();
        return Objects.requireNonNull(R.ok().put("member", memberEntity)).put("coupons",memberCoupons.get("coupons"));
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
