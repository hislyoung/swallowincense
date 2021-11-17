package com.swallowincense.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.order.entity.OrderItemEntity;

import java.util.Map;

/**
 * 订单项信息
 *
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 23:02:16
 */
public interface OrderItemService extends IService<OrderItemEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

