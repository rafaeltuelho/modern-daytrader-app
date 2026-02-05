# Phase 1: Core Infrastructure - Modernization Specification

## Executive Summary

This document provides the complete infrastructure specification for the modernized DayTrader application. Phase 1 establishes the foundational components: project structure, database schema, security framework, observability stack, CI/CD pipeline, and development environment.

---

## Phase Objectives

1. **Project Foundation**: Create Quarkus multi-module project with parent POM and dependency management
2. **Database Setup**: Design PostgreSQL schema with Flyway migrations and connection pooling
3. **Security Framework**: Implement OIDC/JWT authentication with Keycloak integration
4. **Observability**: Configure Prometheus metrics, Jaeger tracing, and structured logging
5. **CI/CD Pipeline**: Create GitHub Actions workflow for automated build, test, and deployment
6. **Development Environment**: Docker Compose setup for local development with all dependencies

---

## 1. Migration Approach: Strangler Fig Pattern

We recommend an **incremental migration** using the Strangler Fig pattern rather than a big-bang rewrite:

```
┌─────────────────────────────────────────────────────────────────────┐
│                        API Gateway / Router                         │
│                    (Routes traffic to old or new)                   │
└─────────────────────────────────────────────────────────────────────┘
                                │
            ┌───────────────────┴───────────────────┐
            ▼                                       ▼
    ┌───────────────┐                       ┌───────────────┐
    │   Legacy      │                       │   New         │
    │   DayTrader   │ ◄─────────────────►   │   Quarkus     │
    │   (Liberty)   │   Parallel Run        │   Services    │
    └───────────────┘                       └───────────────┘
            │                                       │
            └───────────────────┬───────────────────┘
                                ▼
                        ┌───────────────┐
                        │   Shared      │
                        │   Database    │
                        └───────────────┘
```

### Benefits of Incremental Approach
- ✅ Lower risk - can rollback individual components
- ✅ Continuous delivery - value delivered incrementally
- ✅ Team learning - gradual skill building
- ✅ Business continuity - legacy system stays operational

---

## 2. Project Structure

### Multi-Module Quarkus Project Layout

```
daytrader-quarkus/
├── pom.xml                              # Parent POM (BOM, dependency management)
├── .mvn/
│   └── wrapper/                         # Maven wrapper for consistent builds
├── daytrader-common/                    # Shared library module
│   ├── src/main/java/
│   │   └── com/daytrader/common/
│   │       ├── dto/                     # Data Transfer Objects (records)
│   │       │   ├── AccountDTO.java
│   │       │   ├── QuoteDTO.java
│   │       │   ├── OrderDTO.java
│   │       │   ├── HoldingDTO.java
│   │       │   └── MarketSummaryDTO.java
│   │       ├── event/                   # Domain events
│   │       │   ├── OrderCreatedEvent.java
│   │       │   ├── OrderCompletedEvent.java
│   │       │   └── QuoteUpdatedEvent.java
│   │       ├── exception/               # Custom exceptions
│   │       │   ├── BusinessException.java
│   │       │   ├── ResourceNotFoundException.java
│   │       │   └── InsufficientFundsException.java
│   │       └── util/                    # Shared utilities
│   │           └── TradeConfig.java
│   └── pom.xml
├── daytrader-account-service/           # Account bounded context
│   ├── src/main/java/
│   │   └── com/daytrader/account/
│   │       ├── entity/                  # JPA entities
│   │       ├── repository/              # Panache repositories
│   │       ├── service/                 # Business logic
│   │       ├── resource/                # REST endpoints
│   │       └── mapper/                  # Entity-DTO mappers
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── db/migration/                # Flyway migrations
│   ├── src/test/java/                   # Unit and integration tests
│   └── pom.xml
├── daytrader-trading-service/           # Trading bounded context
│   ├── src/main/java/
│   │   └── com/daytrader/trading/
│   │       ├── entity/
│   │       ├── repository/
│   │       ├── service/
│   │       ├── resource/
│   │       ├── mapper/
│   │       └── messaging/               # Order processing consumers
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── db/migration/
│   ├── src/test/java/
│   └── pom.xml
├── daytrader-market-service/            # Market bounded context
│   ├── src/main/java/
│   │   └── com/daytrader/market/
│   │       ├── entity/
│   │       ├── repository/
│   │       ├── service/
│   │       ├── resource/
│   │       ├── mapper/
│   │       └── websocket/               # Real-time quote streaming
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── db/migration/
│   ├── src/test/java/
│   └── pom.xml
├── daytrader-gateway/                   # API Gateway (optional - for routing)
│   ├── src/main/java/
│   │   └── com/daytrader/gateway/
│   ├── src/main/resources/
│   └── pom.xml
├── docker/                              # Docker and compose files
│   ├── docker-compose.yml
│   ├── docker-compose.dev.yml
│   ├── keycloak/
│   │   └── realm-export.json
│   └── postgres/
│       └── init.sql
├── .github/
│   └── workflows/
│       ├── ci.yml                       # CI pipeline
│       └── release.yml                  # Release pipeline
└── docs/
    └── architecture/
        └── c4-diagrams.md
```

---

## 3. Technology Stack

### Core Dependencies

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| **Runtime** | Quarkus | 3.17.x | Cloud-native framework |
| **Java** | Java 21 LTS | 21 | Language runtime |
| **Build** | Maven | 3.9.x | Build automation |
| **REST** | RESTEasy Reactive | - | JAX-RS implementation |
| **Persistence** | Hibernate ORM with Panache | - | Data access |
| **Database** | PostgreSQL | 16.x | Primary database |
| **Messaging** | Smallrye Reactive Messaging + Kafka | - | Event streaming |
| **Security** | Quarkus OIDC / JWT | - | Authentication |
| **Observability** | Micrometer + OpenTelemetry | - | Metrics/Tracing |

### Development Dependencies

| Tool | Purpose |
|------|---------|
| Quarkus Dev Services | Auto-provision dev containers |
| Testcontainers | Integration testing |
| REST Assured | API testing |
| JUnit 5 + Mockito | Unit testing |

