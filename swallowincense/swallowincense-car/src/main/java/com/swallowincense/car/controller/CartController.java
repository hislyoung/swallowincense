package com.swallowincense.car.controller;

import com.swallowincense.car.service.CartService;
import com.swallowincense.car.vo.Cart;
import com.swallowincense.car.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CartController {

    @Autowired
    CartService cartService;

    /**
     * 获取被选中的购物车用于结算
     * @param
     * @return
     */
    @GetMapping("/currentUserItems")
    @ResponseBody
    public List<CartItem>getCurrentUserItems(){
        return cartService.getUserCartItems();
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://car.swallowincense.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,@RequestParam("count")Integer count){
        cartService.changeItemCount(skuId,count);
        return "redirect:http://car.swallowincense.com/cart.html";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,@RequestParam("check")Integer check){
        cartService.checkItem(skuId,check);
        return "redirect:http://car.swallowincense.com/cart.html";
    }
    /***
     * 京东会自动生成一个为期一个月的临时身份，每次访问都会带，而且不会改变
     *
     * 登录session有
     * 没登陆按照cookie里面带来的user-key做
     *
     * 全程一个用户，没有创建一个
     * @param model
     * @return
     */
    @GetMapping(value = "/cart.html")
    public String cartListPage(Model model) {
        //快速得到用户信息：id,user-key
        Cart cart = cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    /***
     * 添加到购物车
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes model){
        cartService.addToCart(skuId,num);
        model.addAttribute("skuId",skuId);
        return "redirect:http://car.swallowincense.com/addToCartSuccess.html";
    }
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId,Model model){
        //重定向到成功页面，再次查询购物车即可
        CartItem cartItem= cartService.getCartItem(skuId);
        model.addAttribute("item",cartItem);
        return "success";
    }

}
