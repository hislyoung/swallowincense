package com.swallowincense.product.service.impl;

import com.swallowincense.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.common.utils.Query;

import com.swallowincense.product.dao.BrandDao;
import com.swallowincense.product.entity.BrandEntity;
import com.swallowincense.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = params.get("key").toString();
        QueryWrapper<BrandEntity> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(key)){
            queryWrapper.eq("brand_id",key).or().like("name",key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }
    @Transactional
    @Override
    public void updateDetails(BrandEntity brand) {
        this.updateById(brand);
        if(StringUtils.isNotBlank(brand.getName())){
            categoryBrandRelationService.updateBrand(brand.getBrandId(), brand.getName());
            //TODO 更新其他相关冗余字段
        }
    }

    @Override
    public List<BrandEntity> getBrandsByIds(List<Long> brandIds) {
        return baseMapper.selectList(new QueryWrapper<BrandEntity>().in("brand_id",brandIds));
    }

}