### Parent POM Configuration

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.daytrader</groupId>
    <artifactId>daytrader-quarkus</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>DayTrader Quarkus - Parent</name>
    <description>Cloud-native DayTrader trading platform</description>

    <modules>
        <module>daytrader-common</module>
        <module>daytrader-account-service</module>
        <module>daytrader-trading-service</module>
        <module>daytrader-market-service</module>
        <module>daytrader-gateway</module>
    </modules>

    <properties>
        <!-- Java Version -->
        <java.version>21</java.version>
        <maven.compiler.source>${java.version}</maven.compiler.source>
        <maven.compiler.target>${java.version}</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>

        <!-- Quarkus Platform -->
        <quarkus.platform.artifact-id>quarkus-bom</quarkus.platform.artifact-id>
        <quarkus.platform.group-id>io.quarkus.platform</quarkus.platform.group-id>
        <quarkus.platform.version>3.17.4</quarkus.platform.version>

        <!-- Plugin Versions -->
        <compiler-plugin.version>3.13.0</compiler-plugin.version>
        <surefire-plugin.version>3.5.2</surefire-plugin.version>
        <failsafe-plugin.version>3.5.2</failsafe-plugin.version>
        <jib-maven-plugin.version>3.4.4</jib-maven-plugin.version>

        <!-- Library Versions -->
        <mapstruct.version>1.6.3</mapstruct.version>
        <lombok.version>1.18.36</lombok.version>
        <testcontainers.version>1.20.4</testcontainers.version>
        <rest-assured.version>5.5.0</rest-assured.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- Quarkus BOM -->
            <dependency>
                <groupId>${quarkus.platform.group-id}</groupId>
                <artifactId>${quarkus.platform.artifact-id}</artifactId>
                <version>${quarkus.platform.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- Internal Modules -->
            <dependency>
                <groupId>com.daytrader</groupId>
                <artifactId>daytrader-common</artifactId>
                <version>${project.version}</version>
            </dependency>

            <!-- MapStruct for DTO mapping -->
            <dependency>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct</artifactId>
                <version>${mapstruct.version}</version>
            </dependency>

            <!-- Testcontainers -->
            <dependency>
                <groupId>org.testcontainers</groupId>
                <artifactId>testcontainers-bom</artifactId>
                <version>${testcontainers.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>${quarkus.platform.group-id}</groupId>
                    <artifactId>quarkus-maven-plugin</artifactId>
                    <version>${quarkus.platform.version}</version>
                    <extensions>true</extensions>
                    <executions>
                        <execution>
                            <goals>
                                <goal>build</goal>
                                <goal>generate-code</goal>
                                <goal>generate-code-tests</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>${compiler-plugin.version}</version>
                    <configuration>
                        <source>${java.version}</source>
                        <target>${java.version}</target>
                        <parameters>true</parameters>
                        <annotationProcessorPaths>
                            <path>
                                <groupId>org.mapstruct</groupId>
                                <artifactId>mapstruct-processor</artifactId>
                                <version>${mapstruct.version}</version>
                            </path>
                        </annotationProcessorPaths>
                    </configuration>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <version>${surefire-plugin.version}</version>
                    <configuration>
                        <systemPropertyVariables>
                            <java.util.logging.manager>
                                org.jboss.logmanager.LogManager
                            </java.util.logging.manager>
                        </systemPropertyVariables>
                    </configuration>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-failsafe-plugin</artifactId>
                    <version>${failsafe-plugin.version}</version>
                    <executions>
                        <execution>
                            <goals>
                                <goal>integration-test</goal>
                                <goal>verify</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>

    <profiles>
        <!-- Native build profile -->
        <profile>
            <id>native</id>
            <activation>
                <property>
                    <name>native</name>
                </property>
            </activation>
            <properties>
                <quarkus.native.enabled>true</quarkus.native.enabled>
                <quarkus.package.jar.enabled>false</quarkus.package.jar.enabled>
            </properties>
        </profile>
        <!-- Container image build profile -->
        <profile>
            <id>container</id>
            <properties>
                <quarkus.container-image.build>true</quarkus.container-image.build>
            </properties>
        </profile>
    </profiles>
</project>
```

### Service Module POM Template

Each service module should include these dependencies:

```xml
<dependencies>
    <!-- Internal -->
    <dependency>
        <groupId>com.daytrader</groupId>
        <artifactId>daytrader-common</artifactId>
    </dependency>

    <!-- Quarkus Core -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-arc</artifactId>
    </dependency>
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-rest-jackson</artifactId>
    </dependency>

    <!-- Database -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-hibernate-orm-panache</artifactId>
    </dependency>
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-jdbc-postgresql</artifactId>
    </dependency>
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-flyway</artifactId>
    </dependency>

    <!-- Security -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-oidc</artifactId>
    </dependency>

    <!-- Observability -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-micrometer-registry-prometheus</artifactId>
    </dependency>
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-opentelemetry</artifactId>
    </dependency>
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-smallrye-health</artifactId>
    </dependency>

    <!-- OpenAPI -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-smallrye-openapi</artifactId>
    </dependency>

    <!-- MapStruct -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-junit5</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>io.rest-assured</groupId>
        <artifactId>rest-assured</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-test-keycloak-server</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 4. Database Schema Migration

### Current Tables → New Schema Mapping

| Legacy Table | Legacy Columns | New Table | New Columns | Notes |
|--------------|----------------|-----------|-------------|-------|
| `accountejb` | `ACCOUNTID`, `LOGINCOUNT`, `LOGOUTCOUNT`, `LASTLOGIN`, `CREATIONDATE`, `BALANCE`, `OPENBALANCE`, `PROFILE_USERID` | `account` | `id`, `login_count`, `logout_count`, `last_login`, `creation_date`, `balance`, `open_balance`, `profile_user_id` | Use SEQUENCE, add audit columns |
| `accountprofileejb` | `USERID`, `PASSWD`, `FULLNAME`, `ADDRESS`, `EMAIL`, `CREDITCARD` | `account_profile` | `user_id`, `password_hash`, `full_name`, `address`, `email`, `credit_card` | Hash password, snake_case |
| `quoteejb` | `SYMBOL`, `COMPANYNAME`, `VOLUME`, `PRICE`, `OPEN1`, `LOW`, `HIGH`, `CHANGE1` | `quote` | `symbol`, `company_name`, `volume`, `price`, `open_price`, `low_price`, `high_price`, `price_change` | Rename reserved words |
| `orderejb` | `ORDERID`, `ORDERTYPE`, `ORDERSTATUS`, `OPENDATE`, `COMPLETIONDATE`, `QUANTITY`, `PRICE`, `ORDERFEE`, `ACCOUNT_ACCOUNTID`, `QUOTE_SYMBOL`, `HOLDING_HOLDINGID` | `trade_order` | `id`, `order_type`, `order_status`, `open_date`, `completion_date`, `quantity`, `price`, `order_fee`, `account_id`, `quote_symbol`, `holding_id` | Avoid SQL keyword `ORDER` |
| `holdingejb` | `HOLDINGID`, `QUANTITY`, `PURCHASEPRICE`, `PURCHASEDATE`, `ACCOUNT_ACCOUNTID`, `QUOTE_SYMBOL` | `holding` | `id`, `quantity`, `purchase_price`, `purchase_date`, `account_id`, `quote_symbol` | Standard naming |
| `keygenejb` | `KEYNAME`, `KEYVAL` | *(removed)* | - | Use PostgreSQL sequences |

### Database Configuration (application.properties)

```properties
# PostgreSQL Connection
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=${DB_USERNAME:daytrader}
quarkus.datasource.password=${DB_PASSWORD:daytrader}
quarkus.datasource.jdbc.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:daytrader}

# Connection Pool (Agroal)
quarkus.datasource.jdbc.min-size=5
quarkus.datasource.jdbc.max-size=20
quarkus.datasource.jdbc.initial-size=5
quarkus.datasource.jdbc.acquisition-timeout=30s
quarkus.datasource.jdbc.idle-removal-interval=2m
quarkus.datasource.jdbc.max-lifetime=10m
quarkus.datasource.jdbc.validation-query-sql=SELECT 1
quarkus.datasource.jdbc.background-validation-interval=2m

# Hibernate ORM
quarkus.hibernate-orm.database.generation=none
quarkus.hibernate-orm.log.sql=false
quarkus.hibernate-orm.jdbc.statement-batch-size=25
quarkus.hibernate-orm.jdbc.statement-fetch-size=50
quarkus.hibernate-orm.physical-naming-strategy=org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy

# Flyway
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=classpath:db/migration
quarkus.flyway.validate-on-migrate=true
quarkus.flyway.clean-disabled=true
quarkus.flyway.baseline-on-migrate=false
```

### Flyway Migration Scripts

#### V1.0.0__create_account_schema.sql (Account Service)

```sql
-- ============================================
-- DayTrader Account Service - Initial Schema
-- Migration: V1.0.0
-- ============================================

-- Account Profile Table
CREATE TABLE account_profile (
    user_id         VARCHAR(255) PRIMARY KEY,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255),
    address         VARCHAR(500),
    email           VARCHAR(255),
    credit_card     VARCHAR(255),
    version         INTEGER DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Account Table with sequence
CREATE SEQUENCE account_id_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE account (
    id              BIGINT PRIMARY KEY DEFAULT nextval('account_id_seq'),
    profile_user_id VARCHAR(255) NOT NULL REFERENCES account_profile(user_id) ON DELETE CASCADE,
    login_count     INTEGER DEFAULT 0 NOT NULL,
    logout_count    INTEGER DEFAULT 0 NOT NULL,
    last_login      TIMESTAMP WITH TIME ZONE,
    creation_date   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    balance         DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    open_balance    DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    version         INTEGER DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT account_profile_unique UNIQUE (profile_user_id)
);

-- Indexes
CREATE INDEX idx_account_profile_user_id ON account(profile_user_id);
CREATE INDEX idx_account_profile_email ON account_profile(email);

-- Updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers
CREATE TRIGGER update_account_profile_updated_at
    BEFORE UPDATE ON account_profile
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_account_updated_at
    BEFORE UPDATE ON account
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

#### V1.0.0__create_market_schema.sql (Market Service)

```sql
-- ============================================
-- DayTrader Market Service - Initial Schema
-- Migration: V1.0.0
-- ============================================

-- Quote Table
CREATE TABLE quote (
    symbol          VARCHAR(10) PRIMARY KEY,
    company_name    VARCHAR(255),
    volume          DOUBLE PRECISION NOT NULL DEFAULT 0,
    price           DECIMAL(14,2),
    open_price      DECIMAL(14,2),
    low_price       DECIMAL(14,2),
    high_price      DECIMAL(14,2),
    price_change    DOUBLE PRECISION NOT NULL DEFAULT 0,
    version         INTEGER DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for market queries
CREATE INDEX idx_quote_price_change ON quote(price_change DESC);
CREATE INDEX idx_quote_volume ON quote(volume DESC);
CREATE INDEX idx_quote_company_name ON quote(company_name);

-- Apply trigger
CREATE TRIGGER update_quote_updated_at
    BEFORE UPDATE ON quote
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

#### V1.0.0__create_trading_schema.sql (Trading Service)

```sql
-- ============================================
-- DayTrader Trading Service - Initial Schema
-- Migration: V1.0.0
-- ============================================

-- Order Status Enum Type
CREATE TYPE order_status AS ENUM ('open', 'processing', 'closed', 'completed', 'cancelled');
CREATE TYPE order_type AS ENUM ('buy', 'sell');

-- Holding Table
CREATE SEQUENCE holding_id_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE holding (
    id              BIGINT PRIMARY KEY DEFAULT nextval('holding_id_seq'),
    account_id      BIGINT NOT NULL,
    quote_symbol    VARCHAR(10) NOT NULL,
    quantity        DOUBLE PRECISION NOT NULL DEFAULT 0,
    purchase_price  DECIMAL(14,2),
    purchase_date   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    version         INTEGER DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Trade Order Table (avoiding 'order' keyword)
CREATE SEQUENCE trade_order_id_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE trade_order (
    id              BIGINT PRIMARY KEY DEFAULT nextval('trade_order_id_seq'),
    order_type      order_type NOT NULL,
    order_status    order_status NOT NULL DEFAULT 'open',
    open_date       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completion_date TIMESTAMP WITH TIME ZONE,
    quantity        DOUBLE PRECISION NOT NULL,
    price           DECIMAL(14,2),
    order_fee       DECIMAL(14,2) DEFAULT 0.00,
    account_id      BIGINT NOT NULL,
    quote_symbol    VARCHAR(10) NOT NULL,
    holding_id      BIGINT REFERENCES holding(id) ON DELETE SET NULL,
    version         INTEGER DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for trading queries
CREATE INDEX idx_holding_account_id ON holding(account_id);
CREATE INDEX idx_holding_quote_symbol ON holding(quote_symbol);
CREATE INDEX idx_trade_order_account_id ON trade_order(account_id);
CREATE INDEX idx_trade_order_quote_symbol ON trade_order(quote_symbol);
CREATE INDEX idx_trade_order_status ON trade_order(order_status);
CREATE INDEX idx_trade_order_account_status ON trade_order(account_id, order_status);
CREATE INDEX idx_trade_order_open_date ON trade_order(open_date DESC);

-- Apply triggers
CREATE TRIGGER update_holding_updated_at
    BEFORE UPDATE ON holding
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_trade_order_updated_at
    BEFORE UPDATE ON trade_order
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

#### V1.0.1__seed_test_data.sql (Development Only)

```sql
-- ============================================
-- DayTrader - Test Data Seed (DEV ONLY)
-- Migration: V1.0.1
-- ============================================
-- NOTE: This migration should be profile-specific (dev, test)

-- Sample quotes (50 stocks)
INSERT INTO quote (symbol, company_name, volume, price, open_price, low_price, high_price, price_change)
SELECT
    's:' || i,
    'Company ' || i || ' Inc.',
    floor(random() * 1000000)::double precision,
    (50 + random() * 450)::decimal(14,2),
    (50 + random() * 450)::decimal(14,2),
    (50 + random() * 450)::decimal(14,2),
    (50 + random() * 450)::decimal(14,2),
    (-10 + random() * 20)::double precision
FROM generate_series(0, 49) AS i;

-- Sample user profiles (10 users)
INSERT INTO account_profile (user_id, password_hash, full_name, email)
SELECT
    'uid:' || i,
    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', -- password: 'xxx'
    'User ' || i,
    'user' || i || '@daytrader.com'
FROM generate_series(0, 9) AS i;

-- Sample accounts
INSERT INTO account (profile_user_id, balance, open_balance, login_count)
SELECT
    'uid:' || i,
    (10000 + random() * 90000)::decimal(14,2),
    (10000 + random() * 90000)::decimal(14,2),
    floor(random() * 100)::integer
FROM generate_series(0, 9) AS i;
```

### JPA Entity Examples

#### Account Entity (Panache)

```java
package com.daytrader.account.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "account")
public class Account extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_seq")
    @SequenceGenerator(name = "account_seq", sequenceName = "account_id_seq", allocationSize = 50)
    public Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_user_id", referencedColumnName = "user_id")
    public AccountProfile profile;

    @Column(name = "login_count")
    public int loginCount;

    @Column(name = "logout_count")
    public int logoutCount;

    @Column(name = "last_login")
    public Instant lastLogin;

    @Column(name = "creation_date")
    public Instant creationDate;

    @Column(precision = 14, scale = 2)
    public BigDecimal balance;

    @Column(name = "open_balance", precision = 14, scale = 2)
    public BigDecimal openBalance;

    @Version
    public int version;

    @Column(name = "created_at")
    public Instant createdAt;

    @Column(name = "updated_at")
    public Instant updatedAt;

    // Panache finder methods
    public static Account findByProfileUserId(String userId) {
        return find("profile.userId", userId).firstResult();
    }
}
```

#### Quote Entity (Panache)

```java
package com.daytrader.market.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "quote")
public class Quote extends PanacheEntityBase {

