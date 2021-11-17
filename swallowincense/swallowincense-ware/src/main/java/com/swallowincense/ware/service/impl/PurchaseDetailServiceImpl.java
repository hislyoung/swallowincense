package com.swallowincense.ware.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.common.utils.Query;

import com.swallowincense.ware.dao.PurchaseDetailDao;
import com.swallowincense.ware.entity.PurchaseDetailEntity;
import com.swallowincense.ware.service.PurchaseDetailService;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseDetailEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(StringUtils.isNotBlank(key)){
            queryWrapper.and(item->{
               item.eq("purchase_id",key)
                       .or().eq("sku_id",key)
                       .or().eq("sku_num",key)
                       .or().eq("sku_price",key);
            });
        }
        String status = (String) params.get("status");
        if(StringUtils.isNotBlank(status)){
            queryWrapper.eq("status",status);
        }
        String wareId = (String) params.get("wareId");
        if(StringUtils.isNotBlank(wareId)){
            queryWrapper.eq("ware_id",wareId);
        }
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long id) {
        return this.list(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", id));
    }

}