# 1. Descripción General (Re-Arquitectura SofkianOS)

Este análisis examina la estructura actual del proyecto **SofkianOS MVP**.  
Aunque el sistema se presenta como una arquitectura de microservicios (Producer API, Consumer Worker y Frontend), técnicamente opera como un **Monolito Distribuido**. Esto significa que las piezas están separadas físicamente, pero acopladas lógicamente, arrastrando una deuda técnica que impide su escalabilidad.

El objetivo de esta fase de re-arquitectura es documentar estos *dolores* y contrastarlos con los beneficios de migrar hacia una **Clean Architecture**.

---

## 1.1. Dolores del Monolito Heredado(Estado Actual)

Basado en el inventario de deuda técnica, los principales obstáculos identificados son:

### 🔴 Acoplamiento de Datos y Lógica
- Credenciales de base de datos *hardcoded* en `application.properties`.
- Duplicación manual de la entidad `KudoEvent` entre el Producer y el Consumer.
- Ausencia de versionamiento de contratos: un cambio en el Producer **rompe inmediatamente** al Consumer.

### 🔴 Lógica Mezclada en la Infraestructura
- La lógica de negocio está dispersa y mezclada dentro de:
    - Controllers de la API.
    - Métodos de escucha de RabbitMQ.
- Esto dificulta extraer o evolucionar reglas de negocio sin afectar el transporte de datos.

### 🔴 Falta de Testabilidad Real
- Cobertura limitada (~25%), enfocada mayormente en controllers.
- Imposibilidad de testear la lógica de asignación de Kudos sin levantar toda la infraestructura (PostgreSQL / RabbitMQ).
- Causa raíz: ausencia de **Inversión de Dependencias**.

### 🔴 Inseguridad y Fragilidad
- API pública sin:
    - Gateways.
    - Políticas de autenticación/autorización.
- Falta de idempotencia en el procesamiento de mensajes, generando duplicados que comprometen la integridad de los datos.

### 🔴 Código Muerto y Duplicidad en Frontend
- ~55% de código inaccesible.
- Capas de API paralelas que confunden el flujo de datos y aumentan el costo de mantenimiento.

### 🔴 Observabilidad Nula
- Dependencia exclusiva de logs de consola.
- Ausencia de *tracing distribuido*, haciendo imposible rastrear un Kudo desde el Producer hasta su persistencia en la base de datos.

---

## 1.2. Beneficios Teóricos de Clean Architecture (Estado Deseado)

La implementación de **Clean Architecture** (basada en los principios de Robert C. Martin) permitirá desacoplar el *Core* del negocio de los detalles técnicos (Frameworks, UI, DB):

### 🟢 Independencia de Frameworks y API
- La API REST deja de ser el centro del sistema y pasa a ser un **Interface Adapter**.
- La lógica de Kudos puede sobrevivir incluso a un cambio de framework (ej. reemplazar Spring Boot).

### 🟢 Centralización en Casos de Uso (Use Cases)
- Toda la regla de negocio (ej. *“Cómo se valida un Kudo”*) reside en una capa central protegida.
- Se elimina la duplicidad de lógica entre Producer y Consumer.

### 🟢 Testabilidad Superior (Mocking de Infraestructura)
- Dependencias inyectadas mediante interfaces.
- Tests unitarios del Core sin bases de datos ni brokers reales.
- Objetivo: elevar la cobertura a **>80%**.

### 🟢 Mantenibilidad y Evolución
- Separación clara en capas:
    - Entities
    - Use Cases
    - Adapters
- Frontend y Backend trabajan sobre **contratos explícitos**, reduciendo la deuda técnica exponencial.

### 🟢 Resiliencia Mediante Capas de Adaptación
- Implementación de:
    - Circuit Breakers.
    - Validaciones de invariantes en los Use Cases.
- Garantiza que solo datos válidos alcancen la capa de persistencia.

---

## 1.3. Contraste y Conclusión

Mientras el modelo actual de **Monolito Distribuido** acumula deuda técnica con cada mensaje enviado (riesgo de duplicados, falta de trazabilidad), **Clean Architecture** previene esta degradación mediante el principio de **Inversión de Dependencias (DIP)**.

La transición permitirá que **SofkianOS** pase de ser un sistema frágil a una **plataforma robusta**, preparada para integrarse a un ecosistema de APIs escalable.

---

# 2. Documentacion Producer API

**Versión**: 1.0.0  
**Última Actualización**: 2026-02-25  
**Estado**: Validado y Documentado  

---

## 2.1. Introducción Estratégica

La **Producer API** es el componente responsable de recibir solicitudes de creación de kudos desde clientes, validarlas, persistirlas e integrarlas al sistema de mensajería asincrónica.

Este documento formaliza el contrato del API, incluyendo:
- Semántica HTTP y códigos de estado
- Especificación formal de endpoints
- Esquemas de request/response
- Validaciones de entrada

---

## 2.2. Códigos de Estado HTTP

| Código | Significado | Escenario |
|--------|-------------|----------|
| **200** | OK | GET exitoso, resultados encontrados o no |
| **202** | Accepted | POST aceptado para procesamiento asincrónico |
| **400** | Bad Request | Validación fallida, parámetro inválido |
| **404** | Not Found | Recurso no encontrado |
| **503** | Service Unavailable | Broker de mensajes no disponible |
| **500** | Internal Server Error | Error interno no esperado |

---

## 2.3. Manejo de Errores

Todas las respuestas de error siguen la estructura `ApiError` estandarizada:

