# DayTrader Modernization Migration Roadmap
Status: Draft v0.1 – subject to review.

## 1. Migration Strategy Overview
- **Approach**: Incremental, phased migration to reduce risk and deliver early value.
- **Strangler-fig pattern**:
  - Introduce Quarkus APIs + SPA alongside legacy app.
  - Route low-risk, read-only features (e.g., quote lookup) to new stack first.
  - Gradually replace legacy entry points behind a stable URL/auth boundary.
- **Parallel running**:
  - Legacy remains primary system of record until Phase 5 cutover.
  - New stack runs in non-prod, then limited beta, then broader rollout.
  - Feature flags/routing rules control which users hit which implementation.
- **Data strategy**:
  - Initially share existing schema; avoid breaking changes until parity achieved.
  - Additive DB migrations only; use views/sync jobs if new tables are required.
- **Rollback planning**:
  - Releases must allow routing traffic back to legacy without data loss.
  - Use blue/green or canary deployments for Quarkus services + SPA.
  - Maintain backward-compatible contracts for APIs and database access.

## 2. Prerequisites
- **Dev environment**: Standardized setup (JDK 17+, Quarkus CLI, Node.js, Docker), sample datasets, seed scripts.
- **CI/CD**: Separate pipelines for backend and frontend with unit tests, linters, coverage, SCA/SAST.
- **Test environments**: DEV, QA, PERF with representative data and automated reset/seed.
- **Team training**: Quarkus (Panache, RESTEasy Reactive, testing), React+TS (routing, state, testing), observability stack.
- **Tooling/infra**: Git workflow, code review rules, logging/metrics/tracing platform, artifact + container registries.

## 3. Phase 1: Foundation (Weeks 1–3)
**Objective**: Establish baseline backend/frontend projects, CI/CD and observability.
### 3.1 Backend Foundation
- Create Quarkus project(s) aligned with target service boundaries.
- Configure build (Maven/Gradle) with dev/test/prod profiles and basic CI job.
- Set up testing stack (JUnit, REST-assured; Testcontainers where feasible).
- Configure datasource to existing DayTrader schema; baseline migrations (Flyway/Liquibase).
- Implement health checks, metrics, structured logging; enable OpenAPI and initial API skeletons.
### 3.2 Frontend Foundation
- Create React + Vite project with TypeScript, Vitest/Jest, ESLint, Prettier, basic CI job.
- Implement base layout, navigation shell, and routing for main application areas.
- Establish API client abstraction (base URL config, auth token handling, error mapping).
### 3.3 Deliverables & Milestones
- Backend and frontend build successfully in CI and deploy to DEV.
- Health/metrics/OpenAPI endpoints live in DEV; SPA shell reachable via stable URL.
- Onboarding docs and dev scripts/containers available and validated by team.

## 4. Phase 2: Core Services (Weeks 4–7)
**Objective**: Deliver core auth, account, and quote capabilities end-to-end.
### 4.1 Backend Services
- Migrate key JPA entities to Panache entities/repositories.
- Implement `TradeService` equivalent (from `TradeSLSBBean`) for core operations.
- Expose REST endpoints for authentication, account management, and quote services.
- Implement centralized error handling, validation, and logging for these APIs.
### 4.2 Frontend Features
- Implement login/registration screens with validation and error feedback.
- Build dashboard layout with account summary and market overview placeholders.
- Implement quote lookup component with search, display, and simple caching.
- Wire SPA to new auth/account/quote APIs, including secure token storage/refresh.
### 4.3 Deliverables & Milestones
- End-to-end happy paths for login, registration, account overview, quote lookup.
- Automated tests (backend + frontend) cover core flows and run in CI.
- Performance baseline for core endpoints captured in PERF environment.
- Limited internal beta users exercise new core features; feedback collected.

## 5. Phase 3: Trading Features (Weeks 8–11)
**Objective**: Migrate trading workflows (portfolio, holdings, orders) with reliable processing.
### 5.1 Backend Services
- Implement portfolio/holdings services (positions, balances, valuations).
- Implement order-processing service for buy/sell with validation and persistence.
- Replace/augment MDB flows with reactive messaging (Kafka/AMQP) where applicable.
- Implement market summary service for dashboards and monitoring.
### 5.2 Frontend Features
- Portfolio view with holdings, valuations, and key performance indicators.
- Buy/sell interface with confirmations, validation, and clear error messages.
- Order history view with filtering/pagination.
- Market summary display integrated into dashboard.
### 5.3 Deliverables & Milestones
- All major trading flows available and stable in QA on new stack.
- Messaging-based operations validated through integration and load tests.
- Business stakeholders sign off on functional parity for trading.
- Decision gate: readiness to pilot full trading workloads on new platform.

