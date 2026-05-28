package cl.smartlogix.ms_inventario.listener;

import cl.smartlogix.ms_inventario.config.RabbitMQConfig;
import cl.smartlogix.ms_inventario.service.InventarioService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventarioEventListener {

    private final InventarioService inventarioService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.CANCEL_QUEUE)
    public void handlePedidoCancelado(String message) {
        try {
            log.info("Received pedido.cancelado event: {}", message);
            JsonNode jsonNode = objectMapper.readTree(message);
            Long productoId = jsonNode.get("productoId").asLong();
            Integer cantidad = jsonNode.get("cantidad").asInt();
            
            inventarioService.restoreStock(productoId, cantidad);
            log.info("Stock restored successfully from cancelled order. Product: {}, Restored Quantity: {}", productoId, cantidad);
        } catch (Exception e) {
            log.error("Failed to process pedido.cancelado event: {}", e.getMessage(), e);
        }
    }
}
