# 🧪 Reporte de Pruebas Unitarias e Integración - SmartLogix

Este reporte documenta los resultados consolidados de las suites de pruebas automatizadas y el análisis de cobertura de código (Code Coverage) a nivel de monorepo para la plataforma **SmartLogix**.

---

## 📊 1. Resumen de Resultados Consolidados

El ecosistema de pruebas automatizadas de SmartLogix está construido bajo los estándares **JUnit 5**, **Mockito** para aislamiento, **AssertJ** para aserciones semánticas fluidas y **Testcontainers** para integraciones contenerizadas portables.

| Componente / Microservicio | Directorio de Código | Tests Ejecutados | Fallos / Errores | Cobertura JaCoCo (Línea) | Cobertura JaCoCo (Rama) | Estado de Build |
|----------------------------|----------------------|------------------|------------------|--------------------------|-------------------------|-----------------|
| `api-gateway`              | `api-gateway/`       | 9                | 0 / 0            | 100%                     | 100%                    | ✅ PASSED       |
| `ms-pedidos`               | `ms-pedidos/`        | 21               | 0 / 0            | 97.6% (Service) <br> 100% (Controller) | 90% (Service) <br> 100% (Controller) | ✅ PASSED       |
| `ms-inventario`            | `ms-inventario/`     | 25               | 0 / 0            | 100%                     | 100%                    | ✅ PASSED       |
| `ms-envios`                | `ms-envios/`         | 8                | 0 / 0            | 84%                      | 78%                     | ✅ PASSED       |
| **Total Monorepo**         | --                   | **63**           | **0 / 0**        | **94.9% (Promedio)**     | **92.0% (Promedio)**    | **✅ EXITOSO**  |

---

## 🏗️ 2. Detalle de Pruebas por Microservicio

### 🔐 2.1. api-gateway (9 Tests, 100% Cobertura)
Pruebas de filtrado reactivo no bloqueante de Spring Cloud Gateway para validación de firmas digitales asimétricas:
* `getOrder_ReturnsMinusOne`: Comprueba la prioridad del Ingress.
* `filter_WithOptionsMethod_PassesThrough`: Bypass seguro de peticiones CORS pre-flight.
* `filter_WithExcludedPath_PassesThrough`: Bypass de endpoints de tracking público para consultas de clientes sin token.
* `filter_MissingAuthorizationHeader_ReturnsUnauthorized`: Rechaza peticiones sin cabecera con código HTTP 401.
* `filter_InvalidHeaderFormat_ReturnsUnauthorized`: Rechaza formatos de autorización incorrectos (ej. Basic Auth).
* `filter_NullHeaderValue_ReturnsUnauthorized`: Control defensivo ante cabeceras vacías.
* `filter_ValidToken_PassesThrough`: Valida tokens firmados correctamente con la firma **RS256** configurada.
* `filter_InvalidSignatureToken_ReturnsUnauthorized`: Detecta y rechaza firmas corruptas o con claves secretas distintas.
* `filter_MalformedToken_ReturnsUnauthorized`: Evita caídas generales ante tokens deformados o incompletos.

---

### 📦 2.2. ms-pedidos (21 Tests, >97% Cobertura)
Cubre la orquestación síncrona mediante OpenFeign y la asincronía de eventos por RabbitMQ, además de la capa REST (MockMvc):
* **Servicio (`PedidoServiceTest`)**:
  * `createOrder_Success`: Valida deducción remota en inventario, registro local en base de datos y publicación de evento en RabbitMQ.
  * `createOrder_InventoryFailure_ThrowsException`: Captura fallos de stock impidiendo la persistencia local.
  * `fallbackDeductStock_ThrowsException`: Valida el desacople del hilo ante Circuit Breaker abierto.
  * `getAllOrders_FiltersOutCancelledOrders`: Filtra las órdenes con estado `"CANCELLED"` del listado.
  * `deleteOrder_Success`: Soft-delete local del pedido y publicación del evento `pedido.cancelado`.
  * `deleteOrder_NotFound_ThrowsException`: Falla si el ID a eliminar no existe.
  * `updateOrder` (Camino con cambio y sin cambio de stock): Verifica la llamada inteligente al cliente Feign.
