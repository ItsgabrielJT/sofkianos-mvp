# Historias de Usuario de Despliegue (Alta Calidad)

---

## 🚀 Refactorización y estandarización de los Dockerfile para seguridad y eficiencia (US-001)

### 1. Definición de la HU
**Como** persona desarrolladora
**Quiero** refactorizar y estandarizar los Dockerfile de todos los servicios
**Para** garantizar imágenes seguras, eficientes y alineadas con las mejores prácticas

### 2. Especificaciones de Arquitectura y Despliegue
* **Capa de Clean Architecture:** Infraestructura
* **Patrón Aplicado:** Adaptadores de infraestructura, separación de build y runtime
* **Estrategia de Despliegue:** Rolling Update

### 3. Matriz de Calidad INVEST
| Criterio | Puntuación (0-3) | Justificación de la nota |
| :--- | :---: | :--- |
| **Independent** | 3 | Puede ejecutarse sin depender de otras historias |
| **Negotiable** | 3 | El enfoque de refactorización puede adaptarse |
| **Valuable** | 3 | Mejora seguridad y performance del producto |
| **Estimable** | 3 | Alcance claro y medible |
| **Small** | 2 | Puede requerir dividirse si hay muchos servicios |
| **Testable** | 3 | Se puede validar con linters y pruebas de build |

### 4. Validación (Gherkin)
- **Escenario:** Build seguro y eficiente de imágenes Docker
  - **Dado** que existen Dockerfile legacy en los servicios
  - **Cuando** se refactorizan y aplican buenas prácticas de seguridad y eficiencia
  - **Entonces** las imágenes resultantes pasan escaneo de vulnerabilidades y cumplen con los tiempos de build esperados

### 5. Definición de Hecho (DoD)
- [x] Instalada en entornos de pre-producción.
- [x] Pruebas de humo (Smoke Tests) superadas.
- [x] Pruebas de regresión completadas.
- [x] Dockerfile validados con linters y escáner de vulnerabilidades.

---

## 🚀 Mejorar y versionar los archivos docker-compose para ambientes dev, test y prod (US-002)

### 1. Definición de la HU
**Como** persona de operaciones
**Quiero** mejorar y versionar los archivos docker-compose para cada ambiente
**Para** facilitar despliegues consistentes y reproducibles en dev, test y prod

### 2. Especificaciones de Arquitectura y Despliegue
* **Capa de Clean Architecture:** Infraestructura
* **Patrón Aplicado:** Adaptadores de configuración, separación de ambientes
* **Estrategia de Despliegue:** Rolling Update

### 3. Matriz de Calidad INVEST
| Criterio | Puntuación (0-3) | Justificación de la nota |
| :--- | :---: | :--- |
| **Independent** | 3 | Cada ambiente puede mejorarse de forma aislada |
| **Negotiable** | 3 | Se puede ajustar la estructura de los archivos |
| **Valuable** | 3 | Reduce errores y acelera despliegues |
| **Estimable** | 3 | Alcance claro y delimitado |
| **Small** | 3 | Puede completarse en un sprint |
| **Testable** | 3 | Se valida con despliegues en cada ambiente |

### 4. Validación (Gherkin)
- **Escenario:** Despliegue reproducible por ambiente
  - **Dado** que existen archivos docker-compose desactualizados
  - **Cuando** se actualizan y versionan para dev, test y prod
  - **Entonces** los servicios se despliegan correctamente en cada ambiente sin errores de configuración

### 5. Definición de Hecho (DoD)
- [x] Instalada en entornos de pre-producción.
- [x] Pruebas de humo (Smoke Tests) superadas.
- [x] Pruebas de regresión completadas.
- [x] Versionado y documentación de los archivos docker-compose.

---

## 🚀 Fortalecer la gestión de secretos y variables sensibles usando vaults o servicios cloud (US-003)

### 1. Definición de la HU
**Como** persona de operaciones
**Quiero** gestionar secretos y variables sensibles mediante vaults o servicios cloud
**Para** proteger credenciales y datos críticos en los pipelines y despliegues

