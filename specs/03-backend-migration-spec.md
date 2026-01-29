# DayTrader Backend Migration Specification – Backend (03)

This document describes the backend migration of DayTrader from Java EE 7 (EJB/JMS/Servlet/JSF) to Quarkus.

## 0. Objectives, Approach, Dependencies

### 0.1 Objectives

- Migrate all backend business, persistence, and messaging components from Java EE 7 to Quarkus while:
  - Preserving current functional behavior and data semantics.
  - Enabling reactive messaging, modern security, and REST APIs as primary integration surface.
  - Supporting multiple relational databases (Derby, DB2, PostgreSQL) with minimal code branching.

### 0.2 Technical Approach

- Replace EJB components with CDI beans and JAX-RS resources.
- Replace JMS MDBs with SmallRye Reactive Messaging channels.
- Move JPA access toward Panache Repository pattern; selectively use Active Record for simple entities.
- Standardize transaction boundaries on `@Transactional` annotations.
- Replace JAAS security with Quarkus Security (OIDC/JWT + optional JPA identity store).
- Maintain backward compatibility at the HTTP and messaging contract level where feasible.

### 0.3 Dependencies

- Core Quarkus application, build, and baseline configuration from earlier infrastructure phase.
- Access to existing DayTrader schema in at least one dev/test database instance.
- Messaging broker (Kafka or AMQP) and OIDC provider (Keycloak/other) provisioned or mocked for local development.
- Legacy application available as behavioral reference (for regression comparison).

### 0.4 Acceptance Criteria

- All core flows succeed via Quarkus backend:
  - Login/logout, account creation, profile management.
  - View portfolio, view market summary, view quotes.
  - Place/complete/cancel buy and sell orders.
  - Run existing trade scenarios.
- All persisted state (accounts, orders, holdings, quotes) is compatible with existing schema and shows no regression across Derby/DB2/PostgreSQL.
- New automated tests with `@QuarkusTest` cover the above flows; no remaining dependency on EJB container APIs.

### 0.5 Risks & Mitigations

- **Transaction boundaries change** (CMT → `@Transactional`): document current behavior, add integration tests around multi-step operations (e.g., buy/sell).
- **Messaging semantics differ** (JMS vs Kafka/AMQP): explicitly define at-least-once vs at-most-once semantics per channel; implement idempotent consumers for order processing.
- **Security gaps** during cutover: use feature flags to enable Quarkus security in parallel with legacy, compare role/permission behavior before decommissioning JAAS.

### 0.6 Implementation Notes

- Implement incrementally by component (services → messaging → REST), behind stable REST/resources and message contracts.
- Keep Java packages mostly stable to simplify diffing and review, but move away from `ejb` naming in new types.

## 1. Technology Migration Matrix

| Java EE Component / Concept | Quarkus Equivalent | Notes |
|-----------------------------|--------------------|-------|
| `@Stateless` EJB | `@ApplicationScoped` CDI bean | One shared instance per app; injected with `@Inject`. |
| `@Singleton` EJB | `@ApplicationScoped` + startup event observer | Use `@Observes StartupEvent` for initialization instead of `@Startup`. |
| `@Stateful` EJB (if present) | Request/Session-scoped CDI + explicit state ID | Persist conversational state explicitly; avoid container-managed state. |
| `@MessageDriven` MDB | SmallRye Reactive Messaging `@Incoming` | Map JMS destination to channel; use Kafka/AMQP connector. |
| JPA `EntityManager` usage | Panache Repository / Active Record | Repositories per aggregate; limited direct EM usage where needed. |
| `@Resource` `DataSource` | `quarkus.datasource.*` + Agroal | Configure in `application.properties`; Panache/EM pick it up. |
| JMS `ConnectionFactory`, `Queue`, `Topic` | Reactive Messaging connector config | `mp.messaging.incoming/outgoing.*` properties define topic/queue, group, acks. |
| JAAS (`web.xml`, `login-config`) | Quarkus Security + OIDC/JWT or Security JPA | Use `@RolesAllowed`, `SecurityIdentity`, and HTTP bearer or session. |
| Servlet `HttpServlet` | JAX-RS resource (`@Path`) | RESTEasy Reactive + JSON; one resource per domain area. |
| JSF managed beans | REST resources + views/frontends | JSF actions map to REST endpoints consumed by UI. |
| WebSocket endpoints | Quarkus WebSocket/SSE | Optionally backed by Reactive Messaging `@Outgoing` channels. |
| EJB Timers/`TimerService` | Quarkus Scheduler (`@Scheduled`) | Use scheduler or messaging-based triggers. |
| Interceptors (`@AroundInvoke`) | CDI interceptors | Register via `beans.xml` or interceptor bindings. |
| JTA CMT/BMT | `@Transactional` (CDI) | Default `TxType.REQUIRED`; use other types for advanced cases. |

