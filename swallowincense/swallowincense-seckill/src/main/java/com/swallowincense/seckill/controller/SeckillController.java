package com.swallowincense.seckill.controller;

import com.swallowincense.common.utils.R;
import com.swallowincense.seckill.service.SeckillService;
import com.swallowincense.seckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SeckillController {
    /**
     * 查询当前秒杀的商品信息
     * @return
     */
    @Autowired
    SeckillService seckillService;
    @GetMapping("/currentSeckillSkus")
    @ResponseBody
    public R getCurrentSeckillSkus(){
        List<SeckillSkuRedisTo> redisTos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(redisTos);
    }
    @GetMapping("/sku/seckill/{skuId}")
    @ResponseBody
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId){
        SeckillSkuRedisTo  seckillInfo =seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(seckillInfo);
    }
    @GetMapping("/kill")
    public String sedKill(@RequestParam("killId") String killId,
                          @RequestParam("key") String key,
                          @RequestParam("num") Integer num,
                          Model model){
        //判断是否登录interceptor
        String orderSn = seckillService.kill(killId,key,num);
        model.addAttribute("orderSn",orderSn);
        return "success";
    }

}
