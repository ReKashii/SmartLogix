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
                .productoId(productId)
                .cantidad(quantity)
                .montoTotal(amount)
                .tipoDespacho(shippingDesc)
                .estado("COMPLETED")
                .build();

        Pedido savedPedido = pedidoRepository.save(pedido);

        // 4. Asynchronous event publishing to RabbitMQ
        String eventMessage = String.format("{\"pedidoId\": %d, \"tipoDespacho\": \"%s\", \"montoTotal\": %.2f}", 
                savedPedido.getId(), shipType.name(), amount).replace(',', '.');
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, eventMessage);
        log.info("Order {} created and event published: {}", savedPedido.getId(), eventMessage);

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
        return pedidoRepository.findAll().stream()
                .filter(p -> !"CANCELLED".equals(p.getEstado()))
                .toList();
    }

    /**
     * Deletes an order by ID (Soft delete) and restores inventory.
     */
    @Transactional
    public void deleteOrder(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        pedido.setEstado("CANCELLED");
        pedidoRepository.save(pedido);
        
        // Publish event to RabbitMQ to restore stock
        Long prodId = pedido.getProductoId() != null ? pedido.getProductoId() : 0L;
        Integer cant = pedido.getCantidad() != null ? pedido.getCantidad() : 0;
        String cancelEvent = String.format("{\"productoId\": %d, \"cantidad\": %d}", prodId, cant);
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "pedido.cancelado", cancelEvent);
        
        log.info("Order {} cancelled and event published to restore stock", id);
    }

    /**
     * Updates an existing order.
     * NOTE: This is a basic update. In a real system, if the product or quantity changes,
     * you must recalculate stock (restore old stock, deduct new stock).
     */
    @Transactional
    public Pedido updateOrder(Long id, String cliente, Long productoId, Integer cantidad, Double montoTotal, ShippingFactory.ShippingType shipType) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        pedido.setCliente(cliente);
        pedido.setProductoId(productoId);
        pedido.setCantidad(cantidad);
        pedido.setMontoTotal(montoTotal);
        pedido.setTipoDespacho(shippingFactory.createShippingMethod(shipType));
        
        return pedidoRepository.save(pedido);
    }
}
