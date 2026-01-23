# DayTrader Development Guide

This guide covers setting up your local development environment for the DayTrader application.

## Prerequisites

### Java 21

The backend requires Java 21 or higher.

**macOS (using Homebrew):**
```bash
brew install openjdk@21
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

**Windows:**
Download and install from [Adoptium](https://adoptium.net/) or use:
```powershell
winget install EclipseAdoptium.Temurin.21.JDK
```

Verify installation:
```bash
java -version
```

### Node.js 18+

The frontend requires Node.js 18 or higher.

**macOS:**
```bash
brew install node@18
```

**Linux:**
```bash
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs
```

**Windows:**
```powershell
winget install OpenJS.NodeJS.LTS
```

Verify installation:
```bash
node --version
npm --version
```

### Docker (Optional)

Docker is optional but useful for running PostgreSQL manually.

Install from [Docker Desktop](https://www.docker.com/products/docker-desktop/) or via package manager.

---

## Database Setup

### Option 1: Docker Compose

Start PostgreSQL manually with Docker Compose:

```bash
cd daytrader-quarkus
docker-compose up -d
```

**Database Connection Details:**
| Property | Value |
|----------|-------|
| Host | localhost |
| Port | 5432 |
| Database | daytraderdb |
| Username | daytrader |
| Password | daytrader |

**Optional:** Start pgAdmin for database management:
```bash
docker-compose --profile admin up -d
# Access at http://localhost:5050
# Login: admin@daytrader.local / admin
```

**Stop/Reset:**
```bash
docker-compose down      # Stop containers
docker-compose down -v   # Stop and remove all data
```

### Option 2: Quarkus Dev Services (Recommended)

Quarkus automatically starts a PostgreSQL container in dev mode. No manual setup required!

Simply run the backend and Quarkus handles everything:
```bash
cd daytrader-quarkus
./mvnw quarkus:dev
```

Dev Services uses the same credentials as Docker Compose.

---

## Running the Backend

```bash
cd daytrader-quarkus
./mvnw quarkus:dev
```

**Available endpoints:**
- **API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui
- **OpenAPI Spec:** http://localhost:8080/openapi
- **Health Check:** http://localhost:8080/q/health
- **Health UI:** http://localhost:8080/q/health-ui

**Test Accounts (pre-seeded):**
| UserID | Password | Balance |
|--------|----------|---------|
| uid:0 | xxx | $100,000 |
| uid:1 | xxx | $100,000 |
| uid:2 | xxx | $100,000 |

---

## Running the Frontend

```bash
cd daytrader-frontend
npm install
npm run dev
```

**Available at:** http://localhost:5173

**Other commands:**
```bash
npm run build    # Production build
npm run preview  # Preview production build
npm run lint     # Run ESLint
```

---

## Development Workflow

### Hot Reload

Both backend and frontend support hot reload:
- **Backend:** Quarkus Dev Mode automatically recompiles on file changes
- **Frontend:** Vite HMR updates the browser instantly

### Running Tests

**Backend tests:**
```bash
cd daytrader-quarkus
./mvnw test                    # Unit tests
./mvnw verify                  # Integration tests
./mvnw verify -Pnative         # Native image tests
```

**Frontend tests:**
```bash
cd daytrader-frontend
npm run lint                   # Linting
```

### Code Formatting

**Backend:** Uses standard Java formatting. Configure your IDE for Java 21.

**Frontend:** Uses ESLint with TypeScript rules:
```bash
npm run lint
```

---

## Environment Variables

### Database Configuration (Production)

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | JDBC connection URL | `jdbc:postgresql://localhost:5432/daytraderdb` |
| `DB_USER` | Database username | `daytrader` |
| `DB_PASSWORD` | Database password | `daytrader` |

### JWT Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_ISSUER` | JWT token issuer | `https://daytrader.example.com` |
| `JWT_AUDIENCE` | JWT token audience | `daytrader-api` |

Token expiration is configured via `daytrader.jwt.token.expiration` (default: 3600 seconds / 1 hour).

### CORS Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `CORS_ORIGINS` | Allowed origins (comma-separated) | `http://localhost:5173` |

Dev mode automatically allows `http://localhost:5173` and `http://localhost:3000`.

---

## Troubleshooting

### Port Already in Use

**Backend (8080):**
```bash
lsof -i :8080
kill -9 <PID>
```

**Frontend (5173):**
```bash
lsof -i :5173
kill -9 <PID>
```

### Database Connection Failed

1. **Using Docker Compose:** Ensure containers are running:
   ```bash
   docker-compose ps
   docker-compose logs postgres
   ```

2. **Using Dev Services:** Check if Docker is running. Quarkus Dev Services requires Docker.

3. **Port conflict:** If port 5432 is in use, stop other PostgreSQL instances.

### Maven Wrapper Permission Denied

```bash
chmod +x daytrader-quarkus/mvnw
```

### Node Modules Issues

```bash
cd daytrader-frontend
rm -rf node_modules package-lock.json
npm install
```

### JWT Authentication Errors

- Ensure you're using a valid token from `/api/auth/login`
- Tokens expire after 1 hour by default
- Include token in Authorization header: `Bearer <token>`

### CORS Errors in Browser

- Ensure the backend is running on port 8080
- Check that your frontend origin is in the allowed CORS origins
- Clear browser cache and try again

### Quarkus Dev Services Not Starting

1. Ensure Docker is running
2. Check for port conflicts on 5432
3. Try stopping and removing existing containers:
   ```bash
   docker stop daytrader-postgres 2>/dev/null
   docker rm daytrader-postgres 2>/dev/null
   ```

---

## Additional Resources

- [Quarkus Documentation](https://quarkus.io/guides/)
- [Vite Documentation](https://vitejs.dev/guide/)
- [React Documentation](https://react.dev/)
- [TanStack Query](https://tanstack.com/query/latest)

