package com.swallowincense.search.controller;

import com.swallowincense.search.service.MallSearchService;
import com.swallowincense.search.vo.SearchParam;
import com.swallowincense.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {
    @Autowired
    MallSearchService mallSearchService;

    /**
     * 将前端传来的值自动封装为对象
     * @param param
     * @return
     */
    @GetMapping("/list.html")
    public String listPage(SearchParam param , Model model, HttpServletRequest request){
        param.set_queryString(request.getQueryString());
        //根据参数去ES中检索
        SearchResult result= mallSearchService.search(param);
        model.addAttribute("result",result);
        return "list";
    }


}
