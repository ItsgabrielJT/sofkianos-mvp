# AI Workflow — SofkianOS

**Document:** AI Interaction Strategy  
**Team:** SofkianOS  
**Version:** 1.0

---

## 1. Methodology: AI-First

SofkianOS adopts an **AI-First** approach to development:

- **Humans = Architects.** We define vision, requirements, architecture, and quality criteria. We decide *what* to build and *why*.
- **AI = Junior Developer.** AI executes implementation under human direction: code, tests, and docs. We review and refine its output.

We do not treat AI as a replacement for judgment; we use it as a disciplined executor that we orchestrate and validate.

---

## 2. Golden Rule

**Strictly NO manual boilerplate code.**

- All scaffolding, boilerplate, and repetitive structure must be **orchestrated via AI** (prompts, code generation, templates).
- Humans **do not** write repetitive setup by hand; we **prompt and review** so that AI produces it.
- Exceptions (e.g. one-off config or security-critical snippets) must be justified and documented.

---

## 3. Roles

| Responsibility              | Owner   | Description |
|----------------------------|--------|-------------|
| Strategy                   | Humans | Product/tech direction, priorities, architecture decisions. |
| Prompt Engineering         | Humans | Designing and improving prompts; defining [ROLE], [CONTEXT], [CONSTRAINT], [OUTPUT]. |
| Security Review            | Humans | Review of dependencies, auth, data handling, and security-sensitive changes. |
| PR Merging                 | Humans | Final approval and merge to `develop` / `main`; no automated merge without human gate. |
| Coding                     | AI     | Implementation of features, refactors, and fixes from approved specs. |
| Unit Tests                 | AI     | Writing and maintaining unit tests aligned with acceptance criteria. |
| Documentation              | AI     | Inline docs, README updates, and technical documentation from human outlines. |

---

## 4. Prompt Protocol

Every request to AI must follow this structure:

```
[ROLE]      — Who the AI is acting as (e.g. "Backend developer", "QA engineer").
[CONTEXT]   — Relevant background: ticket, architecture, files, or decisions.
[CONSTRAINT] — Hard limits: stack, patterns, style, security, performance.
[OUTPUT]    — Exact deliverable: files, format, behavior, acceptance criteria.
```

**Example:**

```text
[ROLE] Act as backend developer for our REST API.
[CONTEXT] We are adding a new "projects" resource; see ADR-002 and OpenAPI spec in /docs.
[CONSTRAINT] Use our existing service/repository pattern; no new dependencies without approval; follow project ESLint config.
[OUTPUT] Implement GET /api/v1/projects and POST /api/v1/projects with validation; add unit tests and update OpenAPI.
```

Prompts that omit any of the four parts should be completed by the author before sending.

---

## 5. Git Flow

We use a simple branching model:

| Branch       | Purpose |
|-------------|---------|
| `main`      | Production-ready code. Protected; only updated via merges from `develop` (or release process). |
| `develop`   | Integration branch. All feature work is merged here after review. |
| `feature/*` | One branch per feature/task (e.g. `feature/auth-login`, `feature/api-projects`). Branched from `develop`, merged back into `develop` via PR. |

**Rules:**

- Create `feature/<short-name>` from `develop`.
- Work (human + AI) happens on the feature branch.
- Open a Pull Request into `develop`; humans perform Security Review and approval.
- Only humans merge PRs. After merge, delete the feature branch.
- Releases to `main` follow the team’s release process (e.g. from `develop` or via release branches).

---

## Summary

| Principle        | Rule |
|------------------|------|
| Methodology      | AI-First: Humans architect, AI implements. |
| Golden Rule      | No manual boilerplate; AI generates scaffolding. |
| Roles            | Humans: Strategy, Prompts, Security, Merge. AI: Code, Tests, Docs. |
| Prompts          | Always use [ROLE] + [CONTEXT] + [CONSTRAINT] + [OUTPUT]. |
| Git              | `main`, `develop`, `feature/*`; humans gate merges. |

---

*SofkianOS — AI Workflow v1.0*
