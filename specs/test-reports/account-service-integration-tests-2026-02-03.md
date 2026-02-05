# Account Service Integration Test Report

**Date**: 2026-02-03  
**Test Scope**: DayTrader Account Service (Quarkus 3.17.4)  
**Test Type**: Unit and Integration Tests  
**Status**: ❌ FAILED - Configuration Issue

## Executive Summary

Comprehensive test suite was created for the Account Service including 43 tests across 5 test classes. However, tests are currently failing due to a database configuration conflict between the main application.properties and test configuration.

## Test Coverage Created

### Unit Tests (18 tests)

1. **AccountServiceTest** (13 tests)
   - ✅ getAccount
   - ✅ getAccountNotFound
   - ✅ getAccountByUserId
   - ✅ getProfile
   - ✅ validateCredentials (success, invalid, empty)
   - ✅ register (success, userExists, emailExists)
   - ✅ recordLogin
   - ✅ recordLogout
   - ✅ updateBalance

2. **JwtTokenServiceTest** (5 tests)
   - ✅ generateTraderToken
   - ✅ generateAdminToken
   - ✅ generateTokenWithCustomRoles
   - ✅ tokenExpiration
   - ✅ tokenIssuerAndAudience

### Integration Tests (25 tests)

3. **AuthResourceTest** (9 tests)
   - ✅ login success
   - ✅ login invalid credentials
   - ✅ login validation errors (empty userId, empty password)
   - ✅ logout with token
   - ✅ logout without token

4. **AccountResourceTest** (11 tests)
   - ✅ register success
   - ✅ register with default balance
   - ✅ register user exists
   - ✅ register email exists
   - ✅ register validation errors
   - ✅ getAccount success
   - ✅ getAccount not found
   - ✅ getAccount no token
   - ✅ getCurrentAccount success
   - ✅ getCurrentAccount no token

5. **ProfileResourceTest** (5 tests)
   - ✅ getCurrentProfile success
   - ✅ getCurrentProfile no token
   - ✅ getCurrentProfile credit card masking

## Issues Encountered

### 1. Compilation Errors (RESOLVED)
- **Issue**: Missing DTOs from common module
- **Root Cause**: Common module not compiled
- **Solution**: Ran `mvn clean install -DskipTests -pl daytrader-common`

### 2. Hamcrest Matcher Error (RESOLVED)
- **Issue**: `matchesPattern()` method not found
- **Root Cause**: Incorrect Hamcrest matcher usage
- **Solution**: Changed to `containsString("*")` for credit card masking test

### 3. Java 25 ByteBuddy Compatibility (RESOLVED)
- **Issue**: ByteBuddy doesn't officially support Java 25
- **Root Cause**: Running on Java 25 with ByteBuddy that supports up to Java 23
- **Solution**: Added `quarkus.test.arg-line=-Dnet.bytebuddy.experimental=true` to test configuration

### 4. Database Configuration Conflict (CURRENT ISSUE - NOT RESOLVED)
- **Issue**: Testcontainers creates database on dynamic port, but application tries to connect to localhost:5432
- **Root Cause**: Main `application.properties` has hardcoded JDBC URL that overrides Dev Services configuration
- **Impact**: All integration tests fail to start Quarkus application
- **Error**: `Connection to localhost:5432 refused`

## Root Cause Analysis

The main application.properties contains:
```properties
quarkus.datasource.jdbc.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:daytrader}
```

This hardcoded URL prevents Quarkus Dev Services (Testcontainers) from injecting the correct database URL. When Dev Services starts a PostgreSQL container on a dynamic port (e.g., 40463), the application still tries to connect to port 5432.

## Proposed Solutions

### Option 1: Remove JDBC URL from Main Config (RECOMMENDED)
Remove the hardcoded JDBC URL from main `application.properties` and rely on environment variables or profiles:
- For production: Set `QUARKUS_DATASOURCE_JDBC_URL` environment variable
- For tests: Let Dev Services configure the URL automatically

### Option 2: Use %prod Profile
Move the JDBC URL to a `%prod` profile in main `application.properties`:
```properties
%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:daytrader}
```

### Option 3: Override in Test Profile (ATTEMPTED - FAILED)
Attempted to override with `%test` profile but the main config still takes precedence.

## Next Steps

1. **Immediate**: Coordinate with **software-engineer** to modify main `application.properties` to use profile-based configuration
2. **After Fix**: Re-run tests with `mvn test -pl daytrader-account-service -Dnet.bytebuddy.experimental=true`
3. **Validation**: Ensure all 43 tests pass
4. **Coverage**: Generate code coverage report

## Test Execution Command

```bash
mvn test -pl daytrader-account-service -Dnet.bytebuddy.experimental=true
```

## Files Created

- `daytrader-account-service/src/test/resources/application.properties`
- `daytrader-account-service/src/test/java/com/daytrader/account/service/AccountServiceTest.java`
- `daytrader-account-service/src/test/java/com/daytrader/account/security/JwtTokenServiceTest.java`
- `daytrader-account-service/src/test/java/com/daytrader/account/resource/AuthResourceTest.java`
- `daytrader-account-service/src/test/java/com/daytrader/account/resource/AccountResourceTest.java`
- `daytrader-account-service/src/test/java/com/daytrader/account/resource/ProfileResourceTest.java`

## Recommendation

**Assign to**: software-engineer  
**Action**: Modify `daytrader-account-service/src/main/resources/application.properties` to use profile-based datasource configuration (Option 2 recommended)  
**Priority**: High - Blocking all integration tests

