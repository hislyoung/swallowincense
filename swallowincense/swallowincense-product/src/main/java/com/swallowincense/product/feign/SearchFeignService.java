package com.swallowincense.product.feign;

import com.swallowincense.common.to.es.SkuEsModel;
import com.swallowincense.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
@FeignClient("swallowincense-search")
@Component
public interface SearchFeignService {

    @RequestMapping("/search/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
