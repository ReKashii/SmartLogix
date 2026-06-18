package cl.smartlogix.ms_envios.listener;

import cl.smartlogix.ms_envios.service.EnvioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvioEventListenerTest {

    @Mock
    private EnvioService envioService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private EnvioEventListener envioEventListener;

    @Test
    void onPedidoCreado_ValidJsonMessage_CreatesEnvio() {
        // Arrange
        String message = "{\"pedidoId\": 10, \"tipoDespacho\": \"EXPRESS\", \"montoTotal\": 15000.0}";

        // Act
        envioEventListener.onPedidoCreado(message);

        // Assert
        verify(envioService, times(1)).createEnvio(10L, "EXPRESS", 15000.0);
    }

    @Test
    void onPedidoCreado_LegacyTextMessage_CreatesEnvioWithDefaultValues() {
        // Arrange
        String message = "Order created with ID: 45";

        // Act
        envioEventListener.onPedidoCreado(message);

        // Assert
        verify(envioService, times(1)).createEnvio(45L, "STANDARD", 0.0);
    }

    @Test
    void onPedidoCreado_InvalidMessage_LogsErrorAndDoesNotCreate() {
        // Arrange
        String message = "invalid string pattern";

        // Act
        envioEventListener.onPedidoCreado(message);

        // Assert
        verify(envioService, never()).createEnvio(anyLong(), anyString(), anyDouble());
    }

    @Test
    void onPedidoCancelado_ValidJsonMessage_CancelsEnvio() {
        // Arrange
        String message = "{\"pedidoId\": 22}";

        // Act
        envioEventListener.onPedidoCancelado(message);

        // Assert
        verify(envioService, times(1)).cancelEnvioByPedidoId(22L);
    }

    @Test
    void onPedidoCancelado_InvalidMessage_LogsErrorAndDoesNotCancel() {
        // Arrange
        String message = "invalid json";

        // Act
        envioEventListener.onPedidoCancelado(message);

        // Assert
        verify(envioService, never()).cancelEnvioByPedidoId(anyLong());
    }
}
