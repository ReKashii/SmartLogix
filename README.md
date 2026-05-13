# SmartLogix 

**SmartLogix** es una plataforma orientada a microservicios diseñada para optimizar los procesos logísticos, de inventario y envíos de pequeñas y medianas empresas (PYMEs) de eCommerce. 

En esta fase del proyecto se realiza la migración desde un sistema monolítico hacia un ecosistema escalable de microservicios.

---

## Arquitectura

El proyecto sigue una arquitectura de microservicios aplicando el patrón **Database-per-Service** para asegurar un desacoplamiento total.

### Componentes Principales:
1. **API Gateway / BFF (`/api-gateway`):**
   - **Puerto:** `8080`
   - Punto de entrada para el cliente (Frontend).
   - Gestiona el enrutamiento hacia los microservicios internos y resuelve las políticas de CORS.
   - Implementa un filtro de seguridad global (`JwtAuthenticationFilter`) que exige un token JWT válido (Bearer Token) para proteger los endpoints internos, cumpliendo con los estándares de diseño ético y seguro (EAD).

2. **Microservicio de Inventario (`/ms-inventario`):**
   - **Puerto API:** `8082` | **Puerto DB (PostgreSQL):** `5432`
   - Control de stock de los productos.
   - Expone endpoints REST para consultar y descontar el inventario de manera transaccional.

3. **Microservicio de Pedidos (`/ms-pedidos`):**
   - **Puerto API:** `8081` | **Puerto DB (PostgreSQL):** `5431`
   - Orquestador del proceso de venta.
   - Se comunica de forma **síncrona** con Inventario (vía OpenFeign) para verificar y descontar stock antes de confirmar un pedido.
   - Se comunica de forma **asíncrona** emitiendo un evento (`pedido.creado`) hacia RabbitMQ una vez que la orden se guarda en su base de datos.

4. **Frontend (`/smartlogix-frontend`):**
   - **Puerto:** `5173`
   - Single Page Application (SPA) construida con React 18, Vite y TypeScript.
   - Consume los servicios a través del API Gateway.
   - Posee un interceptor de Axios (`axiosConfig.ts`) que inyecta automáticamente un token JWT para la autorización.

---

## Stack Tecnológico

* **Backend:** Java 17, Spring Boot 3.2.x, Spring Cloud Gateway, Spring Data JPA.
* **Frontend:** React 18, Vite, TypeScript, Axios, React Router DOM.
* **Bases de Datos:** PostgreSQL 15 (Contenerizado).
* **Mensajería / Eventos:** RabbitMQ (Contenerizado).
* **Infraestructura:** Docker & Docker Compose.

---

## Patrones de Diseño Implementados

1. **Repository Pattern (Estructural / Acceso a Datos):**
   - *Ubicación:* En las interfaces `InventarioRepository` y `PedidoRepository`.
   - *Justificación:* Aísla la lógica de negocio de la complejidad del acceso a datos mediante Spring Data JPA. Permite cambiar el motor de base de datos sin afectar los servicios.

2. **Circuit Breaker - Resilience4j (Estabilidad / Resiliencia):**
   - *Ubicación:* En `PedidoService.java` (método `createOrder`).
   - *Justificación:* Protege al MS de Pedidos en caso de que el MS de Inventario falle o se sature. Si la llamada síncrona mediante Feign falla, el circuito se abre y ejecuta un método de contingencia (`fallbackDeductStock`), evitando fallos en cascada en toda la plataforma.

3. **Factory Method (Creacional):**
   - *Ubicación:* `ShippingFactory.java` (en `ms-pedidos`).
   - *Justificación:* Centraliza y encapsula la lógica de creación de los distintos tipos de despacho (Standard, Express, Next Day). Prepara el terreno para el futuro Microservicio de Envíos, haciendo que el sistema sea abierto a la extensión y cerrado a la modificación (Principio Open/Closed).

---

## Pruebas Unitarias y Calidad de Código

El backend incluye pruebas unitarias implementadas con **JUnit 5 y Mockito**, ubicadas en los paquetes `src/test/java/.../service`.
- Se testean los servicios críticos (`InventarioServiceTest` y `PedidoServiceTest`).
- Se validan casos de exito (ej. creación exitosa de pedido) como las excepciones (ej. rechazo de pedido por stock insuficiente o caída del circuito).
- Preparado para métricas de SonarQube (objetivo de cobertura > 60%).
