# TECHNICAL AUDIT — Sofkianos MVP (Full-Stack)

> **Project:** Sofkianos-MVP  
> **Date:** February 11, 2026  
> **Auditor:** Senior Software Architect & Frontend Architect  
> **Backend Stack:** Spring Boot · RabbitMQ · PostgreSQL · Maven  
> **Frontend Stack:** React 19 · Vite 7 · TypeScript 5.9 · Zustand 5 · TailwindCSS 3 · PrimeReact 10 · Axios · Zod 4 · react-hook-form 7 · Framer Motion 12 · Vitest 4

---

## Table of Contents

- [Executive Summary](#executive-summary)

**Part I — Backend Audit**

1. [Backend Architectural State](#1-backend-architectural-state)
2. [Backend SOLID Violations](#2-backend-solid-violations)
3. [Backend Code Smells & Technical Debt](#3-backend-code-smells--technical-debt)
4. [Backend Findings Matrix](#4-backend-findings-matrix)
5. [Backend Refactoring Recommendations](#5-backend-refactoring-recommendations)

**Part II — Frontend Audit**

6. [Frontend Architectural State](#6-frontend-architectural-state)
7. [Frontend SOLID Violations](#7-frontend-solid-violations)
8. [Frontend Code Smells](#8-frontend-code-smells)
9. [Frontend Detailed Findings Catalog](#9-frontend-detailed-findings-catalog)
10. [Frontend Quality Assessment](#10-frontend-quality-assessment)
11. [Frontend Risk Matrix](#11-frontend-risk-matrix)
12. [Frontend Strategic Recommendations](#12-frontend-strategic-recommendations)

**Part III — Consolidated**

13. [Cross-Cutting Concerns](#13-cross-cutting-concerns)

---

## Executive Summary

The Sofkianos MVP system follows an event-driven microservices architecture with three main components:

1. **producer-api:** REST API (Spring Boot) that receives requests and publishes them to RabbitMQ.
2. **consumer-worker:** Worker (Spring Boot) that consumes messages from RabbitMQ and persists data in PostgreSQL.
3. **frontend:** SPA Application (React/Vite) that interacts with the producer-api.

**General State:** The base architecture is functional for an MVP, decoupling request reception from heavy processing. However, both backend and frontend exhibit significant violations of SOLID principles and a lack of abstractions that will hinder scalability and maintenance.

### Critical Findings at a Glance

| Layer | Critical Issues |
|---|---|
| **Backend** | SRP violation in consumer's `KudoServiceImpl` (manual JSON parsing), DIP violation with direct `RabbitTemplate` coupling, no shared DTO kernel, anemic domain model |
| **Frontend** | 2 parallel API layers (`src/api/` vs `src/services/api/`), >55% dead code, missing Error Boundaries, hardcoded user data, Zustand installed but unused, stale closure bugs |

---

# PART I — BACKEND AUDIT

---

## 1. Backend Architectural State

The system is divided into two Spring Boot microservices communicating via RabbitMQ:

- **producer-api** receives HTTP requests, validates them, serializes to JSON, and publishes to a RabbitMQ exchange.
- **consumer-worker** listens on the queue, manually parses JSON messages, and persists `Kudo` entities to PostgreSQL.

**Key Concern:** The code tends to be "imperative" rather than declarative or domain-object oriented. Business logic is spread across service implementations rather than encapsulated in domain entities.

---

## 2. Backend SOLID Violations

### SRP (Single Responsibility Principle)

- **Critical Violation in `consumer-worker`:** The `KudoServiceImpl` class in the worker flagrantly violates SRP. Its responsibility should be to orchestrate the business logic of "saving a Kudo", but currently it is also responsible for **manually parsing JSON** and mapping fields.
  - *Location:* `com.sofkianos.consumer.service.impl.KudoServiceImpl.java`
  - *Evidence:* `JsonNode root = objectMapper.readTree(kudoJson);` and subsequent manual mapping.

- **Violation in `producer-api`:** `KudoServiceImpl` mixes business logic (validating/preparing the kudo) with infrastructural logic of JSON serialization and direct publication to RabbitMQ.

### DIP (Dependency Inversion Principle)

- **Infrastructure Coupling in `producer-api`:** `KudoServiceImpl` directly depends on `RabbitTemplate` (a concrete Spring AMQP implementation).
  - *Impact:* If the messaging broker needed to be changed (e.g., to Kafka or SQS), the domain service would have to be rewritten.
  - *Solution:* It should depend on a `MessagePublisher` or `EventBus` interface that abstracts the messaging implementation.

- **Coupling to Third-Party Libraries:** Both services directly depend on `ObjectMapper` (Jackson) within the business logic, coupling them to a specific serialization library.

---

## 3. Backend Code Smells & Technical Debt

### 3.1 Logic Duplication and "Shotgun Surgery"

- **Implicit Data Schema:** The JSON message structure (`from`, `to`, `category`, `message`) is defined in `KudoRequest` (producer) and *hardcoded* as strings in `KudoServiceImpl` (consumer).
- **Risk:** A change in a field name in the producer will silently break the consumer at runtime. There is no shared type safety.

### 3.2 Anemic Domain Model

- The entities (`Kudo`, `KudoRequest`) are mere data containers (Getters/Setters) without business logic. All logic resides in the services, leading to a procedural design disguised as OOP.

### 3.3 Primitive Obsession

- The `consumer-worker` receives and processes messages as raw `String`s instead of automatically deserializing them into typed objects or using a shared DTO.
- *Evidence:* `public void handleKudo(@Payload String message)` in `KudosConsumer`.

### 3.4 Generic Error Handling

- In `producer-api`, serialization exceptions are caught and re-thrown as generic `RuntimeException`s with the message "Error processing message". This hides the root cause and hinders specific monitoring.

---

## 4. Backend Findings Matrix

| Component | File / Class | Finding Type | Risk | Description |
|---|---|---|---|---|
| **Consumer** | `consumer-worker/.../service/impl/KudoServiceImpl.java` | **SRP Violation** | 🔴 High | Manually parses JSON. Hinders testing and maintenance. |
| **Consumer** | `consumer-worker/.../service/impl/KudoServiceImpl.java` | **Hardcoded Strings** | 🔴 High | Magic strings ("from", "to") duplicate the DTO contract. Error-prone. |
| **Producer** | `producer-api/.../service/impl/KudoServiceImpl.java` | **DIP Violation** | 🟡 Medium | Direct dependency on `RabbitTemplate`. Hinders infrastructure changes. |
| **General** | N/A | **Missing Abstraction** | 🔴 High | Lack of shared library for DTOs or schemas (Shared Kernel). |
| **Consumer** | `consumer-worker/.../component/KudosConsumer.java` | **Primitive Obsession** | 🟡 Medium | Receives `String` instead of object. Loses Spring's automatic validation. |
| **Producer** | `producer-api/.../service/impl/KudoServiceImpl.java` | **Generic Error Handling** | 🟡 Medium | Generic `RuntimeException` hides root cause. |

---

## 5. Backend Refactoring Recommendations

1. **Extract Shared Kernel:** Create a shared Maven module containing the DTOs (`KudoEvent`) so that producer and consumer share the contract and duplication is eliminated.
2. **Refactor Consumer Service:**
   - Remove `ObjectMapper` from the service.
   - Configure the `RabbitListener` to use Spring's `MessageConverter` and automatically deserialize the JSON to the shared DTO.
   - The service should only receive the DTO and call the repository or apply business logic.
3. **Apply Ports & Adapters (Hexagonal) in Producer:**
   - Create `KudoPublisherPort` interface in the domain.
   - Implement `RabbitMqKudoPublisher` in infrastructure using `RabbitTemplate`.
4. **Enrich the Domain Model:** Implement business validations in the `Kudo` entity if applicable, instead of leaving them scattered.

---

# PART II — FRONTEND AUDIT

---

## 6. Frontend Architectural State

### 6.1 Folder Structure

```
frontend/src/
├── api/                    ← LEGACY API layer (duplicated)
│   ├── axiosConfig.ts      ← Duplicate axios config
│   └── kudosApi.ts         ← Direct sendKudo() call
├── components/
│   ├── common/             ← CustomInput, CustomButton (PrimeReact wrappers)
│   ├── landing/            ← 6 landing page components
│   ├── layouts/            ← Hero, Footer, MainLayout, KudosList (DEAD)
│   ├── KudoForm.tsx        ← Main form (ACTIVE)
│   ├── Navbar.tsx          ← Navigation bar (ACTIVE)
│   └── index.ts            ← Barrel export
├── hooks/
│   ├── data/               ← useKudos (fetch data)
│   ├── forms/              ← useKudoForm, useHomeForm (DEAD)
│   ├── landing/            ← 3 animation hooks
│   ├── useApp.ts           ← Root navigation hook
│   └── index.ts            ← Barrel export
├── pages/                  ← ALL DEAD (unreferenced)
│   ├── public/
│   │   ├── Home.tsx
│   │   ├── KudoAppPage.tsx
│   │   └── LandingPage.tsx
│   └── index.ts
├── routes/
│   └── index.tsx           ← DEAD router (never mounted)
├── schemas/
│   ├── kudoFormSchema.ts   ← Active schema
│   ├── kudosSchema.ts      ← LEGACY schema (duplicate)
│   └── kudosSchema.test.ts ← Test for dead schema
├── services/
│   ├── api/
│   │   ├── client.ts       ← Axios config with interceptors
│   │   ├── kudosService.ts ← Real service
│   │   └── kudosService.mock.ts ← Mock service
│   └── index.ts            ← Mock/real factory
├── App.tsx                 ← Root component (monolithic SPA)
├── main.tsx                ← Entry point
└── index.css               ← Base styles
```

### 6.2 Structure Diagnosis

| Aspect | Status | Observation |
|---|---|---|
| Layered organization | ⚠️ Partial | Hybrid attempt between layered and monolith. Not consistently feature-based or layered |
| Barrel exports | ✅ Present | `components/index.ts`, `hooks/index.ts`, `services/index.ts` facilitate imports |
| Separation of concerns | ❌ Weak | `App.tsx` contains routing logic, layout, and conditional rendering all together |
| Dead code | ❌ Critical | >30% of code in `src/` is unreachable from `App.tsx` or `main.tsx` |

### 6.3 Component Hierarchy

```
main.tsx
└── App (root)
    ├── Navbar
    │     ├── Logo (inline)
    │     ├── Desktop Nav (inline)
    │     └── Mobile Menu (inline)
    ├── [isAppView = true]
    │     └── KudoForm (KudoFormSystem)
    │           └── useKudoForm (hook)
    │                 └── sendKudo() → api/kudosApi
    └── [isAppView = false]
          ├── LandingHero
          │     └── useLaunchSlider (hook)
          ├── LandingHowItWorks
          │     └── useArchitectureAnimation (hook)
          ├── LandingTech
          │     └── useInfiniteScroll (hook)
          └── LandingFooter
```

> **Note:** The pages `Home`, `KudoAppPage`, `LandingPage`, the layouts `MainLayout`, `Hero`, `Footer`, `KudosList`, the hook `useHomeForm`, and the hook `useKudos` are **NOT connected** to this tree.

### 6.4 State Management Strategy

| Mechanism | Actual Usage | File |
|---|---|---|
| `useState` (local) | App/landing navigation, transitions | `useApp.ts` |
| `useState` + `useForm` (react-hook-form) | Kudo form | `useKudoForm.ts` |
| `useState` (local) | Slider UI | `useLaunchSlider.ts` |
| `useState` (local) | Animations | `useArchitectureAnimation.ts` |
| **Zustand** | **NEVER USED** | Only in `package.json` |

**Conclusion:** There is no real global state. Everything is local state distributed across individual hooks. Zustand was installed but never implemented.

### 6.5 API Communication Strategy

Two **parallel and disconnected** API layers exist:

| Layer | Files | Used by | Status |
|---|---|---|---|
| `src/api/` | `axiosConfig.ts`, `kudosApi.ts` | `useKudoForm.ts` (ACTIVE) | Legacy, no interceptors |
| `src/services/api/` | `client.ts`, `kudosService.ts`, `kudosService.mock.ts` | `useHomeForm.ts` (DEAD) | More mature, with interceptors and mock switching |

**Impact:** The more mature layer (`services/api/`) with auth interceptors, 401 handling, and mock switching is **not used** by active flows. The active flow (`KudoForm`) uses the legacy layer without interceptors.

---

## 7. Frontend SOLID Violations

### 7.1 SRP — Single Responsibility Principle

#### Finding SRP-01: `App.tsx` assumes multiple responsibilities

| Field | Detail |
|---|---|
| **File** | `src/App.tsx` |
| **Description** | The root component simultaneously manages: (1) conditional routing between landing and app, (2) global layout, (3) background decoration rendering, and (4) animation transitions |
| **Lines** | 10-70 |
| **Impact** | Every new view or section requires modifying this file, also violating OCP |
| **Risk** | 🔴 **High** |

#### Finding SRP-02: `useKudoForm.ts` concentrates excessive logic

| Field | Detail |
|---|---|
| **File** | `src/hooks/forms/useKudoForm.ts` |
| **Description** | A single hook manages: (1) form data, (2) hardcoded users (line 12-17), (3) validation, (4) slider UI (drag events), (5) avatar loading logic, and (6) API call. At least **4 distinct responsibilities** |
| **Lines** | 1-101 |
| **Impact** | Impossible to reuse the slider or user logic without dragging the entire hook. Zero testability without refactoring |
| **Risk** | 🔴 **High** |

#### Finding SRP-03: `LandingHowItWorks.tsx` is a God Component

| Field | Detail |
|---|---|
| **File** | `src/components/landing/LandingHowItWorks.tsx` |
| **Description** | A **194-line** component containing: (1) Bézier curve calculation functions (line 5-25), (2) animated data packet rendering, (3) hardcoded SVG paths, and (4) 6 architecture nodes inline. The Bézier functions are animation domain logic that should be in a utility |
| **Lines** | 1-194 |
| **Impact** | Maintainability severely compromised. Any visual adjustment requires navigating 194 lines of dense code |
| **Risk** | 🟡 **Medium** (landing page, not main flow) |

#### Finding SRP-04: `MainLayout.tsx` mixes layout with business logic

| Field | Detail |
|---|---|
| **File** | `src/components/layouts/MainLayout.tsx` |
| **Description** | Accepts 11 props to control everything: hero text, input value, placeholders, form callbacks, error/success messages, and loading state. It's a "layout" that actually orchestrates the complete application |
| **Lines** | 8-128 |
| **Impact** | Not reusable as a generic layout. Each new feature requires adding more props |
| **Risk** | 🟡 **Medium** (dead code currently) |

---

### 7.2 OCP — Open/Closed Principle

#### Finding OCP-01: `App.tsx` is not extensible

| Field | Detail |
|---|---|
| **File** | `src/App.tsx` |
| **Description** | The `isAppView` toggle turns routing into a binary `if-else`. Adding a third view (e.g., dashboard, profile) requires modifying this conditional, violating OCP |
| **Lines** | 34-66 |
| **Impact** | Does not scale to multiple views/routes. The `AppRouter` in `routes/index.tsx` that would solve this is **dead** |
| **Risk** | 🔴 **High** |

#### Finding OCP-02: Kudo categories not dynamically extensible

| Field | Detail |
|---|---|
| **File** | `src/schemas/kudoFormSchema.ts` |
| **Description** | `KUDO_CATEGORIES` is defined as a static `const` array (line 3-8). Adding categories requires modifying source code |
| **Lines** | 3-8 |
| **Impact** | Low for now, but if categories become dynamic (backend-driven) a complete schema refactor will be needed |
| **Risk** | 🟢 **Low** |

---

### 7.3 DIP — Dependency Inversion Principle

#### Finding DIP-01: `useKudoForm.ts` depends on a concrete API implementation

| Field | Detail |
|---|---|
| **File** | `src/hooks/forms/useKudoForm.ts` (line 10) |
| **Description** | The hook directly imports `sendKudo` from `../../api/kudosApi`, which in turn directly imports `axiosConfig`. There is no abstraction or intermediate interface. It is impossible to inject a mock, stub, or alternative implementation without modifying the import |
| **Impact** | Unit testing the hook requires full module mocking. Impossible to swap API implementations |
| **Risk** | 🔴 **High** |

#### Finding DIP-02: `client.ts` has side-effects coupled to the DOM

| Field | Detail |
|---|---|
| **File** | `src/services/api/client.ts` (lines 35-38) |
| **Description** | The response interceptor directly accesses `localStorage` and does `window.location.href = '/login'` on 401. This couples the HTTP layer to the DOM/browser and to a non-existent route (`/login` doesn't exist) |
| **Impact** | (1) Impossible to test in server-side environment or test runner without window/localStorage mocks. (2) Redirecting to `/login` causes a navigation error |
| **Risk** | 🔴 **High** |

#### Finding DIP-03: `KudosList.tsx` depends directly on concrete hooks

| Field | Detail |
|---|---|
| **File** | `src/components/layouts/KudosList.tsx` (line 2) |
| **Description** | The component imports `useKudos` directly. It does not receive data as props, preventing its use as a presentational component and hindering testing |
| **Impact** | Not testable without mounting the complete hook and its dependency chain (service → axios → network) |
| **Risk** | 🟡 **Medium** (dead code currently) |

---

### 7.4 LSP — Liskov Substitution Principle

#### Finding LSP-01: API interfaces are not interchangeable

| Field | Detail |
|---|---|
| **Files** | `src/api/kudosApi.ts` vs `src/services/api/kudosService.ts` |
| **Description** | `sendKudo()` in `kudosApi.ts` returns `Promise<void>`, while `sendKudos()` in `kudosService.ts` returns `Promise<KudosResponse>`. They accept different input types (`KudoFormData` vs `KudosFormData`). They are not substitutable despite both representing "send a kudo" |
| **Impact** | Impossible to swap one implementation for the other without modifying consumers |
| **Risk** | 🟡 **Medium** |

---

### 7.5 ISP — Interface Segregation Principle

#### Finding ISP-01: `MainLayout` requires an overly broad contract

| Field | Detail |
|---|---|
| **File** | `src/components/layouts/MainLayout.tsx` |
| **Description** | The `MainLayoutProps` interface has **11 properties**. A consumer that only wants a layout with hero and footer is forced to know about form props (`onInputChange`, `onSubmit`, `buttonLabel`, etc.) |
| **Impact** | Cannot be used partially; the entire "interface" must be satisfied |
| **Risk** | 🟢 **Low** (dead code) |

---

## 8. Frontend Code Smells

### 8.1 Dead Code / Unreachable Code

| File / Folder | Type | Reason |
|---|---|---|
| `src/routes/index.tsx` | Complete router | `AppRouter` is never imported or mounted. `App.tsx` uses conditional `isAppView` instead of routing |
| `src/pages/public/Home.tsx` | Page | Only imported by the dead router |
| `src/pages/public/KudoAppPage.tsx` | Page | Uses `Link` from react-router but there is no active router |
| `src/pages/public/LandingPage.tsx` | Page | Only imported by the dead router |
| `src/pages/index.ts` | Barrel export | Only exports dead pages |
| `src/components/layouts/MainLayout.tsx` | Layout | Only used by `Home.tsx` (dead) |
| `src/components/layouts/Hero.tsx` | Component | Only used by `MainLayout.tsx` (dead) |
| `src/components/layouts/Footer.tsx` | Component | Only used by `MainLayout.tsx` (dead) |
| `src/components/layouts/KudosList.tsx` | Component | Only used by `MainLayout.tsx` (dead) |
| `src/components/layouts/Hero.test.tsx` | Test | Tests a dead component |
| `src/components/common/CustomInput.tsx` | Component | Only used by `MainLayout.tsx` (dead) |
| `src/components/common/CustomButton.tsx` | Component | Only used by `MainLayout.tsx` (dead) |
| `src/hooks/forms/useHomeForm.ts` | Hook | Only used by `Home.tsx` (dead) |
| `src/hooks/data/useKudos.ts` | Hook | Only used by `KudosList.tsx` (dead) |
| `src/services/api/kudosService.ts` | Service | Only used by dead hooks |
| `src/services/api/kudosService.mock.ts` | Mock | Only used by dead service |
| `src/services/api/client.ts` | API Client | Only used by dead service |
| `src/services/index.ts` | Factory | Only used indirectly by dead hooks |
| `src/schemas/kudosSchema.ts` | Schema | Only used by `useHomeForm.ts` (dead) |
| `src/schemas/kudosSchema.test.ts` | Test | Tests dead schema |

> **⚠️ Impact:** Approximately **~55-60% of source code** in `src/` is unreachable. This artificially inflates the codebase, generates confusion for new developers, and the tests cover code that never runs.

### 8.2 Duplicate API Layers

**Risk:** 🔴 **High**

Two completely independent Axios configurations exist:

```
src/api/axiosConfig.ts          →  Axios instance WITHOUT interceptors
src/services/api/client.ts      →  Axios instance WITH interceptors (auth, 401 redirect)
```

The active flow (`KudoForm → useKudoForm → sendKudo`) uses the instance **without interceptors**, while the more robust instance (`client.ts`) is in dead code.

### 8.3 Duplicate Schemas

**Risk:** 🟡 **Medium**

| Schema | File | Fields |
|---|---|---|
| `kudoFormSchema` | `schemas/kudoFormSchema.ts` | `from`, `to`, `category`, `message` (rich validation) |
| `kudosSchema` | `schemas/kudosSchema.ts` | Only `message` (basic validation) |

Both represent "a kudo" but with incompatible contracts. This can cause domain model drift.

### 8.4 Hardcoded Data

**Risk:** 🔴 **High**

```typescript
// hooks/forms/useKudoForm.ts (lines 12-17)
export const USERS = [
  { id: '1', name: 'Christopher Pallo', email: 'christopher@sofkianos.com', ... },
  { id: '2', name: 'Santiago', email: 'santiago@sofkianos.com', ... },
  { id: '3', name: 'Backend Team', email: 'backend@sofkianos.com', ... },
  { id: '4', name: 'Frontend Team', email: 'frontend@sofkianos.com', ... },
];
```

The user list is embedded in a form hook. It should come from the backend or at least from a centralized configuration file.

### 8.5 Stale Closures in Event Handlers

**Risk:** 🟡 **Medium**

#### In `useKudoForm.ts` (lines 71-88):

```typescript
useEffect(() => {
  const move = (e: MouseEvent) => handleMove(e.clientX);
  const up = () => handleEnd();  // ← captures handleEnd in closure

  if (isDragging) {
    window.addEventListener('mousemove', move);
    window.addEventListener('mouseup', up);
  }
  return () => { /* cleanup */ };
}, [isDragging, sliderValue]);  // ← handleEnd is NOT in dependencies
```

`handleEnd` (line 53) is an `async` function captured in a closure that is recreated on every render. The `useEffect` captures it but does not include it in dependencies, which can cause it to execute with stale `formData` state.

#### In `useLaunchSlider.ts` (lines 45-58):

Same pattern: `handleEnd` and `handleMove` are captured in closures inside the `useEffect` without being listed as dependencies.

### 8.6 Weak Typing with `any`

**Risk:** 🟡 **Medium**

| File | Line | Instance |
|---|---|---|
| `services/api/kudosService.ts` | 28 | `getAllKudos(): Promise<any>` |
| `services/api/kudosService.ts` | 20 | `catch (error: any)` |
| `services/api/kudosService.mock.ts` | 75 | `getAllKudos(): Promise<any>` |
| `services/api/kudosService.mock.ts` | 87 | `getKudoById(): Promise<any>` |
| `services/api/kudosService.mock.ts` | 103 | `deleteKudo(): Promise<any>` |
| `hooks/forms/useHomeForm.ts` | 49 | `catch (err: any)` |

Using `any` erodes TypeScript benefits, especially in API responses where type integrity is critical.

### 8.7 Mixed UI Framework Coupling

**Risk:** 🟡 **Medium**

The application mixes **three UI systems** without a clear strategy:

| System | Used in |
|---|---|
| **TailwindCSS** | Entire app, extensive inline styles |
| **PrimeReact** | `CustomInput.tsx` (InputTextarea), `CustomButton.tsx` (Button) |
| **PrimeFlex** | Imported in `main.tsx` but no visible usage |

The `common/` components are PrimeReact wrappers that are **not used** in active flows. The active `KudoForm` uses native HTML inputs with Tailwind.

### 8.8 Hardcoded Colors (Magic Values)

**Risk:** 🟢 **Low**

The theme color `#FF5F00` appears **>40 times** across components as inline Tailwind values. It should be centralized in the Tailwind configuration as a design system token (e.g., `text-brand`, `bg-brand`).

### 8.9 Missing Error Boundaries

**Risk:** 🔴 **High**

There are no `React.ErrorBoundary` instances in the entire application. An uncaught error in any child component will crash the entire SPA without any feedback to the user.

### 8.10 Inline Styles and Direct DOM Manipulation

**Risk:** 🟢 **Low**

| File | Line | Type |
|---|---|---|
| `LandingHero.tsx` | 42-56 | `onMouseEnter`/`onMouseLeave` manipulate `e.currentTarget.style` directly instead of using state or CSS |
| `useInfiniteScroll.ts` | 22 | `trackRef.current.style.transform` manipulates DOM directly |
| `LandingHowItWorks.tsx` | 55, 19 | `style={{ animationDelay }}` inline |

### 8.11 Console Statements in Production

**Risk:** 🟢 **Low**

```typescript
// hooks/data/useKudos.ts (line 24)
console.error('Error fetching kudos:', error);
```

There is no centralized logging service. Errors are lost in the browser console.

---

## 9. Frontend Detailed Findings Catalog

| ID | File / Resource | Principle / Smell | Description | Risk |
|---|---|---|---|---|
| F-01 | `App.tsx` | SRP, OCP | Binary conditional routing, layout and transitions coupled | 🔴 High |
| F-02 | `useKudoForm.ts` | SRP, DIP | Monolithic hook: form + slider + avatar + API + hardcoded data | 🔴 High |
| F-03 | `api/` vs `services/api/` | Duplication | Two parallel API layers with different Axios configurations | 🔴 High |
| F-04 | `kudoFormSchema.ts` vs `kudosSchema.ts` | Duplication | Two schemas for the same domain entity | 🟡 Medium |
| F-05 | `useKudoForm.ts` L12-17 | Hardcoded Data | User list embedded in hook | 🔴 High |
| F-06 | `routes/`, `pages/`, `layouts/` | Dead Code | >55% of codebase is unreachable | 🔴 High |
| F-07 | `client.ts` L35-38 | DIP, Side Effects | Interceptor with `window.location` and direct `localStorage` | 🔴 High |
| F-08 | Entire app | Missing Error Boundary | No React Error Boundaries | 🔴 High |
| F-09 | `useKudoForm.ts`, `useLaunchSlider.ts` | Stale Closure | `useEffect` with incorrect dependencies | 🟡 Medium |
| F-10 | `package.json` | Unused Dependency | Zustand installed but never imported | 🟢 Low |
| F-11 | `services/api/*.ts` | Weak Typing | `Promise<any>` in service return types | 🟡 Medium |
| F-12 | Multiple components | Magic Values | `#FF5F00` hardcoded >40 times | 🟢 Low |
| F-13 | `LandingHowItWorks.tsx` | SRP, God Component | 194 lines: Bézier math + SVG + 6 nodes + animation | 🟡 Medium |
| F-14 | `main.tsx` | Mixed UI Frameworks | PrimeReact + PrimeFlex + Tailwind imported simultaneously | 🟡 Medium |
| F-15 | `LandingHero.tsx` L45-57 | Imperative DOM | Direct `.style` manipulation in event handlers | 🟢 Low |
| F-16 | `LandingNav.tsx`, `KudoAppPage.tsx` | Dead Dependency | Use `Link` from react-router-dom but no routing provider exists | 🟡 Medium |
| F-17 | `Navbar.tsx` | SRP | Manages its own scroll state and mobile menu internally | 🟢 Low |
| F-18 | `client.ts` L38 | Broken Navigation | Redirects to `/login` which does not exist as a route | 🔴 High |

---

## 10. Frontend Quality Assessment

### 10.1 Component Reusability

| Criterion | Rating | Justification |
|---|---|---|
| Pure presentational components | ⭐⭐☆☆☆ (2/5) | `Hero`, `Footer`, `LandingFooter` are presentational but dead. Active components (`KudoForm`, `Navbar`) have embedded logic |
| Design System / Token System | ⭐⭐☆☆☆ (2/5) | Hardcoded colors, no tokens in tailwind config, no reusable atomic components |
| Reusable hooks | ⭐⭐⭐☆☆ (3/5) | `useInfiniteScroll`, `useLaunchSlider`, `useArchitectureAnimation` are good examples. But `useKudoForm` is monolithic |

### 10.2 Presentation / Logic Separation

| Criterion | Rating | Justification |
|---|---|---|
| Landing components | ⭐⭐⭐⭐☆ (4/5) | Good hook extraction (`useLaunchSlider`, `useInfiniteScroll`). Except `LandingHowItWorks` |
| Main flow (KudoForm) | ⭐⭐⭐☆☆ (3/5) | Logic extracted to `useKudoForm` (good), but the hook mixes UI (slider) with business logic (API, validation) |
| Layouts | ⭐⭐☆☆☆ (2/5) | `MainLayout` violates separation by accepting 11 business logic props |

### 10.3 Testability

| Criterion | Rating | Justification |
|---|---|---|
| Existing tests | ⭐⭐☆☆☆ (2/5) | Only 2 test files: `Hero.test.tsx` (dead component) and `kudosSchema.test.ts` (dead schema). Tests cover 0% of active functionality |
| Design for testability | ⭐⭐☆☆☆ (2/5) | Direct axios dependencies hinder mocking. No dependency injection. No props-based data flow in key components |
| Testing infrastructure | ⭐⭐⭐⭐☆ (4/5) | Vitest + Testing Library + jsdom correctly configured |

### 10.4 API Error Handling

| Criterion | Rating | Justification |
|---|---|---|
| Error handling in services | ⭐⭐⭐☆☆ (3/5) | `kudosService.ts` catches errors and re-throws with readable messages |
| Error handling in hooks | ⭐⭐☆☆☆ (2/5) | `useKudoForm` shows `toast.error('Error enviando')` without detail. `useKudos` does `console.error` without error UX |
| Error Boundaries | ⭐☆☆☆☆ (1/5) | Completely absent |
| User feedback | ⭐⭐⭐☆☆ (3/5) | Uses `sonner` (toast) for success/error, but no visual error states in the form UI |

### 10.5 State Management Consistency

| Criterion | Rating | Justification |
|---|---|---|
| Consistent pattern | ⭐⭐☆☆☆ (2/5) | Mixes `useState`, `react-hook-form`, and a phantom Zustand. No clear convention |
| Global state | ⭐☆☆☆☆ (1/5) | Does not exist. `isAppView` should be global state but lives in a local hook of `App.tsx` |
| State synchronization | ⭐⭐☆☆☆ (2/5) | No mechanism to synchronize between views if the app grows |

---

## 11. Frontend Risk Matrix

### Distribution of Findings by Risk

```
🔴 High     ████████  8 findings
🟡 Medium   ██████    6 findings
🟢 Low      ████      4 findings
```

### High Risk Findings (require immediate attention)

| ID | Summary |
|---|---|
| F-01 | `App.tsx` as monolithic orchestrator without real routing |
| F-02 | `useKudoForm` mixes 4+ responsibilities |
| F-03 | Two parallel API layers (confusion and drift) |
| F-05 | Users hardcoded in hook |
| F-06 | >55% dead code |
| F-07 | Interceptor with side-effects to the DOM |
| F-08 | No Error Boundaries |
| F-18 | Redirect to non-existent `/login` |

---

## 12. Frontend Strategic Recommendations

### 12.1 Immediate Priority (Sprint 1)

1. **Remove dead code:** Remove `routes/`, `pages/`, `layouts/`, `hooks/forms/useHomeForm.ts`, `hooks/data/useKudos.ts`, `schemas/kudosSchema.ts`, `api/axiosConfig.ts`, and the complete `services/` folder, or **activate** the router and pages as a replacement for the conditional in `App.tsx`.

2. **Consolidate API layer:** Choose a single layer (`services/api/` recommended) and migrate the active flow to it. Delete `src/api/`.

3. **Add Error Boundaries:** Implement at least one global `ErrorBoundary` in `main.tsx` and one per critical section (form).

### 12.2 High Priority (Sprint 2)

4. **Refactor `useKudoForm`:** Decompose into:
   - `useSlider()` — drag logic
   - `useUsers()` — fetch users from API
   - `useKudoForm()` — only form and validation
   - `useAvatarPreview()` — avatar loading logic

5. **Implement real routing:** Migrate from the `isAppView` conditional to `react-router-dom` with defined routes. The `AppRouter` already exists as a base.

6. **Extract hardcoded data:** Move users to an API endpoint or at least to a configuration file (`config/users.ts`).

### 12.3 Medium Priority (Sprint 3-4)

7. **Implement Zustand** or remove it: If global state is needed (authenticated user, preferences), activate Zustand. If not, remove it from `package.json`.

8. **Create Design System tokens:** Centralize colors (`#FF5F00` → `brand`), spacing, and typography in the Tailwind configuration.

9. **Type API responses:** Replace all `Promise<any>` with typed interfaces (`KudosListResponse`, `KudoDetailResponse`, etc.).

10. **Decouple `LandingHowItWorks`:** Extract Bézier functions to `utils/bezier.ts` and create sub-components for each architecture node.

### 12.4 Low Priority (Sprint 5+)

11. **Testing:** Add tests for active flows (`KudoForm`, `useKudoForm`, `Navbar`, landing components).

12. **Remove PrimeReact/PrimeFlex:** If the active UI does not use them, remove the dependencies to reduce bundle size.

13. **Implement logging service:** Replace `console.error` with a centralized service.

---

# PART III — CROSS-CUTTING CONCERNS

---

## 13. Cross-Cutting Concerns

### 13.1 Contract Drift Between Frontend and Backend

The frontend `KudoFormData` schema (fields: `from`, `to`, `category`, `message`) and the backend `KudoRequest` DTO define the same data structure independently. Changes in either side can silently break the other.

**Recommendation:** Consider an API contract mechanism (e.g., OpenAPI schema generation) or at minimum, document the API contract in a shared specification file.

### 13.2 No Shared Error Strategy

- **Backend** throws generic `RuntimeException` with "Error processing message".
- **Frontend** displays generic `toast.error('Error enviando')` without parsing the backend error shape.
- Neither side has a standardized error response format.

**Recommendation:** Define a unified error response DTO (`{ code, message, details }`) on the backend and a typed error handler on the frontend.

### 13.3 Authentication Mismatch

- `client.ts` (frontend, dead code) reads `localStorage.getItem('authToken')` and sends it as `Bearer` token.
- Neither backend service validates authentication tokens.
- The 401 handler redirects to `/login` — a route that does not exist.

**Recommendation:** Either implement authentication end-to-end or remove the auth interceptor to avoid confusion.

---

> **Conclusion:** The Sofkianos MVP has a functional event-driven architecture with a visually polished frontend. However, both layers exhibit significant technical debt: the backend suffers from SRP/DIP violations and an anemic domain model, while the frontend is burdened by >55% dead code, duplicate API layers, and no global state strategy. The remediation roadmap outlined above follows an incremental approach that prioritizes eliminating confusion (dead code, duplicate layers, contract drift) before adding new capabilities.
