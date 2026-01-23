# DayTrader Deployment Guide

This guide covers deployment options for the modernized DayTrader application with Quarkus backend and React frontend.

## Overview

The modernized DayTrader application consists of:
- **Backend**: Quarkus REST API (Java 21)
- **Frontend**: React + TypeScript + Vite
- **Database**: PostgreSQL 16

Deployment options include:
- JVM mode (recommended for most deployments)
- Native mode (GraalVM - for optimized startup and memory)
- Docker containers
- Kubernetes / OpenShift

---

## Building for Production

### Backend (Quarkus)

#### JVM Mode

```bash
cd daytrader-quarkus
./mvnw clean package
```

Output location: `target/quarkus-app/`
- Main JAR: `target/quarkus-app/quarkus-run.jar`
- Dependencies: `target/quarkus-app/lib/`

Run the application:
```bash
java -jar target/quarkus-app/quarkus-run.jar
```

#### Native Mode (GraalVM)

Requires GraalVM with native-image installed:

```bash
cd daytrader-quarkus
./mvnw clean package -Dnative
```

Output: `target/daytrader-quarkus-1.0.0-SNAPSHOT-runner`

Run the native executable:
```bash
./target/daytrader-quarkus-1.0.0-SNAPSHOT-runner
```

### Frontend (React)

```bash
cd daytrader-frontend
npm install
npm run build
```

Output location: `dist/`

The `dist/` folder contains static files ready to be served by nginx or any static file server.

---

## Docker Deployment

### Building Backend Container

Quarkus provides Dockerfiles in `src/main/docker/`:

**JVM Mode:**
```bash
cd daytrader-quarkus
./mvnw clean package
docker build -f src/main/docker/Dockerfile.jvm -t daytrader/backend:latest .
```

**Native Mode:**
```bash
cd daytrader-quarkus
./mvnw clean package -Dnative
docker build -f src/main/docker/Dockerfile.native -t daytrader/backend-native:latest .
```

### Building Frontend Container

Create `daytrader-frontend/Dockerfile`:
```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

Create `daytrader-frontend/nginx.conf`:
```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### Docker Compose (Full Stack)

