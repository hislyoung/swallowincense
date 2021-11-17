package com.swallowincense.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.ware.entity.WareOrderTaskEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 23:09:29
 */
public interface WareOrderTaskService extends IService<WareOrderTaskEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据订单号获得库存工作单
     * @param orderSn
     * @return
     */
    WareOrderTaskEntity getOrderTaskByOrderSn(String orderSn);
}

