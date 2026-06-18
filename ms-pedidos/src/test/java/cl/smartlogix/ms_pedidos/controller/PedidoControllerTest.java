package cl.smartlogix.ms_pedidos.controller;

import cl.smartlogix.ms_pedidos.dto.PedidoRequestDTO;
import cl.smartlogix.ms_pedidos.factory.ShippingFactory;
import cl.smartlogix.ms_pedidos.model.Pedido;
import cl.smartlogix.ms_pedidos.service.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllOrders_ReturnsOk() throws Exception {
        // Arrange
        when(pedidoService.getAllOrders()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/pedidos"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(pedidoService, times(1)).getAllOrders();
    }

    @Test
    void createOrder_Success_ReturnsOrder() throws Exception {
        // Arrange
        PedidoRequestDTO dto = new PedidoRequestDTO();
        dto.setCliente("Juan Perez");
        dto.setProductoId(10L);
        dto.setCantidad(5);
        dto.setMontoTotal(10000.0);
        dto.setTipoDespacho(ShippingFactory.ShippingType.STANDARD);

        Pedido saved = Pedido.builder()
                .id(1L)
                .cliente("Juan Perez")
                .productoId(10L)
                .cantidad(5)
                .montoTotal(10000.0)
                .tipoDespacho("Standard Shipping")
                .estado("COMPLETED")
                .build();

        when(pedidoService.createOrder(anyString(), anyLong(), anyInt(), anyDouble(), any(ShippingFactory.ShippingType.class)))
                .thenReturn(saved);

        // Act & Assert
        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cliente").value("Juan Perez"))
                .andExpect(jsonPath("$.estado").value("COMPLETED"));
    }

    @Test
    void deleteOrder_ReturnsNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/pedidos/1"))
                .andExpect(status().isNoContent());

        verify(pedidoService, times(1)).deleteOrder(1L);
    }
}
