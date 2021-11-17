package com.swallowincense.common.constant;

public class   ProductConstant {
    public enum AttrEnum {
        //属性类型[0-销售属性，1-基本属性]
        ATTR_TYPE_SALE(0,"销售属性"),
        ATTR_TYPE_BASE(1,"基本属性");
        private Integer code;
        private String value;
        AttrEnum(Integer code,String value){
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
    public enum UpStatusEnum {
        //属性类型[0-销售属性，1-基本属性]
        NEW_SPU(0,"新建"),
        UP_SPU(1,"上架"),
        DOWN_SPU(2,"下架");
        private Integer code;
        private String value;
        UpStatusEnum(Integer code,String value){
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
