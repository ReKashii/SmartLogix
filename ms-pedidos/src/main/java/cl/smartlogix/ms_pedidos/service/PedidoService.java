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
    private final org.springframework.transaction.support.TransactionTemplate transactionTemplate;

    private static final String EXCHANGE_NAME = "smartlogix.exchange";
    private static final String ROUTING_KEY = "pedido.creado";
    private static final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    /**
     * Creates a new order. 
     * 1. Deducts stock synchronously using an external API call (outside database transaction).
     * 2. Saves the order to the database and publishes event in a transaction block.
     * 
     * @param cliente Customer name
     * @param productId ID of the product to order
     * @param quantity Quantity to purchase
     * @param amount Total amount
     * @param shipType Shipping type enum
     * @return The created order
     * @throws RuntimeException if stock deduction fails or Circuit Breaker is open
     */
    @CircuitBreaker(name = "inventarioCB", fallbackMethod = "fallbackDeductStock")
    public Pedido createOrder(String cliente, Long productId, Integer quantity, Double amount, ShippingFactory.ShippingType shipType) {
        log.info("Attempting to create order for customer: {}", cliente);

        // 1. Synchronous call to Inventario MS to deduct stock (outside transaction)
        inventarioClient.deductStock(productId, quantity);

        // 2. Save order locally and publish event inside transaction boundary
        return transactionTemplate.execute(status -> {
            String shippingDesc = shippingFactory.createShippingMethod(shipType);

            Pedido pedido = Pedido.builder()
                    .cliente(cliente)
                    .productoId(productId)
                    .cantidad(quantity)
                    .montoTotal(amount)
                    .tipoDespacho(shippingDesc)
                    .estado("COMPLETED")
                    .build();

            Pedido savedPedido = pedidoRepository.save(pedido);

            // Publish event within transaction to ensure consistency
            java.util.Map<String, Object> event = new java.util.HashMap<>();
            event.put("pedidoId", savedPedido.getId());
            event.put("tipoDespacho", shipType.name());
            event.put("montoTotal", amount);

            String eventMessage;
            try {
                eventMessage = objectMapper.writeValueAsString(event);
            } catch (Exception e) {
                log.error("Failed to serialize order creation event", e);
                throw new RuntimeException("Error processing order event", e);
            }

            rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, eventMessage);
            log.info("Order {} created and event published: {}", savedPedido.getId(), eventMessage);

            return savedPedido;
        });
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
        String cancelEvent = String.format("{\"pedidoId\": %d, \"productoId\": %d, \"cantidad\": %d}", id, prodId, cant);
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "pedido.cancelado", cancelEvent);
        
        log.info("Order {} cancelled and event published to restore stock", id);
    }

    /**
     * Updates an existing order.
     * NOTE: This is a basic update. In a real system, if the product or quantity changes,
     * you must recalculate stock (restore old stock, deduct new stock).
     */
    // Execute stock adjustments outside transaction block to protect Hikari pool
    public Pedido updateOrder(Long id, String cliente, Long productoId, Integer cantidad, Double montoTotal, ShippingFactory.ShippingType shipType) {
        Pedido oldPedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        Long oldProductId = oldPedido.getProductoId();
        Integer oldQuantity = oldPedido.getCantidad();

        // Check if there are changes in product or quantity
        if (!productoId.equals(oldProductId) || !cantidad.equals(oldQuantity)) {
            // Restore previous stock
            if (oldProductId != null && oldQuantity != null) {
                log.info("Restoring previous stock for product ID: {} (Quantity: {})", oldProductId, oldQuantity);
                inventarioClient.restoreStock(oldProductId, oldQuantity);
            }
            // Deduct new stock
            log.info("Deducting new stock for product ID: {} (Quantity: {})", productoId, cantidad);
            inventarioClient.deductStock(productoId, cantidad);
        }

        // Save new order details inside a transactional context
        return transactionTemplate.execute(status -> {
            Pedido pedido = pedidoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
            
            pedido.setCliente(cliente);
            pedido.setProductoId(productoId);
            pedido.setCantidad(cantidad);
            pedido.setMontoTotal(montoTotal);
            pedido.setTipoDespacho(shippingFactory.createShippingMethod(shipType));
            
            return pedidoRepository.save(pedido);
        });
    }
}
