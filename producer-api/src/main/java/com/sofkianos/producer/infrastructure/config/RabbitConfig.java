package com.sofkianos.producer.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology configuration for the Producer API.
 *
 * <p>Architecture principles:
 * <ul>
 *     <li>API Gateway Pattern: supports the inbound API by defining the outbound messaging contracts.</li>
 *     <li>Asynchronous Messaging: declares exchange, queue, and binding for non-blocking delivery.</li>
 *     <li>Separation of Concerns: isolates messaging infrastructure from web controllers.</li>
 * </ul>
 *
 * <p><strong>Important:</strong> The queue declaration must match the Consumer's
 * declaration exactly — including the {@code x-dead-letter-exchange} argument —
 * to avoid a {@code precondition_failed} error from RabbitMQ.</p>
 */
@Configuration
public class RabbitConfig {

    private static final String DLX_EXCHANGE_NAME = "kudos.dlx";

    @Value("${app.rabbitmq.queue}")
    private String queueName;

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    @Bean
    public Queue kudosQueue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE_NAME)
                .build();
    }

    @Bean
    public TopicExchange kudosExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Binding kudosBinding(Queue kudosQueue, TopicExchange kudosExchange) {
        return BindingBuilder.bind(kudosQueue).to(kudosExchange).with(routingKey);
    }

    @Bean
    public org.springframework.amqp.support.converter.MessageConverter jsonMessageConverter() {
        return new org.springframework.amqp.support.converter.Jackson2JsonMessageConverter();
    }

    @Bean
    public org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory) {
        org.springframework.amqp.rabbit.core.RabbitTemplate template = new org.springframework.amqp.rabbit.core.RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}