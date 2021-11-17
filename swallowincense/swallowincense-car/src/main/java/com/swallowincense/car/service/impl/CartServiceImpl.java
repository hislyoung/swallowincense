package com.swallowincense.car.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.swallowincense.car.feign.ProductFeignService;
import com.swallowincense.car.interceptor.CartInterceptor;
import com.swallowincense.car.service.CartService;
import com.swallowincense.car.to.UserInfoTo;
import com.swallowincense.car.vo.Cart;
import com.swallowincense.car.vo.CartItem;
import com.swallowincense.car.vo.SkuInfoVo;
import com.swallowincense.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service(value = "cartService")
public class CartServiceImpl implements CartService {
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    ThreadPoolExecutor executor;
    private final String CART_PREFIX = "swallowincense:cart:";

    /**
     *
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public CartItem addToCart(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        //如果商品存在
        String res = (String) cartOps.get(skuId.toString());
        if(StringUtils.isEmpty(res)){
            //购物车无此商品
            return addToCart(skuId, num, cartOps);
        }else {
            //购物车有此商品，修改数量
            return updateToCart(skuId, num, cartOps, res);
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        return JSON.parseObject((String) cartOps.get(skuId.toString()),CartItem.class);
    }

    @Override
    public Cart getCart() {
        //1、区分登录与不登录
        Cart cart = new Cart();
        String cartKey="";
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId()!=null){
            //登录后的购物车
            cartKey = CART_PREFIX+userInfoTo.getUserId();
            //登录状态下判断是否存在临时购物车，存在则合并
            List<CartItem> tempCartItems = getCartItems(CART_PREFIX+userInfoTo.getUserKey());
            if(tempCartItems!=null){
                //需要合并
                for (CartItem tempCartItem : tempCartItems) {
                    addToCart(tempCartItem.getSkuId(),tempCartItem.getCount());
                }
                //清除临时购物车
                clearCart(CART_PREFIX+userInfoTo.getUserKey());
            }
            //获取登录并且合并之后的购物车
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }else{
            cartKey = CART_PREFIX+userInfoTo.getUserKey();
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }
        return cart;

    }

    private CartItem updateToCart(Long skuId, Integer num, BoundHashOperations<String, Object, Object> cartOps, String res) {
        CartItem cartItem = JSON.parseObject(res, CartItem.class);
        cartItem.setCount(cartItem.getCount()+num);
        cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));
        return  cartItem;
    }

    private CartItem addToCart(Long skuId, Integer num, BoundHashOperations<String, Object, Object> cartOps) {
        //添加新商品
        CartItem cartItem = new CartItem();
        //1、远程查询要添加商品的基本信息
        CompletableFuture<Void> skuInfoFuture = CompletableFuture.runAsync(() -> {
            R skuInfo = productFeignService.skuInfo(skuId);
            if (skuInfo.getCode() == 0) {
                SkuInfoVo skuInfoData = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                //2、商品添加到购物车
                cartItem.setSkuId(skuId);
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(skuInfoData.getSkuDefaultImg());
                cartItem.setTitle(skuInfoData.getSkuTitle());
                cartItem.setPrice(skuInfoData.getPrice());
            }
        }, executor);
        //3、sku的组合信息
        CompletableFuture<Void> attrValuesFuture = CompletableFuture.runAsync(() -> {
            List<String> attrValues = productFeignService.getSkuSaleAttrValues(skuId);
            cartItem.setSkuAttr(attrValues);
        }, executor);
        try {
            CompletableFuture.allOf(skuInfoFuture,attrValuesFuture).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        String jsonString = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),jsonString);
        return cartItem;
    }

    /**
     * 获取购物车redis的操作方法（Hash）
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        //通过threadLocal获得用户信息
        String cartKey="";
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId()!=null){
            //登录后的购物车
            cartKey = CART_PREFIX+userInfoTo.getUserId();
        }else{
            cartKey = CART_PREFIX+userInfoTo.getUserKey();
        }
        //根据key获得hash数据结构的操作方法
        return redisTemplate.boundHashOps(cartKey);
    }

    private List<CartItem> getCartItems(String cartKey){
        BoundHashOperations<String, Object, Object> cartOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = cartOps.values();
        if(values!=null&&values.size()>0){
            List<CartItem> collect = values.stream().map((obj) -> {
                String str = (String) obj;
                return JSON.parseObject(str, CartItem.class);
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    @Override
    public  void clearCart(String cartKey){
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check==1);
        String string = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),string);
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String string = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),string);
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId()!=null){
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            List<CartItem> cartItems = getCartItems(cartKey);
            if(cartItems!=null&&cartItems.size()>0) {
                //获取所有被选中的购物项
                return cartItems.stream()
                        .filter(CartItem::getCheck)
                        .map(item->{
                            BigDecimal price = productFeignService.getPrice(item.getSkuId());
                            //更新为最新价格
                            item.setPrice(price);
                            return item;
                        })
                        .collect(Collectors.toList());
            }else {
                return null;
            }
        }else {
            return null;
        }

    }
}
