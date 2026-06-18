package cl.smartlogix.ms_inventario.controller;

import cl.smartlogix.ms_inventario.exception.GlobalExceptionHandler;
import cl.smartlogix.ms_inventario.model.Inventario;
import cl.smartlogix.ms_inventario.service.InventarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventarioController.class)
@Import(GlobalExceptionHandler.class)
class InventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventarioService inventarioService;

    @Autowired
    private ObjectMapper objectMapper;

    private Inventario product;

    @BeforeEach
    void setUp() {
        product = Inventario.builder()
                .id(1L)
                .nombreProducto("Product 1")
                .stock(10)
                .precio(100.0)
                .build();
    }

    @Test
    void getAll_ReturnsOk() throws Exception {
        when(inventarioService.getAllProducts()).thenReturn(Collections.singletonList(product));

        mockMvc.perform(get("/inventario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreProducto").value("Product 1"))
                .andExpect(jsonPath("$[0].stock").value(10));

        verify(inventarioService, times(1)).getAllProducts();
    }

    @Test
    void getById_Success_ReturnsOk() throws Exception {
        when(inventarioService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/inventario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreProducto").value("Product 1"));

        verify(inventarioService, times(1)).getProductById(1L);
    }

    @Test
    void getById_NotFound_ReturnsNotFound() throws Exception {
        when(inventarioService.getProductById(99L)).thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(get("/inventario/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found"));
    }

    @Test
    void create_ReturnsOk() throws Exception {
        when(inventarioService.saveProduct(any(Inventario.class))).thenReturn(product);

        mockMvc.perform(post("/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreProducto").value("Product 1"));
    }

    @Test
    void updateProduct_ReturnsOk() throws Exception {
        when(inventarioService.updateInventario(eq(1L), any(Inventario.class))).thenReturn(product);

        mockMvc.perform(put("/inventario/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreProducto").value("Product 1"));
    }

    @Test
    void deleteProduct_ReturnsNoContent() throws Exception {
        doNothing().when(inventarioService).deleteInventario(1L);

        mockMvc.perform(delete("/inventario/1"))
                .andExpect(status().isNoContent());

        verify(inventarioService, times(1)).deleteInventario(1L);
    }

    @Test
    void deductStock_Success_ReturnsOk() throws Exception {
        when(inventarioService.deductStock(1L, 5)).thenReturn(product);

        mockMvc.perform(post("/inventario/1/deduct")
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreProducto").value("Product 1"));
    }

    @Test
    void deductStock_InsufficientStock_ReturnsBadRequest() throws Exception {
        when(inventarioService.deductStock(1L, 20)).thenThrow(new IllegalArgumentException("Insufficient stock"));

        mockMvc.perform(post("/inventario/1/deduct")
                        .param("quantity", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient stock"));
    }

    @Test
    void restoreStock_Success_ReturnsOk() throws Exception {
        when(inventarioService.restoreStock(1L, 5)).thenReturn(product);

        mockMvc.perform(post("/inventario/1/restore")
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreProducto").value("Product 1"));
    }
}
