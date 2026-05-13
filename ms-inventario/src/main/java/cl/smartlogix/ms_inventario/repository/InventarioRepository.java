package cl.smartlogix.ms_inventario.repository;

import cl.smartlogix.ms_inventario.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * me faltan metodos personalizados para consultas complejas lmao
 * Este repositorio se encarga de la persistencia de los productos en la base de datos
 */
@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    Optional<Inventario> findByNombreProducto(String nombreProducto);
}
