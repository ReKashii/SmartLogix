package cl.smartlogix.ms_envios.controller;

import cl.smartlogix.ms_envios.model.Envio;
import cl.smartlogix.ms_envios.service.EnvioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/envios")
@RequiredArgsConstructor
public class EnvioController {

    private final EnvioService envioService;

    @GetMapping
    public ResponseEntity<List<Envio>> getAllEnvios() {
        return ResponseEntity.ok(envioService.getAllEnvios());
    }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<?> getByPedidoId(@PathVariable Long pedidoId) {
        return envioService.getEnvioByPedidoId(pedidoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> updateEstado(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String nuevoEstado = payload.get("estado");
            if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "El estado es requerido"));
            }
            Envio envio = envioService.updateEstadoEnvio(id, nuevoEstado.toUpperCase());
            return ResponseEntity.ok(envio);
        } catch (RuntimeException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Error interno del servidor";
            return ResponseEntity.badRequest().body(Map.of("message", msg));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnvio(@PathVariable Long id) {
        try {
            envioService.deleteEnvio(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
