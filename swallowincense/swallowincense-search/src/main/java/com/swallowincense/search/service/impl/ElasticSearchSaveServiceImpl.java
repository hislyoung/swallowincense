package com.swallowincense.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.swallowincense.common.to.es.SkuEsModel;
import com.swallowincense.search.config.ElasticSearchConfig;
import com.swallowincense.search.constant.EsConstant;
import com.swallowincense.search.service.ElasticSearchSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service("elasticSearchSaveService")
@Slf4j
public class ElasticSearchSaveServiceImpl implements ElasticSearchSaveService {
    @Autowired
    @Qualifier(value = "elasticSearchClient")
    RestHighLevelClient restHighLevelClient;

    @Override
    public Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws Exception{
        //保存到ES
        //给ES中建立索引product、建立好映射关系
        //给Es中保存这些数据
        //BulkRequest bulkRequest, RequestOptions options
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModels) {
            //构造批量保存请求
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            String jsonString = JSON.toJSONString(skuEsModel);
            indexRequest.source(jsonString, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, ElasticSearchConfig.COMMON_OPTIONS);
        //如果批量错误
        boolean b = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems()).map(BulkItemResponse::getId).collect(Collectors.toList());

        log.info("商品上架完成{}",collect);
        return b;

    }
}
