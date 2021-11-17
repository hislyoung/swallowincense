package com.swallowincense.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.member.entity.MemberReceiveAddressEntity;

import java.util.List;
import java.util.Map;

/**
 * 会员收货地址
 *
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 22:54:02
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获得会员的地址列表
     * @param memberId
     * @return
     */
    List<MemberReceiveAddressEntity> getAddress(Long memberId);


    /**
     * 根据用户Id修改默认地址
     */
    void updateInBatchByMemberId(Long addrId);
}