### 2. Especificaciones de Arquitectura y Despliegue
* **Capa de Clean Architecture:** Infraestructura
* **Patrón Aplicado:** Inversión de dependencias, externalización de configuración
* **Estrategia de Despliegue:** Rolling Update

### 3. Matriz de Calidad INVEST
| Criterio | Puntuación (0-3) | Justificación de la nota |
| :--- | :---: | :--- |
| **Independent** | 3 | Puede implementarse sin bloquear otras tareas |
| **Negotiable** | 3 | Se puede elegir la herramienta de gestión |
| **Valuable** | 3 | Reduce riesgos de seguridad |
| **Estimable** | 3 | Alcance claro y medible |
| **Small** | 3 | Implementable en un sprint |
| **Testable** | 3 | Se valida con pruebas de acceso y rotación de secretos |

### 4. Validación (Gherkin)
- **Escenario:** Protección de secretos en despliegue
  - **Dado** que existen variables sensibles en archivos de configuración
  - **Cuando** se migran a un vault o servicio cloud seguro
  - **Entonces** los secretos no son accesibles en texto plano y se audita su acceso

### 5. Definición de Hecho (DoD)
- [ ] Instalada en entornos de pre-producción.
- [ ] Pruebas de humo (Smoke Tests) superadas.
- [ ] Pruebas de regresión completadas.
- [ ] Acceso a secretos auditado y documentado.

---

## 🚀 Implementar escaneo automático de vulnerabilidades en imágenes Docker y dependencias (US-004)

### 1. Definición de la HU
**Como** persona responsable de seguridad
**Quiero** implementar escaneo automático de vulnerabilidades en imágenes Docker y dependencias
**Para** detectar y mitigar riesgos antes del despliegue a producción

### 2. Especificaciones de Arquitectura y Despliegue
* **Capa de Clean Architecture:** Infraestructura
* **Patrón Aplicado:** Adaptadores de seguridad, integración en pipeline
* **Estrategia de Despliegue:** Rolling Update

### 3. Matriz de Calidad INVEST
| Criterio | Puntuación (0-3) | Justificación de la nota |
| :--- | :---: | :--- |
| **Independent** | 3 | Puede integrarse sin bloquear otros procesos |
| **Negotiable** | 3 | Se puede ajustar la herramienta de escaneo |
| **Valuable** | 3 | Reduce riesgos de seguridad en producción |
| **Estimable** | 3 | Alcance claro y medible |
| **Small** | 3 | Implementable en un sprint |
| **Testable** | 3 | Se valida con reportes automáticos de escaneo |

### 4. Validación (Gherkin)
- **Escenario:** Escaneo de vulnerabilidades en pipeline
  - **Dado** que existen imágenes Docker y dependencias en el proyecto
  - **Cuando** se ejecuta el pipeline CI/CD
  - **Entonces** se generan reportes de vulnerabilidades y se bloquea el despliegue si hay hallazgos críticos

### 5. Definición de Hecho (DoD)
- [ ] Instalada en entornos de pre-producción.
- [ ] Pruebas de humo (Smoke Tests) superadas.
- [ ] Pruebas de regresión completadas.
- [ ] Reportes de escaneo integrados al pipeline.

---

## 🚀 Mejorar el pipeline CI/CD (Jenkinsfile) para incluir validaciones de seguridad, calidad y despliegue automatizado (US-005)

### 1. Definición de la HU
**Como** persona de DevOps
**Quiero** mejorar el pipeline CI/CD para incluir validaciones de seguridad, calidad y despliegue automatizado
**Para** asegurar entregas confiables y seguras en todos los ambientes

### 2. Especificaciones de Arquitectura y Despliegue
* **Capa de Clean Architecture:** Infraestructura
* **Patrón Aplicado:** Adaptadores de integración continua, validaciones automáticas
* **Estrategia de Despliegue:** Rolling Update

### 3. Matriz de Calidad INVEST
| Criterio | Puntuación (0-3) | Justificación de la nota |
| :--- | :---: | :--- |
| **Independent** | 3 | Puede implementarse sin bloquear otras tareas |
| **Negotiable** | 3 | Se puede ajustar el pipeline según necesidades |
| **Valuable** | 3 | Mejora la calidad y seguridad del producto |
| **Estimable** | 3 | Alcance claro y medible |
| **Small** | 2 | Puede requerir dividirse si el pipeline es complejo |
| **Testable** | 3 | Se valida con ejecuciones exitosas y reportes |

