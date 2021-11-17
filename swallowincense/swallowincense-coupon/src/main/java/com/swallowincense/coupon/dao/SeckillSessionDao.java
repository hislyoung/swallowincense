package com.swallowincense.coupon.dao;

import com.swallowincense.coupon.entity.SeckillSessionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀活动场次
 * 
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 22:36:50
 */
@Mapper
public interface SeckillSessionDao extends BaseMapper<SeckillSessionEntity> {
	
}
