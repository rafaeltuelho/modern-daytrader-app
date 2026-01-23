# 📈 DayTrader - Modern Stock Trading Application

![Quarkus](https://img.shields.io/badge/Quarkus-3.30.7-blue?logo=quarkus)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react)
![TypeScript](https://img.shields.io/badge/TypeScript-5.9-3178C6?logo=typescript)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![License](https://img.shields.io/badge/License-Apache%202.0-green)

## Overview

DayTrader is a **modernized stock trading benchmark application** originally developed by IBM. This version has been completely re-architected from Java EE7/WebSphere Liberty to a modern cloud-native stack featuring **Quarkus** on the backend and **React** on the frontend.

The application simulates an online stock trading system where users can register, manage portfolios, buy and sell stocks, and track market activity.

---

## 🛠️ Technology Stack

| Layer | Technology | Description |
|-------|------------|-------------|
| **Backend** | [Quarkus 3.30](https://quarkus.io/) | Supersonic Subatomic Java framework |
| | [Hibernate Panache](https://quarkus.io/guides/hibernate-orm-panache) | Simplified ORM with active record pattern |
| | [PostgreSQL](https://www.postgresql.org/) | Relational database |
| | [SmallRye JWT](https://smallrye.io/smallrye-jwt/) | JWT-based authentication |
| | [Flyway](https://flywaydb.org/) | Database migrations |
| | [SmallRye OpenAPI](https://quarkus.io/guides/openapi-swaggerui) | API documentation |
| **Frontend** | [React 19](https://react.dev/) | UI component library |
| | [TypeScript 5.9](https://www.typescriptlang.org/) | Type-safe JavaScript |
| | [Vite 7](https://vitejs.dev/) | Next-generation build tool |
| | [TailwindCSS 4](https://tailwindcss.com/) | Utility-first CSS framework |
| | [TanStack Query](https://tanstack.com/query) | Async state management |
| | [React Router 7](https://reactrouter.com/) | Client-side routing |

---

## 📋 Prerequisites

- **Java 21** (OpenJDK or GraalVM)
- **Node.js 18+** with npm
- **Docker** (optional, for PostgreSQL)
- **Maven 3.9+** (or use included wrapper)

---

## 🚀 Quick Start

### 1. Start the Database

```bash
cd daytrader-quarkus
docker-compose up -d
```

### 2. Start the Backend

```bash
cd daytrader-quarkus
./mvnw quarkus:dev
```

The backend starts at **http://localhost:8080**

### 3. Start the Frontend

```bash
cd daytrader-frontend
npm install
npm run dev
```

The frontend starts at **http://localhost:5173**

---

## 📁 Project Structure

```
sample-daytrader7/
├── daytrader-quarkus/        # Backend - Quarkus REST API
│   ├── src/main/java/        # Java source code
│   ├── src/main/resources/   # Configuration & migrations
│   ├── docker-compose.yml    # PostgreSQL container
│   └── pom.xml               # Maven configuration
│
├── daytrader-frontend/       # Frontend - React SPA
│   ├── src/
│   │   ├── api/              # API client layer
│   │   ├── components/       # Reusable UI components
│   │   ├── context/          # React context providers
│   │   ├── hooks/            # Custom React hooks
│   │   ├── pages/            # Page components
│   │   └── types/            # TypeScript type definitions
│   └── package.json          # npm configuration
│
├── docs/                     # Documentation
├── jmeter_files/             # Load testing scripts
└── README.md                 # This file
```

---

## 📖 API Documentation

Once the backend is running, access the interactive API documentation:

- **Swagger UI**: [http://localhost:8080/swagger-ui](http://localhost:8080/swagger-ui)
- **OpenAPI Spec**: [http://localhost:8080/openapi](http://localhost:8080/openapi)

---

## 🔐 Test Accounts

The application comes with pre-configured test accounts for development:

| Username | Password | Description |
|----------|----------|-------------|
| `uid:0` | `xxx` | Test user 0 |
| `uid:1` | `xxx` | Test user 1 |
| `uid:2` | `xxx` | Test user 2 |

---

## 📚 Documentation

Additional documentation is available in the [`docs/`](./docs/) folder:

- Architecture overview
- API design decisions
- Deployment guides
- Development guidelines

---

## 🧪 Running Tests

### Backend Tests

```bash
cd daytrader-quarkus
./mvnw test
```

### Frontend Linting

```bash
cd daytrader-frontend
npm run lint
```

---

## 📝 License

This project is licensed under the **Apache License 2.0** - see the [LICENSE](./LICENSE) file for details.

---

<p align="center">
  <sub>Originally developed by IBM • Modernized with ❤️ using Quarkus & React</sub>
</p>
