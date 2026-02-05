package com.daytrader.market.resource;

import com.daytrader.market.entity.Quote;
import com.daytrader.market.repository.QuoteRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

/**
 * Integration tests for MarketResource
 */
@QuarkusTest
class MarketResourceTest {

    @Inject
    QuoteRepository quoteRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up any existing test data
        cleanupTestData();
        
        // Create test quotes with different price changes
        createTestQuoteWithChange("GAIN1", "Top Gainer 1", new BigDecimal("100.00"), 15.0);
        createTestQuoteWithChange("GAIN2", "Top Gainer 2", new BigDecimal("200.00"), 10.0);
        createTestQuoteWithChange("GAIN3", "Top Gainer 3", new BigDecimal("150.00"), 8.0);
        createTestQuoteWithChange("NEUTRAL", "Neutral Stock", new BigDecimal("50.00"), 0.0);
        createTestQuoteWithChange("LOSS1", "Top Loser 1", new BigDecimal("75.00"), -12.0);
        createTestQuoteWithChange("LOSS2", "Top Loser 2", new BigDecimal("80.00"), -8.0);
        createTestQuoteWithChange("LOSS3", "Top Loser 3", new BigDecimal("90.00"), -5.0);
    }

    @AfterEach
    @Transactional
    void tearDown() {
        cleanupTestData();
    }

    @Transactional
    void cleanupTestData() {
        quoteRepository.deleteAll();
    }

    @Test
    void testGetTopGainers_Default() {
        given()
        .when()
            .get("/api/market/gainers")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(3)))
            .body("[0].symbol", notNullValue())
            .body("[0].priceChange", notNullValue());
    }

    @Test
    void testGetTopGainers_WithLimit() {
        given()
            .queryParam("limit", "2")
        .when()
            .get("/api/market/gainers")
        .then()
            .statusCode(200)
            .body("$", hasSize(2));
    }

    @Test
    void testGetTopLosers_Default() {
        given()
        .when()
            .get("/api/market/losers")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(3)))
            .body("[0].symbol", notNullValue())
            .body("[0].priceChange", notNullValue());
    }

    @Test
    void testGetTopLosers_WithLimit() {
        given()
            .queryParam("limit", "2")
        .when()
            .get("/api/market/losers")
        .then()
            .statusCode(200)
            .body("$", hasSize(2));
    }

    @Test
    void testGetMarketSummary() {
        given()
        .when()
            .get("/api/market/summary")
        .then()
            .statusCode(200)
            .body("tsia", notNullValue())
            .body("openTsia", notNullValue())
            .body("volume", notNullValue())
            .body("topGainers", notNullValue())
            .body("topLosers", notNullValue())
            .body("summaryDate", notNullValue())
            .body("gainPercent", notNullValue())
            .body("marketStatus", notNullValue())
            .body("topGainersCount", notNullValue())
            .body("topLosersCount", notNullValue());
    }

    @Test
    void testGetMarketSummary_HasTopGainersAndLosers() {
        given()
        .when()
            .get("/api/market/summary")
        .then()
            .statusCode(200)
            .body("topGainers", hasSize(greaterThanOrEqualTo(3)))
            .body("topLosers", hasSize(greaterThanOrEqualTo(3)));
    }

    @Transactional
    void createTestQuoteWithChange(String symbol, String companyName, BigDecimal price, double priceChange) {
        Quote quote = new Quote();
        quote.symbol = symbol;
        quote.companyName = companyName;
        quote.volume = 500000.0;
        quote.price = price;
        quote.openPrice = price;
        quote.lowPrice = price;
        quote.highPrice = price;
        quote.priceChange = priceChange;
        quoteRepository.persist(quote);
    }
}