## 6. Phase 4: Real-time & Advanced (Weeks 12–14)
**Objective**: Add real-time capabilities, polish UX, and harden for production.
### 6.1 Backend Services
- Implement WebSocket/SSE for live market/portfolio updates.
- Finalize messaging integration with idempotency and retry strategies.
- Perform performance profiling/tuning (caching, connection pools, queries).
- Strengthen security (rate limiting, input validation, token policies, TLS, headers).
### 6.2 Frontend Features
- Real-time market updates in dashboard and quote views.
- Account profile management (password, contact details, preferences).
- Consistent error-handling UX and global notification patterns.
- Responsive design and accessibility improvements.
### 6.3 Deliverables & Milestones
- Real-time flows validated end-to-end and under load.
- Security review completed; key findings fixed.
- UX approved by product stakeholders.
- Go/no-go decision for production cutover based on NFR benchmarks.

## 7. Phase 5: Testing & Cutover (Weeks 15–17)
- Execute comprehensive integration and regression testing across legacy and new stacks.
- Run performance tests to validate throughput, latency, and scalability targets.
- Conduct security testing (penetration tests, vulnerability scans, config reviews).
- Facilitate UAT with representative users and sign-off criteria tied to success metrics.
- Execute data alignment/migration steps if schema changes introduced in earlier phases.
- Perform staged production deployment using blue/green or canary strategy with rollback plan.
- Enable monitoring dashboards/alerts; finalize runbooks and on-call processes.
- Execute legacy decommissioning plan after stability and parity windows are met.

## 8. Risk Register
| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Hidden complexity in legacy business rules | High | Medium | Frequent demos, involve domain experts, keep scope slices small. |
| Performance regressions on new stack | High | Medium | Early baselines, perf tests each phase, tuning and caching. |
| Skill gaps in Quarkus/React | Medium | Medium | Training, pairing, coding standards, reference implementations. |
| Environment / tooling delays | Medium | Medium | Start infra early, clear ownership, escalation paths. |
| External system dependencies change | High | Low | Document contracts, use mocks, coordinate change windows, fallbacks. |
| Data inconsistencies between old/new paths | High | Low | Shared DB initially, validation, migration dry-runs, targeted monitoring. |

## 9. Team & Resource Requirements
- **Backend developers (Quarkus)**: 2–3 FTE for entities/services, messaging, performance, security.
- **Frontend developers (React)**: 2 FTE for SPA features, UX, responsive design.
- **DevOps engineer**: 1 FTE for CI/CD, environments, observability, deployments.
- **QA engineer(s)**: 1–2 FTE for test design, automation, performance + security testing.
- **Indicative effort per phase** (with above team size): P1=3 weeks, P2=4 weeks, P3=4 weeks, P4=3 weeks, P5=3 weeks.

## 10. Success Criteria
- Functional parity with legacy DayTrader for agreed feature set.
- Performance targets met or exceeded vs. baseline (throughput, latency, error rates).
- All automated tests green in CI; no critical production defects in first 4 weeks.
- Security audit passed with no critical findings; major items remediated.
- Operations team can monitor and troubleshoot using dashboards and runbooks.
- Architecture and user documentation updated and handed over.

## 11. Dependencies & Blockers
- Availability/stability of existing DayTrader DB and any external market data feeds.
- Provisioning of environments, DB instances, messaging, and observability platform.
- Completion of team training and agreement on coding standards/review process.
- Timely stakeholder decisions on scope, trade-offs, and decommissioning timelines.
- Availability of security/architecture/operations reviewers at key gates.

## 12. Quick Wins
- Implement OpenAPI documentation early to align API consumers and testers.
- Add health checks and metrics in Phase 1 to gain immediate observability.
- Containerize backend/frontend early to validate deployment model and dev parity.
- Establish automated test suites (unit, API, UI smoke) and run on every commit.
- Introduce correlation IDs in logging/tracing to simplify troubleshooting from day one.

