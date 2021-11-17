package com.swallowincense.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.swallowincense.common.exception.BizCodeEnum;
import com.swallowincense.ware.exception.NoStockException;
import com.swallowincense.ware.vo.LockStockResult;
import com.swallowincense.ware.vo.SkuHasStockVo;
import com.swallowincense.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.swallowincense.ware.entity.WareSkuEntity;
import com.swallowincense.ware.service.WareSkuService;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.common.utils.R;



/**
 * 商品库存
 *
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 23:09:29
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 锁定库存方法
     */
    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo){
        try {
            Boolean lockStock = wareSkuService.orderLockStock(vo);
            return R.ok();
        }catch (NoStockException e){
            return R.error(BizCodeEnum.NO_STOCK_EXCEPTION.getCode(),BizCodeEnum.NO_STOCK_EXCEPTION.getMsg());
        }
    }
    /**
     * 查询是否有库存
     */
    @PostMapping("/hasStock")
    public R getSkusHasStock(@RequestBody List<Long> skuIds){
        List<SkuHasStockVo> vos = wareSkuService.getSkusHasStock(skuIds);
        return R.ok().setData(vos);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
