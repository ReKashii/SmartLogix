# SmartLogix

![Java 21](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![TypeScript](https://img.shields.io/badge/TypeScript-007ACC?style=for-the-badge&logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![PostgreSQL 15](https://img.shields.io/badge/PostgreSQL-15-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white)

## Resumen del Proyecto

**SmartLogix** es una plataforma logística orientada a maximizar la eficiencia operativa mediante una arquitectura distribuida de nivel empresarial (Enterprise-grade). Diseñado bajo un enfoque de **Diseño Éticamente Alineado**, el sistema garantiza la resiliencia de las operaciones, la privacidad de los datos transaccionales y la mitigación proactiva de amenazas cibernéticas.

El proyecto está estructurado como un monorepo que contiene una arquitectura de microservicios, preparado estratégicamente para un despliegue *Lift & Shift* en instancias de AWS EC2. Este diseño asegura una alta disponibilidad y escalabilidad, manteniendo una segmentación estricta de la red para aislar la lógica de negocio central y los datos sensibles del tráfico público.

## Arquitectura del Sistema

La topología de red de SmartLogix se divide de forma estricta en tres subredes aisladas para garantizar el principio de defensa en profundidad. El flujo de datos inicia a través de un perímetro de seguridad reforzado con un **WAF (Web Application Firewall) basado en Nginx + ModSecurity**, mitigando vectores de ataque como DDoS, Inyecciones SQL (SQLi) y Cross-Site Scripting (XSS).

### 1. Subred Pública (Perímetro y Enrutamiento)
Expuesta al tráfico proveniente del WAF, esta subred maneja la autenticación y la canalización segura de las peticiones.
*   **`api-gateway`**: Actúa como el punto de entrada único. Realiza enrutamiento inteligente, valida tokens de acceso (sin generarlos) y aplica *Rate Limiting* (limitación de tasa) apoyado por Redis Reactivo para prevenir el agotamiento de recursos.
*   **`auth-service`**: Servicio de gestión de identidad centralizado. Emite tokens JWT cifrados asimétricamente mediante el algoritmo RS256. Mantiene una lista de revocación (*Blacklist*) en Redis para invalidar sesiones comprometidas en tiempo real.

### 2. Subred Privada (Lógica de Negocio)
Aislada del exterior, procesa las reglas de negocio del sistema. Los servicios aquí desplegados no son accesibles directamente desde internet.
*   **`orders-service` (Pedidos)**: Orquestador central de las transacciones. Gestiona el ciclo de vida de los pedidos y se comunica con el inventario de manera síncrona vía HTTP, protegiendo las llamadas mediante el patrón *Circuit Breaker* provisto por Resilience4j.
*   **`inventory-service` (Inventario)**: Guardián del stock físico. Servicio altamente cohesionado que encapsula el acceso a los datos de existencias a través del *Repository Pattern*.
*   **`shipping-service` (Envíos)**: Motor de cálculo logístico. Implementa un patrón *Factory Method* para determinar la lógica de despacho óptima, instanciando estrategias como `StandardShipping`, `ExpressShipping` o `SameDayShipping` según los parámetros del pedido.

### 3. Capa de Datos y Mensajería (Subred Aislada)
La capa más profunda, inaccesible excepto por los microservicios autorizados de la Subred Privada.
*   **Persistencia (PostgreSQL 15)**: Implementa de forma rigurosa el patrón *Database-per-Service*, utilizando esquemas separados en la base de datos para asegurar el desacoplamiento a nivel de datos.
*   **Broker de Mensajería (RabbitMQ)**: Eje central de la comunicación asíncrona orientada a eventos. Facilita la coreografía entre servicios emitiendo y consumiendo eventos críticos del dominio como `pedido.creado`, `pago.confirmado` y `despacho.listo`.

## Stack Tecnológico

### Backend (Microservicios)
*   **Lenguaje**: Java 21
*   **Framework Principal**: Spring Boot 3.2.x
*   **Gateway**: Spring Cloud Gateway
*   **Seguridad**: Spring Security, JWT (RS256 Asymmetric Encryption)
*   **Resiliencia**: Resilience4j (Circuit Breaker)

### Frontend (SPA)
*   **Librería/Framework**: React
*   **Lenguaje**: TypeScript
*   **Build Tool**: Vite

### Infraestructura y Datos
*   **Base de Datos Relacional**: PostgreSQL 15
*   **Caché en Memoria y Blacklist**: Redis (con soporte Reactivo)
*   **Mensajería Asíncrona**: RabbitMQ
*   **Firewall Perimetral**: Nginx + ModSecurity (WAF)
*   **Orquestación Local**: Docker Compose

## Patrones de Diseño Clave

La arquitectura de SmartLogix se apoya en patrones de diseño sólidos para la industria del software:

1.  **Microservices Architecture**: Desacoplamiento funcional mediante servicios autónomos orientados a dominios específicos.
2.  **API Gateway**: Punto único de entrada para simplificar la interfaz hacia el cliente y unificar políticas transversales (Rate Limiting, validación de JWT).
3.  **Database-per-Service**: Cada microservicio es dueño de sus propios datos, garantizando una alta disponibilidad y evitando contenciones de base de datos compartida.
4.  **Circuit Breaker (Cortocircuito)**: Implementado en `orders-service` con Resilience4j para prevenir fallos en cascada al comunicarse sincrónicamente con `inventory-service`.
5.  **Event-Driven Architecture (EDA)**: Comunicación asíncrona mediante coreografía de eventos (RabbitMQ) para mantener el bajo acoplamiento entre módulos transaccionales.
6.  **Repository Pattern**: Encapsulación de la lógica de persistencia, observable predominantemente en el `inventory-service`.
7.  **Factory Method**: Utilizado en el `shipping-service` para delegar la creación de la estrategia de despacho (`StandardShipping`, etc.) aislando la complejidad de las reglas de envío.

## Instrucciones de Despliegue Local

El proyecto está diseñado para ser levantado íntegramente en un entorno de desarrollo local mediante la orquestación de contenedores, simulando el aislamiento del entorno de producción.

### Requisitos Previos
*   [Docker Desktop](https://www.docker.com/products/docker-desktop/) o Docker Engine instalado y en ejecución.
*   [Docker Compose](https://docs.docker.com/compose/install/).

### Pasos de Ejecución

1.  Clona el repositorio monorepo de SmartLogix.
2.  Abre una terminal en la raíz del proyecto (donde se ubica el archivo `docker-compose.yml`).
3.  Ejecuta el siguiente comando para construir las imágenes y levantar toda la infraestructura en segundo plano:

```bash
docker-compose up -d --build
```

4.  Para verificar el estado de los contenedores y asegurarse de que los servicios han levantado correctamente:

```bash
docker-compose ps
```

5.  Para detener y eliminar los contenedores, redes y volúmenes generados:

```bash
docker-compose down -v
```
