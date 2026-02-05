# Account Service Test Progress Report

**Date**: 2026-02-03
**Service**: DayTrader Account Service (Quarkus 3.17.4)
**Test Scope**: Unit and Integration Tests
**Status**: ⚠️ **PARTIALLY PASSING** - 31/43 tests passing (72.1% pass rate)

## Test Summary

| Test Class | Total | Passed | Failed | Pass Rate |
|------------|-------|--------|--------|-----------|
| AccountServiceTest | 13 | 11 | 2 | 84.6% |
| JwtTokenServiceTest | 5 | 5 | 0 | 100% |
| AuthResourceTest | 9 | 4 | 5 | 44.4% |
| AccountResourceTest | 11 | 6 | 5 | 54.5% |
| ProfileResourceTest | 5 | 5 | 0 | 100% |
| **TOTAL** | **43** | **31** | **12** | **72.1%** |

## Issues Resolved ✅

### 1. JWT Signing Key Error (21 test failures) - RESOLVED
- **Issue**: `JwtSignatureException: SRJWT05009: Signing key can not be created from the loaded content`
- **Root Cause**: Private key was in PKCS#1 format, SmallRye JWT requires PKCS#8
- **Solution**: Converted key using `openssl pkcs8 -topk8` and updated test configuration
- **Files Changed**: 
  - Created `src/test/resources/privateKey-pkcs8.pem`
  - Updated `src/test/resources/application.properties`

### 2. User ID Masking Bug (10 test failures) - RESOLVED
- **Issue**: All user IDs were being masked like credit cards (e.g., `****-****-****-9660`)
- **Root Cause**: MapStruct bug - when using `expression` for one field, it applied `maskCreditCard()` to ALL fields
- **Solution**: Implemented all mapper methods as default methods instead of using @Mapping annotations
- **Files Changed**: `src/main/java/com/daytrader/account/mapper/AccountMapper.java`

## Remaining Failures (12 tests) ❌

### 1. Missing Exception Mappers (9 failures) - **CRITICAL**

**Root Cause**: No JAX-RS ExceptionMapper implementations found in the codebase

**Impact**: All exceptions return HTTP 500 instead of appropriate status codes

**Affected Tests**:

#### AccountResourceTest (3 failures):
- `testGetAccount_NotFound`: Expected 404 but got 500
  - `ResourceNotFoundException` should map to 404 NOT_FOUND
- `testRegisterAccount_UserExists`: Expected 400 but got 500
  - `BusinessException` with USER_EXISTS should map to 400 BAD_REQUEST
- `testRegisterAccount_EmailExists`: Expected 400 but got 500
  - `BusinessException` with EMAIL_EXISTS should map to 400 BAD_REQUEST

#### AuthResourceTest (4 failures):
- `testLogin_InvalidCredentials_WrongUser`: Expected 400 but got 500
  - `BusinessException` with INVALID_CREDENTIALS should map to 400 BAD_REQUEST
- `testLogin_InvalidCredentials_EmptyPassword`: Expected 400 but got 500
  - Validation error should map to 400 BAD_REQUEST
- `testLogin_MissingUserId`: Expected 400 but got 500
  - Validation error should map to 400 BAD_REQUEST
- `testLogin_MissingPassword`: Expected 400 but got 500
  - Validation error should map to 400 BAD_REQUEST

**Required Implementation** (for software-engineer):
```java
// Need to create these exception mappers:
// 1. BusinessExceptionMapper - maps BusinessException to 400 with error code
// 2. ResourceNotFoundExceptionMapper - maps ResourceNotFoundException to 404
// 3. ConstraintViolationExceptionMapper - maps validation errors to 400
```

---

### 2. Missing Authentication Check (1 failure - AuthResourceTest)

**Test**: `testLogout_NoToken`
- Expected: 401 UNAUTHORIZED
- Actual: 204 NO_CONTENT

**Root Cause**: The `/api/auth/logout` endpoint is not annotated with `@RolesAllowed` or similar security annotation

**Current Behavior**: Endpoint allows anonymous access and returns success even without authentication

**Required Fix** (for software-engineer):
- Add `@RolesAllowed({"trader", "admin"})` annotation to logout method
- OR check if JWT subject is null and return 401

---

### 3. Jakarta Validation Not Enforced (2 failures - AccountResourceTest)

**Tests**:
- `testRegisterAccount_ValidationError_InvalidEmail`: Expected 400 but got 201
- `testRegisterAccount_ValidationError_ShortPassword`: Expected 400 but got 201