Implementation note: the matrix is normative for new code; exceptions (e.g., retaining bare `EntityManager`) must be justified in the spec or code comments.

## 2. Component Migration Details

This section covers the major backend components:

- `TradeSLSBBean` → `TradeService` CDI bean.
- `DTBroker3MDB` → `OrderProcessorService` using Reactive Messaging.
- `DTStreamer3MDB` → `MarketDataStreamer` pipeline.
- `MarketSummarySingleton` → `MarketSummaryService` CDI singleton.

### 2.1 `TradeSLSBBean` → `TradeService` CDI Bean

**Current implementation summary**

- `@Stateless` EJB acting as main trading facade.
- Exposes operations such as login, register, buy, sell, get holdings, get quote, get market summary.
- Orchestrates persistence via `TradeServices`/`TradeDirect` and JPA entities.
- May be invoked from servlets, JSF managed beans, and MDBs.

**Target Quarkus implementation**

- `@ApplicationScoped` CDI bean named `TradeService` in a `service` or `application` package.
- Injected into REST resources, messaging consumers, and other services via `@Inject`.
- Public methods form the primary internal API for trading operations; REST resources should be thin wrappers.

**Code transformation patterns**

- Replace EJB annotations:

  ```java
  @ApplicationScoped
  public class TradeService {

      @Inject
      OrderRepository orders;

      @Transactional
      public OrderDataBean buy(String userId, String symbol, double quantity) {
          // existing business logic migrated from TradeSLSBBean
      }
  }
  ```

- Convert EJB-local/remote interfaces into plain Java interfaces implemented by CDI beans.
- Replace `@TransactionAttribute` with `@Transactional` (default REQUIRED). Where non-standard attributes were used, map to appropriate `TxType`:
  - `REQUIRES_NEW` → `@Transactional(TxType.REQUIRES_NEW)`.
  - `MANDATORY`, `NEVER`, `NOT_SUPPORTED` → evaluate and refactor; avoid where possible.
- Remove EJB-specific injection (`@EJB`); use `@Inject` for collaborators.

**Dependencies needed**

- `quarkus-arc` (CDI), `quarkus-hibernate-orm-panache`, `quarkus-resteasy-reactive-jackson`.

### 2.2 `DTBroker3MDB` → `OrderProcessorService` with `@Incoming`

**Current implementation summary**

- `@MessageDriven` EJB listening on an order queue.
- Implements `MessageListener.onMessage(Message msg)`; extracts payload, calls trading services, persists `OrderDataBean`.
- Relies on container-managed transactions (CMT) and JMS redelivery semantics.

**Target Quarkus implementation**

- `@ApplicationScoped` CDI bean `OrderProcessorService`.
- Method annotated with `@Incoming("orders")` to consume order messages from Kafka/AMQP.
- Uses `@Transactional` to wrap order processing and persistence in a JTA transaction.

**Code transformation patterns**

- Define a typed `OrderEvent` DTO representing the serialized message.
- Map from `OrderEvent` to domain entities (`OrderDataBean`, `HoldingDataBean`, etc.).
- Use Panache repositories or `EntityManager` to persist within the transactional method.

  ```java
  @ApplicationScoped
  public class OrderProcessorService {

      @Inject OrderRepository orders;

      @Incoming("orders")
      @Transactional
      public void onOrder(OrderEvent event) {
          // validate, map to OrderDataBean, persist
      }
  }
  ```

- Configure channel `orders` via `mp.messaging.incoming.orders.*` (see §7).
- For error handling and retries, prefer connector-level configs and idempotent business logic instead of container-managed redelivery.

**Dependencies needed**

- `quarkus-smallrye-reactive-messaging-kafka` or `quarkus-smallrye-reactive-messaging-amqp`.
- Existing ORM/Panache stack for persistence.

### 2.3 `DTStreamer3MDB` → `MarketDataStreamer` with `@Incoming`/`@Outgoing`

**Current implementation summary**

- `@MessageDriven` EJB consuming raw market events.
- Updates market summary and pushes changes toward JSF/WebSocket clients.

**Target Quarkus implementation**

- `@ApplicationScoped` CDI bean `MarketDataStreamer` implementing a Reactive Messaging pipeline:
  - `@Incoming("market-events")` from broker.
  - `@Outgoing("market-summary-stream")` to internal channel feeding WebSocket/SSE.

