package com.swallowincense.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyRabbitMqConfig  {
    @Autowired
    RabbitTemplate rabbitTemplate;
    //配置amqp序列化器
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
    //配置发送消息到Broke confirmCallback
    //配置交换机到队列时失败回调 returnCallback
    @PostConstruct //当前类被加载之后进行
    public void initRabbitTemplate(){
        //new RabbitTemplate.ConfirmCallback()
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            //回调逻辑
        });
        //new RabbitTemplate.ReturnsCallback() 参数ReturnedMessage
        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            //失败回调逻辑
        });
    }

    /**
     * 创建Binding Queue Exchange
     */
    @Bean //自动创建,一旦创建好，不会修改覆盖
    public Queue orderDelayQueue(){
        //String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments
        Map<String,Object> arguments = new HashMap<>(3);
        arguments.put("x-dead-letter-exchange","order-event-exchange");
        arguments.put("x-dead-letter-routing-key","order.release.order");
        arguments.put("x-message-ttl",6000);
        return new Queue("order.delay.queue", true, false, false,arguments);
    }

    @Bean
    public Queue orderReleaseQueue(){
        return new Queue("order.release.order.queue", true, false, false);
    }

    @Bean
    public Exchange orderEventExchange(){
        //String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange("order-event-exchange",true,false);
    }

    @Bean
    public Binding createOrderBinding(){
        //String destination, DestinationType destinationType, String exchange, String routingKey,
        //			@Nullable Map<String, Object> arguments
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null);
    }

    @Bean
    public Binding releaseOrderBinding(){
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null);
    }

    @Bean
    public Binding orderReleaseOther(){
        return new Binding("stock.release.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#",
                null);
    }
    @Bean
    public Queue orderSeckillQueue(){
        return new Queue("order.seckill.order.queue",true,false,false,null);
    }

    @Bean
    public Binding orderSeckillBinding(){
        return new Binding("order.seckill.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.seckill.order",
                null);
    }

}
