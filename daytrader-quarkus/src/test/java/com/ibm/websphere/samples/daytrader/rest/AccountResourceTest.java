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

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * AccountResource REST integration tests using REST-assured
 * Tests POST /api/v1/accounts and GET /api/v1/accounts/{id}
 * Per Phase 3: Backend Migration specification section 9 - Testing Strategy
 */
@QuarkusTest
class AccountResourceTest {

    @Inject
    AccountRepository accountRepository;

    @Inject
    AccountProfileRepository accountProfileRepository;

    private String testUserID = "resttest";

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
    }

    @Test
    void testRegisterAccount() {
        String requestBody = """
            {
                "userID": "%s",
                "password": "password123",
                "fullName": "REST Test User",
                "address": "123 REST St",
                "email": "rest@example.com",
                "creditCard": "1234-5678-9012-3456",
                "openBalance": 10000.00
            }
            """.formatted(testUserID);

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when().post("/api/v1/accounts")
            .then()
                .statusCode(201)
                .body("accountID", notNullValue())
                .body("profileID", is(testUserID))
                .body("balance", is(10000.00f))
                .body("openBalance", is(10000.00f))
                .body("loginCount", is(0))
                .body("logoutCount", is(0));
    }

    @Test
    void testRegisterAccountDuplicate() {
        String requestBody = """
            {
                "userID": "%s",
                "password": "password123",
                "fullName": "REST Test User",
                "address": "123 REST St",
                "email": "rest@example.com",
                "creditCard": "1234-5678-9012-3456",
                "openBalance": 10000.00
            }
            """.formatted(testUserID);

        // First registration should succeed
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when().post("/api/v1/accounts")
            .then()
                .statusCode(201);

        // Second registration should fail
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when().post("/api/v1/accounts")
            .then()
                .statusCode(400)
                .body("message", notNullValue());
    }

    @Test
    void testRegisterAccountWithDefaultBalance() {
        String requestBody = """
            {
                "userID": "defaultbalance",
                "password": "password123",
                "fullName": "Default Balance User",
                "address": "123 Default St",
                "email": "default@example.com",
                "creditCard": "1234-5678-9012-3456"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when().post("/api/v1/accounts")
            .then()
                .statusCode(201)
                .body("balance", is(100000.00f))
                .body("openBalance", is(100000.00f));
    }

    @Test
    void testGetAccountById() {
        // First create an account
        String requestBody = """
            {
                "userID": "%s",
                "password": "password123",
                "fullName": "REST Test User",
                "address": "123 REST St",
                "email": "rest@example.com",
                "creditCard": "1234-5678-9012-3456",
                "openBalance": 10000.00
            }
            """.formatted(testUserID);

        Integer accountID = given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when().post("/api/v1/accounts")
            .then()
                .statusCode(201)
                .extract().path("accountID");

        // Now get the account by ID
        given()
            .when().get("/api/v1/accounts/" + accountID)
            .then()
                .statusCode(200)
                .body("accountID", is(accountID))
                .body("profileID", is(testUserID))
                .body("balance", is(10000.00f));
    }

    @Test
    void testGetAccountByIdNotFound() {
        given()
            .when().get("/api/v1/accounts/99999")
            .then()
                .statusCode(404)
                .body("message", notNullValue());
    }

    @Test
    void testRegisterAccountMissingFields() {
        String requestBody = """
            {
                "userID": "incomplete",
                "password": "password123"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when().post("/api/v1/accounts")
            .then()
                .statusCode(400);
    }

    @Test
    void testAccountResponseContentType() {
        String requestBody = """
            {
                "userID": "contenttype",
                "password": "password123",
                "fullName": "Content Type User",
                "address": "123 Content St",
                "email": "content@example.com",
                "creditCard": "1234-5678-9012-3456",
                "openBalance": 10000.00
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when().post("/api/v1/accounts")
            .then()
                .statusCode(201)
                .contentType("application/json");
    }
}


