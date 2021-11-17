package com.swallowincense.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.swallowincense.common.constant.ProductConstant;
import com.swallowincense.product.dao.AttrAttrgroupRelationDao;
import com.swallowincense.product.dao.AttrGroupDao;
import com.swallowincense.product.dao.CategoryDao;
import com.swallowincense.product.entity.AttrAttrgroupRelationEntity;
import com.swallowincense.product.entity.AttrGroupEntity;
import com.swallowincense.product.entity.CategoryEntity;
import com.swallowincense.product.service.CategoryService;
import com.swallowincense.product.vo.AttrGroupRelationVo;
import com.swallowincense.product.vo.AttrResponseVo;
import com.swallowincense.product.vo.AttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.common.utils.Query;

import com.swallowincense.product.dao.AttrDao;
import com.swallowincense.product.entity.AttrEntity;
import com.swallowincense.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Resource
    AttrAttrgroupRelationDao relationDao;

    @Resource
    AttrGroupDao attrGroupDao;

    @Resource
    CategoryDao categoryDao;

    @Autowired
    CategoryService categoryService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, String type, Long catelogId) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("attr_type","base".equalsIgnoreCase(type)
                ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()
                : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        String key = (String) params.get("key");
        if (catelogId != 0) {
            queryWrapper.and(item->item.eq("catelog_id", catelogId));
        }
        if(StringUtils.isNotBlank(key)){
            queryWrapper.and(item->item.eq("attr_id",key).or().like("attr_name",key));
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );
        PageUtils pageUtils = new PageUtils(page);

        List<AttrEntity> records = page.getRecords();

        List<AttrResponseVo> responseVos = records.stream().map((attrEntity) -> {
            AttrResponseVo attrResponseVo = new AttrResponseVo();
            BeanUtils.copyProperties(attrEntity, attrResponseVo);
            //设置分类和分组的名字
            if(type.equals(ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode())) {
                AttrAttrgroupRelationEntity relationEntity = relationDao.selectById(attrEntity.getAttrId());
                if (relationEntity != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                    attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrResponseVo.setCatelogName(categoryEntity.getName());
            }
            return attrResponseVo;
        }).collect(Collectors.toList());
        pageUtils.setList(responseVos);
        return pageUtils;
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        //可以一个一个赋值，但是太麻烦
        //BeanUtils.copyProperties(源, 目标) 将源里存在的的与目标命名一致的值赋值到目标中
        BeanUtils.copyProperties(attr,attrEntity);
        this.save(attrEntity);
        if(attr.getAttrType().equals(ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) && attr.getAttrGroupId() != null){
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            //对于新增的值，自动创建ID需要用返回的对象
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationDao.insert(relationEntity);
        }
    }

    @Cacheable(value = "attr",key = "'attrInfo:'+#root.args[0]")
    @Override
    public AttrResponseVo getAttrInfo(Long attrId) {
        AttrResponseVo attrResponseVo = new AttrResponseVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity,attrResponseVo);
        if(attrEntity.getAttrType().equals(ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode())) {
            AttrAttrgroupRelationEntity entity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (entity != null) {
                attrResponseVo.setAttrGroupId(entity.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(entity.getAttrGroupId());
                if (attrGroupEntity != null) {
                    attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }
        Long[] catelogPath = categoryService.findCatelogPath(attrEntity.getCatelogId());
        attrResponseVo.setCatelogPath(catelogPath);
        CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
        if(categoryEntity!=null){
            attrResponseVo.setCatelogName(categoryEntity.getName());
        }
        return attrResponseVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr,attrEntity);
        this.updateById(attrEntity);
        //修改分组关联
        if(attr.getAttrType().equals(ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode())) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attr.getAttrId());
            Integer count = relationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
            if (count > 0) {
                relationDao.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
            } else {
                relationDao.insert(relationEntity);
            }
        }
    }
    /**
     * 根据分组Id查找关联的所有基本属性
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrGroupId) {
        List<AttrAttrgroupRelationEntity> entities = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupId));
        List<Long> longs = entities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(longs)){
            return null;
        }
        return (List<AttrEntity>) this.listByIds(longs);
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo... relationVos) {
        //relationDao.delete(new QueryWrapper<>().eq("attr_id",1L).eq("attr_group_id",1L));
        //批量删除
        List<AttrAttrgroupRelationEntity> collect = Arrays.stream(relationVos).map((item) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        relationDao.deleteBatchRelation(collect);
    }

    /**
     * 获取当前分组没有关联的规格属性
     * @param params
     * @param attrGroupId
     * @return
     */
    @Override
    public PageUtils getNoAttrRelation(Map<String, Object> params, Long attrGroupId) {
        //当前分组只能关联自己所属分类的里的属性
        AttrGroupEntity groupEntity = attrGroupDao.selectById(attrGroupId);
        //当前分组只能关联别的分组没有引用的属性
        Long catelogId = groupEntity.getCatelogId();
        //获取所偶的分组信息,不包含当前分组
        List<AttrGroupEntity> groupEntities = attrGroupDao.selectList(
                new QueryWrapper<AttrGroupEntity>()
                        .eq("catelog_id", catelogId));
        List<Long> attrGroupIds = groupEntities.stream()
                .map(AttrGroupEntity::getAttrGroupId)
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(attrGroupIds)){
            return null;
        }
        List<AttrAttrgroupRelationEntity> groupIds = relationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>()
                        .in("attr_group_id", attrGroupIds));
        List<Long> attrIds = groupIds.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>()
                .eq("catelog_id", catelogId).eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if(!CollectionUtils.isEmpty(attrIds)){
            queryWrapper.notIn("attr_id", attrIds);
        }
        String key = params.get("key").toString();
        if (StringUtils.isNotBlank(key)) {
            queryWrapper.and(item->
                item.eq("attr_id",key).or().like("attr_name",key)
            );
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public List<Long> selectSearchAttrs(List<Long> attrIds) {
        return baseMapper.selectSearchAttrs(attrIds);
    }

}