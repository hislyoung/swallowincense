package com.swallowincense.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.product.entity.AttrEntity;
import com.swallowincense.product.vo.AttrGroupRelationVo;
import com.swallowincense.product.vo.AttrResponseVo;
import com.swallowincense.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 19:55:16
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, String type, Long catelogId);

    void saveAttr(AttrVo attr);

    AttrResponseVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrGroupId);

    void deleteRelation(AttrGroupRelationVo... relationVos);

    PageUtils getNoAttrRelation(Map<String, Object> params, Long attrGroupId);

    /***
     * 获得可检索的attr
     * @param attrIds
     * @return
     */
    List<Long> selectSearchAttrs(List<Long> attrIds);
}

