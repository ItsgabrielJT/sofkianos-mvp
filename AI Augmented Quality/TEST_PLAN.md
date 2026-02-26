# Estrategia de Calidad: Diseño de Pruebas

## Teoría Aplicada: Principio Fundamental

Tras auditar el sistema distribuido (RabbitMQ + Arquitectura Hexagonal), se ha determinado el principio rector para el diseño de pruebas.

### Principio Seleccionado: Las pruebas dependen del contexto

### Justificación

En un entorno asíncrono y desacoplado, la calidad no se limita a la lógica funcional, sino a la **integridad de los datos** a lo largo de su ciclo de vida.

El incidente del **“Kudo Fantasma”** (fallo de serialización) demuestra que el riesgo real reside en los *bordes del sistema* y en cómo los datos sobreviven al transporte entre microservicios.

---

## Definición de Niveles de Prueba

### Diseño del Nivel Unitario

El enfoque principal estará en el componente **`Kudo.Builder`** dentro del *Core Domain*.

Este componente actúa como el **“portero” del sistema**:
- Si una regla de negocio falla aquí, el proceso se detiene inmediatamente
- Se evita que datos inválidos contaminen la infraestructura
- Se protege la consistencia del dominio, independientemente del origen del mensaje

Al validar primero este nivel, garantizamos que el sistema falle **rápido, barato y de forma explícita**, alineado con los principios de TDD y Arquitectura Hexagonal.

# 🧪 Plan de Pruebas — Endpoint de Listado Público de Kudos

**Fecha de creación**: 20 de febrero de 2026  
**Historia(s) base**: US-001, US-002, US-004, US-006, US-007, US-012, US-013

---

## 📋 Índice de Tests

| Completado | ID Test | Capa | Prioridad | Historia | Descripción |
|------------|---------|------|------------|----------|-------------|
| ☐ | TC-001 | Backend | CRÍTICA | US-001 | Endpoint GET retorna estructura paginada correcta |
| ☐ | TC-002 | Backend | CRÍTICA | US-001 | Validación de límites en parámetros size y sortDirection |
| ☑ | TC-003 | Backend | CRÍTICA | US-002 | Conexión read-only a PostgreSQL sin escrituras |
| ☐ | TC-004 | Backend | CRÍTICA | US-006 | Enmascaramiento de emails en fromUser y toUser |
| ☐ | TC-005 | Backend | CRÍTICA | US-007 | Manejo de errores y mapeo de estados HTTP |
| ☐ | TC-006 | Backend | CRÍTICA | US-004 | Filtros dinámicos construyen queries correctas |
| ☐ | TC-007 | Backend | ALTA | US-001, US-004 | Paginación y ordenamiento respetan límites |
| ☐ | TC-008 | Frontend | CRÍTICA | US-013 | Renderizado de KudosListPage con datos reales |
| ☐ | TC-009 | Frontend | CRÍTICA | US-013 | Manejo de estados loading, error y empty |
| ☐ | TC-010 | Frontend | CRÍTICA | US-012, US-013 | Integración con backend y mapeo de errores HTTP |
| ☐ | TC-011 | Frontend | ALTA | US-013 | Filtros invocan servicio con query params correctos |
| ☐ | TC-012 | Frontend | ALTA | US-013, US-016 | Navegación de paginación preserva filtros activos |

---

## 🔵 Pruebas Backend

### TC-001 — Endpoint GET retorna estructura paginada correcta

- **ID del Test**: TC-001
- **Capa**: Backend
- **Prioridad**: CRÍTICA
- **Historia asociada**: US-001
- **Descripción**: Validar que el endpoint GET /api/v1/kudos retorna 200 OK con estructura de respuesta correcta y todos los campos obligatorios presentes.
- **Riesgo cubierto**: Exponer datos incompletos o estructura inválida que rompa el contrato API con frontend.
- **Precondiciones**: 
  - Base de datos contiene al menos 25 kudos registrados
  - Producer API está corriendo en puerto 8082
  - Conexión a PostgreSQL establecida
- **Postcondiciones**: Respuesta retorna datos sin modificar estado de base de datos.

#### Escenario (Gherkin)

```gherkin
Given la base de datos contiene 25 kudos activos
When ejecuto GET /api/v1/kudos sin parámetros
Then el status code es 200 OK
And la respuesta contiene fields: content, totalElements, totalPages, currentPage, size
And content es un array con 20 items (default page size)
And cada item contiene: id, fromUser, toUser, category, message, createdAt
And createdAt está en formato ISO 8601 UTC
And los items están ordenados DESC por createdAt
```

#### Partición de Equivalencia

| Grupo | Valores | Tipo |
|-------|---------|------|
| Sin parámetros | GET /api/v1/kudos | Válido |
| Parámetros default | page=0, size=20, sortDirection=DESC | Válido |
| Base de datos vacía | 0 kudos registrados | Válido - retorna content vacío |
| Base de datos poblada | 1-50 kudos | Válido |
| Base de datos grande | >1000 kudos | Válido - valida performance |

#### Valores Límite

| Valor | Contexto | Resultado Esperado |
|-------|----------|-------------------|
| 0 kudos | Base de datos vacía | content=[], totalElements=0, totalPages=0 |
| 1 kudo | Base de datos con 1 registro | content=[1 item], totalElements=1, totalPages=1 |
| 20 kudos | Justo 1 página completa | content=[20 items], totalPages=1 |
| 21 kudos | Más de 1 página | content=[20 items], totalPages=2 |
| 1000 kudos | Dataset grande | content=[20 items], totalPages=50, query <200ms |

#### Tabla de Decisión

| BD vacía | Solicitud válida | content | totalElements | Status |
|----------|------------------|---------|---------------|--------|
| Sí | Sí | [] | 0 | 200 |
| No | Sí | [20 items] | N | 200 |

---

### TC-002 — Validación de límites en parámetros size y sortDirection

- **ID del Test**: TC-002
- **Capa**: Backend
- **Prioridad**: CRÍTICA
- **Historia asociada**: US-001, US-007
- **Descripción**: Validar que parámetros size y sortDirection cumplen reglas de negocio (size max 50, sortDirection solo ASC/DESC) y rechazan valores inválidos con 400 Bad Request.
- **Riesgo cubierto**: Permitir queries masivas (size=10000) que generen problemas de memoria o permitir SQL injection via sortDirection inválido.
- **Precondiciones**: 
  - Endpoint configurado con validaciones Jakarta Validation
  - Base de datos contiene al menos 100 kudos

#### Escenario (Gherkin)

```gherkin
Given el endpoint tiene validación @Min(1) @Max(50) en size
When ejecuto GET /api/v1/kudos?size=0
Then el status code es 400 Bad Request
And el mensaje de error indica "size debe estar entre 1 y 50"

Given el endpoint valida sortDirection con pattern ASC|DESC
When ejecuto GET /api/v1/kudos?sortDirection=INVALID
Then el status code es 400 Bad Request
And el mensaje indica "sortDirection debe ser ASC o DESC"

When ejecuto GET /api/v1/kudos?size=25&sortDirection=ASC
Then el status code es 200 OK
And retorna 25 items ordenados ASC por createdAt
```

#### Partición de Equivalencia

| Grupo | Valores | Tipo |
|-------|---------|------|
| size válido | 1-50 | Válido |
| size = 0 | 0 | Inválido - menor que mínimo |
| size < 0 | -1, -10 | Inválido - negativo |
| size > 50 | 51, 100, 1000 | Inválido - excede máximo |
| sortDirection válido | ASC, DESC | Válido |
| sortDirection inválido | INVALID, asc, desc, null | Inválido - no coincide pattern |

#### Valores Límite

| Valor | Contexto | Resultado Esperado |
|-------|----------|-------------------|
| size=1 | Límite inferior válido | 200 OK, retorna 1 item |
| size=50 | Límite superior válido | 200 OK, retorna 50 items |
| size=0 | Justo debajo del mínimo | 400 Bad Request |
| size=51 | Justo arriba del máximo | 400 Bad Request |
| sortDirection=ASC | Valor válido 1 | 200 OK, orden ascendente |
| sortDirection=DESC | Valor válido 2 | 200 OK, orden descendente |
| sortDirection="" | String vacío | 400 Bad Request |

---

### TC-003 — Conexión read-only a PostgreSQL sin escrituras

