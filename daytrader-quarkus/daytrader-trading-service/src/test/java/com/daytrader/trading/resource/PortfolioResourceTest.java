package com.daytrader.trading.resource;

import com.daytrader.trading.entity.Holding;
import com.daytrader.trading.entity.Order;
import com.daytrader.trading.repository.HoldingRepository;
import com.daytrader.trading.repository.OrderRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

/**
 * Integration tests for PortfolioResource
 */
@QuarkusTest
class PortfolioResourceTest {

    @Inject
    HoldingRepository holdingRepository;

    @Inject
    OrderRepository orderRepository;

    private Long testAccountId;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up any existing test data
        cleanupTestData();
        
        // Create test account ID
        testAccountId = 3000L + System.currentTimeMillis() % 1000;
        
        // Create test holdings
        Holding holding1 = new Holding();
        holding1.accountId = testAccountId;
        holding1.quoteSymbol = "IBM";
        holding1.quantity = 100.0;
        holding1.purchasePrice = new BigDecimal("150.00");
        holding1.purchaseDate = Instant.now();
        holdingRepository.persist(holding1);

        Holding holding2 = new Holding();
        holding2.accountId = testAccountId;
        holding2.quoteSymbol = "AAPL";
        holding2.quantity = 50.0;
        holding2.purchasePrice = new BigDecimal("175.00");
        holding2.purchaseDate = Instant.now();
        holdingRepository.persist(holding2);

        // Create test orders
        Order order1 = new Order();
        order1.orderType = "buy";
        order1.orderStatus = "open";
        order1.accountId = testAccountId;
        order1.quoteSymbol = "MSFT";
        order1.quantity = 75.0;
        order1.price = new BigDecimal("300.00");
        order1.orderFee = new BigDecimal("9.95");
        order1.openDate = Instant.now();
        orderRepository.persist(order1);

        Order order2 = new Order();
        order2.orderType = "sell";
        order2.orderStatus = "completed";
        order2.accountId = testAccountId;
        order2.quoteSymbol = "GOOGL";
        order2.quantity = 10.0;
        order2.price = new BigDecimal("2800.00");
        order2.orderFee = new BigDecimal("9.95");
        order2.openDate = Instant.now().minusSeconds(3600);
        order2.completionDate = Instant.now();
        orderRepository.persist(order2);
    }

    @AfterEach
    @Transactional
    void tearDown() {
        cleanupTestData();
    }

    @Transactional
    void cleanupTestData() {
        orderRepository.deleteAll();
        holdingRepository.deleteAll();
    }

    @Test
    void testGetPortfolioSummary() {
        given()
            .queryParam("accountId", testAccountId)
            .queryParam("cashBalance", 5000.00)
        .when()
            .get("/api/portfolio/summary")
        .then()
            .statusCode(200)
            .body("accountId", equalTo(testAccountId.intValue()))
            .body("cashBalance", equalTo(5000.00f))
            .body("holdingsValue", equalTo(23750.00f)) // (100 * 150) + (50 * 175)
            .body("totalValue", equalTo(28750.00f)) // 5000 + 23750
            .body("totalGain", equalTo(0.0f)) // Using purchase price as current price
            .body("totalGainPercent", equalTo(0.0f))
            .body("holdingsCount", equalTo(2))
            .body("recentOrders.size()", equalTo(2))
            .body("topHoldings.size()", equalTo(2));
    }

    @Test
    void testGetPortfolioSummary_EmptyPortfolio() {
        Long emptyAccountId = 9999L;

        given()
            .queryParam("accountId", emptyAccountId)
            .queryParam("cashBalance", 10000.00)
        .when()
            .get("/api/portfolio/summary")
        .then()
            .statusCode(200)
            .body("accountId", equalTo(emptyAccountId.intValue()))
            .body("cashBalance", equalTo(10000.00f))
            .body("holdingsValue", equalTo(0))
            .body("totalValue", equalTo(10000.00f))
            .body("holdingsCount", equalTo(0))
            .body("recentOrders.size()", equalTo(0))
            .body("topHoldings.size()", equalTo(0));
    }

    @Test
    void testGetPortfolioSummary_MissingAccountId() {
        given()
            .queryParam("cashBalance", 10000.00)
        .when()
            .get("/api/portfolio/summary")
        .then()
            .statusCode(400);
    }

    @Test
    void testGetPortfolioSummary_DefaultCashBalance() {
        given()
            .queryParam("accountId", testAccountId)
        .when()
            .get("/api/portfolio/summary")
        .then()
            .statusCode(200)
            .body("cashBalance", equalTo(0))
            .body("totalValue", equalTo(23750.00f)); // Only holdings value
    }
}

