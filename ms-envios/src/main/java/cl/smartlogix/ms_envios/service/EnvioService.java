package cl.smartlogix.ms_envios.service;

import cl.smartlogix.ms_envios.config.RabbitMQConfig;
import cl.smartlogix.ms_envios.factory.ShippingFactory;
import cl.smartlogix.ms_envios.factory.ShippingMethod;
import cl.smartlogix.ms_envios.model.Envio;
import cl.smartlogix.ms_envios.repository.EnvioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnvioService {

    private final EnvioRepository envioRepository;
    private final ShippingFactory shippingFactory;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public void createEnvio(Long pedidoId, String tipoDespacho, Double montoTotal) {
        // Obtenemos la estrategia de envío a través del Factory Method
        ShippingMethod shippingMethod = shippingFactory.createShippingMethodFromString(tipoDespacho);

        Envio envio = Envio.builder()
                .pedidoId(pedidoId)
                .tipoDespacho(shippingMethod.getDescription())
                .estadoEnvio("PENDING")
                .costo(shippingMethod.calculateCost(montoTotal))
                .fechaEstimadaEntrega(shippingMethod.calculateEstimatedDeliveryDate())
                .build();

        envioRepository.save(envio);
        log.info("Envio creado en estado PENDING para el pedido {}", pedidoId);
    }

    public List<Envio> getAllEnvios() {
        return envioRepository.findAll();
    }

    @Transactional
    public Envio updateEstadoEnvio(Long id, String nuevoEstado) {
        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Envio no encontrado"));

        envio.setEstadoEnvio(nuevoEstado);
        Envio saved = envioRepository.save(envio);

        // Si el estado es DISPATCHED, notificamos mediante RabbitMQ
        if ("DISPATCHED".equalsIgnoreCase(nuevoEstado)) {
            String message = String.format("{\"envioId\": %d, \"pedidoId\": %d, \"estado\": \"DISPATCHED\"}", saved.getId(), saved.getPedidoId());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "despacho.listo", message);
            log.info("Evento despacho.listo publicado para envío {}", id);
        }

        return saved;
    }

    @Transactional
    public void cancelEnvioByPedidoId(Long pedidoId) {
        envioRepository.findByPedidoId(pedidoId).ifPresent(envio -> {
            envio.setEstadoEnvio("CANCELLED");
            envioRepository.save(envio);
            log.info("Envío para el pedido {} cancelado automáticamente", pedidoId);
        });
    }
}
