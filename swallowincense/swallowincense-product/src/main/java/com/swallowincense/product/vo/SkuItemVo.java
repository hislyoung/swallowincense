package com.swallowincense.product.vo;

import com.swallowincense.product.entity.SkuImagesEntity;
import com.swallowincense.product.entity.SkuInfoEntity;
import com.swallowincense.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {
    boolean hasStock = true;
    //Sku基本信息 pms_sku_info
    private SkuInfoEntity info;
    //sku的图片信息 pms_sku_images
    private List<SkuImagesEntity> images;
    //spu的销售属性组合
    private List<SkuItemSaleAttrVo> saleAttr;
    //获取spu的介绍
    private SpuInfoDescEntity desc;

    //获取Spu的规格参数
    private List<SpuItemGroupVo> groupAttrs;

    //秒杀信息
    private SeckillInfoVo seckillInfoVo;

    @Data
    public static class SkuItemSaleAttrVo{
        private Long attrId;
        private String attrName;
        private List<AttrValueWithSkuIdVo> attrValues;
    }

    @Data
    public static class AttrValueWithSkuIdVo{
        private String attrValue;
        private String skuIds;

    }

    @Data
    public static class SpuItemGroupVo{
        private String groupName;
        private List<SpuBaseAttrVo> attrs;
    }
    @Data
    public static class SpuBaseAttrVo{
        private String attrName;
        private String attrValue;
    }
}
