package cl.smartlogix.ms_pedidos.service;

import cl.smartlogix.ms_pedidos.client.InventarioClient;
import cl.smartlogix.ms_pedidos.factory.ShippingFactory;
import cl.smartlogix.ms_pedidos.model.Pedido;
import cl.smartlogix.ms_pedidos.repository.PedidoRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Core service for order processing.
 * Implements synchronous stock deduction via Feign with a Circuit Breaker
 * and asynchronous event notification via RabbitMQ.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final InventarioClient inventarioClient;
    private final ShippingFactory shippingFactory;
    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE_NAME = "smartlogix.exchange";
    private static final String ROUTING_KEY = "pedido.creado";

    /**
     * Creates a new order. 
     * 1. Deducts stock synchronously using an external API call.
     * 2. Saves the order to the database.
     * 3. Publishes a "pedido.creado" event to RabbitMQ.
     * 
     * @param cliente Customer name
     * @param productId ID of the product to order
     * @param quantity Quantity to purchase
     * @param amount Total amount
     * @param shipType Shipping type enum
     * @return The created order
     * @throws RuntimeException if stock deduction fails or Circuit Breaker is open
     */
    @Transactional
    @CircuitBreaker(name = "inventarioCB", fallbackMethod = "fallbackDeductStock")
    public Pedido createOrder(String cliente, Long productId, Integer quantity, Double amount, ShippingFactory.ShippingType shipType) {
        log.info("Attempting to create order for customer: {}", cliente);

        // 1. Synchronous call to Inventario MS to deduct stock
        inventarioClient.deductStock(productId, quantity);

        // 2. Use Factory Method to determine shipping description
        String shippingDesc = shippingFactory.createShippingMethod(shipType);

        // 3. Save order locally
        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .montoTotal(amount)
                .tipoDespacho(shippingDesc)
                .estado("COMPLETED")
                .build();

        Pedido savedPedido = pedidoRepository.save(pedido);

        // 4. Asynchronous event publishing to RabbitMQ
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, "Order created with ID: " + savedPedido.getId());
        log.info("Order {} created and event published", savedPedido.getId());

        return savedPedido;
    }

    /**
     * Fallback method for Circuit Breaker when Inventario MS is unavailable or fails.
     */
    public Pedido fallbackDeductStock(String cliente, Long productId, Integer quantity, Double amount, ShippingFactory.ShippingType shipType, Throwable t) {
        log.error("Circuit Breaker OPEN or Error occurred calling Inventory MS: {}", t.getMessage());
        throw new RuntimeException("Inventory service is currently unavailable. Please try again later.");
    }

    public List<Pedido> getAllOrders() {
        return pedidoRepository.findAll();
    }
}
