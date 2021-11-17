package com.swallowincense.product.controller;

import java.util.Arrays;
import java.util.Map;

import com.swallowincense.product.vo.SpuSaveVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.swallowincense.product.entity.SpuInfoEntity;
import com.swallowincense.product.service.SpuInfoService;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.common.utils.R;



/**
 * spu信息
 *
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 19:55:16
 */
@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;

    /**
     * 根据SKU查询SPU信息
     */
    @GetMapping("/skuId/{id}")
    public R getSpuInfoBySku(@PathVariable("id")Long id){
        SpuInfoEntity entity= spuInfoService.getSpuInfoBySku(id);
        return R.ok().setData(entity);
    }

    /**
     * 商品上架功能
     */
    @RequestMapping("/{spuId}/up")
    public R spuUp(@PathVariable("spuId")Long spuId){
        spuInfoService.up(spuId);

        return R.ok();
    }
    /**
     * Spu检索
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        /*PageUtils page = spuInfoService.queryPage(params);*/
        PageUtils page = spuInfoService.queryPageByCondition(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return R.ok().put("spuInfo", spuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SpuSaveVo vo){
		//spuInfoService.save(spuInfo);
        spuInfoService.saveSpuInfo(vo);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SpuInfoEntity spuInfo){
		spuInfoService.updateById(spuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