### 4. Validación (Gherkin)
- **Escenario:** Pipeline CI/CD robusto y seguro
  - **Dado** que existe un pipeline básico en Jenkins
  - **Cuando** se agregan validaciones de seguridad, calidad y despliegue automatizado
  - **Entonces** las entregas pasan todas las validaciones antes de desplegarse

### 5. Definición de Hecho (DoD)
- [ ] Instalada en entornos de pre-producción.
- [ ] Pruebas de humo (Smoke Tests) superadas.
- [ ] Pruebas de regresión completadas.
- [ ] Pipeline documentado y versionado.

---

## 🚀 Automatizar pruebas de integración y smoke tests post-despliegue (US-006)

### 1. Definición de la HU
**Como** persona de QA
**Quiero** automatizar pruebas de integración y smoke tests post-despliegue
**Para** validar la estabilidad y funcionalidad básica tras cada despliegue

### 2. Especificaciones de Arquitectura y Despliegue
* **Capa de Clean Architecture:** Núcleo (Use Cases) / Infraestructura
* **Patrón Aplicado:** Adaptadores de testing, integración en pipeline
* **Estrategia de Despliegue:** Rolling Update

### 3. Matriz de Calidad INVEST
| Criterio | Puntuación (0-3) | Justificación de la nota |
| :--- | :---: | :--- |
| **Independent** | 3 | Puede implementarse sin bloquear otras tareas |
| **Negotiable** | 3 | Se puede ajustar el alcance de las pruebas |
| **Valuable** | 3 | Reduce riesgos de regresión y fallos en producción |
| **Estimable** | 3 | Alcance claro y medible |
| **Small** | 3 | Implementable en un sprint |
| **Testable** | 3 | Se valida con reportes automáticos de pruebas |

### 4. Validación (Gherkin)
- **Escenario:** Pruebas automáticas post-despliegue
  - **Dado** que se realiza un despliegue en cualquier ambiente
  - **Cuando** se ejecutan pruebas de integración y smoke tests
  - **Entonces** se reportan resultados y se bloquea la promoción si hay fallos críticos

### 5. Definición de Hecho (DoD)
- [ ] Instalada en entornos de pre-producción.
- [ ] Pruebas de humo (Smoke Tests) superadas.
- [ ] Pruebas de regresión completadas.
- [ ] Reportes de pruebas integrados al pipeline.

---

## 🚀 Implementar monitoreo centralizado (logs, métricas, alertas) para todos los servicios (US-007)

### 1. Definición de la HU
**Como** persona de operaciones
**Quiero** implementar monitoreo centralizado de logs, métricas y alertas
**Para** detectar incidentes y anomalías en tiempo real en todos los servicios

### 2. Especificaciones de Arquitectura y Despliegue
* **Capa de Clean Architecture:** Infraestructura
* **Patrón Aplicado:** Adaptadores de observabilidad, integración con sistemas externos
* **Estrategia de Despliegue:** Rolling Update

### 3. Matriz de Calidad INVEST
| Criterio | Puntuación (0-3) | Justificación de la nota |
| :--- | :---: | :--- |
| **Independent** | 3 | Puede implementarse por servicio |
| **Negotiable** | 3 | Se puede elegir la herramienta de monitoreo |
| **Valuable** | 3 | Mejora la capacidad de respuesta ante incidentes |
| **Estimable** | 3 | Alcance claro y medible |
| **Small** | 2 | Puede requerir dividirse por servicio |
| **Testable** | 3 | Se valida con generación y visualización de alertas |

### 4. Validación (Gherkin)
- **Escenario:** Monitoreo centralizado activo
  - **Dado** que existen múltiples servicios desplegados
  - **Cuando** se integran con un sistema de monitoreo centralizado
  - **Entonces** se reciben alertas y métricas en tiempo real ante incidentes