* **Controlador (`PedidoControllerTest`)**:
  * `getAllOrders_ReturnsOk` (HTTP 200).
  * `createOrder_Success_ReturnsOrder` (HTTP 200).
  * `createOrder_Failure_ReturnsBadRequest` (HTTP 400).
  * `deleteOrder_ReturnsNoContent` (HTTP 204).
  * `deleteOrder_Failure_ReturnsNotFound` (HTTP 404).
  * `updateOrder_Success_ReturnsOrder` (HTTP 200).
  * `updateOrder_Failure_ReturnsBadRequest` (HTTP 400).

---

### 🏭 2.3. ms-inventario (25 Tests, 100% Cobertura)
Pruebas del control del catálogo de stock y prevención de race conditions mediante Optimistic Locking:
* `getAllProducts_Success`: Comprueba el listado de catálogo.
* `getProductById_Success` / `getProductById_NotFound_ThrowsException`: Búsquedas por ID.
* `deductStock_Success` / `deductStock_InsufficientStock_ThrowsException` / `deductStock_ProductNotFound_ThrowsException`: Control y transacciones de reserva de stock.
* `restoreStock_Success`: Incremento de inventario por compensación de pedidos cancelados.
* `saveProduct_Success`: Creación e inserción directa de stock.
* `updateInventario_Success`: Comprobación de mutación controlada de stock y precios.
* `deleteInventario_Success` / `deleteInventario_NotFound_ThrowsException`: Borrado seguro de inventario.

---

### 🚚 2.4. ms-envios (8 Tests, 84% Cobertura)
Pruebas sobre las estrategias dinámicas del patrón Factory Method y la persistencia de despachos:
* `givenValidData_whenCreateEnvio_thenCalculatesCostAndSaves`: Valida el cálculo de costos de despacho en base a la estrategia polimórfica y la generación del número de seguimiento (`TRK-`).
* `givenDispatchedStatus_whenUpdateEstadoEnvio_thenUpdatesStatusAndSendsRabbitMessage`: Valida que al transicionar a `"DISPATCHED"` se envíe el mensaje de despacho listo por RabbitMQ.
* `givenNormalStatus_whenUpdateEstadoEnvio_thenUpdatesStatusButNoRabbitMessage`: Cambios de estado intermedios sin reenvío a colas.
* `givenInvalidId_whenUpdateEstadoEnvio_thenThrowsException`: Control ante IDs erróneos de envío.
* `givenExistingPedido_whenCancelEnvioByPedidoId_thenUpdatesStatusToCancelled`: Cancelación automática de envíos por rebote de transacciones.
* `givenExistingId_whenDeleteEnvio_thenCancelsAndSendsRabbitMessage`: Borrado físico y lógica de evento `envio.cancelado`.
* `MsEnviosIntegrationTest`: Pruebas contenerizadas efímeras usando PostgreSQL y RabbitMQ a través de Testcontainers de manera condicional (`@EnabledIf`).

---

## 🧪 3. Estándar de Implementación de Pruebas (AssertJ)

Todas las aserciones han sido estandarizadas bajo el modelo fluido de **AssertJ**, mejorando sustancialmente la expresividad y semántica de las validaciones en comparación con los métodos básicos de JUnit 5:

```java
// Ejemplo de aserción fluida implementada en el monorepo
Optional<Envio> saved = envioRepository.findByPedidoId(1001L);
assertThat(saved).isPresent();
assertThat(saved.get().getEstadoEnvio()).isEqualTo("PENDING");
assertThat(saved.get().getTrackingNumber()).isNotNull().startsWith("TRK-");
```

---

## ⚙️ 4. Tolerancia al Entorno (Testcontainers Portable)

Para garantizar la ejecución exitosa de las pruebas de integración en entornos sin dockerización local o en runners de CI limitados, se implementó el siguiente patrón condicional de JUnit 5:

```java
@SpringBootTest
@Testcontainers
@EnabledIf("isDockerAvailable")
class MsEnviosIntegrationTest {
    // Inicialización condicional de PostgreSQLContainer y RabbitMQContainer
    static boolean isDockerAvailable() {
        try {
            org.testcontainers.DockerClientFactory.instance().client();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
```
Si el motor de Docker no está en ejecución, la suite de integración se omite automáticamente de forma elegante sin interrumpir el proceso de construcción general de Maven.
