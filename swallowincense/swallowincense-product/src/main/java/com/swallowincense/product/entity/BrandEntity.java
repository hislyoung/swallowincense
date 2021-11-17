package com.swallowincense.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.swallowincense.common.valid.AddGroup;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 品牌
 * 
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 19:55:16
 */
@Data
@TableName("pms_brand")
@NotNull
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名不为空")
	private String name;
	/**
	 * 品牌logo地址
	 */
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotBlank(message = "检索首字母不为空")
	@Pattern(regexp = "^[a-zA-Z]$",message = "检索首字母为1个字母" ,groups = {AddGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(message = "排序不为空")
	@Range(min=0,max = 9999,message = "排序为数字")
	private Integer sort;

}
