package com.swallowincense.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.swallowincense.common.to.es.SkuEsModel;
import com.swallowincense.common.utils.R;
import com.swallowincense.search.config.ElasticSearchConfig;
import com.swallowincense.search.constant.EsConstant;
import com.swallowincense.search.feign.ProductFeignService;
import com.swallowincense.search.service.MallSearchService;
import com.swallowincense.search.vo.AttrResponseVo;
import com.swallowincense.search.vo.BrandVo;
import com.swallowincense.search.vo.SearchParam;
import com.swallowincense.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.util.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service(value = "mallSearchService")
public class MallSearchServiceImpl implements MallSearchService {
    @Autowired
    @Qualifier(value = "elasticSearchClient")
    private RestHighLevelClient client;
    @Autowired
    ProductFeignService productFeignService;
    //ES??????
    @Override
    public SearchResult search(SearchParam param) {
        //??????????????????
        //??????????????????
        SearchRequest searchRequest = buildSearchRequest(param);
        SearchResult searchResult = null;
        try {
            //??????????????????
            SearchResponse response = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
            searchResult = buildSearchResult(response,param);
            //???????????????????????????
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResult;
    }

    /**
     * ??????????????????
     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        /**
         * ???????????????????????????????????????????????????????????????????????????DSL??????
         */
        //1???bool??????
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1.1???must
        if(StringUtils.isNotBlank(param.getKeyword())){
            //????????????
            boolQuery.must(QueryBuilders.matchQuery("skuTitle",param.getKeyword()));
        }
        //1.2 ??????-filter ????????????ID
        if(param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId",param.getCatalog3Id()));
        }
        //1.2 ??????-filter ??????ID
        if(param.getBrandId()!=null&&param.getBrandId().size()>0){
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        //1.2 ??????-filter ???????????????????????????
        if (param.getAttrs()!=null&&param.getAttrs().size()>0){
            for (String attr : param.getAttrs()) {
                BoolQueryBuilder nestedBoolQueryBuilder = QueryBuilders.boolQuery();
                String[] split = attr.split("_");//??????id????????????
                String s = split[0];
                String[] attrValues = split[1].split(":");//???????????????
                nestedBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId",s));
                nestedBoolQueryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));
                //???????????????????????????nestedQuery
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQueryBuilder, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }

        }
        //1.2 ??????-filter ??????
        if(param.getHasStock()!=null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }
        //1.2 ??????-filter ????????????
        if(StringUtils.isNotBlank(param.getSkuPrice())){
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] split = param.getSkuPrice().split("_");
            if(split.length == 2){
                rangeQuery.gte(split[0]).lte(split[1]);
            }else if(split.length == 1){
                if(param.getSkuPrice().startsWith("_")){
                    rangeQuery.lte(split[0]);
                }
                if(param.getSkuPrice().endsWith("_")){
                    rangeQuery.gte(split[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }
        //???????????????????????????
        builder.query(boolQuery);

        /**
         * ????????????????????????
         */
        if(StringUtils.isNotBlank(param.getSort())){
            String[] s = param.getSort().split("_");
            SortOrder sortOrder = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            builder.sort(s[0], sortOrder);
        }
        //from=(pageNum-1)*pageSize
        builder.from((param.getPageNum()-1)*EsConstant.PRODUCT_PAGESIZE);
        builder.size(EsConstant.PRODUCT_PAGESIZE);

        //??????
        if(StringUtils.isNotBlank(param.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style=color:red>");
            highlightBuilder.postTags("</b>");
            builder.highlighter(highlightBuilder);
        }

        /**
         * ????????????
         */
        //2.1????????????
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        //?????????,?????????????????????
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        builder.aggregation(brand_agg);
        //2.2 ????????????
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg");
        catalog_agg.field("catalogId").size(20);
        //?????????
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catelogName").size(1));
        builder.aggregation(catalog_agg);

        //2.3???????????????????????????
        NestedAggregationBuilder nested = AggregationBuilders.nested("attr_agg", "attrs");
        //??????attrId
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        //?????????attrName
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        //?????????attrValue
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue")).size(50);
        nested.subAggregation(attr_id_agg);
        //??????????????????
        builder.aggregation(nested);
        log.info("------------------------------------------------------------------");
        log.info("????????????DSL?????????{}",builder.toString());
        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, builder);

    }

    /**
     * ??????????????????
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response,SearchParam param) {
        SearchResult searchResult = new SearchResult();
        SearchHits hits = response.getHits();
        //??????????????????????????????
        List<SkuEsModel> esModels = new ArrayList<>();
        if(hits.getHits()!=null&&hits.getHits().length>0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if(StringUtils.isNotBlank(param.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);
                }
                esModels.add(esModel);
            }
        }
        searchResult.setProducts(esModels);
        //?????????????????????????????????
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        for (Terms.Bucket bucket : catalog_agg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //????????????ID
            catalogVo.setCatalogId(Long.parseLong(bucket.getKeyAsString()));
            //??????????????????
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String name = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(name);
            catalogVos.add(catalogVo);
        }
        searchResult.setCatalogs(catalogVos);
        //????????????????????????????????????
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            //??????Id
            brandVo.setBrandId(Long.parseLong(bucket.getKeyAsString()));
            //????????????
            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            String name = brand_name_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(name);
            //????????????
            ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brand_img_agg");
            String img = brand_img_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(img);
            brandVos.add(brandVo);
        }
        searchResult.setBrands(brandVos);
        //????????????????????????????????????
        List<SearchResult.AttrVo> attrs = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attr = new SearchResult.AttrVo();
            //??????ID
            attr.setAttrId(bucket.getKeyAsNumber().longValue());
            //????????????
            ParsedStringTerms attr_name_agg = bucket.getAggregations().get("attr_name_agg");
            attr.setAttrName(attr_name_agg.getBuckets().get(0).getKeyAsString());
            //???????????????
            ParsedStringTerms attr_value_agg = bucket.getAggregations().get("attr_value_agg");
            List<String> values = attr_value_agg.getBuckets().stream().map(item -> ((Terms.Bucket) item).getKeyAsString()).collect(Collectors.toList());
            attr.setAttrValue(values);
            attrs.add(attr);
        }
        searchResult.setAttrs(attrs);
        //===========????????????????????????==============
        //????????????
        //????????????
        Long total = hits.getTotalHits().value;
        searchResult.setTotal(total);
        //??????
        searchResult.setPageNum(param.getPageNum());
        //?????????
        Long totalPages = total%EsConstant.PRODUCT_PAGESIZE==0?total/EsConstant.PRODUCT_PAGESIZE:total/EsConstant.PRODUCT_PAGESIZE+1;
        searchResult.setTotalPages(totalPages.intValue());

        //????????????
        List<Integer> pageNavs = new ArrayList<>();
        for(int i =1;i<=totalPages;i++){
            pageNavs.add(i);
        }
        searchResult.setPageNavs(pageNavs);


        //?????????????????????
        if(param.getAttrs()!=null&&param.getAttrs().size()>0) {
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                searchResult.getAttrIds().add(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo resVo = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(resVo.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }
                String replace = replaceQueryString(param,attr,"attrs");
                navVo.setLink("http://search.swallowincense.com/list.html?" + replace);
                return navVo;
            }).collect(Collectors.toList());
            searchResult.setNavs(navVos);
        }

        //???????????????
        if(param.getBrandId()!=null&&param.getBrandId().size()>0){
            List<SearchResult.NavVo> navs = searchResult.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("??????");
            R r = productFeignService.brandsInfo(param.getBrandId());
            if (r.getCode()==0) {
                List<BrandVo> brands = r.getData("brands", new TypeReference<List<BrandVo>>() {
                });
                StringBuffer buffer = new StringBuffer();
                String replace = "";
                for (BrandVo brand : brands) {
                    buffer.append(brand.getName()+";");
                    replace = replaceQueryString(param,brand.getBrandId()+"","brandId");
                }
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.swallowincense.com/list.html?" + replace);
            }

            navs.add(navVo);
        }
        return searchResult;
    }


    //????????????
    private String replaceQueryString(SearchParam param, String value,String key) {
        String encode = null;

        try {
            encode = URLEncoder.encode(value,"UTF-8");
            encode = encode.replace("%3B",";"); //???????????????
            encode = encode.replace("+","%20"); //???????????????
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return param.get_queryString().replace("&"+key+"="+encode,"");
    }
}
