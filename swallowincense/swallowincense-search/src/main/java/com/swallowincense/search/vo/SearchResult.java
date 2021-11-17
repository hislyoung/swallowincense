package com.swallowincense.search.vo;

import com.swallowincense.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResult {
    //所有商品信息
    List<SkuEsModel> products;
    //分页信息
    private Integer pageNum;//当前页码
    private Long total;//总记录数
    private Integer totalPages;//总页码

    private List<BrandVo> brands;//当前查询到的结果，所有涉及到的商品
    private List<CatalogVo> catalogs;//当前查询到的结果涉及的所有分类
    private List<AttrVo> attrs;//当前查询到的商品所涉及的属性
    private List<Integer> pageNavs;//页面集合
    private List<NavVo> navs = new ArrayList<>();//面包屑导航
    private List<Long> attrIds = new ArrayList<>();

    //==================返回分类的信息========================//
    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }
    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class NavVo{
        private String navName;
        private String navValue;
        private String link;
    }

}
