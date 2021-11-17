package com.swallowincense.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.member.entity.MemberCollectSpuEntity;

import java.util.Map;

/**
 * 会员收藏的商品
 *
 * @author LiuYang
 * @email 1093048680@qq.com
 * @date 2021-07-23 22:54:02
 */
public interface MemberCollectSpuService extends IService<MemberCollectSpuEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

