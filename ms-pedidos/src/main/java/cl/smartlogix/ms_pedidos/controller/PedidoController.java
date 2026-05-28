package cl.smartlogix.ms_pedidos.controller;

import cl.smartlogix.ms_pedidos.factory.ShippingFactory;
import cl.smartlogix.ms_pedidos.model.Pedido;
import cl.smartlogix.ms_pedidos.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


import cl.smartlogix.ms_pedidos.dto.PedidoRequestDTO;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @GetMapping
    public ResponseEntity<List<Pedido>> getAll() {
        return ResponseEntity.ok(pedidoService.getAllOrders());
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody PedidoRequestDTO request) {
        try {
            Pedido pedido = pedidoService.createOrder(
                    request.getCliente(),
                    request.getProductoId(),
                    request.getCantidad(),
                    request.getMontoTotal(),
                    request.getTipoDespacho()
            );
            return ResponseEntity.ok(pedido);
        } catch (RuntimeException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Error interno del servidor";
            return ResponseEntity.badRequest().body(java.util.Map.of("message", msg));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        try {
            pedidoService.deleteOrder(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody PedidoRequestDTO request) {
        try {
            Pedido pedido = pedidoService.updateOrder(
                    id,
                    request.getCliente(),
                    request.getProductoId(),
                    request.getCantidad(),
                    request.getMontoTotal(),
                    request.getTipoDespacho()
            );
            return ResponseEntity.ok(pedido);
        } catch (RuntimeException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Error interno del servidor";
            return ResponseEntity.badRequest().body(java.util.Map.of("message", msg));
        }
    }
}
