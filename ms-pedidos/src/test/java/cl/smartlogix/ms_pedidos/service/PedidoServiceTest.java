package cl.smartlogix.ms_pedidos.service;

import cl.smartlogix.ms_pedidos.client.InventarioClient;
import cl.smartlogix.ms_pedidos.factory.ShippingFactory;
import cl.smartlogix.ms_pedidos.model.Pedido;
import cl.smartlogix.ms_pedidos.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private InventarioClient inventarioClient;

    @Mock
    private ShippingFactory shippingFactory;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private PedidoService pedidoService;

    private final String customer = "Test Customer";
    private final Long productId = 1L;
    private final Integer quantity = 2;
    private final Double amount = 200.0;
    private final ShippingFactory.ShippingType shipType = ShippingFactory.ShippingType.EXPRESS;

    @BeforeEach
    void setUp() {
        // Stub TransactionTemplate to run the callback block inline
        lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
    }

    @Test
    void createOrder_Success() {
        // Arrange
        when(inventarioClient.deductStock(productId, quantity)).thenReturn(new Object());
        when(shippingFactory.createShippingMethod(shipType)).thenReturn("Express Shipping");

        Pedido pedido = Pedido.builder()
                .id(1L)
                .cliente(customer)
                .montoTotal(amount)
                .tipoDespacho("Express Shipping")
                .estado("COMPLETED")
                .build();

        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // Act
        Pedido result = pedidoService.createOrder(customer, productId, quantity, amount, shipType);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEstado()).isEqualTo("COMPLETED");
        verify(inventarioClient, times(1)).deductStock(productId, quantity);
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void createOrder_InventoryFailure_ThrowsException() {
        // Arrange
        when(inventarioClient.deductStock(productId, quantity)).thenThrow(new RuntimeException("Out of stock"));

        // Act & Assert
        assertThatThrownBy(() -> {
            pedidoService.createOrder(customer, productId, quantity, amount, shipType);
        }).isInstanceOf(RuntimeException.class);

        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void updateOrder_NoProductOrQuantityChange_SavesWithoutStockAdjustment() {
        // Arrange
        Pedido oldPedido = Pedido.builder()
                .id(1L)
                .cliente(customer)
                .productoId(productId)
                .cantidad(quantity)
                .montoTotal(amount)
                .tipoDespacho("Express Shipping")
                .estado("COMPLETED")
                .build();

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(oldPedido));
        when(shippingFactory.createShippingMethod(shipType)).thenReturn("Express Shipping");
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Pedido result = pedidoService.updateOrder(1L, "Updated Customer", productId, quantity, 300.0, shipType);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCliente()).isEqualTo("Updated Customer");
        assertThat(result.getMontoTotal()).isEqualTo(300.0);
        verify(inventarioClient, never()).restoreStock(anyLong(), anyInt());
        verify(inventarioClient, never()).deductStock(anyLong(), anyInt());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void updateOrder_ProductOrQuantityChanged_RestoresOldAndDeductsNewStock() {
        // Arrange
        Pedido oldPedido = Pedido.builder()
                .id(1L)
                .cliente(customer)
                .productoId(productId)
                .cantidad(quantity)
                .montoTotal(amount)
                .tipoDespacho("Express Shipping")
                .estado("COMPLETED")
                .build();

        Long newProductId = 2L;
        Integer newQuantity = 10;

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(oldPedido));
        when(inventarioClient.restoreStock(productId, quantity)).thenReturn(new Object());
        when(inventarioClient.deductStock(newProductId, newQuantity)).thenReturn(new Object());
        when(shippingFactory.createShippingMethod(shipType)).thenReturn("Express Shipping");
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Pedido result = pedidoService.updateOrder(1L, "Updated Customer", newProductId, newQuantity, 500.0, shipType);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductoId()).isEqualTo(newProductId);
        assertThat(result.getCantidad()).isEqualTo(newQuantity);
        verify(inventarioClient, times(1)).restoreStock(productId, quantity);
        verify(inventarioClient, times(1)).deductStock(newProductId, newQuantity);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void updateOrder_NotFound_ThrowsException() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> {
            pedidoService.updateOrder(1L, "Updated Customer", productId, quantity, 500.0, shipType);
        }).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Order not found with id: 1");

        verify(inventarioClient, never()).restoreStock(anyLong(), anyInt());
        verify(inventarioClient, never()).deductStock(anyLong(), anyInt());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void fallbackDeductStock_ThrowsException() {
        // Arrange
        Throwable throwable = new RuntimeException("Connection timed out");

        // Act & Assert
        assertThatThrownBy(() -> {
            pedidoService.fallbackDeductStock(customer, productId, quantity, amount, shipType, throwable);
        }).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Inventory service is currently unavailable. Please try again later.");
    }

    @Test
    void getAllOrders_FiltersOutCancelledOrders() {
        // Arrange
        Pedido activeOrder = Pedido.builder().id(1L).estado("COMPLETED").build();
        Pedido cancelledOrder = Pedido.builder().id(2L).estado("CANCELLED").build();
        when(pedidoRepository.findAll()).thenReturn(java.util.List.of(activeOrder, cancelledOrder));

        // Act
        java.util.List<Pedido> result = pedidoService.getAllOrders();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(pedidoRepository, times(1)).findAll();
    }

    @Test
    void deleteOrder_Success() {
        // Arrange
        Pedido order = Pedido.builder()
                .id(1L)
                .productoId(productId)
                .cantidad(quantity)
                .estado("COMPLETED")
                .build();
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        pedidoService.deleteOrder(1L);

        // Assert
        assertThat(order.getEstado()).isEqualTo("CANCELLED");
        verify(pedidoRepository, times(1)).save(order);
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("smartlogix.exchange"),
                eq("pedido.cancelado"),
                contains("\"pedidoId\": 1")
        );
    }

    @Test
    void deleteOrder_NotFound_ThrowsException() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> {
            pedidoService.deleteOrder(1L);
        }).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Order not found with id: 1");

        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }
}