### 5. Definición de Hecho (DoD)
- [ ] Instalada en entornos de pre-producción.
- [ ] Pruebas de humo (Smoke Tests) superadas.
- [ ] Pruebas de regresión completadas.
- [ ] Dashboards y alertas configurados y documentados.

---

## 🚀 Desplegar infraestructura como código (Terraform) con validaciones y rollback automático (US-008)

### 1. Definición de la HU
**Como** persona de DevOps
**Quiero** desplegar infraestructura como código usando Terraform con validaciones y rollback automático
**Para** asegurar consistencia, trazabilidad y recuperación ante fallos

### 2. Especificaciones de Arquitectura y Despliegue
* **Capa de Clean Architecture:** Infraestructura
* **Patrón Aplicado:** Adaptadores de IaC, validaciones automáticas
* **Estrategia de Despliegue:** Blue-Green

### 3. Matriz de Calidad INVEST
| Criterio | Puntuación (0-3) | Justificación de la nota |
| :--- | :---: | :--- |
| **Independent** | 3 | Puede ejecutarse sin bloquear otras tareas |
| **Negotiable** | 3 | Se puede ajustar la estrategia de rollback |
| **Valuable** | 3 | Reduce riesgos y tiempos de recuperación |
| **Estimable** | 3 | Alcance claro y medible |
| **Small** | 2 | Puede requerir dividirse por recursos |
| **Testable** | 3 | Se valida con despliegues y rollbacks controlados |

### 4. Validación (Gherkin)
- **Escenario:** Despliegue y rollback de infraestructura
  - **Dado** que se requiere provisionar infraestructura en cloud
  - **Cuando** se ejecuta Terraform con validaciones y rollback
  - **Entonces** la infraestructura se provisiona correctamente y se revierte ante fallos

### 5. Definición de Hecho (DoD)
- [ ] Instalada en entornos de pre-producción.
- [ ] Pruebas de humo (Smoke Tests) superadas.
- [ ] Pruebas de regresión completadas.
- [ ] Rollback documentado y probado.

---

## 🚀 Documentar el proceso de despliegue, rollback y recuperación ante fallos (US-009)

### 1. Definición de la HU
**Como** persona de operaciones
**Quiero** documentar el proceso de despliegue, rollback y recuperación ante fallos
**Para** asegurar la trazabilidad y facilitar la respuesta ante incidentes

### 2. Especificaciones de Arquitectura y Despliegue
* **Capa de Clean Architecture:** Infraestructura
* **Patrón Aplicado:** Documentación de procesos, externalización de conocimiento
* **Estrategia de Despliegue:** N/A

### 3. Matriz de Calidad INVEST
| Criterio | Puntuación (0-3) | Justificación de la nota |
| :--- | :---: | :--- |
| **Independent** | 3 | Puede ejecutarse en paralelo a otras tareas |
| **Negotiable** | 3 | El formato y detalle pueden ajustarse |
| **Valuable** | 3 | Facilita la gestión de incidentes |
| **Estimable** | 3 | Alcance claro y medible |
| **Small** | 3 | Implementable en un sprint |
| **Testable** | 3 | Se valida con revisiones y simulacros |

### 4. Validación (Gherkin)
- **Escenario:** Documentación de procesos críticos
  - **Dado** que existen procesos de despliegue y recuperación
  - **Cuando** se documentan y validan con el equipo
  - **Entonces** la documentación es accesible y comprensible para todos los miembros relevantes

### 5. Definición de Hecho (DoD)
- [ ] Instalada en entornos de pre-producción.
- [ ] Pruebas de humo (Smoke Tests) superadas.
- [ ] Pruebas de regresión completadas.
- [ ] Documentación revisada y aprobada por el equipo.

---

## 🚀 Automatizar backups y restauración de datos críticos (US-010)

### 1. Definición de la HU
**Como** persona de operaciones
**Quiero** automatizar backups y restauración de datos críticos
**Para** garantizar la disponibilidad y recuperación ante incidentes

### 2. Especificaciones de Arquitectura y Despliegue
* **Capa de Clean Architecture:** Infraestructura
* **Patrón Aplicado:** Adaptadores de backup, externalización de almacenamiento
* **Estrategia de Despliegue:** Rolling Update

