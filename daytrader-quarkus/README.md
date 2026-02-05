# DayTrader Quarkus - Cloud-Native Trading Platform

This is the modernized version of DayTrader, rebuilt using Quarkus 3.17.x and cloud-native patterns.

## Project Structure

```
daytrader-quarkus/
├── pom.xml                              # Parent POM
├── daytrader-common/                    # Shared library (DTOs, events, exceptions)
├── daytrader-account-service/           # Account management service
├── daytrader-trading-service/           # Trading operations service (Phase 2)
├── daytrader-market-service/            # Market data service (Phase 2)
└── docker/                              # Docker Compose for local development
```

## Technology Stack

- **Runtime**: Quarkus 3.17.4
- **Java**: Java 21 LTS
- **Database**: PostgreSQL 16
- **Messaging**: Redpanda (Kafka-compatible)
- **Security**: Keycloak (OIDC/JWT)
- **Observability**: Prometheus, Jaeger, OpenTelemetry
- **Build**: Maven 3.9.x

## Prerequisites

- Java 21 or later
- Maven 3.9.x or later
- Docker and Docker Compose (for local development)

## Quick Start

### 1. Build the Project

```bash
cd daytrader-quarkus
mvn clean install
```

### 2. Start Infrastructure Services

```bash
cd docker
docker-compose up -d
```

This starts:
- PostgreSQL (port 5432)
- Keycloak (port 8180)
- Redpanda/Kafka (port 19092)
- Redpanda Console (port 8090)
- Jaeger UI (port 16686)
- Prometheus (port 9090)

### 3. Run the Account Service

```bash
cd daytrader-account-service
mvn quarkus:dev
```

The service will be available at http://localhost:8081

## Development

### Dev Mode

Quarkus dev mode provides hot reload and dev services:

```bash
mvn quarkus:dev
```

### Running Tests

```bash
mvn test                    # Unit tests
mvn verify                  # Integration tests
```

### Building Native Image

```bash
mvn package -Pnative
```

## API Documentation

Once the service is running, access the OpenAPI documentation at:
- Swagger UI: http://localhost:8081/q/swagger-ui
- OpenAPI Spec: http://localhost:8081/q/openapi

## Observability

- **Metrics**: http://localhost:8081/q/metrics (Prometheus format)
- **Health**: http://localhost:8081/q/health
- **Jaeger UI**: http://localhost:16686
- **Prometheus**: http://localhost:9090

## Security

Default Keycloak credentials:
- Admin Console: http://localhost:8180
- Username: `admin`
- Password: `admin`

Test users:
- Trader: `trader1` / `password`
- Admin: `admin` / `admin`

## Phase Implementation Status

- ✅ **Phase 1**: Core Infrastructure (Current)
  - Multi-module project structure
  - Database schema and migrations
  - Security framework (OIDC/JWT)
  - Observability stack
  - Docker Compose development environment

- 🔄 **Phase 2**: Feature Implementation (Planned)
  - Account service implementation
  - Trading service implementation
  - Market service implementation

- 📋 **Phase 3**: Advanced Features (Planned)
  - Event-driven architecture
  - WebSocket support
  - Performance optimization

## License

See LICENSE file in the root directory.