**Code transformation patterns**

- Introduce `MarketEvent` DTO and `MarketSummary` DTO.
- Implement stateless transformation logic (no mutable shared state; delegate caching to `MarketSummaryService`).

  ```java
  @ApplicationScoped
  public class MarketDataStreamer {

      @Incoming("market-events")
      @Outgoing("market-summary-stream")
      public MarketSummary toSummary(MarketEvent event) {
          // compute partial or full market summary
      }
  }
  ```

- WebSocket/SSE layer subscribes to `market-summary-stream` using Quarkus WebSockets or server-sent events.

**Dependencies needed**

- Same Reactive Messaging connector as orders.
- Quarkus WebSockets or RESTEasy Reactive with SSE.

### 2.4 `MarketSummarySingleton` → `MarketSummaryService`

**Current implementation summary**

- `@Singleton` EJB maintaining a cached market summary.
- May use `@Schedule` or `TimerService` for periodic refresh; `@Lock` annotations for concurrency.

**Target Quarkus implementation**

- `@ApplicationScoped` CDI bean `MarketSummaryService`.
- Provides cached `MarketSummary` to REST/WebSocket resources.
- Can be updated by scheduler, by `MarketDataStreamer`, or on-demand.

**Code transformation patterns**

- Replace `@Singleton` with `@ApplicationScoped`.
- Replace EJB timers with Quarkus Scheduler (`@Scheduled`) or reactive updates.
- Replace EJB locks with Java concurrency primitives if needed (e.g., `AtomicReference`, `synchronized`, or `StampedLock`).

  ```java
  @ApplicationScoped
  public class MarketSummaryService {

      private final AtomicReference<MarketSummary> current = new AtomicReference<>();

      @Scheduled(every = "10s")
      @Transactional
      void refresh() {
          // recompute summary from Quote/Holding data
      }

      public MarketSummary getCurrent() {
          return current.get();
      }
  }
  ```

**Dependencies needed**

- `quarkus-scheduler`, ORM/Panache for queries.

## 3. Entity Migration

### 3.1 Scope & Goals

- Entities: `AccountDataBean`, `AccountProfileDataBean`, `HoldingDataBean`, `OrderDataBean`, `QuoteDataBean`.
- Goals:
  - Preserve existing table/column mappings and constraints.
  - Reduce boilerplate in DAOs using Panache Repositories.
  - Keep business logic out of entities where possible.

### 3.2 Panache Pattern Selection

- **Repository-first approach (recommended):**
  - Keep entities as standard JPA entities (no Panache base class).
  - Introduce per-entity repositories implementing `PanacheRepository<…>`.
  - Suitable for complex queries and unit testing.
- **Active Record (optional):**
  - For simple, frequently-used entities (e.g., `QuoteDataBean`), consider extending `PanacheEntity`/`PanacheEntityBase` to inline simple finders.
  - Use sparingly to avoid spreading persistence logic across many types.

### 3.3 Transformation Steps

1. Move entity classes to a package recognized by Quarkus JPA (`@Entity` unchanged).
2. Verify `@Table`, `@Column`, `@NamedQuery`, `@NamedNativeQuery` annotations remain intact; adjust only if Quarkus/Hibernate naming strategies differ.
3. For each entity, create a repository, e.g. `OrderRepository implements PanacheRepository<OrderDataBean>`.
4. Replace direct `EntityManager` usage in EJBs with repository calls (`persist`, `find`, `list`, `update`).
5. Where named queries are used, call them via Panache (`find("NamedQuery", params...)`) or refactor to Panache queries.

### 3.4 Example Repository

```java
@ApplicationScoped
public class OrderRepository implements PanacheRepository<OrderDataBean> {

    public List<OrderDataBean> findCompletedByAccount(String accountId) {
        return find("account.accountId = ?1 and status = ?2", accountId, "closed").list();
    }
}
```

### 3.5 Named Queries Migration

- Retain named queries defined on entities; verify they are compatible with Hibernate 5/6 dialects used by Quarkus.
- Prefer Panache query methods for new queries; migrate legacy named queries gradually when modifying related code.
- Document any dialect-specific concerns (e.g., DB2 vs PostgreSQL syntax) in repository comments.

## 4. REST API Implementation

### 4.1 Mapping Existing Components to REST

