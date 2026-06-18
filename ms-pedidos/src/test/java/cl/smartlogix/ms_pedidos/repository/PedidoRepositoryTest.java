package cl.smartlogix.ms_pedidos.repository;

import cl.smartlogix.ms_pedidos.model.Pedido;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PedidoRepositoryTest {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Test
    void saveAndFindById_ReturnsSavedPedido() {
        // Arrange
        Pedido pedido = Pedido.builder()
                .cliente("Cliente Test")
                .productoId(10L)
                .cantidad(2)
                .montoTotal(5000.0)
                .tipoDespacho("STANDARD")
                .estado("PENDING")
                .build();

        // Act
        Pedido saved = pedidoRepository.save(pedido);
        Optional<Pedido> found = pedidoRepository.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getCliente()).isEqualTo("Cliente Test");
        assertThat(found.get().getProductoId()).isEqualTo(10L);
        assertThat(found.get().getCantidad()).isEqualTo(2);
        assertThat(found.get().getMontoTotal()).isEqualTo(5000.0);
    }

    @Test
    void deletePedido_RemovesFromDatabase() {
        // Arrange
        Pedido pedido = Pedido.builder()
                .cliente("Cliente Test 2")
                .montoTotal(100.0)
                .tipoDespacho("EXPRESS")
                .estado("COMPLETED")
                .build();

        Pedido saved = pedidoRepository.save(pedido);

        // Act
        pedidoRepository.deleteById(saved.getId());
        Optional<Pedido> found = pedidoRepository.findById(saved.getId());

        // Assert
        assertThat(found).isEmpty();
    }
}
