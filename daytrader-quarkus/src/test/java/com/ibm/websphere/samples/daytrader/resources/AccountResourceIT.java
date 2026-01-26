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
import com.ibm.websphere.samples.daytrader.services.JWTService;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@DisplayName("AccountResource Integration Tests")
class AccountResourceIT {

    @Inject
    AuthService authService;

    @Inject
    JWTService jwtService;

    private static final String TEST_USER = "accountuser";
    private static final String TEST_PASSWORD = "password123";

    @BeforeEach
    @Transactional
    void setUp() {
        Account.deleteAll();
        AccountProfile.deleteAll();
    }

    @Transactional
    String createTestUserAndGetToken() {
        authService.register(TEST_USER, TEST_PASSWORD, "Account User", "123 Account St",
                "account@example.com", "1234-5678", new BigDecimal("10000"));
        return jwtService.generateToken(TEST_USER);
    }

    @Nested
    @DisplayName("GET /api/account")
    class GetAccountTests {

        @Test
        @DisplayName("should return 200 and account data for authenticated user")
        void getAccountSuccess() {
            String token = createTestUserAndGetToken();

            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get("/api/account")
            .then()
                .statusCode(200)
                .body("balance", equalTo(10000));
        }

        @Test
        @DisplayName("should return 401 without authentication")
        void getAccountUnauthorized() {
            given()
            .when()
                .get("/api/account")
            .then()
                .statusCode(401);
        }

        @Test
        @DisplayName("should return 401 with invalid token")
        void getAccountInvalidToken() {
            given()
                .header("Authorization", "Bearer invalid.token.here")
            .when()
                .get("/api/account")
            .then()
                .statusCode(401);
        }
    }

    @Nested
    @DisplayName("GET /api/account/profile")
    class GetProfileTests {

        @Test
        @DisplayName("should return 200 and profile data for authenticated user")
        void getProfileSuccess() {
            String token = createTestUserAndGetToken();

            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get("/api/account/profile")
            .then()
                .statusCode(200)
                .body("userID", equalTo(TEST_USER))
                .body("fullName", equalTo("Account User"))
                .body("email", equalTo("account@example.com"));
        }

        @Test
        @DisplayName("should return 401 without authentication")
        void getProfileUnauthorized() {
            given()
            .when()
                .get("/api/account/profile")
            .then()
                .statusCode(401);
        }
    }

    @Nested
    @DisplayName("PUT /api/account/profile")
    class UpdateProfileTests {

        @Test
        @DisplayName("should return 200 and updated profile")
        void updateProfileSuccess() {
            String token = createTestUserAndGetToken();

            String requestBody = """
                {
                    "password": "newpassword",
                    "fullName": "Updated Name",
                    "address": "999 Updated St",
                    "email": "updated@example.com",
                    "creditCard": "0000-1111"
                }
                """;

            given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(requestBody)
            .when()
                .put("/api/account/profile")
            .then()
                .statusCode(200)
                .body("fullName", equalTo("Updated Name"))
                .body("email", equalTo("updated@example.com"));
        }

        @Test
        @DisplayName("should return 401 without authentication")
        void updateProfileUnauthorized() {
            given()
                .contentType(ContentType.JSON)
                .body("{\"fullName\": \"New Name\"}")
            .when()
                .put("/api/account/profile")
            .then()
                .statusCode(401);
        }
    }
}