### 3. Matriz de Calidad INVEST
| Criterio | Puntuación (0-3) | Justificación de la nota |
| :--- | :---: | :--- |
| **Independent** | 3 | Puede implementarse sin bloquear otras tareas |
| **Negotiable** | 3 | Se puede ajustar la frecuencia y alcance |
| **Valuable** | 3 | Reduce riesgos de pérdida de datos |
| **Estimable** | 3 | Alcance claro y medible |
| **Small** | 3 | Implementable en un sprint |
| **Testable** | 3 | Se valida con restauraciones exitosas |

### 4. Validación (Gherkin)
- **Escenario:** Backup y restauración automatizada
  - **Dado** que existen datos críticos en los servicios
  - **Cuando** se ejecutan backups y restauraciones automáticas
  - **Entonces** los datos pueden recuperarse íntegramente ante incidentes

### 5. Definición de Hecho (DoD)
- [ ] Instalada en entornos de pre-producción.
- [ ] Pruebas de humo (Smoke Tests) superadas.
- [ ] Pruebas de regresión completadas.
- [ ] Restauración documentada y validada periódicamente.

---

## 🚀 Validar cumplimiento de políticas de seguridad y compliance en el pipeline (US-011)

### 1. Definición de la HU
**Como** persona responsable de cumplimiento
**Quiero** validar el cumplimiento de políticas de seguridad y compliance en el pipeline
**Para** asegurar que el producto cumple normativas y estándares requeridos

### 2. Especificaciones de Arquitectura y Despliegue
* **Capa de Clean Architecture:** Infraestructura
* **Patrón Aplicado:** Adaptadores de compliance, validaciones automáticas
* **Estrategia de Despliegue:** Rolling Update

### 3. Matriz de Calidad INVEST
| Criterio | Puntuación (0-3) | Justificación de la nota |
| :--- | :---: | :--- |
| **Independent** | 3 | Puede implementarse sin bloquear otras tareas |
| **Negotiable** | 3 | Se puede ajustar el alcance de las validaciones |
| **Valuable** | 3 | Reduce riesgos legales y de seguridad |
| **Estimable** | 3 | Alcance claro y medible |
| **Small** | 3 | Implementable en un sprint |
| **Testable** | 3 | Se valida con reportes de cumplimiento |

### 4. Validación (Gherkin)
- **Escenario:** Validación de compliance en pipeline
  - **Dado** que existen políticas de seguridad y compliance
  - **Cuando** se ejecuta el pipeline CI/CD
  - **Entonces** se generan reportes de cumplimiento y se bloquea el despliegue si hay incumplimientos

### 5. Definición de Hecho (DoD)
- [ ] Instalada en entornos de pre-producción.
- [ ] Pruebas de humo (Smoke Tests) superadas.
- [ ] Pruebas de regresión completadas.
- [ ] Reportes de cumplimiento archivados.

---

## 🚀 Estrategia de transición y migración de datos entre versiones (US-012)

### 1. Definición de la HU
**Como** persona de operaciones
**Quiero** definir e implementar una estrategia de transición y migración de datos entre versiones
**Para** asegurar la integridad y disponibilidad de los datos durante actualizaciones

### 2. Especificaciones de Arquitectura y Despliegue
* **Capa de Clean Architecture:** Infraestructura
* **Patrón Aplicado:** Adaptadores de migración, externalización de scripts
* **Estrategia de Despliegue:** Rolling Update

### 3. Matriz de Calidad INVEST
| Criterio | Puntuación (0-3) | Justificación de la nota |
| :--- | :---: | :--- |
| **Independent** | 3 | Puede implementarse por módulo o servicio |
| **Negotiable** | 3 | Se puede ajustar la estrategia según el caso |
| **Valuable** | 3 | Reduce riesgos de pérdida o corrupción de datos |
| **Estimable** | 3 | Alcance claro y medible |
| **Small** | 2 | Puede requerir dividirse por tipo de dato |
| **Testable** | 3 | Se valida con migraciones simuladas y reales |

### 4. Validación (Gherkin)
- **Escenario:** Migración de datos segura
  - **Dado** que existen cambios de versión en los servicios
  - **Cuando** se ejecuta la estrategia de migración
  - **Entonces** los datos se mantienen íntegros y disponibles

