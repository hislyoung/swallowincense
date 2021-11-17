package com.swallowincense.ware.dao;

import com.swallowincense.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 23:09:29
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Long getSkuStock(Long skuId);

    List<Long> listWareIdHasStock(@Param("skuId") Long skuId);

    Long lockSkuStock(@Param("skuId") Long skuId, @Param("id") Long id, @Param("num") Integer num);

    void unLockStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num, @Param("detailId") Long detailId);
}
