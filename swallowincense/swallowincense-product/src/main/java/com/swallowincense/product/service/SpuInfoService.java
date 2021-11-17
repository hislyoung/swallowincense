package com.swallowincense.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.product.entity.SpuInfoEntity;
import com.swallowincense.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 19:55:16
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo vo);

    void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);
    /**
     * 商品上架功能
     * @Param spuId
     */
    void up(Long spuId);

    /**
     * 根据Sku找到Spu信息
     * @param id
     * @return
     */
    SpuInfoEntity getSpuInfoBySku(Long id);
}

