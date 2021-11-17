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
        //RequestContextHolder将请求信息存在ThreadLocal中，线程之间不共享，每次从主线程中获得
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> memberFeignFuture = CompletableFuture.runAsync(() -> {
            //赋值到子线程
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberVo.getId());
            orderConfirmVo.setAddress(address);
        }, executor);
        CompletableFuture<Void> orderFeignFuture = CompletableFuture.runAsync(() -> {
            //购物车
            //feign在远程调用之前会构造请求调用很多拦截器，默认是没有的，会丢失请求头信息
            //赋值到子线程
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

        //用户积分
        Integer integration = memberVo.getIntegration();
        orderConfirmVo.setIntegration(integration);
        //其他数据自己计算
        // TODO 放重复提交令牌
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
        //1、验证令牌[对比删除必须保证原子性]，lua脚本 返回值为影响行数
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        //页面token
        String orderToken = vo.getOrderToken();
        //服务器token key
        String key = OrderConstant.USER_ORDR_TOKEN_PREFIX + memberVo.getId();
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Collections.singletonList(key), orderToken);

        if(result!=null && result==1L){
            //验证通过
            redisTemplate.delete(OrderConstant.USER_ORDR_TOKEN_PREFIX + memberVo.getId());
            //创建订单、验令牌、验价格、锁库存
            OrderCreateTo order = createOrder();

            BigDecimal payPrice = order.getOrder().getPayAmount();
            BigDecimal voPayPrice = vo.getPayPrice();
            if (Math.abs(payPrice.subtract(voPayPrice).doubleValue())<0.01){
                //金额对比
                //保存订单
                saveOrder(order);
                //库存锁定
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> collect = order.getOrderItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setSkuId(item.getSkuId());
                    return orderItemVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setLocks(collect);
                //远程调用锁定库存
                R r = wmsFeignService.orderLockStock(wareSkuLockVo);
                if(r.getCode()==0){
                    //锁成功
                    submitOrderResponseVo.setOrder(order.getOrder());
                    //订单创建成功，发送消息
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrder());
                    return submitOrderResponseVo;
                }else {
                    //锁定失败
                    submitOrderResponseVo.setCode(3);
                    return submitOrderResponseVo;
                }
            }else {
                submitOrderResponseVo.setCode(2);
                return submitOrderResponseVo;
            }
        }else {
            //不通过
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
        //查询订单的最新状态
        OrderEntity byId = this.getById(orderEntity.getId());
        if (byId.getStatus()== OrderStatusEnum.CREATE_NEW.getCode()) {
            //关单
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
     * 保存订单及订单项
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
     * 创建订单
     * @return
     */
    private OrderCreateTo createOrder(){

        OrderSubmitVo orderSubmitVo = submitVoThreadLocal.get();
        OrderCreateTo createTo = new OrderCreateTo();

        //创建订单号、雪花算法
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(orderSubmitVo, orderSn);
        createTo.setOrder(orderEntity);

        //获取订单项信息
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);
        createTo.setOrderItems(orderItemEntities);

        //验价.
        computePrice(orderEntity,orderItemEntities);

        return createTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        //1、订单价格相关
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalCoupon = BigDecimal.ZERO;
        BigDecimal totalIntegration = BigDecimal.ZERO;
        BigDecimal totalPromotion = BigDecimal.ZERO;
        //2、积分
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
        //订单总额
        orderEntity.setTotalAmount(totalPrice);
        //应付总额
        orderEntity.setPayAmount(totalPrice.add(orderEntity.getFreightAmount()));
        orderEntity.setCouponAmount(totalCoupon);
        orderEntity.setIntegrationAmount(totalIntegration);
        orderEntity.setPromotionAmount(totalPromotion);
        //积分信息
        orderEntity.setGrowth(totalGrowth.intValue());
        orderEntity.setIntegration(totalGiftIntegration.intValue());
        //未删除
        orderEntity.setDeleteStatus(0);
    }

    /**
     * 创建订单
     * @param orderSubmitVo orderSn
     * @param orderSn
     * @return
     */
    private OrderEntity buildOrder(OrderSubmitVo orderSubmitVo, String orderSn) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        //获取收获地址信息以及运费信息
        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        if(fare.getCode()==0){
            FareVo fareVo = fare.getData(new TypeReference<FareVo>() {
            });
            //运费
            orderEntity.setFreightAmount(fareVo.getFare());
            //地址
            orderEntity.setReceiverProvince(fareVo.getAddress().getProvince());
            orderEntity.setReceiverCity(fareVo.getAddress().getCity());
            orderEntity.setReceiverRegion(fareVo.getAddress().getRegion());
            orderEntity.setReceiverDetailAddress(fareVo.getAddress().getDetailAddress());
            orderEntity.setReceiverPostCode(fareVo.getAddress().getPostCode());
            //收货人信息
            orderEntity.setMemberId(fareVo.getAddress().getMemberId());
            orderEntity.setReceiverName(fareVo.getAddress().getName());
            orderEntity.setReceiverPhone(fareVo.getAddress().getPhone());

            //设置订单状态
            orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
            orderEntity.setAutoConfirmDay(7);
        }
        return orderEntity;
    }

    /**
     * 构建所有订单项
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        //最后一次确认订单项的价格
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
     * 构建订单项
     * @param cartItem
     * @return
     */
    private OrderItemEntity createOrderItem(OrderItemVo cartItem) {
        //订单信息
        OrderItemEntity itemEntity = new OrderItemEntity();
        //SPU信息
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
        //SKU信息
        itemEntity.setSkuId(skuId);
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        //将集合以某个分隔符组成字符串
        String attrs = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(attrs);
        itemEntity.setSkuQuantity(cartItem.getCount());
        //优惠信息
        //积分信息
        itemEntity.setGiftGrowth(cartItem.getPrice().intValue()*cartItem.getCount());
        itemEntity.setGiftIntegration(cartItem.getPrice().intValue()*cartItem.getCount());

        //价格信息
        //促销信息
        itemEntity.setPromotionAmount(BigDecimal.ZERO);
        //优惠券信息
        itemEntity.setCouponAmount(BigDecimal.ZERO);
        //积分信息
        itemEntity.setIntegrationAmount(BigDecimal.ZERO);
        BigDecimal orign = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        BigDecimal realPrice = orign
                .subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getIntegrationAmount());
        //实际价格
        itemEntity.setRealAmount(realPrice);
        return itemEntity;
    }


}