    @Id
    @Column(length = 10)
    public String symbol;

    @Column(name = "company_name")
    public String companyName;

    public double volume;

    @Column(precision = 14, scale = 2)
    public BigDecimal price;

    @Column(name = "open_price", precision = 14, scale = 2)
    public BigDecimal openPrice;

    @Column(name = "low_price", precision = 14, scale = 2)
    public BigDecimal lowPrice;

    @Column(name = "high_price", precision = 14, scale = 2)
    public BigDecimal highPrice;

    @Column(name = "price_change")
    public double priceChange;

    @Version
    public int version;

    // Panache finder methods
    public static List<Quote> findTopGainers(int limit) {
        return find("ORDER BY priceChange DESC").page(0, limit).list();
    }

    public static List<Quote> findTopLosers(int limit) {
        return find("ORDER BY priceChange ASC").page(0, limit).list();
    }

    public static List<Quote> findMostActive(int limit) {
        return find("ORDER BY volume DESC").page(0, limit).list();
    }
}
```

---

## 5. Security Architecture

### Authentication Flow: OIDC / JWT

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                         Authentication Flow                                   │
└──────────────────────────────────────────────────────────────────────────────┘

    ┌──────────┐                ┌─────────────┐                ┌───────────────┐
    │  Client  │                │  Keycloak   │                │   Quarkus     │
    │  (SPA)   │                │   (IdP)     │                │   Service     │
    └────┬─────┘                └──────┬──────┘                └───────┬───────┘
         │                             │                               │
         │  1. Login Request           │                               │
         │ ─────────────────────────►  │                               │
         │                             │                               │
         │  2. Return JWT Tokens       │                               │
         │ ◄─────────────────────────  │                               │
         │    (access_token,           │                               │
         │     refresh_token)          │                               │
         │                             │                               │
         │  3. API Request + Bearer Token                              │
         │ ───────────────────────────────────────────────────────────►│
         │                             │                               │
         │                             │  4. Validate JWT (offline)    │
         │                             │ ◄─────────────────────────────│
         │                             │    (via JWKS public keys)     │
         │                             │                               │
         │  5. API Response            │                               │
         │ ◄───────────────────────────────────────────────────────────│
         │                             │                               │
```