- **ID del Test**: TC-003
- **Capa**: Backend
- **Prioridad**: CRÍTICA
- **Historia asociada**: US-002
- **Descripción**: Validar que Producer API conecta a PostgreSQL en modo read-only y rechaza cualquier operación de escritura (INSERT, UPDATE, DELETE).
- **Riesgo cubierto**: Producer API modifica accidentalmente datos que deberían ser gestionados únicamente por Consumer Worker, generando inconsistencias.
- **Precondiciones**: 
  - DataSource configurada con read-only=true en application.properties
  - spring.jpa.hibernate.ddl-auto=none
  - Base de datos contiene datos de prueba

#### Escenario (Gherkin)

```gherkin
Given Producer API está configurada con DataSource read-only=true
When la aplicación Spring Boot inicia
Then los logs confirman "Database connection successful"
And el connection pool HikariCP se inicializa con maxPoolSize=10

Given intento ejecutar kudoRepository.save(newKudo)
When el método de escritura es invocado
Then lanza exception org.springframework.dao.DataAccessException
And el mensaje indica "connection is read-only"
And la base de datos no tiene cambios

When ejecuto kudoRepository.findAll()
Then la operación de lectura es exitosa
And retorna datos sin errores
```

#### Partición de Equivalencia

| Grupo | Operación | Tipo |
|-------|-----------|------|
| Lectura SELECT | findAll(), findById() | Válido - permitido |
| Escritura INSERT | save(newEntity) | Inválido - debe fallar |
| Escritura UPDATE | save(existingEntity) | Inválido - debe fallar |
| Escritura DELETE | delete(entity) | Inválido - debe fallar |

#### Valores Límite

| Valor | Contexto | Resultado Esperado |
|-------|----------|-------------------|
| findAll() primera ejecución | Lectura inicial | Conexión exitosa, retorna datos |
| save() en read-only | Intento de escritura | DataAccessException lanzada |
| Conexión con credenciales incorrectas | Configuración inválida | Falla al iniciar aplicación |

---

### TC-004 — Enmascaramiento de emails en fromUser y toUser

- **ID del Test**: TC-004
- **Capa**: Backend
- **Prioridad**: CRÍTICA
- **Historia asociada**: US-006
- **Descripción**: Validar que EmailMaskingUtil.mask() enmascara correctamente emails en respuestas API, protegiendo información personal mientras mantiene contexto.
- **Riesgo cubierto**: Exponer emails completos de usuarios, violando privacidad y potencialmente GDPR/LOPD.
- **Precondiciones**: 
  - EmailMaskingUtil implementado
  - KudoQueryService aplica enmascaramiento en mapeo a DTOs

#### Escenario (Gherkin)

```gherkin
Given un kudo con fromUser="juan.perez@sofkianos.com"
When el servicio mapea entity a KudoListItemDTO
Then el DTO contiene fromUser="j***z@sofkianos.com"

Given un kudo con toUser="a@domain.com"
When se aplica enmascaramiento
Then el resultado es "a***a@domain.com"

Given un kudo con email inválido "notanemail"
When se aplica enmascaramiento
Then el resultado es "***@***.com" (fallback seguro)

Given un kudo con toUser=null
When se aplica enmascaramiento
Then el resultado es null (no modifica null)
```

#### Partición de Equivalencia

| Grupo | Valores | Tipo |
|-------|---------|------|
| Email normal | juan.perez@domain.com | Válido - enmascara correctamente |
| Email corto (1-2 chars) | a@d.com, ab@d.com | Válido - caso especial |
| Email largo (50+ chars) | verylongemailaddress@domain.com | Válido - enmascara igual |
| Email sin @ | notanemail | Inválido - fallback seguro |
| Null | null | Válido - retorna null |
| String vacío | "" | Inválido - fallback seguro |

#### Valores Límite

| Valor | Contexto | Resultado Esperado |
|-------|----------|-------------------|
| a@d.com | Email mínimo válido | a***a@d.com |
| juan.perez@sofkianos.com | Email típico empresarial | j***z@sofkianos.com |
| verylonglocalpart123456789@domain.com | Email muy largo | v***9@domain.com |
| @domain.com | Sin local-part | ***@***.com |
| user@ | Sin dominio | ***@***.com |
| null | Null explícito | null |

---

### TC-005 — Manejo de errores y mapeo de estados HTTP

- **ID del Test**: TC-005
- **Capa**: Backend
- **Prioridad**: CRÍTICA
- **Historia asociada**: US-007
- **Descripción**: Validar que el controller NO maneja excepciones localmente y que @RestControllerAdvice mapea correctamente excepciones a estados HTTP apropiados.
- **Riesgo cubierto**: Controller expone stack traces completos, devuelve 500 genéricos sin información útil, o suprime errores silenciosamente.
- **Precondiciones**: 
  - @RestControllerAdvice global configurado
  - Controller delega completamente a service sin try-catch

#### Escenario (Gherkin)

```gherkin
Given el controller no contiene bloques try-catch
When el servicio lanza IllegalArgumentException("Invalid category")
Then el @RestControllerAdvice captura la excepción
And retorna 400 Bad Request
And el body contiene mensaje de error estructurado sin stack trace

Given la base de datos está inaccesible
When se ejecuta GET /api/v1/kudos
Then el servicio lanza DataAccessException
And el @RestControllerAdvice mapea a 503 Service Unavailable
And el mensaje indica "Servicio temporalmente no disponible"

When se ejecuta GET /api/v1/kudos con parámetros válidos
Then el flujo es exitoso sin excepciones
And retorna 200 OK con datos
```

#### Partición de Equivalencia

| Grupo | Excepción | Estado HTTP esperado |
|-------|-----------|---------------------|
| Validación fallida | IllegalArgumentException | 400 Bad Request |
| Recurso no encontrado | ResourceNotFoundException | 404 Not Found |
| Error de BD | DataAccessException | 503 Service Unavailable |
| Violación de regla | BusinessRuleViolationException | 422 Unprocessable Entity |
| Sin error | Flujo normal | 200 OK |

#### Valores Límite

| Valor | Contexto | Resultado Esperado |
|-------|----------|-------------------|
| BD desconectada | DataAccessException al query | 503 Service Unavailable |
| Parámetro inválido | IllegalArgumentException | 400 Bad Request |
| Solicitud válida | Sin excepciones | 200 OK |

#### Tabla de Decisión

| Controller try-catch | Service lanza excepción | Advice captura | Status HTTP | Stack trace visible |
|---------------------|------------------------|----------------|-------------|---------------------|
| No | Sí | Sí | 4xx/5xx apropiado | No |
| Sí | Sí | No | Genérico 500 | Sí (MAL) |
| No | No | N/A | 200 OK | N/A |

---

### TC-006 — Filtros dinámicos construyen queries correctas

- **ID del Test**: TC-006
- **Capa**: Backend
- **Prioridad**: CRÍTICA
- **Historia asociada**: US-004, US-008
- **Descripción**: Validar que KudoSpecifications construye predicates JPA correctos basados en criterios opcionales sin generar queries incorrectas.
- **Riesgo cubierto**: Filtros ignorados (retorna todos los datos siempre), queries con SQL incorrecto que fallan, o filtros que generan resultados erróneos.
- **Precondiciones**: 
  - Base de datos contiene kudos con categorías: Innovation (10), Teamwork (15), Passion (8), Mastery (12)
  - Kudos con fechas variadas entre 2025-01-01 y 2026-02-20

#### Escenario (Gherkin)

```gherkin
Given la base de datos contiene 45 kudos distribuidos en 4 categorías
When ejecuto GET /api/v1/kudos?category=TEAMWORK
Then el query SQL incluye WHERE category = 'TEAMWORK'
And retorna solo 15 kudos con category=TEAMWORK
And no incluye kudos de otras categorías

Given kudos con fechas entre 2026-01-01 y 2026-02-20
When ejecuto GET /api/v1/kudos?startDate=2026-02-01&endDate=2026-02-10
Then el query incluye WHERE created_at BETWEEN '2026-02-01' AND '2026-02-10'
And retorna solo kudos en ese rango (inclusive)

Given kudos con message conteniendo "proyecto"
When ejecuto GET /api/v1/kudos?searchText=proyecto
Then el query aplica ILIKE '%proyecto%'
And retorna kudos cuyo message contiene "proyecto" (case insensitive)

When ejecuto GET /api/v1/kudos sin filtros
Then el query es SELECT * FROM kudos ORDER BY created_at DESC
And retorna todos los 45 kudos
```

