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
import static org.hamcrest.CoreMatchers.notNullValue;

import java.math.BigDecimal;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ibm.websphere.samples.daytrader.entity.Quote;
import com.ibm.websphere.samples.daytrader.repository.QuoteRepository;
import com.ibm.websphere.samples.daytrader.service.MarketSummaryService;

import io.quarkus.test.junit.QuarkusTest;

/**
 * MarketResource REST integration tests
 * Tests get market summary endpoint
 * Per Phase 2: Market Summary & Profiles specification
 */
@QuarkusTest
class MarketResourceTest {

    @Inject
    QuoteRepository quoteRepository;

    @Inject
    MarketSummaryService marketSummaryService;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up and create test quotes
        quoteRepository.deleteAll();

        Quote quote1 = new Quote("MKT1", "Market Test 1", 1000000.0,
                                new BigDecimal("100.00"), new BigDecimal("95.00"),
                                new BigDecimal("94.00"), new BigDecimal("101.00"), 5.0);
        Quote quote2 = new Quote("MKT2", "Market Test 2", 2000000.0,
                                new BigDecimal("200.00"), new BigDecimal("195.00"),
                                new BigDecimal("194.00"), new BigDecimal("201.00"), 3.0);
        Quote quote3 = new Quote("MKT3", "Market Test 3", 1500000.0,
                                new BigDecimal("150.00"), new BigDecimal("155.00"),
                                new BigDecimal("149.00"), new BigDecimal("156.00"), -2.0);
        Quote quote4 = new Quote("MKT4", "Market Test 4", 1800000.0,
                                new BigDecimal("180.00"), new BigDecimal("185.00"),
                                new BigDecimal("179.00"), new BigDecimal("186.00"), -1.0);
        Quote quote5 = new Quote("MKT5", "Market Test 5", 2200000.0,
                                new BigDecimal("220.00"), new BigDecimal("215.00"),
                                new BigDecimal("214.00"), new BigDecimal("221.00"), 4.0);

        quoteRepository.persist(quote1);
        quoteRepository.persist(quote2);
        quoteRepository.persist(quote3);
        quoteRepository.persist(quote4);
        quoteRepository.persist(quote5);

        // Refresh market summary
        marketSummaryService.refreshMarketSummary();
    }

    @Test
    void testGetMarketSummary() {
        given()
            .when().get("/api/v1/market/summary")
            .then()
                .statusCode(200)
                .body("tsia", notNullValue())
                .body("openTSIA", notNullValue())
                .body("volume", notNullValue())
                .body("topGainers", notNullValue())
                .body("topLosers", notNullValue());
    }

    @Test
    void testGetMarketSummaryContentType() {
        given()
            .when().get("/api/v1/market/summary")
            .then()
                .statusCode(200)
                .contentType("application/json");
    }

    @Test
    @Transactional
    void testGetMarketSummaryWithEmptyDatabase() {
        // Delete all quotes
        quoteRepository.deleteAll();

        // Should still return 200 with previous cached data or handle gracefully
        given()
            .when().get("/api/v1/market/summary")
            .then()
                .statusCode(200);
    }
}

