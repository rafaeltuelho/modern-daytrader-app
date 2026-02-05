package com.daytrader.account.security;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/**
 * Service for generating and managing JWT tokens.
 * Uses SmallRye JWT for simple token-based authentication (no OIDC/Keycloak).
 */
@ApplicationScoped
public class JwtTokenService {

    private static final Logger LOG = Logger.getLogger(JwtTokenService.class);

    @ConfigProperty(name = "smallrye.jwt.new-token.issuer", defaultValue = "https://daytrader.example.com")
    String issuer;

    @ConfigProperty(name = "smallrye.jwt.new-token.lifespan", defaultValue = "3600")
    long tokenLifespanSeconds;

    /**
     * Generate a JWT token for the authenticated user.
     *
     * @param userId   The user's unique identifier
     * @param email    The user's email address
     * @param fullName The user's full name
     * @param roles    The user's roles (e.g., "trader", "admin")
     * @return The signed JWT token string
     */
    public String generateToken(String userId, String email, String fullName, Set<String> roles) {
        LOG.infof("Generating JWT token for user: %s", userId);

        Instant now = Instant.now();
        Instant expiry = now.plus(Duration.ofSeconds(tokenLifespanSeconds));

        String token = Jwt.issuer(issuer)
                .subject(userId)
                .upn(email)
                .claim("name", fullName)
                .claim("email", email)
                .groups(roles)
                .issuedAt(now)
                .expiresAt(expiry)
                .audience("daytrader-api")
                .sign();

        LOG.debugf("Token generated for user %s, expires at %s", userId, expiry);
        return token;
    }

    /**
     * Generate a token with default trader role.
     */
    public String generateTraderToken(String userId, String email, String fullName) {
        return generateToken(userId, email, fullName, Set.of("trader"));
    }

    /**
     * Generate a token with admin role.
     */
    public String generateAdminToken(String userId, String email, String fullName) {
        return generateToken(userId, email, fullName, Set.of("trader", "admin"));
    }
}