### Role-Based Access Control (RBAC)

| Legacy Role | New Role | Description | Permissions |
|-------------|----------|-------------|-------------|
| `grp1-grp5` | `trader` | Standard trading user | Buy, sell, view portfolio, view quotes |
| (new) | `admin` | System administrator | All trader + user management, config |
| (new) | `viewer` | Read-only access | View quotes, market summary only |
| (new) | `service` | Service-to-service | Internal API calls between services |

### Keycloak Realm Configuration

```json
{
  "realm": "daytrader",
  "enabled": true,
  "sslRequired": "external",
  "registrationAllowed": true,
  "loginWithEmailAllowed": true,
  "duplicateEmailsAllowed": false,
  "resetPasswordAllowed": true,
  "editUsernameAllowed": false,
  "bruteForceProtected": true,
  "permanentLockout": false,
  "maxFailureWaitSeconds": 900,
  "minimumQuickLoginWaitSeconds": 60,
  "waitIncrementSeconds": 60,
  "quickLoginCheckMilliSeconds": 1000,
  "maxDeltaTimeSeconds": 43200,
  "failureFactor": 5,
  "roles": {
    "realm": [
      { "name": "trader", "description": "Standard trading user" },
      { "name": "admin", "description": "System administrator" },
      { "name": "viewer", "description": "Read-only access" }
    ]
  },
  "clients": [
    {
      "clientId": "daytrader-api",
      "enabled": true,
      "publicClient": false,
      "bearerOnly": true,
      "standardFlowEnabled": false,
      "directAccessGrantsEnabled": true,
      "serviceAccountsEnabled": true
    },
    {
      "clientId": "daytrader-web",
      "enabled": true,
      "publicClient": true,
      "standardFlowEnabled": true,
      "directAccessGrantsEnabled": false,
      "redirectUris": [
        "http://localhost:3000/*",
        "https://daytrader.example.com/*"
      ],
      "webOrigins": [
        "http://localhost:3000",
        "https://daytrader.example.com"
      ]
    }
  ],
  "defaultRoles": ["trader"],
  "accessTokenLifespan": 300,
  "ssoSessionIdleTimeout": 1800,
  "ssoSessionMaxLifespan": 36000
}
```

### Security Configuration (application.properties)

```properties
# ===========================================
# OIDC / Keycloak Configuration
# ===========================================

# Auth server URL (Keycloak)
quarkus.oidc.auth-server-url=${KEYCLOAK_URL:http://localhost:8180}/realms/daytrader
quarkus.oidc.client-id=daytrader-api
quarkus.oidc.credentials.secret=${OIDC_CLIENT_SECRET:}
quarkus.oidc.tls.verification=none

# JWT token validation
quarkus.oidc.token.issuer=${KEYCLOAK_URL:http://localhost:8180}/realms/daytrader
quarkus.oidc.token.audience=account
quarkus.oidc.token.principal-claim=preferred_username

# Token refresh
quarkus.oidc.token.refresh-expired=true
quarkus.oidc.token.lifespan-grace=2

# ===========================================
# Authorization Policies
# ===========================================

# Public endpoints (no authentication required)
quarkus.http.auth.permission.public.paths=/q/health/*,/q/metrics/*,/q/openapi/*,/api/quotes/*
quarkus.http.auth.permission.public.policy=permit

# Authenticated endpoints (any valid token)
quarkus.http.auth.permission.authenticated.paths=/api/*
quarkus.http.auth.permission.authenticated.policy=authenticated

# Role-based policies
quarkus.http.auth.policy.trader-policy.roles-allowed=trader,admin
quarkus.http.auth.policy.admin-policy.roles-allowed=admin
quarkus.http.auth.policy.viewer-policy.roles-allowed=viewer,trader,admin

# Trading endpoints require trader role
quarkus.http.auth.permission.trading.paths=/api/orders/*,/api/holdings/*
quarkus.http.auth.permission.trading.policy=trader-policy

# Admin endpoints require admin role
quarkus.http.auth.permission.admin.paths=/api/admin/*
quarkus.http.auth.permission.admin.policy=admin-policy

# ===========================================
# CORS Configuration
# ===========================================
quarkus.http.cors=true
quarkus.http.cors.origins=${CORS_ORIGINS:http://localhost:3000}
quarkus.http.cors.headers=accept,authorization,content-type,x-requested-with
quarkus.http.cors.methods=GET,POST,PUT,DELETE,OPTIONS
quarkus.http.cors.exposed-headers=location,info
quarkus.http.cors.access-control-max-age=24H
quarkus.http.cors.access-control-allow-credentials=true

# ===========================================
# Security Headers
# ===========================================
quarkus.http.header."X-Content-Type-Options".value=nosniff
quarkus.http.header."X-Frame-Options".value=DENY
quarkus.http.header."X-XSS-Protection".value=1; mode=block
quarkus.http.header."Strict-Transport-Security".value=max-age=31536000; includeSubDomains
quarkus.http.header."Content-Security-Policy".value=default-src 'self'
```

### Secured REST Resource Example

```java
package com.daytrader.trading.resource;

import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class OrderResource {

    @Inject
    JsonWebToken jwt;

    @Inject
    OrderService orderService;

    @GET
    @RolesAllowed({"trader", "admin"})
    public Response getOrders(@Context SecurityContext ctx) {
        String userId = jwt.getClaim("preferred_username");
        return Response.ok(orderService.getOrdersByUser(userId)).build();
    }

    @POST
    @RolesAllowed({"trader", "admin"})
    @Blocking
    public Response createOrder(CreateOrderRequest request, @Context SecurityContext ctx) {
        String userId = jwt.getClaim("preferred_username");

        // Validate user can only create orders for themselves
        if (!userId.equals(request.userId())) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity("Cannot create orders for other users")
                .build();
        }

        var order = orderService.createOrder(request);
        return Response.status(Response.Status.CREATED).entity(order).build();
    }

    @GET
    @Path("/{orderId}")
    @RolesAllowed({"trader", "admin"})
    public Response getOrder(@PathParam("orderId") Long orderId) {
        String userId = jwt.getClaim("preferred_username");
        var order = orderService.getOrder(orderId);

        // Ensure user can only access their own orders (unless admin)
        if (!order.accountUserId().equals(userId) && !jwt.getGroups().contains("admin")) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        return Response.ok(order).build();
    }
}
```

### Service-to-Service Authentication

For internal service communication, use client credentials flow:

```properties
# Service account for inter-service calls
quarkus.oidc-client.auth-server-url=${KEYCLOAK_URL}/realms/daytrader
quarkus.oidc-client.client-id=daytrader-service
quarkus.oidc-client.credentials.secret=${SERVICE_CLIENT_SECRET}
quarkus.oidc-client.grant.type=client_credentials
```

