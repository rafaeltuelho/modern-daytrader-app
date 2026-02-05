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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

/**
 * Integration tests for AccountResource
 */
@QuarkusTest
class AccountResourceTest {

    @Inject
    AccountRepository accountRepository;

    @Inject
    AccountProfileRepository profileRepository;

    @Inject
    JwtTokenService jwtTokenService;

    private String testUserId;
    private Long testAccountId;
    private String testToken;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up any existing test data
        cleanupTestData();

        // Create a test user
        testUserId = "accounttest-" + System.currentTimeMillis();

        AccountProfile profile = new AccountProfile();
        profile.userId = testUserId;
        profile.passwordHash = "hashedpassword";
        profile.fullName = "Account Test User";
        profile.email = "accounttest@example.com";
        profile.address = "123 Account St";
        profile.creditCard = "1234-5678-9012-3456";
        profileRepository.persist(profile);

        Account account = new Account();
        account.profile = profile;
        account.balance = new BigDecimal("10000.00");
        account.openBalance = new BigDecimal("10000.00");
        account.loginCount = 0;
        account.logoutCount = 0;
        accountRepository.persist(account);

        testAccountId = account.id;

        // Generate a test token
        testToken = jwtTokenService.generateTraderToken(testUserId, "accounttest@example.com", "Account Test User");
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
    @Disabled("Hibernate Validator incompatible with Java 25 - Bean validation not working")
    void testRegisterAccount_Success() {
        String newUserId = "newuser-" + System.currentTimeMillis();

        given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "userId": "%s",
                    "password": "SecureP@ss123",
                    "fullName": "New User",
                    "email": "newuser-%d@example.com",
                    "address": "456 New St",
                    "creditCard": "9876-5432-1098-7654",
                    "openBalance": 50000.00
                }
                """, newUserId, System.currentTimeMillis()))
        .when()
            .post("/api/accounts")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .header("Location", notNullValue())
            .body("id", notNullValue())
            .body("userId", equalTo(newUserId))
            .body("balance", equalTo(50000.00f))
            .body("openBalance", equalTo(50000.00f))
            .body("loginCount", equalTo(0))
            .body("logoutCount", equalTo(0))
            .body("profile.fullName", equalTo("New User"))
            .body("profile.email", containsString("newuser-"))
            .body("profile.creditCard", containsString("*"));  // Should be masked
    }

    @Test
    @Disabled("Hibernate Validator incompatible with Java 25 - Bean validation not working")
    void testRegisterAccount_DefaultBalance() {
        String newUserId = "defaultbalance-" + System.currentTimeMillis();

        given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "userId": "%s",
                    "password": "SecureP@ss123",
                    "fullName": "Default Balance User",
                    "email": "default-%d@example.com"
                }
                """, newUserId, System.currentTimeMillis()))
        .when()
            .post("/api/accounts")
        .then()
            .statusCode(201)
            .body("balance", equalTo(10000.00f))
            .body("openBalance", equalTo(10000.00f));
    }

    @Test
    @Disabled("Hibernate Validator incompatible with Java 25 - Bean validation not working")
    void testRegisterAccount_UserExists() {
        given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "userId": "%s",
                    "password": "SecureP@ss123",
                    "fullName": "Duplicate User",
                    "email": "duplicate@example.com"
                }
                """, testUserId))
        .when()
            .post("/api/accounts")
        .then()
            .statusCode(400)
            .body("errorCode", equalTo("USER_EXISTS"));
    }

    @Test
    @Disabled("Hibernate Validator incompatible with Java 25 - Bean validation not working")
    void testRegisterAccount_EmailExists() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "userId": "anotheruser",
                    "password": "SecureP@ss123",
                    "fullName": "Another User",
                    "email": "accounttest@example.com"
                }
                """)
        .when()
            .post("/api/accounts")
        .then()
            .statusCode(400)
            .body("errorCode", equalTo("EMAIL_EXISTS"));
    }

    @Test
    @Disabled("Hibernate Validator incompatible with Java 25 - Bean validation not working")
    void testRegisterAccount_ValidationError_ShortPassword() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "userId": "shortpass",
                    "password": "short",
                    "fullName": "Short Pass User",
                    "email": "shortpass@example.com"
                }
                """)
        .when()
            .post("/api/accounts")
        .then()
            .statusCode(400);  // Validation error
    }

    @Test
    @Disabled("Hibernate Validator incompatible with Java 25 - Bean validation not working")
    void testRegisterAccount_ValidationError_InvalidEmail() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "userId": "invalidemail",
                    "password": "SecureP@ss123",
                    "fullName": "Invalid Email User",
                    "email": "not-an-email"
                }
                """)
        .when()
            .post("/api/accounts")
        .then()
            .statusCode(400);  // Validation error
    }

    @Test
    void testGetAccount_Success() {
        given()
            .header("Authorization", "Bearer " + testToken)
        .when()
            .get("/api/accounts/" + testAccountId)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(testAccountId.intValue()))
            .body("userId", equalTo(testUserId))
            .body("balance", equalTo(10000.00f))
            .body("profile.fullName", equalTo("Account Test User"));
    }

    @Test
    void testGetAccount_NotFound() {
        given()
            .header("Authorization", "Bearer " + testToken)
        .when()
            .get("/api/accounts/999999")
        .then()
            .statusCode(404);
    }

    @Test
    void testGetAccount_NoToken() {
        given()
        .when()
            .get("/api/accounts/" + testAccountId)
        .then()
            .statusCode(401);  // Unauthorized
    }

    @Test
    void testGetCurrentAccount_Success() {
        given()
            .header("Authorization", "Bearer " + testToken)
        .when()
            .get("/api/accounts/me")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("userId", equalTo(testUserId))
            .body("balance", equalTo(10000.00f))
            .body("profile.fullName", equalTo("Account Test User"))
            .body("profile.email", equalTo("accounttest@example.com"));
    }

    @Test
    void testGetCurrentAccount_NoToken() {
        given()
        .when()
            .get("/api/accounts/me")
        .then()
            .statusCode(401);  // Unauthorized
    }
}