- **TradeAppServlet / TradeAppJSF** → `TradeResource` (`/trade`): login, logout, register, home.
- **PortfolioJSF** → `PortfolioResource` (`/portfolio`): list holdings, portfolio value, gains/losses.
- **QuoteJSF** → `QuoteResource` (`/quotes`): get single/multi-symbol quotes, market summary.
- **TradeScenarioServlet** → `ScenarioResource` (`/scenarios`): start/stop/load scenarios.
- **MarketSummaryWebSocket** → REST + WebSocket/SSE endpoints under `/market-summary`.

### 4.2 Design Guidelines

- Use RESTEasy Reactive with Jackson (`quarkus-resteasy-reactive-jackson`).
- Keep resources thin: validate input, map to DTOs, delegate to `TradeService`/other services.
- Use DTOs for request/response payloads; avoid exposing JPA entities directly.
- Use standard HTTP status codes and error payloads; centralize handling in `ExceptionMapper` implementations.

### 4.3 Example Endpoint Shape

```java
@Path("/portfolio")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PortfolioResource {

    @Inject TradeService tradeService;

    @GET
    public PortfolioView getPortfolio() {
        return tradeService.getPortfolioForCurrentUser();
    }
}
```

### 4.4 Error Handling Patterns

- Define custom exceptions for business errors (e.g., `InsufficientFundsException`).
- Implement `ExceptionMapper<BusinessException>` and `ExceptionMapper<Throwable>` to map exceptions to structured JSON (error code, message, correlation id).
- Log server-side details; avoid leaking stack traces to clients.

## 5. Transaction Management

### 5.1 CMT (Container-Managed Transactions) → `@Transactional`

- Replace EJB CMT with CDI-based `@Transactional` on service methods.
- Default mapping:
  - EJB `REQUIRED` → `@Transactional` (default).
  - EJB `REQUIRES_NEW` → `@Transactional(TxType.REQUIRES_NEW)` on the called method.
- Avoid complex combinations like `SUPPORTS`, `NOT_SUPPORTED`, unless justified; prefer explicit service boundaries.

### 5.2 BMT / `UserTransaction` Patterns

- Where EJB code manually begins/commits transactions, refactor to service methods annotated with `@Transactional` and restructured logic.
- If a true nested or independent transaction is required, split into two methods and use `REQUIRES_NEW` on the inner one.
- Avoid injecting or looking up `UserTransaction` directly; Quarkus focuses on declarative transaction management.

### 5.3 Messaging + Transactions

- For `OrderProcessorService`, use `@Transactional` on the `@Incoming` method to ensure database updates are atomic.
- Accept that broker transactions may not be perfectly coupled with DB transactions; use idempotent processing and retry policies.
- For outbox-like patterns (DB changes → message), consider future adoption of Debezium/outbox; outside current scope but keep design compatible.

## 6. Security Migration

### 6.1 JAAS → Quarkus Security

- Replace JAAS realm configuration and `web.xml` security constraints with:
  - OIDC/JWT-based authentication using `quarkus-oidc` or `quarkus-smallrye-jwt`.
  - Optionally, `quarkus-security-jpa` for username/password stored in DB.
- Map existing roles (e.g., `Trader`, `Admin`) to Quarkus roles surfaced in `SecurityIdentity`.

### 6.2 Authorization in Code

- Annotate REST resources and service methods with `@RolesAllowed`, `@PermitAll`, and `@DenyAll` as appropriate.
- Example:

  ```java
  @Path("/trade")
  public class TradeResource {

      @GET
      @Path("/summary")
      @RolesAllowed("Trader")
      public TradeSummary getSummary() { /* ... */ }
  }
  ```

### 6.3 Authentication Endpoints / Flows

- For OIDC:
  - Prefer redirect-based login handled by the identity provider for browser clients.
  - For API clients, accept bearer tokens and validate them via `quarkus-oidc`.
- If username/password login is retained for compatibility:
  - Expose `/auth/login` endpoint accepting credentials over HTTPS.
  - Validate credentials using `quarkus-security-jpa` identity store or custom `IdentityProvider`.
  - Issue JWT or session token for subsequent requests.

### 6.4 Migration Notes

- Keep JAAS configuration in place until Quarkus auth behavior is verified in QA.
- Create mapping documentation from old roles to new roles/claims.

## 7. Messaging Migration

### 7.1 JMS Destinations → Reactive Messaging Channels

- Identify all JMS queues/topics used by MDBs (`DTBroker3MDB`, `DTStreamer3MDB`, others if present).
- Define corresponding channels in Quarkus:
  - Example: `jms/Queue/Order` → `orders` channel.
  - Example: `jms/Topic/MarketData` → `market-events` channel.

### 7.2 Configuration Templates (Kafka Example)

