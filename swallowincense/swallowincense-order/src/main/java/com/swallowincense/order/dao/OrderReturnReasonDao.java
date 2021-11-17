package com.swallowincense.order.dao;

import com.swallowincense.order.entity.OrderReturnReasonEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退货原因
 * 
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 23:02:16
 */
@Mapper
public interface OrderReturnReasonDao extends BaseMapper<OrderReturnReasonEntity> {
	
}
