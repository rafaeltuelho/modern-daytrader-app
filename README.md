# DayTrader - Stock Trading Application

A comprehensive stock trading simulation application demonstrating modern cloud-native architecture patterns.

## 🚀 Modern Architecture (Quarkus Microservices)

This repository contains a **modernized version** of the DayTrader application, migrated from Java EE7/WebSphere Liberty to a cloud-native microservices architecture.

### Architecture Overview

| Component | Technology | Port |
|-----------|------------|------|
| **Account Service** | Quarkus 3.x, PostgreSQL, SmallRye JWT | 8080 |
| **Trading Service** | Quarkus 3.x, PostgreSQL, REST Client | 8081 |
| **Market Service** | Quarkus 3.x, PostgreSQL | 8082 |
| **Frontend** | React 18, TypeScript, Vite, Tailwind CSS | 3001 |

### Features

- 🔐 **JWT Authentication** - Secure login and registration with SmallRye JWT
- 📊 **Portfolio Management** - View holdings, buy/sell stocks
- 📈 **Market Data** - Real-time quotes with 50 seeded stocks
- 💹 **Trading** - Place buy/sell orders with automatic processing
- 🎨 **Modern UI** - React SPA with responsive Tailwind CSS design

### Quick Start (Modern Version)

```bash
# Clone the repository
git clone git@github.com:augment-solutions/daytrader.git
cd daytrader

# Start backend services (requires Docker for PostgreSQL)
cd daytrader-quarkus

# Terminal 1: Account Service
cd daytrader-account-service && mvn quarkus:dev -Dquarkus.http.port=8080

# Terminal 2: Trading Service
cd daytrader-trading-service && mvn quarkus:dev -Dquarkus.http.port=8081

# Terminal 3: Market Service
cd daytrader-market-service && mvn quarkus:dev -Dquarkus.http.port=8082

# Terminal 4: Frontend
cd daytrader-frontend && npm install && npm run dev
```

Once all services are running, open [http://localhost:3001](http://localhost:3001) in your browser.

### 📚 Documentation

For comprehensive documentation on the modernization effort, see:

**[📖 Specification Index](specs/spec-index.md)** - Complete documentation including:
- [Architecture Assessment](specs/phase-00-architecture-assessment.md) - Legacy analysis
- [Phase Specifications](specs/spec-index.md#document-status) - Implementation phases 1-6
- [API Specifications](specs/spec-index.md#api-specifications) - OpenAPI docs for all services
- [Architecture Decision Records](specs/spec-index.md#architecture-decision-records-adrs) - Key technical decisions
- [Verification Reports](specs/spec-index.md#verification-reports) - Testing and validation

### Technology Stack

**Backend:**
- Quarkus 3.x (Supersonic Subatomic Java)
- PostgreSQL with Flyway migrations
- SmallRye JWT for authentication
- CDI Events for in-memory messaging
- Panache ORM for data access
- REST Assured for integration testing

**Frontend:**
- React 18 with TypeScript
- Vite for fast builds and HMR
- TanStack Query for server state
- Zustand for client state
- Tailwind CSS for styling
- Vitest for unit testing

---

## 📦 Legacy Application (Java EE7)

The original DayTrader 7 benchmark application is preserved in the `daytrader-ee7` directory.

### About the Legacy Version

This sample contains the DayTrader 7 benchmark, which is an application built around the paradigm of an online stock trading system. The application allows users to login, view their portfolio, lookup stock quotes, and buy or sell stock shares. With the aid of a Web-based load driver such as Apache JMeter, the real-world workload provided by DayTrader can be used to measure and compare the performance of Java Platform, Enterprise Edition (Java EE) application servers.

DayTrader's design spans Java EE 7, including WebSockets, JSPs, Servlets, EJBs, JPA, JDBC, JSF, CDI, Bean Validation, JSON, JMS, MDBs, and transactions.

### Running the Legacy Version

This sample can be installed onto WAS Liberty runtime versions 8.5.5.6 and later. A prebuilt Derby database is provided in `resources/data`.

```bash
# Build and run on Open Liberty
mvn install
cd daytrader-ee7
mvn liberty:run
```

Once started, go to [http://localhost:9082/daytrader](http://localhost:9082/daytrader).

### Containerizing with DB2

```bash
docker build -t sample-daytrader7 -f Containerfile_db2 .
```

---

## Notice

© Copyright IBM Corporation 2015, 2026.

## License

```text
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
