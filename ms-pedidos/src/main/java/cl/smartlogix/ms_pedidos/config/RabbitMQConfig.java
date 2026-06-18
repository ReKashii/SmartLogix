package cl.smartlogix.ms_pedidos.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "smartlogix.exchange";
    public static final String QUEUE_ENVIO_CANCELADO = "queue.pedidos.envio.cancelado";
    public static final String ROUTING_KEY_ENVIO_CANCELADO = "envio.cancelado";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue queueEnvioCancelado() {
        return new Queue(QUEUE_ENVIO_CANCELADO, true);
    }

    @Bean
    public Binding bindingEnvioCancelado(Queue queueEnvioCancelado, TopicExchange exchange) {
        return BindingBuilder.bind(queueEnvioCancelado).to(exchange).with(ROUTING_KEY_ENVIO_CANCELADO);
    }
}
