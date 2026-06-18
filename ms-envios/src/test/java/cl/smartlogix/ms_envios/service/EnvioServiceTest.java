package cl.smartlogix.ms_envios.service;

import cl.smartlogix.ms_envios.config.RabbitMQConfig;
import cl.smartlogix.ms_envios.factory.ShippingFactory;
import cl.smartlogix.ms_envios.factory.ShippingMethod;
import cl.smartlogix.ms_envios.model.Envio;
import cl.smartlogix.ms_envios.repository.EnvioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvioServiceTest {

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private ShippingFactory shippingFactory;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EnvioService envioService;

    @Captor
    private ArgumentCaptor<Envio> envioCaptor;

    private Envio envioMock;
    private final Long PEDIDO_ID = 100L;
    private final Long ENVIO_ID = 1L;

    @BeforeEach
    void setUp() {
        envioMock = Envio.builder()
                .id(ENVIO_ID)
                .pedidoId(PEDIDO_ID)
                .estadoEnvio("PENDING")
                .tipoDespacho("STANDARD")
                .build();
    }

    @Test
    void givenValidData_whenCreateEnvio_thenCalculatesCostAndSaves() {
        // Arrange
        String tipoDespacho = "STANDARD";
        Double montoTotal = 5000.0;
        
        ShippingMethod mockShippingMethod = mock(ShippingMethod.class);
        when(shippingFactory.createShippingMethodFromString(tipoDespacho)).thenReturn(mockShippingMethod);
        when(mockShippingMethod.getDescription()).thenReturn("Standard Delivery");
        when(mockShippingMethod.calculateCost(montoTotal)).thenReturn(1500.0);
        when(mockShippingMethod.calculateEstimatedDeliveryDate()).thenReturn(LocalDate.now().plusDays(3));

        // Act
        envioService.createEnvio(PEDIDO_ID, tipoDespacho, montoTotal);

        // Assert
        verify(envioRepository, times(1)).save(envioCaptor.capture());
        Envio savedEnvio = envioCaptor.getValue();
        
        assertThat(savedEnvio.getPedidoId()).isEqualTo(PEDIDO_ID);
        assertThat(savedEnvio.getEstadoEnvio()).isEqualTo("PENDING");
        assertThat(savedEnvio.getTipoDespacho()).isEqualTo("Standard Delivery");
        assertThat(savedEnvio.getCosto()).isEqualTo(1500.0);
        assertThat(savedEnvio.getTrackingNumber()).isNotNull();
        assertThat(savedEnvio.getTrackingNumber()).startsWith("TRK-");
    }

    @Test
    void givenDispatchedStatus_whenUpdateEstadoEnvio_thenUpdatesStatusAndSendsRabbitMessage() {
        // Arrange
        String newState = "DISPATCHED";
        when(envioRepository.findById(ENVIO_ID)).thenReturn(Optional.of(envioMock));
        when(envioRepository.save(any(Envio.class))).thenReturn(envioMock);

        // Act
        Envio result = envioService.updateEstadoEnvio(ENVIO_ID, newState);

        // Assert
        assertThat(result.getEstadoEnvio()).isEqualTo(newState);
        verify(envioRepository, times(1)).save(envioMock);
        
        // Verifica que se haya enviado el mensaje por RabbitMQ a la cola correcta
        String expectedMessage = String.format("{\"envioId\": %d, \"pedidoId\": %d, \"estado\": \"DISPATCHED\"}", ENVIO_ID, PEDIDO_ID);
        verify(rabbitTemplate, times(1)).convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "despacho.listo", expectedMessage);
    }

    @Test
    void givenNormalStatus_whenUpdateEstadoEnvio_thenUpdatesStatusButNoRabbitMessage() {
        // Arrange
        String newState = "IN_TRANSIT";
        when(envioRepository.findById(ENVIO_ID)).thenReturn(Optional.of(envioMock));
        when(envioRepository.save(any(Envio.class))).thenReturn(envioMock);

        // Act
        Envio result = envioService.updateEstadoEnvio(ENVIO_ID, newState);

        // Assert
        assertThat(result.getEstadoEnvio()).isEqualTo(newState);
        verify(envioRepository, times(1)).save(envioMock);
        // NO debe enviar mensaje a RabbitMQ
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void givenInvalidId_whenUpdateEstadoEnvio_thenThrowsException() {
        // Arrange
        when(envioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> 
            envioService.updateEstadoEnvio(999L, "DISPATCHED")
        ).isInstanceOf(RuntimeException.class)
         .hasMessage("Envio no encontrado");

        verify(envioRepository, never()).save(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void givenExistingPedido_whenCancelEnvioByPedidoId_thenUpdatesStatusToCancelled() {
        // Arrange
        when(envioRepository.findByPedidoId(PEDIDO_ID)).thenReturn(Optional.of(envioMock));

        // Act
        envioService.cancelEnvioByPedidoId(PEDIDO_ID);

        // Assert
        verify(envioRepository, times(1)).save(envioCaptor.capture());
        assertThat(envioCaptor.getValue().getEstadoEnvio()).isEqualTo("CANCELLED");
    }

    @Test
    void givenNonExistingPedido_whenCancelEnvioByPedidoId_thenDoNothing() {
        // Arrange
        when(envioRepository.findByPedidoId(999L)).thenReturn(Optional.empty());

        // Act
        envioService.cancelEnvioByPedidoId(999L);

        // Assert
        verify(envioRepository, never()).save(any());
    }

    @Test
    void givenExistingId_whenDeleteEnvio_thenCancelsAndSendsRabbitMessage() {
        // Arrange
        when(envioRepository.findById(ENVIO_ID)).thenReturn(Optional.of(envioMock));

        // Act
        envioService.deleteEnvio(ENVIO_ID);

        // Assert
        verify(envioRepository, times(1)).save(envioCaptor.capture());
        assertThat(envioCaptor.getValue().getEstadoEnvio()).isEqualTo("CANCELLED");
        
        String expectedMessage = String.format("{\"pedidoId\": %d}", PEDIDO_ID);
        verify(rabbitTemplate, times(1)).convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "envio.cancelado", expectedMessage);
    }
}
