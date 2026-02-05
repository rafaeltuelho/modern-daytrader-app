package com.daytrader.market.resource;

import com.daytrader.market.entity.Quote;
import com.daytrader.market.repository.QuoteRepository;
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
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

/**
 * Integration tests for QuoteResource
 */
@QuarkusTest
class QuoteResourceTest {

    @Inject
    QuoteRepository quoteRepository;

    private String testSymbol;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up any existing test data
        cleanupTestData();
        
        // Create test quote
        testSymbol = "IBM";
        Quote quote = new Quote();
        quote.symbol = testSymbol;
        quote.companyName = "IBM Corporation";
        quote.volume = 1000000.0;
        quote.price = new BigDecimal("150.00");
        quote.openPrice = new BigDecimal("148.00");
        quote.lowPrice = new BigDecimal("147.00");
        quote.highPrice = new BigDecimal("152.00");
        quote.priceChange = 2.0;
        quoteRepository.persist(quote);
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
    void testGetQuote_Success() {
        given()
        .when()
            .get("/api/quotes/" + testSymbol)
        .then()
            .statusCode(200)
            .body("symbol", equalTo(testSymbol))
            .body("companyName", equalTo("IBM Corporation"))
            .body("volume", equalTo(1000000.0f))
            .body("price", equalTo(150.00f))
            .body("priceChange", equalTo(2.0f));
    }

    @Test
    void testGetQuote_NotFound() {
        given()
        .when()
            .get("/api/quotes/NONEXISTENT")
        .then()
            .statusCode(404)
            .body("error", equalTo("RESOURCE_NOT_FOUND"))
            .body("message", containsString("Quote not found"));
    }

    @Test
    void testListQuotes() {
        // Create additional quotes
        createTestQuote("AAPL", "Apple Inc.", new BigDecimal("175.00"));
        createTestQuote("GOOGL", "Alphabet Inc.", new BigDecimal("140.00"));
        
        given()
        .when()
            .get("/api/quotes")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThan(2)));
    }

    @Test
    @Disabled("Hibernate Validator incompatible with Java 25 - Bean validation not working")
    void testSaveQuote_Success() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "symbol": "MSFT",
                    "companyName": "Microsoft Corporation",
                    "volume": 2000000.0,
                    "price": 380.00,
                    "openPrice": 375.00,
                    "lowPrice": 374.00,
                    "highPrice": 382.00,
                    "priceChange": 5.0
                }
                """)
        .when()
            .post("/api/quotes")
        .then()
            .statusCode(200)
            .body("symbol", equalTo("MSFT"))
            .body("companyName", equalTo("Microsoft Corporation"))
            .body("price", equalTo(380.00f));
    }

    @Test
    void testUpdatePrice_Success() {
        given()
            .queryParam("price", "155.00")
        .when()
            .put("/api/quotes/" + testSymbol + "/price")
        .then()
            .statusCode(200)
            .body("symbol", equalTo(testSymbol))
            .body("price", equalTo(155.00f))
            .body("priceChange", equalTo(5.0f)); // 155 - 150 = 5
    }

    @Test
    void testUpdatePrice_NotFound() {
        given()
            .queryParam("price", "100.00")
        .when()
            .put("/api/quotes/NONEXISTENT/price")
        .then()
            .statusCode(404)
            .body("error", equalTo("RESOURCE_NOT_FOUND"));
    }

    @Transactional
    void createTestQuote(String symbol, String companyName, BigDecimal price) {
        Quote quote = new Quote();
        quote.symbol = symbol;
        quote.companyName = companyName;
        quote.volume = 500000.0;
        quote.price = price;
        quote.openPrice = price;
        quote.lowPrice = price;
        quote.highPrice = price;
        quote.priceChange = 0.0;
        quoteRepository.persist(quote);
    }
}

