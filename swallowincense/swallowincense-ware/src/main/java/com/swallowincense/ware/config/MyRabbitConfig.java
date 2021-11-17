package com.swallowincense.ware.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class MyRabbitConfig {
    /**
     * JSON序列化器
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Exchange stockEventExchange(){
        //String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange("stock-event-change",true,false);
    }
    @Bean
    public Queue stockReleaseQueue(){
        //String name, boolean durable, boolean exclusive, boolean autoDelete,
        //			@Nullable Map<String, Object> arguments
        return new Queue("stock.release.queue",true,false,false,null);
    }
    @Bean
    public Queue stockDelayQueue(){
        //String name, boolean durable, boolean exclusive, boolean autoDelete,
        //			@Nullable Map<String, Object> arguments
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","stock-event-change");
        arguments.put("x-dead-letter-routing-key","stock.release");
        arguments.put("x-message-ttl",6000);
        return new Queue("stock.delay.queue",true,false,false,arguments);
    }

    @Bean
    public Binding stockReleaseBinding(){
        //String destination, DestinationType destinationType, String exchange, String routingKey,
        //			@Nullable Map<String, Object> arguments
        return new Binding("stock.release.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-change",
                "stock.release.#",
                null);
    }

    @Bean
    public Binding stockDelayBinding(){
        //String destination, DestinationType destinationType, String exchange, String routingKey,
        //			@Nullable Map<String, Object> arguments
        return new Binding("stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-change",
                "stock.locked",
                null);
    }
}
