package com.swallowincense.product.dao;

import com.swallowincense.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 19:55:16
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