#### Partición de Equivalencia

| Grupo | Filtros aplicados | Tipo |
|-------|------------------|------|
| Sin filtros | Ninguno | Válido - retorna todos |
| Solo categoría | category=TEAMWORK | Válido - filtra por categoría |
| Solo fechas | startDate + endDate | Válido - filtra rango |
| Solo texto | searchText=proyecto | Válido - full-text search |
| Múltiples filtros | category + startDate + endDate + searchText | Válido - AND lógico |
| Categoría inválida | category=INVALID | Inválido - 400 Bad Request |

#### Valores Límite

| Valor | Contexto | Resultado Esperado |
|-------|----------|-------------------|
| category=null | Sin filtro categoría | Ignora filtro, retorna todos |
| startDate sin endDate | Solo fecha inicio | Ignora filtro (requiere ambas) |
| searchText="" | String vacío | Ignora filtro |
| searchText con acentos | "reconócimiento" | Normaliza y busca correctamente |

---

### TC-007 — Paginación y ordenamiento respetan límites

- **ID del Test**: TC-007
- **Capa**: Backend
- **Prioridad**: ALTA
- **Historia asociada**: US-001, US-004
- **Descripción**: Validar que paginación retorna exactamente el número de items solicitado, respeta el ordenamiento por fecha, y calcula correctamente totalPages.
- **Riesgo cubierto**: Paginación incorrecta que omite o duplica registros, ordenamiento inconsistente, o cálculos erróneos de totalPages.
- **Precondiciones**: 
  - Base de datos contiene exactamente 47 kudos con fechas incrementales

#### Escenario (Gherkin)

```gherkin
Given la base de datos contiene 47 kudos
When ejecuto GET /api/v1/kudos?page=0&size=20
Then retorna content con 20 items
And totalElements es 47
And totalPages es 3
And currentPage es 0

When ejecuto GET /api/v1/kudos?page=2&size=20
Then retorna content con 7 items (última página)
And totalPages es 3
And currentPage es 2

When ejecuto GET /api/v1/kudos?page=0&size=20&sortDirection=DESC
Then el primer item tiene la fecha más reciente
And el último item (position 19) tiene fecha anterior al primero

When ejecuto GET /api/v1/kudos?page=0&size=20&sortDirection=ASC
Then el primer item tiene la fecha más antigua
And los items están en orden cronológico ascendente
```

#### Partición de Equivalencia

| Grupo | Valores | Tipo |
|-------|---------|------|
| Primera página completa | page=0, size=20 | Válido |
| Última página parcial | page=2, size=20 (7 items) | Válido |
| Página fuera de rango | page=99 | Válido - retorna content vacío |
| Ordenamiento ASC | sortDirection=ASC | Válido |
| Ordenamiento DESC | sortDirection=DESC | Válido |

#### Valores Límite

| Valor | Contexto | Resultado Esperado |
|-------|----------|-------------------|
| page=0, 47 kudos | Primera página | 20 items, totalPages=3 |
| page=2, 47 kudos | Última página | 7 items, totalPages=3 |
| page=3, 47 kudos | Página inexistente | content=[], totalElements=47 |
| size=1 | Paginación mínima | 1 item por página, totalPages=47 |
| size=50 | Paginación máxima | Todos los 47 items en 1 página |

---

## 🟢 Pruebas Frontend

### TC-008 — Renderizado de KudosListPage con datos reales

- **ID del Test**: TC-008
- **Capa**: Frontend
- **Prioridad**: CRÍTICA
- **Historia asociada**: US-013
- **Descripción**: Validar que KudosListPage renderiza correctamente la tabla con datos obtenidos del backend, mostrando todos los campos esperados.
- **Riesgo cubierto**: Componente no renderiza, muestra datos incorrectos, o estructura HTML rota que impide uso.
- **Precondiciones**: 
  - Backend retorna 200 OK con 20 kudos
  - MSW mock configurado para tests
  - Vitest + React Testing Library configurado

#### Escenario (Gherkin)

```gherkin
Given el backend retorna PagedKudoResponse con 20 kudos
When el componente KudosListPage se monta
Then se ejecuta fetch a /api/v1/kudos
And se renderiza tabla con 20 filas
And cada fila muestra: fromUser, toUser, category, message, createdAt
And emails están enmascarados (j***z@domain.com)
And fechas están formateadas ("20 feb 2026, 10:30")
And categorías muestran badges con colores correctos

When verifico badges de categoría
Then Innovation tiene color azul
And Teamwork tiene color verde
And Passion tiene color rojo
And Mastery tiene color amarillo
```

#### Partición de Equivalencia

| Grupo | Datos del backend | Tipo |
|-------|------------------|------|
| 20 items | Response completa | Válido - renderiza tabla |
| 1 item | Response mínima | Válido - renderiza 1 fila |
| 0 items | content=[] | Válido - muestra EmptyState |
| Error 500 | Backend falla | Válido - muestra ErrorState |

#### Valores Límite

| Valor | Contexto | Resultado Esperado |
|-------|----------|-------------------|
| 0 kudos | Base datos vacía | EmptyState renderizado |
| 1 kudo | Mínimo dataset | 1 fila en tabla |
| 20 kudos | Página completa | 20 filas renderizadas |
| Message > 100 chars | Mensaje largo | Texto truncado con "..." |

---

### TC-009 — Manejo de estados loading, error y empty

- **ID del Test**: TC-009
- **Capa**: Frontend
- **Prioridad**: CRÍTICA
- **Historia asociada**: US-013
- **Descripción**: Validar que KudosListPage maneja correctamente estados de carga, error y respuesta vacía mostrando feedback visual apropiado.
- **Riesgo cubierto**: Usuario ve pantalla en blanco sin feedback, no sabe si hay error o está cargando, o no puede reintentar después de error.
- **Precondiciones**: 
  - MSW configurado para simular loading delay, error 500, y response vacía

#### Escenario (Gherkin)

```gherkin
Given el componente se monta
When el fetch está en progreso (loading=true)
Then se muestra SkeletonLoaders (shimmer effect)
And no se muestra tabla ni error

Given el backend retorna error 500
When el estado error se actualiza
Then se oculta SkeletonLoaders
And se muestra ErrorMessage component
And el mensaje incluye botón "Reintentar"

When el usuario hace click en "Reintentar"
Then se ejecuta nuevo fetch
And loading state se activa nuevamente

Given el backend retorna content=[]
When loading=false y data.content.length === 0
Then se muestra EmptyState component
And el mensaje es "No se encontraron kudos"
And no se muestra ErrorMessage
```

#### Partición de Equivalencia

| Grupo | Estado | Tipo |
|-------|--------|------|
| Cargando | loading=true | Válido - muestra skeleton |
| Error | error !== null | Válido - muestra error message |
| Vacío | data.content=[] | Válido - muestra empty state |
| Exitoso | data.content.length > 0 | Válido - muestra tabla |

#### Valores Límite

| Valor | Contexto | Resultado Esperado |
|-------|----------|-------------------|
| loading=true | Fetch en progreso | SkeletonLoaders visible |
| error="Network error" | Backend inaccesible | ErrorMessage con texto del error |
| content=[] | Sin resultados | EmptyState visible |
| content=[1 item] | Mínimo exitoso | Tabla con 1 fila |

#### Tabla de Decisión

| loading | error | content.length | Componente visible |
|---------|-------|----------------|-------------------|
| true | null | 0 | SkeletonLoaders |
| false | "Error" | 0 | ErrorMessage |
| false | null | 0 | EmptyState |
| false | null | >0 | KudoTable |

---

### TC-010 — Integración con backend y mapeo de errores HTTP

- **ID del Test**: TC-010
- **Capa**: Frontend
- **Prioridad**: CRÍTICA
- **Historia asociada**: US-012, US-013
- **Descripción**: Validar que kudosService.list() realiza petición HTTP correcta, maneja errores del backend apropiadamente, y retorna datos tipados.
- **Riesgo cubierto**: Errores HTTP no manejados que rompen aplicación, datos mal tipados que causan runtime errors, o requests con query params incorrectos.
- **Precondiciones**: 
  - kudosService implementado en services/api/
  - apiClient configurado con interceptores

#### Escenario (Gherkin)

