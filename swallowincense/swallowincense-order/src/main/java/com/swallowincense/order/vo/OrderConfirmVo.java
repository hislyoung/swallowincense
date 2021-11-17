package com.swallowincense.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

//订单确认页
public class OrderConfirmVo {
    //收货地址
    @Setter@Getter
    private List<MemberAddressVo> address;
    //选中的购物项
    @Setter@Getter
    private List<OrderItemVo> items;

    //发票记录

    //优惠券信息
    @Setter@Getter
    private Integer integration;
    //防重令牌
    @Setter@Getter
    private String orderToken;
    @Setter@Getter//库存
    Map<Long,Boolean> stocks;
    public Integer getCount(){
        Integer count = 0;
        if(items!=null&&items.size()>0){
            for (OrderItemVo item : items) {
                count+=item.getCount();
            }
        }
        return count;
    }

    public BigDecimal getTotalPrice() {
        BigDecimal sum = new BigDecimal(BigInteger.ZERO);
        if(items!=null&&items.size()>0){
            for (OrderItemVo item : items) {
                BigDecimal price = item.getPrice().multiply(new BigDecimal(item.getCount()));
                sum = sum.add(price);
            }
        }
        return sum;
    }

    public BigDecimal getPayPrice() {
        return getTotalPrice().subtract(BigDecimal.ZERO);
    }
}
