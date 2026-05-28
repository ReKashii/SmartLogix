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
    // Nuevo endpoint para actualizar un producto existente
    @PutMapping("/{id}")
    public ResponseEntity<Inventario> updateProduct(@PathVariable Long id, @RequestBody Inventario inventario) {
        Inventario updatedProduct = inventarioService.updateInventario(id, inventario);
        return ResponseEntity.ok(updatedProduct);
    }
    // Nuevo endpoint para eliminar un producto por su ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        inventarioService.deleteInventario(id);
        // Devuelve 204 No Content, que es el estándar REST para eliminaciones exitosas
        return ResponseEntity.noContent().build(); 
    }

    /**
     * deduce stock para un producto especifico, utilizado por el microservicio de pedidos para actualizar el inventario cuando se realiza un pedido.
     * maneja excepciones para casos de stock insuficiente o producto no encontrado, devolviendo respuestas HTTP adecuadas.
     */
    @PostMapping("/{id}/deduct")
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