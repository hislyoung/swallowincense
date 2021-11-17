package com.swallowincense.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.product.entity.SkuSaleAttrValueEntity;
import com.swallowincense.product.vo.SkuItemVo;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 19:55:16
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuItemVo.SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId);

    List<String> getSkuSaleAttrValues(Long skuId);
}

