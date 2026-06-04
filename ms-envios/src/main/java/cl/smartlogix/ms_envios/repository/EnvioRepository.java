package cl.smartlogix.ms_envios.repository;

import cl.smartlogix.ms_envios.model.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Long> {
    Optional<Envio> findByPedidoId(Long pedidoId);
}
