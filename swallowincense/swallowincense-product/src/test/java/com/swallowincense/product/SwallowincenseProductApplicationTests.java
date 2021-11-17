package com.swallowincense.product;

import com.swallowincense.product.entity.BrandEntity;
import com.swallowincense.product.service.AttrGroupService;
import com.swallowincense.product.service.BrandService;
import com.swallowincense.product.vo.SkuItemVo;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

@SpringBootTest
class SwallowincenseProductApplicationTests {
    @Autowired
    AttrGroupService attrGroupService;
    @Autowired
    BrandService brandService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;
    @Test
    void getAttr(){
        List<SkuItemVo.SpuItemGroupVo> attrGroupWithAttrsBySpuId = attrGroupService.getAttrGroupWithAttrsBySpuId(7L, 225L);
        System.out.println(attrGroupWithAttrsBySpuId);
    }
    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setDescript("");
        brandEntity.setName("测试商品");
        brandService.save(brandEntity);
        System.out.println("保存成功");
    }

    @Test
    void testRedis(){
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.set("hello","world");
        System.out.println(operations.get("hello"));
    }

    @Test
    void testRedisson(){
        System.out.println(redissonClient);
    }

}
