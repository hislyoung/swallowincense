package com.swallowincense.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.swallowincense.product.entity.AttrEntity;
import com.swallowincense.product.service.AttrAttrgroupRelationService;
import com.swallowincense.product.service.AttrService;
import com.swallowincense.product.service.CategoryService;
import com.swallowincense.product.vo.AttrGroupRelationVo;
import com.swallowincense.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.swallowincense.product.entity.AttrGroupEntity;
import com.swallowincense.product.service.AttrGroupService;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.common.utils.R;



/**
 * 属性分组
 *
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 19:55:16
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private AttrAttrgroupRelationService relationService;
    /**
     * 关联分组查询
     */
    @GetMapping("{attrGroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrGroupId") Long attrGroupId){
        List<AttrEntity> attrEntities = attrService.getRelationAttr(attrGroupId);
        return R.ok().put("data",attrEntities);
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params,@PathVariable("catelogId")Long catelogId){
        //PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] catelogIds = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(catelogIds);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    /**
     * 删除分组与规格参数关联关系
     */
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] relationVos){
        attrService.deleteRelation(relationVos);
        return R.ok();
    }
    /**
     * 获取没有关联的属性分组
     */
    @GetMapping("/{attrGroupId}/noattr/relation")
    public R noAttrRelation(
            @RequestParam Map<String,Object> params,
            @PathVariable("attrGroupId")Long attrGroupId){
        PageUtils page = attrService.getNoAttrRelation(params,attrGroupId);
        return R.ok().put("page",page);
    }

    /**
     * 添加分组与规格参数的关联关系
     * /product/attrgroup
     */
    @PostMapping("/attr/relation")
    public R attrRelation(@RequestBody List<AttrGroupRelationVo> relationVos){
        relationService.saveBatch(relationVos);
        return R.ok();
    }

    /**
     * 获取分类下所有分组&关联属性
     * /product/attrgroup/{catelogId}/withattr
     */
    @GetMapping("/{catelogId}/withattr")
    public R withattr(@PathVariable("catelogId") Long catelogId){
        List<AttrGroupWithAttrsVo> vos = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data",vos);
    }
}
