package com.swallowincense.product.dao;

import com.swallowincense.product.entity.CommentReplayEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价回复关系
 * 
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 19:55:15
 */
@Mapper
public interface CommentReplayDao extends BaseMapper<CommentReplayEntity> {
	
}
