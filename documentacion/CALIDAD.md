# CALIDAD: Anatomía de un Incidente

> **Proyecto:** Sofkianos MVP  
> **Patrones Involucrados:** Adapter, DTO, Infrastructure Layer  
> **Tipo de Incidente:** Fallo de Serialización en Sistema Distribuido  

---

## 1. Introducción

Este documento analiza técnicamente un defecto complejo surgido tras la refactorización a Arquitectura Hexagonal. El objetivo es distinguir claramente entre el error humano, el defecto en el código y el fallo en tiempo de ejecución, utilizando un escenario realista de **"Mismatch de Serialización de Fechas"**.

---

## 2. Definición del Incidente: "El Kudo Fantasma"

**Contexto:**
El sistema utiliza `RabbitMqKudoPublisher` (Adapter) para publicar eventos `KudoEvent` (DTO). El evento incluye un campo `timestamp` de tipo `LocalDateTime`.

### ERROR (La causa humana)
> *Acción incorrecta, ignorancia o equivocación cometida por una persona.*

El desarrollador Backend asumió que la librería de serialización (`Jackson`), al estar incluida en `spring-boot-starter-web`, estaba configurada por defecto para manejar tipos de Java 8 (`java.time.LocalDateTime`) como Strings ISO-8601.

**El error específico:** Ignorar que el módulo `JavaTimeModule` debe ser registrado explícitamente o configurado vía `properties` en el bean del `ObjectMapper` infraestructural cuando no se usa la auto-configuración web estándar para mensajes AMQP.

### DEFECTO (El problema en el código)
> *Imperfección estática en el software (bug).*

El defecto reside en la clase `RabbitConfig` (infraestructura) o en la falta de configuración del Adapter.

**Código Defectuoso Refactorizado:**
```java
// RabbitMqKudoPublisher.java
public void publish(KudoEvent event) {
    // Defecto: Usa un ObjectMapper sin el módulo de tiempos configurado/registrado
    String json = objectMapper.writeValueAsString(event); 
    rabbitTemplate.convertAndSend(exchange, key, json);
}
```

Al serializar, Jackson convierte `LocalDateTime.now()` no en un String `"2026-02-11T10:00:00"`, sino en un **Array de enteros** (formato fallback): `[2026, 2, 11, 10, 0, 0]`.

Este defecto es "invisible" en tiempo de compilación porque es un comportamiento en tiempo de ejecución de la librería.

### FALLO (El comportamiento observable)
> *Desviación del sistema respecto a los requisitos esperados durante la ejecución.*

**Manifestación al Usuario (Frontend):**
1. El usuario diligencia el formulario y hace clic en "Enviar".
2. La API (Producer) responde `202 Accepted` porque el mensaje se publicó exitosamente en la cola (aunque con formato incorrecto).
3. **El Fallo:** El usuario ve el mensaje de éxito "¡Kudo enviado!", pero el Kudo **nunca aparece en el listado**.

**Manifestación en el Sistema (Backend Consumer):**
El Worker intenta deserializar el mensaje. Espera un String para el campo timestamp pero encuentra un Array `START_ARRAY`.
- Excepción: `com.fasterxml.jackson.databind.exc.MismatchedInputException`.
- Consecuencia: El mensaje es rechazado (NACK) y enviado a la Dead Letter Queue (DLQ) o descartado, perdiéndose la información silenciosamente para el usuario final.

---

## 3. Análisis de Causa Raíz (RCA)

Using the **5 Whys** technique:

1.  **¿Por qué falló el sistema?**
    Porque el Consumer no pudo leer el JSON del evento.
2.  **¿Por qué no pudo leer el JSON?**
    Porque el campo `timestamp` venía como `[2026, 2...]` (Array) y se esperaba `"2026-02..."` (String).
3.  **¿Por qué se envió como Array?**
    Porque el Producer usó la serialización por defecto de Jackson para `LocalDateTime`.
4.  **¿Por qué se usó la serialización por defecto?**
    Porque el bean `ObjectMapper` inyectado en el Adapter no tenía el `JavaTimeModule` activado ni la property `WRITE_DATES_AS_TIMESTAMPS=false`.
5.  **¿Por qué no se configuró?**
    Porque en la refactorización se creó una configuración manual de RabbitMQ (`RabbitConfig`) que sobrescribió o ignoró las auto-configuraciones de Spring Boot que normalmente manejan esto.

---

## 4. Matriz de Riesgo y Prevención

| Criterio | Evaluación | Justificación |
|---|---|---|
| **Impacto** | 🔴 Alto | Pérdida de datos (Data Loss). El usuario cree que la operación fue exitosa. |
| **Probabilidad** | 🟡 Media | Común en refactorizaciones manuales de infraestructura. |
| **Detección** | 🟡 Difícil | No falla en compilación ni en tests unitarios simples (si se usan mocks de Jackson). |

