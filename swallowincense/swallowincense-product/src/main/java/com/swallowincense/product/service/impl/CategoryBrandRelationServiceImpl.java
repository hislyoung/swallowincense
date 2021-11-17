package com.swallowincense.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.swallowincense.product.dao.BrandDao;
import com.swallowincense.product.dao.CategoryDao;
import com.swallowincense.product.entity.BrandEntity;
import com.swallowincense.product.entity.CategoryEntity;
import com.swallowincense.product.service.BrandService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.common.utils.Query;

import com.swallowincense.product.dao.CategoryBrandRelationDao;
import com.swallowincense.product.entity.CategoryBrandRelationEntity;
import com.swallowincense.product.service.CategoryBrandRelationService;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {
    @Resource
    CategoryDao categoryDao;

    @Resource
    BrandDao brandDao;

    @Resource
    CategoryBrandRelationDao relationDao;

    @Resource
    BrandService brandService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetails(CategoryBrandRelationEntity categoryBrandRelation) {
        Long cateId = categoryBrandRelation.getCatelogId();
        Long brandId = categoryBrandRelation.getBrandId();
        CategoryEntity categoryEntity = categoryDao.selectById(cateId);
        BrandEntity brandEntity = brandDao.selectById(brandId);
        categoryBrandRelation.setCatelogName(categoryEntity.getName());
        categoryBrandRelation.setBrandName(brandEntity.getName());
        this.save(categoryBrandRelation);
    }

    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity entity = new CategoryBrandRelationEntity();
        entity.setBrandId(brandId);
        entity.setBrandName(name);
        this.update(entity,new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id",brandId));
    }

    @Override
    public void updateCategory(Long catId, String name) {
        baseMapper.updateCategory(catId,name);
    }

    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {
        if(catId!=0){
            List<CategoryBrandRelationEntity> entities = relationDao.selectList(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
            if(!CollectionUtils.isEmpty(entities)){
                return entities.stream().map(item -> {
                    Long brandId = item.getBrandId();
                    return brandService.getById(brandId);
                }).collect(Collectors.toList());
            }
        }
        return null;
    }
}