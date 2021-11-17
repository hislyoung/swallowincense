package com.swallowincense.member.dao;

import com.swallowincense.member.entity.MemberReceiveAddressEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 会员收货地址
 * 
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 22:54:02
 */
@Mapper
public interface MemberReceiveAddressDao extends BaseMapper<MemberReceiveAddressEntity> {

    void updateInBatchByMemberId(@Param("memberId") Long memberId);
}
