package com.swallowincense.seckill.config;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class MyRabbitMqConfig {
    @Autowired
    RabbitTemplate rabbitTemplate;
    //配置amqp序列化器
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
    //配置发送消息到Broke confirmCallback
    //配置交换机到队列的是被回调 returnCallback
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
}
