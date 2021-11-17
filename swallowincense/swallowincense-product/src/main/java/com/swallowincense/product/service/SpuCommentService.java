package com.swallowincense.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.product.entity.SpuCommentEntity;

import java.util.Map;

/**
 * 商品评价
 *
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 19:55:16
 */
public interface SpuCommentService extends IService<SpuCommentEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