```gherkin
Given el usuario solicita lista con filtros {category: 'TEAMWORK'}
When se ejecuta kudosService.list({category: 'TEAMWORK'}, 0, 20, 'DESC')
Then la petición es GET /api/v1/kudos?page=0&size=20&sortDirection=DESC&category=TEAMWORK
And el header Content-Type es application/json

Given el backend retorna 200 OK con PagedKudoResponse
When la promesa se resuelve
Then el resultado es tipo PagedKudoResponse
And contiene fields: content, totalElements, totalPages, currentPage, size
And content[0] es tipo KudoListItem

Given el backend retorna 400 Bad Request
When la promesa se rechaza
Then el error es capturado por interceptor
And el error.message contiene descripción legible
And no se muestra stack trace al usuario

Given el backend retorna 503 Service Unavailable
When el servicio falla
Then el error.message indica "Servicio no disponible, intenta más tarde"
```

#### Partición de Equivalencia

| Grupo | Status HTTP | Tipo |
|-------|-------------|------|
| Éxito | 200 OK | Válido - retorna datos |
| Error cliente | 400, 404 | Inválido - error mapeable |
| Error servidor | 500, 503 | Inválido - error de servicio |
| Error red | Network timeout | Inválido - error de conexión |

#### Valores Límite

| Valor | Contexto | Resultado Esperado |
|-------|----------|-------------------|
| 200 OK | Response exitosa | Promise.resolve(PagedKudoResponse) |
| 400 Bad Request | Validación fallida | Promise.reject con mensaje descriptivo |
| 503 Service Unavailable | BD desconectada | Promise.reject con mensaje de servicio |
| Network Error | Sin conexión | Promise.reject con mensaje de red |

---

### TC-011 — Filtros invocan servicio con query params correctos

- **ID del Test**: TC-011
- **Capa**: Frontend
- **Prioridad**: ALTA
- **Historia asociada**: US-013, US-015
- **Descripción**: Validar que KudoFilters construye correctamente query params y kudosService.list() es invocado con los valores correctos al aplicar filtros.
- **Riesgo cubierto**: Filtros no se aplican, se envían parámetros incorrectos al backend, o debounce no funciona generando peticiones excesivas.
- **Precondiciones**: 
  - KudoFilters renderizado con callbacks
  - Debounce de 500ms configurado en searchText

#### Escenario (Gherkin)

```gherkin
Given el usuario selecciona category="TEAMWORK"
And escribe searchText="proyecto"
And selecciona startDate="2026-02-01"
And selecciona endDate="2026-02-10"
When el usuario hace click en "Aplicar Filtros"
Then kudosService.list es invocado con:
  filters: {category: 'TEAMWORK', searchText: 'proyecto', startDate: '2026-02-01', endDate: '2026-02-10'}
  page: 0
  size: 20
  sortDirection: 'DESC'
And la petición incluye todos los query params

Given el usuario escribe "pro", "proj", "proye" rápidamente en searchText
When pasan <500ms entre keystrokes
Then NO se ejecutan peticiones intermedias
When pasan 500ms después del último keystroke
Then se ejecuta 1 sola petición con searchText="proye"

When el usuario hace click en "Limpiar"
Then todos los campos se resetean
And kudosService.list es invocado con filters={}
And retorna todos los kudos sin filtros
```

#### Partición de Equivalencia

| Grupo | Filtros aplicados | Tipo |
|-------|------------------|------|
| Sin filtros | {} | Válido - retorna todos |
| Solo categoría | {category: 'TEAMWORK'} | Válido |
| Solo texto | {searchText: 'proyecto'} | Válido |
| Solo fechas | {startDate, endDate} | Válido |
| Todos los filtros | {category, searchText, startDate, endDate} | Válido |

#### Valores Límite

| Valor | Contexto | Resultado Esperado |
|-------|----------|-------------------|
| searchText="" | Campo vacío | Filtro ignorado (no envía param) |
| startDate > endDate | Fechas inválidas | Muestra error de validación |
| Debounce <500ms | Typing rápido | No ejecuta petición |
| Debounce ≥500ms | Pausa en typing | Ejecuta 1 petición |

---

### TC-012 — Navegación de paginación preserva filtros activos

- **ID del Test**: TC-012
- **Capa**: Frontend
- **Prioridad**: ALTA
- **Historia asociada**: US-013, US-016
- **Descripción**: Validar que al cambiar de página con KudoPagination, los filtros activos se preservan y la petición incluye los mismos query params.
- **Riesgo cubierto**: Cambiar de página resetea filtros, usuario pierde contexto de búsqueda, o la URL no refleja estado actual (deep linking roto).
- **Precondiciones**: 
  - Usuario ha aplicado filtros: category=TEAMWORK, searchText="proyecto"
  - Resultados tienen totalPages=5
  - Usuario está en página 0

#### Escenario (Gherkin)

```gherkin
Given el usuario tiene filtros activos: {category: 'TEAMWORK', searchText: 'proyecto'}
And está en currentPage=0
When el usuario hace click en botón "Siguiente"
Then setPage(1) es invocado
And kudosService.list es llamado con:
  filters: {category: 'TEAMWORK', searchText: 'proyecto'}
  page: 1
  size: 20
  sortDirection: 'DESC'
And los filtros NO se resetean

When el usuario navega a página 4 (última página)
Then el botón "Siguiente" está disabled
And el botón "Anterior" está enabled

When el usuario hace click en "Anterior"
Then setPage(3) es invocado
And kudosService.list se ejecuta con page=3 y mismos filtros

Given el usuario está en página 2 con filtros activos
When el usuario recarga la página (F5)
Then los query params en URL se preservan: ?page=2&category=TEAMWORK&searchText=proyecto
And KudosListPage lee params de URL
And ejecuta kudosService.list con valores de URL
```

#### Partición de Equivalencia

| Grupo | Acción | Tipo |
|-------|--------|------|
| Siguiente habilitado | currentPage < totalPages-1 | Válido |
| Siguiente deshabilitado | currentPage === totalPages-1 | Válido - disabled |
| Anterior habilitado | currentPage > 0 | Válido |
| Anterior deshabilitado | currentPage === 0 | Válido - disabled |
| Cambio de página con filtros | Navegación con filtros activos | Válido - preserva filtros |

#### Valores Límite

| Valor | Contexto | Resultado Esperado |
|-------|----------|-------------------|
| currentPage=0 | Primera página | Botón "Anterior" disabled |
| currentPage=totalPages-1 | Última página | Botón "Siguiente" disabled |
| Cambio página 0→1 | Click "Siguiente" | kudosService.list(filters, 1, 20, 'DESC') |
| Cambio página 2→1 | Click "Anterior" | kudosService.list(filters, 1, 20, 'DESC') |

#### Tabla de Decisión

| currentPage | totalPages | Botón Anterior | Botón Siguiente | Filtros preservados |
|-------------|------------|----------------|-----------------|---------------------|
| 0 | 5 | disabled | enabled | Sí |
| 2 | 5 | enabled | enabled | Sí |
| 4 | 5 | enabled | disabled | Sí |

---

# 🧪 Plan de Pruebas — Refactorización FASE 3: Adapter RabbitMQ

**Fecha de creación**: 21 de febrero de 2026  
**Historia(s) base**: US-005 (Implementar Adapter `RabbitMqKudoPublisher`)

---

## 📋 Índice de Tests — US-005

| Completado | ID Test | Capa | Prioridad | Historia | Descripción |
|------------|---------|------|------------|----------|-------------|
| ☐ | TC-R05-001 | Backend (Unit) | CRÍTICA | US-005 | Adapter delega correctamente a RabbitTemplate con exchange y routing-key |
| ☐ | TC-R05-002 | Backend (Unit) | CRÍTICA | US-005 | AmqpException se envuelve en KudoPublishingException |
| ☐ | TC-R05-003 | Backend (Architecture) | CRÍTICA | US-005 | KudoServiceImpl NO importa RabbitTemplate ni ObjectMapper |
| ☐ | TC-R05-004 | Backend (Integration) | ALTA | US-005 | Mensaje llega a RabbitMQ con formato JSON correcto via Testcontainers |
| ☐ | TC-R05-005 | Backend (Unit) | ALTA | US-005 | Adapter registra logs de publicación exitosa y fallida |

---

## 🔵 Pruebas Backend — US-005

### TC-R05-001 — Adapter delega correctamente a RabbitTemplate con exchange y routing-key

