package com.swallowincense.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.ware.entity.WareInfoEntity;
import com.swallowincense.ware.vo.FareVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 仓库信息
 *
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 23:09:29
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据收货地址计算运费
     * @param addrId
     * @return
     */
    FareVo getFare(Long addrId);
}

