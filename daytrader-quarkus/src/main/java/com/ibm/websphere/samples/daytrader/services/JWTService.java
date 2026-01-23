/**
 * (C) Copyright IBM Corporation 2015, 2025.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.websphere.samples.daytrader.services;

import java.util.Set;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Service for JWT token generation and management.
 * Uses SmallRye JWT for token creation with RSA signing.
 */
@ApplicationScoped
public class JWTService {

    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "https://daytrader.example.com")
    String issuer;

    @ConfigProperty(name = "mp.jwt.verify.audiences", defaultValue = "daytrader-api")
    String audience;

    @ConfigProperty(name = "daytrader.jwt.token.expiration", defaultValue = "3600")
    long tokenExpirationSeconds;

    /**
     * Generate a JWT token for the authenticated user.
     *
     * @param userID the user ID to include in the token
     * @return the generated JWT token string
     */
    public String generateToken(String userID) {
        return generateToken(userID, Set.of("user"));
    }

    /**
     * Generate a JWT token for the authenticated user with specific roles.
     *
     * @param userID the user ID to include in the token
     * @param roles  the roles to assign to the user
     * @return the generated JWT token string
     */
    public String generateToken(String userID, Set<String> roles) {
        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        long expirationTime = currentTimeSeconds + tokenExpirationSeconds;

        return Jwt.issuer(issuer)
                .audience(audience)
                .subject(userID)
                .upn(userID)
                .groups(roles)
                .issuedAt(currentTimeSeconds)
                .expiresAt(expirationTime)
                .claim("userId", userID)
                .sign();
    }

    /**
     * Get the token expiration time in seconds.
     *
     * @return token expiration time in seconds
     */
    public long getTokenExpirationSeconds() {
        return tokenExpirationSeconds;
    }
}

