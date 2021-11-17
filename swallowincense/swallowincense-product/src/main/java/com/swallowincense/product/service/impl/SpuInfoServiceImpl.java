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
        //1、保存Spu基本信息pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        //2、保存Spu的描述图片pms_spu_info_desc
        List<String> descript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuInfoEntity.getId());
        descEntity.setDecript(String.join(",", descript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);

        //3、保存Spu的图片集pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);

        //4、保存Spu的规格参数pms_product_attr_value
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

        //5、保存Spu的所有Sku信息
        //5.1、Sku的基本信息pms_sku_info
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

                //5.2、Sku的图片信息pms_sku_images
                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity imagesEntity = new SkuImagesEntity();
                    imagesEntity.setSkuId(skuId);
                    imagesEntity.setImgUrl(img.getImgUrl());
                    imagesEntity.setDefaultImg(img.getDefaultImg());
                    return imagesEntity;
                }).filter(image->{
                    //返回true就是需要，返回false需要过滤掉
                    return StringUtils.isNotBlank(image.getImgUrl());
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(imagesEntities);

                //5.3、Sku的销售属性值pms_sku_sale_attr_value
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity entity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, entity);
                    entity.setSkuId(skuId);
                    return entity;
                }).collect(Collectors.toList());
                saleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                //5.4、保存Spu的积分信息sms_spu_bounds
                Bounds bounds = vo.getBounds();
                SpuBoundTo spuBoundTo = new SpuBoundTo();
                BeanUtils.copyProperties(bounds,spuBoundTo);
                spuBoundTo.setSpuId(spuInfoEntity.getId());
                R r = couponFeignService.saveSpuBounds(spuBoundTo);
                if(r.getCode()!=0){
                    log.error("远程保存Spu积分信息失败");
                }
                //5.5、Sku的优惠信息和满减信息sms_sku_ladder(优惠)->sms_sku_full_reduction(满减)->sms_member_price(会员价格)
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
                        log.error("远程保存Sku优惠信息失败");
                    }
                }

                //5.6、会员信息

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
        //1、查出当前spuId对应的所有Sku信息，品牌的名字
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        //查询当前Sku下的所有可以被用来检索的规格属性
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
            log.error("库存查询出现错误：原因{}",e);
        }

        //2、封装每一个Sku信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> upProducts = skus.stream().map(sku -> {
            //组装需要的数据
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(sku,esModel);
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());
            //设置库存信息
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
            //热度评分，后台可控
            esModel.setHotScore(0L);
            //设置检索属性
            esModel.setAttrs(attrsList);
            return esModel;
        }).collect(Collectors.toList());

        //将数据发送给ES进行保存：swallowincense-search
        R r = searchFeignService.productStatusUp(upProducts);
        if(r.getCode()==0){
            //TODO//修改上架状态
            baseMapper.updateSpuStatus(spuId, ProductConstant.UpStatusEnum.UP_SPU.getCode());
        }else {
            //TODO//重复调用问题？接口的幂等性；重试机制
            /* Feign调用流程
             * 调用ReflectiveFeign类下的invoke方法
             * 发送请求dispatch.get(method).invoke(args)
             * SynchronousMethodHandler的invoke方法中
             * 构造请求的模板包含请求信息以及数据信息
             * RequestTemplate template = buildTemplateFromArgs.create(argv);
             * 执行并将数据解码
             * executeAndDecode(template, options)
             * Retryer retryer = this.retryer.clone();重试器默认是关闭的
             * 在类Retryer中 public Default() {this(100, SECONDS.toMillis(1), 5);}默认的重试器最大重试次数为5次
             * Retryer NEVER_RETRY = new Retryer() NEVER_RETRY为永不重试，发现错误直接抛出异常结束
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