package com.ibm.websphere.samples.daytrader.resources;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.ibm.websphere.samples.daytrader.entities.Account;
import com.ibm.websphere.samples.daytrader.entities.AccountProfile;
import com.ibm.websphere.samples.daytrader.services.AuthService;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@DisplayName("AuthResource Integration Tests")
class AuthResourceIT {

    @Inject
    AuthService authService;

    @BeforeEach
    @Transactional
    void setUp() {
        Account.deleteAll();
        AccountProfile.deleteAll();
    }

    @Transactional
    void createTestUser(String userID, String password) {
        authService.register(userID, password, "Test User", "123 Test St", 
                "test@example.com", "1234-5678", new BigDecimal("10000"));
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginEndpointTests {

        @Test
        @DisplayName("should return 200 and token on successful login")
        void loginSuccess() {
            createTestUser("testuser", "password123");

            given()
                .contentType(ContentType.JSON)
                .body("{\"userID\": \"testuser\", \"password\": \"password123\"}")
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(200)
                .body("userID", equalTo("testuser"))
                .body("token", notNullValue())
                .body("expiresIn", greaterThan(0));
        }

        @Test
        @DisplayName("should return 401 on invalid password")
        void loginInvalidPassword() {
            createTestUser("testuser", "password123");

            given()
                .contentType(ContentType.JSON)
                .body("{\"userID\": \"testuser\", \"password\": \"wrongpassword\"}")
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(401)
                .body("error", equalTo("Invalid credentials"));
        }

        @Test
        @DisplayName("should return 401 when user does not exist")
        void loginUserNotFound() {
            given()
                .contentType(ContentType.JSON)
                .body("{\"userID\": \"nonexistent\", \"password\": \"password\"}")
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(401)
                .body("error", equalTo("User not found"));
        }

        @Test
        @DisplayName("should return 400 on empty body")
        void loginEmptyBody() {
            given()
                .contentType(ContentType.JSON)
                .body("{}")
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterEndpointTests {

        @Test
        @DisplayName("should return 201 on successful registration")
        void registerSuccess() {
            String requestBody = """
                {
                    "userID": "newuser",
                    "password": "password123",
                    "fullName": "New User",
                    "address": "456 New St",
                    "email": "new@example.com",
                    "creditCard": "9999-0000",
                    "openBalance": 5000
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
            .when()
                .post("/api/auth/register")
            .then()
                .statusCode(201)
                .body("balance", equalTo(5000));
        }

        @Test
        @DisplayName("should return 409 when user already exists")
        void registerDuplicateUser() {
            createTestUser("existinguser", "password");

            String requestBody = """
                {
                    "userID": "existinguser",
                    "password": "password123",
                    "fullName": "Another User",
                    "openBalance": 1000
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
            .when()
                .post("/api/auth/register")
            .then()
                .statusCode(409);
        }

        @Test
        @DisplayName("should return 400 when required fields are missing")
        void registerMissingFields() {
            given()
                .contentType(ContentType.JSON)
                .body("{\"fullName\": \"Test\"}")
            .when()
                .post("/api/auth/register")
            .then()
                .statusCode(400);
        }
    }
}

