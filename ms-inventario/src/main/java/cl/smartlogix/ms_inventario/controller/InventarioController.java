package cl.smartlogix.ms_inventario.controller;

import cl.smartlogix.ms_inventario.model.Inventario;
import cl.smartlogix.ms_inventario.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * metodos CRUD para el inventario, incluyendo un endpoint para deducir stock, que es utilizado por el microservicio de pedidos.
 */
@RestController
@RequestMapping("/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping
    public ResponseEntity<List<Inventario>> getAll() {
        return ResponseEntity.ok(inventarioService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inventario> getById(@PathVariable Long id) {
        return ResponseEntity.ok(inventarioService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<Inventario> create(@RequestBody Inventario product) {
        return ResponseEntity.ok(inventarioService.saveProduct(product));
    }

    /**
     * deduce stock para un producto especifico, utilizado por el microservicio de pedidos para actualizar el inventario cuando se realiza un pedido.
     * maneja excepciones para casos de stock insuficiente o producto no encontrado, devolviendo respuestas HTTP adecuadas.
     */
    @PatchMapping("/{id}/deduct")
    public ResponseEntity<?> deductStock(@PathVariable Long id, @RequestParam Integer quantity) {
        try {
            Inventario updated = inventarioService.deductStock(id, quantity);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