- **ID del Test**: TC-R05-001
- **Capa**: Backend (Unit — Mockito)
- **Prioridad**: CRÍTICA
- **Historia asociada**: US-005
- **Descripción**: Validar que `RabbitMqKudoPublisher.publish()` invoca `rabbitTemplate.convertAndSend(exchange, routingKey, event)` con los argumentos exactos configurados por `@Value`.
- **Riesgo cubierto**: Adapter envía mensaje al exchange/routing-key incorrecto, causando pérdida silenciosa de eventos Kudo. El "Kudo Fantasma" a nivel de infraestructura.
- **Precondiciones**:
  - `RabbitMqKudoPublisher` inyectado con mocks de `RabbitTemplate`
  - Campos `exchangeName` y `routingKey` configurados con valores de prueba
- **Postcondiciones**: RabbitTemplate invocado exactamente una vez con los 3 argumentos correctos.

#### Escenario (Gherkin)

```gherkin
Given un RabbitMqKudoPublisher con exchange="kudos.exchange" y routingKey="kudos.key"
And un KudoEvent válido con from="alice@sofka.com", to="bob@sofka.com", category="Teamwork"
When se invoca publish(event)
Then rabbitTemplate.convertAndSend es invocado exactamente 1 vez
And el primer argumento es "kudos.exchange"
And el segundo argumento es "kudos.key"
And el tercer argumento es el KudoEvent original (referencia exacta)
```

#### Partición de Equivalencia

| Grupo | Valores | Tipo |
|-------|---------|------|
| Evento con todos los campos | from, to, category, message, timestamp | Válido — publica correctamente |
| Evento con campos mínimos | from, to (message=null, timestamp=null) | Válido — delega sin validar |
| Evento válido con categorías distintas | Innovation, Teamwork, Passion, Mastery | Válido — publica todas |

#### Valores Límite

| Valor | Contexto | Resultado Esperado |
|-------|----------|-------------------|
| KudoEvent con message="" | Mensaje vacío | publish delega (validación es responsabilidad del dominio) |
| KudoEvent con message de 500 chars | Mensaje en límite máximo | publish delega correctamente |
| KudoEvent con timestamp=null | Sin timestamp | publish delega (no valida) |

#### Tabla de Decisión

| Evento válido | RabbitTemplate OK | Resultado |
|--------------|-------------------|-----------|
| Sí | Sí | convertAndSend invocado 1 vez, sin excepción |
| Sí | No (AmqpException) | KudoPublishingException lanzada (ver TC-R05-002) |

---

### TC-R05-002 — AmqpException se envuelve en KudoPublishingException

- **ID del Test**: TC-R05-002
- **Capa**: Backend (Unit — Mockito)
- **Prioridad**: CRÍTICA
- **Historia asociada**: US-005
- **Descripción**: Validar que cuando `RabbitTemplate.convertAndSend()` lanza `AmqpException`, el adapter la captura y la re-lanza envuelta en `KudoPublishingException` con mensaje descriptivo y causa original preservada.
- **Riesgo cubierto**: Excepciones de infraestructura (AMQP) se propagan al service layer sin envoltura de dominio, rompiendo la separación de capas. El `GlobalExceptionHandler` no podría mapear correctamente a HTTP 503.
- **Precondiciones**:
  - `RabbitTemplate` mockeado para lanzar `AmqpException` en `convertAndSend()`
- **Postcondiciones**: `KudoPublishingException` lanzada con causa encadenada.

#### Escenario (Gherkin)

```gherkin
Given un RabbitMqKudoPublisher con RabbitTemplate que falla
And rabbitTemplate.convertAndSend() lanza AmqpException("Connection refused")
When se invoca publish(event)
Then se lanza KudoPublishingException
And el mensaje contiene "Error publishing KudoEvent to message broker"
And la causa (getCause()) es la AmqpException original
And el stack trace preserva la cadena completa

Given un RabbitMqKudoPublisher con RabbitTemplate que retorna OK
When se invoca publish(event)
Then NO se lanza ninguna excepción
```

#### Partición de Equivalencia

| Grupo | Excepción del RabbitTemplate | Tipo |
|-------|------------------------------|------|
| Sin excepción | Ninguna | Válido — publicación exitosa |
| AmqpException genérica | AmqpException | Inválido — envuelve en KudoPublishingException |
| AmqpConnectException | Conexión rechazada | Inválido — envuelve en KudoPublishingException |
| AmqpIOException | I/O failure | Inválido — envuelve en KudoPublishingException |

#### Valores Límite

| Valor | Contexto | Resultado Esperado |
|-------|----------|-------------------|
| AmqpException con mensaje null | Excepción sin mensaje | KudoPublishingException con mensaje fijo del adapter |
| AmqpException con causa anidada | Nested exception | causa preservada en la cadena |

---

### TC-R05-003 — KudoServiceImpl NO importa RabbitTemplate ni ObjectMapper

- **ID del Test**: TC-R05-003
- **Capa**: Backend (Architecture — Pure JUnit 5)
- **Prioridad**: CRÍTICA
- **Historia asociada**: US-005
- **Descripción**: Validar mediante inspección reflexiva que `KudoServiceImpl` del producer es libre de dependencias de infraestructura (`RabbitTemplate`, `ObjectMapper`). Esto garantiza que el patrón Adapter/Port funciona correctamente y la inversión de dependencias (DIP) se mantiene.
- **Riesgo cubierto**: Desarrollador inyecta `RabbitTemplate` directamente en el servicio, violando DIP y acoplando el dominio a la infraestructura de mensajería.
- **Precondiciones**:
  - `KudoServiceImpl` compilado y accesible vía reflexión
- **Postcondiciones**: Test puro sin contexto Spring.

#### Escenario (Gherkin)

```gherkin
Given la clase KudoServiceImpl del producer
When inspecciono sus campos declarados (getDeclaredFields)
Then ningún campo tiene tipo RabbitTemplate
And ningún campo tiene tipo ObjectMapper

Given la clase KudoServiceImpl del producer
When inspecciono los parámetros de su constructor
Then ningún parámetro es RabbitTemplate
And ningún parámetro es ObjectMapper

Given el source file de KudoServiceImpl
When examino sus imports
Then NO contiene "org.springframework.amqp"
And NO contiene "com.fasterxml.jackson"
```

#### Partición de Equivalencia

| Grupo | Verificación | Tipo |
|-------|-------------|------|
| Campos del servicio | Solo KudoEventPublisher | Válido — DIP cumplido |
| Constructor del servicio | Solo recibe ports/interfaces | Válido — inyección limpia |
| Imports del servicio | Sin amqp ni jackson | Válido — desacoplado |

#### Tabla de Decisión

| ¿Tiene RabbitTemplate? | ¿Tiene ObjectMapper? | Resultado |
|------------------------|---------------------|-----------|
| No | No | ✅ DIP cumplido — test PASS |
| Sí | No | ❌ DIP violado — test FAIL |
| No | Sí | ❌ DIP violado — test FAIL |
| Sí | Sí | ❌ DIP violado — test FAIL |

---

### TC-R05-004 — Mensaje llega a RabbitMQ con formato JSON correcto via Testcontainers

- **ID del Test**: TC-R05-004
- **Capa**: Backend (Integration — Testcontainers)
- **Prioridad**: ALTA
- **Historia asociada**: US-005
- **Descripción**: Validar que el adapter `RabbitMqKudoPublisher` publica un mensaje JSON completo a RabbitMQ real (via Testcontainers) y que el mensaje puede ser consumido y deserializado correctamente preservando todos los campos del contrato `KudoEvent`.
- **Riesgo cubierto**: Fallo de serialización (el incidente "Kudo Fantasma"), incompatibilidad de formato JSON entre producer y consumer, pérdida de datos en tránsito.
- **Precondiciones**:
  - Testcontainers con RabbitMQ levantado
  - Spring Boot context completo
  - `Jackson2JsonMessageConverter` configurado en `RabbitConfig`
- **Postcondiciones**: Mensaje consumido con todos los campos intactos.

#### Escenario (Gherkin)

