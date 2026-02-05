package com.daytrader.trading.resource;

import com.daytrader.trading.entity.Order;
import com.daytrader.trading.repository.OrderRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Integration tests for OrderResource
 */
@QuarkusTest
class OrderResourceTest {

    @Inject
    OrderRepository orderRepository;

    private Long testAccountId;
    private Long testOrderId;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up any existing test data
        cleanupTestData();
        
        // Create test account ID
        testAccountId = 1000L + System.currentTimeMillis() % 1000;
        
        // Create a test order
        Order order = new Order();
        order.orderType = "buy";
        order.orderStatus = "open";
        order.accountId = testAccountId;
        order.quoteSymbol = "IBM";
        order.quantity = 100.0;
        order.price = new BigDecimal("150.00");
        order.orderFee = new BigDecimal("9.95");
        order.openDate = Instant.now();
        orderRepository.persist(order);
        testOrderId = order.id;
    }

    @AfterEach
    @Transactional
    void tearDown() {
        cleanupTestData();
    }

    @Transactional
    void cleanupTestData() {
        orderRepository.deleteAll();
    }

    @Test
    @Disabled("Hibernate Validator incompatible with Java 25 - Bean validation not working")
    void testCreateOrder_Success() {
        given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "orderType": "buy",
                    "accountId": %d,
                    "symbol": "AAPL",
                    "quantity": 50.0,
                    "price": 175.50
                }
                """, testAccountId))
        .when()
            .post("/api/orders")
        .then()
            .statusCode(201)
            .body("orderType", equalTo("buy"))
            .body("orderStatus", equalTo("open"))
            .body("symbol", equalTo("AAPL"))
            .body("quantity", equalTo(50.0f))
            .body("orderFee", equalTo(9.95f));
    }

    @Test
    void testCreateOrder_WithoutValidation() {
        // Test without relying on bean validation
        String response = given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "orderType": "sell",
                    "accountId": %d,
                    "symbol": "MSFT",
                    "quantity": 75.0,
                    "price": 300.00,
                    "orderFee": 5.00
                }
                """, testAccountId))
        .when()
            .post("/api/orders")
        .then()
            .statusCode(201)
            .extract().asString();

        // Verify response contains expected data
        assert response.contains("sell");
        assert response.contains("MSFT");
    }

    @Test
    void testListOrders() {
        given()
            .queryParam("accountId", testAccountId)
        .when()
            .get("/api/orders")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .body("[0].symbol", equalTo("IBM"));
    }

    @Test
    void testListOrdersByStatus() {
        given()
            .queryParam("accountId", testAccountId)
            .queryParam("status", "open")
        .when()
            .get("/api/orders")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .body("[0].orderStatus", equalTo("open"));
    }

    @Test
    void testGetOrder() {
        given()
            .pathParam("orderId", testOrderId)
            .queryParam("accountId", testAccountId)
        .when()
            .get("/api/orders/{orderId}")
        .then()
            .statusCode(200)
            .body("id", equalTo(testOrderId.intValue()))
            .body("symbol", equalTo("IBM"))
            .body("quantity", equalTo(100.0f));
    }

    @Test
    void testGetOrder_NotFound() {
        given()
            .pathParam("orderId", 999999L)
            .queryParam("accountId", testAccountId)
        .when()
            .get("/api/orders/{orderId}")
        .then()
            .statusCode(404);
    }

    @Test
    void testCancelOrder() {
        given()
            .pathParam("orderId", testOrderId)
            .queryParam("accountId", testAccountId)
        .when()
            .post("/api/orders/{orderId}/cancel")
        .then()
            .statusCode(200)
            .body("orderStatus", equalTo("cancelled"))
            .body("completionDate", notNullValue());
    }
}

