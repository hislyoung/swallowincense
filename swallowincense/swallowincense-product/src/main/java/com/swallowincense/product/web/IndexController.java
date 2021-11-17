package com.swallowincense.product.web;

import com.swallowincense.product.entity.CategoryEntity;
import com.swallowincense.product.service.CategoryService;
import com.swallowincense.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;
    @Autowired
    RedissonClient redissonClient;
    @GetMapping({"/","index.html"})
    public String indexPage(Model model){
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();
        model.addAttribute("categorys",categoryEntities);
        //查出一级分类
        //DEFAULT_PREFIX = "classpath:/templates/"
        //DEFAULT_SUFFIX = ".html"
        return "index";
    }

    @GetMapping("/index/catalog.json")
    @ResponseBody
    public Map<String, List<Catelog2Vo>> getCatalogJson(){
        Map<String, List<Catelog2Vo>> map = categoryService.getCatalogJson();
        return map;
    }

    @GetMapping("/hello")
    public String hello(){
        RLock lock = redissonClient.getLock("my-lock");
        lock.lock(10, TimeUnit.SECONDS);
        try{
            Thread.sleep(30000);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
        return null;
    }
}
