# ADR-001: Simple JWT Authentication (No OIDC/Keycloak)

**Status**: Accepted  
**Date**: 2026-02-01  
**Decision Makers**: Project Owner  

## Context

The original architecture specification (Phase 01) called for OIDC/JWT authentication using Keycloak as the identity provider. This would provide:
- Full OAuth 2.0 / OpenID Connect support
- Centralized user management
- Token refresh flows
- Social login integration
- Multi-factor authentication

However, for the current phase of development, the full OIDC infrastructure adds complexity that is not immediately needed.

## Decision

**We will use simple JWT authentication without OIDC/Keycloak.**

The Account Service will:
1. Generate signed JWT tokens using SmallRye JWT
2. Use RSA key pairs (privateKey.pem / publicKey.pem) for signing and verification
3. Include user claims (subject, email, name, roles) in the token
4. Validate tokens using the public key

Other services (Trading, Market) will:
1. Validate incoming JWT tokens using the shared public key
2. Extract user identity from the token claims
3. Enforce role-based access control based on token groups

## Consequences

### Positive
- Simpler architecture with fewer moving parts
- Faster local development (no Keycloak container needed)
- Reduced resource consumption
- Easier to understand and debug
- Faster startup times

### Negative
- No centralized user management UI
- No built-in token refresh mechanism (tokens expire after 1 hour)
- No social login support
- Password management is application responsibility
- No multi-factor authentication

### Neutral
- Can migrate to OIDC/Keycloak in the future if needed
- JWT token format remains compatible with OIDC

## Implementation

### Dependencies Changed

**Before (OIDC)**:
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-oidc</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-test-keycloak-server</artifactId>
    <scope>test</scope>
</dependency>
```

**After (Simple JWT)**:
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-jwt</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-jwt-build</artifactId>
</dependency>
```

### Configuration Changed

**Before (OIDC)**:
```properties
quarkus.oidc.auth-server-url=http://localhost:8180/realms/daytrader
quarkus.oidc.client-id=daytrader-api
quarkus.oidc.credentials.secret=secret
```

**After (Simple JWT)**:
```properties
mp.jwt.verify.issuer=https://daytrader.example.com
smallrye.jwt.sign.key.location=privateKey.pem
smallrye.jwt.verify.key.location=publicKey.pem
mp.jwt.verify.audiences=daytrader-api
```

### Docker Compose Changed

Keycloak service removed from `docker/docker-compose.yml`.

### New Files Created

- `daytrader-account-service/src/main/java/com/daytrader/account/security/JwtTokenService.java`
- `daytrader-account-service/src/main/resources/privateKey.pem`
- `daytrader-account-service/src/main/resources/publicKey.pem`
- `daytrader-trading-service/src/main/resources/publicKey.pem`

## Future Considerations

If OIDC/Keycloak is needed in the future:
1. Add `quarkus-oidc` dependency back
2. Configure Keycloak auth server URL
3. Add Keycloak service to Docker Compose
4. Update application.properties with OIDC configuration
5. The JWT token format will remain compatible

## References

- [Quarkus SmallRye JWT Guide](https://quarkus.io/guides/security-jwt)
- [MicroProfile JWT RBAC](https://microprofile.io/project/eclipse/microprofile-jwt-auth)
- Original spec: `specs/phase-01-core-infrastructure.md`

