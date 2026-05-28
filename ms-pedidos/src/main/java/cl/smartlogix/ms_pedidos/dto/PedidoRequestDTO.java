package cl.smartlogix.ms_pedidos.dto;

import cl.smartlogix.ms_pedidos.factory.ShippingFactory;
import lombok.Data;

@Data
public class PedidoRequestDTO {
    private String cliente;
    private Long productoId;
    private Integer cantidad;
    private Double montoTotal;
    private ShippingFactory.ShippingType tipoDespacho;
}
