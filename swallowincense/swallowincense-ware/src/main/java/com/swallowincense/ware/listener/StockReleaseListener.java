package com.swallowincense.ware.listener;

import com.rabbitmq.client.Channel;
import com.swallowincense.common.to.mq.StockLockedTo;
import com.swallowincense.ware.service.WareSkuService;
import com.swallowincense.ware.vo.OrderVo;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "stock.release.queue")
public class StockReleaseListener {
    @Autowired
    WareSkuService wareSkuService;

    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel){
        try{
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            try {
                channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }

    @RabbitHandler
    public void handleOrderCloseRelease(OrderVo vo, Message message, Channel channel) throws IOException {
        System.out.println("订单关闭解锁库存");
        try{
            wareSkuService.unlockStock(vo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);

        }

    }
}
