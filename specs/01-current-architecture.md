# DayTrader7 – Current Architecture

## 1. System Overview and Purpose
- DayTrader7 is a Java EE 7 sample application that models an online brokerage platform.
- Major capabilities: user registration and profile management, account and portfolio views, stock quote lookup, buy/sell/cancel order processing, and market summary dashboards.
- The application is implemented as a single EAR deployed to WebSphere Liberty, using in-process EJB calls, container-managed transactions, and JPA for persistence.
- It is designed for benchmark and demo usage, but the architecture and domain model resemble a real brokerage backend, making it a realistic modernization candidate.

## 2. Project Structure (Maven Modules)
- **`daytrader-ee7` (EAR module)**
  - Assembles the application into `daytrader-ee7.ear`.
  - Declares module composition and shared configuration: EJB module, web module, persistence unit, and JMS resource references.
  - Contains deployment descriptors and server-specific configuration hooks where necessary.
- **`daytrader-ee7-ejb` (EJB/business module)**
  - Hosts EJB components, JPA entities, and core service interfaces.
  - Implements trading workflows, order processing, account operations, and market summary calculations.
  - Integrates with the relational database via JPA and with JMS for asynchronous operations.
- **`daytrader-ee7-web` (Web module)**
  - Hosts JSF pages, servlets, WebSocket endpoints, and static resources.
  - Implements the user-facing UI and HTTP entry points that delegate to the EJB layer.
  - Manages navigation flows and session state within the web tier.

## 3. Technology Stack Details (Java EE 7 APIs)
- **Enterprise JavaBeans (EJB)**: Stateless session beans, singleton session beans, and message-driven beans for business logic and messaging.
- **Java Persistence API (JPA 2.x)**: ORM mapping of domain entities (`AccountDataBean`, `AccountProfileDataBean`, `HoldingDataBean`, `OrderDataBean`, `QuoteDataBean`) to relational tables.
- **Java Message Service (JMS)**: Asynchronous messaging for order processing and market data streaming, consumed by MDBs (`DTBroker3MDB`, `DTStreamer3MDB`).
- **JavaServer Faces (JSF)**: Server-side component model for HTML UI, with backing beans and navigation rules.
- **Servlets (Servlet 3.x)**: Classic HTTP entry points (`TradeAppServlet`, `TradeScenarioServlet`) for core operations and scenario/benchmark workflows.
- **WebSocket API**: Full-duplex push of streaming market data and updates to connected browsers.
- **Java Transaction API (JTA)**: Container-managed transactions across EJB, JPA, and JMS boundaries.
- **JDBC**: Direct database access for some operations via `TradeDirect` as a JDBC-based implementation of `TradeServices`.
- **Container services**: Security realms, connection pools, JMS provider, thread pools, and monitoring provided by WebSphere Liberty.

## 4. Component Inventory

### 4.1 EJB Components
- **`TradeSLSBBean` (Stateless Session Bean)**
  - Primary façade for trading operations: login/logout, account retrieval, quote lookup, and order placement/cancellation.
  - Coordinates persistence operations via JPA entities and, in some paths, delegates to `TradeDirect` for JDBC-based access.
- **`DTBroker3MDB` (Message-Driven Bean)**
  - Listens to a JMS destination carrying order-related messages.
  - Performs asynchronous order processing, status updates, and persistence, offloading work from synchronous web requests.
- **`DTStreamer3MDB` (Message-Driven Bean)**
  - Listens to a JMS destination carrying market data or quote update events.
  - Updates quote information and triggers downstream notifications (e.g., for WebSocket or UI refresh).
- **`MarketSummarySingleton` (Singleton Session Bean)**
  - Maintains an in-memory representation of market summary statistics (top gainers/losers, volume, etc.).
  - Periodically refreshes its state from the database and exposes summary data to web tier components.
- **`TradeServices` (interface) and `TradeAction` (facade)**
  - `TradeServices` abstracts the core trading operations; `TradeAction` acts as a higher-level façade used by servlets/JSF to orchestrate UI flows.
- **`TradeDirect` (JDBC implementation)**
  - Implements `TradeServices` using direct JDBC calls for scenarios favouring raw JDBC performance over JPA.

### 4.2 JPA Entities
- **`AccountDataBean`**
  - Represents a brokerage account, including balance, creation date, and relationships to profile, orders, and holdings.
- **`AccountProfileDataBean`**
  - Stores user profile data such as name, address, and contact information; typically a one-to-one relationship with `AccountDataBean`.