### 5. Definición de Hecho (DoD)
- [ ] Instalada en entornos de pre-producción.
- [ ] Pruebas de humo (Smoke Tests) superadas.
- [ ] Pruebas de regresión completadas.
- [ ] Estrategia documentada y validada en simulacros.

---

## 🚀 Pruebas de performance y stress automatizadas en el pipeline (US-013)

### 1. Definición de la HU
**Como** persona de QA
**Quiero** automatizar pruebas de performance y stress en el pipeline
**Para** identificar cuellos de botella y asegurar la escalabilidad del sistema

### 2. Especificaciones de Arquitectura y Despliegue
* **Capa de Clean Architecture:** Núcleo (Use Cases) / Infraestructura
* **Patrón Aplicado:** Adaptadores de testing, integración en pipeline
* **Estrategia de Despliegue:** Rolling Update

### 3. Matriz de Calidad INVEST
| Criterio | Puntuación (0-3) | Justificación de la nota |
| :--- | :---: | :--- |
| **Independent** | 3 | Puede implementarse sin bloquear otras tareas |
| **Negotiable** | 3 | Se puede ajustar el alcance de las pruebas |
| **Valuable** | 3 | Mejora la calidad y escalabilidad del sistema |
| **Estimable** | 3 | Alcance claro y medible |
| **Small** | 3 | Implementable en un sprint |
| **Testable** | 3 | Se valida con reportes automáticos de pruebas |

### 4. Validación (Gherkin)
- **Escenario:** Pruebas de performance automatizadas
  - **Dado** que se realiza un despliegue en cualquier ambiente
  - **Cuando** se ejecutan pruebas de performance y stress
  - **Entonces** se reportan resultados y se bloquea la promoción si hay cuellos de botella críticos

### 5. Definición de Hecho (DoD)
- [ ] Instalada en entornos de pre-producción.
- [ ] Pruebas de humo (Smoke Tests) superadas.
- [ ] Pruebas de regresión completadas.
- [ ] Reportes de performance archivados.

---

## 🚀 Validación de infraestructura provisionada y destrucción segura de recursos obsoletos (US-014)

### 1. Definición de la HU
**Como** persona de DevOps
**Quiero** validar la infraestructura provisionada y automatizar la destrucción segura de recursos obsoletos
**Para** optimizar costos y reducir riesgos de seguridad

### 2. Especificaciones de Arquitectura y Despliegue
* **Capa de Clean Architecture:** Infraestructura
* **Patrón Aplicado:** Adaptadores de IaC, validaciones automáticas
* **Estrategia de Despliegue:** Rolling Update

### 3. Matriz de Calidad INVEST
| Criterio | Puntuación (0-3) | Justificación de la nota |
| :--- | :---: | :--- |
| **Independent** | 3 | Puede implementarse sin bloquear otras tareas |
| **Negotiable** | 3 | Se puede ajustar el alcance de la validación |
| **Valuable** | 3 | Reduce costos y riesgos de seguridad |
| **Estimable** | 3 | Alcance claro y medible |
| **Small** | 3 | Implementable en un sprint |
| **Testable** | 3 | Se valida con reportes y auditorías de recursos |

### 4. Validación (Gherkin)
- **Escenario:** Validación y destrucción segura de recursos
  - **Dado** que existen recursos provisionados en cloud
  - **Cuando** se ejecutan validaciones y destrucción automatizada
  - **Entonces** solo permanecen los recursos necesarios y los obsoletos se eliminan de forma segura

### 5. Definición de Hecho (DoD)
- [ ] Instalada en entornos de pre-producción.
- [ ] Pruebas de humo (Smoke Tests) superadas.
- [ ] Pruebas de regresión completadas.
- [ ] Auditoría de recursos y destrucción documentada.

---

## 🏗️ Plan de Migración: consumer-worker

### 1. Diagnóstico del Monolito
* **Problema detectado:** Lógica de negocio y persistencia acopladas en servicios Spring y entidades JPA. El dominio depende de detalles de infraestructura.
* **Riesgo:** Dificultad para testear unitariamente, bajo aislamiento de lógica, cambios costosos y acoplamiento fuerte a frameworks.

