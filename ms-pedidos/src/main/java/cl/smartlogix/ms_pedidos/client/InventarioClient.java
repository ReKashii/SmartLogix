package cl.smartlogix.ms_pedidos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * comunicacion sincrona con el microservicio de inventario para la deduccion de stock, utilizando Feign Client.
 */
@FeignClient(name = "ms-inventario", url = "${services.inventario.url:http://localhost:8082/inventario}")
public interface InventarioClient {

    @PostMapping("/{id}/deduct")
    Object deductStock(@PathVariable("id") Long id, @RequestParam("quantity") Integer quantity);

    @PostMapping("/{id}/restore")
    Object restoreStock(@PathVariable("id") Long id, @RequestParam("quantity") Integer quantity);
}
