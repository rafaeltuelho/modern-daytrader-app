package com.daytrader.account.resource;

import com.daytrader.account.entity.Account;
import com.daytrader.account.entity.AccountProfile;
import com.daytrader.account.repository.AccountProfileRepository;
import com.daytrader.account.repository.AccountRepository;
import com.daytrader.account.security.JwtTokenService;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

/**
 * Integration tests for ProfileResource
 */
@QuarkusTest
class ProfileResourceTest {

    @Inject
    AccountRepository accountRepository;

    @Inject
    AccountProfileRepository profileRepository;

    @Inject
    JwtTokenService jwtTokenService;

    private String testUserId;
    private String testToken;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up any existing test data
        cleanupTestData();

        // Create a test user
        testUserId = "profiletest-" + System.currentTimeMillis();

        AccountProfile profile = new AccountProfile();
        profile.userId = testUserId;
        profile.passwordHash = "hashedpassword";
        profile.fullName = "Profile Test User";
        profile.email = "profiletest@example.com";
        profile.address = "123 Profile St";
        profile.creditCard = "1234-5678-9012-3456";
        profileRepository.persist(profile);

        Account account = new Account();
        account.profile = profile;
        account.balance = new BigDecimal("10000.00");
        account.openBalance = new BigDecimal("10000.00");
        account.loginCount = 0;
        account.logoutCount = 0;
        accountRepository.persist(account);

        // Generate a test token
        testToken = jwtTokenService.generateTraderToken(testUserId, "profiletest@example.com", "Profile Test User");
    }

    @AfterEach
    @Transactional
    void tearDown() {
        cleanupTestData();
    }

    @Transactional
    void cleanupTestData() {
        accountRepository.deleteAll();
        profileRepository.deleteAll();
    }

    @Test
    void testGetCurrentProfile_Success() {
        given()
            .header("Authorization", "Bearer " + testToken)
        .when()
            .get("/api/profiles/me")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("userId", equalTo(testUserId))
            .body("fullName", equalTo("Profile Test User"))
            .body("email", equalTo("profiletest@example.com"))
            .body("address", equalTo("123 Profile St"))
            .body("creditCard", containsString("*"))  // Should be masked
            .body("creditCard", not(containsString("1234-5678-9012-3456")))  // Original should not be visible
            .body("createdAt", notNullValue())
            .body("updatedAt", notNullValue());
    }

    @Test
    void testGetCurrentProfile_NoToken() {
        given()
        .when()
            .get("/api/profiles/me")
        .then()
            .statusCode(401);  // Unauthorized
    }

    @Test
    void testGetCurrentProfile_InvalidToken() {
        given()
            .header("Authorization", "Bearer invalid.token.here")
        .when()
            .get("/api/profiles/me")
        .then()
            .statusCode(401);  // Unauthorized
    }

    @Test
    void testGetCurrentProfile_MalformedAuthHeader() {
        given()
            .header("Authorization", "InvalidFormat")
        .when()
            .get("/api/profiles/me")
        .then()
            .statusCode(401);  // Unauthorized
    }

    @Test
    void testGetCurrentProfile_CreditCardMasking() {
        given()
            .header("Authorization", "Bearer " + testToken)
        .when()
            .get("/api/profiles/me")
        .then()
            .statusCode(200)
            .body("creditCard", containsString("*"));  // Should contain asterisks for masking
    }
}

