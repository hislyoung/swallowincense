package com.swallowincense.member.service.impl;

import com.swallowincense.member.dao.MemberLevelDao;
import com.swallowincense.member.entity.MemberLevelEntity;
import com.swallowincense.member.exception.EmailExistException;
import com.swallowincense.member.exception.PhoneExistException;
import com.swallowincense.member.exception.UsernameExistException;
import com.swallowincense.member.vo.MemberLoginVo;
import com.swallowincense.member.vo.MemberRegistVo;
import com.swallowincense.member.vo.OAuth2UserInfoVo;
import com.swallowincense.member.vo.SocialUserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.common.utils.Query;

import com.swallowincense.member.dao.MemberDao;
import com.swallowincense.member.entity.MemberEntity;
import com.swallowincense.member.service.MemberService;
import org.springframework.transaction.annotation.Transactional;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {
        MemberEntity memberEntity = new MemberEntity();
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        //设置默认等级
        memberEntity.setLevelId(levelEntity.getId());
        //检查用户名手机号是否唯一
        checkPhoneUnique(vo.getPhone());

        checkUsernameUnique(vo.getUsername());

        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUsername());
        //加密存储
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);

        this.baseMapper.insert(memberEntity);
    }

    @Override
    public void checkEmailUnique(String email) throws EmailExistException{
        Integer integer = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("email", email));
        if(integer>0) {
            throw new EmailExistException();
        }
    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException{
        Integer integer = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if(integer>0) {
            throw new UsernameExistException();
        }
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException{
        Integer integer = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if(integer>0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();
        //去数据库查询
        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>()
                .eq("username", loginacct).or()
                .eq("mobile", loginacct).or()
                .eq("email", loginacct));
        if(entity==null){
            return null;
        }
        String passwordDb = entity.getPassword();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean matches = passwordEncoder.matches(password, passwordDb);
        if(matches){
            return entity;
        }else {
            return null;
        }
    }

    @Transactional
    @Override
    public MemberEntity login(SocialUserVo vo) {
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("gitee_id", vo.getId()));
        if(memberEntity==null) {
            MemberEntity entity = new MemberEntity();
            entity.setGiteeId(vo.getId());
            entity.setNickname(vo.getName());
            entity.setUsername(vo.getLogin());
            baseMapper.insert(entity);
            return entity;
        }else {
            return memberEntity;
        }
    }
}