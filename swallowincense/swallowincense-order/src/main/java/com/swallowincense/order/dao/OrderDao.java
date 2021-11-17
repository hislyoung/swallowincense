package com.swallowincense.order.dao;

import com.swallowincense.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 23:02:16
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