```java
@ApplicationScoped
public class AccountServiceClient {

    @Inject
    @RestClient
    AccountService accountService;

    @Inject
    OidcClient oidcClient;

    public AccountDTO getAccount(Long accountId) {
        Tokens tokens = oidcClient.getTokens().await().indefinitely();
        return accountService.getAccount(accountId, "Bearer " + tokens.getAccessToken());
    }
}
```

---

## 6. Observability Stack

### Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Observability Stack                                  │
└─────────────────────────────────────────────────────────────────────────────┘

     ┌──────────────────┐         ┌──────────────────┐
     │   Quarkus        │         │   Quarkus        │
     │   Services       │         │   Services       │
     └────────┬─────────┘         └────────┬─────────┘
              │                            │
              │ ┌──────────────────────────┤
              │ │                          │
              ▼ ▼                          ▼
     ┌──────────────────┐         ┌──────────────────┐
     │   Prometheus     │         │     Jaeger       │
     │   (Metrics)      │         │    (Tracing)     │
     └────────┬─────────┘         └────────┬─────────┘
              │                            │
              ▼                            ▼
     ┌──────────────────┐         ┌──────────────────┐
     │    Grafana       │         │   Jaeger UI      │
     │   (Dashboards)   │         │  (Trace Search)  │
     └──────────────────┘         └──────────────────┘

                          ┌──────────────────┐
                          │   Loki/ELK       │
                          │   (Log Aggr.)    │
                          └──────────────────┘
```

### Metrics Configuration (Micrometer + Prometheus)

```properties
# ===========================================
# Metrics Configuration
# ===========================================

# Enable Prometheus metrics
quarkus.micrometer.enabled=true
quarkus.micrometer.registry-enabled-default=true
quarkus.micrometer.binder-enabled-default=true
quarkus.micrometer.export.prometheus.enabled=true
quarkus.micrometer.export.prometheus.path=/q/metrics

# Custom metrics prefix
quarkus.micrometer.binder.jvm=true
quarkus.micrometer.binder.http-client.enabled=true
quarkus.micrometer.binder.http-server.enabled=true
quarkus.micrometer.binder.system=true

# Histogram configuration for better percentile calculations
quarkus.micrometer.export.prometheus.histogram.buckets[0]=0.005
quarkus.micrometer.export.prometheus.histogram.buckets[1]=0.01
quarkus.micrometer.export.prometheus.histogram.buckets[2]=0.025
quarkus.micrometer.export.prometheus.histogram.buckets[3]=0.05
quarkus.micrometer.export.prometheus.histogram.buckets[4]=0.1
quarkus.micrometer.export.prometheus.histogram.buckets[5]=0.25
quarkus.micrometer.export.prometheus.histogram.buckets[6]=0.5
quarkus.micrometer.export.prometheus.histogram.buckets[7]=1.0
quarkus.micrometer.export.prometheus.histogram.buckets[8]=2.5
quarkus.micrometer.export.prometheus.histogram.buckets[9]=5.0
quarkus.micrometer.export.prometheus.histogram.buckets[10]=10.0
```

### Custom Business Metrics

```java
package com.daytrader.trading.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.DistributionSummary;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.Callable;

@ApplicationScoped
public class TradingMetrics {

    private final Counter buyOrdersCounter;
    private final Counter sellOrdersCounter;
    private final Counter ordersFailedCounter;
    private final Timer orderProcessingTimer;
    private final DistributionSummary orderValueSummary;
    private final DistributionSummary holdingValueSummary;

    @Inject
    public TradingMetrics(MeterRegistry registry) {
        this.buyOrdersCounter = Counter.builder("daytrader.orders.total")
            .tag("type", "buy")
            .description("Total number of buy orders")
            .register(registry);

        this.sellOrdersCounter = Counter.builder("daytrader.orders.total")
            .tag("type", "sell")
            .description("Total number of sell orders")
            .register(registry);

        this.ordersFailedCounter = Counter.builder("daytrader.orders.failed")
            .description("Total number of failed orders")
            .register(registry);

        this.orderProcessingTimer = Timer.builder("daytrader.order.processing.time")
            .description("Time taken to process an order")
            .publishPercentiles(0.5, 0.95, 0.99)
            .publishPercentileHistogram()
            .register(registry);

        this.orderValueSummary = DistributionSummary.builder("daytrader.order.value")
            .description("Distribution of order values in USD")
            .baseUnit("usd")
            .publishPercentiles(0.5, 0.75, 0.95, 0.99)
            .register(registry);

        this.holdingValueSummary = DistributionSummary.builder("daytrader.holding.value")
            .description("Distribution of holding values")
            .baseUnit("usd")
            .register(registry);
    }

    public void recordBuyOrder() {
        buyOrdersCounter.increment();
    }

    public void recordSellOrder() {
        sellOrdersCounter.increment();
    }

    public void recordOrderFailed() {
        ordersFailedCounter.increment();
    }

    public void recordOrderValue(BigDecimal value) {
        orderValueSummary.record(value.doubleValue());
    }

    public <T> T timeOrderProcessing(Callable<T> operation) throws Exception {
        return orderProcessingTimer.recordCallable(operation);
    }

    public void recordOrderProcessingTime(Duration duration) {
        orderProcessingTimer.record(duration);
    }
}
```

### Distributed Tracing (OpenTelemetry)

```properties
# ===========================================
# OpenTelemetry / Tracing Configuration
# ===========================================

# Enable OpenTelemetry
quarkus.otel.enabled=true
quarkus.otel.sdk.disabled=false

# Service name (appears in traces)
quarkus.otel.service.name=${quarkus.application.name}
quarkus.application.name=daytrader-trading-service

# OTLP exporter configuration (Jaeger)
quarkus.otel.exporter.otlp.endpoint=${OTEL_EXPORTER_ENDPOINT:http://localhost:4317}
quarkus.otel.exporter.otlp.traces.protocol=grpc
quarkus.otel.exporter.otlp.traces.compression=gzip

# Sampling (adjust for production - 1.0 = 100% of traces)
quarkus.otel.traces.sampler=parentbased_traceidratio
quarkus.otel.traces.sampler.arg=1.0

# Propagation format
quarkus.otel.propagators=tracecontext,baggage

# Resource attributes
quarkus.otel.resource.attributes=service.namespace=daytrader,deployment.environment=${ENVIRONMENT:development}

# Span limits
quarkus.otel.span.attribute.count.limit=128
quarkus.otel.span.event.count.limit=128
quarkus.otel.span.link.count.limit=128
```

### Custom Tracing Example

```java
package com.daytrader.trading.service;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OrderService {

    @Inject
    Tracer tracer;

    @Inject
    OrderRepository orderRepository;

    @Inject
    AccountServiceClient accountClient;

    @WithSpan("process-buy-order")
    @Transactional
    public OrderDTO processBuyOrder(
            @SpanAttribute("order.symbol") String symbol,
            @SpanAttribute("order.quantity") double quantity,
            @SpanAttribute("order.accountId") Long accountId) {

        Span currentSpan = Span.current();

        // Add custom attributes to the span
        currentSpan.setAttribute("order.type", "buy");

        // Manual child span for specific operation
        Span validateSpan = tracer.spanBuilder("validate-account-funds")
            .startSpan();

        try (Scope scope = validateSpan.makeCurrent()) {
            var account = accountClient.getAccount(accountId);
            validateSpan.setAttribute("account.balance", account.balance().toString());

            if (account.balance().compareTo(/* calculated cost */) < 0) {
                validateSpan.recordException(new InsufficientFundsException());
                throw new InsufficientFundsException("Insufficient funds");
            }
        } finally {
            validateSpan.end();
        }

        // Continue with order creation
        var order = createOrder(symbol, quantity, accountId);
        currentSpan.setAttribute("order.id", order.id());

        return order;
    }
}
```

### Structured Logging Configuration

```properties
# ===========================================
# Logging Configuration
# ===========================================

# Console logging
quarkus.log.console.enable=true
quarkus.log.console.format=%d{yyyy-MM-dd HH:mm:ss.SSS} %-5p [%c{2.}] (%t) %s%e%n
quarkus.log.console.level=INFO
quarkus.log.console.color=true

# JSON logging for production (structured logs)
%prod.quarkus.log.console.json=true
%prod.quarkus.log.console.json.date-format=yyyy-MM-dd HH:mm:ss.SSS
%prod.quarkus.log.console.json.exception-output-type=formatted
%prod.quarkus.log.console.json.print-details=true

# Add trace context to logs (correlates logs with traces)
quarkus.log.console.json.additional-field.traceId.value=trace_id
quarkus.log.console.json.additional-field.spanId.value=span_id
quarkus.log.console.json.additional-field.service.value=${quarkus.application.name}

# Log categories
quarkus.log.category."com.daytrader".level=DEBUG
quarkus.log.category."org.hibernate.SQL".level=DEBUG
quarkus.log.category."io.quarkus.oidc".level=DEBUG
%prod.quarkus.log.category."org.hibernate.SQL".level=WARN

# File logging (optional)
quarkus.log.file.enable=false
%prod.quarkus.log.file.enable=true
%prod.quarkus.log.file.path=/var/log/daytrader/application.log
%prod.quarkus.log.file.rotation.max-file-size=10M
%prod.quarkus.log.file.rotation.max-backup-index=5
```

### Health Checks

```java
package com.daytrader.common.health;

import io.smallrye.health.api.HealthGroup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.Startup;

import java.sql.Connection;

@Liveness
@ApplicationScoped
public class LivenessCheck implements HealthCheck {
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.up("Application is alive");
    }
}

