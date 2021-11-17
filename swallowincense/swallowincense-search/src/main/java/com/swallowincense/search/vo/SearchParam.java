package com.swallowincense.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面可能的参数
 */
@Data
public class SearchParam {
    private String keyword;//输入全文检索，名字
    private Long catalog3Id;//三级分类ID
    /*
     * sort = saleCount_asc/desc
     * sort = skuPrice_asc/desc
     * sort = hotScore_asc/desc
     */
    private String sort;//排序有各种模式

    /*
     * 很多的过滤条件
     * hasStock(是否有货)、skuPrice区间，brandId,catalog3Id,attrs
     * hasStock=0/1
     * skuPrice=1_500_1000
     * brandId = 1
     * attrs = 2_5寸:6寸
     */
    private Integer hasStock;
    private String skuPrice;
    private List<Long> brandId;
    private List<String> attrs;
    private Integer pageNum = 1;//页码

    private String _queryString;//原生所有查询条件
}
