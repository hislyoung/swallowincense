package com.swallowincense.product.service.impl;

import com.swallowincense.product.entity.AttrEntity;
import com.swallowincense.product.service.AttrService;
import com.swallowincense.product.vo.AttrGroupWithAttrsVo;
import com.swallowincense.product.vo.SkuItemVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.common.utils.Query;

import com.swallowincense.product.dao.AttrGroupDao;
import com.swallowincense.product.entity.AttrGroupEntity;
import com.swallowincense.product.service.AttrGroupService;

import javax.annotation.Resource;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Resource
    AttrService attrService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        if (catelogId == 0) {
            return queryPage(params);
        } else {
            QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId);
            String key = params.get("key").toString();
            if (StringUtils.isNotBlank(key)) {
                queryWrapper.and((obj) -> obj.eq("attr_group_id", key).or().like("attr_group_name", key));
            }
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    queryWrapper
            );
            return new PageUtils(page);
        }
    }

    /**
     * 根据三级分类Id查出所有分组信息
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<AttrGroupWithAttrsVo> collect = groupEntities.stream().map(item -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item,attrGroupWithAttrsVo);
            List<AttrEntity> relationAttr = attrService.getRelationAttr(attrGroupWithAttrsVo.getAttrGroupId());
            attrGroupWithAttrsVo.setAttrs(relationAttr);
            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());

        return collect;
    }

    @Override
    public List<SkuItemVo.SpuItemGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        //查出当前Spu对应的所有属性的分组信息以及当前所有分组下的所有属性值
        return baseMapper.getAttrGroupWithAttrsBySpuId(spuId, catalogId);

    }
}