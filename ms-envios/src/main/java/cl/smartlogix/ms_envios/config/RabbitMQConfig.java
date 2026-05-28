package cl.smartlogix.ms_envios.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "smartlogix.exchange";
    public static final String QUEUE_PEDIDO_CREADO = "queue.pedido.creado";
    public static final String ROUTING_KEY_PEDIDO_CREADO = "pedido.creado";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue queuePedidoCreado() {
        return new Queue(QUEUE_PEDIDO_CREADO, true);
    }

    @Bean
    public Binding bindingPedidoCreado(Queue queuePedidoCreado, TopicExchange exchange) {
        return BindingBuilder.bind(queuePedidoCreado).to(exchange).with(ROUTING_KEY_PEDIDO_CREADO);
    }
}
