package cl.smartlogix.ms_inventario.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "smartlogix.exchange";
    public static final String CANCEL_QUEUE = "inventario.pedido.cancelado";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue cancelQueue() {
        return new Queue(CANCEL_QUEUE);
    }

    @Bean
    public Binding cancelBinding(Queue cancelQueue, TopicExchange exchange) {
        return BindingBuilder.bind(cancelQueue).to(exchange).with("pedido.cancelado");
    }
}
