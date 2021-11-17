package com.swallowincense.order.web;

import com.swallowincense.order.service.OrderService;
import com.swallowincense.order.vo.OrderConfirmVo;
import com.swallowincense.order.vo.OrderSubmitVo;
import com.swallowincense.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderWebController {
    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model){
        OrderConfirmVo order = orderService.confirmOrder();
        model.addAttribute("order",order);
        return "confirm";
    }

    /**
     * 下单
     * @param vo
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo , Model model, RedirectAttributes redirectAttributes){
        //创建订单、验令牌、验价格、锁库存
        SubmitOrderResponseVo respVo= orderService.submitOrder(vo);
        Integer code = respVo.getCode();
        if(code ==0){
            model.addAttribute("SubmitOrderResponseVo",respVo);
            //成功支付页
            return "pay";
        }else {
            String msg = "下单失败";
            //失败会到订单确认页
            switch (code){
                case 1:msg+="订单信息过期，请重新提交";break;
                case 2:msg+="订单商品发生变化，请确认后再次提交";break;
                case 3:msg+="库存锁定失败，商品库存不足";break;
                default:msg+="请联系管理员";break;
            }
            redirectAttributes.addFlashAttribute("msg",msg);
            return "redirect:http://order.swallowincense.com/toTrade";
        }
    }
}
