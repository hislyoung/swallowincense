package com.swallowincense.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.product.entity.ProductAttrValueEntity;

import java.util.Map;

/**
 * spu属性值
 *
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 19:55:16
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