### 2. Historia de Usuario de Migración
**Como** Arquitecto de Software
**Quiero** extraer la lógica de negocio y persistencia a un núcleo limpio desacoplado de frameworks
**Para** facilitar la evolución tecnológica, testing y escalabilidad del servicio

### 3. Diseño de Clean Architecture
* **Capa Destino:** Domain (entidades y casos de uso), Application (servicios de orquestación), Infrastructure (adaptadores JPA, mensajería)
* **Puerto/Adaptador a crear:** KudoPersistencePort, KudoEventConsumerPort
* **Patrón de Despliegue:** Strangler Fig + Canary Deployment para transición progresiva

### 4. Matriz INVEST
| Criterio        | Puntos (0-3) | Observación |
| :---            | :---:        | :--- |
| **Independent** | 3            | Puede migrarse módulo a módulo |
| **Negotiable**  | 3            | Estrategia y alcance pueden ajustarse |
| **Valuable**    | 3            | Reduce deuda técnica y acelera releases |
| **Estimable**   | 2            | Alcance claro, pero posible refactor adicional |
| **Small**       | 2            | Puede dividirse por entidad/caso de uso |
| **Testable**    | 3            | Pruebas unitarias y de regresión posibles |

### 5. Criterios de Aceptación (Gherkin)
- **Escenario:** Migración exitosa de la lógica de persistencia de Kudos
  - **Dado** que la lógica reside en el núcleo limpio
  - **Cuando** se invoca a través del puerto definido
  - **Entonces** el resultado es idéntico al sistema anterior y no hay dependencias de infraestructura.

### 6. Definición de Hecho (DoD)
- [ ] Código refactorizado sigue el principio de Inversión de Dependencias.
- [ ] Pruebas de regresión en pre-producción comparando Monolito vs Clean.
- [ ] Cobertura de pruebas unitarias en el Núcleo > 90%.

---
## 🏗️ Plan de Migración: producer-api

### 1. Diagnóstico del Monolito
* **Problema detectado:** Lógica de orquestación y validación mezclada con detalles de mensajería (RabbitMQ) y DTOs en servicios Spring.
* **Riesgo:** Dificultad para evolucionar el flujo de negocio, bajo aislamiento, dependencias cruzadas y testing limitado.

### 2. Historia de Usuario de Migración
**Como** Arquitecto de Software
**Quiero** extraer la lógica de publicación y validación a casos de uso y puertos independientes
**Para** desacoplar el dominio de la infraestructura y facilitar pruebas y cambios futuros

### 3. Diseño de Clean Architecture
* **Capa Destino:** Domain (entidades y validaciones), Application (casos de uso), Infrastructure (adaptadores de mensajería)
* **Puerto/Adaptador a crear:** KudoEventPublisherPort, KudoValidationPort
* **Patrón de Despliegue:** Strangler Fig + Canary Deployment, manteniendo rutas API actuales

### 4. Matriz INVEST
| Criterio        | Puntos (0-3) | Observación |
| :---            | :---:        | :--- |
| **Independent** | 3            | Puede migrarse por flujo (publicación, validación) |
| **Negotiable**  | 3            | Alcance y orden de migración ajustables |
| **Valuable**    | 3            | Mejora mantenibilidad y calidad |
| **Estimable**   | 2            | Alcance claro, pero posible ajuste por dependencias |
| **Small**       | 2            | Puede dividirse por caso de uso |
| **Testable**    | 3            | Pruebas unitarias y de integración posibles |

### 5. Criterios de Aceptación (Gherkin)
- **Escenario:** Migración exitosa de la lógica de publicación de Kudos
  - **Dado** que la lógica reside en el núcleo limpio
  - **Cuando** se invoca a través del puerto definido
  - **Entonces** el resultado es idéntico al sistema anterior y las rutas API no cambian.

### 6. Definición de Hecho (DoD)
- [ ] Código refactorizado sigue el principio de Inversión de Dependencias.
- [ ] Pruebas de regresión en pre-producción comparando Monolito vs Clean.
- [ ] Cobertura de pruebas unitarias en el Núcleo > 90%.
