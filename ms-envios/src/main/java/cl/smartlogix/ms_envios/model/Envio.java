package cl.smartlogix.ms_envios.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "envios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long pedidoId;

    @Column(nullable = false)
    private String tipoDespacho;

    @Column(nullable = false)
    private String estadoEnvio; // PENDING, DISPATCHED, DELIVERED

    @Column(nullable = false)
    private Double costo;

    @Column(nullable = false)
    private LocalDate fechaEstimadaEntrega;

    @Column(unique = true)
    private String trackingNumber;
}