```gherkin
Given un contenedor RabbitMQ en ejecución via Testcontainers
And RabbitMqKudoPublisher inyectado con contexto real
And un KudoEvent con from="alice@sofka.com", to="bob@sofka.com", category="Innovation", message="Great work!", timestamp=2026-02-21T10:00:00
When se invoca publish(event)
Then el mensaje llega a la cola "kudos.queue"
And se deserializa correctamente a KudoEvent
And event.getFrom() == "alice@sofka.com"
And event.getTo() == "bob@sofka.com"
And event.getCategory() == "Innovation"
And event.getMessage() == "Great work!"
And event.getTimestamp() == 2026-02-21T10:00:00

Given un KudoEvent con timestamp LocalDateTime preciso
When se serializa a JSON y luego se deserializa
Then el timestamp mantiene formato ISO-8601 sin pérdida de precisión
```

#### Partición de Equivalencia

| Grupo | Evento enviado | Tipo |
|-------|----------------|------|
| Evento completo | Todos los campos no-null | Válido — serialización completa |
| Evento con campos opcionales null | timestamp=null | Válido — campo omitido en JSON |
| Evento con caracteres especiales | message con acentos, emojis | Válido — UTF-8 preservado |

#### Valores Límite

| Valor | Contexto | Resultado Esperado |
|-------|----------|-------------------|
| message="a".repeat(500) | Mensaje en límite máximo | JSON completo, sin truncamiento |
| timestamp con nanosegundos | LocalDateTime.of(2026,2,21,10,0,0,123456789) | Precisión preservada en ISO-8601 |
| category="Innovation" | Primera categoría válida | Serialización correcta |

---

### TC-R05-005 — Adapter registra logs de publicación exitosa y fallida

- **ID del Test**: TC-R05-005
- **Capa**: Backend (Unit — Mockito)
- **Prioridad**: ALTA
- **Historia asociada**: US-005
- **Descripción**: Validar que `RabbitMqKudoPublisher` produce logs INFO al publicar exitosamente y logs ERROR cuando falla la publicación, facilitando la observabilidad del sistema en producción.
- **Riesgo cubierto**: Sin logs adecuados, los fallos de publicación son invisibles en producción. Dificultad para diagnosticar el "Kudo Fantasma".
- **Precondiciones**:
  - Appender de logs capturado (LogCaptor o similar)
- **Postcondiciones**: Logs verificados sin levantar contexto Spring.

#### Escenario (Gherkin)

```gherkin
Given un RabbitMqKudoPublisher con RabbitTemplate mock exitoso
And un KudoEvent con from="alice@sofka.com", to="bob@sofka.com", category="Teamwork"
When se invoca publish(event)
Then se registra log INFO conteniendo "Publishing KudoEvent to RabbitMQ"
And el log incluye from="alice@sofka.com", to="bob@sofka.com", category="Teamwork"
And se registra log DEBUG conteniendo "KudoEvent published successfully"

Given un RabbitMqKudoPublisher con RabbitTemplate que lanza AmqpException
When se invoca publish(event) y falla
Then se registra log ERROR conteniendo "Failed to publish KudoEvent to RabbitMQ"
And la excepción se incluye como parámetro del log (stack trace disponible)
```

#### Partición de Equivalencia

| Grupo | Resultado de publish | Logs esperados |
|-------|---------------------|----------------|
| Publicación exitosa | Sin excepción | INFO + DEBUG |
| Publicación fallida | AmqpException | INFO + ERROR |

#### Tabla de Decisión

| Publicación OK | Log INFO presente | Log DEBUG presente | Log ERROR presente |
|---------------|-------------------|--------------------|--------------------|
| Sí | Sí | Sí | No |
| No (AmqpException) | Sí (antes del error) | No | Sí |

---

# 🧪 Plan de Pruebas — Refactorización FASE 3: Consumer Tipado (US-007)

**Fecha de creación**: 21 de febrero de 2026  
**Historia(s) base**: US-007 (Eliminar Primitive Obsession en KudosConsumer con Deserialización Tipada)

---

## 📋 Índice de Tests — US-007

| Completado | ID Test | Capa | Prioridad | Historia | Descripción |
|------------|---------|------|------------|----------|-------------|
| ☐ | TC-R07-001 | Backend (Unit) | CRÍTICA | US-007 | KudosConsumer recibe KudoEvent tipado (no String) |
| ☐ | TC-R07-002 | Backend (Architecture) | CRÍTICA | US-007 | KudosConsumer y KudoService NO usan String como payload |
| ☐ | TC-R07-003 | Backend (Unit) | CRÍTICA | US-007 | Jackson2JsonMessageConverter registrado como Bean en RabbitConfig |
| ☐ | TC-R07-004 | Backend (Unit) | ALTA | US-007 | KudoServiceImpl mapea KudoEvent a Kudo vía Builder validado |
| ☐ | TC-R07-005 | Backend (Unit) | ALTA | US-007 | KudoServiceImpl NO contiene ObjectMapper, JsonNode ni readTree() |
| ☐ | TC-R07-006 | Backend (Unit) | ALTA | US-007 | KudoEvent inválido lanza InvalidKudoException al construir entidad |

---

## 🔵 Pruebas Backend — US-007

### TC-R07-001 — KudosConsumer recibe KudoEvent tipado (no String)

- **ID del Test**: TC-R07-001
- **Capa**: Backend (Unit — Mockito)
- **Prioridad**: CRÍTICA
- **Historia asociada**: US-007
- **Descripción**: Validar que `KudosConsumer.handleKudo()` acepta `KudoEvent` como parámetro (no `String`) y delega directamente al servicio sin parseo manual.
- **Riesgo cubierto**: KudosConsumer mantiene firma con `String`, requiriendo parseo manual de JSON y exponiendo al incidente "Kudo Fantasma" por deserialización incorrecta.
- **Precondiciones**:
  - `KudosConsumer` con firma `handleKudo(@Payload KudoEvent event)`
  - `KudoService` mockeado
- **Postcondiciones**: `kudoService.saveKudo(event)` invocado exactamente 1 vez con el mismo objeto `KudoEvent`.

#### Escenario (Gherkin)

```gherkin
Given un KudosConsumer con KudoService mockeado
And un KudoEvent válido con from="alice@sofka.com", to="bob@sofka.com", category="Teamwork"
When se invoca handleKudo(event)
Then kudoService.saveKudo es invocado exactamente 1 vez
And el argumento pasado es el mismo KudoEvent (referencia exacta)
And NO se invoca ObjectMapper, JsonNode ni parseo manual alguno
```

#### Partición de Equivalencia

| Grupo | Tipo de payload | Tipo |
|-------|----------------|------|
| KudoEvent con todos los campos | Evento completo | Válido — se delega al servicio |
| KudoEvent con campos mínimos | from, to, category, message | Válido — se delega (timestamp null) |

#### Tabla de Decisión

| Evento válido | saveKudo invocado | Resultado |
|--------------|-------------------|-----------|
| Sí | Sí (1 vez) | Procesamiento exitoso |

---

### TC-R07-002 — KudosConsumer y KudoService NO usan String como payload

- **ID del Test**: TC-R07-002
- **Capa**: Backend (Architecture — Pure JUnit 5)
- **Prioridad**: CRÍTICA
- **Historia asociada**: US-007
- **Descripción**: Validar vía reflexión que la firma de `handleKudo()` en `KudosConsumer` y `saveKudo()` en `KudoService` usan `KudoEvent` como tipo de parámetro, no `String`.
- **Riesgo cubierto**: Regresión que reintroduce `String` como payload, re-habilitando el Primitive Obsession eliminado.
- **Precondiciones**:
  - Clases compiladas y accesibles vía reflexión
- **Postcondiciones**: Test puro sin contexto Spring.

#### Escenario (Gherkin)

```gherkin
Given la clase KudosConsumer del consumer
When inspecciono el método handleKudo
Then su primer parámetro es de tipo KudoEvent, no String

Given la interfaz KudoService del consumer
When inspecciono el método saveKudo
Then su primer parámetro es de tipo KudoEvent, no String

Given la implementación KudoServiceImpl del consumer
When inspecciono sus campos declarados
Then NO tiene campos de tipo ObjectMapper
And NO tiene campos de tipo JsonNode
```

#### Tabla de Decisión

| handleKudo(KudoEvent) | saveKudo(KudoEvent) | Resultado |
|-----------------------|---------------------|-----------|
| Sí | Sí | ✅ Primitive Obsession eliminado |
| No (String) | Sí | ❌ Consumer no refactorizado |
| Sí | No (String) | ❌ Service no refactorizado |

---

