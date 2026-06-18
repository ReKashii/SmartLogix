package cl.smartlogix.ms_envios.repository;

import cl.smartlogix.ms_envios.model.Envio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EnvioRepositoryTest {

    @Autowired
    private EnvioRepository envioRepository;

    @Test
    void saveAndFindByPedidoId_ReturnsEnvio() {
        // Arrange
        Envio envio = Envio.builder()
                .pedidoId(456L)
                .tipoDespacho("Priority Shipping")
                .estadoEnvio("PENDING")
                .costo(3000.0)
                .fechaEstimadaEntrega(LocalDate.now().plusDays(1))
                .trackingNumber("TRK-456")
                .build();

        // Act
        envioRepository.save(envio);
        Optional<Envio> found = envioRepository.findByPedidoId(456L);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getPedidoId()).isEqualTo(456L);
        assertThat(found.get().getTrackingNumber()).isEqualTo("TRK-456");
    }

    @Test
    void findByPedidoId_NotFound_ReturnsEmpty() {
        // Act
        Optional<Envio> found = envioRepository.findByPedidoId(9999L);

        // Assert
        assertThat(found).isEmpty();
    }
}
