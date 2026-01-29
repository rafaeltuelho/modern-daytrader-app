/**
 * (C) Copyright IBM Corporation 2024.
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
package com.ibm.websphere.samples.daytrader.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/**
 * Service for generating and managing JWT tokens
 * Per Phase 3: Backend Migration specification section 6.1
 */
@ApplicationScoped
public class JwtService {

    private static final Logger LOG = Logger.getLogger(JwtService.class);

    @ConfigProperty(name = "smallrye.jwt.new-token.issuer", defaultValue = "https://daytrader.example.com")
    String issuer;

    @ConfigProperty(name = "smallrye.jwt.new-token.lifespan", defaultValue = "3600")
    long tokenLifespan;

    /**
     * Generate a JWT token for the authenticated user
     * 
     * @param userID the user's ID (becomes the subject and upn claims)
     * @param roles the roles to assign to the user
     * @return signed JWT token string
     */
    public String generateToken(String userID, Set<String> roles) {
        LOG.debugf("Generating JWT token for user: %s with roles: %s", userID, roles);
        
        Instant now = Instant.now();
        Instant expiry = now.plus(Duration.ofSeconds(tokenLifespan));
        
        String token = Jwt.issuer(issuer)
                .subject(userID)
                .upn(userID)
                .groups(roles)
                .issuedAt(now)
                .expiresAt(expiry)
                .claim("daytrader_user", true)
                .sign();
        
        LOG.infof("JWT token generated for user: %s, expires at: %s", userID, expiry);
        return token;
    }

    /**
     * Generate a JWT token with the default "Trader" role
     * 
     * @param userID the user's ID
     * @return signed JWT token string
     */
    public String generateTraderToken(String userID) {
        return generateToken(userID, Set.of("Trader", "User"));
    }

    /**
     * Get the token lifespan in seconds
     * 
     * @return token lifespan in seconds
     */
    public long getTokenLifespan() {
        return tokenLifespan;
    }
}

