package com.swallowincense.product.vo;

import lombok.Data;

@Data
public class AttrResponseVo extends AttrVo{
    /**
     * catelogName:所属分类名称
     * groupName:所属分组名称
     */
    private String catelogName;

    private String groupName;

    private Long[] catelogPath;
}
