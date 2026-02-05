package com.daytrader.account.security;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtTokenService
 */
@QuarkusTest
class JwtTokenServiceTest {

    @Inject
    JwtTokenService jwtTokenService;

    @Inject
    JWTParser jwtParser;

    @Test
    void testGenerateTraderToken() throws ParseException {
        String userId = "testuser";
        String email = "test@example.com";
        String fullName = "Test User";

        String token = jwtTokenService.generateTraderToken(userId, email, fullName);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Parse and verify token
        JsonWebToken jwt = jwtParser.parse(token);
        assertEquals(userId, jwt.getSubject());
        assertEquals(email, jwt.getClaim("upn"));
        assertEquals(fullName, jwt.getClaim("name"));
        assertEquals(email, jwt.getClaim("email"));
        assertTrue(jwt.getGroups().contains("trader"));
        assertEquals("https://daytrader.example.com", jwt.getIssuer());
        assertTrue(jwt.getAudience().contains("daytrader-api"));
    }

    @Test
    void testGenerateAdminToken() throws ParseException {
        String userId = "adminuser";
        String email = "admin@example.com";
        String fullName = "Admin User";

        String token = jwtTokenService.generateAdminToken(userId, email, fullName);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Parse and verify token
        JsonWebToken jwt = jwtParser.parse(token);
        assertEquals(userId, jwt.getSubject());
        assertTrue(jwt.getGroups().contains("trader"));
        assertTrue(jwt.getGroups().contains("admin"));
    }

    @Test
    void testGenerateTokenWithCustomRoles() throws ParseException {
        String userId = "customuser";
        String email = "custom@example.com";
        String fullName = "Custom User";
        Set<String> roles = Set.of("trader", "viewer");

        String token = jwtTokenService.generateToken(userId, email, fullName, roles);

        assertNotNull(token);

        // Parse and verify token
        JsonWebToken jwt = jwtParser.parse(token);
        assertEquals(userId, jwt.getSubject());
        assertTrue(jwt.getGroups().contains("trader"));
        assertTrue(jwt.getGroups().contains("viewer"));
        assertFalse(jwt.getGroups().contains("admin"));
    }

    @Test
    void testTokenExpiration() throws ParseException {
        String token = jwtTokenService.generateTraderToken("user", "user@example.com", "User");

        JsonWebToken jwt = jwtParser.parse(token);
        
        assertNotNull(jwt.getIssuedAtTime());
        assertNotNull(jwt.getExpirationTime());
        
        // Token should expire after issued time
        assertTrue(jwt.getExpirationTime() > jwt.getIssuedAtTime());
        
        // Token should be valid for approximately 3600 seconds (1 hour)
        long lifespan = jwt.getExpirationTime() - jwt.getIssuedAtTime();
        assertTrue(lifespan >= 3590 && lifespan <= 3610, "Token lifespan should be around 3600 seconds");
    }

    @Test
    void testTokenIssuerAndAudience() throws ParseException {
        String token = jwtTokenService.generateTraderToken("user", "user@example.com", "User");

        JsonWebToken jwt = jwtParser.parse(token);
        
        assertEquals("https://daytrader.example.com", jwt.getIssuer());
        assertTrue(jwt.getAudience().contains("daytrader-api"));
    }
}

