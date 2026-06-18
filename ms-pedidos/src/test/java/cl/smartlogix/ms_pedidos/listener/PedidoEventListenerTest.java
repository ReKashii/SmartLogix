package cl.smartlogix.ms_pedidos.listener;

import cl.smartlogix.ms_pedidos.service.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoEventListenerTest {

    @Mock
    private PedidoService pedidoService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PedidoEventListener pedidoEventListener;

    @Test
    void onEnvioCancelado_ValidMessage_DeletesOrder() {
        // Arrange
        String message = "{\"pedidoId\": 123}";

        // Act
        pedidoEventListener.onEnvioCancelado(message);

        // Assert
        verify(pedidoService, times(1)).deleteOrder(123L);
    }

    @Test
    void onEnvioCancelado_InvalidMessage_LogsErrorAndDoesNotDelete() {
        // Arrange
        String message = "invalid-json";

        // Act
        pedidoEventListener.onEnvioCancelado(message);

        // Assert
        verify(pedidoService, never()).deleteOrder(anyLong());
    }

    @Test
    void onEnvioCancelado_MissingPedidoId_LogsErrorAndDoesNotDelete() {
        // Arrange
        String message = "{\"otherField\": 123}";

        // Act
        pedidoEventListener.onEnvioCancelado(message);

        // Assert
        verify(pedidoService, never()).deleteOrder(anyLong());
    }
}
