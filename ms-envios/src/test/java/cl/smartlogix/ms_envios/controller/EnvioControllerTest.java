package cl.smartlogix.ms_envios.controller;

import cl.smartlogix.ms_envios.model.Envio;
import cl.smartlogix.ms_envios.service.EnvioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnvioController.class)
class EnvioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnvioService envioService;

    @Autowired
    private ObjectMapper objectMapper;

    private Envio envio;

    @BeforeEach
    void setUp() {
        envio = Envio.builder()
                .id(1L)
                .pedidoId(100L)
                .tipoDespacho("STANDARD")
                .estadoEnvio("PENDING")
                .costo(1500.0)
                .fechaEstimadaEntrega(LocalDate.now().plusDays(3))
                .trackingNumber("TRK-12345")
                .build();
    }

    @Test
    void getAllEnvios_ReturnsOk() throws Exception {
        when(envioService.getAllEnvios()).thenReturn(Collections.singletonList(envio));

        mockMvc.perform(get("/envios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pedidoId").value(100L))
                .andExpect(jsonPath("$[0].estadoEnvio").value("PENDING"));

        verify(envioService, times(1)).getAllEnvios();
    }

    @Test
    void getByPedidoId_Found_ReturnsOk() throws Exception {
        when(envioService.getEnvioByPedidoId(100L)).thenReturn(Optional.of(envio));

        mockMvc.perform(get("/envios/pedido/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoId").value(100L));

        verify(envioService, times(1)).getEnvioByPedidoId(100L);
    }

    @Test
    void getByPedidoId_NotFound_ReturnsNotFound() throws Exception {
        when(envioService.getEnvioByPedidoId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/envios/pedido/999"))
                .andExpect(status().isNotFound());

        verify(envioService, times(1)).getEnvioByPedidoId(999L);
    }

    @Test
    void updateEstado_Success_ReturnsOk() throws Exception {
        when(envioService.updateEstadoEnvio(1L, "DISPATCHED")).thenReturn(envio);

        Map<String, String> payload = new HashMap<>();
        payload.put("estado", "DISPATCHED");

        mockMvc.perform(put("/envios/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        verify(envioService, times(1)).updateEstadoEnvio(1L, "DISPATCHED");
    }

    @Test
    void updateEstado_MissingEstado_ReturnsBadRequest() throws Exception {
        Map<String, String> payload = new HashMap<>();

        mockMvc.perform(put("/envios/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El estado es requerido"));

        verify(envioService, never()).updateEstadoEnvio(anyLong(), anyString());
    }

    @Test
    void updateEstado_ExceptionThrown_ReturnsBadRequest() throws Exception {
        when(envioService.updateEstadoEnvio(1L, "DISPATCHED")).thenThrow(new RuntimeException("Envio no encontrado"));

        Map<String, String> payload = new HashMap<>();
        payload.put("estado", "DISPATCHED");

        mockMvc.perform(put("/envios/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Envio no encontrado"));
    }

    @Test
    void deleteEnvio_Success_ReturnsNoContent() throws Exception {
        doNothing().when(envioService).deleteEnvio(1L);

        mockMvc.perform(delete("/envios/1"))
                .andExpect(status().isNoContent());

        verify(envioService, times(1)).deleteEnvio(1L);
    }

    @Test
    void deleteEnvio_NotFound_ReturnsNotFound() throws Exception {
        doThrow(new RuntimeException("Envio no encontrado")).when(envioService).deleteEnvio(999L);

        mockMvc.perform(delete("/envios/999"))
                .andExpect(status().isNotFound());
    }
}