- **`HoldingDataBean`**
  - Represents a single holding (position) in a specific stock within an account's portfolio.
- **`OrderDataBean`**
  - Represents a trading order (buy, sell, cancel), including status, quantity, price, and timestamps.
- **`QuoteDataBean`**
  - Represents a stock quote/security, including symbol, company name, price, and volume metrics.

### 4.3 Web Components
- **Servlets**
  - `TradeAppServlet`: main controller-style servlet handling many user operations and delegating to `TradeAction` / `TradeServices`.
  - `TradeScenarioServlet`: drives scenario and benchmark workflows (e.g., load and stress patterns) for performance evaluation.
- **JSF Pages / Backing Beans**
  - `TradeAppJSF` and related pages implement JSF-based views for login, account summary, portfolio, quote lookup, and trade execution.
  - Use managed beans and expression language to bind UI components to EJB/service-layer operations.
- **WebSockets**
  - One or more WebSocket endpoints push market updates and notifications to the browser.
  - Typically fed by updates from the EJB layer (e.g., `MarketSummarySingleton`, order status changes).

### 4.4 JMS/Messaging Components
- JMS provider configured via WebSphere Liberty, exposing queues/topics for DayTrader.
- `DTBroker3MDB` and `DTStreamer3MDB` consume from these destinations using standard activation configuration.
- EJB or servlet components publish messages for asynchronous order handling and market data streaming.

## 5. Data Model Overview
- **Account and Profile**
  - `AccountDataBean` has a one-to-one association with `AccountProfileDataBean` containing user details.
- **Account and Holdings**
  - `AccountDataBean` has a one-to-many relationship with `HoldingDataBean`, representing the portfolio of positions.
- **Account and Orders**
  - `AccountDataBean` has a one-to-many relationship with `OrderDataBean`, capturing the order history and current open orders.
- **Securities and Holdings/Orders**
  - `QuoteDataBean` is referenced by both `HoldingDataBean` and `OrderDataBean`, linking positions and orders to a stock symbol.
- **Market Summary**
  - `MarketSummarySingleton` queries across `QuoteDataBean` and related tables to compute aggregates such as top movers and volume leaders.
- The schema is optimized for benchmark workloads and relies heavily on the application server for transaction and connection management.

## 6. Architecture Diagram
```mermaid
flowchart LR
  subgraph Client
    Browser[Browser (JSF/HTML + WebSocket)]
  end

  subgraph Web[daytrader-ee7-web (WAR)]
    TradeServlet[TradeAppServlet]
    ScenarioServlet[TradeScenarioServlet]
    JSF[JSF Pages / Backing Beans]
    WS[WebSocket Endpoints]
  end

  subgraph EJB[daytrader-ee7-ejb (EJB/JPA)]
    TradeSLSB[TradeSLSBBean]
    MarketSummary[MarketSummarySingleton]
    BrokerMDB[DTBroker3MDB]
    StreamerMDB[DTStreamer3MDB]
    TradeDirectComp[TradeDirect / TradeServices]
  end

  subgraph Infra[WebSphere Liberty]
    JMS[(JMS Provider)]
    DB[(Relational Database)]
  end

  Browser --> Web
  Web --> TradeSLSB
  Web --> TradeDirectComp
  TradeSLSB --> DB
  TradeDirectComp --> DB
  TradeSLSB --> JMS
  JMS --> BrokerMDB
  JMS --> StreamerMDB
  BrokerMDB --> DB
  MarketSummary --> DB
  MarketSummary --> Web
  StreamerMDB --> Web
  Web --> WS
```

## 7. External Dependencies
- Relational database (e.g., Derby/DB2 or similar) configured via container-managed data sources.
- JMS provider embedded in or configured for WebSphere Liberty.
- Application server services for security, connection pooling, and resource management.
- No significant external HTTP services are required for core trading flows; the application is largely self-contained.

## 8. Deployment Model
- The application is packaged as an EAR (`daytrader-ee7.ear`) containing the EJB module (`daytrader-ee7-ejb.jar`) and web module (`daytrader-ee7-web.war`).
- The EAR is deployed to a WebSphere Liberty server with configuration (JDBC data sources, JMS resources, security realms) defined in server configuration files.
- All modules run within a single application server instance and share the same JVM, classloader hierarchy, and transaction manager.
- Scaling is typically achieved by adding more Liberty instances running the same EAR, with the database and JMS provider shared across instances.
- Operational practices rely on application-server-centric administration rather than container-orchestration or service mesh tooling.
