package com.swallowincense.ware.service.impl;

import com.swallowincense.common.constant.WareConstant;
import com.swallowincense.ware.entity.PurchaseDetailEntity;
import com.swallowincense.ware.service.PurchaseDetailService;
import com.swallowincense.ware.service.WareSkuService;
import com.swallowincense.ware.vo.MergeVo;
import com.swallowincense.ware.vo.PurchaseDoneVo;
import com.swallowincense.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.common.utils.Query;

import com.swallowincense.ware.dao.PurchaseDao;
import com.swallowincense.ware.entity.PurchaseEntity;
import com.swallowincense.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {
    @Autowired
    PurchaseDetailService detailService;
    @Autowired
    WareSkuService wareSkuService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status",0).or().eq("status",1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo vo) {
        Long purchaseId = vo.getPurchaseId();
        if(purchaseId==null){
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseEnum.PURCHASE_CREATED.getCode());
            this.save(purchaseEntity);
        }
        //TODO 确认采购状态是0，1才可以合并
        List<Long> items = vo.getItems();
        List<PurchaseDetailEntity> collect = items.stream().map(item -> {
            PurchaseDetailEntity entity = new PurchaseDetailEntity();
            entity.setId(item);
            entity.setPurchaseId(purchaseId);
            entity.setStatus(WareConstant.PurchaseDetailsEnum.PURCHASE_ASSIGNED.getCode());
            return entity;
        }).collect(Collectors.toList());
        detailService.updateBatchById(collect);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    /**
     *
     * @param ids 采购单ID
     */
    @Override
    public void received(List<Long> ids) {
        //1、确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> collect = ids.stream().map(this::getById)
                .filter(item-> item.getStatus().equals(WareConstant.PurchaseEnum.PURCHASE_CREATED.getCode())
                        || item.getStatus().equals(WareConstant.PurchaseEnum.PURCHASE_ASSIGNED.getCode()))
                .peek(item->{
                    item.setStatus(WareConstant.PurchaseEnum.PURCHASE_RECEIVED.getCode());
                    item.setUpdateTime(new Date());
                })
                .collect(Collectors.toList());
        //2、改变采购单状态
        this.updateBatchById(collect);
        //3、改变采购项状态
        collect.forEach((item)->{
            List<PurchaseDetailEntity> detailEntities = detailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> entities = detailEntities.stream().map(entity -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(entity.getId());
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailsEnum.PURCHASE_BYING.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            detailService.updateBatchById(entities);
        });
    }

    @Override
    public void done(PurchaseDoneVo vo) {
        //1、改变采购项状态
        boolean flag = true;
        List<PurchaseItemDoneVo> items = vo.getItems();
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if(item.getStatus().equals(WareConstant.PurchaseDetailsEnum.PURCHASE_HASHERROR.getCode())){
                flag = false;
                detailEntity.setStatus(item.getStatus());
            }else {
                detailEntity.setStatus(WareConstant.PurchaseDetailsEnum.PURCHASE_FINISHED.getCode());
                //3、将采购成功的入库
                PurchaseDetailEntity entity = detailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum());
            }
            //
            detailEntity.setId(item.getItemId());
            updates.add(detailEntity);
        }
        if(!CollectionUtils.isEmpty(updates)){
            detailService.updateBatchById(updates);
        }
        //2、改变采购单状态
        Long id = vo.getId();
        PurchaseEntity entity = new PurchaseEntity();
        entity.setId(id);
        entity.setStatus(flag?WareConstant.PurchaseEnum.PURCHASE_FINISHED.getCode()
                :WareConstant.PurchaseEnum.PURCHASE_HASHERROR.getCode());
        entity.setUpdateTime(new Date());
        this.updateById(entity);
    }

}