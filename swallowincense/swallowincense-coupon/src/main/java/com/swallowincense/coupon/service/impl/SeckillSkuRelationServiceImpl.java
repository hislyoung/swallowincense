package com.swallowincense.coupon.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.common.utils.Query;

import com.swallowincense.coupon.dao.SeckillSkuRelationDao;
import com.swallowincense.coupon.entity.SeckillSkuRelationEntity;
import com.swallowincense.coupon.service.SeckillSkuRelationService;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SeckillSkuRelationEntity> relationEntityQueryWrapper = new QueryWrapper<>();
        String sessionId = (String) params.get("promotionSessionId");

        if(StringUtils.isNotBlank(sessionId)){
            relationEntityQueryWrapper.eq("promotion_session_id", sessionId);
        }
        String key = (String) params.get("key");
        if(StringUtils.isNotBlank(key)){
            relationEntityQueryWrapper.eq("id", key);
        }
        IPage<SeckillSkuRelationEntity> page = this.page(
                new Query<SeckillSkuRelationEntity>().getPage(params),
                relationEntityQueryWrapper
        );

        return new PageUtils(page);
    }

}