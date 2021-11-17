package com.swallowincense.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.coupon.entity.CouponSpuRelationEntity;

import java.util.Map;

/**
 * 优惠券与产品关联
 *
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 22:36:51
 */
public interface CouponSpuRelationService extends IService<CouponSpuRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