@Readiness
@ApplicationScoped
public class DatabaseReadinessCheck implements HealthCheck {

    @Inject
    DataSource dataSource;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse
            .named("Database connection");

        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(5);
            if (valid) {
                return builder.up()
                    .withData("database", connection.getMetaData().getDatabaseProductName())
                    .withData("url", connection.getMetaData().getURL())
                    .build();
            } else {
                return builder.down()
                    .withData("error", "Connection validation failed")
                    .build();
            }
        } catch (Exception e) {
            return builder.down()
                .withData("error", e.getMessage())
                .build();
        }
    }
}

@Startup
@ApplicationScoped
public class StartupCheck implements HealthCheck {

    private volatile boolean ready = false;

    public void markReady() {
        this.ready = true;
    }

    @Override
    public HealthCheckResponse call() {
        if (ready) {
            return HealthCheckResponse.up("Application started");
        }
        return HealthCheckResponse.down("Application still starting");
    }
}

@Readiness
@HealthGroup("external")
@ApplicationScoped
public class KeycloakHealthCheck implements HealthCheck {

    @ConfigProperty(name = "quarkus.oidc.auth-server-url")
    String keycloakUrl;

    @Override
    public HealthCheckResponse call() {
        // Check Keycloak connectivity
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(keycloakUrl + "/.well-known/openid-configuration"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

            HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return HealthCheckResponse.up("Keycloak");
            }
            return HealthCheckResponse.down("Keycloak - HTTP " + response.statusCode());
        } catch (Exception e) {
            return HealthCheckResponse.down("Keycloak - " + e.getMessage());
        }
    }
}
```

### Health Check Configuration

```properties
# ===========================================
# Health Check Configuration
# ===========================================

# Enable health endpoints
quarkus.smallrye-health.root-path=/q/health
quarkus.smallrye-health.liveness-path=/live
quarkus.smallrye-health.readiness-path=/ready
quarkus.smallrye-health.startup-path=/started

# Include additional info in health responses
quarkus.smallrye-health.additional.property.app.name=${quarkus.application.name}
quarkus.smallrye-health.additional.property.app.version=${quarkus.application.version:1.0.0}

# Health check timeout
quarkus.smallrye-health.check.timeout=10s
```

---

## 7. CI/CD Pipeline

### GitHub Actions Workflow Structure

```
.github/
└── workflows/
    ├── ci.yml              # Continuous Integration (PR, push)
    ├── release.yml         # Release and deployment
    └── security-scan.yml   # Security vulnerability scanning
```

### CI Workflow (.github/workflows/ci.yml)

```yaml
name: CI Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

env:
  JAVA_VERSION: '21'
  MAVEN_OPTS: '-Xmx2g -XX:+UseG1GC'
  REGISTRY: ghcr.io
  IMAGE_PREFIX: ${{ github.repository_owner }}/daytrader

jobs:
  # ==========================================
  # Build and Test
  # ==========================================
  build:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
      security-events: write

    strategy:
      matrix:
        module: [daytrader-common, daytrader-account-service,
                 daytrader-trading-service, daytrader-market-service]

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: 'maven'

      - name: Cache Maven dependencies
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-maven-

      - name: Build parent and common module
        run: |
          mvn -B install -pl daytrader-common -am -DskipTests

      - name: Build ${{ matrix.module }}
        run: |
          mvn -B verify -pl ${{ matrix.module }} -am \
            -Dquarkus.package.jar.enabled=true

      - name: Upload test results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-results-${{ matrix.module }}
          path: ${{ matrix.module }}/target/surefire-reports/

      - name: Upload coverage report
        uses: codecov/codecov-action@v4
        with:
          files: ${{ matrix.module }}/target/jacoco-report/jacoco.xml
          flags: ${{ matrix.module }}
          fail_ci_if_error: false

  # ==========================================
  # Integration Tests
  # ==========================================
  integration-tests:
    runs-on: ubuntu-latest
    needs: build

    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB: daytrader_test
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: 'maven'

      - name: Run integration tests
        run: |
          mvn -B verify -Pintegration-tests \
            -Dquarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/daytrader_test \
            -Dquarkus.datasource.username=test \
            -Dquarkus.datasource.password=test

  # ==========================================
  # Build Container Images
  # ==========================================
  container-build:
    runs-on: ubuntu-latest
    needs: [build, integration-tests]
    if: github.event_name == 'push' && github.ref == 'refs/heads/main'

    strategy:
      matrix:
        module: [daytrader-account-service, daytrader-trading-service, daytrader-market-service]

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: 'maven'

      - name: Log in to Container Registry
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Extract module name
        id: module
        run: |
          MODULE_NAME=$(echo ${{ matrix.module }} | sed 's/daytrader-//')
          echo "name=${MODULE_NAME}" >> $GITHUB_OUTPUT

      - name: Build and push container image
        run: |
          mvn -B package -pl ${{ matrix.module }} -am -DskipTests \
            -Dquarkus.container-image.build=true \
            -Dquarkus.container-image.push=true \
            -Dquarkus.container-image.registry=${{ env.REGISTRY }} \
            -Dquarkus.container-image.group=${{ github.repository_owner }} \
            -Dquarkus.container-image.name=${{ steps.module.outputs.name }} \
            -Dquarkus.container-image.tag=${{ github.sha }} \
            -Dquarkus.container-image.additional-tags=latest \
            -Dquarkus.jib.base-jvm-image=eclipse-temurin:21-jre-alpine

  # ==========================================
  # Security Scanning
  # ==========================================
  security-scan:
    runs-on: ubuntu-latest
    needs: build

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Run Trivy vulnerability scanner
        uses: aquasecurity/trivy-action@master
        with:
          scan-type: 'fs'
          scan-ref: '.'
          format: 'sarif'
          output: 'trivy-results.sarif'
          severity: 'CRITICAL,HIGH'

      - name: Upload Trivy scan results
        uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: 'trivy-results.sarif'

      - name: OWASP Dependency Check
        uses: dependency-check/Dependency-Check_Action@main
        with:
          project: 'daytrader'
          path: '.'
          format: 'HTML'
          args: >-
            --failOnCVSS 8
            --enableRetired