### TC-R07-003 — Jackson2JsonMessageConverter registrado como Bean en RabbitConfig

- **ID del Test**: TC-R07-003
- **Capa**: Backend (Architecture — Pure JUnit 5)
- **Prioridad**: CRÍTICA
- **Historia asociada**: US-007
- **Descripción**: Validar que `RabbitConfig` del consumer declara un `@Bean` de tipo `MessageConverter` con implementación `Jackson2JsonMessageConverter` para deserialización automática.
- **Riesgo cubierto**: Sin `Jackson2JsonMessageConverter`, Spring AMQP usa `SimpleMessageConverter` que entrega `byte[]`/`String` raw, obligando a parseo manual.
- **Precondiciones**:
  - `RabbitConfig` compilada y accesible vía reflexión
- **Postcondiciones**: Al menos un método `@Bean` retorna `MessageConverter`.

#### Escenario (Gherkin)

```gherkin
Given la clase RabbitConfig del consumer
When inspecciono sus métodos anotados con @Bean
Then al menos un método retorna tipo MessageConverter
And ese método retorna una instancia de Jackson2JsonMessageConverter
```

#### Partición de Equivalencia

| Grupo | Configuración | Tipo |
|-------|--------------|------|
| Jackson2JsonMessageConverter como @Bean | Método retorna MessageConverter | Válido — deserialización automática |
| SimpleMessageConverter (default) | Sin @Bean de converter | Inválido — payload llega como byte[] |

---

### TC-R07-004 — KudoServiceImpl mapea KudoEvent a Kudo vía Builder validado

- **ID del Test**: TC-R07-004
- **Capa**: Backend (Unit — Mockito)
- **Prioridad**: ALTA
- **Historia asociada**: US-007
- **Descripción**: Validar que `KudoServiceImpl.saveKudo(KudoEvent)` construye la entidad `Kudo` usando el Builder con validaciones de dominio y persiste via `KudoPersistencePort`.
- **Riesgo cubierto**: Servicio mapea campos incorrectamente (from→toUser swap), o construye entidad sin pasar por Builder validado, permitiendo datos inválidos.
- **Precondiciones**:
  - `KudoPersistencePort` mockeado
  - `KudoEvent` con todos los campos válidos
- **Postcondiciones**: `persistencePort.save()` invocado con `Kudo` correctamente construido.

#### Escenario (Gherkin)

```gherkin
Given un KudoServiceImpl con KudoPersistencePort mockeado
And un KudoEvent con from="alice@sofka.com", to="bob@sofka.com", category="Innovation", message="Great idea!", timestamp=2026-02-21T10:00:00
When se invoca saveKudo(event)
Then persistencePort.save es invocado 1 vez
And el Kudo capturado tiene fromUser="alice@sofka.com"
And toUser="bob@sofka.com"
And category=KudoCategory.INNOVATION
And message="Great idea!"
And createdAt=2026-02-21T10:00:00
```

#### Partición de Equivalencia

| Grupo | KudoEvent | Tipo |
|-------|-----------|------|
| Evento con todas las categorías | Innovation, Teamwork, Passion, Mastery | Válido — mapea correctamente |
| Evento con timestamp null | Sin fecha | Válido — Builder asigna LocalDateTime.now() |
| Evento con from==to | Self-kudo | Inválido — Builder lanza InvalidKudoException |

#### Valores Límite

| Valor | Contexto | Resultado Esperado |
|-------|----------|-------------------|
| message de 10 chars | Mínimo válido | Kudo construido correctamente |
| message de 500 chars | Máximo válido | Kudo construido correctamente |
| category="Innovation" | Case-insensitive match | KudoCategory.INNOVATION |
| timestamp=null | Sin timestamp en evento | Builder asigna now() |

---

### TC-R07-005 — KudoServiceImpl NO contiene ObjectMapper, JsonNode ni readTree()

- **ID del Test**: TC-R07-005
- **Capa**: Backend (Architecture — Pure JUnit 5)
- **Prioridad**: ALTA
- **Historia asociada**: US-007
- **Descripción**: Validar que `KudoServiceImpl` del consumer no tiene dependencias de Jackson (`ObjectMapper`, `JsonNode`, `readTree`). Toda deserialización debe ser responsabilidad del framework (Spring AMQP) o de la capa de infraestructura.
- **Riesgo cubierto**: Servicio de dominio acoplado a librería de serialización, violando SRP y Clean Architecture.
- **Precondiciones**:
  - `KudoServiceImpl` compilada y accesible vía reflexión
- **Postcondiciones**: Test puro sin contexto Spring.

#### Escenario (Gherkin)

```gherkin
Given la clase KudoServiceImpl del consumer
When inspecciono sus campos declarados
Then NO tiene campos de tipo ObjectMapper
And NO tiene campos de tipo JsonNode

Given la clase KudoServiceImpl del consumer
When inspecciono los parámetros de sus constructores
Then ningún parámetro es ObjectMapper
```

#### Tabla de Decisión

| ¿Tiene ObjectMapper? | ¿Tiene JsonNode? | Resultado |
|---------------------|------------------|-----------|
| No | No | ✅ SRP cumplido |
| Sí | No | ❌ SRP violado |
| No | Sí | ❌ SRP violado |
| Sí | Sí | ❌ SRP violado |

---

### TC-R07-006 — KudoEvent inválido lanza InvalidKudoException al construir entidad

- **ID del Test**: TC-R07-006
- **Capa**: Backend (Unit — Mockito)
- **Prioridad**: ALTA
- **Historia asociada**: US-007
- **Descripción**: Validar que cuando un `KudoEvent` tiene datos inválidos (campos vacíos, self-kudo), el `Kudo.Builder` lanza `InvalidKudoException` y `KudoPersistencePort.save()` nunca se invoca.
- **Riesgo cubierto**: Datos inválidos que pasan el Builder y se persisten en BD, contaminando datos de producción.
- **Precondiciones**:
  - `KudoPersistencePort` mockeado
  - `KudoEvent` con datos inválidos
- **Postcondiciones**: `persistencePort.save()` NUNCA invocado.

#### Escenario (Gherkin)

```gherkin
Given un KudoEvent con from="" (vacío)
When se invoca saveKudo(event)
Then se lanza InvalidKudoException con mensaje "'fromUser' must not be null or empty"
And persistencePort.save() NO es invocado

Given un KudoEvent con from="alice@sofka.com" y to="alice@sofka.com" (self-kudo)
When se invoca saveKudo(event)
Then se lanza InvalidKudoException con mensaje "Cannot send kudo to yourself"
And persistencePort.save() NO es invocado

Given un KudoEvent con category="INVALID_CATEGORY"
When se invoca saveKudo(event)
Then se lanza IllegalArgumentException con mensaje "Unknown KudoCategory"
And persistencePort.save() NO es invocado
```

#### Partición de Equivalencia

| Grupo | Datos del evento | Tipo |
|-------|-----------------|------|
| from vacío | from="" | Inválido — lanza InvalidKudoException |
| to vacío | to="" | Inválido — lanza InvalidKudoException |
| message vacío | message="" | Inválido — lanza InvalidKudoException |
| self-kudo | from==to | Inválido — lanza InvalidKudoException |
| categoría inválida | category="INVALID" | Inválido — lanza IllegalArgumentException |
| Evento completamente válido | Todos los campos OK | Válido — persiste correctamente |

---

# 🧪 Plan de Pruebas — Refactorización FASE 3: Strategy Validation (US-012)

**Fecha de creación**: 21 de febrero de 2026  
**Historia(s) base**: US-012 (Integrar Validación por Strategy en el Flujo del Producer)

---

## 📋 Índice de Tests — US-012

| Completado | ID Test | Capa | Prioridad | Historia | Descripción |
|------------|---------|------|------------|----------|-------------|
| ☐ | TC-R12-001 | Backend (Unit) | CRÍTICA | US-012 | KudoValidationContext resuelve y ejecuta estrategia por categoría |
| ☐ | TC-R12-002 | Backend (Unit) | CRÍTICA | US-012 | Categoría no registrada lanza InvalidKudoException |
| ☐ | TC-R12-003 | Backend (Unit) | CRÍTICA | US-012 | KudoServiceImpl invoca validationContext.validate() antes de publish |
| ☐ | TC-R12-004 | Backend (Unit) | ALTA | US-012 | Validación fallida impide publicación al broker |
| ☐ | TC-R12-005 | Backend (Architecture) | ALTA | US-012 | Cada estrategia es independiente y extensible (OCP) |
| ☐ | TC-R12-006 | Backend (Controller) | ALTA | US-012 | Bean Validation (API) + Strategy (dominio) coexisten sin conflicto |

