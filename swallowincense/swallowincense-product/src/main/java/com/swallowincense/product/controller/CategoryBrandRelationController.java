package com.swallowincense.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.swallowincense.product.entity.BrandEntity;
import com.swallowincense.product.vo.BrandVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import com.swallowincense.product.entity.CategoryBrandRelationEntity;
import com.swallowincense.product.service.CategoryBrandRelationService;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.common.utils.R;



/**
 * 品牌分类关联
 *
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-30 20:53:07
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:categorybrandrelation:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 获取当前品牌关联的所有分类列表
     */
    @GetMapping("/catelog/list")
    //@RequiresPermissions("product:categorybrandrelation:list")
    public R catelogList(@RequestParam("brandId")String brandId){
        //PageUtils page = categoryBrandRelationService.queryPage(params);
        List<CategoryBrandRelationEntity> entities = categoryBrandRelationService.list(
                new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id",brandId));
        return R.ok().put("data", entities);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("product:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);
        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:categorybrandrelation:save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.saveDetails(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 获得分类下的所有品牌
     */
    @GetMapping("/brands/list")
    public R brandsList(@RequestParam("catId")Long catId){
        List<BrandEntity> brandEntities = categoryBrandRelationService.getBrandsByCatId(catId);
        List<BrandVo> collect = null;
        if(!CollectionUtils.isEmpty(brandEntities)){
             collect = brandEntities.stream().map(item -> {
                BrandVo brandVo = new BrandVo();
                brandVo.setBrandId(item.getBrandId());
                brandVo.setBrandName(item.getName());
                return brandVo;
            }).collect(Collectors.toList());
        }
        return R.ok().put("data",collect);
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:categorybrandrelation:delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