### Medidas Preventivas (Defense in Depth)

#### 1. Arquitectura (Adapter Pattern)
Reforzar el Adapter para que no dependa implícitamente de la configuración global.
```java
// Solución en Adapter: Configuración explícita local o Test de Contrato
private final ObjectMapper mapper = JsonMapper.builder()
    .addModule(new JavaTimeModule()) // Forzar módulo
    .build();
```

#### 2. Testing (Pirámide de Pruebas)
Este defecto **escapó** a los Unit Tests porque se mockeó el comportamiento de serialización (`when(mapper.write...).thenReturn("json")`).

- **Nivel de Detección Requerido:** **Tests de Integración (Component Tests)**.
- **Prueba Sugerida:** Levantar el contexto de Spring completo o usar `@JsonTest` verificando la salida literal del JSON generado.

```java
@Test
void whenSerializingDate_thenFormatIsIso8601() {
    KudoEvent event = new KudoEvent(..., LocalDateTime.of(2026, 2, 11, 12, 0));
    String json = rabbitMqKudoPublisher.serialize(event);
    assertThat(json).contains("\"timestamp\":\"2026-02-11T12:00:00\""); // Validar contrato
}
```

---

## 5. Conclusión

Este incidente demuestra que la aplicación correcta de patrones como **Adapter** no exime al sistema de fallos infraestructurales. De hecho, la abstracción puede ocultar detalles de configuración críticos.

La validación de la calidad no solo debe verificar la estructura del código (Validación estática), sino garantizar que los **contratos de datos** (JSON Schemas) entre componentes desacoplados se respeten rigurosamente en tiempo de ejecución.

- **Lección Aprendida:** Nunca asumir defaults en la capa de infraestructura.
- **Acción:** Configurar `Jackson` explícitamente en el *Shared Kernel* o en la configuración base de ambos microservicios.

---

## 6. Análisis de la Pirámide de Pruebas

### Tesis: La Inversión de la Pirámide y el Costo del Defecto

En muchos proyectos fallidos, la pirámide de pruebas se invierte (Patrón "Cono de Helado"): pocos tests unitarios y excesiva dependencia de tests manuales o E2E. Esto es insostenible porque:

1.  **Fragilidad:** Los tests E2E se rompen con cambios triviales de UI.
2.  **Lentitud:** El feedback loop es de minutos u horas, no milisegundos.
3.  **Ambigüedad:** Cuando falla un E2E, no sabemos si es el Frontend, la Red, la API, el Broker o la Base de Datos.

Nuestra arquitectura (Clean Architecture + SOLID) **exige** una base sólida de Tests Unitarios porque la lógica está desacoplada. Los Tests de Integración solo deben verificar que los "cables" (Adapters) conecten bien.

### Escenarios de Alto Valor

#### 1. Unit Test (Base) — 70%
- **Componente:** `Kudo.builder()` (Dominio)
- **Riesgo Mitigado:** Integridad del Modelo de Dominio.
- **Por qué es alto valor:** Verifica que *no se pueda* crear un Kudo inválido (e.g., auto-kudo, mensaje vacío) antes de que toque cualquier infraestructura. Costo de ejecución: <1ms.

#### 2. Integration Test (Medio) — 20%
- **Componente:** `RabbitMqKudoPublisher` (Infrastructure Adapter)
- **Riesgo Mitigado:** Coupling Risk & **Contract Mismatch** (El incidente del "Kudo Fantasma").
- **Validación:** Levantar un contenedor de RabbitMQ (Testcontainers) y verificar que el JSON publicado por el Adapter es *exactamente* lo que el Consumer espera (incluyendo formato de fecha String vs Array).

#### 3. E2E Test (Punta) — 10%
- **Flujo:** "Usuario envía Kudo y este aparece en el feed".
- **Riesgo Mitigado:** Configuración del entorno completo (DNS, Puertos, Credenciales).
- **Validación:** Cypress/Playwright simula al usuario logueado, envía el form, y espera que el WebSocket o el polling actualice la lista. Garantiza que "todo funciona junto".

### Prevención del Incidente "Kudo Fantasma"

| Nivel | ¿Lo detecta? | Explicación |
|---|---|---|
| **Unit** | ❌ No | Al mockear `ObjectMapper`, asumimos que serializa bien. El test pasa falsamente. |
| **Integration** | ✅ **SÍ** | Al usar un `RabbitTemplate` real contra un Testcontainer, el test falla si el mensaje llega como Array o si Rabbit lo rechaza. **Este es el nivel crítico para fallos de serialización.** |
| **E2E** | ✅ Sí | Pero tarde y costoso. El error saldría en el reporte de la noche, no en el PR del desarrollador. |

---

## 7. Implementación de Aprendizaje

