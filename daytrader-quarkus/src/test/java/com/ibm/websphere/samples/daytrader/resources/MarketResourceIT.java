package com.ibm.websphere.samples.daytrader.resources;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.ibm.websphere.samples.daytrader.entities.Quote;
import com.ibm.websphere.samples.daytrader.services.MarketService;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@DisplayName("MarketResource Integration Tests")
class MarketResourceIT {

    @Inject
    MarketService marketService;

    @BeforeEach
    @Transactional
    void setUp() {
        Quote.deleteAll();
    }

    @Transactional
    void createTestQuote(String symbol, String companyName, BigDecimal price) {
        marketService.createQuote(symbol, companyName, price);
    }

    @Nested
    @DisplayName("GET /api/market/quotes")
    class GetAllQuotesTests {

        @Test
        @DisplayName("should return 200 and list of all quotes")
        void getAllQuotesSuccess() {
            createTestQuote("AAPL", "Apple Inc.", new BigDecimal("150"));
            createTestQuote("GOOGL", "Alphabet Inc.", new BigDecimal("2800"));

            given()
            .when()
                .get("/api/market/quotes")
            .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("symbol", hasItems("AAPL", "GOOGL"));
        }

        @Test
        @DisplayName("should return 200 and empty list when no quotes exist")
        void getAllQuotesEmpty() {
            given()
            .when()
                .get("/api/market/quotes")
            .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }
    }

    @Nested
    @DisplayName("GET /api/market/quotes/{symbol}")
    class GetQuoteTests {

        @Test
        @DisplayName("should return 200 and quote for existing symbol")
        void getQuoteSuccess() {
            createTestQuote("AAPL", "Apple Inc.", new BigDecimal("150"));

            given()
            .when()
                .get("/api/market/quotes/AAPL")
            .then()
                .statusCode(200)
                .body("symbol", equalTo("AAPL"))
                .body("companyName", equalTo("Apple Inc."))
                .body("price", equalTo(150));
        }

        @Test
        @DisplayName("should return 404 when symbol does not exist")
        void getQuoteNotFound() {
            given()
            .when()
                .get("/api/market/quotes/INVALID")
            .then()
                .statusCode(404)
                .body("error", containsString("Quote not found"));
        }
    }

    @Nested
    @DisplayName("GET /api/market/summary")
    class GetMarketSummaryTests {

        @Test
        @DisplayName("should return 200 and market summary")
        void getMarketSummarySuccess() {
            createTestQuote("AAPL", "Apple Inc.", new BigDecimal("150"));
            createTestQuote("GOOGL", "Alphabet Inc.", new BigDecimal("100"));

            given()
            .when()
                .get("/api/market/summary")
            .then()
                .statusCode(200)
                .body("tsia", notNullValue())
                .body("topGainers", notNullValue())
                .body("topLosers", notNullValue());
        }

        @Test
        @DisplayName("should return 200 with empty summary when no quotes exist")
        void getMarketSummaryEmpty() {
            given()
            .when()
                .get("/api/market/summary")
            .then()
                .statusCode(200)
                .body("tsia", equalTo(0))
                .body("volume", equalTo(0.0F))
                .body("topGainers", hasSize(0))
                .body("topLosers", hasSize(0));
        }
    }
}

