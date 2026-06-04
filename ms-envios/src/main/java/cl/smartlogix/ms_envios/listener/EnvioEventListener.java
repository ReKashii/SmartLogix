package cl.smartlogix.ms_envios.listener;

import cl.smartlogix.ms_envios.config.RabbitMQConfig;
import cl.smartlogix.ms_envios.service.EnvioService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnvioEventListener {

    private final EnvioService envioService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PEDIDO_CREADO)
    public void onPedidoCreado(String message) {
        log.info("Mensaje recibido en ms-envios: {}", message);
        try {
            // Se espera JSON: {"pedidoId": 1, "tipoDespacho": "EXPRESS", "montoTotal": 15000.0}
            JsonNode node = objectMapper.readTree(message);
            Long pedidoId = node.get("pedidoId").asLong();
            String tipoDespacho = node.get("tipoDespacho").asText();
            Double montoTotal = node.get("montoTotal").asDouble();

            envioService.createEnvio(pedidoId, tipoDespacho, montoTotal);
        } catch (Exception e) {
            log.error("Error al procesar evento pedido.creado: {}", e.getMessage());
            // Compatibilidad con el mensaje antiguo que era texto plano: "Order created with ID: X"
            if (message.startsWith("Order created with ID:")) {
                try {
                    Long pedidoId = Long.parseLong(message.split(":")[1].trim());
                    // Asumimos valores por defecto si no es JSON
                    envioService.createEnvio(pedidoId, "STANDARD", 0.0);
                } catch (Exception ex) {
                    log.error("No se pudo extraer ID del mensaje legacy.");
                }
            }
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PEDIDO_CANCELADO)
    public void onPedidoCancelado(String message) {
        log.info("Mensaje de cancelación recibido en ms-envios: {}", message);
        try {
            JsonNode node = objectMapper.readTree(message);
            if (node.has("pedidoId")) {
                Long pedidoId = node.get("pedidoId").asLong();
                envioService.cancelEnvioByPedidoId(pedidoId);
            }
        } catch (Exception e) {
            log.error("Error al procesar evento pedido.cancelado: {}", e.getMessage());
        }
    }
}
