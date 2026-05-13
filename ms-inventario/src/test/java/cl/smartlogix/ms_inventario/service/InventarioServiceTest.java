package cl.smartlogix.ms_inventario.service;

import cl.smartlogix.ms_inventario.model.Inventario;
import cl.smartlogix.ms_inventario.repository.InventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
    void deductStock_Success() {
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(i -> i.getArguments()[0]);

        Inventario result = inventarioService.deductStock(1L, 4);

        assertEquals(6, result.getStock());
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    @Test
    void deductStock_InsufficientStock_ThrowsException() {
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(product));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inventarioService.deductStock(1L, 11);
        });

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void deductStock_ProductNotFound_ThrowsException() {
        when(inventarioRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventarioService.deductStock(1L, 1);
        });

        assertTrue(exception.getMessage().contains("Product not found"));
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }
}