Esta sección demuestra cómo aplicar la teoría de pruebas a nuestra arquitectura refactorizada.

### 7.1 Unit Test (Backend - Domain Layer)

**Objetivo:** Validar el patrón **Builder** y la integridad del Dominio.
**Principio Protegido:** SRP (La entidad se valida a sí misma) y Domain Integrity.

```java
// KudoUnit.test.java
@Test
@DisplayName("Debe lanzar excepción si se intenta crear un auto-kudo")
void shouldThrowException_WhenSenderIsSameAsReceiver() {
    // Arrange & Act & Assert
    assertThatThrownBy(() -> Kudo.builder()
            .fromUser("Christopher")
            .toUser("CHRISTOPHER") // Case-insensitive check
            .category(KudoCategory.TEAMWORK)
            .message("Gran trabajo")
            .build())
        .isInstanceOf(InvalidKudoException.class)
        .hasMessageContaining("Cannot send kudo to yourself");
}
```

**Análisis de Riesgo:** Mitiga el riesgo de corrupción de datos. Garantiza que *nunca* persistiremos un estado inválido, sin importar qué controlador o servicio invoque al dominio.

### 7.2 Integration Test (Backend - Infrastructure Layer)

**Objetivo:** Validar el patrón **Adapter** y la integración con RabbitMQ.
**Principio Protegido:** DIP (La implementación cumple el contrato del Port) y Contract Safety.

```java
// RabbitMqAdapter.int.test.java
@Testcontainers
@SpringBootTest
class RabbitMqKudoPublisherTest {

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management");

    @Autowired
    private KudoEventPublisher publisherAdapter; // El Adapter a probar

    @Test
    void shouldPublishJsonWithIso8601Dates() {
        // Arrange
        KudoEvent event = new KudoEvent("A", "B", "C", "Msg", LocalDateTime.of(2026, 2, 11, 12, 0));

        // Act
        publisherAdapter.publish(event);

        // Assert (Pseudocódigo de verificación de cola)
        String message = rabbit.consumeOneMessage();
        assertThat(message).contains("\"timestamp\":\"2026-02-11T12:00:00\""); // NO array
    }
}
```

**Análisis de Riesgo:** Preventivo directo del incidente "Kudo Fantasma". Asegura que el adaptador "habla" el protocolo correcto antes de desplegar.

### 7.3 End-to-End Test (Frontend - User Journey)

**Objetivo:** Validar el flujo crítico completo.
**Principio Protegido:** System Cohesion.

```typescript
// kudo-flow.spec.ts (Playwright)
test('Usuario envía un Kudo exitoso', async ({ page }) => {
  // 1. Navegar
  await page.goto('/');
  
  // 2. Interactuar (Page Object Pattern)
  await page.getByLabel('Para quién').fill('Santiago');
  await page.getByLabel('Categoría').selectOption('Desarrollo');
  await page.getByLabel('Mensaje').fill('Excelente refactorización');
  
  // 3. Actuar
  await page.getByRole('button', { name: 'Enviar Kudo' }).click();

  // 4. Verificar (Feedback al usuario)
  await expect(page.getByText('¡Kudo enviado!')).toBeVisible();
  
  // 5. Verificar (Efecto en sistema - opcional dependiendo del scope)
  // await expect(page.getByTestId('kudo-list')).toContainText('Santiago');
});
```

**Análisis de Riesgo:** Mitiga el riesgo de que componentes aislados funcionen bien pero fallen al orquestarse (e.g., CORS issues, errores de red, o desincronización Frontend/Backend).

---

## 8. Análisis de Confiabilidad (SRE)

Este análisis se centra en la robustez del sistema ante fallos, entradas inesperadas y condiciones de borde.

### 8.1 Matriz de Gaps de Validación

| Componente | Gap Detectado | Categoría | Severidad | ¿Quién lo detecta? |
|---|---|---|---|---|
| **Frontend** | `useSlider.ts` ignora el rechazo de la promesa (Unhandled Promise Rejection) | Error Handling | 🟡 Media | E2E / Manual |
| **Frontend** | Estado inconsistente en UI: Slider queda al 100% si falla la API | UX / State | 🟡 Media | Manual Exploratory |
| **Producer** | `GlobalExceptionHandler` expone `ex.getMessage()` en errores 500 | Security / Leakage | 🔴 Alta | Security Scan |
| **Consumer** | Validación de Dominio (`KudoCategory`) lanza `IllegalArgumentException` sin catch explícito | Poison Message | 🔴 Alta | Integration (Chaos) |
| **Domain** | Falta de sanitización de HTML en `message` (XSS Risk) | Security | 🟡 Media | Security Unit Test |

### 8.2 Detalles y Pruebas de Concepto (PoC)

#### Caso 1: UI State Inconsistency (Frontend)
**El Problema:**
En `useSlider.ts`, si `onComplete()` (el envío a la API) falla, la ejecución se detiene en el `await` y nunca llega a limpiar el estado del slider.

