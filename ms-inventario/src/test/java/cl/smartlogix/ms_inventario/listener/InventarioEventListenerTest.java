package cl.smartlogix.ms_inventario.listener;

import cl.smartlogix.ms_inventario.service.InventarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioEventListenerTest {

    @Mock
    private InventarioService inventarioService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private InventarioEventListener inventarioEventListener;

    @Test
    void handlePedidoCancelado_ValidMessage_RestoresStock() {
        // Arrange
        String message = "{\"productoId\": 1, \"cantidad\": 5}";

        // Act
        inventarioEventListener.handlePedidoCancelado(message);

        // Assert
        verify(inventarioService, times(1)).restoreStock(1L, 5);
    }

    @Test
    void handlePedidoCancelado_InvalidMessage_LogsErrorAndDoesNotRestore() {
        // Arrange
        String invalidMessage = "invalid json";

        // Act
        inventarioEventListener.handlePedidoCancelado(invalidMessage);

        // Assert
        verify(inventarioService, never()).restoreStock(anyLong(), anyInt());
    }

    @Test
    void handlePedidoCancelado_MissingFields_LogsErrorAndDoesNotRestore() {
        // Arrange
        String message = "{\"someOtherField\": 123}";

        // Act
        inventarioEventListener.handlePedidoCancelado(message);

        // Assert
        // Jackson will throw NullPointerException when calling asLong()/asInt() on null, which is caught and logged
        verify(inventarioService, never()).restoreStock(anyLong(), anyInt());
    }
}
