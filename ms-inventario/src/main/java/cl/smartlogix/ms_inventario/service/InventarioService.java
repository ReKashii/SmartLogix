package cl.smartlogix.ms_inventario.service;

import cl.smartlogix.ms_inventario.model.Inventario;
import cl.smartlogix.ms_inventario.repository.InventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing inventory business logic.
 */
@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioRepository inventarioRepository;

    public List<Inventario> getAllProducts() {
        return inventarioRepository.findAll();
    }

    public Inventario getProductById(Long id) {
        return inventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    /**
     * Deducts stock for a given product.
     * 
     * @param id Product ID
     * @param quantity Amount to deduct
     * @return Updated product
     * @throws IllegalArgumentException if stock is insufficient
     */
    @Transactional
    public Inventario deductStock(Long id, Integer quantity) {
        Inventario product = getProductById(id);

        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getNombreProducto());
        }

        product.setStock(product.getStock() - quantity);
        return inventarioRepository.save(product);
    }

    @Transactional
    public Inventario saveProduct(Inventario product) {
        return inventarioRepository.save(product);
    }

    @Transactional
    public Inventario updateInventario(Long id, Inventario detallesActualizados) {
        // Reutilizamos tu método getProductById para validar que exista
        Inventario productoExistente = getProductById(id);
        
        // Actualizamos los campos
        productoExistente.setNombreProducto(detallesActualizados.getNombreProducto());
        productoExistente.setStock(detallesActualizados.getStock());
        productoExistente.setPrecio(detallesActualizados.getPrecio());
        
        return inventarioRepository.save(productoExistente);
    }

    /**
     * Deletes a product by its ID.
     */
    @Transactional
    public void deleteInventario(Long id) {
        // Validación de seguridad antes de borrar
        if (!inventarioRepository.existsById(id)) {
            throw new RuntimeException("Cannot delete. Product not found with id: " + id);
        }
        inventarioRepository.deleteById(id);
    }
}
