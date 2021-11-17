package com.swallowincense.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.swallowincense.common.utils.R;
import com.swallowincense.ware.feign.MemberFeignService;
import com.swallowincense.ware.vo.FareVo;
import com.swallowincense.ware.vo.MemberAddressVo;
import com.swallowincense.ware.vo.MemberReceiveAddressVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.common.utils.Query;

import com.swallowincense.ware.dao.WareInfoDao;
import com.swallowincense.ware.entity.WareInfoEntity;
import com.swallowincense.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(StringUtils.isNotBlank(key)){
            wrapper.and(item->{
                item.eq("id",key)
                        .or().like("name",key)
                        .or().like("address",key)
                        .or().like("areacode",key);
            });
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        //远程查询会员模块
        R addrInfo = memberFeignService.addrInfo(addrId);
        if(addrInfo.getCode()==0) {
            MemberAddressVo infoData = addrInfo.getData("memberReceiveAddress",new TypeReference<MemberAddressVo>() {
            });
            String phone = infoData.getPhone();
            String fare = phone.substring(phone.length() - 2);
            fareVo.setFare(new BigDecimal(fare));
            fareVo.setAddress(infoData);
            //重置地址
            R r = memberFeignService.updateInBatchByMemberId(addrId);
            if(r.getCode()==0) {
                //更新默认地址
                MemberReceiveAddressVo addressVo = new MemberReceiveAddressVo();
                addressVo.setId(addrId);
                addressVo.setDefaultStatus(1);
                memberFeignService.updateDef(addressVo);
            }
            return fareVo;
        }
        return null;
    }

}