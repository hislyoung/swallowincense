package com.swallowincense.car.service;

import com.swallowincense.car.vo.Cart;
import com.swallowincense.car.vo.CartItem;

import java.util.List;

public interface CartService {
    /***
     * 添加购物车
     * @param skuId
     * @param num
     * @return
     */
    CartItem addToCart(Long skuId, Integer num);

    /**
     * 避免刷新连接，商品无限增加，重定向后查询方法
     *
     * @param skuId
     * @return
     */
    CartItem getCartItem(Long skuId);

    Cart getCart();

    /**
     * 清空购物车
     * @param cartKey
     */
    void clearCart(String cartKey);

    /**
     * 修改购物车选中
     * @param skuId
     * @param check
     */
    void checkItem(Long skuId, Integer check);

    /**
     * 修改购物项数量
     * @param skuId
     * @param num
     */
    void changeItemCount(Long skuId, Integer num);

    /**
     * 删除购物项
     * @param skuId
     */
    void deleteItem(Long skuId);

    /**
     * 获得当前登录的用户的购物车
     * @return
     */
    List<CartItem> getUserCartItems();
}
