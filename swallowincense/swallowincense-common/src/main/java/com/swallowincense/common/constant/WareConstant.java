package com.swallowincense.common.constant;

public class WareConstant {
    public enum PurchaseEnum {
        //属性类型[0-新建，1-已分配，2-已领取，3-已完成，4-有异常]
        PURCHASE_CREATED(0,"新建"),
        PURCHASE_ASSIGNED(1,"已分配"),
        PURCHASE_RECEIVED(2,"已领取"),
        PURCHASE_FINISHED(3,"已完成"),
        PURCHASE_HASHERROR(4,"有异常");

        private Integer code;
        private String value;
        PurchaseEnum(Integer code,String value){
            this.code = code;
            this.value = value;
        }

        public void setCode(Integer code){
            this.code = code;
        }

        public Integer getCode(){
            return this.code;
        }

        public void setValue(String value){
            this.value = value;
        }

        public String getValue(){
            return this.value;
        }

    }

    public enum PurchaseDetailsEnum {
        //属性类型[0-新建，1-已分配，2-已领取，3-已完成，4-有异常]
        PURCHASE_CREATED(0,"新建"),
        PURCHASE_ASSIGNED(1,"已分配"),
        PURCHASE_BYING(2,"正在采购"),
        PURCHASE_FINISHED(3,"已完成"),
        PURCHASE_HASHERROR(4,"采购失败");

        private Integer code;
        private String value;
        PurchaseDetailsEnum(Integer code,String value){
            this.code = code;
            this.value = value;
        }

        public void setCode(Integer code){
            this.code = code;
        }

        public Integer getCode(){
            return this.code;
        }

        public void setValue(String value){
            this.value = value;
        }

        public String getValue(){
            return this.value;
        }

    }
}
