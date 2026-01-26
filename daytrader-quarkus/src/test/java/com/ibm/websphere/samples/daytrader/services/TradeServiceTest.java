package com.ibm.websphere.samples.daytrader.services;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.ibm.websphere.samples.daytrader.entities.Account;
import com.ibm.websphere.samples.daytrader.entities.AccountProfile;
import com.ibm.websphere.samples.daytrader.entities.Holding;
import com.ibm.websphere.samples.daytrader.entities.Order;
import com.ibm.websphere.samples.daytrader.entities.Quote;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@Transactional
@DisplayName("TradeService Unit Tests")
class TradeServiceTest {

    @Inject
    TradeService tradeService;

    @Inject
    AuthService authService;

    @Inject
    MarketService marketService;

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

    private Account createTestUser(BigDecimal balance) {
        return authService.register(TEST_USER, "password", "Trade User",
                "123 Trade St", "trade@example.com", "1234-5678", balance);
    }

    private Quote createTestQuote(String symbol, BigDecimal price) {
        return marketService.createQuote(symbol, symbol + " Company", price);
    }

    @Nested
    @DisplayName("buy() tests")
    class BuyTests {

        @Test
        @DisplayName("should buy stock successfully")
        void buySuccess() {
            // Given
            createTestUser(new BigDecimal("10000"));
            createTestQuote(TEST_SYMBOL, new BigDecimal("100"));

            // When
            Order order = tradeService.buy(TEST_USER, TEST_SYMBOL, 10);

            // Then
            assertNotNull(order);
            assertEquals("buy", order.orderType);
            assertEquals("closed", order.orderStatus);
            assertEquals(10, order.quantity);
            assertEquals(0, new BigDecimal("100").compareTo(order.price));
        }

        @Test
        @DisplayName("should throw BadRequestException when userID is null")
        void buyNullUserID() {
            assertThrows(BadRequestException.class, () -> tradeService.buy(null, TEST_SYMBOL, 10));
        }

        @Test
        @DisplayName("should throw BadRequestException when symbol is null")
        void buyNullSymbol() {
            createTestUser(new BigDecimal("10000"));
            assertThrows(BadRequestException.class, () -> tradeService.buy(TEST_USER, null, 10));
        }

        @Test
        @DisplayName("should throw BadRequestException when quantity is zero or negative")
        void buyInvalidQuantity() {
            createTestUser(new BigDecimal("10000"));
            createTestQuote(TEST_SYMBOL, new BigDecimal("100"));
            assertThrows(BadRequestException.class, () -> tradeService.buy(TEST_USER, TEST_SYMBOL, 0));
            assertThrows(BadRequestException.class, () -> tradeService.buy(TEST_USER, TEST_SYMBOL, -5));
        }

        @Test
        @DisplayName("should throw NotFoundException when user does not exist")
        void buyUserNotFound() {
            createTestQuote(TEST_SYMBOL, new BigDecimal("100"));
            assertThrows(NotFoundException.class, () -> tradeService.buy("nonexistent", TEST_SYMBOL, 10));
        }

        @Test
        @DisplayName("should throw NotFoundException when symbol does not exist")
        void buySymbolNotFound() {
            createTestUser(new BigDecimal("10000"));
            assertThrows(NotFoundException.class, () -> tradeService.buy(TEST_USER, "INVALID", 10));
        }

        @Test
        @DisplayName("should throw BadRequestException when insufficient funds")
        void buyInsufficientFunds() {
            createTestUser(new BigDecimal("100")); // Only $100
            createTestQuote(TEST_SYMBOL, new BigDecimal("100")); // $100/share
            // Buying 10 shares = $1000 + $24.95 fee > $100 balance
            assertThrows(BadRequestException.class, () -> tradeService.buy(TEST_USER, TEST_SYMBOL, 10));
        }

        @Test
        @DisplayName("should debit account balance after purchase")
        void buyDebitsBalance() {
            // Given
            createTestUser(new BigDecimal("10000"));
            createTestQuote(TEST_SYMBOL, new BigDecimal("100"));

            // When
            tradeService.buy(TEST_USER, TEST_SYMBOL, 10);

            // Then - balance should be 10000 - (100 * 10) - 24.95 = 8975.05
            Account account = Account.findByProfileUserID(TEST_USER);
            assertEquals(new BigDecimal("8975.05"), account.balance.setScale(2));
        }
    }

    @Nested
    @DisplayName("sell() tests")
    class SellTests {

        @Test
        @DisplayName("should throw BadRequestException when userID is null")
        void sellNullUserID() {
            assertThrows(BadRequestException.class, () -> tradeService.sell(null, 1L));
        }

        @Test
        @DisplayName("should throw BadRequestException when holdingId is null")
        void sellNullHoldingId() {
            createTestUser(new BigDecimal("10000"));
            assertThrows(BadRequestException.class, () -> tradeService.sell(TEST_USER, null));
        }

