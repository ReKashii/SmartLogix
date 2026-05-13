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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    @InjectMocks
    private PedidoService pedidoService;

    private final String customer = "Test Customer";
    private final Long productId = 1L;
    private final Integer quantity = 2;
    private final Double amount = 200.0;
    private final ShippingFactory.ShippingType shipType = ShippingFactory.ShippingType.EXPRESS;

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
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("COMPLETED", result.getEstado());
        verify(inventarioClient, times(1)).deductStock(productId, quantity);
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void createOrder_InventoryFailure_ThrowsException() {
        // Arrange
        when(inventarioClient.deductStock(productId, quantity)).thenThrow(new RuntimeException("Out of stock"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            pedidoService.createOrder(customer, productId, quantity, amount, shipType);
        });

        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }
}