```

### Release Workflow (.github/workflows/release.yml)

```yaml
name: Release Pipeline

on:
  release:
    types: [published]

env:
  JAVA_VERSION: '21'
  REGISTRY: ghcr.io

jobs:
  release:
    runs-on: ubuntu-latest
    permissions:
      contents: write
      packages: write

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: 'maven'

      - name: Set version from tag
        run: |
          VERSION=${GITHUB_REF#refs/tags/v}
          mvn -B versions:set -DnewVersion=${VERSION}

      - name: Build and deploy
        run: |
          mvn -B clean deploy -DskipTests \
            -Dquarkus.container-image.build=true \
            -Dquarkus.container-image.push=true \
            -Dquarkus.container-image.tag=${VERSION}
        env:
          VERSION: ${{ github.ref_name }}
```

---

## 8. Development Environment

### Docker Compose Setup

```yaml
# docker/docker-compose.yml
version: '3.9'

services:
  # ==========================================
  # PostgreSQL Database
  # ==========================================
  postgres:
    image: postgres:16-alpine
    container_name: daytrader-postgres
    environment:
      POSTGRES_DB: daytrader
      POSTGRES_USER: daytrader
      POSTGRES_PASSWORD: daytrader
      PGDATA: /var/lib/postgresql/data/pgdata
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./postgres/init.sql:/docker-entrypoint-initdb.d/init.sql:ro
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U daytrader -d daytrader"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - daytrader-network

  # ==========================================
  # Keycloak Identity Provider
  # ==========================================
  keycloak:
    image: quay.io/keycloak/keycloak:26.0
    container_name: daytrader-keycloak
    command: start-dev --import-realm
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/daytrader
      KC_DB_USERNAME: daytrader
      KC_DB_PASSWORD: daytrader
      KC_DB_SCHEMA: keycloak
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      KC_HTTP_PORT: 8180
      KC_HEALTH_ENABLED: true
      KC_METRICS_ENABLED: true
    volumes:
      - ./keycloak/realm-export.json:/opt/keycloak/data/import/realm-export.json:ro
    ports:
      - "8180:8180"
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "exec 3<>/dev/tcp/localhost/8180"]
      interval: 30s
      timeout: 10s
      retries: 5
    networks:
      - daytrader-network

  # ==========================================
  # Apache Kafka (Redpanda - Kafka-compatible)
  # ==========================================
  redpanda:
    image: docker.redpanda.com/redpandadata/redpanda:v24.3.1
    container_name: daytrader-kafka
    command:
      - redpanda
      - start
      - --kafka-addr internal://0.0.0.0:9092,external://0.0.0.0:19092
      - --advertise-kafka-addr internal://redpanda:9092,external://localhost:19092
      - --pandaproxy-addr internal://0.0.0.0:8082,external://0.0.0.0:18082
      - --schema-registry-addr internal://0.0.0.0:8081,external://0.0.0.0:18081
      - --smp 1
      - --memory 1G
      - --reserve-memory 0M
      - --overprovisioned
      - --node-id 0
    ports:
      - "18081:18081"  # Schema Registry
      - "18082:18082"  # Pandaproxy
      - "19092:19092"  # Kafka
    volumes:
      - redpanda_data:/var/lib/redpanda/data
    healthcheck:
      test: ["CMD-SHELL", "rpk cluster health | grep -E 'Healthy:.+true'"]
      interval: 15s
      timeout: 5s
      retries: 5
    networks:
      - daytrader-network

  # ==========================================
  # Redpanda Console (Kafka UI)
  # ==========================================
  redpanda-console:
    image: docker.redpanda.com/redpandadata/console:v2.8.0
    container_name: daytrader-kafka-ui
    environment:
      KAFKA_BROKERS: redpanda:9092
      SCHEMA_REGISTRY_URL: http://redpanda:8081
    ports:
      - "8088:8080"
    depends_on:
      redpanda:
        condition: service_healthy
    networks:
      - daytrader-network

  # ==========================================
  # Jaeger (Distributed Tracing)
  # ==========================================
  jaeger:
    image: jaegertracing/all-in-one:1.64
    container_name: daytrader-jaeger
    environment:
      COLLECTOR_OTLP_ENABLED: true
    ports:
      - "4317:4317"   # OTLP gRPC
      - "4318:4318"   # OTLP HTTP
      - "16686:16686" # Jaeger UI
    networks:
      - daytrader-network

  # ==========================================
  # Prometheus (Metrics)
  # ==========================================
  prometheus:
    image: prom/prometheus:v3.0.1
    container_name: daytrader-prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.enable-lifecycle'
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus_data:/prometheus
    ports:
      - "9090:9090"
    networks:
      - daytrader-network

  # ==========================================
  # Grafana (Dashboards)
  # ==========================================
  grafana:
    image: grafana/grafana:11.4.0
    container_name: daytrader-grafana
    environment:
      GF_SECURITY_ADMIN_USER: admin
      GF_SECURITY_ADMIN_PASSWORD: admin
      GF_USERS_ALLOW_SIGN_UP: false
    volumes:
      - grafana_data:/var/lib/grafana
      - ./grafana/provisioning:/etc/grafana/provisioning:ro
      - ./grafana/dashboards:/var/lib/grafana/dashboards:ro
    ports:
      - "3001:3000"
    depends_on:
      - prometheus
    networks:
      - daytrader-network

volumes:
  postgres_data:
  redpanda_data:
  prometheus_data:
  grafana_data:

networks:
  daytrader-network:
    driver: bridge
```

### PostgreSQL Initialization Script

```sql
-- docker/postgres/init.sql
-- Create schema for Keycloak
CREATE SCHEMA IF NOT EXISTS keycloak;

-- Create separate databases for each service (optional - for stricter isolation)
-- CREATE DATABASE daytrader_account;
-- CREATE DATABASE daytrader_trading;
-- CREATE DATABASE daytrader_market;

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA public TO daytrader;
GRANT ALL PRIVILEGES ON SCHEMA keycloak TO daytrader;
```

### Prometheus Configuration

```yaml
# docker/prometheus/prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'daytrader-account-service'
    metrics_path: '/q/metrics'
    static_configs:
      - targets: ['host.docker.internal:8081']
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
        replacement: 'account-service'

  - job_name: 'daytrader-trading-service'
    metrics_path: '/q/metrics'
    static_configs:
      - targets: ['host.docker.internal:8082']
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
        replacement: 'trading-service'

  - job_name: 'daytrader-market-service'
    metrics_path: '/q/metrics'
    static_configs:
      - targets: ['host.docker.internal:8083']
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
        replacement: 'market-service'

  - job_name: 'keycloak'
    metrics_path: '/metrics'
    static_configs:
      - targets: ['keycloak:8180']
```

### Quarkus Dev Mode Configuration

For local development without Docker Compose (using Quarkus Dev Services):

```properties
# application.properties - Development profile
%dev.quarkus.datasource.devservices.enabled=true
%dev.quarkus.datasource.devservices.image-name=postgres:16-alpine
%dev.quarkus.datasource.devservices.port=5433

%dev.quarkus.oidc.devservices.enabled=true
%dev.quarkus.oidc.devservices.port=8180
%dev.quarkus.oidc.devservices.realm-path=keycloak/realm-export.json

%dev.quarkus.kafka.devservices.enabled=true
%dev.quarkus.kafka.devservices.port=9092

# Disable telemetry in dev
%dev.quarkus.otel.sdk.disabled=true
```

### Development Workflow Commands

```bash
# Start all infrastructure services
cd docker && docker-compose up -d

# Start Quarkus in dev mode (with live reload)
cd daytrader-account-service
mvn quarkus:dev

# Run specific service on different port
mvn quarkus:dev -Dquarkus.http.port=8081

# Run integration tests
mvn verify -Pintegration-tests

# Build container image locally
mvn package -Dquarkus.container-image.build=true