        @Test
        @DisplayName("should throw NotFoundException when user does not exist")
        void sellUserNotFound() {
            assertThrows(NotFoundException.class, () -> tradeService.sell("nonexistent", 1L));
        }

        @Test
        @DisplayName("should throw NotFoundException when holding does not exist")
        void sellHoldingNotFound() {
            createTestUser(new BigDecimal("10000"));
            assertThrows(NotFoundException.class, () -> tradeService.sell(TEST_USER, 99999L));
        }

        @Test
        @DisplayName("should throw BadRequestException when userID is blank")
        void sellBlankUserID() {
            assertThrows(BadRequestException.class, () -> tradeService.sell("  ", 1L));
        }
    }

    @Nested
    @DisplayName("getOrders() tests")
    class GetOrdersTests {

        @Test
        @DisplayName("should return orders for user")
        void getOrdersSuccess() {
            // Given
            createTestUser(new BigDecimal("10000"));
            createTestQuote(TEST_SYMBOL, new BigDecimal("100"));
            tradeService.buy(TEST_USER, TEST_SYMBOL, 5);
            tradeService.buy(TEST_USER, TEST_SYMBOL, 3);

            // When
            List<Order> orders = tradeService.getOrders(TEST_USER);

            // Then
            assertNotNull(orders);
            assertEquals(2, orders.size());
        }

        @Test
        @DisplayName("should return empty list for user with no orders")
        void getOrdersEmpty() {
            // Given
            createTestUser(new BigDecimal("10000"));

            // When
            List<Order> orders = tradeService.getOrders(TEST_USER);

            // Then
            assertNotNull(orders);
            assertTrue(orders.isEmpty());
        }

        @Test
        @DisplayName("should throw BadRequestException when userID is null")
        void getOrdersNullUserID() {
            assertThrows(BadRequestException.class, () -> tradeService.getOrders(null));
        }
    }

    @Nested
    @DisplayName("getHoldings() tests")
    class GetHoldingsTests {

        @Test
        @DisplayName("should return holdings for user")
        void getHoldingsSuccess() {
            // Given
            createTestUser(new BigDecimal("10000"));
            createTestQuote(TEST_SYMBOL, new BigDecimal("100"));
            createTestQuote("GOOGL", new BigDecimal("50"));
            tradeService.buy(TEST_USER, TEST_SYMBOL, 5);
            tradeService.buy(TEST_USER, "GOOGL", 10);

            // When
            List<Holding> holdings = tradeService.getHoldings(TEST_USER);

            // Then
            assertNotNull(holdings);
            assertEquals(2, holdings.size());
        }

        @Test
        @DisplayName("should throw BadRequestException when userID is null")
        void getHoldingsNullUserID() {
            assertThrows(BadRequestException.class, () -> tradeService.getHoldings(null));
        }
    }

    @Nested
    @DisplayName("getHolding() tests")
    class GetHoldingTests {

        @Test
        @DisplayName("should return holding by ID for user")
        void getHoldingSuccess() {
            // Given
            createTestUser(new BigDecimal("10000"));
            createTestQuote(TEST_SYMBOL, new BigDecimal("100"));
            Order order = tradeService.buy(TEST_USER, TEST_SYMBOL, 5);
            Long holdingId = order.holding.id;

            // When
            Holding holding = tradeService.getHolding(TEST_USER, holdingId);

            // Then
            assertNotNull(holding);
            assertEquals(holdingId, holding.id);
            assertEquals(5, holding.quantity);
        }

        @Test
        @DisplayName("should return null when userID is null")
        void getHoldingNullUserID() {
            assertNull(tradeService.getHolding(null, 1L));
        }

        @Test
        @DisplayName("should return null when holdingId is null")
        void getHoldingNullHoldingId() {
            assertNull(tradeService.getHolding(TEST_USER, null));
        }

        @Test
        @DisplayName("should return null when holding does not exist")
        void getHoldingNotFound() {
            createTestUser(new BigDecimal("10000"));
            assertNull(tradeService.getHolding(TEST_USER, 99999L));
        }
    }

    @Nested
    @DisplayName("getClosedOrders() tests")
    class GetClosedOrdersTests {

        @Test
        @DisplayName("should return closed orders for user")
        void getClosedOrdersSuccess() {
            // Given
            createTestUser(new BigDecimal("10000"));
            createTestQuote(TEST_SYMBOL, new BigDecimal("100"));
            tradeService.buy(TEST_USER, TEST_SYMBOL, 5);

            // When
            List<Order> closedOrders = tradeService.getClosedOrders(TEST_USER);

            // Then
            assertNotNull(closedOrders);
            assertEquals(1, closedOrders.size());
            assertEquals("closed", closedOrders.get(0).orderStatus);
        }

        @Test
        @DisplayName("should throw BadRequestException when userID is null")
        void getClosedOrdersNullUserID() {
            assertThrows(BadRequestException.class, () -> tradeService.getClosedOrders(null));
        }
    }
}