```properties
mp.messaging.incoming.orders.connector=smallrye-kafka
mp.messaging.incoming.orders.topic=orders
mp.messaging.incoming.orders.value.deserializer=...
mp.messaging.incoming.orders.group.id=daytrader-orders

mp.messaging.incoming.market-events.connector=smallrye-kafka
mp.messaging.incoming.market-events.topic=market-events

mp.messaging.outgoing.market-summary-stream.connector=smallrye-kafka
mp.messaging.outgoing.market-summary-stream.topic=market-summary
```

- Use equivalent AMQP properties if AMQP is selected.

### 7.3 Delivery Semantics & Error Handling

- Document required semantics per channel (e.g., orders: at-least-once with idempotent processing).
- Configure retry policies and dead-letter topics at connector level when available.
- For unrecoverable business errors, log and route to DLQ rather than causing infinite redelivery.

## 8. Database Configuration

### 8.1 DataSource & Agroal

- Configure default datasource via `application.properties`:

  ```properties
  quarkus.datasource.db-kind=postgresql  # or db2, derby
  quarkus.datasource.username=daytrader
  quarkus.datasource.password=...
  quarkus.datasource.jdbc.url=jdbc:postgresql://.../daytrader
  quarkus.hibernate-orm.database.generation=none
  ```

- Use Agroal defaults; tune pool size/timeouts for production based on load testing.

### 8.2 Multi-Database Support Strategy

- Use Quarkus configuration profiles to support multiple DBs with the same codebase:

  ```properties
  %dev.quarkus.datasource.db-kind=derby
  %dev.quarkus.datasource.jdbc.url=jdbc:derby:...;

  %db2.quarkus.datasource.db-kind=db2
  %db2.quarkus.datasource.jdbc.url=jdbc:db2://.../daytrader
  ```

- Validate schema compatibility across Derby/DB2/PostgreSQL; address dialect-specific issues via Hibernate dialect and SQL tuning.

### 8.3 Implementation Notes

- Keep DDL outside the application (Flyway/Liquibase or manual scripts) to avoid accidental schema changes.
- Ensure all Panache queries are portable across target databases; avoid vendor-specific SQL unless absolutely required.

## 9. Testing Strategy

### 9.1 Unit and Integration Testing with `@QuarkusTest`

- Use `@QuarkusTest` for integration-style tests of REST resources and services.
- Use pure JUnit 5 + Mockito for isolated unit tests of business logic.

### 9.2 Database Testing with Testcontainers

- Use Testcontainers to spin up PostgreSQL and DB2 containers in CI and local dev.
- Configure tests to use the same JPA mappings and Panache repositories as production.
- Seed minimal reference data for accounts, quotes, and scenarios.

### 9.3 Messaging Testing

- Use in-memory connectors (`smallrye-messaging-in-memory`) for fast tests of `OrderProcessorService` and `MarketDataStreamer`.
- For higher-fidelity tests, spin up Kafka/AMQP containers via Testcontainers and exercise full pipelines.

### 9.4 Mocking Patterns

- Use CDI alternatives or Mockito-based mocks for external dependencies (e.g., external market feeds, auth providers).
- Avoid mocking the ORM layer; prefer real DB via Testcontainers when testing repositories.

## 10. Dependencies (Maven/Quarkus Extensions)

### 10.1 Core Backend Extensions

- `quarkus-arc` (CDI)
- `quarkus-resteasy-reactive` and `quarkus-resteasy-reactive-jackson`
- `quarkus-hibernate-orm-panache`
- JDBC drivers: `quarkus-jdbc-postgresql`, `quarkus-jdbc-db2`, `quarkus-jdbc-derby` (as needed)
- `quarkus-narayana-jta`

### 10.2 Messaging & Streaming

- `quarkus-smallrye-reactive-messaging`
- `quarkus-smallrye-reactive-messaging-kafka` and/or `quarkus-smallrye-reactive-messaging-amqp`
- Optional: `quarkus-vertx` or `quarkus-websockets` for market summary streaming

### 10.3 Security

- `quarkus-oidc` or `quarkus-smallrye-jwt`
- `quarkus-security-jpa` if using DB-backed credentials

### 10.4 Testing

- `quarkus-junit5`
- `quarkus-test-h2` or dedicated Testcontainers dependencies
- Testcontainers modules for PostgreSQL/DB2/Kafka/AMQP as required

Implementation note: actual `pom.xml` changes should be applied via Maven/Quarkus CLI (e.g., `mvn quarkus:add-extension`) rather than manual dependency edits, to keep BOM alignment and version management consistent.

