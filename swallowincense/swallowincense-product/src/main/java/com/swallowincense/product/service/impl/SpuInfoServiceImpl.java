package com.swallowincense.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.swallowincense.common.constant.ProductConstant;
import com.swallowincense.common.to.MemberPrice;
import com.swallowincense.common.to.SkuHasStockVo;
import com.swallowincense.common.to.SkuReductionTo;
import com.swallowincense.common.to.SpuBoundTo;
import com.swallowincense.common.to.es.SkuEsModel;
import com.swallowincense.common.utils.R;
import com.swallowincense.product.entity.*;
import com.swallowincense.product.feign.CouponFeignService;
import com.swallowincense.product.feign.SearchFeignService;
import com.swallowincense.product.feign.WareFeignService;
import com.swallowincense.product.service.*;
import com.swallowincense.product.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.common.utils.Query;

import com.swallowincense.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    SpuImagesService spuImagesService;
    @Autowired
    AttrService attrService;
    @Autowired
    ProductAttrValueService productAttrValueService;
    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SkuSaleAttrValueService saleAttrValueService;
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    BrandService brandService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    SearchFeignService searchFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //1?????????Spu????????????pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        //2?????????Spu???????????????pms_spu_info_desc
        List<String> descript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuInfoEntity.getId());
        descEntity.setDecript(String.join(",", descript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);

        //3?????????Spu????????????pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);

        //4?????????Spu???????????????pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(item -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(item.getAttrId());
            AttrEntity entity = attrService.getById(item.getAttrId());
            valueEntity.setAttrName(entity.getAttrName());
            valueEntity.setAttrValue(item.getAttrValues());
            valueEntity.setQuickShow(item.getShowDesc());
            valueEntity.setSpuId(spuInfoEntity.getId());
            return valueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(collect);

        //5?????????Spu?????????Sku??????
        //5.1???Sku???????????????pms_sku_info
        List<Skus> skus = vo.getSkus();
        if (!CollectionUtils.isEmpty(skus)) {
            skus.forEach(item -> {
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.save(skuInfoEntity);

                //5.2???Sku???????????????pms_sku_images
                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity imagesEntity = new SkuImagesEntity();
                    imagesEntity.setSkuId(skuId);
                    imagesEntity.setImgUrl(img.getImgUrl());
                    imagesEntity.setDefaultImg(img.getDefaultImg());
                    return imagesEntity;
                }).filter(image->{
                    //??????true?????????????????????false???????????????
                    return StringUtils.isNotBlank(image.getImgUrl());
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(imagesEntities);

                //5.3???Sku??????????????????pms_sku_sale_attr_value
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity entity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, entity);
                    entity.setSkuId(skuId);
                    return entity;
                }).collect(Collectors.toList());
                saleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                //5.4?????????Spu???????????????sms_spu_bounds
                Bounds bounds = vo.getBounds();
                SpuBoundTo spuBoundTo = new SpuBoundTo();
                BeanUtils.copyProperties(bounds,spuBoundTo);
                spuBoundTo.setSpuId(spuInfoEntity.getId());
                R r = couponFeignService.saveSpuBounds(spuBoundTo);
                if(r.getCode()!=0){
                    log.error("????????????Spu??????????????????");
                }
                //5.5???Sku??????????????????????????????sms_sku_ladder(??????)->sms_sku_full_reduction(??????)->sms_member_price(????????????)
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item,skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                List<MemberPrice> memberPrices = item.getMemberPrice().stream().map(price -> {
                    MemberPrice memberPrice = new MemberPrice();
                    BeanUtils.copyProperties(price, memberPrice);
                    return memberPrice;
                }).collect(Collectors.toList());
                skuReductionTo.setMemberPrice(memberPrices);
                if(skuReductionTo.getFullCount() <= 0 || skuReductionTo.getFullPrice().compareTo(BigDecimal.ZERO) > 0){
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if(r1.getCode()!=0){
                        log.error("????????????Sku??????????????????");
                    }
                }

                //5.6???????????????

            });
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String)params.get("key");
        if(StringUtils.isNotBlank(key)){
            queryWrapper.and(item->{
                item.eq("id",key).or().like("spu_name",key);
            });
        }
        String status = (String)params.get("status");
        if(StringUtils.isNotBlank(status)){
            queryWrapper.eq("publish_status",status);
        }
        String brandId = (String)params.get("brandId");
        if(StringUtils.isNotBlank(brandId)){
            queryWrapper.eq("brand_id",brandId);
        }
        String catelogId = (String)params.get("catelogId");
        if(StringUtils.isNotBlank(catelogId)){
            queryWrapper.eq("catalog_id",catelogId);
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        //1???????????????spuId???????????????Sku????????????????????????
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        //????????????Sku????????????????????????????????????????????????
        List<ProductAttrValueEntity> valueEntities = productAttrValueService
                .list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
        List<Long> attrIds = valueEntities.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        List<Long> searchAttrIds =attrService.selectSearchAttrs(attrIds);
        Set<Long> idSet = new HashSet<>(searchAttrIds);
        List<SkuEsModel.Attrs> attrsList = valueEntities.stream()
                .filter(item -> idSet.contains(item.getAttrId()))
                .map(item -> {
                    SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(item, attrs);
                    return attrs;
                }).collect(Collectors.toList());
        Map<Long, Boolean> stockMap = null;
        try{
            R skusHasStock = wareFeignService.getSkusHasStock(skuIds);
            stockMap = skusHasStock.getData(new TypeReference<List<SkuHasStockVo>>(){})
                    .stream()
                    .collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
        }catch (Exception e){
            log.error("?????????????????????????????????{}",e);
        }

        //2??????????????????Sku??????
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> upProducts = skus.stream().map(sku -> {
            //?????????????????????
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(sku,esModel);
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());
            //??????????????????
            if(finalStockMap == null){
                esModel.setHasStock(true);
            }else {
                esModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }
            BrandEntity brandEntity = brandService.getById(esModel.getBrandId());
            esModel.setBrandName(brandEntity.getName());
            esModel.setBrandImg(brandEntity.getLogo());
            CategoryEntity categoryEntity = categoryService.getById(esModel.getCatalogId());
            esModel.setCatelogName(categoryEntity.getName());
            //???????????????????????????
            esModel.setHotScore(0L);
            //??????????????????
            esModel.setAttrs(attrsList);
            return esModel;
        }).collect(Collectors.toList());

        //??????????????????ES???????????????swallowincense-search
        R r = searchFeignService.productStatusUp(upProducts);
        if(r.getCode()==0){
            //TODO//??????????????????
            baseMapper.updateSpuStatus(spuId, ProductConstant.UpStatusEnum.UP_SPU.getCode());
        }else {
            //TODO//??????????????????????????????????????????????????????
            /* Feign????????????
             * ??????ReflectiveFeign?????????invoke??????
             * ????????????dispatch.get(method).invoke(args)
             * SynchronousMethodHandler???invoke?????????
             * ?????????????????????????????????????????????????????????
             * RequestTemplate template = buildTemplateFromArgs.create(argv);
             * ????????????????????????
             * executeAndDecode(template, options)
             * Retryer retryer = this.retryer.clone();???????????????????????????
             * ??????Retryer??? public Default() {this(100, SECONDS.toMillis(1), 5);}???????????????????????????????????????5???
             * Retryer NEVER_RETRY = new Retryer() NEVER_RETRY??????????????????????????????????????????????????????
             */
        }

    }

    @Override
    public SpuInfoEntity getSpuInfoBySku(Long id) {
        SkuInfoEntity byId = skuInfoService.getById(id);
        Long spuId = byId.getSpuId();
        return getById(spuId);
    }


}