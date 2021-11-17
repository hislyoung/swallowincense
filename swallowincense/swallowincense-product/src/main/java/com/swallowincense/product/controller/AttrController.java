package com.swallowincense.product.controller;

import java.util.Arrays;
import java.util.Map;

import com.swallowincense.product.vo.AttrResponseVo;
import com.swallowincense.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.swallowincense.product.entity.AttrEntity;
import com.swallowincense.product.service.AttrService;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.common.utils.R;



/**
 * 商品属性
 *
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 19:55:16
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }

    @RequestMapping("/{type}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params,
                  @PathVariable("type") String type,
                  @PathVariable("catelogId") Long catelogId){
        //PageUtils page = attrService.queryPage(params);
        PageUtils page = attrService.queryPage(params,type,catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId){
		//AttrEntity attr = attrService.getById(attrId);
        AttrResponseVo attr = attrService.getAttrInfo(attrId);
        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo attr){
		attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVo attr){
		attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));
        return R.ok();
    }


}
