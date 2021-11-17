package com.swallowincense.member.dao;

import com.swallowincense.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 22:54:01
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
