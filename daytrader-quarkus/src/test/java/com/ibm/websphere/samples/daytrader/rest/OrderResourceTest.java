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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.math.BigDecimal;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ibm.websphere.samples.daytrader.entity.Quote;
import com.ibm.websphere.samples.daytrader.repository.AccountProfileRepository;
import com.ibm.websphere.samples.daytrader.repository.AccountRepository;
import com.ibm.websphere.samples.daytrader.repository.QuoteRepository;
import com.ibm.websphere.samples.daytrader.service.TradeService;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * OrderResource REST integration tests
 * Tests buy stock, sell holding, get orders, and validation errors
 * Per Phase 2: Feature Implementation - Core Trading Operations
 */
@QuarkusTest
class OrderResourceTest {

    @Inject
    TradeService tradeService;

    @Inject
    AccountRepository accountRepository;

    @Inject
    AccountProfileRepository accountProfileRepository;

    @Inject
    QuoteRepository quoteRepository;

    @Inject
    com.ibm.websphere.samples.daytrader.repository.HoldingRepository holdingRepository;

    private String testPassword = "password123";

    @BeforeEach
    @Transactional
    void setUp() {
        // Create test quotes if they don't already exist (avoid deleting to prevent FK violations)
        if (quoteRepository.findBySymbol("BUYTEST").isEmpty()) {
            Quote quote1 = new Quote("BUYTEST", "Buy Test Company", 1000000.0,
                                    new BigDecimal("100.00"), new BigDecimal("99.00"),
                                    new BigDecimal("98.00"), new BigDecimal("101.00"), 1.0);
            quoteRepository.persist(quote1);
        }
        if (quoteRepository.findBySymbol("SELLTEST").isEmpty()) {
            Quote quote2 = new Quote("SELLTEST", "Sell Test Company", 2000000.0,
                                    new BigDecimal("200.00"), new BigDecimal("199.00"),
                                    new BigDecimal("198.00"), new BigDecimal("201.00"), 2.0);
            quoteRepository.persist(quote2);
        }
    }

    @Test
    void testBuyStock() {
        // User registration happens in its own transaction via TradeService
        String testUserID = "ordertest" + System.currentTimeMillis();
        tradeService.register(testUserID, testPassword, "Order Test User", "123 Order St",
                             "order@example.com", "1234-5678-9012-3456", new BigDecimal("10000.00"));

        given()
            .contentType(ContentType.JSON)
            .body("{\"userID\": \"" + testUserID + "\", \"symbol\": \"BUYTEST\", \"quantity\": 10.0, \"orderProcessingMode\": 0}")
            .when().post("/api/v1/orders/buy")
            .then()
                .statusCode(201)
                .body("orderID", notNullValue())
                .body("orderType", is("buy"))
                .body("orderStatus", is("closed"))
                .body("quantity", is(10.0f))
                .body("price", is(100.00f));
    }

    @Test
    void testBuyStockMissingUserID() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"symbol\": \"BUYTEST\", \"quantity\": 10.0}")
            .when().post("/api/v1/orders/buy")
            .then()
                .statusCode(400)
                .body("message", is("userID is required"));
    }

    @Test
    void testBuyStockMissingSymbol() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userID\": \"testuser\", \"quantity\": 10.0}")
            .when().post("/api/v1/orders/buy")
            .then()
                .statusCode(400)
                .body("message", is("symbol is required"));
    }

    @Test
    void testBuyStockInvalidQuantity() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userID\": \"testuser\", \"symbol\": \"BUYTEST\", \"quantity\": 0}")
            .when().post("/api/v1/orders/buy")
            .then()
                .statusCode(400)
                .body("message", is("quantity must be greater than 0"));
    }

    @Test
    void testBuyStockQuoteNotFound() {
        // Register user first so we get the proper 404 for quote, not user
        String testUserID = "quotenotfoundtest" + System.currentTimeMillis();
        tradeService.register(testUserID, testPassword, "Quote Not Found User", "123 Test St",
                             "test@example.com", "1234-5678-9012-3456", new BigDecimal("10000.00"));

        given()
            .contentType(ContentType.JSON)
            .body("{\"userID\": \"" + testUserID + "\", \"symbol\": \"NONEXISTENT\", \"quantity\": 10.0}")
            .when().post("/api/v1/orders/buy")
            .then()
                .statusCode(404)
                .body("message", notNullValue());
    }

    @Test
    void testSellHolding() {
        // User registration and buy happen in their own transactions via TradeService
        String testUserID = "selltest" + System.currentTimeMillis();
        tradeService.register(testUserID, testPassword, "Sell Test User", "123 Order St",
                             "order@example.com", "1234-5678-9012-3456", new BigDecimal("10000.00"));

        // First buy stock to create a holding
        tradeService.buy(testUserID, "SELLTEST", 5.0, 0);

        // Get the holding ID
        Integer holdingID = tradeService.getHoldings(testUserID).get(0).getHoldingID();

        // Sell the holding via REST
        given()
            .contentType(ContentType.JSON)
            .body("{\"userID\": \"" + testUserID + "\", \"holdingID\": " + holdingID + ", \"orderProcessingMode\": 0}")
            .when().post("/api/v1/orders/sell")
            .then()
                .statusCode(201)
                .body("orderID", notNullValue())
                .body("orderType", is("sell"))
                .body("orderStatus", is("closed"))
                .body("quantity", is(5.0f));
    }

    @Test
    void testSellHoldingMissingUserID() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"holdingID\": 123}")
            .when().post("/api/v1/orders/sell")
            .then()
                .statusCode(400)
                .body("message", is("userID is required"));
    }

    @Test
    void testSellHoldingMissingHoldingID() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userID\": \"testuser\"}")
            .when().post("/api/v1/orders/sell")
            .then()
                .statusCode(400)
                .body("message", is("holdingID is required"));
    }

    @Test
    void testGetOrders() {
        // User registration and orders happen in their own transactions via TradeService
        String testUserID = "getorderstest" + System.currentTimeMillis();
        tradeService.register(testUserID, testPassword, "Get Orders User", "123 Order St",
                             "order@example.com", "1234-5678-9012-3456", new BigDecimal("10000.00"));

        // Create some orders
        tradeService.buy(testUserID, "BUYTEST", 5.0, 0);
        tradeService.buy(testUserID, "SELLTEST", 3.0, 0);

        // Get orders via REST
        given()
            .queryParam("userID", testUserID)
            .when().get("/api/v1/orders")
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    void testGetOrdersMissingUserID() {
        given()
            .when().get("/api/v1/orders")
            .then()
                .statusCode(400)
                .body("message", is("userID query parameter is required"));
    }

    @Test
    void testGetOrdersUserNotFound() {
        given()
            .queryParam("userID", "nonexistentuser")
            .when().get("/api/v1/orders")
            .then()
                .statusCode(404)
                .body("message", notNullValue());
    }
}

