package com.tihai.config;

import com.tihai.constant.MQConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Copyright : DuanInnovator
 * @Description : RabbitMQ配置
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/2
 * @Link : <a href="https://github.com/DuanInnovator/TiHaiWuYou-Admin/tree/mine-admin">...</a>
 **/
@Configuration
public class RabbitConfig {

    // 定义任务队列
    @Bean
    public Queue taskQueue() {
        return new Queue(MQConstant.QUEUE_NAME, true);
    }

    // 定义交换机
    @Bean
    public DirectExchange taskExchange() {
        return new DirectExchange(MQConstant.EXCHANGE_NAME);
    }

    // 绑定队列与交换机
    @Bean
    public Binding taskBinding() {
        return BindingBuilder.bind(taskQueue())
                .to(taskExchange())
                .with(MQConstant.ROUTING_KEY);
    }
}
