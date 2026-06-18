package cl.smartlogix.ms_inventario.service;

import cl.smartlogix.ms_inventario.model.Inventario;
import cl.smartlogix.ms_inventario.repository.InventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @InjectMocks
    private InventarioService inventarioService;

    private Inventario product;

    @BeforeEach
    void setUp() {
        product = Inventario.builder()
                .id(1L)
                .nombreProducto("Test Product")
                .stock(10)
                .precio(100.0)
                .build();
    }

    @Test
    void getAllProducts_Success() {
        when(inventarioRepository.findAll()).thenReturn(List.of(product));

        List<Inventario> result = inventarioService.getAllProducts();

        assertThat(result).hasSize(1).contains(product);
        verify(inventarioRepository, times(1)).findAll();
    }

    @Test
    void getProductById_Success() {
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventario result = inventarioService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getNombreProducto()).isEqualTo("Test Product");
        verify(inventarioRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_NotFound_ThrowsException() {
        when(inventarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventarioService.getProductById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found with id: 1");
    }

    @Test
    void deductStock_Success() {
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(i -> i.getArguments()[0]);

        Inventario result = inventarioService.deductStock(1L, 4);

        assertThat(result.getStock()).isEqualTo(6);
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    @Test
    void deductStock_InsufficientStock_ThrowsException() {
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> inventarioService.deductStock(1L, 11))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock");

        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void deductStock_ProductNotFound_ThrowsException() {
        when(inventarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventarioService.deductStock(1L, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found");

        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void restoreStock_Success() {
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(i -> i.getArguments()[0]);

        Inventario result = inventarioService.restoreStock(1L, 5);

        assertThat(result.getStock()).isEqualTo(15);
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    @Test
    void saveProduct_Success() {
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(product);

        Inventario result = inventarioService.saveProduct(product);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(inventarioRepository, times(1)).save(product);
    }

    @Test
    void updateInventario_Success() {
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(i -> i.getArguments()[0]);

        Inventario updateDetails = Inventario.builder()
                .nombreProducto("Updated Name")
                .stock(20)
                .precio(150.0)
                .build();

        Inventario result = inventarioService.updateInventario(1L, updateDetails);

        assertThat(result.getNombreProducto()).isEqualTo("Updated Name");
        assertThat(result.getStock()).isEqualTo(20);
        assertThat(result.getPrecio()).isEqualTo(150.0);
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    @Test
    void deleteInventario_Success() {
        when(inventarioRepository.existsById(1L)).thenReturn(true);
        doNothing().when(inventarioRepository).deleteById(1L);

        inventarioService.deleteInventario(1L);

        verify(inventarioRepository, times(1)).existsById(1L);
        verify(inventarioRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteInventario_NotFound_ThrowsException() {
        when(inventarioRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> inventarioService.deleteInventario(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot delete. Product not found with id: 1");

        verify(inventarioRepository, never()).deleteById(1L);
    }
}
