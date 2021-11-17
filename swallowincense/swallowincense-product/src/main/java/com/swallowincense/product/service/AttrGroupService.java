package com.swallowincense.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.product.entity.AttrGroupEntity;
import com.swallowincense.product.vo.AttrGroupWithAttrsVo;
import com.swallowincense.product.vo.SkuItemVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 19:55:16
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId);

    List<SkuItemVo.SpuItemGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}


