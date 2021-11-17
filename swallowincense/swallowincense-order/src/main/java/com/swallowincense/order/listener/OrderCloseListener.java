package com.swallowincense.order.listener;

import com.rabbitmq.client.Channel;
import com.swallowincense.order.entity.OrderEntity;
import com.swallowincense.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "order.release.order.queue")
public class OrderCloseListener {
    @Autowired
    OrderService orderService;
    @RabbitHandler
    public void closeOrderHandler(OrderEntity orderEntity, Message message, Channel channel){
        try {
            orderService.closeOrder(orderEntity);
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
