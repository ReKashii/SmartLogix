package cl.smartlogix.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * entradal punto de la aplicación API Gateway, responsable de enrutar las solicitudes a los microservicios correspondientes y aplicar filtros de seguridad.
 */
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