---

## 🔵 Pruebas Backend — US-012

### TC-R12-001 — KudoValidationContext resuelve y ejecuta estrategia por categoría

- **ID del Test**: TC-R12-001
- **Capa**: Backend (Unit — Mockito)
- **Prioridad**: CRÍTICA
- **Historia asociada**: US-012
- **Descripción**: Validar que `KudoValidationContext` selecciona la estrategia correcta según `request.getCategory()` y ejecuta `validate()` sobre ella.
- **Riesgo cubierto**: Context no resuelve la estrategia, ejecuta la Strategy incorrecta, o ignora la validación completamente.
- **Precondiciones**:
  - 4 estrategias mockeadas registradas en el contexto
- **Postcondiciones**: Solo la estrategia correspondiente a la categoría es invocada.

#### Escenario (Gherkin)

```gherkin
Given un KudoValidationContext con 4 estrategias: Innovation, Teamwork, Passion, Mastery
And un KudoRequest con category="Teamwork"
When se invoca validate(request)
Then SOLO TeamworkValidationStrategy.validate() es invocado
And las demás estrategias NO son invocadas
```

#### Partición de Equivalencia

| Grupo | Categoría | Tipo |
|-------|-----------|------|
| Innovation | Válida | Válido — ejecuta InnovationValidationStrategy |
| Teamwork | Válida | Válido — ejecuta TeamworkValidationStrategy |
| Passion | Válida | Válido — ejecuta PassionValidationStrategy |
| Mastery | Válida | Válido — ejecuta MasteryValidationStrategy |
| Unknown | "Leadership" | Inválido — lanza InvalidKudoException (TC-R12-002) |

---

### TC-R12-002 — Categoría no registrada lanza InvalidKudoException

- **ID del Test**: TC-R12-002
- **Capa**: Backend (Unit)
- **Prioridad**: CRÍTICA
- **Historia asociada**: US-012
- **Descripción**: Validar que cuando `KudoValidationContext.validate()` recibe un request con categoría no registrada en ninguna estrategia, lanza `InvalidKudoException` con mensaje descriptivo.
- **Riesgo cubierto**: Categoría desconocida pasa validación silenciosamente, se publica un evento con categoría inválida que luego falla en el consumer.
- **Precondiciones**:
  - Context con 4 estrategias (sin cobertura para "Leadership")
- **Postcondiciones**: `InvalidKudoException` lanzada, ninguna estrategia ejecutada.

#### Escenario (Gherkin)

```gherkin
Given un KudoValidationContext sin estrategia para "Leadership"
And un KudoRequest con category="Leadership"
When se invoca validate(request)
Then se lanza InvalidKudoException
And el mensaje contiene "Unsupported category: Leadership"
And ninguna estrategia fue invocada
```

---

### TC-R12-003 — KudoServiceImpl invoca validationContext.validate() antes de publish

- **ID del Test**: TC-R12-003
- **Capa**: Backend (Unit — Mockito)
- **Prioridad**: CRÍTICA
- **Historia asociada**: US-012
- **Descripción**: Validar que `KudoServiceImpl.sendKudo()` ejecuta `validationContext.validate(request)` ANTES de `publisher.publish(event)`, garantizando que la validación de dominio ocurre antes de la publicación.
- **Riesgo cubierto**: Servicio publica al broker sin validar, permitiendo que eventos inválidos por reglas de dominio lleguen al consumer y fallen allí (más caro).
- **Precondiciones**:
  - `KudoValidationContext` y `KudoEventPublisher` mockeados
  - Mockito `InOrder` para verificar secuencia
- **Postcondiciones**: validate() SIEMPRE se ejecuta antes de publish().

#### Escenario (Gherkin)

```gherkin
Given un KudoServiceImpl con KudoValidationContext y KudoEventPublisher mockeados
And un KudoRequest válido
When se invoca sendKudo(request)
Then primero se invoca validationContext.validate(request)
Then después se invoca kudoEventPublisher.publish(event)
And el orden es estrictamente secuencial (validate ANTES de publish)
```

#### Tabla de Decisión

| Validación OK | Publicación | Resultado |
|--------------|-------------|-----------|
| Sí | Se ejecuta | 202 ACCEPTED con KudoResponse |
| No (InvalidKudoException) | NO se ejecuta | Excepción propagada (TC-R12-004) |

---

### TC-R12-004 — Validación fallida impide publicación al broker

- **ID del Test**: TC-R12-004
- **Capa**: Backend (Unit — Mockito)
- **Prioridad**: ALTA
- **Historia asociada**: US-012
- **Descripción**: Validar que cuando `KudoValidationContext.validate()` lanza excepción, `KudoEventPublisher.publish()` NUNCA es invocado.
- **Riesgo cubierto**: Excepción de validación capturada silenciosamente, evento publicado de todas formas.
- **Precondiciones**:
  - `KudoValidationContext.validate()` configurado para lanzar `InvalidKudoException`
- **Postcondiciones**: `publish()` tiene 0 invocaciones.

#### Escenario (Gherkin)

```gherkin
Given KudoValidationContext.validate() lanza InvalidKudoException("Self-kudo not allowed")
When se invoca sendKudo(request)
Then se propaga InvalidKudoException
And kudoEventPublisher.publish() NO es invocado (verify 0 times)
```

---

### TC-R12-005 — Cada estrategia es independiente y extensible (OCP)

- **ID del Test**: TC-R12-005
- **Capa**: Backend (Architecture — Pure JUnit 5)
- **Prioridad**: ALTA
- **Historia asociada**: US-012
- **Descripción**: Validar que cada implementación de `KudoValidationStrategy` es una clase independiente y que agregar una nueva categoría solo requiere crear una nueva clase sin modificar las existentes (Open/Closed Principle).
- **Riesgo cubierto**: Sistema de validación monolítico que requiere modificar clases existentes para agregar nuevas reglas.
- **Precondiciones**:
  - Al menos 4 implementaciones de `KudoValidationStrategy` en el classpath
- **Postcondiciones**: Test puro sin contexto Spring.

#### Escenario (Gherkin)

```gherkin
Given las implementaciones de KudoValidationStrategy en el classpath
When inspecciono las clases que implementan la interfaz
Then existen al menos 4 implementaciones independientes
And cada una está en su propia clase (no inner classes)
And todas implementan el método validate(KudoRequest)
```

---

### TC-R12-006 — Bean Validation (API) + Strategy (dominio) coexisten sin conflicto

- **ID del Test**: TC-R12-006
- **Capa**: Backend (Controller — @WebMvcTest)
- **Prioridad**: ALTA
- **Historia asociada**: US-012
- **Descripción**: Validar que Bean Validation (`@Valid` en controller) y Strategy validation (dominio) operan como dos capas complementarias: API valida formato primero, dominio valida reglas de negocio después.
- **Riesgo cubierto**: Bean Validation y Strategy entran en conflicto, validaciones duplicadas que confunden el error, o una capa anula a la otra.
- **Precondiciones**:
  - `@Valid` activo en `KudosController.publishKudos()`
  - `KudoValidationContext` integrado en `KudoServiceImpl`
- **Postcondiciones**: Cada capa produce errores HTTP distintos (422 vs 400).

#### Escenario (Gherkin)

```gherkin
Given un KudoRequest con from="" (falla Bean Validation @NotBlank)
When se envía POST /api/v1/kudos
Then Bean Validation rechaza ANTES de llegar al servicio
And retorna 400 Bad Request con detalle de campo
And KudoValidationContext.validate() NUNCA es invocado

Given un KudoRequest válido por Bean Validation pero from==to
When se envía POST /api/v1/kudos
Then Bean Validation pasa (formato correcto)
And KudoValidationContext.validate() lanza InvalidKudoException
And retorna error HTTP apropiado
```

#### Tabla de Decisión

| Bean Validation | Strategy Validation | Resultado HTTP |
|----------------|--------------------|----|
| Falla | N/A (no se ejecuta) | 400 Bad Request |
| Pasa | Falla | 400 Bad Request (InvalidKudoException) |
| Pasa | Pasa | 202 Accepted |
