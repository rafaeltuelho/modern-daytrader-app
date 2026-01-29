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

/**
 * PortfolioResource REST integration tests
 * Tests get portfolio, get holding by ID, and not found scenarios
 * Per Phase 2: Feature Implementation - Core Trading Operations
 */
@QuarkusTest
class PortfolioResourceTest {

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
        if (quoteRepository.findBySymbol("PORT1").isEmpty()) {
            Quote quote1 = new Quote("PORT1", "Portfolio Test 1", 1000000.0,
                                    new BigDecimal("100.00"), new BigDecimal("99.00"),
                                    new BigDecimal("98.00"), new BigDecimal("101.00"), 1.0);
            quoteRepository.persist(quote1);
        }
        if (quoteRepository.findBySymbol("PORT2").isEmpty()) {
            Quote quote2 = new Quote("PORT2", "Portfolio Test 2", 2000000.0,
                                    new BigDecimal("200.00"), new BigDecimal("199.00"),
                                    new BigDecimal("198.00"), new BigDecimal("201.00"), 2.0);
            quoteRepository.persist(quote2);
        }
    }

    @Test
    void testGetPortfolio() {
        // User registration and orders happen in their own transactions via TradeService
        String testUserID = "portfoliotest" + System.currentTimeMillis();
        tradeService.register(testUserID, testPassword, "Portfolio Test User", "123 Portfolio St",
                             "portfolio@example.com", "1234-5678-9012-3456", new BigDecimal("10000.00"));

        // Buy some stocks to create holdings
        tradeService.buy(testUserID, "PORT1", 10.0, 0);
        tradeService.buy(testUserID, "PORT2", 5.0, 0);

        // Get portfolio via REST
        given()
            .queryParam("userID", testUserID)
            .when().get("/api/v1/portfolio")
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2))
                .body("[0].holdingID", notNullValue())
                .body("[0].symbol", notNullValue())
                .body("[0].quantity", notNullValue())
                .body("[0].purchasePrice", notNullValue());
    }

    @Test
    void testGetPortfolioMissingUserID() {
        given()
            .when().get("/api/v1/portfolio")
            .then()
                .statusCode(400)
                .body("message", is("userID query parameter is required"));
    }

    @Test
    void testGetPortfolioUserNotFound() {
        given()
            .queryParam("userID", "nonexistentuser")
            .when().get("/api/v1/portfolio")
            .then()
                .statusCode(404)
                .body("message", notNullValue());
    }

    @Test
    void testGetHolding() {
        // User registration and buy happen in their own transactions via TradeService
        String testUserID = "getholdingtest" + System.currentTimeMillis();
        tradeService.register(testUserID, testPassword, "Get Holding User", "123 Portfolio St",
                             "portfolio@example.com", "1234-5678-9012-3456", new BigDecimal("10000.00"));

        // Buy stock to create a holding
        tradeService.buy(testUserID, "PORT1", 8.0, 0);

        // Get the holding ID
        Integer holdingID = tradeService.getHoldings(testUserID).get(0).getHoldingID();

        // Get single holding via REST
        given()
            .when().get("/api/v1/portfolio/" + holdingID)
            .then()
                .statusCode(200)
                .body("holdingID", is(holdingID))
                .body("symbol", is("PORT1"))
                .body("quantity", is(8.0f))
                .body("purchasePrice", is(100.00f));
    }

    @Test
    void testGetHoldingNotFound() {
        given()
            .when().get("/api/v1/portfolio/99999")
            .then()
                .statusCode(404)
                .body("message", notNullValue());
    }
}

