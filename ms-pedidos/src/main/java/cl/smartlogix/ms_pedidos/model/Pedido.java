package cl.smartlogix.ms_pedidos.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * entidad Pedido representa un pedido realizado por un cliente, con detalles como el cliente, monto total, tipo de despacho y estado del pedido.
 * Esta clase es fundamental para el dominio de pedidos, encapsulando la información relevante
 */
@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cliente;

    @Column
    private Long productoId;

    @Column
    private Integer cantidad;

    @Column(nullable = false)
    private Double montoTotal;

    @Column(nullable = false)
    private String tipoDespacho;

    @Column(nullable = false)
    private String estado; // PENDING, COMPLETED, FAILED
}
