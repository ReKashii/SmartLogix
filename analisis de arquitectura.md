# Análisis de Arquitectura y Patrones - SmartLogix Fase 2

## 1. Arquitectura y Arquetipos Maven

La plataforma SmartLogix ha sido implementada siguiendo una arquitectura de **Microservicios**, diseñada para resolver los problemas de rigidez y falta de escalabilidad inherentes a los sistemas monolíticos tradicionales. Esta arquitectura se fundamenta en la separación total de responsabilidades mediante la creación de componentes autónomos:

- **BFF (Backend For Frontend) / API Gateway**: Actúa como el punto de entrada único para el frontend en React. Su función es centralizar el enrutamiento, gestionar la seguridad mediante JWT y manejar políticas de CORS, ocultando la topología interna de la red y reduciendo la complejidad del cliente.
- **Microservicio de Pedidos (`ms-pedidos`)**: Orquestador del proceso de venta, encargado de la gestión del ciclo de vida del pedido y la coordinación con otros servicios.
- **Microservicio de Inventario (`ms-inventario`)**: Guardián de la "fuente de verdad" sobre el stock, asegurando la consistencia de los datos de productos y existencias.

Para garantizar la coherencia y mantenibilidad, se ha replicado estrictamente el arquetipo de Maven propuesto, organizando cada microservicio en capas claras:
- **Controller**: Define los endpoints REST y gestiona las peticiones HTTP.
- **Service**: Encapsula la lógica de negocio y las reglas de validación.
- **Repository**: Maneja la persistencia de datos.
- **Model**: Define las entidades JPA y el esquema de datos.

## 2. Patrones de Diseño Implementados

Para alcanzar el nivel de "Muy Buen Desempeño" según la rúbrica, se han implementado tres patrones de diseño fundamentales que resuelven problemas críticos de software:

### A. Repository Pattern
Implementado mediante **Spring Data JPA** en todos los microservicios. Este patrón aísla la lógica de acceso a datos (PostgreSQL) del resto de la aplicación.
- **Problema resuelto**: Evita que la lógica de negocio dependa directamente de las consultas SQL o de la implementación específica de la base de datos.
- **Impacto**: Facilita la mantenibilidad y permite cambiar el motor de persistencia o realizar pruebas unitarias utilizando mocks sin necesidad de una base de datos real.

### B. Circuit Breaker (Resilience4j)
Implementado en `PedidoService` para las llamadas síncronas hacia el microservicio de inventario mediante **OpenFeign**.
- **Problema resuelto**: En una arquitectura de microservicios, si el `ms-inventario` falla o presenta latencias elevadas, el `ms-pedidos` podría agotar sus hilos de ejecución esperando respuestas, provocando un fallo en cascada en todo el sistema.
- **Impacto**: El Circuit Breaker "abre" el circuito tras detectar un umbral de fallos, devolviendo una respuesta de error controlada (fallback) inmediatamente. Esto protege la estabilidad del sistema y permite que el servicio afectado se recupere.

### C. Factory Method
Implementado a través de la clase `ShippingFactory` en el microservicio de pedidos.
- **Problema resuelto**: La creación de diferentes tipos de despacho (Standard, Express, Next Day) posee reglas de negocio distintas. Hardcodear estas reglas en el servicio de pedidos violaría el principio de Responsabilidad Única.
- **Impacto**: Centraliza la lógica de instanciación de métodos de envío. El sistema se vuelve "abierto para la extensión pero cerrado para la modificación", permitiendo añadir nuevos tipos de despacho sin alterar la lógica central de creación de pedidos.

## 3. Pruebas Unitarias y Cobertura

Se ha implementado una suite de pruebas unitarias utilizando **JUnit 5** y **Mockito**, enfocándose en la capa de servicios (`InventarioService` y `PedidoService`).

- **Estrategia de Pruebas**: Se diseñaron casos de prueba exhaustivos que cubren tanto el "camino feliz" (éxito en la creación de pedidos y descuento de stock) como el manejo de excepciones (errores de stock insuficiente, productos no encontrados y fallos de comunicación externa).
- **Objetivo de Cobertura**: El diseño de estas pruebas busca garantizar una cobertura superior al **60%**, cumpliendo con los estándares de calidad exigidos por herramientas como SonarQube, asegurando que la lógica crítica sea robusta y libre de regresiones.

## 4. Ética y Seguridad (EAD)

Siguiendo el marco de **Ethically Aligned Design (EAD)**, la seguridad se ha integrado como un componente estructural y no como un añadido posterior:

- **Validación JWT**: Se implementó un `JwtAuthenticationFilter` en el API Gateway. Este filtro intercepta todas las peticiones entrantes y valida la presencia y firma de un token JWT en el encabezado de autorización.
- **Protección de APIs Internas**: Al centralizar la seguridad en el Gateway, se garantiza que ningún microservicio interno sea expuesto directamente al público, reduciendo la superficie de ataque y asegurando que solo usuarios autenticados puedan interactuar con la lógica de negocio de SmartLogix.
