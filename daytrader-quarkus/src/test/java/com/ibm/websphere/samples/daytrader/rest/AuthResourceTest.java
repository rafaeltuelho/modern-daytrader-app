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
package com.ibm.websphere.samples.daytrader.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.math.BigDecimal;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ibm.websphere.samples.daytrader.repository.AccountProfileRepository;
import com.ibm.websphere.samples.daytrader.repository.AccountRepository;
import com.ibm.websphere.samples.daytrader.service.TradeService;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * AuthResource REST integration tests
 * Tests login success, invalid credentials, user not found, and logout endpoints
 * Per Phase 2: Feature Implementation - Core Trading Operations
 */
@QuarkusTest
class AuthResourceTest {

    @Inject
    TradeService tradeService;

    @Inject
    AccountRepository accountRepository;

    @Inject
    AccountProfileRepository accountProfileRepository;

    private String testUserID = "authtest" + System.currentTimeMillis();
    private String testPassword = "password123";

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up any existing test data
        accountRepository.findByProfileUserID(testUserID).ifPresent(account -> {
            accountRepository.delete(account);
        });
        accountProfileRepository.findByUserID(testUserID).ifPresent(profile -> {
            accountProfileRepository.delete(profile);
        });

        // Register a test user
        tradeService.register(
            testUserID,
            testPassword,
            "Auth Test User",
            "123 Auth St",
            "auth@example.com",
            "1234-5678-9012-3456",
            new BigDecimal("10000.00")
        );
    }

    @Test
    void testLoginSuccess() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userID\": \"" + testUserID + "\", \"password\": \"" + testPassword + "\"}")
            .when().post("/api/v1/auth/login")
            .then()
                .statusCode(200)
                .body("profileID", is(testUserID))
                .body("accountID", notNullValue())
                .body("balance", notNullValue())
                .body("loginCount", is(1))
                .body("lastLogin", notNullValue());
    }

    @Test
    void testLoginInvalidCredentials() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userID\": \"" + testUserID + "\", \"password\": \"wrongpassword\"}")
            .when().post("/api/v1/auth/login")
            .then()
                .statusCode(401)
                .body("message", notNullValue());
    }

    @Test
    void testLoginUserNotFound() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userID\": \"nonexistentuser\", \"password\": \"password\"}")
            .when().post("/api/v1/auth/login")
            .then()
                .statusCode(401)
                .body("message", notNullValue());
    }

    @Test
    void testLoginMissingUserID() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"password\": \"" + testPassword + "\"}")
            .when().post("/api/v1/auth/login")
            .then()
                .statusCode(400)
                .body("message", is("userID is required"));
    }

    @Test
    void testLoginMissingPassword() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userID\": \"" + testUserID + "\"}")
            .when().post("/api/v1/auth/login")
            .then()
                .statusCode(400)
                .body("message", is("password is required"));
    }

    @Test
    void testLogoutSuccess() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userID\": \"" + testUserID + "\"}")
            .when().post("/api/v1/auth/logout")
            .then()
                .statusCode(204);
    }

    @Test
    void testLogoutUserNotFound() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userID\": \"nonexistentuser\"}")
            .when().post("/api/v1/auth/logout")
            .then()
                .statusCode(404)
                .body("message", notNullValue());
    }

    @Test
    void testLogoutMissingUserID() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
            .when().post("/api/v1/auth/logout")
            .then()
                .statusCode(400)
                .body("message", is("userID is required"));
    }
}

