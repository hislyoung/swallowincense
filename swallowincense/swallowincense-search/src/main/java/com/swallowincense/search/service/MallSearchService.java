package com.swallowincense.search.service;

import com.swallowincense.search.vo.SearchParam;
import com.swallowincense.search.vo.SearchResult;

public interface MallSearchService {
    SearchResult search(SearchParam param);
}
