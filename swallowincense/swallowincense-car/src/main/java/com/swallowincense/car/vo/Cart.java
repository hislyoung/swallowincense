package com.swallowincense.car.vo;

import java.math.BigDecimal;
import java.util.List;

public class Cart {
    List<CartItem> items;
    private Integer countNum;//所有商品数量
    private Integer countType;//分类数量
    private BigDecimal totalAmount;//总价
    private BigDecimal reduce = BigDecimal.ZERO;//减免

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if(items!=null && items.size()>0){
            for (CartItem item : items) {
                count+=item.getCount();
            }
        }

        return count;
    }

    public Integer getCountType() {
        int count = 0;
        if(items!=null && items.size()>0) {
            for (CartItem item : items) {
                count += 1;
            }
        }
        return count;
    }


    public BigDecimal getTotalAmount() {
        BigDecimal amount = BigDecimal.ZERO;
        if(items!=null && items.size()>0) {
            for (CartItem item : items) {
                if(item.getCheck()) {
                    BigDecimal totalPrice = item.getTotalPrice();
                    amount = amount.add(totalPrice);
                }
            }
        }
        amount = amount.subtract(getReduce());
        return amount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
