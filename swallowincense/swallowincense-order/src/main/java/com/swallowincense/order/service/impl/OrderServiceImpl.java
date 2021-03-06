package com.swallowincense.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.swallowincense.common.constant.OrderConstant;
import com.swallowincense.common.to.mq.SeckillOrderTo;
import com.swallowincense.common.utils.R;
import com.swallowincense.common.vo.MemberVo;
import com.swallowincense.order.entity.OrderItemEntity;
import com.swallowincense.order.enume.OrderStatusEnum;
import com.swallowincense.order.feign.CartFeignService;
import com.swallowincense.order.feign.MemberFeignService;
import com.swallowincense.order.feign.ProductFeignService;
import com.swallowincense.order.feign.WmsFeignService;
import com.swallowincense.order.interceptor.LoginUserInterceptor;
import com.swallowincense.order.service.OrderItemService;
import com.swallowincense.order.to.OrderCreateTo;
import com.swallowincense.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swallowincense.common.utils.PageUtils;
import com.swallowincense.common.utils.Query;

import com.swallowincense.order.dao.OrderDao;
import com.swallowincense.order.entity.OrderEntity;
import com.swallowincense.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    OrderService orderService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    CartFeignService cartFeignService;
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    StringRedisTemplate redisTemplate;

    private ThreadLocal<OrderSubmitVo> submitVoThreadLocal = new ThreadLocal<>();
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberVo memberVo = LoginUserInterceptor.loginUser.get();
        //RequestContextHolder?????????????????????ThreadLocal?????????????????????????????????????????????????????????
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> memberFeignFuture = CompletableFuture.runAsync(() -> {
            //??????????????????
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberVo.getId());
            orderConfirmVo.setAddress(address);
        }, executor);
        CompletableFuture<Void> orderFeignFuture = CompletableFuture.runAsync(() -> {
            //?????????
            //feign?????????????????????????????????????????????????????????????????????????????????????????????????????????
            //??????????????????
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> orderItems = cartFeignService.getCurrentUserItems();
            orderConfirmVo.setItems(orderItems);
        }, executor).thenRunAsync(()->{
            List<Long> skuIds = orderConfirmVo.getItems().stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R hasStock = wmsFeignService.getSkusHasStock(skuIds);
            if(hasStock.getCode()==0){
                List<HasStockVo> hasStockVos = hasStock.getData(new TypeReference<List<HasStockVo>>() {
                });
                if(hasStockVos!=null && hasStockVos.size()>0){
                    Map<Long, Boolean> collect = hasStockVos.stream().collect(Collectors.toMap(HasStockVo::getSkuId, HasStockVo::getHasStock));
                    orderConfirmVo.setStocks(collect);
                }
            }
        },executor);

        //????????????
        Integer integration = memberVo.getIntegration();
        orderConfirmVo.setIntegration(integration);
        //????????????????????????
        // TODO ?????????????????????
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDR_TOKEN_PREFIX+memberVo.getId(),token,30, TimeUnit.MINUTES);

        orderConfirmVo.setOrderToken(token);
        try {
            CompletableFuture.allOf(memberFeignFuture,orderFeignFuture).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return orderConfirmVo;
    }
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        submitVoThreadLocal.set(vo);
        MemberVo memberVo = LoginUserInterceptor.loginUser.get();
        SubmitOrderResponseVo submitOrderResponseVo = new SubmitOrderResponseVo();
        submitOrderResponseVo.setCode(0);
        //1???????????????[?????????????????????????????????]???lua?????? ????????????????????????
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        //??????token
        String orderToken = vo.getOrderToken();
        //?????????token key
        String key = OrderConstant.USER_ORDR_TOKEN_PREFIX + memberVo.getId();
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Collections.singletonList(key), orderToken);

        if(result!=null && result==1L){
            //????????????
            redisTemplate.delete(OrderConstant.USER_ORDR_TOKEN_PREFIX + memberVo.getId());
            //????????????????????????????????????????????????
            OrderCreateTo order = createOrder();

            BigDecimal payPrice = order.getOrder().getPayAmount();
            BigDecimal voPayPrice = vo.getPayPrice();
            if (Math.abs(payPrice.subtract(voPayPrice).doubleValue())<0.01){
                //????????????
                //????????????
                saveOrder(order);
                //????????????
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> collect = order.getOrderItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setSkuId(item.getSkuId());
                    return orderItemVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setLocks(collect);
                //????????????????????????
                R r = wmsFeignService.orderLockStock(wareSkuLockVo);
                if(r.getCode()==0){
                    //?????????
                    submitOrderResponseVo.setOrder(order.getOrder());
                    //?????????????????????????????????
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrder());
                    return submitOrderResponseVo;
                }else {
                    //????????????
                    submitOrderResponseVo.setCode(3);
                    return submitOrderResponseVo;
                }
            }else {
                submitOrderResponseVo.setCode(2);
                return submitOrderResponseVo;
            }
        }else {
            //?????????
            submitOrderResponseVo.setCode(1);
            return submitOrderResponseVo;
        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    @Override
    public void closeOrder(OrderEntity orderEntity) {
        //???????????????????????????
        OrderEntity byId = this.getById(orderEntity.getId());
        if (byId.getStatus()== OrderStatusEnum.CREATE_NEW.getCode()) {
            //??????
            OrderEntity closeEntity = new OrderEntity();
            closeEntity.setId(orderEntity.getId());
            closeEntity.setStatus(OrderStatusEnum.CANCLED.getCode());

            this.updateById(closeEntity);
            rabbitTemplate.convertAndSend("order-event-exchange","order.release.other.#",byId);
        }
    }

    @Override
    public void createSeckillOrder(SeckillOrderTo seckillOrder) {
        //TODO
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(seckillOrder.getOrderSn());
        orderEntity.setMemberId(seckillOrder.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());

        BigDecimal multiply = seckillOrder.getSeckillPrice().multiply(new BigDecimal("" + seckillOrder.getNum()));
        orderEntity.setPayAmount(multiply);
        this.save(orderEntity);

        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(seckillOrder.getOrderSn());
        orderItemEntity.setRealAmount(multiply);

        orderItemEntity.setSkuQuantity(seckillOrder.getNum());

        orderItemService.save(orderItemEntity);
    }

    /**
     * ????????????????????????
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity entity = order.getOrder();
        entity.setModifyTime(new Date());
        orderService.saveOrUpdate(entity);
        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveOrUpdateBatch(orderItems);
    }

    /**
     * ????????????
     * @return
     */
    private OrderCreateTo createOrder(){

        OrderSubmitVo orderSubmitVo = submitVoThreadLocal.get();
        OrderCreateTo createTo = new OrderCreateTo();

        //??????????????????????????????
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(orderSubmitVo, orderSn);
        createTo.setOrder(orderEntity);

        //?????????????????????
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);
        createTo.setOrderItems(orderItemEntities);

        //??????.
        computePrice(orderEntity,orderItemEntities);

        return createTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        //1?????????????????????
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalCoupon = BigDecimal.ZERO;
        BigDecimal totalIntegration = BigDecimal.ZERO;
        BigDecimal totalPromotion = BigDecimal.ZERO;
        //2?????????
        BigDecimal totalGrowth = BigDecimal.ZERO;
        BigDecimal totalGiftIntegration = BigDecimal.ZERO;
        for (OrderItemEntity itemEntity : orderItemEntities) {
            totalPrice = totalPrice.add(itemEntity.getRealAmount());
            totalCoupon = totalCoupon.add(itemEntity.getCouponAmount());
            totalIntegration = totalIntegration.add(itemEntity.getIntegrationAmount());
            totalPromotion = totalPromotion.add(itemEntity.getPromotionAmount());
            totalGrowth = totalGrowth.add(new BigDecimal(itemEntity.getGiftGrowth().toString()));
            totalGiftIntegration = totalGiftIntegration.add(new BigDecimal(itemEntity.getGiftIntegration().toString()));
        }
        //????????????
        orderEntity.setTotalAmount(totalPrice);
        //????????????
        orderEntity.setPayAmount(totalPrice.add(orderEntity.getFreightAmount()));
        orderEntity.setCouponAmount(totalCoupon);
        orderEntity.setIntegrationAmount(totalIntegration);
        orderEntity.setPromotionAmount(totalPromotion);
        //????????????
        orderEntity.setGrowth(totalGrowth.intValue());
        orderEntity.setIntegration(totalGiftIntegration.intValue());
        //?????????
        orderEntity.setDeleteStatus(0);
    }

    /**
     * ????????????
     * @param orderSubmitVo orderSn
     * @param orderSn
     * @return
     */
    private OrderEntity buildOrder(OrderSubmitVo orderSubmitVo, String orderSn) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        //??????????????????????????????????????????
        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        if(fare.getCode()==0){
            FareVo fareVo = fare.getData(new TypeReference<FareVo>() {
            });
            //??????
            orderEntity.setFreightAmount(fareVo.getFare());
            //??????
            orderEntity.setReceiverProvince(fareVo.getAddress().getProvince());
            orderEntity.setReceiverCity(fareVo.getAddress().getCity());
            orderEntity.setReceiverRegion(fareVo.getAddress().getRegion());
            orderEntity.setReceiverDetailAddress(fareVo.getAddress().getDetailAddress());
            orderEntity.setReceiverPostCode(fareVo.getAddress().getPostCode());
            //???????????????
            orderEntity.setMemberId(fareVo.getAddress().getMemberId());
            orderEntity.setReceiverName(fareVo.getAddress().getName());
            orderEntity.setReceiverPhone(fareVo.getAddress().getPhone());

            //??????????????????
            orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
            orderEntity.setAutoConfirmDay(7);
        }
        return orderEntity;
    }

    /**
     * ?????????????????????
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        //????????????????????????????????????
        List<OrderItemVo> currentUserItems = cartFeignService.getCurrentUserItems();
        List<OrderItemEntity> orderItemEntities =null;
        if(currentUserItems!=null && currentUserItems.size()>0){
            orderItemEntities = currentUserItems.stream().map( orderItem->{
                OrderItemEntity itemEntity = createOrderItem(orderItem);
                itemEntity.setOrderSn(orderSn);
                return itemEntity;
            }).collect(Collectors.toList());
        }
        return orderItemEntities;
    }

    /**
     * ???????????????
     * @param cartItem
     * @return
     */
    private OrderItemEntity createOrderItem(OrderItemVo cartItem) {
        //????????????
        OrderItemEntity itemEntity = new OrderItemEntity();
        //SPU??????
        Long skuId = cartItem.getSkuId();
        R r = productFeignService.getSpuInfoBySku(skuId);
        if (r.getCode()==0){
            SpuInfoVo spuInfo = r.getData(new TypeReference<SpuInfoVo>() {
            });
            itemEntity.setSpuId(spuInfo.getId());
            itemEntity.setSpuName(spuInfo.getSpuName());
            itemEntity.setSpuBrand(spuInfo.getBrandId().toString());
            itemEntity.setCategoryId(spuInfo.getCatalogId());
        }
        //SKU??????
        itemEntity.setSkuId(skuId);
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        //??????????????????????????????????????????
        String attrs = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(attrs);
        itemEntity.setSkuQuantity(cartItem.getCount());
        //????????????
        //????????????
        itemEntity.setGiftGrowth(cartItem.getPrice().intValue()*cartItem.getCount());
        itemEntity.setGiftIntegration(cartItem.getPrice().intValue()*cartItem.getCount());

        //????????????
        //????????????
        itemEntity.setPromotionAmount(BigDecimal.ZERO);
        //???????????????
        itemEntity.setCouponAmount(BigDecimal.ZERO);
        //????????????
        itemEntity.setIntegrationAmount(BigDecimal.ZERO);
        BigDecimal orign = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        BigDecimal realPrice = orign
                .subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getIntegrationAmount());
        //????????????
        itemEntity.setRealAmount(realPrice);
        return itemEntity;
    }


}