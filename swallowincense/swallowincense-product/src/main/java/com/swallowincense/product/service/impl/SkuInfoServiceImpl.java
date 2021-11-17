package com.swallowincense.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.swallowincense.common.utils.R;
import com.swallowincense.product.entity.SkuImagesEntity;
import com.swallowincense.product.entity.SpuInfoDescEntity;
import com.swallowincense.product.feign.SeckillFeignService;
import com.swallowincense.product.service.*;
import com.swallowincense.product.vo.SeckillInfoVo;
import com.swallowincense.product.vo.SkuItemVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.common.utils.Query;

import com.swallowincense.product.dao.SkuInfoDao;
import com.swallowincense.product.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService imagesService;
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    SeckillFeignService seckillFeignService;
    @Autowired
    ThreadPoolExecutor executor;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(StringUtils.isNotBlank(key)){
            queryWrapper.and(item-> item.eq("sku_id",key).or().like("sku_name",key));
        }
        String catelogId = (String) params.get("catelogId");
        if(StringUtils.isNotBlank(catelogId)){
            queryWrapper.eq("catalog_id",catelogId);
        }
        String brandId = (String) params.get("brandId");
        if(StringUtils.isNotBlank(brandId)){
            queryWrapper.eq("brand_id",brandId);
        }
        String min = (String) params.get("min");
        String max = (String) params.get("max");
        if(StringUtils.isNotBlank(min)&&StringUtils.isNotBlank(max)&&!max.equals("0")){
            queryWrapper.between("price",min,max);
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));

    }

    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo itemVo = new SkuItemVo();
        try {
            CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
                //Sku基本信息 pms_sku_info
                SkuInfoEntity info = getById(skuId);
                itemVo.setInfo(info);
                return info;
            }, executor);
            //sku的图片信息 pms_sku_images
            CompletableFuture imgFuture = CompletableFuture.runAsync(() -> {
                List<SkuImagesEntity> images = imagesService.getImagesBySkuId(skuId);
                itemVo.setImages(images);
            }, executor);
            //spu的销售属性组合
            CompletableFuture<Void> skuFuture = infoFuture.thenAcceptAsync((res) -> {
                Long spuId = res.getSpuId();
                List<SkuItemVo.SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(spuId);
                itemVo.setSaleAttr(saleAttrVos);
            }, executor);
            //获取spu的介绍
            CompletableFuture<Void> spuFuture = infoFuture.thenAcceptAsync((res) -> {
                Long spuId = res.getSpuId();
                SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(spuId);
                itemVo.setDesc(spuInfoDescEntity);
            }, executor);

            //获取Spu的规格参数
            CompletableFuture<Void> groupFuture = infoFuture.thenAcceptAsync((res) -> {
                Long spuId = res.getSpuId();
                Long catalogId = res.getCatalogId();
                List<SkuItemVo.SpuItemGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
                itemVo.setGroupAttrs(attrGroupVos);
            }, executor);

            //查询秒杀信息
            CompletableFuture<Void> secKillFuture = CompletableFuture.runAsync(()->{
                R skuSeckillInfo = seckillFeignService.getSkuSeckillInfo(skuId);
                if (skuSeckillInfo.getCode()==0) {
                    SeckillInfoVo data = skuSeckillInfo.getData(new TypeReference<SeckillInfoVo>() {
                    });
                    itemVo.setSeckillInfoVo(data);
                }
            },executor);

            CompletableFuture.allOf(imgFuture, skuFuture, spuFuture, groupFuture, secKillFuture).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return itemVo;
    }

}