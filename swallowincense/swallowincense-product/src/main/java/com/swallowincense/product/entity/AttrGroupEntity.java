package com.swallowincense.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import lombok.Data;

/**
 * 属性分组
 * 
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 19:55:16
 */
@Data
@TableName("pms_attr_group")
public class AttrGroupEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 分组id
	 */
	@TableId
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
	@TableField(exist = false)
	private Long[] catelogPath;
}
