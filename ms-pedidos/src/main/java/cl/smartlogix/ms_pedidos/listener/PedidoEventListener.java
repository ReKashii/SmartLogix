package cl.smartlogix.ms_pedidos.listener;

import cl.smartlogix.ms_pedidos.config.RabbitMQConfig;
import cl.smartlogix.ms_pedidos.service.PedidoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoEventListener {

    private final PedidoService pedidoService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ENVIO_CANCELADO)
    public void onEnvioCancelado(String message) {
        log.info("Mensaje recibido en ms-pedidos (Envio Cancelado): {}", message);
        try {
            JsonNode node = objectMapper.readTree(message);
            Long pedidoId = node.get("pedidoId").asLong();
            
            // Delete order which sets status to CANCELLED and restores stock
            pedidoService.deleteOrder(pedidoId);
            log.info("Pedido {} cancelado porque el envío fue cancelado desde bodega", pedidoId);
        } catch (Exception e) {
            log.error("Error al procesar evento envio.cancelado: {}", e.getMessage());
        }
    }
}