```json
{
  "timestamp": "2026-02-25T14:35:22.123Z",
  "status": 400,
  "reason": "Bad Request",
  "message": "Validation failed: from: must be a valid email, category: Invalid category",
  "path": "/api/v1/kudos"
}
```

### Mapeo de Excepciones → HTTP Status

| Excepción | HTTP Status | Escenario |
|-----------|-------------|----------|
| `MethodArgumentNotValidException` | **400** | Campo faltante, email inválido, mensaje muy corto |
| `MethodArgumentTypeMismatchException` | **400** | Query param `page=abc` cuando debe ser int |
| `MissingServletRequestParameterException` | **400** | Parámetro requerido no enviado |
| `ResourceNotFoundException` | **404** | Búsqueda sin resultados |
| `KudoPublishingException` | **503** | AMQP Exception, RabbitMQ caído |
| `Exception` (genérica) | **500** | Error no capturado |

---

## 2.4. Contrato REST Formal

### 2.4.1 Endpoint: Crear Kudo

| Propiedad | Valor |
|-----------|-------|
| **Método** | `POST` |
| **Ruta** | `/api/v1/kudos` |
| **Autenticación** | Ninguna (MVP) |
| **Content-Type** | `application/json` |

#### Headers de Solicitud

```http
POST /api/v1/kudos HTTP/1.1
Host: producer-api:8080
Content-Type: application/json
Accept: application/json
```

#### Body de Solicitud

```json
{
  "from": "alice@sofkianos.com",
  "to": "bob@sofkianos.com",
  "category": "Innovation",
  "message": "Excelente trabajo en la refactorización de la API, mejoraste mucho la legibilidad"
}
```

**Esquema (JSON Schema)**:

```json
{
  "type": "object",
  "required": ["from", "to", "category", "message"],
  "properties": {
    "from": {
      "type": "string",
      "format": "email",
      "description": "Email del emisor"
    },
    "to": {
      "type": "string",
      "format": "email",
      "description": "Email del receptor"
    },
    "category": {
      "type": "string",
      "enum": ["Innovation", "Teamwork", "Passion", "Mastery"],
      "description": "Categoría del kudo"
    },
    "message": {
      "type": "string",
      "minLength": 10,
      "maxLength": 500,
      "description": "Mensaje del kudo"
    }
  }
}
```

#### Respuestas

**202 Accepted** (Exitoso)

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Kudo queued successfully",
  "status": "ACCEPTED",
  "timestamp": "2026-02-25T14:30:00Z"
}
```

**400 Bad Request** (Validación fallida)

```json
{
  "timestamp": "2026-02-25T14:31:00Z",
  "status": 400,
  "reason": "Bad Request",
  "message": "Validation failed: from: must be a valid email address, message: must be between 10 and 500 characters",
  "path": "/api/v1/kudos"
}
```

**503 Service Unavailable** (Broker caído)

```json
{
  "timestamp": "2026-02-25T14:32:00Z",
  "status": 503,
  "reason": "Service Unavailable",
  "message": "Error publishing KudoEvent to message broker",
  "path": "/api/v1/kudos"
}
```

---

### 2.4.2 Endpoint: Listar Kudos

| Propiedad | Valor |
|-----------|-------|
| **Método** | `GET` |
| **Ruta** | `/api/v1/kudos` |
| **Autenticación** | Ninguna (MVP) |
| **Accept** | `application/json` |

#### Query Parameters

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `page` | integer | No | 0 | Número de página (0-based) |
| `size` | integer | No | 20 | Elementos por página, máx 50 |
| `sortDirection` | string | No | DESC | ASC o DESC |
| `category` | string | No | — | Filtrar por categoría |
| `searchText` | string | No | — | Búsqueda libre en mensaje/emisor/receptor |

#### Solicitud Ejemplo

```http
GET /api/v1/kudos?page=0&size=10&sortDirection=DESC&category=Innovation&searchText=API HTTP/1.1
Host: producer-api:8080
Accept: application/json
```

#### Respuesta Exitosa: 200 OK

```json
{
  "content": [
    {
      "receptor": "B***",
      "emisor": "A***",
      "mensaje": "Great work on the API refactor, much cleaner now",
      "fecha": "2026-02-25T13:45:00Z",
      "categoria": "Innovation"
    },
    {
      "receptor": "C***",
      "emisor": "D***",
      "mensaje": "Amazing teamwork during the sprint review",
      "fecha": "2026-02-25T11:20:00Z",
      "categoria": "Teamwork"
    }
  ],
  "totalElements": 127,
  "totalPages": 13,
  "number": 0,
  "size": 10
}
```

#### Respuestas de Error

**400 Bad Request** (Parámetro inválido)

```json
{
  "timestamp": "2026-02-25T14:33:00Z",
  "status": 400,
  "reason": "Bad Request",
  "message": "Invalid value for parameter 'page': abc",
  "path": "/api/v1/kudos"
}
```

---

### 2.4.3 Endpoints de Health Check

#### GET /

```http
GET / HTTP/1.1

HTTP/1.1 200 OK
{
  "service": "producer-api",
  "status": "UP"
}
```

#### GET /health

```http
GET /health HTTP/1.1

HTTP/1.1 200 OK
{
  "service": "producer-api",
  "status": "UP"
}
```

---

## 2.5. Protección de Datos Sensibles

Los emails en respuestas de búsqueda están enmascarados por privacidad:

- `alice@sofkianos.com` → `A***`
- `bob@sofkianos.com` → `B***`

---

**Autor**: Technical Team  
**Última Actualización**: 2026-02-25
