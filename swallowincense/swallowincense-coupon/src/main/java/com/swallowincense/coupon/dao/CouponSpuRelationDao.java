package com.swallowincense.coupon.dao;

import com.swallowincense.coupon.entity.CouponSpuRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券与产品关联
 * 
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 22:36:51
 */
@Mapper
public interface CouponSpuRelationDao extends BaseMapper<CouponSpuRelationEntity> {
	
}
