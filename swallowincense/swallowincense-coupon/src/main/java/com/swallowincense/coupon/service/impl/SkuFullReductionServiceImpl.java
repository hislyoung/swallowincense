package com.swallowincense.coupon.service.impl;

import com.swallowincense.common.to.MemberPrice;
import com.swallowincense.common.to.SkuReductionTo;
import com.swallowincense.coupon.entity.MemberPriceEntity;
import com.swallowincense.coupon.entity.SkuLadderEntity;
import com.swallowincense.coupon.service.MemberPriceService;
import com.swallowincense.coupon.service.SkuLadderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.common.utils.Query;

import com.swallowincense.coupon.dao.SkuFullReductionDao;
import com.swallowincense.coupon.entity.SkuFullReductionEntity;
import com.swallowincense.coupon.service.SkuFullReductionService;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {
    @Autowired
    SkuLadderService ladderService;
    @Autowired
    SkuFullReductionService fullReductionService;
    @Autowired
    MemberPriceService memberPriceService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        //1、保存满减打折和会员价 阶梯价格
        try {
            SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
            skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
            skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
            skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
            skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
            //TODO 折后价，需要算，在下订单的时候算也行
            /*skuLadderEntity.setPrice();*/
            if(skuReductionTo.getFullCount()>0){
                ladderService.save(skuLadderEntity);
            }

            //满减
            SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
            BeanUtils.copyProperties(skuReductionTo,reductionEntity);
            if(reductionEntity.getFullPrice().compareTo(BigDecimal.ZERO)>0){
                this.save(reductionEntity);
            }

            //会员价格
            List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
            if(!CollectionUtils.isEmpty(memberPrice)){
                List<MemberPriceEntity> collect = memberPrice.stream().map(item -> {
                    MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                    memberPriceEntity.setSkuId(reductionEntity.getSkuId());
                    memberPriceEntity.setMemberLevelId(item.getId());
                    memberPriceEntity.setMemberLevelName(item.getName());
                    memberPriceEntity.setMemberPrice(item.getPrice());
                    memberPriceEntity.setAddOther(1);
                    return memberPriceEntity;
                }).filter(item->{
                    return item.getMemberPrice().compareTo(BigDecimal.ZERO)>0;
                }).collect(Collectors.toList());
                memberPriceService.saveBatch(collect);
            }
        }catch (Exception e){
            log.error("报错信息{}",e.getMessage(),e);
        }

    }

}