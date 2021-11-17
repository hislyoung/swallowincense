package com.swallowincense.product.vo;

import com.swallowincense.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;
@Data
public class AttrGroupWithAttrsVo {
    /**
     * 分组id
     */
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;
    /**
     * 级联选择器完整路径
     */
    private Long[] catelogPath;
    /**
     * 所有属性的集合
     */
    List<AttrEntity> attrs;
}
