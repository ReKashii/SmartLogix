package cl.smartlogix.ms_inventario.repository;

import cl.smartlogix.ms_inventario.model.Inventario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class InventarioRepositoryTest {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Test
    void saveAndFindByNombreProducto_ReturnsProduct() {
        // Arrange
        Inventario product = Inventario.builder()
                .nombreProducto("Laptop Gamer")
                .stock(15)
                .precio(850000.0)
                .build();

        // Act
        inventarioRepository.save(product);
        Optional<Inventario> found = inventarioRepository.findByNombreProducto("Laptop Gamer");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getNombreProducto()).isEqualTo("Laptop Gamer");
        assertThat(found.get().getStock()).isEqualTo(15);
        assertThat(found.get().getPrecio()).isEqualTo(850000.0);
    }

    @Test
    void findByNombreProducto_NotFound_ReturnsEmpty() {
        // Act
        Optional<Inventario> found = inventarioRepository.findByNombreProducto("Non Existing Product");

        // Assert
        assertThat(found).isEmpty();
    }
}
