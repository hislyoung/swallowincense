package com.swallowincense.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.swallowincense.product.service.CategoryBrandRelationService;
import com.swallowincense.product.vo.Catelog2Vo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.common.utils.Query;

import com.swallowincense.product.dao.CategoryDao;
import com.swallowincense.product.entity.CategoryEntity;
import com.swallowincense.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate redisTemplate;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        return categoryEntities.stream()
                .filter(item -> item.getParentCid() == 0)
                .peek(menu-> menu.setChild(getChildren(menu,categoryEntities)))
                .sorted(Comparator.comparingInt(CategoryEntity::getSort))
                .collect(Collectors.toList());
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1、检查删除的对象是否被引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        CategoryEntity entity = baseMapper.selectById(catelogId);
        List<Long> path = new ArrayList<>(3);
        path.add(entity.getCatId());
        while (entity.getParentCid()!=0){
            path.add(entity.getParentCid());
            entity = baseMapper.selectById(entity.getParentCid());
        }
        Collections.reverse(path);
        return path.toArray(new Long[path.size()]);
    }

    //@CacheEvict(value = {"category"},allEntries = true),当前分区下的数据全部删除
    @Caching(evict = {
            @CacheEvict(value = {"category"},key = "'getLevel1Categorys'"),
            @CacheEvict(value = {"category"},key = "'getCatalogJson'")
    })
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if(StringUtils.isNotBlank(category.getName())){
            categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
            //TODO 更新其他相关冗余字段
        }
    }

    @Cacheable(value = {"category"},key = "#root.methodName" ,sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid",0));
    }
    @Cacheable(value = {"category"},key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        /*
         * 1、空结果缓存，解决缓存穿透问题
         * 2、设置不一样的过期时间，解决缓存雪崩问题
         * 3、加锁，解决缓存击穿问题（注意核心是任何操作都要保持原子性）
         */
        //加入缓存逻辑,注意序列化与反序列化
//        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
//        if(StringUtils.isBlank(catalogJson)){
            //缓存中没有
        System.out.println("重复执行");
        return getCatalogJsonFromDbWithRedisLock();
//        }
//        return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
//        });
    }

    private Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {
        String token = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock",token,30,TimeUnit.SECONDS);
        if(lock){
            //加锁成功，避免死锁，设置过期时间
            Map<String, List<Catelog2Vo>> catalogJsonFromDb;
            try{
                catalogJsonFromDb = getCatalogJsonFromDb();
            }finally {
                /*String lock1 = redisTemplate.opsForValue().get("lock");
            if(token.equals(lock1)){
                //删除自己的锁，会有原子性操作，使用lua脚本保证原子操作
                redisTemplate.delete("lock");
            }*/
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                        "then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                //原子删锁
                redisTemplate.execute(new DefaultRedisScript<Long>(script,Long.class)
                        , Collections.singletonList("lock"),token);
            }
            return catalogJsonFromDb;
        }else{
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //不成功，自旋的方式加锁
            return getCatalogJsonFromDbWithRedisLock();
        }
    }

    //在数据库查询并封装分类数据
    private Map<String, List<Catelog2Vo>> getCatalogJsonFromDb() {
        /*String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if(StringUtils.isNotBlank(catalogJson)){
            //缓存不为空，直接返回
            return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
        }*/
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        //1、查出所有一级分类
        List<CategoryEntity> level1Categorys = getListByParentCid(selectList,0L);
        //封装数据
        Map<String, List<Catelog2Vo>> collect = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //每一个一级分类，查到对应的二级分类
            List<CategoryEntity> categoryEntities = getListByParentCid(selectList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (!CollectionUtils.isEmpty(categoryEntities)) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //找到当前二级分类下的三级分类
                    List<CategoryEntity> leve31Categorys = getListByParentCid(selectList, l2.getCatId());
                    if (!CollectionUtils.isEmpty(leve31Categorys)) {
                        List<Catelog2Vo.Catelog3Vo> catelog3Vos = leve31Categorys.stream().map(l3 -> new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName())).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catelog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        //结果放入缓存,将对象转为JSON
        /*String jsonString = JSON.toJSONString(collect);
        redisTemplate.opsForValue().set("catalogJson",jsonString,1, TimeUnit.DAYS);*/
        return collect;
    }


    private List<CategoryEntity> getListByParentCid(List<CategoryEntity> selectList,Long parentCid) {
        //return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        return selectList.stream().filter(item -> item.getParentCid().equals(parentCid)
        ).collect(Collectors.toList());
    }

    private List<CategoryEntity> getChildren(CategoryEntity entities,List<CategoryEntity> categoryEntities) {
        return categoryEntities.stream().
                filter((categoryEntity -> categoryEntity.getParentCid().equals(entities.getCatId())))
                //找到子菜单
                .peek(menu -> menu.setChild(getChildren(menu, categoryEntities)))
                //菜单的排序
                .sorted(Comparator.comparingInt(menu2 -> (menu2.getSort() == null ? 0 : menu2.getSort())))
                .collect(Collectors.toList());
    }

}