```typescript
// useSlider.ts
if (sliderValue > THRESHOLD) {
  setSliderValue(100); // 1. UI feedback inmediato
  if (onComplete) {
    await onComplete(); // 2. Si esto falla y lanza excepción...
  }
  // 3. ...esta línea NUNCA se ejecuta
  setTimeout(() => setSliderValue(0), 1000); 
}
```

**Consecuencia:** El usuario ve el slider completado (éxito visual) pero recibe un toast de error. Confusión garantizada.
**Test Propuesto (Jest/Vitest):**
```typescript
test('Slider should reset even if API fails', async () => {
  const mockApi = vi.fn().mockRejectedValue(new Error('Network Error'));
  const { result } = renderHook(() => useSlider(mockApi));
  
  // Simular arrastre al 100%
  // ...
  
  // Verificar que el valor vuelve a 0 eventualmente
  await waitFor(() => expect(result.current.sliderValue).toBe(0));
});
```

#### Caso 2: Information Leakage (Producer)
**El Problema:**
`GlobalExceptionHandler` devuelve `ex.getMessage()` directamente en el JSON de respuesta para errores 500.
**Riesgo:** Si falla la conexión a RabbitMQ, la excepción podría contener credenciales o IPs internas (`java.net.ConnectException: Connection refused to 10.0.1.5:5672`).
**Mitigación:** Devolver un UUID de correlación y loguear el stacktrace internamente.

#### Caso 3: Poison Message (Consumer)
**El Problema:**
Si llega un mensaje con `category: "SUPER_KUDO"` (inválido), `Kudo.builder()` lanza `IllegalArgumentException`.
El `@RabbitListener` captura la excepción.
- **Escenario Ideal:** DLQ configurada.
- **Escenario Realista (Riesgo):** Si la DLQ falla o no está configurada, RabbitMQ re-encola el mensaje infinitamente, consumiendo CPU y llenando logs.

**Test de Caos (Integration):**
```java
@Test
void whenCategoryIsInvalid_shouldSendToDLQ_andNotRetryLoop() {
    // 1. Publicar mensaje "venenoso" manualmente a Rabbit
    // 2. Esperar 5 segundos
    // 3. Verificar que el mensaje está en la cola DLQ
    // 4. Verificar que el consumer no entró en bucle infinito (logs count)
}
```

### 8.3 Recomendaciones SRE

1.  **Safe Fail in UI:** Envolver la llamada `await onComplete()` en un bloque `try/finally` dentro de `useSlider` para garantizar que `setSliderValue(0)` siempre se ejecute.
2.  **Sanitize Inputs:** Implementar una política de sanitización en el `Builder` o usar una librería en el Frontend para renderizar el mensaje.
3.  **Opaque Errors:** Modificar `GlobalExceptionHandler` para ocultar detalles internos en errores 500.

---

## 9. Referencia del Test Suite Implementado

Como parte de la fase de calidad, se ha implementado un conjunto mínimo de pruebas para validar los patrones arquitectónicos clave.

### 9.1 Unit Test (Backend: Builder Pattern)
**Archivo:** ``consumer-worker/src/test/java/com/sofkianos/consumer/domain/KudoBuilderTest.java``
- **Objetivo:** Verificar que el **Builder** impida la creación de objetos de dominio inválidos (e.g., auto-kudos).
- **Riesgo Mitigado:** Integridad del Dominio. Evita que datos "basura" o estados inválidos entren al núcleo del sistema.

### 9.2 Integration Test (Backend: Adapter Pattern)
**Archivo:** ``producer-api/src/test/java/com/sofkianos/producer/infrastructure/messaging/RabbitMqKudoPublisherTest.java``
- **Objetivo:** Verificar que el **Adapter** de RabbitMQ serialice correctamente las fechas (ISO-8601) usando un contenedor real de RabbitMQ (Testcontainers).
- **Riesgo Mitigado:** Contract Mismatch. Previene incidentes como "El Kudo Fantasma" asegurando que la infraestructura cumple el contrato esperado por el consumidor.

### 9.3 E2E Test (Frontend: Critical Flow)
**Archivo:** ``frontend/tests/kudo-flow.spec.ts``
- **Objetivo:** Simular un usuario real enviando un Kudo desde la UI.
- **Riesgo Mitigado:** System Cohesion. Verifica que todos los componentes (UI, Cliente HTTP, API, Cola, Worker) funcionen orquestados correctamente.

### 9.4 Alineación con la Pirámide
- **Base (Unit):** Validación rápida y barata de reglas de negocio complejas.
- **Medio (Integration):** Validación de puntos de frontera costosos (Serialización/Red).
- **Punta (E2E):** Validación final del valor entregado al usuario.




