package com.swallowincense.search.controller;


import com.swallowincense.common.exception.BizCodeEnum;
import com.swallowincense.common.to.es.SkuEsModel;
import com.swallowincense.common.utils.R;
import com.swallowincense.search.service.ElasticSearchSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/search")
public class ElasticSearchSaveController {
    @Autowired
    ElasticSearchSaveService elasticSearchSaveService;
    //上架商品检索
    @RequestMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels){
        Boolean up = false;
        try {
            up = elasticSearchSaveService.productStatusUp(skuEsModels);
        } catch (Exception e) {
            log.error("ElasticSearchSaveController商品上架失败，原因是：{}",e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if(!up) {
            return R.ok();
        }else {
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
    }
}
