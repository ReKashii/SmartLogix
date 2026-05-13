package cl.smartlogix.ms_pedidos.repository;

import cl.smartlogix.ms_pedidos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * falta implementacion de metodos personalizados para consultas complejas, si es necesario.
 * Este repositorio se encarga de la persistencia de los pedidos en la base de datos,
 * permitiendo realizar operaciones CRUD y consultas específicas relacionadas con los pedidos
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
