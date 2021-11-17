package com.swallowincense.order.listener;

import com.rabbitmq.client.Channel;
import com.swallowincense.common.to.mq.SeckillOrderTo;
import com.swallowincense.order.entity.OrderEntity;
import com.swallowincense.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
@Slf4j
@RabbitListener(queues = "order.seckill.order.queue")
@Service
public class OrderSeckillListener {
    @Autowired
    OrderService orderService;
    @RabbitHandler
    public void createSeckillOrder(SeckillOrderTo seckillOrder, Message message, Channel channel){
        System.out.println("准备创建秒杀单"+seckillOrder);
        try {
            log.info("准备创建秒杀单"+seckillOrder);
            orderService.createSeckillOrder(seckillOrder);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            try {
                channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
