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
import com.ibm.websphere.samples.daytrader.repository.QuoteRepository;

import io.quarkus.test.junit.QuarkusTest;

/**
 * QuoteResource REST integration tests using REST-assured
 * Tests GET /api/v1/quotes and GET /api/v1/quotes/{symbol}
 * Per Phase 3: Backend Migration specification section 9 - Testing Strategy
 */
@QuarkusTest
class QuoteResourceTest {

    @Inject
    QuoteRepository quoteRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        // Create test quotes if they don't already exist (avoid deleteAll to prevent FK violations)
        if (quoteRepository.findBySymbol("IBM").isEmpty()) {
            Quote quote1 = new Quote("IBM", "IBM Corporation", 1000000.0,
                                    new BigDecimal("150.00"), new BigDecimal("148.00"),
                                    new BigDecimal("147.00"), new BigDecimal("152.00"), 2.0);
            quoteRepository.persist(quote1);
        }
        if (quoteRepository.findBySymbol("AAPL").isEmpty()) {
            Quote quote2 = new Quote("AAPL", "Apple Inc", 2000000.0,
                                    new BigDecimal("175.00"), new BigDecimal("174.00"),
                                    new BigDecimal("173.00"), new BigDecimal("176.00"), 1.0);
            quoteRepository.persist(quote2);
        }
        if (quoteRepository.findBySymbol("GOOGL").isEmpty()) {
            Quote quote3 = new Quote("GOOGL", "Alphabet Inc", 1500000.0,
                                    new BigDecimal("140.00"), new BigDecimal("139.00"),
                                    new BigDecimal("138.00"), new BigDecimal("141.00"), 1.0);
            quoteRepository.persist(quote3);
        }
    }

    @Test
    void testGetAllQuotes() {
        given()
            .when().get("/api/v1/quotes")
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(3));
    }

    @Test
    void testGetQuoteBySymbol() {
        given()
            .when().get("/api/v1/quotes/IBM")
            .then()
                .statusCode(200)
                .body("symbol", is("IBM"))
                .body("companyName", is("IBM Corporation"))
                .body("price", is(150.00f))
                .body("volume", is(1000000.0f))
                .body("change", is(2.0f));
    }

    @Test
    void testGetQuoteBySymbolNotFound() {
        given()
            .when().get("/api/v1/quotes/NONEXISTENT")
            .then()
                .statusCode(404)
                .body("message", notNullValue());
    }

    @Test
    void testGetQuoteBySymbolCaseInsensitive() {
        given()
            .when().get("/api/v1/quotes/ibm")
            .then()
                .statusCode(200)
                .body("symbol", is("IBM"));
    }

    @Test
    void testGetMultipleQuotes() {
        given()
            .when().get("/api/v1/quotes")
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(3))
                .body("[0].symbol", notNullValue())
                .body("[0].companyName", notNullValue())
                .body("[0].price", notNullValue());
    }

    @Test
    void testQuoteResponseStructure() {
        given()
            .when().get("/api/v1/quotes/AAPL")
            .then()
                .statusCode(200)
                .body("symbol", is("AAPL"))
                .body("companyName", is("Apple Inc"))
                .body("price", notNullValue())
                .body("open", notNullValue())
                .body("low", notNullValue())
                .body("high", notNullValue())
                .body("volume", notNullValue())
                .body("change", notNullValue());
    }

    @Test
    void testGetQuoteContentType() {
        given()
            .when().get("/api/v1/quotes/IBM")
            .then()
                .statusCode(200)
                .contentType("application/json");
    }

    @Test
    void testGetAllQuotesContentType() {
        given()
            .when().get("/api/v1/quotes")
            .then()
                .statusCode(200)
                .contentType("application/json");
    }
}

