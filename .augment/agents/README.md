# DayTrader Augment Agents

This directory defines the agents used in the DayTrader modernization project.
All agents follow a **specification-driven, multi-agent pipeline** and communicate
primarily through markdown files in the `/specs` folder.

## Agentic Pipeline (High Level)

```
software-architect
        |
        v
software-engineer / quarkus-engineer
        |
        v
verifier (backend)
        |
        v
qa-engineer (backend tests)
        |
        v
frontend-engineer
        |
        v
verifier (frontend)
        |
        v
qa-engineer (frontend & E2E tests)
```

## Agents

- **software-architect** ([`software-architect.md`](./software-architect.md))
  - Focuses on application modernization context, API design, and migration strategy.
  - Produces `/specs/phase-XX-*.md`, API specs, migration plans, and legacy→modern mappings.

- **software-engineer / quarkus-engineer** ([`software-engineer.md`](./software-engineer.md))
  - Focuses on Quarkus / Jakarta EE backend implementation and modernization.
  - Reads `/specs` and writes `/specs/implementation-notes/<feature>-<timestamp>.md`.

- **frontend-engineer** ([`frontend-engineer.md`](./frontend-engineer.md))
  - Focuses on modern React/TypeScript UI implementation, preserving legacy behavior.
  - Reads backend API specs and writes `/specs/implementation-notes/<feature>-<timestamp>.md`.

- **verifier** ([`verifier.md`](./verifier.md))
  - Validates backend and frontend implementations against `/specs`.
  - Compares behavior, API contracts, and architecture; classifies findings (Critical/Major/Minor).
  - Writes `/specs/verification-reports/<agent>-<timestamp>.md`.

- **qa-engineer** ([`qa-engineer.md`](./qa-engineer.md))
  - Focuses on test strategy, automation, and quality for backend and frontend.
  - Runs unit, integration, E2E, and browser-based tests (Chrome DevTools MCP).
  - Writes `/specs/test-reports/<test-type>-<timestamp>.md`.

## `/specs` Folder Overview

Typical structure:

```
/specs/
  spec-index.md
  phase-XX-*.md
  phase-XX-api-spec-*.md
  phase-XX-migration-plan.md
  phase-XX-legacy-to-modern-mapping.md
  implementation-notes/
  verification-reports/
  test-reports/
```

- The **software-architect** writes and maintains the core specs.
- The **implementation agents** (software-engineer / quarkus-engineer, frontend-engineer) read specs and write implementation notes.
- The **verifier** reads specs + implementation and writes verification reports.
- The **qa-engineer** reads specs + verification reports and writes test reports.

## Context 7 MCP

Backend and frontend agents use **Context 7 MCP** as their primary source for framework/library documentation, aligned with the versions declared in `pom.xml` / `package.json`. See each agent's definition for detailed behavior.