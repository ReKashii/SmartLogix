package cl.smartlogix.ms_pedidos.controller;

import cl.smartlogix.ms_pedidos.factory.ShippingFactory;
import cl.smartlogix.ms_pedidos.model.Pedido;
import cl.smartlogix.ms_pedidos.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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
    public ResponseEntity<?> createOrder(
            @RequestParam String cliente,
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestParam Double amount,
            @RequestParam ShippingFactory.ShippingType shipType) {
        try {
            Pedido pedido = pedidoService.createOrder(cliente, productId, quantity, amount, shipType);
            return ResponseEntity.ok(pedido);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
