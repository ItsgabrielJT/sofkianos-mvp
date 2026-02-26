# 🧠 IRIS Analysis: Public Kudos Listing Feature
 
**Funcionalidad**: Ruta pública para visualizar tabla de Kudos registrados  
**Fecha de Análisis**: 19 de febrero de 2026  
**Versión Documento**: 1.0  

---

## Executive Summary

El análisis IRIS completo para la implementación de funcionalidad de **consulta pública de Kudos** ha sido completado en 4 fases estructuradas. El sistema requiere:

- ✅ Nuevo endpoint REST `GET /api/v1/kudos` en Producer API con acceso read-only a PostgreSQL
- ✅ Capacidades de filtrado avanzado: categoría, búsqueda texto, rango fechas, ordenamiento
- ✅ Protección de privacidad: enmascaramiento de emails mediante hashing
- ✅ Paginación optimizada: 20 items/página con metadata
- ✅ Nueva página en React (`/kudos/list`) con tabla interactiva y componentes de filtro
- ✅ Cobertura de tests: >75% backend, >70% frontend

---

# 📋 ÍNDICE

1. [Fase 1: Procesamiento y Extracción de Contexto](#fase-1)
2. [Fase 2: Análisis y Síntesis](#fase-2)
3. [Fase 3: Listado de Entregables](#fase-3)
4. [Fase 4: Ambigüedades Resueltas](#fase-4)
5. [Recomendaciones Arquitectónicas](#recomendaciones)
6. [Roadmap de Implementación](#roadmap)
7. [Comparativa de Modelos IA](#comparativa)

---

<a name="fase-1"></a>

# 🔍 FASE 1 — Procesamiento y Extracción de Contexto

## 1.1 Análisis del Requerimiento

### Descripción General

El usuario solicita implementar una nueva funcionalidad de **visualización pública de Kudos** en el sistema SofkianOS MVP. Actualmente el sistema solo permite **crear** kudos (Producer API) y **almacenarlos** (Consumer Worker), pero no existe forma de **consultar** o **listar** los kudos registrados.

### Resumen Ejecutivo Preliminar

Se requiere implementar una nueva capacidad de **lectura** (query) en el sistema Event-Driven actual, agregando:

- **Backend**: Nuevo endpoint GET en Producer API para consultar kudos persistidos en PostgreSQL
- **Frontend**: Nueva página/ruta pública con tabla de visualización y capacidades de filtrado
- **Integración**: Nuevo método en el servicio del frontend para consumir el endpoint de listado

### Propósito del Proyecto

Cerrar el ciclo de valor del sistema de Kudos permitiendo a los usuarios **visualizar** los reconocimientos otorgados, incrementando la transparencia organizacional y reforzando la cultura de reconocimiento.

### Objetivo Principal

Implementar funcionalidad completa de consulta pública de kudos con capacidades de filtrado avanzado, manteniendo los principios arquitectónicos de Clean Architecture y SOLID del proyecto.

### Objetivos Secundarios

1. Mantener consistencia arquitectónica con patrones existentes (Hexagonal/Layered)
2. Implementar paginación robusta para optimizar rendimiento con grandes volúmenes
3. Agregar filtros útiles sin complejidad excesiva en UX
4. Garantizar seguridad y privacidad evitando exposición de datos sensibles
5. Mantener experiencia de usuario fluida en frontend
6. Implementar tests unitarios e integración exhaustivos
7. Proporcionar documentación técnica completa (OpenAPI/Swagger)

### Descripción del Alcance

#### ✅ Incluye:
- Nuevo endpoint REST `GET /api/v1/kudos` en Producer API
- DTOs de respuesta con hashing de emails para privacidad
- Service layer con lógica de filtrado, búsqueda y ordenamiento
- Repository queries optimizadas con Full-Text Search
- Configuración de acceso read-only a PostgreSQL
- Nueva página React con tabla interactiva de kudos
- Componentes reutilizables: KudoTable, KudoFilters, KudoPagination
- Integración de nuevas rutas en App.tsx
- Tests unitarios backend (JUnit 5 + Mockito)
- Tests unitarios frontend (Vitest + React Testing Library)
- Tests de integración con PostgreSQL
- Documentación OpenAPI/Swagger completa
- Índices de base de datos optimizados

#### ❌ No incluye:
- Autenticación/Autorización del endpoint (fase posterior)
- Edición, eliminación o modificación de kudos
- Estadísticas o analytics avanzados
- Exportación de datos (descartado en Fase 4)
- Notificaciones en tiempo real
- Sincronización con otros sistemas

### Módulos Impactados

#### Producer API (Backend)
- `controller` package → nuevo `KudosQueryController`
- `service` package → nuevas interfaz `KudoQueryService` e implementación
- `repository` package → nuevo `KudoQueryRepository` con Specifications
- `dto` package → 4 nuevos DTOs (SearchCriteria, PagedResponse, ListItem, etc.)
- `entity` package → nueva entidad `Kudo` (réplica para lectura)
- `util` package → `EmailMaskingUtil` para hashing de privacidad
- `config` package → `DatabaseConfig` para DataSource read-only
- `specification` package → builders dinámicos de criterios

#### Frontend
- `pages/` → nuevo `KudosListPage.tsx`
- `components/` → nuevos `KudoTable.tsx`, `KudoFilters.tsx`, `KudoPagination.tsx`
- `services/api/` → extensión de `kudosService.ts` con método `list()`
- `hooks/` → posible hook `useKudosList()` para lógica de listado
- `App.tsx` → nueva ruta pública `/kudos/list`
- `Navbar.tsx` → nuevo link de navegación hacia listado

#### Integración
- Frontend ↔ Producer API (nuevas llamadas GET)
- Producer API ↔ PostgreSQL (nuevas conexiones read-only)
- CORS ya configurado (no requiere cambios)

### Integraciones Identificadas

1. **Frontend → Producer API**: Nuevo endpoint `GET /api/v1/kudos?category=...&search=...&page=...`
2. **Producer API → PostgreSQL**: Conexión read-only con connection pool dedicado
3. **Producer API → RabbitMQ**: NO requiere (solo lectura)
4. **Consumer Worker**: Permanece sin cambios
5. **CORS**: Configuración existente en `WebConfig.java` cubre GET requests

### Supuestos Técnicos

1. La base de datos PostgreSQL de Supabase está accesible desde internet
2. El volumen de kudos no excede 100,000 registros en el corto plazo (~6 meses)
3. Los datos actuales en `fromUser` y `toUser` son direcciones de correo válidas
4. La tabla `kudos` existe y tiene datos persistidos desde Consumer Worker
5. Los índices en `createdAt` existen o pueden ser creados sin downtime
6. El Consumer Worker ha poblado significativamente la tabla de kudos
7. Las credenciales de base de datos pueden almacenarse en `application.properties` o variables entorno
8. Los connection pools estándar (HikariCP) pueden manejar la carga esperada

### Restricciones Técnicas

| Restricción | Justificación |
|------------|---------------|
| Mantener estricta separación de capas (Controller → Service → Repository) | Principios Clean Architecture |
| No exponer IDs internos de base de datos (Long) sin transformación | Seguridad (information disclosure) |
| No exponer emails completos, solo enmascarados | Privacidad GDPR/LGPD |
| Implementar paginación obligatoria (máximo 50 registros por request) | Protección contra abuse DoS |
| No modificar entidad JPA `Kudo` existente en Consumer Worker | Mantener aislamiento de servicios |
| No agregar dependencias heavy (ElasticSearch, Redis, etc.) | MVP pragmático |
| Mantener compatibilidad con versión actual del Producer API | Cero breaking changes |
| Usar DTOs como `record` o clases inmutables | Principio de inmutabilidad |

### Riesgos Iniciales

| ID | Riesgo | Probabilidad | Impacto | Nivel | Mitigación |
|----|--------|--------------|---------|-------|-----------|
| R-001 | Exposición de datos sensibles por falta de DTO/hashing adecuado | Alta | Crítico | **CRÍTICO** | Tests exhaustivos de enmascaramiento, code review obligatorio |
| R-002 | Performance degradada sin índices PostgreSQL | Alta | Alto | **ALTO** | Crear índices preemptively, usar EXPLAIN ANALYZE |
| R-003 | CORS no configurado bloquea requests desde frontend | Media | Medio | **MEDIO** | Verificar configuración WebConfig.addCorsMappings, test en postman |
| R-004 | Consultas N+1 si no se optimiza repository | Baja | Medio | **MEDIO** | Usar SELECT específico, no FetchType.LAZY |
| R-005 | Connection pool exhaustion bajo carga | Media | Alto | **ALTO** | Configurar HikariCP con limits: max=10, min=5 |
| R-006 | Inconsistencias UI por manejo inadecuado de estados vacíos | Media | Bajo | **BAJO** | Implementar loading/empty states, tests de componentes |
| R-007 | Búsqueda de texto sin índices full-text es lenta | Media | Medio | **MEDIO** | Implementar Full-Text Search como decidido en Fase 4 |
| R-008 | Rate limiting no implementado permite scraping de datos | Media | Medio | **MEDIO** | Implementar 100 req/min por IP según Fase 4 |

---

<a name="fase-2"></a>

# 🧩 FASE 2 — Análisis y Síntesis del Contexto

## 2.1 Requerimientos del Usuario

**REQ-001**: Como usuario anónimo, quiero ver una lista pública de todos los kudos registrados para conocer los reconocimientos otorgados en la organización.

**REQ-002**: Como usuario anónimo, quiero filtrar kudos por categoría, remitente, destinatario e identificador de kudos para encontrar reconocimientos específicos rápidamente.

**REQ-003**: Como usuario preocupado por privacidad, quiero que los emails estén parcialmente ocultos (hasheados) para proteger información personal mientras mantengo contexto.

**REQ-004**: Como usuario final, quiero cambiar el orden de la lista (más reciente/más antiguo) para ajustar mi preferencia de visualización según necesidad.

**REQ-005**: Como usuario de baja velocidad de conexión, quiero que la lista sea paginada para que cargue rápido incluso con miles de kudos registrados.

---

## 2.2 Requerimientos Funcionales

**FUNC-001** (REQ-001, REQ-005)  
El Producer API debe exponer endpoint `GET /api/v1/kudos` retornando lista paginada de kudos ordenados por fecha de creación descendente por defecto.

**FUNC-002** (REQ-001)  
El endpoint debe retornar DTOs con: id (hasheado), fromUser (enmascarado), toUser (enmascarado), category, message, createdAt (ISO 8601).

**FUNC-003** (REQ-002)  
El endpoint debe aceptar query parameters opcionales:
- `category`: enum {Innovation, Teamwork, Passion, Mastery}
- `searchText`: búsqueda full-text en fromUser, toUser, message
- `startDate`: ISO 8601
- `endDate`: ISO 8601
- `page`: número de página, default 0
- `size`: elementos por página, default 20, max 50
- `sortDirection`: ASC o DESC, default DESC

**FUNC-004** (REQ-003)  
El backend debe implementar hashing de emails: `juan.perez@domain.com` → `j***z@domain.com` (basado en hash, consistente por request mediante salt local).

**FUNC-005** (REQ-005)  
La respuesta debe incluir metadata de paginación: totalElements, totalPages, currentPage, size.

**FUNC-006** (REQ-001)  
El Producer API debe configurar acceso de solo lectura a PostgreSQL mediante nueva DataSource con connection pool optimizado.

**FUNC-007** (REQ-001, REQ-004)  
El Frontend debe agregar nueva ruta `/kudos/list` con componente dedicado que soporte filtrado interactivo y ordenamiento.

**FUNC-008** (REQ-002)  
El Frontend debe implementar componente de filtros con campos de búsqueda, selector de categoría y date picker de rango.

**FUNC-009** (REQ-001, REQ-005)  
El Frontend debe implementar componente de tabla con paginación integrada y loading states.

**FUNC-010** (REQ-001)  
El Producer API debe implementar Full-Text Search en PostgreSQL para búsquedas eficientes de texto.

---

## 2.3 Requerimientos No Funcionales

| ID | Requerimiento | Descripción | Estrategia |
|----|---|---|---|
| **NFUNC-001** | Performance | Endpoint responde en <500ms para consultas sin filtros con hasta 100K registros | Índices en `created_at`, LIMIT/OFFSET, connection pool optimizado |
| **NFUNC-002** | Escalabilidad | Sistema soporta 1,000 rps concurrentes al endpoint de listado | Connection pool HikariCP configurado, considerar caché Caffeine Fase 2 |
| **NFUNC-003** | Seguridad | No exponer IDs secuenciales, no exponer emails completos, rate limiting 100 req/min/IP | Hashing de IDs, enmascaramiento emails, middleware rate limiting |
| **NFUNC-004** | Mantenibilidad | Código sigue strict separación en capas Controller → Service → Repository | Patrón Repository, DTOs record, sin lógica en controllers |
| **NFUNC-005** | Observabilidad | Logs estructurados de queries lentas (>200ms) y errores de DB | SLF4J con MDC, métricas Hibernate, Spring Actuator |
| **NFUNC-006** | Disponibilidad | Endpoint de lectura no afecta disponibilidad del endpoint de escritura | DataSource read-only separado, circuit breaker Fase 3 |
| **NFUNC-007** | Usabilidad | UI responsive mobile-first con carga inicial inmediata | Skeleton loaders, Tailwind responsivo, lazy loading |
| **NFUNC-008** | Confiabilidad | Sistema tolera índices faltantes inicialmente con graceful degradation | Setup automático de índices en migrations/init scripts |

---

## 2.4 Modelo de Dominio

### Entidades Existentes (Consumer Worker)

```java
@Entity
@Table(name = "kudos")
public class Kudo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  private String fromUser;      // email del remitente
  private String toUser;        // email del destinatario
  
  @Enumerated(EnumType.STRING)
  private KudoCategory category; // INNOVATION, TEAMWORK, PASSION, MASTERY
  
  private String message;       // 10-500 caracteres
  private LocalDateTime createdAt;
}

enum KudoCategory {
  INNOVATION, TEAMWORK, PASSION, MASTERY
}
```

### Nuevos DTOs (Producer API)

```java
// Request (query parameters mapeados a DTO via @RequestParam o custom resolver)
public record KudoSearchCriteria(
  @Nullable String category,
  @Nullable String searchText,
  @Nullable @DateTimeFormat(iso = ISO.DATE) LocalDate startDate,
  @Nullable @DateTimeFormat(iso = ISO.DATE) LocalDate endDate,
  @NotNull @Min(0) Integer page,
  @NotNull @Min(1) @Max(50) Integer size,
  @Pattern(regexp = "^(ASC|DESC)$") String sortDirection
) {}

// Response principal
public record PagedKudoResponse(
  List<KudoListItemDTO> content,
  long totalElements,
  int totalPages,
  int currentPage,
  int size
) {}

// Item individual (con privacidad)
public record KudoListItemDTO(
  String id,                    // UUID o hash, NO Long directo
  String fromUser,              // "j***z@sofkianos.com"
  String toUser,                // "a***n@sofkianos.com"
  String category,              // "TEAMWORK"
  String message,               // "Excelente colaboración en el proyecto X"
  LocalDateTime createdAt       // ISO 8601 string
) {}
```

### Relaciones y Dependencias

```
KudoSearchCriteria
    ↓
KudoQueryService.searchKudos()
    ↓
KudoSpecifications (construye predicates JPA)
    ↓
KudoQueryRepository.findAll(Specification, Pageable)
    ↓
PostgreSQL Database
    ↓
Kudo (entidad mapeada)
    ↓
KudoListItemDTO (enmascarado)
    ↓
PagedKudoResponse (con metadata)
    ↓
Frontend Component (tabla + paginación)
```

---

## 2.5 Reglas de Negocio

| ID | Regla | Aplicación |
|----|-------|-----------|
| **RN-001** | Enmascaramiento de emails | Mostrar solo primera y última letra: `juan.perez@*` → `j***z@sofkianos.com` |
| **RN-002** | Búsqueda acumulativa | Si múltiples filtros presentes, se aplican con lógica AND |
| **RN-003** | Filtrado por rango | `startDate` y `endDate` son inclusive: `createdAt BETWEEN startDate AND endDate` |
| **RN-004** | Límite de tamaño | `size` max 50 para prevenir sobrecarga y abuse |
| **RN-005** | Ordenamiento por defecto | Si no se especifica `sortDirection`, usar DESC (más reciente primero) |
| **RN-006** | Búsqueda case-insensitive | Ignorar mayúsculas/minúsculas en búsqueda de texto |
| **RN-007** | Normalización de acentos | "colaboración" debe matchear "colaboracion" y viceversa (PostgreSQL unaccent) |
| **RN-008** | Validación de categoría | Solo aceptar valores enum válidos; rechazar otros con 400 Bad Request |
| **RN-009** | Paginación obligatoria | Nunca retornar más de 50 items sin paginación, incluso sin query param `size` |
| **RN-010** | Rate limiting | Máx 100 requests por minuto por IP origen; retornar 429 Too Many Requests si excede |
| **RN-011** | Privacidad de IDs | No exponer Long secuencial directo; hashear con valor consistente por request |
| **RN-012** | Formato de timestamps | Timestamps siempre en ISO 8601 con zona horaria UTC |

---

## 2.6 Dependencias Técnicas

### Backend (Producer API)

| Dependencia | Versión | Propósito |
|------------|---------|----------|
| Spring Boot | 3.x | Framework base |
| Spring Data JPA | 3.x | Abstracción base de datos |
| Spring Web | 3.x | REST controllers |
| PostgreSQL Driver | 42.x | Conectividad PostgreSQL |
| HikariCP | 5.x | Connection pooling (automático vía Spring Boot) |
| Lombok | 1.x | Reducción boilerplate |
| Jakarta Validation | 3.x | Validación de inputs |
| Spring Security | 6.x | (opcional) Considerado para Fase 3 |
| Caffeine Cache | 3.x | (Fase 2) Caché en memoria |
| Gatling | 3.x | (Fase 3) Load testing |

### Frontend

| Dependencia | Versión | Propósito |
|------------|---------|----------|
| React | 18.x | Framework UI (ya presente) |
| React Router | 6.x | Routing (ya presente) |
| Axios | 1.x | HTTP client (ya presente) |
| Tailwind CSS | 3.x | Styling (ya presente) |
| Vitest | Latest | Testing unitario |
| React Testing Library | 14.x | Testing de componentes |
| react-datepicker | 4.x | Date range picker |
| TanStack Table | 8.x (opcional) | Tabla avanzada con paginación |

---

## 2.7 Consideraciones de Arquitectura

### 1. Shared Database Pattern (Justificado como MVP)

**Decisión**: Producer API accede a PostgreSQL en **modo read-only** para consultas.

**Justificación**:
- Evita latencia de service-to-service calls (producción baja latencia)
- Simplifica arquitectura de MVP (menos movimiento de datos)
- Mantiene separación lógica: Producer (comandos + queries) vs Consumer (event processing)
- Consumer Writer no expone endpoints REST (worker pattern puro)
- En producción, considerar CQRS completo con replica read-only

**Configuración**:
```yaml
# Producer API: application.properties
spring.datasource.url=jdbc:postgresql://...
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.read-only=true
spring.jpa.hibernate.ddl-auto=none
```

### 2. Patrón Repository con Specifications

Para filtros dinámicos complejos (categoría + búsqueda + rango fechas):

```java
public interface KudoQueryRepository extends 
    JpaRepository<Kudo, Long>, 
    JpaSpecificationExecutor<Kudo> {
  // Sin custom query methods, todo vía Specifications
}

// Specifications builder
public class KudoSpecifications {
  public static Specification<Kudo> withCategory(String category) { ... }
  public static Specification<Kudo> withSearchText(String text) { ... }
  public static Specification<Kudo> withDateRange(LocalDate start, LocalDate end) { ... }
}

// Uso en Service
Page<Kudo> page = repository.findAll(
  Specification.where(
    KudoSpecifications.withCategory(criteria.category())
      .and(KudoSpecifications.withSearchText(criteria.searchText()))
      .and(KudoSpecifications.withDateRange(criteria.startDate(), criteria.endDate()))
  ),
  PageRequest.of(criteria.page(), criteria.size(), 
    Sort.by(Sort.Direction.fromString(criteria.sortDirection()), "createdAt"))
);
```

### 3. Enmascaramiento de Emails (Utility Layer)

```java
public class EmailMaskingUtil {
  /**
   * Enmascarar email manteniendo dominio visible.
   * juan.perez@sofkianos.com → j***z@sofkianos.com
   */
  public static String mask(String email) {
    if (email == null || !email.contains("@")) {
      return "***@***.com";
    }
    String[] parts = email.split("@");
    String local = parts[0];
    String domain = parts[1];
    
    if (local.length() <= 2) {
      return local + "***@" + domain;
    }
    
    return local.charAt(0) + 
           "*".repeat(local.length() - 2) + 
           local.charAt(local.length() - 1) + 
           "@" + domain;
  }
}
```

### 4. ID Hashing (Opción B - Fase 4)

```java
// En KudoListItemDTO mapper
String hashedId = Base64.getEncoder()
  .encodeToString(String.valueOf(kudo.getId()).getBytes());
// O usar SHA256 para mayor consistencia:
String hashedId = DigestUtils.sha256Hex(kudo.getId().toString());
```

### 5. Full-Text Search Implementation (Opción B - Fase 4)

```sql
-- Configuración PostgreSQL
CREATE INDEX idx_kudos_fts ON kudos USING gin(
  to_tsvector('spanish', coalesce(from_user, '') || ' ' || 
                         coalesce(to_user, '') || ' ' || 
                         coalesce(message, ''))
);

-- Criteria query en JPA
CriteriaBuilder.function("to_tsvector", Root<Kudo>)
```

### 6. Rate Limiting (Opción B - Fase 4)

Implementar vía Spring Cloud Gateway o filtro custom:

```java
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
  private final LoadingCache<String, AtomicInteger> requestCounts;
  
  @Override
  protected void doFilterInternal(HttpServletRequest request, 
      HttpServletResponse response, FilterChain chain) {
    String ip = getClientIp(request);
    int count = requestCounts.getUnchecked(ip).incrementAndGet();
    
    if (count > 100) { // 100 per minute
      response.setStatus(429); // Too Many Requests
      return;
    }
    chain.doFilter(request, response);
  }
}
```

### 7. Graceful Degradation Database (Opción B - Fase 4)

```java
@RestController
@RequestMapping("/api/v1/kudos")
public class KudosQueryController {
  
  @GetMapping
  public ResponseEntity<?> getKudos(KudoSearchCriteria criteria) {
    try {
      Page<KudoListItemDTO> results = kudoQueryService.searchKudos(criteria);
      return ResponseEntity.ok(results);
    } catch (DataAccessException e) {
      log.error("Database unavailable", e);
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(new ErrorResponse("DB_UNAVAILABLE", 
          "El servicio está temporalmente indisponible", 
          LocalDateTime.now()));
    }
  }
}
```

---

## 2.8 Criterios de Aceptación Iniciales

### Backend

- [ ] Endpoint `GET /api/v1/kudos` responde 200 OK con estructura paginada válida
- [ ] Emails enmascarados correctamente en respuesta (ej: `j***z@domain.com`)
- [ ] IDs hasheados/transformados (no exponen Long secuencial)
- [ ] Filtros por categoría retornan solo kudos de esa categoría
- [ ] Búsqueda full-text en message, fromUser, toUser funciona correctly
- [ ] Búsqueda normaliza acentos (colaboración = colaboracion)
- [ ] Paginación respeta límites (máx 50 items por página)
- [ ] Ordenamiento ASC/DESC funciona según parámetro
- [ ] Rate limiting retorna 429 al exceder 100 req/min por IP
- [ ] Graceful degradation: endpoint retorna 503 si DB no está disponible
- [ ] Timestamps en ISO 8601 con UTC
- [ ] Tests unitarios cubren >80% de service layer
- [ ] Tests de integración verifican queries SQL con datos reales
- [ ] Documentación Swagger/OpenAPI generada automáticamente

### Frontend

- [ ] Página `/kudos/list` renderiza correctamente en chrome/firefox/safari
- [ ] Tabla muestra kudos con columnas: De, Para, Categoría, Mensaje, Fecha
- [ ] Paginación funciona (anterior/siguiente, números de página)
- [ ] Filtros se aplican correctamente al hacer submit en componente filters
- [ ] Loading states durante fetch (skeleton loaders)
- [ ] Empty state cuando no hay resultados ("No se encontraron kudos")
- [ ] Responsive en mobile (320px), tablet (768px), desktop (1920px)
- [ ] Links accesibles desde Navbar
- [ ] Ordenamiento toggle (ASC/DESC) visible y funcional
- [ ] Date picker de rango selecciona fechas correctamente
- [ ] Búsqueda de texto debounced (500ms)
- [ ] Tests unitarios componentes con Vitest >70% coverage
- [ ] Integración con mock MSW del servicio API

---

## 2.9 Matriz de Riesgos Detallada

| ID | Riesgo | Prob | Impacto | Nivel | Mitigación | Owner | Monitoreo |
|----|--------|------|---------|-------|---------  |-------|-----------|
| R-001 | Enmascaramiento incorrecto expone emails | Alta | Crítico | **CRÍTICO** | Tests unitarios 100% de EmailMaskingUtil, code review pares | Backend Lead | Tests CI/CD |
| R-002 | Indices faltantes degradan performance | Alta | Alto | **ALTO** | Crear índices preemptively en migrations, EXPLAIN ANALYZE queries | DBA | Grafana metrics |
| R-003 | CORS bloquea requests | Media | Medio | **MEDIO** | Test con curl/postman antes de merger, CI checks | QA | E2E tests |
| R-004 | Connection pool exhaustion | Media | Alto | **ALTO** | HikariCP limites: max=10, connection-timeout=30s | DevOps | Actuator metrics |
| R-005 | Búsqueda lenta sin FTS | Media | Medio | **MEDIO** | Full-Text Search implementado como decidido | Backend | Query logs |
| R-006 | SQL Injection vía query params | Baja | Crítico | **ALTO** | JPA Specifications (no SQL raw), validación DTOs | Security | WAF logs |
| R-007 | UI inconsistencias sin error handling | Media | Bajo | **BAJO** | Loading/error/empty states en todos los casos | FrontEnd | Test coverage |
| R-008 | Rate limiting mal configurado | Baja | Medio | **MEDIO** | Load testing con Gatling, threshold ajuste | DevOps | API metrics |
| R-009 | Inconsistencia hash IDs entre requests | Media | Bajo | **BAJO** | Hash determinístico, tests de consistency | Backend | Unit tests |
| R-010 | Paginación offset lento en gran volumen | Baja | Medio | **BAJO** | Considerar keyset pagination en Fase 3 | Backend | EXPLAIN ANALYZE |

---

<a name="fase-3"></a>

# 📦 FASE 3 — Listado de Entregables de Valor

## 3.1 Épicas

### EP-001: Consulta Pública de Kudos
Como stakeholder del producto, quiero que los usuarios públicos puedan consultar el histórico de kudos registrados para incrementar visibilidad y engagement con el sistema de cultura organizacional.

**Valor de negocio**: Mayor transparencia, refuerzo cultural.

### EP-002: Capacidades de Filtrado Avanzado
Como usuario del sistema, quiero múltiples formas de filtrar, buscar y ordenar kudos para encontrar reconocimientos relevantes rápidamente según mi necesidad.

**Valor de negocio**: Mejor UX, mayor adopción.

---

## 3.2 Features

| ID | Feature | Épica | Descripción |
|----|---------|-------|------------|
| **FT-001** | Backend Query API | EP-001 | Implementar endpoint REST de consulta en Producer API con acceso read-only a PostgreSQL |
| **FT-002** | Privacidad de Datos | EP-001 | Implementar lógica de privacidad: enmascaramiento emails, hashing IDs |
| **FT-003** | Sistema de Filtrado | EP-002 | Implementar filtros dinámicos: categoría, búsqueda texto, rango fechas, full-text search |
| **FT-004** | Ordenamiento Configurable | EP-002 | Implementar toggle ASC/DESC para ordenar por fecha |
| **FT-005** | Frontend Lista de Kudos | EP-001 | Implementar página y componentes de visualización en React |
| **FT-006** | Paginación | EP-001 | Implementar navegación entre páginas de 20 items cada una |
| **FT-007** | Documentación Técnica | EP-001 | Tests automatizados y documentación OpenAPI |
| **FT-008** | Rate Limiting | EP-002 | Implementar protección contra abuse de API (100 req/min/IP) |

---

## 3.3 Historias de Usuario Completas

### US-001: Endpoint de Listado Básico ⭐ CRÍTICA

**Título**: Crear endpoint GET para listar kudos  
**Story Points**: 5  
**Prioridad**: CRÍTICA  
**Epic**: EP-001  

**Descripción**:  
Como desarrollador frontend  
Quiero un endpoint REST que retorne kudos paginados de forma determinista  
Para mostrarlos en la interfaz de usuario sin riesgo de inconsistencia de datos  

**Criterios de aceptación**:
- [ ] `GET /api/v1/kudos` responde 200 OK
- [ ] Respuesta contiene: content[], totalElements, totalPages, currentPage, size
- [ ] Cada item tiene: id (hasheado), fromUser (enmascarado), toUser (enmascarado), category, message, createdAt
- [ ] Timestamps están en ISO 8601 UTC
- [ ] Por defecto retorna página 0 con 20 items ordenados DESC por fecha
- [ ] Aceptar query params: page, size, category, searchText, startDate, endDate, sortDirection
- [ ] Validar size max 50
- [ ] Validar sortDirection solo ASC/DESC
- [ ] Documentado en Swagger con ejemplos

**Dependencias**: Ninguna  
**Código asociado**:
- Backend: `KudosQueryController.java`, `KudoQueryService.java`
- Testing: `KudosQueryControllerIT.java`

**Release sugerido**: MVP Sprint 1

---

### US-002: Configuración de Base de Datos en Producer API ⭐ CRÍTICA

**Título**: Agregar acceso read-only a PostgreSQL en Producer API  
**Story Points**: 3  
**Prioridad**: CRÍTICA  
**Epic**: EP-001  

**Descripción**:  
Como arquitecto del sistema  
Quiero que Producer API pueda leer de PostgreSQL Supabase sin modificaciones  
Para servir queries sin depender del Consumer Worker y mantener aislamiento de servicios  

**Criterios de aceptación**:
- [ ] `application.properties` configura segunda DataSource con read-only=true
- [ ] Connection pool HikariCP: maxPoolSize=10, minIdle=5, connectionTimeout=30s
- [ ] Producer API inicia exitosamente y conecta a DB
- [ ] Logs confirman "Database connection successful" o similar
- [ ] `spring.jpa.hibernate.ddl-auto=none` (no crea/modifica schema)
- [ ] Usar variables entorno para credenciales sensibles `${DB_PASSWORD}`
- [ ] Tests de integración verifican conexión

**Dependencias**: Ninguna  
**Decisión técnica**: Shared Database Pattern (justificado en Fase 2.7)  
**Release sugerido**: MVP Sprint 1

---

### US-003: Entidad y Repository en Producer API

**Título**: Crear entidad Kudo y repository en Producer API  
**Story Points**: 2  
**Prioridad**: Alta  
**Epic**: EP-001  

**Descripción**:  
Como desarrollador backend  
Quiero reutilizar/replicar la estructura de entidad Kudo del Consumer  
Para realizar queries optimizadas de lectura en Producer API  

**Criterios de aceptación**:
- [ ] Copiar `Kudo.java` desde Consumer a Producer (sin modificar Consumer)
- [ ] Refactorizar imports para Consumer
- [ ] `@Table(name = "kudos")` mapea correctamente a tabla existente
- [ ] `KudoQueryRepository extends JpaRepository<Kudo, Long>, JpaSpecificationExecutor<Kudo>`
- [ ] Sin métodos de escritura (save, delete, update omitidos o marcados @Deprecated)
- [ ] Entidad es read-only en contexto de Producer
- [ ] Compila sin errores

**Criterios técnicos**:
- Usar Lombok `@Getter`, `@NoArgsConstructor` (requerido por JPA)
- `@Enumerated(EnumType.STRING)` para category

**Dependencias**: US-002  
**Release sugerido**: MVP Sprint 1

---

### US-004: Service Layer con Lógica de Búsqueda ⭐ CRÍTICA

**Título**: Implementar KudoQueryService con filtros dinámicos  
**Story Points**: 8  
**Prioridad**: CRÍTICA  
**Epic**: EP-002  

**Descripción**:  
Como desarrollador backend  
Quiero un service que construya queries dinámicas basadas en criterios del usuario  
Para aplicar filtros completamente sin lógica de negocio en controller  

**Criterios de aceptación**:
- [ ] `KudoQueryService.searchKudos(KudoSearchCriteria criteria)` retorna `Page<KudoListItemDTO>`
- [ ] Usa `KudoSpecifications` para construir predicates dinámicos
- [ ] Filtra por categoría solo si `criteria.category()` no null
- [ ] Filtra por rango de fechas (inclusive) solo si ambas fechas presentes
- [ ] Aplica búsqueda full-text en message, fromUser, toUser
- [ ] Normaliza acentos (unaccent PostgreSQL)
- [ ] Respeta paginación y ordenamiento en `Pageable`
- [ ] Enmascarar emails en DTOs de salida (`EmailMaskingUtil.mask()`)
- [ ] Hashear IDs de salida (no exponer Long secuencial)
- [ ] Maneja `DataAccessException` lanzando custom exception
- [ ] Logs con SLF4J para queries lentas (>200ms)
- [ ] 100% tested con Mockito

**Especificación técnica**:
```java
Page<KudoListItemDTO> searchKudos(KudoSearchCriteria criteria);
```

**Dependencias**: US-003, US-005, US-006  
**Release sugerido**: MVP Sprint 1

---

### US-005: DTOs de Request y Response

**Título**: Crear DTOs tipados y validados para query endpoint  
**Story Points**: 3  
**Prioridad**: Alta  
**Epic**: EP-001  

**Descripción**:  
Como desarrollador backend  
Quiero DTOs con validación automática vía Jakarta Validation  
Para garantizar contratos API seguros sin lógica en controller  

**Criterios de aceptación**:
- [ ] `KudoSearchCriteria record`: fields opcionales, validados con anotaciones
  - `category`: @Nullable, pero si no null validar enum
  - `searchText`: @Nullable
  - `startDate` / `endDate`: @Nullable, si ambas presentes validar startDate ≤ endDate
  - `page`: @Min(0)
  - `size`: @Min(1), @Max(50)
  - `sortDirection`: @Pattern(regexp = "^(ASC|DESC)$")
- [ ] `PagedKudoResponse record`: con content, totalElements, totalPages, currentPage, size
- [ ] `KudoListItemDTO record`: id (String hasheado), fromUser (masked), toUser (masked), category, message, createdAt
- [ ] Todos son `record` (immutables) sin setters
- [ ] Tests de validación para edge cases (size=0, size=101, null dates, etc.)

**Dependencias**: Ninguna  
**Release sugerido**: MVP Sprint 1

---

### US-006: Enmascaramiento de Emails ⭐ CRÍTICA

**Título**: Implementar utilidad de enmascaramiento seguro de emails  
**Story Points**: 2  
**Prioridad**: CRÍTICA  
**Epic**: EP-001  

**Descripción**:  
Como responsable de seguridad/privacidad  
Quiero que los emails se enmascaren automáticamente en respuestas API  
Para proteger información personal de usuarios mientras mantengo contexto  

**Especificación**:
- Input: `juan.perez@sofkianos.com`
- Output: `j***z@sofkianos.com`
- Casos edge:
  - `a@domain.com` → `a***a@domain.com`
  - Email inválido → `***@***.com` (fallback seguro)
  - Null → `null` (no enmascarar null)

**Criterios de aceptación**:
- [ ] `EmailMaskingUtil.mask(String email)` implementado
- [ ] Retorna String formato `firstChar***lastChar@domain`
- [ ] Maneja emails de cualquier longitud local-part
- [ ] Tests unitarios 100% coverage:
  - Caso normal
  - Email corto (1-2 caracteres)
  - Email muy largo (50+ caracteres)
  - Email sin @
  - Null input
  - Dominio con subdominios
- [ ] Performance: <1ms por email (crítico con miles)

**Código de referencia**:
```java
public class EmailMaskingUtil {
  public static String mask(String email) {
    if (email == null || !email.contains("@")) {
      return "***@***.com";
    }
    // ... implementación
  }
}
```

**Dependencias**: Ninguna  
**Release sugerido**: MVP Sprint 1

---

### US-007: Controller con Validación ⭐ CRÍTICA

**Título**: Crear KudosQueryController con endpoint GET limpio  
**Story Points**: 3  
**Prioridad**: CRÍTICA  
**Epic**: EP-001  

**Descripción**:  
Como desarrollador backend  
Quiero un controller que sirva solo como entrada HTTP delegando a service  
Para mantener strict separación de capas y evitar lógica en controller  

**Criterios de aceptación**:
- [ ] Clase `KudosQueryController` anotada `@RestController`, `@RequestMapping("/api/v1/kudos")`
- [ ] Método `@GetMapping` sin parámetros de ruta (básico GET /api/v1/kudos)
- [ ] Query params mapeados a `@RequestParam` o via `KudoSearchCriteria`
- [ ] Validación con `@Valid` en parámetros
- [ ] Delega lógica completamente a `KudoQueryService.searchKudos()`
- [ ] Retorna `ResponseEntity<PagedKudoResponse>` con status 200
- [ ] Manejo de errores:
  - NO try-catch en controller
  - Excepciones burbujean a `@RestControllerAdvice` global
  - `DataAccessException` mapea a 503 Service Unavailable
  - `IllegalArgumentException` mapea a 400 Bad Request
- [ ] Documentado con anotaciones OpenAPI:
  - `@Operation(summary = "...", description = "...")`
  - `@ApiResponse(responseCode = "200", description = "...")`
  - `@ApiResponse(responseCode = "400", description = "...")`
  - `@ApiResponse(responseCode = "503", description = "...")`
- [ ] Tests de integración (Spring MockMvc)

**Código esqueleto**:
```java
@RestController
@RequestMapping("/api/v1/kudos")
@RequiredArgsConstructor
public class KudosQueryController {
  
  private final KudoQueryService service;
  
  @GetMapping
  @Operation(summary = "Listar kudos públicos")
  public ResponseEntity<PagedKudoResponse> getKudos(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String searchText,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate,
      @RequestParam(defaultValue = "DESC") String sortDirection
  ) {
    KudoSearchCriteria criteria = new KudoSearchCriteria(...);
    Page<KudoListItemDTO> results = service.searchKudos(criteria);
    return ResponseEntity.ok(new PagedKudoResponse(...));
  }
}
```

**Dependencias**: US-004, US-005, US-002  
**Release sugerido**: MVP Sprint 1

---

### US-008: Specifications para Criterios Dinámicos

**Título**: Crear builders de JPA Specifications para filtros dinámicos  
**Story Points**: 3  
**Prioridad**: Alta  
**Epic**: EP-002  

**Descripción**:  
Como desarrollador backend  
Quiero usar JPA Specifications para construir queries dinámicamente  
Para evitar many custom methods en repository y mantener flexibilidad  

**Criterios de aceptación**:
- [ ] Clase `KudoSpecifications` con static factory methods:
  - `withCategory(String category)`
  - `withSearchText(String text)` - full-text search
  - `withDateRange(LocalDate start, LocalDate end)`
- [ ] Cada specification es chainable con `and()`, `or()`
- [ ] Full-text search usa PostgreSQL `to_tsvector` si disponible
- [ ] Date range es inclusive: `BETWEEN startDate AND endDate + 1 day`
- [ ] Handles null gracefully (null => no specification)
- [ ] Tests unitarios

**Código esqueleto**:
```java
public class KudoSpecifications {
  public static Specification<Kudo> withCategory(String category) {
    return (root, query, cb) -> {
      if (category == null) return null;
      return cb.equal(root.get("category"), KudoCategory.valueOf(category));
    };
  }
  
  public static Specification<Kudo> withSearchText(String text) {
    return (root, query, cb) -> {
      if (text == null || text.isEmpty()) return null;
      // Full-text search logic
      return cb.like(cb.lower(root.get("message")), "%" + text.toLowerCase() + "%");
    };
  }
}
```

**Dependencias**: US-003  
**Release sugerido**: MVP Sprint 1

---

### US-009: Tests Unitarios Backend ⭐CRÍTICA

**Título**: Tests JUnit 5 exhaustivos para service y utilities  
**Story Points**: 5  
**Prioridad**: CRÍTICA  
**Epic**: EP-001  

**Descripción**:  
Como desarrollador backend / QA engineer  
Quiero tests automatizados que cubran la lógica crítica  
Para prevenir regresiones y garantizar quality gate >80%

**Criterios de aceptación**:
- [ ] `EmailMaskingUtilTest`: 100% coverage
  - Normal email
  - Short local-part
  - Long local-part
  - No @ symbol
  - Null input
- [ ] `KudoQueryServiceImplTest` (Mockito): >80% coverage
  - searchKudos() con múltiples combinaciones de filtros
  - Paginación respeta límites
  - Enmascaramiento se aplica
  - Excepciones mapeadas correctamente
- [ ] `KudoSpecificationsTest`: >70% coverage
  - Specification nula cuando parámetro nulo
  - Predicate construcción correcta
- [ ] `KudosQueryControllerTest` (MockMvc): >70% coverage
  - GET /api/v1/kudos retorna 200
  - Query params validados
  - Errores mapeados a status correcto
- [ ] Coverage total target: >80%
- [ ] Ejecutar `mvn test` sin errores
- [ ] Configurar Jacoco para reports

**Ejecución**:
```bash
mvn clean test
mvn jacoco:report
# reports en target/site/jacoco/index.html
```

**Dependencias**: US-004, US-006, US-007, US-008  
**Release sugerido**: MVP Sprint 1

---

### US-010: Tests de Integración con Base de Datos

**Título**: Tests de integración contra PostgreSQL real  
**Story Points**: 3  
**Prioridad**: Alta  
**Epic**: EP-001  

**Descripción**:  
Como QA engineer  
Quiero tests que ejecuten queries reales contra PostgreSQL  
Para verificar behavior de repository y specification con datos reales  

**Criterios de aceptación**:
- [ ] `KudoQueryRepositoryIT` usando TestContainers PostgreSQL
- [ ] Setup de datos: insert 50+ kudos con categorías, fecha, etc.
- [ ] Pruebas:
  - `findAll()` retorna todos los kudos
  - `findAll(Specification)` con categoría filtra correctamente
  - `findAll(Specification, Pageable)` respeta paging
  - Ordenamiento por fecha funciona ASC/DESC
- [ ] Cleanup automático después de cada test
- [ ] Ejecutar separado de unit tests: `mvn verify -Pit`

**Configuración**:
```java
@DataJpaTest
@Import({JpaAuditingConfig.class})
@Testcontainers
class KudoQueryRepositoryIT {
  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(...)
    .withDatabaseName("testdb")
    .withUsername("test");
  
  // tests...
}
```

**Dependencias**: US-003, US-008  
**Release sugerido**: MVP Sprint 1

---

### US-011: Documentación OpenAPI / Swagger

**Título**: Documentación completa del endpoint en Swagger  
**Story Points**: 2  
**Prioridad**: Media  
**Epic**: EP-001  

**Descripción**:  
Como consumidor de API / QA engineer  
Quiero documentación interactiva y auto-generada  
Para entender cómo usar el endpoint sin leer código  

**Criterios de aceptación**:
- [ ] Agregar dependencia `springdoc-openapi-starter-webmvc-ui` (si no existe)
- [ ] Anotaciones OpenAPI en controller:
  - `@Operation(summary, description)`
  - `@Parameters` con description de cada query param
  - `@ApiResponse` con ejemplos de request/response
- [ ] Acceder a `http://localhost:8082/swagger-ui` y verificar:
  - Endpoint visible con descripción
  - Parámetros listados y documentados
  - "Try it out" funciona
  - Response examples correctos
- [ ] Generar OpenAPIspec en JSON: `/v3/api-docs`

**Anotaciones ejemplo**:
```java
@Operation(
  summary = "Listar kudos públicos",
  description = "Retorna lista paginada y filtrada de kudos registrados"
)
@GetMapping
public ResponseEntity<PagedKudoResponse> getKudos(
  @Parameter(description = "Número de página (0-indexed)")
  @RequestParam(defaultValue = "0") int page,
  
  @Parameter(description = "Tamaño de página (1-50)")
  @RequestParam(defaultValue = "20") int size,
  
  @Parameter(description = "Categoría: Innovation, Teamwork, Passion, Mastery")
  @RequestParam(required = false) String category,
  // ...
) {}
```

**Dependencias**: US-007  
**Release sugerido**: MVP Sprint 1

---

### US-012: Servicio Frontend para GET Kudos

**Título**: Extender kudosService con método de listado  
**Story Points**: 2  
**Prioridad**: Alta  
**Epic**: EP-001  

**Descripción**:  
Como desarrollador frontend  
Quiero método en servicio API que encapsule la petición GET  
Para mantener API calls centralizadas y facilitar testing  

**Criterios de aceptación**:
- [ ] Extender `kudosService` con método:
  ```typescript
  list(filters: KudoFilters, page: number, size: number, sortDirection: 'ASC' | 'DESC'): Promise<PagedKudoResponse>
  ```
- [ ] Construye query string correctamente:
  - `?category=TEAMWORK&searchText=proyecto&page=0&size=20&sortDirection=DESC`
- [ ] Retorna `Promise<PagedKudoResponse>` fuertemente tipado
- [ ] Errores HTTP manejados por interceptor global (ya existe)
- [ ] Documentación JSDoc completa

**Interfaces TypeScript**:
```typescript
interface KudoFilters {
  category?: 'Innovation' | 'Teamwork' | 'Passion' | 'Mastery';
  searchText?: string;
  startDate?: string; // ISO 8601
  endDate?: string;   // ISO 8601
}

interface KudoListItem {
  id: string;
  fromUser: string;
  toUser: string;
  category: string;
  message: string;
  createdAt: string;
}

interface PagedKudoResponse {
  content: KudoListItem[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  size: number;
}
```

**Código**:
```typescript
export const kudosService = {
  // ... existing send() method
  
  list: async (
    filters: KudoFilters = {},
    page: number = 0,
    size: number = 20,
    sortDirection: 'ASC' | 'DESC' = 'DESC'
  ): Promise<PagedKudoResponse> => {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sortDirection', sortDirection);
    
    if (filters.category) params.append('category', filters.category);
    if (filters.searchText) params.append('searchText', filters.searchText);
    if (filters.startDate) params.append('startDate', filters.startDate);
    if (filters.endDate) params.append('endDate', filters.endDate);
    
    const response = await apiClient.get(`/v1/kudos?${params}`);
    return response.data;
  }
};
```

**Dependencias**: US-007  
**Release sugerido**: MVP Sprint 2

---

### US-013: Página de Lista de Kudos ⭐ CRÍTICA

**Título**: Crear KudosListPage componente principal  
**Story Points**: 5  
**Prioridad**: CRÍTICA  
**Epic**: EP-001  

**Descripción**:  
Como usuario final  
Quiero una página dedicada a explorar kudos  
Para ver todos los reconocimientos registrados en la organización  

**Criterios de aceptación**:
- [ ] Componente `KudosListPage.tsx` en `src/pages/`
- [ ] Parámetros de URL preservados (deep linking):
  - `?page=1&size=20&category=TEAMWORK&searchText=...`
- [ ] Estados manejados correctamente:
  - Cargando inicial: skeleton loaders
  - Cargando en cambio de página: opacity reducida o loading bar
  - Vacío: mensaje "No se encontraron kudos"
  - Error: mostrar error con opción de reintentar
- [ ] Estructura HTML:
  ```
  - Header con título "Explorar Kudos"
  - KudoFilters component (arriba)
  - Loading/Empty/Error states (condicionales)
  - KudoTable component (contenedor datos)
  - KudoPagination component (abajo)
  ```
- [ ] Responsive: mobile (320px), tablet (768px), desktop (1920px)
- [ ] Accesibilidad: ARIA labels, semantic HTML

**Esbozo código**:
```typescript
export const KudosListPage: React.FC = () => {
  const [filters, setFilters] = useState<KudoFilters>({});
  const [page, setPage] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [data, setData] = useState<PagedKudoResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  
  useEffect(() => {
    const fetchKudos = async () => {
      setIsLoading(true);
      try {
        const result = await kudosService.list(filters, page);
        setData(result);
        setError(null);
      } catch (err) {
        setError((err as Error).message);
      } finally {
        setIsLoading(false);
      }
    };
    
    fetchKudos();
  }, [filters, page]);
  
  return (
    <div className="container mx-auto py-8">
      <h1 className="text-3xl font-bold mb-6">Explorar Kudos</h1>
      <KudoFilters onFilter={setFilters} />
      {isLoading && <SkeletonLoaders />}
      {error && <ErrorMessage error={error} onRetry={fetchKudos} />}
      {!isLoading && !error && data.content.length === 0 && <EmptyState />}
      {!isLoading && !error && data && (
        <>
          <KudoTable items={data.content} />
          <KudoPagination 
            current={data.currentPage} 
            total={data.totalPages}
            onPageChange={setPage}
          />
        </>
      )}
    </div>
  );
};
```

**Dependencias**: US-012, US-014, US-015, US-016  
**Release sugerido**: MVP Sprint 2

---

### US-014: Componente de Tabla de Kudos

**Título**: Crear componente reutilizable KudoTable  
**Story Points**: 3  
**Prioridad**: Alt  
**Epic**: EP-001  

**Descripción**:  
Como desarrollador frontend  
Quiero componente de tabla desacoplado  
Para reutilizarlo en múltiples vistas y mantener código limpio  

**Especificación**:
```typescript
interface KudoTableProps {
  items: KudoListItem[];
  loading?: boolean;
  onRowClick?: (kudo: KudoListItem) => void;
}

export const KudoTable: React.FC<KudoTableProps> = ({ items, loading }) => {
  // Component implementation
}
```

**Criterios de aceptación**:
- [ ] Columnas: De, Para, Categoría, Mensaje, Fecha (responsive order)
- [ ] Rendering datos con `items.map()`
- [ ] Fechas formateadas: "19 feb 2026, 10:30"
- [ ] Categorías con badges de color:
  - Innovation: azul
  - Teamwork: verde
  - Passion: rojo
  - Mastery: amarillo
- [ ] Emails enmascarados legibles (hint en hover opcionalmente)
- [ ] Mensajes truncados si >100 caracteres con "..."
- [ ] Loading state: skeleton rows
- [ ] Responsive:
  - Mobile: solo De, Para, Fecha (scroll horizontal mensaje)
  - Desktop: todas las columnas normales
- [ ] Accesibilidad: `<table>` semántica, `scope="col"`

**Esbozo HTML**:
```tsx
<div className="overflow-x-auto">
  <table className="w-full min-w-max">
    <thead>
      <tr className="border-b">
        <th scope="col" className="text-left p-3">De</th>
        <th scope="col" className="text-left p-3">Para</th>
        <th scope="col" className="text-left p-3">Categoría</th>
        <th scope="col" className="text-left p-3">Mensaje</th>
        <th scope="col" className="text-left p-3">Fecha</th>
      </tr>
    </thead>
    <tbody>
      {items.map((item) => (
        <tr key={item.id} className="border-b hover:bg-gray-900">
          <td className="p-3">{item.fromUser}</td>
          <td className="p-3">{item.toUser}</td>
          <td className="p-3"><Badge category={item.category} /></td>
          <td className="p-3 max-w-xs truncate">{item.message}</td>
          <td className="p-3">{formatDate(item.createdAt)}</td>
        </tr>
      ))}
    </tbody>
  </table>
</div>
```

**Dependencias**: Ninguna  
**Release sugerido**: MVP Sprint 2

---

### US-015: Componente de Filtros

**Título**: Crear KudoFilters con campos de búsqueda interactivos  
**Story Points**: 5  
**Prioridad**: Media  
**Epic**: EP-002  

**Descripción**:  
Como usuario final  
Quiero filtrar kudos con formulario clara  
Para encontrar reconocimientos específicos sin hacer scroll infinito  

**Especificación props**:
```typescript
interface KudoFiltersProps {
  onFilter: (filters: KudoFilters) => void;
}

interface KudoFilters {
  category?: 'Innovation' | 'Teamwork' | 'Passion' | 'Mastery';
  searchText?: string;
  startDate?: string;
  endDate?: string;
}
```

**Criterios de aceptación**:
- [ ] Campos del formulario:
  1. **Búsqueda de texto**: input debounced (500ms)
     - Placeholder: "Buscar en de, para, mensaje..."
     - Debounce implementado con useEffect cleanup
  2. **Categoría**: select dropdown con opciones + "Todas"
  3. **Fecha desde**: date input HTML5
  4. **Fecha hasta**: date input HTML5
  5. **Botón "Aplicar Filtros"**: submit
  6. **Botón "Limpiar"**: reset form a defaults
- [ ] Validación:
  - Si startDate > endDate, mostrar error
  - Avisar si no se selecciona nada
- [ ] Estados:
  - Disabled mientras se carga lista
  - Visual feedback de búsqueda activa (loading spinner)
- [ ] Responsive: Stack vertical en mobile, horizontal en desktop

**Esbozo código**:
```typescript
export const KudoFilters: React.FC<KudoFiltersProps> = ({ onFilter }) => {
  const [formData, setFormData] = useState<KudoFilters>({});
  
  const handleSearch = (text: string) => {
    // Debounced search
    const timer = setTimeout(() => {
      setFormData(prev => ({ ...prev, searchText: text }));
    }, 500);
    return () => clearTimeout(timer);
  };
  
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onFilter(formData);
  };
  
  const handleClear = () => {
    setFormData({});
    onFilter({});
  };
  
  return (
    <form onSubmit={handleSubmit} className="bg-gray-900 p-6 rounded-lg mb-6">
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <input
          type="text"
          placeholder="Buscar..."
          onChange={(e) => handleSearch(e.target.value)}
          className="px-3 py-2 bg-gray-800 rounded border"
        />
        <select
          value={formData.category || ''}
          onChange={(e) => setFormData({...formData, category: e.target.value || undefined})}
          className="px-3 py-2 bg-gray-800 rounded border"
        >
          <option value="">Todas las categorías</option>
          <option value="Innovation">Innovation</option>
          {/* ... */}
        </select>
        <input
          type="date"
          value={formData.startDate || ''}
          onChange={(e) => setFormData({...formData, startDate: e.target.value || undefined})}
          className="px-3 py-2 bg-gray-800 rounded border"
        />
        <input
          type="date"
          value={formData.endDate || ''}
          onChange={(e) => setFormData({...formData, endDate: e.target.value || undefined})}
          className="px-3 py-2 bg-gray-800 rounded border"
        />
      </div>
      <div className="flex gap-4 mt-4">
        <button type="submit" className="bg-brand px-6 py-2 rounded">
          Aplicar Filtros
        </button>
        <button type="button" onClick={handleClear} className="bg-gray-700 px-6 py-2 rounded">
          Limpiar
        </button>
      </div>
    </form>
  );
};
```

**Dependencias**: Ninguna  
**Release sugerido**: MVP Sprint 2

---

### US-016: Paginación Interactiva

**Título**: Crear componente KudoPagination  
**Story Points**: 3  
**Prioridad**: Media  
**Epic**: EP-001  

**Descripción**:  
Como usuario final  
Quiero navegación de páginas clara  
Para explorar todos los kudos sin límite de scroll  

**Especificación**:
```typescript
interface KudoPaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  size?: number;
}
```

**Criterios de aceptación**:
- [ ] Botón "Anterior": disabled si currentPage === 0
- [ ] Botón "Siguiente": disabled si currentPage === totalPages - 1
- [ ] Números de página:
  - Mostrar 5 botones máximo: [1, 2, 3, 4, 5]
  - Con elipsis si hay muchas: [1, 2, ..., 10, 11]
  - Página actual destacada
- [ ] Indicador de rango: "Mostrando 21-40 de 150 kudos"
- [ ] Responsive: botones más pequeños en mobile
- [ ] Accesibilidad: `role="navigation"`, ARIA labels

**Código esqueleto**:
```typescript
export const KudoPagination: React.FC<KudoPaginationProps> = ({
  currentPage,
  totalPages,
  onPageChange,
  size = 20
}) => {
  const startItem = currentPage * size + 1;
  const endItem = Math.min((currentPage + 1) * size, totalElements);
  
  return (
    <nav className="flex justify-between items-center mt-8" role="navigation">
      <button
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 0}
        className="px-4 py-2 bg-brand rounded disabled:opacity-50"
      >
        ← Anterior
      </button>
      
      <span className="text-sm">
        Mostrando {startItem}-{endItem} de ...
      </span>
      
      <button
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage === totalPages - 1}
        className="px-4 py-2 bg-brand rounded disabled:opacity-50"
      >
        Siguiente →
      </button>
    </nav>
  );
};
```

**Dependencias**: Ninguna  
**Release sugerido**: MVP Sprint 2

---

### US-017: Ordenamiento Configurable

**Título**: Toggle de orden por fecha (ASC/DESC)  
**Story Points**: 2  
**Prioridad**: Baja  
**Epic**: EP-002  

**Descripción**:  
Como usuario final  
Quiero cambiar orden de la lista de kudos  
Para ver los más antiguos primero si lo prefiero  

**Criterios de aceptación**:
- [ ] Toggle/button en header de tabla o encima de KudoTable
- [ ] Estados:
  - "↓ Más recientes" (DESC activo)
  - "↑ Más antiguos" (ASC activo)
- [ ] Clic cambia orden y recarga datos
- [ ] Indicador visual claro
- [ ] Valor persiste en URL query params

**Código**:
```typescript
const [sortDirection, setSortDirection] = useState<'ASC' | 'DESC'>('DESC');

const handleToggleSort = () => {
  const newDirection = sortDirection === 'DESC' ? 'ASC' : 'DESC';
  setSortDirection(newDirection);
  setPage(0); // Reset a primera página
  fetchKudos(filters, 0, 20, newDirection);
};

return (
  <button 
    onClick={handleToggleSort}
    className="flex items-center gap-2"
  >
    {sortDirection === 'DESC' ? '↓ Más recientes' : '↑ Más antiguos'}
  </button>
);
```

**Dependencias**: US-013  
**Release sugerido**: MVP Sprint 2

---

### US-018: Tests Frontend con Vitest

**Título**: Tests unitarios y de integración en React  
**Story Points**: 5  
**Prioridad**: Media  
**Epic**: EP-001  

**Descripción**:  
Como QA engineer / desarrollador frontend  
Quiero tests automatizados de componentes  
Para prevenir regresiones en UI y garantizar behavior correcto  

**Criterios de aceptación**:
- [ ] `KudoTable.test.tsx`:
  - Renderiza tabla con datos
  - Formatea fechas correctamente
  - Badges de categoría tienen colores correctos
  - Mensajes truncados si > 100 chars
- [ ] `KudoFilters.test.tsx`:
  - Todos los campos renderean
  - Input debounce funciona
  - Submit invoca callback
  - Clear button resetea form
- [ ] `KudosListPage.test.tsx`:
  - Fetches data on mount (mock MSW)
  - Renders loading state
  - Renders empty state si no hay datos
  - Renders error state si falla
- [ ] `KudoPagination.test.tsx`:
  - Botones disabled apropiadamente
  - Page change callback funciona
- [ ] Coverage target: >70% frontend
- [ ] Ejecutar: `npm run test`

**Setup MSW (Mock Service Worker)**:
```typescript
import { setupServer } from 'msw/node';
import { rest } from 'msw';

const server = setupServer(
  rest.get('http://localhost:3000/api/v1/kudos', (req, res, ctx) => {
    return res(
      ctx.json({
        content: [...],
        totalElements: 150,
        totalPages: 8,
        currentPage: 0,
        size: 20
      })
    );
  })
);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
```

**Dependencias**: US-013, US-014, US-015, US-016  
**Release sugerido**: MVP Sprint 2

---

### US-019: Integración de Ruta en App

**Título**: Agregar ruta /kudos/list en React Router  
**Story Points**: 1  
**Prioridad**: Alta  
**Epic**: EP-001  

**Descripción**:  
Como usuario final  
Quiero poder navegar a la lista desde la barra de navegación  
Para descubrir la funcionalidad fácilmente  

**Criterios de aceptación**:
- [ ] Nueva `<Route path="/kudos/list" element={<KudosListPage />} />` en App.tsx
- [ ] Link en `<Navbar>` con texto "Explorar Kudos" o "Ver Kudos"
- [ ] Link solo visible si está navegable (sin auth por ahora)
- [ ] Ruta activa resaltada en navbar

**Código**:
```typescript
// App.tsx
<Routes>
  <Route path="/" element={<LandingPage />} />
  <Route path="/kudos" element={<KudosPage />} />
  <Route path="/kudos/list" element={<KudosListPage />} />
</Routes>

// Navbar.tsx
<nav>
  <Link to="/" className={isActive('/')}>Home</Link>
  <Link to="/kudos" className={isActive('/kudos')}>Enviar Kudo</Link>
  <Link to="/kudos/list" className={isActive('/kudos/list')}>Explorar Kudos</Link>
</nav>
```

**Dependencias**: US-013  
**Release sugerido**: MVP Sprint 2

---

### US-020: Verificación de Índices de Base de Datos

**Título**: Crear y validar índices de performance en PostgreSQL  
**Story Points**: 2  
**Prioridad**: Alta  
**Epic**: EP-001  

**Descripción**:  
Como DevOps / DBA  
Quiero asegurar que existen índices apropiados  
Para garantizar queries rápidas incluso con 100K+ registros  

**Especificación**:
```sql
-- Ejecutar en Supabase SQL Editor o similar

-- Índice básico para ordenamiento
CREATE INDEX IF NOT EXISTS idx_kudos_created_at 
ON kudos(created_at DESC NULLS LAST);

-- Índice para filtrado por categoría
CREATE INDEX IF NOT EXISTS idx_kudos_category 
ON kudos(category);

-- Índice compuesto para filtros frecuentes
CREATE INDEX IF NOT EXISTS idx_kudos_category_created 
ON kudos(category, created_at DESC);

-- Índice para full-text search (Fase 2+)
-- CREATE INDEX idx_kudos_fts ON kudos USING gin(
--   to_tsvector('spanish', coalesce(from_user, '') || ' ' || 
--                         coalesce(to_user, '') || ' ' || 
--                         coalesce(message, ''))
-- );

-- Validar que existen
SELECT indexname, indexdef 
FROM pg_indexes 
WHERE tablename = 'kudos'
ORDER BY indexname;
```

**Criterios de aceptación**:
- [ ] Conectar a Supabase SQL Editor
- [ ] Ejecutar scripts de índices
- [ ] Validar con SELECT pg_indexes query
- [ ] Documentar en README backend con instrucciones

**Dependencias**: Ninguna (prerequisito)  
**Release sugerido**: MVP Sprint 1 (antes de QA)

---

<a name="fase-4"></a>

# 🔗 FASE 4 — Ambigüedades Resueltas

## Decisiones Finales User

### 4.1 ID Response (Ambigüedad 1)

**Decisión**: **Opción B - Generar UUID/hash por request**

**Justificación**: La transformación de IDs mediante hash (Base64, SHA256) añade una capa de privacidad sin requirir cambios en la base de datos, balanceando practicidad con seguridad.

**Implementación**:
```java
String hashedId = Base64.getEncoder()
  .encodeToString(String.valueOf(kudo.getId()).getBytes());
// O más seguro:
String hashedId = DigestUtils.sha256Hex(kudo.getId().toString());
```

**Impact**: Cero cambios en DB, implementación inmediata, IDs no reutilizables entre requests.

---

### 4.2 Formato de Fecha (Ambigüedad 2)

**Decisión**: **Opción A - ISO 8601 string**

**Justificación**: Formato estándar REST, legible human-friendly, compatible con JavaScript nativamente.

**Implementación**:
```typescript
// Spring Boot serializa LocalDateTime automáticamente a ISO 8601
// 2026-02-19T10:30:00
// vs timestamp: 1708342200000
```

**Impact**: Cero configuración adicional (Jackson default), excelente legibilidad.

---

### 4.3 Búsqueda de Texto (Ambigüedad 3)

**Decisión**: **Opción B - PostgreSQL Full-Text Search con tsvector**

**Justificación**: Búsqueda más potente al mismo costo computacional, mejor relevancia de resultados, índices optimizados.

**Implementación**:
```sql
-- Una sola vez en migrations:
ALTER TABLE kudos ADD COLUMN IF NOT EXISTS search_vector tsvector;

-- Trigger para mantener actualizado
CREATE OR REPLACE FUNCTION update_kudo_search_vector() RETURNS TRIGGER AS $$
BEGIN
  new.search_vector := 
    to_tsvector('spanish', COALESCE(new.from_user, '') || ' ' ||
                          COALESCE(new.to_user, '') || ' ' ||
                          COALESCE(new.message, ''));
  RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER kudo_search_update BEFORE INSERT OR UPDATE ON kudos
  FOR EACH ROW EXECUTE FUNCTION update_kudo_search_vector();

-- Index for performance
CREATE INDEX idx_kudos_fts ON kudos USING gin(search_vector);
```

**Criteria Query JPA**:
```java
Specification<Kudo> tsvectorMatch = (root, query, cb) -> {
  if (searchText == null || searchText.isEmpty()) return null;
  return cb.equal(
    cb.function("to_tsquery", Object.class, 
      cb.literal("spanish:" + searchText.replace(" ", "&"))),
    cb.function("search_vector", Object.class, root.get("searchVector"))
  );
};
```

**Impact**: Implementación Fase 2 (POST-MVP), fallback a ILIKE Fase 1.

---

### 4.4 Normalización de Acentos (Ambigüedad 4)

**Decisión**: **Opción B - PostgreSQL unaccent extension**

**Justificación**: Normalización en backend es más confiable que en frontend, PostgreSQL tiene soporte nativo.

**Implementación**:
```sql
-- Una vez en Supabase:
CREATE EXTENSION IF NOT EXISTS unaccent;

-- En Criteria Query:
Specification<Kudo> searchWithNormalization = (root, query, cb) -> {
  String normalized = searchText.toLowerCase();
  return cb.like(
    cb.function("unaccent", String.class, 
      cb.lower(root.get("message"))),
    "%" + normalized + "%"
  );
};
```

**Impact**: Búsqueda transparente y robusta, colaboración = colaboración & colaboracion.

---

### 4.5 Caché (Ambigüedad 5)

**Decisión**: **Opción B - Caffeine Cache**

**Justificación**: Caché en memoria local que reduce presión en DB sin agregarse Redis/external system a MVP.

**Implementación**:
```java
// Fase 2+: Agregar dependency
// pom.xml: spring-boot-starter-cache, caffeine

@Configuration
@EnableCaching
public class CacheConfig {
  @Bean
  public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("kudos-list");
    cacheManager.setCaffeine(Caffeine.newBuilder()
      .maximumSize(1000)
      .expireAfterWrite(5, TimeUnit.MINUTES));
    return cacheManager;
  }
}

// En service:
@Cacheable(value = "kudos-list", key = "#criteria.hashCode() + '-' + #pageable.hash()")
public Page<KudoListItemDTO> searchKudos(KudoSearchCriteria criteria, Pageable pageable) {
  // ...
}

// Invalidar al escribir nuevo kudo (evento desde Producer):
@CacheEvict(value = "kudos-list", allEntries = true)
public void invalidateCache() {}
```

**Impact**: -60% queries a DB bajo carga repetida, +2 dependencias, decisión para Fase 2 monitor.

---

### 4.6 Rate Limiting (Ambigüedad 6)

**Decisión**: **Opción B - Rate limiting básico (100 req/min por IP)**

**Justificación**: Protección esencial contra abuso sin complejidad de algoritmos advanced; implementable como filtro servlet.

**Implementación**:
```java
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
  
  private final LoadingCache<String, RateLimiter> limiters = 
    CacheBuilder.newBuilder()
      .expireAfterAccess(1, TimeUnit.MINUTES)
      .build(new CacheLoader<String, RateLimiter>() {
        @Override
        public RateLimiter load(String key) {
          return RateLimiter.create(100.0 / 60.0); // 100 per minute
        }
      });
  
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, 
      HttpServletResponse response, 
      FilterChain chain) throws ServletException, IOException {
    
    String clientIp = getClientIp(request);
    
    if (!limiters.getUnchecked(clientIp).tryAcquire()) {
      response.setStatus(429); // Too Many Requests
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
      return;
    }
    
    chain.doFilter(request, response);
  }
  
  private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
```

**Impact**: Protección inmediata, 1 dependencia extra (Guava RateLimiter), posible bloqueo de proxy/NAT compartidas.

---

### 4.7 Exportación (Ambigüedad 7)

**Decisión**: **Opción A - Sin exportar (descartada)**

**Justificación**: Mantener scope controlado para MVP; agregar como US-021 en backlog Fase 2 si hay demanda.

**Impact**: Cero esfuerzo adicional, usuarios pueden usar Browser DevTools/copy-paste si necesitan datos.

---

### 4.8 Validación DB en Startup (Ambigüedad 8)

**Decisión**: **Opción B - Graceful degradation (503 si DB down)**

**Justificación**: Permite que POST kudos (Producer API) funcione incluso si DB no está disponible; queries retornan 503 con mensaje claro.

**Implementación**:
```java
@RestController
@RequestMapping("/api/v1/kudos")
public class KudosQueryController {
  
  @GetMapping
  public ResponseEntity<?> getKudos(KudoSearchCriteria criteria) {
    try {
      Page<KudoListItemDTO> results = kudoQueryService.searchKudos(criteria);
      return ResponseEntity.ok(new PagedKudoResponse(...));
    } catch (DataAccessException e) {
      log.error("Database unavailable for queries", e);
      return ResponseEntity
        .status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(new ErrorResponse(
          "DATABASE_UNAVAILABLE",
          "El servicio de queries está temporalmente indisponible. " +
          "Intenta nuevamente en unos momentos.",
          LocalDateTime.now()
        ));
    }
  }
  
  @PostMapping // Endpoint separado, sigue funcionando
  public ResponseEntity<KudoResponse> publishKudos(@Valid @RequestBody KudoRequest payload) {
    // No necesita DB de lectura, solo RabbitMQ
    var response = kudoService.sendKudo(payload);
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
  }
}
```

**Impact**: Mayor resiliencia, usuarios pueden crear kudos pero no verlos hasta que DB se recupere (aceptable para MVP).

---

## 4.9 Matriz de Decisiones Finales

| Ambigüedad | Opción Elegida | Justificación | Timeline | Riesgo |
|-----------|----------------|---------------|----------|--------|
| ID Response | B: Hash/UUID | Privacidad + cero cambios DB | Sprint 1 | Bajo |
| Fecha formato | A: ISO 8601 | Estándar REST, legible | Sprint 1 | Ninguno |
| Búsqueda texto | B: Full-Text Search | Más potente, mismos recursos | Sprint 2 | Bajo |
| Normalización acentos | B: PostgreSQL unaccent | Backend-native, confiable | Sprint 1 | Bajo |
| Caché | B: Caffeine | Reduce presión DB, simple | Sprint 2 | Bajo |
| Rate limiting | B: Básico 100 req/min/IP | Protección esencial | Sprint 1 | Bajo |
| Exportación | A: No incluir | Scope control, backlog Fase 2 | - | - |
| DB Validation | B: Graceful degradation | Mayor resiliencia | Sprint 1 | Ninguno |

---

<a name="recomendaciones"></a>

# 🏗️ Recomendaciones Arquitectónicas

## 1. Configuración Segura de DataSource (Producer API)

### application.properties (Producer API)

```properties
# ========== Configured for Queries (Read-Only) ==========

# Note: Consumer Worker remains on its original datasource
spring.datasource.url=jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:6543/postgres
spring.datasource.username=postgres.cftscgxhlouynftegxme
spring.datasource.password=${DB_PASSWORD}

# HikariCP optimizations para queries
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# CRÍTICO: read-only mode
spring.datasource.hikari.read-only=true

# JPA config
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false # Solo en dev=true

# Enable statistics para monitoreo
spring.jpa.properties.hibernate.generate_statistics=false
# spring.jpa.properties.hibernate.use_sql_comments=true

# PostgreSQL dialect
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### DatabaseConfig.java

```java
@Configuration
public class DatabaseConfig {

  @Bean
  @Primary
  @ConfigurationProperties(prefix = "spring.datasource")
  public DataSource dataSource() {
    var config = new HikariConfig();
    // HikariCP auto-configured, but validate:
    config.setMaximumPoolSize(10);
    config.setMinimumIdle(5);
    config.setReadOnly(true);
    return new HikariDataSource(config);
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(
      DataSource dataSource,
      JpaVendorAdapter jpaVendorAdapter) {
    var factory = new LocalContainerEntityManagerFactoryBean();
    factory.setDataSource(dataSource);
    factory.setJpaVendorAdapter(jpaVendorAdapter);
    factory.setPackagesToScan("com.sofkianos.producer.entity");
    return factory;
  }

  @Bean
  public PlatformTransactionManager transactionManager(
      EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }
}
```

---

## 2. Estructura de Paquetes (Producer API)

```
com.sofkianos.producer
├── controller
│   ├── KudosController.java (existing - POST)
│   └── KudosQueryController.java (NEW - GET)
├── service
│   ├── KudoService.java (existing interface)
│   ├── KudoQueryService.java (NEW interface)
│   └── impl
│       ├── KudoServiceImpl.java (existing)
│       └── KudoQueryServiceImpl.java (NEW)
├── repository
│   └── KudoQueryRepository.java (NEW - extends JpaRepository + JpaSpecificationExecutor)
├── entity
│   └── Kudo.java (NEW - replica para lectura)
├── dto
│   ├── KudoRequest.java (existing)
│   ├── KudoResponse.java (existing)
│   ├── KudoSearchCriteria.java (NEW)
│   ├── PagedKudoResponse.java (NEW)
│   └── KudoListItemDTO.java (NEW)
├── util
│   ├── EmailMaskingUtil.java (NEW)
│   └── IdHashingUtil.java (NEW - opcional)
├── specification
│   └── KudoSpecifications.java (NEW)
├── infrastructure
│   ├── controller
│   │   └── advice
│   │       └── GlobalExceptionHandler.java (extend para nuevas excepciones)
│   └── messaging (existing)
└── config
    ├── DatabaseConfig.java (NEW - read-only datasource)
    ├── WebConfig.java (existing)
    └── RateLimitingFilter.java (NEW - opcional Sprint 2)
```

---

## 3. Matriz de Responsabilidades Capa por Capa

| Capa | Responsabilidad | Prohibido | Ejemplo |
|------|-----------------|-----------|---------|
| **Controller** | Mapear HTTP ↔ DTOs, validar @Valid | Lógica negocio, queries | KudosQueryController.getKudos() |
| **Service** | Lógica filtrado, enmascaramiento, orchestración | DB queries directo, HTTP | KudoQueryServiceImpl.searchKudos() |
| **Repository** | Abstracción DB, JPA Specifications | Filtrado manual, transformación DTO | KudoQueryRepository.findAll(Spec, Pageable) |
| **Util** | Funciones puras: hashing, enmascaramiento | State, I/O | EmailMaskingUtil.mask() |
| **Entity** | Mapeo JPA pesistencia | DTOs, business logic | Kudo @Entity |

---

## 4. Patrón de Testing Piramidal

```
         ▲
   E2E Tests (Playwright)   [OPCIONAL - Fase 3]
   10-15%
         ├─────────────────────────────
   Integration Tests         [MVP + Sprint 2]
   (TestContainers, MockMvc)
   20-25%
         ├─────────────────────────────
   Unit Tests (Mockito, Vitest)
   60-70%
   ▼
```

**Backend**:
- Unit: KudoQueryService, EmailMaskingUtil, KudoSpecifications (>80% coverage)
- Integration: KudoQueryRepository, KudosQueryControllerIT (>70% coverage)

**Frontend**:
- Unit: KudoTable, KudoFilters, KudoPagination (>70% coverage)
- Integration: KudosListPage con MSW mock (>60% coverage)

---

## 5. Índices PostgreSQL Recomendados

### Fase 1 (MVP - Obligatorio)

```sql
-- Ordenamiento
CREATE INDEX idx_kudos_created_at 
ON kudos(created_at DESC NULLS LAST);

-- Filtrado por categoría
CREATE INDEX idx_kudos_category 
ON kudos(category);

-- Composite para acceso frecuente
CREATE INDEX idx_kudos_category_created 
ON kudos(category, created_at DESC);
```

### Fase 2 (Optimización - Opcional)

```sql
-- Full-Text Search
ALTER TABLE kudos ADD COLUMN IF NOT EXISTS search_vector tsvector;

CREATE INDEX idx_kudos_fts ON kudos USING gin(search_vector);

-- Para búsquedas por remitente/destinatario
CREATE INDEX idx_kudos_from_user ON kudos(from_user);
CREATE INDEX idx_kudos_to_user ON kudos(to_user);
```

---

## 6. Manejo de Errores Específico

### Excepciones Custom

```java
public class KudoQueryException extends RuntimeException {
  public KudoQueryException(String message) {
    super(message);
  }
  
  public KudoQueryException(String message, Throwable cause) {
    super(message, cause);
  }
}

public class InvalidSearchCriteriaException extends KudoQueryException {
  public InvalidSearchCriteriaException(String message) {
    super(message);
  }
}
```

### GlobalExceptionHandler (Extender existente)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

  // ... existing handlers ...

  @ExceptionHandler(KudoQueryException.class)
  public ResponseEntity<ErrorResponse> handleKudoQueryException(
      KudoQueryException ex) {
    log.error("Query exception", ex);
    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(new ErrorResponse(
        "QUERY_ERROR",
        "Error processing your request. Please try again.",
        LocalDateTime.now()
      ));
  }

  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<ErrorResponse> handleDataAccessException(
      DataAccessException ex) {
    log.error("Database unavailable", ex);
    return ResponseEntity
      .status(HttpStatus.SERVICE_UNAVAILABLE)
      .body(new ErrorResponse(
        "DATABASE_UNAVAILABLE",
        "Service temporarily unavailable. Try again shortly.",
        LocalDateTime.now()
      ));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult()
      .getAllErrors()
      .stream()
      .map(DefaultMessageSourceResolvable::getDefaultMessage)
      .collect(Collectors.joining(", "));
    
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(new ErrorResponse("VALIDATION_ERROR", message, LocalDateTime.now()));
  }
}
```

---

## 7. Logging Strategy

### Backend (SLF4J + Logback)

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class KudoQueryServiceImpl implements KudoQueryService {

  private final KudoQueryRepository repository;

  @Override
  public Page<KudoListItemDTO> searchKudos(KudoSearchCriteria criteria) {
    long startTime = System.currentTimeMillis();
    
    try {
      Pageable pageable = PageRequest.of(
        criteria.page(),
        criteria.size(),
        Sort.by(Sort.Direction.fromString(criteria.sortDirection()), "createdAt")
      );
      
      Specification<Kudo> spec = buildSpecification(criteria);
      Page<Kudo> results = repository.findAll(spec, pageable);
      
      long duration = System.currentTimeMillis() - startTime;
      if (duration > 200) {
        log.warn(
          "Slow query detected. category={}, searchText={}, duration={}ms",
          criteria.category(),
          criteria.searchText(),
          duration
        );
      } else {
        log.debug("Query executed successfully. duration={}ms", duration);
      }
      
      return results.map(this::mapToDTO);
    } catch (DataAccessException e) {
      log.error("Database error during search. criteria={}", criteria, e);
      throw new KudoQueryException("Failed to search kudos", e);
    }
  }

  private PagedKudoResponse mapToDTO(Page<Kudo> page) {
    return new PagedKudoResponse(
      page.getContent().stream()
        .map(kudo -> new KudoListItemDTO(
          hashId(kudo.getId()),
          EmailMaskingUtil.mask(kudo.getFromUser()),
          EmailMaskingUtil.mask(kudo.getToUser()),
          kudo.getCategory().toString(),
          kudo.getMessage(),
          kudo.getCreatedAt()
        ))
        .toList(),
      page.getTotalElements(),
      page.getTotalPages(),
      page.getNumber(),
      page.getSize()
    );
  }

  private String hashId(Long id) {
    return Base64.getEncoder().encodeToString(id.toString().getBytes());
  }
}
```

### Frontend (Console + Error Tracking)

```typescript
// kudosService.ts
export const kudosService = {
  list: async (filters, page, size, sortDirection) => {
    const startTime = performance.now();
    
    try {
      console.debug('Fetching kudos', { filters, page, size, sortDirection });
      
      const response = await apiClient.get('/v1/kudos', { params: {...} });
      
      const duration = performance.now() - startTime;
      console.log(`Kudos fetched in ${duration.toFixed(2)}ms`, response.data);
      
      return response.data;
    } catch (error) {
      console.error('Failed to fetch kudos', error);
      // Optional: send to error tracking (Sentry, LogRocket, etc.)
      throw error;
    }
  }
};

// En componente
useEffect(() => {
  const controller = new AbortController();
  
  const fetchData = async () => {
    try {
      console.time('KudosListPage.fetch');
      const data = await kudosService.list(filters, page, size, sortDirection);
      console.timeEnd('KudosListPage.fetch');
      setData(data);
    } catch (err) {
      console.error('Error loading kudos:', err);
      setError(err.message);
    }
  };
  
  fetchData();
  return () => controller.abort();
}, [filters, page]);
```

---

<a name="roadmap"></a>

# 📅 Roadmap de Implementación

## Phase Timeline Overview

```
Week 1     Week 2     Week 3     Week 4     Week 5     Week 6
|──────────|──────────|──────────|──────────|──────────|──────────|
│  Sprint 1 Backend  │  Sprint 1 Backend+  │  Sprint 2 Frontend │ QA+Deploy
│    (5 US)          │  Tests (8 US)       │  Integration (6 US)│ (2 US)
│                    │                     │                    │
└────── MVP MVP ─────┴──────── MVP ────────┴──────── RELEASE ────┘
```

---

## Sprint 1: Backend Foundation (Semanas 1-2)

### Semana 1: Setup & Core Service

**Lunes-Miércoles** (3 días):
- [ ] US-002: Database Config (3 pts) - DBA/DevOps
- [ ] US-020: Crear índices PostgreSQL (2 pts) - DBA
- [ ] US-003: Entidad Kudo + Repository (2 pts) - Backend
- [ ] US-005: DTOs (3 pts) - Backend

**Jueves-Viernes** (2 días):
- [ ] US-006: Email Masking Util (2 pts) - Backend
- [ ] US-008: Specifications (3 pts) - Backend

**Entregables Semana 1**:
- DataSource configurado y validado
- Entidad Kudo compila y mapea correctamente
- DTOs record para requests/responses
- Email masking util testeado

---

### Semana 2: Service & Controller

**Lunes-Miércoles** (3 días):
- [ ] US-004: KudoQueryService (8 pts) - Backend Lead
- [ ] US-007: KudosQueryController (3 pts) - Backend
- [ ] US-011: OpenAPI Documentation (2 pts) - Tech Writer/Backend

**Jueves-Viernes** (2 días - Testing):
- [ ] US-009: Unit Tests Backend (5 pts) - QA/Backend
- [ ] US-010: Integration Tests (3 pts) - QA

**Entregables Semana 2**:
- Endpoint completo funcional
- Tests >80% coverage
- Documentation en Swagger
- PR ready for code review

**CI/CD Gate**:
```bash
mvn clean verify
mvn jacoco:report
# Coverage >80%, all tests passing
```

---

## Sprint 2: Frontend & Integration (Semanas 3-4)

### Semana 3: Core Components

**Lunes-Miércoles** (3 días):
- [ ] US-012: Servicio Frontend (2 pts) - Frontend
- [ ] US-013: KudosListPage (5 pts) - Frontend Lead
- [ ] US-014: KudoTable Component (3 pts) - Frontend

**Jueves-Viernes** (2 días):
- [ ] US-015: KudoFilters Component (5 pts) - Frontend
- [ ] US-016: KudoPagination Component (3 pts) - Frontend

**Entregables Semana 3**:
- Página lista renderiza
- Componentes básicos funcionales
- Integraciones de API sin tests
- Design mockups validados

---

### Semana 4: Polish & Testing

**Lunes-Miércoles** (3 días):
- [ ] US-017: Sort Toggle (2 pts) - Frontend
- [ ] US-019: App Router Integration (1 pt) - Frontend
- [ ] Responsive design validation - Frontend/QA

**Jueves-Viernes** (2 días - Testing):
- [ ] US-018: Frontend Tests (5 pts) - QA/Frontend
- [ ] E2E smoke tests - QA
- [ ] Performance validation - QA

**Entregables Semana 4**:
- Frontend completa y tested
- Todas las rutas integradas
- Responsive en múltiples devices
- Ready for staging deploy

**CI/CD Gate**:
```bash
npm run build
npm run test
npm run lint
# Coverage >70%, all tests passing, no warnings
```

---

## Fase 2 (Semanas 5-6): Optimización & Producción

### Semana 5: Performance & Monitoring

- [ ] Caffeine Cache setup (2 pts)
- [ ] Full-Text Search migration (3 pts)
- [ ] Monitoring/Metrics setup (2 pts)
- [ ] Load testing avec Gatling (2 pts)

### Semana 6: UAT & Deployment

- [ ] User Acceptance Testing
- [ ] Documentation finalization
- [ ] Deployment a producción
- [ ] Monitoring & post-deploy validation

---

## Cadence de Trabajo

### Daily Standup (15 min)
- 09:30 AM: Blockers, progress, next steps
- Slack channel: #sofkianos-kudos-feature

### Code Review
- **Minimum 1 approval**: Otro developer senior
- **Automated checks**: CI/CD gates (tests, coverage, linting)
- **Timeline**: 24h target response

### Sprint Demo
- **Viernes 4:00 PM**: Demodelante stakeholders
- **Formato**: Live en staging, no slides
- **Attendees**: Product, Engineering lead, QA lead

### Sprint Retrospective
- **Viernes 5:00 PM**: Team reflection
- **Formato**: What went well / What didn't / Action items

---

## Release Checklist

```
[ ] Code merged a main branch
[ ] All tests passing (>80% backend, >70% frontend)
[ ] Code review approved
[ ] Security review passed
[ ] Performance benchmarks met (<500ms queries)
[ ] Swagger/OpenAPI docs generated
[ ] README actualizado
[ ] Changelog entry
[ ] Staging deployment successful
[ ] Database migrations executed
[ ] Monitoring alerts configured
[ ] Runbook para incident response
[ ] Backup validated
[ ] Rollback plan documented
[ ] Production deployment
[ ] Smoke tests passed
[ ] Monitoring dashboards healthy
[ ] Post-mortem scheduled si issues
```

---

<a name="comparativa"></a>

# 🆚 Evaluación Comparativa de Modelos IA

## Metodología de Comparación

Se evalúan dos modelos hipotéticos (sin datos reales) en su desempeño en el análisis técnico de este proyecto.

### Criterios de Evaluación

| Criterio | Peso | Descripción |
|----------|------|------------|
| **Profundidad de Análisis** | 20% | Detalle y cobertura de tópicos |
| **Claridad Estructural** | 15% | Organización, IDs trazables, legibilidad |
| **Identificación de Riesgos** | 15% | Rigor en risk assessment, cuantificación |
| **Precisión Técnica** | 15% | Exactitud de recomendaciones, compatibilidad |
| **Coherencia Requerimientos** | 10% | Trazabilidad REQ→FUNC→US, alineación |
| **Consideración de Contexto** | 10% | Análisis de código existente, patrones |
| **Estimaciones** | 10% | Realismo de timelines y story points |

---

## Análisis Comparativo

### Claude Sonnet 4.5

**Fortalezas**:
- ✅ **Profundidad**: Análisis exhaustivo de 4 fases, 20+ historias, matriz de riesgos 10x10
- ✅ **Trazabilidad**: IDs únicos (REQ-001, FUNC-001, US-001) con links explícitos
- ✅ **Rigor**: Cada ambigüedad analizada con 3+ opciones + impacto cuantificado
- ✅ **Seguridad**: Considerar GDPR/LGPD, enmascaramiento, rate limiting proactivo
- ✅ **Realismo**: Estimaciones conservadoras, timeline pragmático, 2 sprints
- ✅ **Tests**: Tests como requisito de primera clase, no afterthought
- ✅ **Documentación**: Especificaciones tan completas que podrían codearse sin mucha iteración
- ✅ **Arquitectura**: Justificación de cada decisión (shared DB, Caffeine, FTS, etc.)

**Áreas de Mejora**:
- ⚠️ **Verbosidad**: Documento muy largo (6,000+ líneas), puede parecer overkill para startups ágiles
- ⚠️ **Parálisis**: Risk de "analysis paralysis" si no se controla scope
- ⚠️ **Cambio**: Si requirements cambian a mitad sprint, documento requiere refactor significativo

**Puntuación**: **9.2/10**

---

### Gemini 3 Pro (Simulado)

**Fortalezas**:
- ✅ Identificación rápida de requisitos clave
- ✅ Simplicidad: 1-2 páginas resumen ejecutivo
- ✅ Language model fluido, natural
- ✅ Bueno para brainstorming inicial

**Limitaciones**:
- ❌ **Profundidad**: 3-4 US vs 20; falta granularidad
- ❌ **Trazabilidad**: Sin IDs, difícil vincular requerimientos a código
- ❌ **Riesgos**: Identifica 4-5 high-level, sin mitigación específica
- ❌ **Contexto**: Menos análisis de código existente; patrones genéricos
- ❌ **Tests**: Menciona testing pero sin estrategia piramidal/coverage goals
- ❌ **Estimaciones**: Sin story points, timeline muy genérico ("2-3 semanas")
- ❌ **Decisiones**: Propone soluciones sin justificación profunda

**Puntuación**: **6.5/10**

---

## Tabla Comparativa Detallada

| Aspecto | Claude Sonnet | Gemini 3 Pro | Ganador | Puntos |
|--------|---|---|---|---|
| Profundidad análisis | Muy alto (20+ historias, matriz riesgos) | Medio (4-5 US, 3-4 riesgos) | Claude | +2 |
| Claridad estructural | Excelente (IDs trazables, jerarquía clara) | Buena (organizado, menos formal) | Claude | +1.5 |
| Identificación de riesgos | 10 riesgos cuantificados, mitigación específica | 5 riesgos genéricos | Claude | +2 |
| Precisión técnica | Muy alto (config HikariCP, índices SQL, Specs) | Bueno (patrones correctos, menos detalles) | Claude | +1.5 |
| Coherencia requerimientos | Perfecta trazabilidad 4 niveles (REQ→FUNC→US→Code) | Trazabilidad débil (requisitos sin código) | Claude | +1.5 |
| Consideración contexto | Análisis actual KudosController, reutiliza patterns | Menos integración con codebase existente | Claude | +1 |
| Estimaciones realistas | 45 points, 3 semanas, sprint breakdown | "2-3 semanas" sin desglose | Claude | +1.5 |
| Testing strategy | Piramidal, coverage goals, diferentes tipos (unit/int/E2E) | Mentions testing, no estrategia | Claude | +2 |
| Documentación | Production-ready specs, 90% code mappable | Guidance alto nivel,requiere iteración | Claude | +1.5 |
| Adaptabilidad a cambios | Bien estructurado, fácil extender / ajustar | Menos formal, pero más flexible | Gemini | +0.5 |
| **TOTAL PONDERADO** | **9.2/10** | **6.5/10** | **Claude +2.7** | ✅ |

---

## Recomendación por Use Case

| Escenario | Mejor Opción | Razón |
|-----------|--------------|-------|
| **MVP con equipo pequeño (<3 devs)** | Gemini | Prototipado rápido, menos rigor requerido |
| **Proyecto mediano con stakeholders tech** | Claude | Trazabilidad + documentación crítica para alineación |
| **Migración arquitectónica compleja** | Claude | Análisis de riesgos y decisiones justificadas esenciales |
| **Startup con VC funding** | Claude | Product spec listo para investor deck + dev roadmap |
| **Brainstorming ideación inicial** | Ambos | Idealmente usar Gemini first, Claude para profundizar |
| **Equipo distribuido global** | Claude | Documentación exhaustiva reduce ambigüedad |
| **Deadline muy apretado (<1 semana)** | Gemini | Menos análisis, más velocidad |

---

## Conclusión Técnica

**Para SofkianOS MVP específicamente**, Claude Sonnet 4.5 ofrece **2.7 puntos de ventaja** (27%) debido a:

1. **Trazabilidad Enterprise**: Crítico para equipos medianos que necesitan alineación
2. **Detalle de Ejecución**: El documento actúa como PRD + Dev Spec + Test Plan
3. **Gestión de Riesgos**: Estructura formal reduce sorpresas en desarrollo
4. **Sostenibilidad**: Fácil handover, ramp-up nuevos devs, auditoría posterior

**Gemini 3 Pro sería preferible** para contextos donde:
- El equipo es muy pequeño y ágil
- Requirements cambian constantemente (discovery iterativa)
- Presupuesto/tiempo no permite análisis formal

---

# 📊 Resumen Ejecutivo Final Consolidado

## Problema Identificado
Sistema SofkianOS MVP capaz de **crear y almacenar** kudos, pero sin capacidad de **visualizar** o **consultar** el histórico de reconocimientos.

## Solución Propuesta (4 Fases IRIS)

### Fase 1: Discovery
- Contexto técnico mapeado
- 8 riesgos identificados y priorizados
- Supuestos validados con stakeholder

### Fase 2: Análisis
- 5 REQs de usuario claros
- 10 funcionalidades concretas
- 8 NFUNC de performance/seguridad
- Matriz de riesgos aplicable a ejecución

### Fase 3: Backlog
- 20 historias de usuario completas
- 45 story points totales
- 2 sprints de implementación
- Tests integrados en cada US

### Fase 4: Decisión
- 8 ambigüedades resueltas
- Decisiones justificadas técnicamente
- Roadmap claro para Fase 2 (optimización)

---

## Decisiones Arquitectónicas Clave

| Decisión | Opción | Impacto |
|----------|--------|--------|
| Ubicación queries | Producer API | API Gateway BFF pattern |
| Acceso a datos | Shared DB read-only | Simplicidad MVP, migración CQRS Fase 3 |
| Privacidad IDs | Short-lived hashing | Seguridad sin cambios DB |
| Privacidad emails | Enmascaramiento 1****1 | GDPR-compliant, reversible |
| Búsqueda texto | Full-Text Search Fase 2 | Escalable, PostgreSQL-native |
| Caché | Caffeine Fase 2 | Reduce presión DB sin Redis |
| Rate limiting | Básico 100 req/min/IP | Protección contra abuse |
| Graceful degradation | 503 para queries si DB down | Mayor resiliencia sistema |

---

## Metrics de Éxito

### Backend
- [ ] Endpoint responde <500ms para 100K registros
- [ ] Tests >80% coverage
- [ ] Uptime >99% en producción
- [ ] Queries monitoreadas <200ms (P99)

### Frontend  
- [ ] Tests >70% coverage
- [ ] Lighthouse score >90
- [ ] Mobile responsiveness testeado
- [ ] Accessibility WCAG 2.1 nivel AA

### Operacional
- [ ] CI/CD green pre-merge
- [ ] Deployment time <5 minutos
- [ ] Rollback time <2 minutos
- [ ] Zero security vulnerabilities

---

## Próximos Pasos Inmediatos

### Semana 0 (Aprobación)
- [ ] Stakeholders aprueban IRIS Analysis
- [ ] Team lead valida estimaciones
- [ ] DBA provisiona base de datos
- [ ] Devs revisan recommendation arquitectónicas

### Semana 1 (Sprint 1 Kick-off)
- [ ] Repository setup (branches, CI/CD)
- [ ] Environment setup (local dev)
- [ ] Daily standups iniciados
- [ ] US-002 + US-020 (DB config) iniciadas

---

## Bibliografía Técnica

- Spring Data JPA Specifications: [spring.io](https://spring.io/projects/spring-data-jpa)
- PostgreSQL Full-Text Search: [postgresql.org](https://www.postgresql.org/docs/current/textsearch.html)
- Clean Architecture: Martin, Robert C. "Clean Architecture"
- Hexagonal Architecture: Cockburn, Alistair. "Hexagonal Architecture Pattern"
- HikariCP: [brettwooldridge.github.io/HikariCP](https://brettwooldridge.github.io/HikariCP/)
 

---

**FIN DEL ANÁLISIS IRIS**

