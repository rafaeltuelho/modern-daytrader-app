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
package com.ibm.websphere.samples.daytrader.util;

import io.smallrye.jwt.build.Jwt;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/**
 * Utility class for generating JWT tokens in tests
 */
public class TestJwtGenerator {

    private static final String ISSUER = "https://daytrader.example.com";

    /**
     * Generate a JWT token for testing with Trader and User roles
     */
    public static String generateToken(String userID) {
        return generateToken(userID, Set.of("Trader", "User"));
    }

    /**
     * Generate a JWT token with specific roles
     */
    public static String generateToken(String userID, Set<String> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plus(Duration.ofHours(1));
        
        return Jwt.issuer(ISSUER)
                .subject(userID)
                .upn(userID)
                .groups(roles)
                .issuedAt(now)
                .expiresAt(expiry)
                .claim("daytrader_user", true)
                .sign();
    }
}

