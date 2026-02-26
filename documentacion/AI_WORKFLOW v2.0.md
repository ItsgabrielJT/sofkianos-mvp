# AI Workflow — SofkianOS v2.0

> **Document:** AI-Augmented Architecture & Quality Workflow
> **Team:** SofkianOS
> **Version:** 2.0
> **Effective Date:** February 12, 2026
> **Supersedes:** AI_WORKFLOW.md v1.0, promps.md
> **Classification:** Internal Engineering Standard

---

## Table of Contents

1. [Methodology Evolution](#1-methodology-evolution)
2. [AI Roles & Responsibilities](#2-ai-roles--responsibilities)
3. [Prompt Protocol](#3-prompt-protocol)
4. [Quality Gate Protocol](#4-quality-gate-protocol)
5. [Commit Validation Flow](#5-commit-validation-flow)
6. [Git Flow](#6-git-flow)
7. [Prompt Catalog v2](#7-prompt-catalog-v2)
8. [Governance Rules](#8-governance-rules)

---

## 1. Methodology Evolution

### From AI-First to AI-Augmented Architecture & Quality

| Dimension | v1.0 (AI-First) | v2.0 (AI-Augmented) |
|-----------|-----------------|---------------------|
| AI Role | Junior Developer | Multi-role: Developer, Architect Tutor, Quality Analyst, Quality Gate |
| Scope | Code generation | Architecture validation, quality enforcement, debt prevention |
| Quality | Post-hoc review | Pre-commit automated gate |
| Patterns | Ad-hoc | Justified against audit findings with trade-off analysis |
| Debt | Not tracked | Classified (Fowler), interest-rated, governed |
| Prompts | Generic [ROLE/CONTEXT/CONSTRAINT/OUTPUT] | Phase-specific catalog with standardized templates |
| Maturity | Level 1 (Initial) | Level 2 (Managed) |

### Core Principles

1. **Humans = Architects & Governors.** Define vision, architecture, quality criteria, and governance rules.
2. **AI = Multi-Role Executor.** Implements, reviews, validates, and gates — under human direction.
3. **No commit without Quality Gate.** Every change passes AI-driven validation before entering the repository.
4. **No refactor without justification.** Every structural change references an audit finding or architectural rationale.
5. **No pattern without trade-off.** Every pattern adoption documents the problem, the alternative considered, and why it was rejected.
6. **No manual boilerplate.** All scaffolding and repetitive structure is AI-generated through prompts.

---

## 2. AI Roles & Responsibilities

### 2.1 Role Matrix

| Role | Activation | Responsibilities | Owner |
|------|------------|-----------------|-------|
| **Junior Developer** | Feature implementation, bug fixes | Write code, unit tests, documentation from approved specs. Follow existing patterns. No architectural decisions. | AI |
| **Architect Tutor** | Pattern research, refactoring design | Analyze GoF patterns against current architecture. Justify selections. Explain trade-offs. Propose refactoring strategies. | AI |
| **Quality Analyst** | Post-refactor validation, incident analysis | Distinguish Error/Defect/Failure. Perform SOLID compliance checks. Identify regressions. Design test pyramid strategy. | AI |
| **Quality Gate** | Pre-commit / pre-PR | Enforce SOLID, detect code smells, assess coupling risk, evaluate debt impact, verify test coverage. Block or approve. | AI |
| **Strategy & Architecture** | All phases | Product direction, architecture decisions, governance rules, prompt engineering, security review, PR approval. | Humans |
| **Final Approval** | PR merge | Security review, architectural sign-off, merge authorization. | Humans |

### 2.2 Role Activation Protocol

Roles are activated via the `[ROLE]` directive in each prompt. The AI does **not** self-select roles.

```
[ROLE] Act as {role_name} for {scope}.
```

**Escalation:** If the AI detects an issue outside its activated role (e.g., a security vulnerability found during a code review), it must flag it explicitly and recommend human escalation — never act outside its assigned role.

---

## 3. Prompt Protocol

### Standard Structure

Every AI request **must** include all four directives:

```
[ROLE]       — Who the AI acts as. One of the defined roles above.
[CONTEXT]    — Background: phase, architecture, files, decisions, prior findings.
[CONSTRAINT] — Hard limits: stack, patterns, style, security, scope boundaries.
[OUTPUT]     — Exact deliverable: file name, format, structure, acceptance criteria.
```

### Validation Rules

| Rule | Enforcement |
|------|-------------|
| All four directives present | Prompt rejected if any is missing |
| Role matches defined roles | No ad-hoc roles; use the role matrix |
| Context references source documents | Link to AUDITORIA.md, DEUDA_TECNICA.md, or relevant ADR |
| Constraints include "do not modify code" for analysis prompts | Prevents unintended changes during audits |
| Output specifies file name and structure | Ensures traceability and consistency |

---

## 4. Quality Gate Protocol

### 4.1 Mandatory Pre-Commit Flow

```
Developer completes change
        │
        ▼
┌─────────────────────────┐
│  STEP 1: Self-Check     │  Developer reviews own diff against checklist
│  (Developer)            │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│  STEP 2: AI Quality     │  AI scans change for SOLID violations,
│  Gate Prompt             │  code smells, coupling, debt impact
│  (Quality Gate Role)     │
└────────┬────────────────┘
         │
    ┌────┴────┐
    │         │
  PASS      FAIL
    │         │
    ▼         ▼
  Commit    Fix findings → Re-run Gate
```

### 4.2 Quality Gate Checklist

The AI Quality Gate evaluates every change against these criteria:

| # | Category | Checks |
|---|----------|--------|
| 1 | **SOLID Compliance** | SRP: single reason to change? DIP: depends on abstractions? OCP: open for extension? |
| 2 | **Code Smells** | God class? Feature envy? Long method? Primitive obsession? Data clumps? |
| 3 | **Coupling Risk** | New dependencies introduced? Infrastructure leaking into domain? Tight coupling to specific implementation? |
| 4 | **Debt Impact** | Does this change introduce new debt? Does it resolve existing debt? Fowler quadrant classification. |
| 5 | **Test Coverage** | New logic covered by test? Test validates the correct risk? Test pyramid level appropriate? |
| 6 | **Pattern Integrity** | Existing patterns respected? No anti-pattern regression? Pattern usage justified? |
| 7 | **Security** | Credentials hardcoded? Input validated? Exceptions handled? Auth boundaries respected? |

### 4.3 Standard Quality Gate Prompt

```
[ROLE]
Act as a Quality Gate Analyst for SofkianOS.

[CONTEXT]
A developer is about to commit the following changes:
- Files modified: {list_of_files}
- Purpose: {brief_description}
- Related debt items: {DTB/DTF/DTC IDs if applicable}

Architecture context:
- Event-driven microservices (Spring Boot + RabbitMQ + PostgreSQL)
- Hexagonal architecture with ports/adapters
- React/Vite SPA frontend
- Patterns in use: Builder, Adapter, Strategy

[CONSTRAINT]
- Do NOT modify code.
- Evaluate strictly against the Quality Gate Checklist (7 categories).
- Be objective. Flag partial compliance as CONDITIONAL PASS.
- Reference specific files and line ranges.
- If the change introduces new technical debt, classify it using Martin Fowler's quadrant.

[OUTPUT]
Generate a Quality Gate Report with:
1. Verdict: PASS / CONDITIONAL PASS / FAIL
2. Per-category assessment (7 categories, each: ✅ Pass / ⚠️ Warning / ❌ Fail)
3. Findings detail (file, issue, severity, recommendation)
4. Debt impact assessment (new debt introduced? existing debt resolved?)
5. Required actions before commit (if any)
```

### 4.4 Verdict Definitions

| Verdict | Meaning | Action |
|---------|---------|--------|
| **PASS** | All 7 categories clear. No findings. | Proceed to commit. |
| **CONDITIONAL PASS** | Minor warnings (Low severity). No structural issues. | Document warnings, proceed to commit. |
| **FAIL** | One or more categories failed. Medium/High findings. | Fix all findings. Re-run Quality Gate. |

---

## 5. Commit Validation Flow

For significant commits (features, refactors, architectural changes), the full 5-step validation flow is required.

### Step-by-Step Protocol

| Step | Role | Purpose | Required For |
|------|------|---------|--------------|
| **Step 1** | Developer | Submit change description with intent and scope | All commits |
| **Step 2** | Architect Tutor | Architectural review: pattern compliance, coupling, SOLID | Refactors, new features |
| **Step 3** | Quality Analyst | Quality analysis: code smells, regressions, test coverage | All significant changes |
| **Step 4** | Quality Gate | Debt classification: Fowler quadrant for any new/resolved debt | Changes touching debt items |
| **Step 5** | Human | Final approval statement | All PR merges |

### Step 1: Developer Prompt

```
[ROLE] Act as a Code Review Assistant.
[CONTEXT] I am submitting the following change:
- Branch: {branch_name}
- Files: {modified_files}
- Intent: {what_and_why}
- Related items: {ticket, debt ID, or ADR reference}
[CONSTRAINT] Summarize only. Do not evaluate yet.
[OUTPUT] Structured change summary: scope, impact, dependencies, risk areas.
```

### Step 2: Architectural Review Prompt

```
[ROLE] Act as an Architect Tutor reviewing a proposed change.
[CONTEXT]
Change summary from Step 1: {summary}
Current architecture: Hexagonal, event-driven, Builder/Adapter/Strategy patterns.
Reference documents: AUDITORIA.md, DEUDA_TECNICA.md.
[CONSTRAINT]
- Evaluate against SOLID principles.
- Verify pattern compliance (no anti-pattern regression).
- Assess coupling impact.
- Do NOT modify code.
[OUTPUT]
Architectural Review:
1. SOLID compliance verdict (per principle)
2. Pattern integrity assessment
3. Coupling risk (Low / Medium / High)
4. Architectural recommendation (Approve / Revise / Reject)
```

### Step 3: Quality Analysis Prompt

```
[ROLE] Act as a Quality Analyst.
[CONTEXT]
Change summary: {summary}
Architectural review result: {step_2_verdict}
[CONSTRAINT]
- Detect code smells.
- Verify test coverage for new logic.
- Check error handling completeness.
- Assess regression risk.
[OUTPUT]
Quality Analysis:
1. Code smell findings (type, file, severity)
2. Test coverage assessment (covered / gap identified)
3. Error handling completeness
4. Regression risk (Low / Medium / High)
5. Quality verdict (Pass / Conditional / Fail)
```

### Step 4: Debt Classification Prompt

```
[ROLE] Act as a Technical Debt Strategist.
[CONTEXT]
This change resolves or introduces technical debt.
Reference: DEUDA_TECNICA.md
Fowler Quadrant: Prudent/Reckless × Deliberate/Inadvertent
[CONSTRAINT]
- Classify any new debt using the quadrant.
- If resolving debt, confirm resolution against DEUDA_TECNICA.md item.
- Assign risk severity and interest rate.
[OUTPUT]
Debt Impact Report:
1. Debt items resolved (IDs, confirmation)
2. New debt introduced (description, quadrant, risk, interest)
3. Net debt change (improved / neutral / degraded)
4. Updated refactoring pressure estimate
```

### Step 5: Approval Statement Format

The human reviewer signs off with:

```
APPROVAL — {branch_name}
Date: {YYYY-MM-DD}
Reviewer: {name}
Quality Gate: {PASS/CONDITIONAL PASS}
Architectural Review: {APPROVE/REVISE}
Debt Impact: {IMPROVED/NEUTRAL/DEGRADED}
Notes: {any observations}
Merged: {YES/NO}
```

---

## 6. Git Flow

### Branch Model

| Branch | Purpose | Protection |
|--------|---------|------------|
| `main` | Production-ready code | Merge only from `develop` via release process. Human approval required. |
| `develop` | Integration branch | All feature work merges here. Quality Gate required for PRs. |
| `feature/*` | One branch per feature/task | Branched from `develop`. Quality Gate + human review before merge. |
| `fix/*` | Bug fixes | Same flow as `feature/*`. |

### Commit Flow with Quality Gate

```
1. Create feature/{name} from develop
2. Work on branch (human + AI)
3. Run Quality Gate Prompt (Step 3 minimum)
4. For significant changes: run full 5-step flow
5. Open PR into develop
6. Human performs Security Review + Architectural Sign-off
7. Human merges PR
8. Delete feature branch
```

### Commit Message Standard

```
{type}({scope}): {description}

Quality Gate: {PASS/CONDITIONAL}
Debt: {resolved: DTB-XX | introduced: none | N/A}
```

**Types:** `feat`, `fix`, `refactor`, `test`, `docs`, `chore`

---

## 7. Prompt Catalog v2

### Catalog Index

| ID | Phase | Prompt Name | AI Role | Output Document |
|----|-------|-------------|---------|-----------------|
| P-01 | 1 | SOLID Audit | Quality Analyst | AUDITORIA.md |
| P-02 | 2 | Pattern Research | Architect Tutor | INVESTIGACION_PATRONES.md |
| P-03 | 3 | Incremental Refactor | Junior Developer | Code changes |
| P-04 | 3 | Post-Refactor Validation | Quality Analyst | VALIDACION.md |
| P-05 | 4 | Incident Anatomy (Error/Defect/Failure) | Quality Analyst | CALIDAD.md |
| P-06 | 4 | Test Pyramid Analysis | Architect Tutor | CALIDAD.md (section) |
| P-07 | 4 | Minimal Test Suite | Junior Developer | Test files |
| P-08 | 5 | Technical Debt Classification | Quality Analyst | DEUDA_TECNICA.md |
| P-09 | 5 | Pre-Commit Quality Gate | Quality Gate | Gate Report |
| P-10 | — | Error Handling Validation | Quality Analyst | Validation report |

---

### P-01: SOLID Audit

```
[ROLE]
Act as a Senior Software Auditor and Architecture Reviewer.

[CONTEXT]
Post-MVP evaluation of the codebase for architectural and maintainability assessment.
Goal: identify technical debt and structural inconsistencies before scaling.

[CONSTRAINT]
- Do NOT modify existing code.
- Do NOT perform or suggest git commits.
- Analyze the code as-is.
- Base analysis on SOLID principles and clean architecture.
- Reference specific files, layers, and components.

[OUTPUT]
Generate AUDITORIA.md including:
1. Overview of current architectural state
2. SOLID violations (especially SRP, DIP) with affected components
3. Code smells: tight coupling, duplication, missing abstractions, god objects
4. Per finding: component, violated principle, impact on scalability/maintainability
5. Risk severity per finding (Low / Medium / High)
```

---

### P-02: Pattern Research & Selection

```
[ROLE]
Act as a Senior Software Architect and Design Patterns Specialist.

[CONTEXT]
Phase 2 follows a completed audit (Phase 1) that identified:
- SRP and DIP violations
- Tight coupling to infrastructure
- Lack of abstractions
- Anemic domain model
- Primitive obsession

Goal: research and select patterns that address audit findings.

[CONSTRAINT]
- Act as Architects, not implementers. Do NOT generate code.
- Use the 25 GoF patterns as research universe.
- Analyze patterns contextually against current architecture, not generically.
- Every selection must include trade-off analysis.

[OUTPUT]
Generate INVESTIGACION_PATRONES.md with:
1. Categorized GoF analysis (Creational, Structural, Behavioral)
2. Per pattern: intent, relevance to system, mitigation of Phase 1 findings
3. Selected patterns with justification and rejected alternatives
4. Comparative table: Coupling, SRP, DIP, Scalability
5. Conclusion: architectural impact and evolution enablement
```

---

### P-03: Incremental Refactor

```
[ROLE]
Act as a Backend/Frontend Developer implementing approved refactoring.

[CONTEXT]
Phase 3 implements patterns selected in Phase 2.
Approved patterns: {Builder, Adapter, Strategy — or as applicable}.
Reference: INVESTIGACION_PATRONES.md, AUDITORIA.md.

[CONSTRAINT]
- Implement ONLY the approved pattern for the specified component.
- Preserve existing functionality (no behavioral changes).
- Follow existing project style and conventions.
- Do NOT introduce new dependencies without human approval.
- Each refactor must be atomic and independently testable.

[OUTPUT]
1. Modified files with pattern implementation
2. Brief explanation of what changed and why
3. Confirmation of which audit finding this resolves
4. Any new risks or trade-offs introduced
```

---

### P-04: Post-Refactor Validation

```
[ROLE]
Act as a Senior Software Architect performing Post-Refactor Validation.

[CONTEXT]
Refactoring changes were implemented in Phase 3 based on AUDITORIA.md findings.
Task: verify whether previously identified issues are resolved, not a new audit.

[CONSTRAINT]
- Evaluate ONLY against findings in AUDITORIA.md.
- Do NOT suggest new features.
- Do NOT modify code.
- If partially fixed, state clearly. If fixed incorrectly, explain why.

[OUTPUT]
Generate VALIDACION.md with:
1. Executive summary (Fully / Partially / Not Resolved)
2. Validation matrix: original issue → current status → residual risk
3. SOLID compliance re-evaluation
4. Pattern implementation review (correct usage? anti-patterns?)
5. Regression check: new coupling? over-engineering? unnecessary abstractions?
6. Final verdict: scalable? clean-architecture compliant? production-ready?
```

---

### P-05: Incident Anatomy (Error / Defect / Failure)

```
[ROLE]
Act as a Senior QA Analyst and Software Quality Engineer.

[CONTEXT]
Document the anatomy of a complex incident to distinguish:
- Error: human mistake that introduced the problem
- Defect: static code-level imperfection
- Failure: runtime incorrect behavior experienced by users/system

The system implements: SRP, DIP, Adapter, Strategy, Builder patterns.

[CONSTRAINT]
- Propose a realistic, technically credible bug.
- Must relate to: validation gaps, DIP misuse, strategy selection, infrastructure
  coupling, Builder without validation, or exception handling.
- Include root cause analysis, risk classification, preventive measure, and
  which test pyramid level should have caught it.

[OUTPUT]
Generate CALIDAD.md with:
1. ERROR section: human action that caused it
2. DEFECT section: specific class/method/logic mistake
3. FAILURE section: runtime impact
4. Root cause analysis
5. Risk classification and preventive measures
6. Test pyramid level mapping
```

---

### P-06: Test Pyramid Analysis

```
[ROLE]
Act as a Senior Test Architect.

[CONTEXT]
Justify the test pyramid structure for a system with Adapter, Strategy, and Builder
patterns and SOLID-focused refactoring.

[CONSTRAINT]
- Explain why unit tests should dominate over integration and E2E.
- Define one high-value scenario per test level.
- Connect each level to the incident documented in CALIDAD.md.

[OUTPUT]
Add to CALIDAD.md:
1. Technical thesis on test pyramid economics
2. High-value scenario per level (Unit, Integration, E2E)
3. Risk mitigation mapping per level
```

---

### P-07: Minimal Test Suite

```
[ROLE]
Act as a Senior QA Automation Engineer.

[CONTEXT]
Phase 4 (Quality Phase). Patterns implemented: Builder, Adapter, Strategy.
Implement ONE meaningful test per pyramid level.

[CONSTRAINT]
- Minimal but correct. Do not overengineer.
- Each test validates ONE implemented pattern.
- Backend: JUnit / Mockito / Spring Boot Test.
- Frontend: Vitest.
- E2E: Playwright.
- Include file names and full example code.

[OUTPUT]
1. Unit Test: Builder or Strategy validation logic. File, code, risk mitigated.
2. Integration Test: Adapter or Controller-Service wiring. File, code, risk mitigated.
3. E2E Test: Full flow (Frontend → Backend). File, code, risk mitigated.
4. Test pyramid alignment explanation.
```

---

### P-08: Technical Debt Classification

```
[ROLE]
Act as a Senior Software Architect and Technical Debt Strategist.

[CONTEXT]
Classify identified technical debt using Martin Fowler's Quadrant:
- Prudent / Deliberate
- Prudent / Inadvertent
- Reckless / Deliberate
- Reckless / Inadvertent

Source: AUDITORIA.md findings and architectural knowledge.

[CONSTRAINT]
- Do NOT rewrite source documents.
- Do NOT generate code.
- Base classification strictly on documented findings.
- Distinguish strategic MVP trade-offs from architectural negligence.
- State assumptions when context is insufficient.

[OUTPUT]
Generate or update DEUDA_TECNICA.md with:
1. Quadrant classification table (all items)
2. Per item: description, quadrant, justification, risk severity, interest rate
3. Remediation strategy grouped by quadrant
4. Executive summary: strategically acceptable / transitional / structurally dangerous
```

---

### P-09: Pre-Commit Quality Gate

See [Section 4.3](#43-standard-quality-gate-prompt) for the full standardized prompt.

---

### P-10: Error Handling Validation

```
[ROLE]
Act as a Software Reliability Engineer.

[CONTEXT]
Detect gaps in error handling and input validation across the codebase.

[CONSTRAINT]
- Analyze code as-is. Do NOT modify.
- Focus on: null checks, try/catch, Builder validation, infrastructure exceptions
  leaking to domain, unhandled promises, missing controller input validation.

[OUTPUT]
1. List of detected validation gaps (file, location, type)
2. Risk severity per gap
3. Test pyramid level that should detect each
4. Concrete example test per critical gap
```

---

## 8. Governance Rules

### Non-Negotiable Rules

| # | Rule | Enforcement |
|---|------|-------------|
| 1 | **No commit without Quality Gate.** | Every PR requires a Quality Gate Report (PASS or CONDITIONAL PASS). |
| 2 | **No refactor without justification.** | Refactoring must reference an audit finding (AUDITORIA.md) or debt item (DEUDA_TECNICA.md). |
| 3 | **No pattern without trade-off.** | Pattern adoption documents: problem solved, alternative considered, why rejected. |
| 4 | **No manual boilerplate.** | AI generates all scaffolding. Exceptions documented. |
| 5 | **No AI self-direction.** | AI never selects its own role. Humans assign via `[ROLE]` directive. |
| 6 | **No automated merge.** | Humans gate all PR merges. |
| 7 | **No new dependency without approval.** | Third-party additions require human security review. |

### Capacity Allocation

| Activity | Sprint Allocation |
|----------|------------------|
| Feature Development | 80% |
| Debt Reduction | 20% |

**Exception:** If refactoring pressure exceeds **70%**, declare a Debt Sprint (50% capacity for remediation).

### Governance Rituals

| Ritual | Frequency | Duration | Participants |
|--------|-----------|----------|-------------|
| Debt Triage | Sprint Planning | 30 min | Architect + PO |
| Debt Review | Quarterly | 2h | Full team |
| Debt Retrospective | After incidents | 1h | Architect + Dev |
| Quality Gate Review | Weekly standup | 5 min | Full team |
| Workflow Retrospective | Quarterly | 1h | Full team |

### Document Lineage

| Document | Phase | Purpose | Status |
|----------|-------|---------|--------|
| AUDITORIA.md | 1 | SOLID audit findings | Baseline |
| INVESTIGACION_PATRONES.md | 2 | Pattern research and selection | Complete |
| VALIDACION.md | 3 | Post-refactor validation | Complete |
| CALIDAD.md | 4 | Incident anatomy + test pyramid analysis | Complete |
| DEUDA_TECNICA.md | 5 | Technical debt registry + Fowler matrix | Living document |
| AI_WORKFLOW v2.0.md | — | This document | Active standard |

---

## Summary

| Dimension | v2.0 Standard |
|-----------|---------------|
| Methodology | AI-Augmented Architecture & Quality |
| AI Roles | Junior Developer, Architect Tutor, Quality Analyst, Quality Gate |
| Prompt Protocol | [ROLE] + [CONTEXT] + [CONSTRAINT] + [OUTPUT] — mandatory |
| Quality Gate | Pre-commit validation — 7-category checklist — PASS/CONDITIONAL/FAIL |
| Commit Flow | 5-step validation for significant changes |
| Git | `main`, `develop`, `feature/*` — humans gate merges |
| Debt Governance | Fowler quadrant classification, interest tracking, 80/20 allocation |
| Rules | 7 non-negotiable rules enforced across all phases |

---

*SofkianOS — AI Workflow v2.0*