# Run native build (requires GraalVM)
mvn package -Pnative

# Generate OpenAPI spec
mvn quarkus:dev
# Then visit: http://localhost:8080/q/openapi
```

---

## 9. Phase 1 Deliverables

| Deliverable | Description | Success Criteria |
|-------------|-------------|------------------|
| Project scaffolding | Maven multi-module structure with parent POM | `mvn clean install` builds successfully |
| Database schema | Flyway migrations for all entities | All migrations apply without errors |
| Dev environment | Docker Compose with all dependencies | `docker-compose up` starts all services |
| Security foundation | OIDC/JWT with Keycloak integration | JWT authentication works with test tokens |
| Common library | Shared DTOs, events, exceptions | Imported and used by all service modules |
| CI/CD pipeline | GitHub Actions workflows | Build, test, and image push automated |
| Observability | Metrics, tracing, health checks | Endpoints respond at `/q/health`, `/q/metrics` |
| API documentation | OpenAPI/Swagger generated | Spec available at `/q/openapi` |

---

## 10. Acceptance Criteria

### Infrastructure Acceptance

| Criteria | Verification Method | Target |
|----------|---------------------|--------|
| Quarkus services start quickly (dev mode) | `time mvn quarkus:dev` | < 3 seconds |
| Quarkus services start quickly (JVM mode) | `time java -jar app.jar` | < 1.5 seconds |
| Native image startup | `time ./app-runner` | < 100ms |
| Database migrations apply | `mvn flyway:migrate` | All migrations pass |
| JWT authentication works | REST Assured tests | Protected endpoints return 401/403 appropriately |
| Health endpoints respond | `curl /q/health` | All checks pass |
| Metrics exposed | `curl /q/metrics` | Prometheus format returned |
| OpenAPI generated | `curl /q/openapi` | Valid OpenAPI 3.x spec |
| Unit test coverage | JaCoCo report | > 70% line coverage |
| Integration tests pass | `mvn verify -Pit` | All tests green |
| Container image builds | `docker build` | Image < 200MB (JVM), < 100MB (native) |
| Docker Compose starts | `docker-compose up -d` | All services healthy within 60s |

### Code Quality Acceptance

| Criteria | Tool | Target |
|----------|------|--------|
| No critical vulnerabilities | Trivy / OWASP | 0 CRITICAL, 0 HIGH |
| Code style consistency | Checkstyle / Spotless | 0 violations |
| No code smells | SonarQube | A rating |
| Documentation coverage | Javadoc | All public APIs documented |

### Checklist

- [ ] Maven multi-module project builds with `mvn clean install`
- [ ] All Flyway migrations apply successfully to PostgreSQL
- [ ] Keycloak realm imports correctly
- [ ] JWT tokens are validated correctly by services
- [ ] RBAC permissions enforce role-based access
- [ ] Health endpoints (`/q/health/live`, `/q/health/ready`) return UP
- [ ] Prometheus metrics available at `/q/metrics`
- [ ] Traces appear in Jaeger when requests are made
- [ ] Structured JSON logs output in production profile
- [ ] GitHub Actions CI pipeline passes
- [ ] Container images push to registry
- [ ] Docker Compose environment starts all dependencies

---

## 11. Dependencies

### External Dependencies

| Dependency | Version | Purpose | Source |
|------------|---------|---------|--------|
| PostgreSQL | 16.x | Primary database | Docker Hub |
| Keycloak | 26.x | Identity provider | Quay.io |
| Redpanda/Kafka | 24.x | Event streaming | Docker Hub |
| Jaeger | 1.64.x | Distributed tracing | Docker Hub |
| Prometheus | 3.x | Metrics collection | Docker Hub |
| Grafana | 11.x | Dashboards | Docker Hub |

### Internal Dependencies

- **Phase 0 (Architecture Assessment)**: Complete ✅
- **Legacy system access**: Read access to legacy codebase for reference
- **GitHub repository**: Repository created with branch protection

### Team Dependencies

- Development team onboarded to Quarkus
- Access to container registry (GHCR)
- Access to Keycloak admin console

---

## 12. Risks & Mitigations

| Risk | Probability | Impact | Mitigation Strategy | Owner |
|------|-------------|--------|---------------------|-------|
| Team unfamiliar with Quarkus | Medium | High | Provide Quarkus training sessions, create quickstart guides, pair programming | Tech Lead |
| Database schema migration complexity | Low | Medium | Thorough field mapping, maintain dual-write period, automated data validation | DBA |
| OIDC/Keycloak integration issues | Medium | Medium | Start with dev services, document common issues, have fallback to simple JWT | Security Lead |
| Kafka message format changes | Low | Medium | Use Avro/JSON Schema, version events, maintain backward compatibility | Architect |
| Container image size too large | Low | Low | Use multi-stage builds, native images for critical paths, Alpine base | DevOps |
| CI/CD pipeline failures | Medium | Medium | Comprehensive test coverage, parallel job execution, caching | DevOps |
| Performance regression | Low | High | Establish baseline metrics, continuous benchmarking, load testing | QA Lead |

### Risk Response Plan

1. **If Quarkus adoption is slow**: Schedule additional training, create internal documentation
2. **If migrations fail in production**: Rollback procedure documented, test migrations on production copy first
3. **If OIDC doesn't work**: Fallback to simple JWT validation without Keycloak

---

## 13. Implementation Notes for Downstream Agents

### For quarkus-engineer

1. **Project Creation**: Use Quarkus CLI or https://code.quarkus.io to scaffold initial project
2. **Module Order**: Build `daytrader-common` first, then services in parallel
3. **Dev Mode**: Use `quarkus:dev` for rapid iteration with live reload
4. **Testing**: Write tests alongside implementation, use `@QuarkusTest` for integration tests
5. **Entity Mapping**: Follow JPA entity examples in Section 4

### For qa-engineer

1. **Test Framework**: JUnit 5 + REST Assured + Testcontainers
2. **Test Profiles**: Create separate test profiles for unit, integration, and e2e
3. **Security Testing**: Test both authenticated and unauthenticated flows
4. **Performance Baseline**: Establish baseline response times for key endpoints

### For verifier

1. **Structural Verification**: Ensure directory structure matches spec
2. **Configuration Verification**: Validate application.properties against spec
3. **Schema Verification**: Compare Flyway migrations against legacy schema
4. **Security Verification**: Test all protected endpoints with various roles

---

## 14. Next Agent Actions

| Agent | Priority | Action | Deliverable |
|-------|----------|--------|-------------|
| **quarkus-engineer** | P0 | Create Maven multi-module project structure | `daytrader-quarkus/` directory |
| **quarkus-engineer** | P0 | Implement `daytrader-common` module with DTOs, events, exceptions | Compiled JAR |
| **quarkus-engineer** | P1 | Configure parent POM with all dependencies | `pom.xml` |
| **quarkus-engineer** | P1 | Create Flyway migrations for all entities | `db/migration/*.sql` |
| **quarkus-engineer** | P1 | Set up Docker Compose development environment | `docker/docker-compose.yml` |
| **quarkus-engineer** | P2 | Configure Keycloak realm with roles | `realm-export.json` |
| **quarkus-engineer** | P2 | Create GitHub Actions CI workflow | `.github/workflows/ci.yml` |
| **qa-engineer** | P1 | Set up test framework and base test classes | Test infrastructure |
| **qa-engineer** | P2 | Create smoke tests for health endpoints | Health check tests |
| **verifier** | P1 | Validate project structure against this spec | Verification report |
| **verifier** | P2 | Validate database schema completeness | Schema comparison report |

---

## 15. Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-01-31 | Software Architect | Initial draft |
| 2.0 | 2026-01-31 | Software Architect | Complete specification with all sections |

---
*Document Version: 2.0 | Last Updated: 2026-01-31 | Status: Complete - Ready for Review*

