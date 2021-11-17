package com.swallowincense.search.service;

import com.swallowincense.common.to.es.SkuEsModel;

import java.util.List;

public interface ElasticSearchSaveService {
    Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws Exception;
}
