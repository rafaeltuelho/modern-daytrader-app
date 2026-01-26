package com.ibm.websphere.samples.daytrader.resources;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.ibm.websphere.samples.daytrader.entities.Account;
import com.ibm.websphere.samples.daytrader.entities.AccountProfile;
import com.ibm.websphere.samples.daytrader.entities.Holding;
import com.ibm.websphere.samples.daytrader.entities.Order;
import com.ibm.websphere.samples.daytrader.entities.Quote;
import com.ibm.websphere.samples.daytrader.services.AuthService;
import com.ibm.websphere.samples.daytrader.services.JWTService;
import com.ibm.websphere.samples.daytrader.services.MarketService;
import com.ibm.websphere.samples.daytrader.services.TradeService;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@DisplayName("TradeResource Integration Tests")
class TradeResourceIT {

    @Inject AuthService authService;
    @Inject JWTService jwtService;
    @Inject MarketService marketService;
    @Inject TradeService tradeService;

    private static final String TEST_USER = "tradeuser";
    private static final String TEST_SYMBOL = "AAPL";

    @BeforeEach
    @Transactional
    void setUp() {
        Order.deleteAll();
        Holding.deleteAll();
        Account.deleteAll();
        AccountProfile.deleteAll();
        Quote.deleteAll();
    }

    @Transactional
    String createTestUserAndGetToken(BigDecimal balance) {
        authService.register(TEST_USER, "password", "Trade User", "123 Trade St",
                "trade@example.com", "1234-5678", balance);
        return jwtService.generateToken(TEST_USER);
    }

    @Transactional
    void createTestQuote(String symbol, BigDecimal price) {
        marketService.createQuote(symbol, symbol + " Company", price);
    }

    @Nested
    @DisplayName("POST /api/trade/buy")
    class BuyTests {

        @Test
        @DisplayName("should return 201 on successful buy")
        void buySuccess() {
            String token = createTestUserAndGetToken(new BigDecimal("10000"));
            createTestQuote(TEST_SYMBOL, new BigDecimal("100"));

            given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("{\"symbol\": \"" + TEST_SYMBOL + "\", \"quantity\": 10}")
            .when()
                .post("/api/trade/buy")
            .then()
                .statusCode(201)
                .body("orderType", equalTo("buy"))
                .body("orderStatus", equalTo("closed"));
        }

        @Test
        @DisplayName("should return 400 on insufficient funds")
        void buyInsufficientFunds() {
            String token = createTestUserAndGetToken(new BigDecimal("100"));
            createTestQuote(TEST_SYMBOL, new BigDecimal("100"));

            given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("{\"symbol\": \"" + TEST_SYMBOL + "\", \"quantity\": 10}")
            .when()
                .post("/api/trade/buy")
            .then()
                .statusCode(400)
                .body("error", containsString("Insufficient funds"));
        }

        @Test
        @DisplayName("should return 404 when symbol not found")
        void buySymbolNotFound() {
            String token = createTestUserAndGetToken(new BigDecimal("10000"));

            given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("{\"symbol\": \"INVALID\", \"quantity\": 10}")
            .when()
                .post("/api/trade/buy")
            .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("should return 401 without authentication")
        void buyUnauthorized() {
            given()
                .contentType(ContentType.JSON)
                .body("{\"symbol\": \"AAPL\", \"quantity\": 10}")
            .when()
                .post("/api/trade/buy")
            .then()
                .statusCode(401);
        }
    }

    @Nested
    @DisplayName("GET /api/trade/orders")
    class GetOrdersTests {

        @Test
        @DisplayName("should return 200 and list of orders")
        void getOrdersSuccess() {
            String token = createTestUserAndGetToken(new BigDecimal("10000"));

            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get("/api/trade/orders")
            .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
        }
    }

    @Nested
    @DisplayName("GET /api/trade/holdings")
    class GetHoldingsTests {

        @Test
        @DisplayName("should return 200 and list of holdings")
        void getHoldingsSuccess() {
            String token = createTestUserAndGetToken(new BigDecimal("10000"));

            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get("/api/trade/holdings")
            .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
        }
    }
}