**Root Cause**: Jakarta Validation annotations on `RegisterAccountRequest` DTO are not being enforced

**Investigation Needed**:
- Verify `@Valid` annotation is present on REST endpoint parameters
- Check if `quarkus-hibernate-validator` dependency is included
- Verify validation annotations on `RegisterAccountRequest` DTO

---

### 4. Error Message Format Mismatch (2 failures - AccountServiceTest)

**Tests**:
- `testRegister_UserExists`: Expected `USER_EXISTS` but got `User ID 'testuser-xxx' is already registered`
- `testRegister_EmailExists`: Expected `EMAIL_EXISTS` but got `Email 'test@example.com' is already registered`

**Root Cause**: Tests expect error codes but service returns full descriptive error messages

**Options**:
1. Update tests to expect full error messages (recommended for better error reporting)
2. Update service to return error codes only (less user-friendly)

## Next Steps - Action Required

### For software-engineer:

**Priority 1 - CRITICAL** (9 test failures):
1. **Implement Exception Mappers**:
   - Create `BusinessExceptionMapper` to map `BusinessException` → 400 BAD_REQUEST
   - Create `ResourceNotFoundExceptionMapper` to map `ResourceNotFoundException` → 404 NOT_FOUND
   - Create `ConstraintViolationExceptionMapper` to map validation errors → 400 BAD_REQUEST
   - Location: `daytrader-account-service/src/main/java/com/daytrader/account/exception/`

**Priority 2 - HIGH** (1 test failure):
2. **Add Authentication to Logout Endpoint**:
   - Add `@RolesAllowed({"trader", "admin"})` to `AuthResource.logout()` method
   - Ensure endpoint returns 401 when no valid JWT token is provided

**Priority 3 - MEDIUM** (2 test failures):
3. **Investigate Jakarta Validation**:
   - Verify `@Valid` annotation is on `AccountResource.registerAccount()` parameter
   - Check if validation annotations exist on `RegisterAccountRequest` DTO
   - Ensure `quarkus-hibernate-validator` dependency is present

**Priority 4 - LOW** (2 test failures):
4. **Align Error Messages**:
   - Option A: Update tests to expect full error messages (recommended)
   - Option B: Update service to return error codes only

### For qa-engineer (after fixes):

1. Re-run all tests after software-engineer implements fixes
2. Verify all 43 tests pass
3. Generate final test report with 100% pass rate
4. Perform exploratory testing of edge cases

## Test Environment

- **Java Version**: 25.0.1 (Eclipse Adoptium)
- **Quarkus Version**: 3.17.4
- **Database**: PostgreSQL 15 (Testcontainers)
- **Test Framework**: JUnit 5, REST-assured
- **Special Flags**: `-Dnet.bytebuddy.experimental=true` (for Java 25 compatibility)

## Test Execution Command

```bash
mvn test -pl daytrader-account-service -Dnet.bytebuddy.experimental=true
```

---

## Summary

### Test Coverage Created

✅ **43 comprehensive tests** created across 5 test classes:
- **Unit Tests** (18 tests): AccountServiceTest, JwtTokenServiceTest
- **Integration Tests** (25 tests): AuthResourceTest, AccountResourceTest, ProfileResourceTest

### Issues Resolved During Testing

1. ✅ JWT signing key format issue (PKCS#1 → PKCS#8 conversion)
2. ✅ MapStruct code generation bug (user ID masking)
3. ✅ Test configuration for Testcontainers and PostgreSQL
4. ✅ ByteBuddy Java 25 compatibility

### Critical Findings

🔴 **Missing Exception Mappers** - The service lacks proper exception handling infrastructure, causing all exceptions to return HTTP 500. This is a critical gap that affects API usability and error reporting.

🟡 **Missing Authentication on Logout** - The logout endpoint allows anonymous access, which is a security concern.

🟡 **Jakarta Validation Not Working** - Validation annotations are not being enforced, allowing invalid data to be processed.

### Test Pass Rate Progress

- **Initial**: 0/43 (0%) - Compilation errors
- **After JWT fix**: 20/43 (46.5%) - JWT signing resolved
- **After mapper fix**: 31/43 (72.1%) - User ID masking resolved
- **Target**: 43/43 (100%) - After software-engineer implements fixes

---

**Report Generated**: 2026-02-03 13:20:00
**Agent**: qa-engineer
**Status**: Waiting for software-engineer to implement exception mappers and security fixes
**Next Action**: Re-run tests after fixes are applied

