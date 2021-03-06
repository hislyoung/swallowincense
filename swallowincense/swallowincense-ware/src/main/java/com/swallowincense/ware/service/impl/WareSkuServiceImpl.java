package com.swallowincense.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.swallowincense.common.to.mq.StockDetailTo;
import com.swallowincense.common.to.mq.StockLockedTo;
import com.swallowincense.common.utils.R;
import com.swallowincense.ware.entity.WareOrderTaskDetailEntity;
import com.swallowincense.ware.entity.WareOrderTaskEntity;
import com.swallowincense.ware.exception.NoStockException;
import com.swallowincense.ware.feign.OrderFeignService;
import com.swallowincense.ware.feign.ProductFeignService;
import com.swallowincense.ware.service.WareOrderTaskDetailService;
import com.swallowincense.ware.service.WareOrderTaskService;
import com.swallowincense.ware.vo.*;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.common.utils.Query;

import com.swallowincense.ware.dao.WareSkuDao;
import com.swallowincense.ware.entity.WareSkuEntity;
import com.swallowincense.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    WareOrderTaskDetailService orderTaskDetailService;
    @Autowired
    WareOrderTaskService orderTaskService;
    @Autowired
    OrderFeignService orderFeignService;

    private void unLockStock(Long skuId,Long wareId,Integer num,Long detailId){
        //????????????
        baseMapper.unLockStock(skuId,wareId,num,detailId);
        //?????????????????????
        WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity();
        taskDetailEntity.setId(detailId);
        taskDetailEntity.setLockStatus(2);
        orderTaskDetailService.updateById(taskDetailEntity);

    }
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        String wareId = (String) params.get("wareId");
        if(StringUtils.isNotBlank(wareId)){
            wrapper.eq("ware_id",wareId);
        }
        if(StringUtils.isNotBlank(skuId)){
            wrapper.and(item->{
               item.eq("sku_id",skuId).or().like("sku_name",skuId);
            });
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //??????????????????????????????
        List<WareSkuEntity> wareSkuEntities = baseMapper.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if(CollectionUtils.isEmpty(wareSkuEntities)){
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(0);
            //TODO ????????????Sku?????????,????????????????????????????????????
            //1?????????Catch??????
            try {
                R info = productFeignService.info(skuId);
                if(info.getCode() == 0){
                    Map<String,Object> data = (Map<String, Object>) info.get("skuInfo");
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            baseMapper.insert(wareSkuEntity);
        }else {
            baseMapper.addStock(skuId, wareId, skuNum);
        }

    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        return skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            Long count = baseMapper.getSkuStock(skuId);
            vo.setSkuId(skuId);
            vo.setHasStock(count != null && count > 0);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * ??????????????????
     * @Transactional(rollbackFor = NoStockException.class)
     * ???????????????
     *
     * ???????????????
     *  ?????????????????????????????????30??????????????????????????????
     *  ????????????????????????????????????????????????seata????????????????????????
     *
     * @param vo
     * @return
     */
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        //????????????????????????????????????????????????
        WareOrderTaskEntity orderTaskEntity = new WareOrderTaskEntity();
        orderTaskEntity.setOrderSn(vo.getOrderSn());
        orderTaskService.save(orderTaskEntity);

        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            List<Long> wareIds = baseMapper.listWareIdHasStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());
        for (SkuWareHasStock hasStock : collect) {
            Boolean skuStock = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareId = hasStock.getWareId();
            if(wareId==null||wareId.size()==0){
                //??????????????????
                throw new NoStockException(skuId);
            }
            for (Long id : wareId) {
                Long line = baseMapper.lockSkuStock(skuId,id,hasStock.getNum());
                if(line==1){
                    skuStock = true;
                    WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity(
                            null,skuId,"",hasStock.getNum(),orderTaskEntity.getId(),id,1
                    );
                    orderTaskDetailService.save(taskDetailEntity);
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    stockLockedTo.setId(orderTaskEntity.getId());
                    BeanUtils.copyProperties(taskDetailEntity,stockDetailTo);
                    stockLockedTo.setDetail(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-change","stock.locked",stockLockedTo);
                    break;
                }else {
                    //???????????????
                }
            }
            if(!skuStock){
                //???????????????????????????
                throw new NoStockException(skuId);
            }
        }
        //????????????????????????

        return true;
    }

    @Override
    public void unlockStock(StockLockedTo to) {
        System.out.println("??????????????????");

        StockDetailTo detail = to.getDetail();
        Long skuId = detail.getSkuId();
        Long detailId = detail.getId();
        //??????????????????????????????
        //??????
        //????????????
        //??????????????????????????????
        //?????????????????????????????????
        //?????????
        //?????????????????????????????????????????????????????????????????????
        WareOrderTaskDetailEntity serviceById = orderTaskDetailService.getById(detailId);
        System.out.println(serviceById);
        if (serviceById != null) {
            //??????
            Long id = to.getId();
            WareOrderTaskEntity orderTaskServiceById = orderTaskService.getById(id);
            System.out.println(orderTaskServiceById);
            String orderSn = orderTaskServiceById.getOrderSn();//??????????????????
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode()==0){
                OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });
                //?????????????????????????????????
                if(data==null||data.getStatus() == 4){
                    if (serviceById.getLockStatus()==1) {
                        unLockStock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum(),detailId);
                    }
                }
            }else {
                //??????????????????????????????
                throw new RuntimeException("?????????????????????");
            }
        }
    }

    @Transactional
    @Override
    public void unlockStock(OrderVo to) {
        String orderSn = to.getOrderSn();
        //?????????????????????????????????
        WareOrderTaskEntity orderTaskEntity = orderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = orderTaskEntity.getId();
        //????????????????????????????????????????????????????????????
        List<WareOrderTaskDetailEntity> detailEntities = orderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", id)
                .eq("lock_status", 1));
        for (WareOrderTaskDetailEntity detailEntity : detailEntities) {
            unLockStock(detailEntity.getSkuId(),detailEntity.getWareId(),detailEntity.getSkuNum(),detailEntity.getId());
        }

    }

    @Data
    class SkuWareHasStock{
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }
}