Create `docker-compose.prod.yml`:
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: daytraderdb
      POSTGRES_USER: ${DB_USER:-daytrader}
      POSTGRES_PASSWORD: ${DB_PASSWORD:-changeme}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U daytrader -d daytraderdb"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    image: daytrader/backend:latest
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/daytraderdb
      DB_USER: ${DB_USER:-daytrader}
      DB_PASSWORD: ${DB_PASSWORD:-changeme}
      CORS_ORIGINS: ${CORS_ORIGINS:-http://localhost}
      JWT_ISSUER: ${JWT_ISSUER:-https://daytrader.example.com}
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy

  frontend:
    image: daytrader/frontend:latest
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  postgres_data:
```

Run with:
```bash
docker-compose -f docker-compose.prod.yml up -d
```

---

## Environment Variables for Production

### Backend Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | JDBC connection URL | `jdbc:postgresql://localhost:5432/daytraderdb` |
| `DB_USER` | Database username | `daytrader` |
| `DB_PASSWORD` | Database password | `daytrader` |
| `CORS_ORIGINS` | Allowed CORS origins (comma-separated) | `http://localhost:5173` |
| `JWT_ISSUER` | JWT token issuer | `https://daytrader.example.com` |
| `JWT_AUDIENCE` | JWT token audience | `daytrader-api` |

### JWT Keys

For production, you **must** replace the default JWT keys:

1. Generate new RSA key pair:
```bash
openssl genrsa -out privateKey.pem 2048
openssl rsa -in privateKey.pem -pubout -out publicKey.pem
```

2. Place keys in `src/main/resources/` or mount as volume in container

---

## Database Migration

### Flyway Automatic Migrations

Flyway runs automatically on application startup:

- **Configuration**: `quarkus.flyway.migrate-at-start=true`
- **Migration files**: `src/main/resources/db/migration/`
- **Naming convention**: `V{version}__{description}.sql`

Current migrations:
- `V1__create_schema.sql` - Creates all database tables
- `V2__seed_data.sql` - Seeds sample data (quotes, test accounts)

### Production Considerations

⚠️ **Important**: The `clean-at-start` option is disabled in production:
```properties
%prod.quarkus.flyway.clean-at-start=false
```

For production deployments:
1. Always backup the database before migrations
2. Review migration scripts before deployment
3. Test migrations on a staging environment first

---

## Security Considerations

### 1. Change Default JWT Keys

**Critical**: Replace the default JWT key pair before production deployment:

```bash
# Generate production keys
openssl genrsa -out privateKey.pem 2048
openssl rsa -in privateKey.pem -pubout -out publicKey.pem
```

Store private keys securely (e.g., Kubernetes secrets, HashiCorp Vault).

### 2. Restrict CORS Origins

Configure allowed origins for your production domain:

```bash
export CORS_ORIGINS=https://yourdomain.com
```

Never use wildcard (`*`) in production.

### 3. Use Environment Variables for Secrets

Never commit secrets to version control:

```bash
export DB_PASSWORD=<secure-password>
export JWT_ISSUER=https://yourdomain.com
```

Use secret management solutions:
- Kubernetes Secrets
- AWS Secrets Manager
- HashiCorp Vault

### 4. Enable HTTPS in Production

Configure TLS termination at load balancer or use Quarkus SSL:

```properties
# application.properties
%prod.quarkus.http.ssl.certificate.file=/path/to/cert.pem
%prod.quarkus.http.ssl.certificate.key-file=/path/to/key.pem
%prod.quarkus.http.insecure-requests=redirect
```

### 5. Additional Security Headers

Consider adding security headers via nginx or Quarkus:
- Content-Security-Policy
- X-Frame-Options
- X-Content-Type-Options
- Strict-Transport-Security

---

## Health Checks

The application provides health check endpoints for container orchestration:

### Endpoints

| Endpoint | Purpose | Response |
|----------|---------|----------|
| `/api/health` | Liveness probe | `{"status": "UP", "application": "daytrader-quarkus"}` |
| `/api/health/ready` | Readiness probe | `{"status": "READY", "checks": {...}}` |
| `/q/health` | SmallRye Health (standard) | MicroProfile Health format |
| `/q/health/live` | Liveness (SmallRye) | MicroProfile Health format |
| `/q/health/ready` | Readiness (SmallRye) | MicroProfile Health format |

### Kubernetes Configuration Example

```yaml
livenessProbe:
  httpGet:
    path: /api/health
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /api/health/ready
    port: 8080
  initialDelaySeconds: 5
  periodSeconds: 5
```

### Docker Compose Health Check

```yaml
backend:
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8080/api/health"]
    interval: 30s
    timeout: 10s
    retries: 3
    start_period: 40s
```

---

## Monitoring & Observability

### OpenAPI / Swagger

- OpenAPI spec: `http://localhost:8080/openapi`
- Swagger UI (dev only): `http://localhost:8080/swagger-ui`

### Logging

Configure log levels via environment:

```bash
export QUARKUS_LOG_LEVEL=INFO
export QUARKUS_LOG_CATEGORY__COM_IBM_WEBSPHERE_SAMPLES_DAYTRADER__LEVEL=DEBUG
```

---

## Quick Start Checklist

- [ ] Build backend: `./mvnw clean package`
- [ ] Build frontend: `npm run build`
- [ ] Configure database connection
- [ ] Generate production JWT keys
- [ ] Set CORS_ORIGINS for your domain
- [ ] Configure secrets via environment variables
- [ ] Enable HTTPS/TLS
- [ ] Configure health check probes
- [ ] Test database migrations on staging
- [ ] Deploy and verify health endpoints