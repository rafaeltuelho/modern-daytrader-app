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
package com.ibm.websphere.samples.daytrader.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ibm.websphere.samples.daytrader.dto.AccountDTO;
import com.ibm.websphere.samples.daytrader.dto.AccountProfileDTO;
import com.ibm.websphere.samples.daytrader.dto.HoldingDTO;
import com.ibm.websphere.samples.daytrader.dto.MarketSummaryDTO;
import com.ibm.websphere.samples.daytrader.dto.OrderDTO;
import com.ibm.websphere.samples.daytrader.dto.QuoteDTO;
import com.ibm.websphere.samples.daytrader.entity.Account;
import com.ibm.websphere.samples.daytrader.entity.AccountProfile;
import com.ibm.websphere.samples.daytrader.entity.Quote;
import com.ibm.websphere.samples.daytrader.repository.AccountProfileRepository;
import com.ibm.websphere.samples.daytrader.repository.AccountRepository;
import com.ibm.websphere.samples.daytrader.repository.HoldingRepository;
import com.ibm.websphere.samples.daytrader.repository.OrderRepository;
import com.ibm.websphere.samples.daytrader.repository.QuoteRepository;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

import io.quarkus.test.junit.QuarkusTest;

/**
 * TradeService integration tests using @QuarkusTest
 * Tests login, logout, register, getQuote, getAllQuotes, and error handling
 * Per Phase 3: Backend Migration specification section 9 - Testing Strategy
 */
@QuarkusTest
class TradeServiceTest {

    @Inject
    TradeService tradeService;

    @Inject
    AccountRepository accountRepository;

    @Inject
    AccountProfileRepository accountProfileRepository;

    @Inject
    QuoteRepository quoteRepository;

    @Inject
    OrderRepository orderRepository;

    @Inject
    HoldingRepository holdingRepository;

    private String testPassword = "password123";

    private String generateUniqueUserID() {
        return "servicetest" + System.currentTimeMillis() + "_" + Math.random();
    }

    private String generateUniqueSymbol(String prefix) {
        return prefix + System.currentTimeMillis() % 100000;
    }

    @BeforeEach
    void setUp() {
        // Note: Not using @Transactional here to avoid rollback issues
        // Each test method has its own @Transactional annotation
    }

    @Test
    @Transactional
    void testRegister() {
        String testUserID = generateUniqueUserID();
        AccountDTO account = tradeService.register(
            testUserID,
            testPassword,
            "Service Test User",
            "123 Service St",
            "service@example.com",
            "1234-5678-9012-3456",
            new BigDecimal("10000.00")
        );

        assertNotNull(account);
        assertNotNull(account.getAccountID());
        assertEquals(testUserID, account.getProfileID());
        assertEquals(new BigDecimal("10000.00"), account.getBalance());
        assertEquals(new BigDecimal("10000.00"), account.getOpenBalance());
    }

    @Test
    @Transactional
    void testRegisterDuplicateUser() {
        String testUserID = generateUniqueUserID();
        tradeService.register(
            testUserID,
            testPassword,
            "Service Test User",
            "123 Service St",
            "service@example.com",
            "1234-5678-9012-3456",
            new BigDecimal("10000.00")
        );

        assertThrows(IllegalArgumentException.class, () -> {
            tradeService.register(
                testUserID,
                testPassword,
                "Service Test User",
                "123 Service St",
                "service@example.com",
                "1234-5678-9012-3456",
                new BigDecimal("10000.00")
            );
        });
    }

    @Test
    @Transactional
    void testLoginSuccess() {
        String testUserID = generateUniqueUserID();
        tradeService.register(
            testUserID,
            testPassword,
            "Service Test User",
            "123 Service St",
            "service@example.com",
            "1234-5678-9012-3456",
            new BigDecimal("10000.00")
        );

        AccountDTO account = tradeService.login(testUserID, testPassword);

        assertNotNull(account);
        assertEquals(testUserID, account.getProfileID());
        assertEquals(1, account.getLoginCount());
        assertNotNull(account.getLastLogin());
    }

    @Test
    @Transactional
    void testLoginInvalidCredentials() {
        String testUserID = generateUniqueUserID();
        tradeService.register(
            testUserID,
            testPassword,
            "Service Test User",
            "123 Service St",
            "service@example.com",
            "1234-5678-9012-3456",
            new BigDecimal("10000.00")
        );

        assertThrows(IllegalArgumentException.class, () -> {
            tradeService.login(testUserID, "wrongpassword");
        });
    }

    @Test
    @Transactional
    void testLoginNonExistentUser() {
        String testUserID = generateUniqueUserID();
        assertThrows(IllegalArgumentException.class, () -> {
            tradeService.login("nonexistent", "password");
        });
    }

    @Test
    @Transactional
    void testLogout() {
        String testUserID = generateUniqueUserID();
        tradeService.register(
            testUserID,
            testPassword,
            "Service Test User",
            "123 Service St",
            "service@example.com",
            "1234-5678-9012-3456",
            new BigDecimal("10000.00")
        );

        tradeService.logout(testUserID);

        Account account = accountRepository.findByProfileUserID(testUserID).orElseThrow();
        assertEquals(1, account.getLogoutCount());
    }

    @Test
    @Transactional
    void testLogoutNonExistentUser() {
        String testUserID = generateUniqueUserID();
        assertThrows(IllegalArgumentException.class, () -> {
            tradeService.logout("nonexistent");
        });
    }

    @Test
    @Transactional
    void testGetQuote() {
        String testUserID = generateUniqueUserID();
        Quote quote = new Quote("TSLA", "Tesla Inc", 1000000.0,
                               new BigDecimal("250.00"), new BigDecimal("248.00"),
                               new BigDecimal("247.00"), new BigDecimal("252.00"), 2.0);
        quoteRepository.persist(quote);

        QuoteDTO quoteDTO = tradeService.getQuote("TSLA");

        assertNotNull(quoteDTO);
        assertEquals("TSLA", quoteDTO.getSymbol());
        assertEquals("Tesla Inc", quoteDTO.getCompanyName());
        assertEquals(new BigDecimal("250.00"), quoteDTO.getPrice());
    }

    @Test
    @Transactional
    void testGetQuoteNotFound() {
        String testUserID = generateUniqueUserID();
        assertThrows(IllegalArgumentException.class, () -> {
            tradeService.getQuote("NONEXISTENT");
        });
    }

    @Test
    @Transactional
    void testGetAllQuotes() {
        String testUserID = generateUniqueUserID();
        // Use unique symbols to avoid conflicts with other tests
        String symbol1 = "TEST1" + System.currentTimeMillis();
        String symbol2 = "TEST2" + System.currentTimeMillis();

        Quote quote1 = new Quote(symbol1, "Test Company 1", 1000000.0,
                                new BigDecimal("150.00"), new BigDecimal("149.00"),
                                new BigDecimal("148.00"), new BigDecimal("151.00"), 1.0);
        Quote quote2 = new Quote(symbol2, "Test Company 2", 2000000.0,
                                new BigDecimal("140.00"), new BigDecimal("139.00"),
                                new BigDecimal("138.00"), new BigDecimal("141.00"), 1.0);

        quoteRepository.persist(quote1);
        quoteRepository.persist(quote2);

        List<QuoteDTO> quotes = tradeService.getAllQuotes();

        assertNotNull(quotes);
        assertTrue(quotes.size() >= 2);
    }

    @Test
    @Transactional
    void testCreateQuote() {
        String testUserID = generateUniqueUserID();
        QuoteDTO quoteDTO = tradeService.createQuote("MSFT", "Microsoft Corporation", new BigDecimal("380.00"));

        assertNotNull(quoteDTO);
        assertEquals("MSFT", quoteDTO.getSymbol());
        assertEquals("Microsoft Corporation", quoteDTO.getCompanyName());
        assertEquals(new BigDecimal("380.00"), quoteDTO.getPrice());
    }

    @Test
    @Transactional
    void testGetAccountData() {
        String testUserID = generateUniqueUserID();
        AccountDTO registered = tradeService.register(
            testUserID,
            testPassword,
            "Service Test User",
            "123 Service St",
            "service@example.com",
            "1234-5678-9012-3456",
            new BigDecimal("10000.00")
        );

        AccountDTO account = tradeService.getAccountData(registered.getAccountID());

        assertNotNull(account);
        assertEquals(registered.getAccountID(), account.getAccountID());
        assertEquals(testUserID, account.getProfileID());
    }

    @Test
    @Transactional
    void testGetAccountDataNotFound() {
        String testUserID = generateUniqueUserID();
        assertThrows(IllegalArgumentException.class, () -> {
            tradeService.getAccountData(99999);
        });
    }

    // ========== New Trading Operations Tests ==========
    // Tests for Phase 2: Feature Implementation - Core Trading Operations

    @Test
    @Transactional
    void testBuyStock() {
        String testUserID = generateUniqueUserID();
        String testSymbol = generateUniqueSymbol("BUY");

        // Register user with sufficient balance
        AccountDTO account = tradeService.register(
            testUserID,
            testPassword,
            "Test User",
            "123 Test St",
            "test@example.com",
            "1234-5678-9012-3456",
            new BigDecimal("10000.00")
        );

        // Create a quote with unique symbol
        Quote quote = new Quote(testSymbol, "Test Company", 1000000.0,
                               new BigDecimal("150.00"), new BigDecimal("149.00"),
                               new BigDecimal("148.00"), new BigDecimal("151.00"), 1.0);
        quoteRepository.persist(quote);

        BigDecimal initialBalance = account.getBalance();

        // Buy stock
        OrderDTO order = tradeService.buy(testUserID, testSymbol, 10.0, TradeConfig.SYNCH);

        assertNotNull(order);
        assertEquals("buy", order.getOrderType());
        assertEquals("closed", order.getOrderStatus());
        assertEquals(10.0, order.getQuantity());
        assertEquals(0, new BigDecimal("150.00").compareTo(order.getPrice()));
        assertNotNull(order.getOrderID());

        // Verify balance was deducted (use compareTo for BigDecimal)
        AccountDTO updatedAccount = tradeService.getAccountData(account.getAccountID());
        BigDecimal expectedCost = new BigDecimal("10.0").multiply(new BigDecimal("150.00"))
                                 .add(new BigDecimal("24.95")); // order fee
        assertEquals(0, initialBalance.subtract(expectedCost).compareTo(updatedAccount.getBalance()));

        // Verify holding was created
        List<HoldingDTO> holdings = tradeService.getHoldings(testUserID);
        assertEquals(1, holdings.size());
        assertEquals(testSymbol, holdings.get(0).getSymbol());
        assertEquals(10.0, holdings.get(0).getQuantity());
    }

    @Test
    @Transactional
    void testBuyStockInsufficientFunds() {
        String testUserID = generateUniqueUserID();
        String testSymbol = generateUniqueSymbol("INS");

        // Register user with low balance
        tradeService.register(
            testUserID,
            testPassword,
            "Test User",
            "123 Test St",
            "test@example.com",
            "1234-5678-9012-3456",
            new BigDecimal("10.00")
        );

        // Create expensive quote with unique symbol
        Quote quote = new Quote(testSymbol, "Expensive Inc", 1000000.0,
                               new BigDecimal("1000.00"), new BigDecimal("999.00"),
                               new BigDecimal("998.00"), new BigDecimal("1001.00"), 1.0);
        quoteRepository.persist(quote);

        // Attempt to buy - should fail due to insufficient funds
        assertThrows(RuntimeException.class, () -> {
            tradeService.buy(testUserID, testSymbol, 100.0, TradeConfig.SYNCH);
        });
    }

    @Test
    void testSellHolding() {
        // This test does NOT use @Transactional because buy() and sell() each need
        // their own committed transactions for proper entity management
        String testUserID = generateUniqueUserID();
        String testSymbol = generateUniqueSymbol("SEL");

        // Create the quote first (tradeService.createQuote has its own transaction)
        tradeService.createQuote(testSymbol, "Sell Test Inc", new BigDecimal("140.00"));

        // Setup: Register user
        tradeService.register(
            testUserID,
            testPassword,
            "Test User",
            "123 Test St",
            "test@example.com",
            "1234-5678-9012-3456",
            new BigDecimal("10000.00")
        );

        // Buy stock (happens in its own transaction)
        OrderDTO buyOrder = tradeService.buy(testUserID, testSymbol, 5.0, TradeConfig.SYNCH);
        assertNotNull(buyOrder);

        // Get the holding
        List<HoldingDTO> holdings = tradeService.getHoldings(testUserID);
        assertEquals(1, holdings.size());
        Integer holdingID = holdings.get(0).getHoldingID();

        // Sell the holding (happens in its own transaction)
        OrderDTO sellOrder = tradeService.sell(testUserID, holdingID, TradeConfig.SYNCH);

        assertNotNull(sellOrder);
        assertEquals("sell", sellOrder.getOrderType());
        assertEquals("closed", sellOrder.getOrderStatus());
        assertEquals(5.0, sellOrder.getQuantity());

        // Verify holding was removed
        List<HoldingDTO> remainingHoldings = tradeService.getHoldings(testUserID);
        assertEquals(0, remainingHoldings.size());
    }

    @Test
    @Transactional
    void testSellHoldingNotFound() {
        String testUserID = generateUniqueUserID();
        // Register user
        tradeService.register(
            testUserID,
            testPassword,
            "Test User",
            "123 Test St",
            "test@example.com",
            "1234-5678-9012-3456",
            new BigDecimal("10000.00")
        );

        // Try to sell non-existent holding
        OrderDTO order = tradeService.sell(testUserID, 99999, TradeConfig.SYNCH);

        // Should return cancelled order
        assertNotNull(order);
        assertEquals("cancelled", order.getOrderStatus());
    }

    @Test
    @Transactional
    void testCompleteOrder() {
        String testUserID = generateUniqueUserID();
        String testSymbol = generateUniqueSymbol("CMP");

        // Setup: Register user and create a buy order
        tradeService.register(
            testUserID,
            testPassword,
            "Test User",
            "123 Test St",
            "test@example.com",
            "1234-5678-9012-3456",
            new BigDecimal("10000.00")
        );

        Quote quote = new Quote(testSymbol, "Complete Test Corp", 1000000.0,
                               new BigDecimal("380.00"), new BigDecimal("379.00"),
                               new BigDecimal("378.00"), new BigDecimal("381.00"), 1.0);
        quoteRepository.persist(quote);

        // Buy stock (synchronous mode completes automatically)
        OrderDTO order = tradeService.buy(testUserID, testSymbol, 3.0, TradeConfig.SYNCH);

        assertNotNull(order);
        assertEquals("closed", order.getOrderStatus());
        assertNotNull(order.getCompletionDate());
    }

    @Test
    @Transactional
    void testCancelOrder() {
        String testUserID = generateUniqueUserID();
        // Setup: Register user and create a buy order
        tradeService.register(
            testUserID,
            testPassword,
            "Test User",
            "123 Test St",
            "test@example.com",
            "1234-5678-9012-3456",
            new BigDecimal("10000.00")
        );

        Quote quote = new Quote("NVDA", "NVIDIA Corporation", 1000000.0,
                               new BigDecimal("500.00"), new BigDecimal("499.00"),
                               new BigDecimal("498.00"), new BigDecimal("501.00"), 1.0);
        quoteRepository.persist(quote);

        // Buy stock
        OrderDTO order = tradeService.buy(testUserID, "NVDA", 2.0, TradeConfig.SYNCH);
        Integer orderID = order.getOrderID();

        // Cancel the order (even though it's already closed)
        tradeService.cancelOrder(orderID);

        // Verify order was cancelled
        List<OrderDTO> orders = tradeService.getOrders(testUserID);
        OrderDTO cancelledOrder = orders.stream()
                .filter(o -> o.getOrderID().equals(orderID))
                .findFirst()
                .orElse(null);

        assertNotNull(cancelledOrder);
        assertEquals("cancelled", cancelledOrder.getOrderStatus());
    }

    @Test
    @Transactional
    void testGetOrders() {
        String testUserID = generateUniqueUserID();
        String symbol1 = generateUniqueSymbol("ORD1");
        String symbol2 = generateUniqueSymbol("ORD2");

        // Setup: Register user and create multiple orders
        tradeService.register(
            testUserID,
            testPassword,
            "Test User",
            "123 Test St",
            "test@example.com",
            "1234-5678-9012-3456",
            new BigDecimal("10000.00")
        );

        Quote quote1 = new Quote(symbol1, "Order Test Corp 1", 1000000.0,
                                new BigDecimal("150.00"), new BigDecimal("149.00"),
                                new BigDecimal("148.00"), new BigDecimal("151.00"), 1.0);
        Quote quote2 = new Quote(symbol2, "Order Test Corp 2", 1000000.0,
                                new BigDecimal("120.00"), new BigDecimal("119.00"),
                                new BigDecimal("118.00"), new BigDecimal("121.00"), 1.0);
        quoteRepository.persist(quote1);
        quoteRepository.persist(quote2);

        // Create multiple orders
        tradeService.buy(testUserID, symbol1, 5.0, TradeConfig.SYNCH);
        tradeService.buy(testUserID, symbol2, 10.0, TradeConfig.SYNCH);

        // Get all orders
        List<OrderDTO> orders = tradeService.getOrders(testUserID);

        assertNotNull(orders);
        assertEquals(2, orders.size());
    }

    @Test
    @Transactional
    void testGetClosedOrders() {
        String testUserID = generateUniqueUserID();
        // Setup: Register user and create orders
        tradeService.register(
            testUserID,
            testPassword,
            "Test User",
            "123 Test St",
            "test@example.com",
            "1234-5678-9012-3456",
            new BigDecimal("10000.00")
        );

        Quote quote = new Quote("AMD", "Advanced Micro Devices", 1000000.0,
                               new BigDecimal("180.00"), new BigDecimal("179.00"),
                               new BigDecimal("178.00"), new BigDecimal("181.00"), 1.0);
        quoteRepository.persist(quote);

        // Create order (will be closed automatically in SYNCH mode)
        tradeService.buy(testUserID, "AMD", 5.0, TradeConfig.SYNCH);

        // Get closed orders
        List<OrderDTO> closedOrders = tradeService.getClosedOrders(testUserID);

        assertNotNull(closedOrders);
        assertEquals(1, closedOrders.size());
        assertEquals("closed", closedOrders.get(0).getOrderStatus());
    }

    @Test
    @Transactional
    void testGetHoldings() {
        String testUserID = generateUniqueUserID();
        // Setup: Register user and buy multiple stocks
        tradeService.register(
            testUserID,
            testPassword,
            "Test User",
            "123 Test St",
            "test@example.com",
            "1234-5678-9012-3456",
            new BigDecimal("10000.00")
        );

        Quote quote1 = new Quote("INTC", "Intel Corporation", 1000000.0,
                                new BigDecimal("45.00"), new BigDecimal("44.50"),
                                new BigDecimal("44.00"), new BigDecimal("45.50"), 1.0);
        Quote quote2 = new Quote("CSCO", "Cisco Systems", 1000000.0,
                                new BigDecimal("55.00"), new BigDecimal("54.50"),
                                new BigDecimal("54.00"), new BigDecimal("55.50"), 1.0);
        quoteRepository.persist(quote1);
        quoteRepository.persist(quote2);

        // Buy stocks
        tradeService.buy(testUserID, "INTC", 20.0, TradeConfig.SYNCH);
        tradeService.buy(testUserID, "CSCO", 15.0, TradeConfig.SYNCH);

        // Get holdings
        List<HoldingDTO> holdings = tradeService.getHoldings(testUserID);

        assertNotNull(holdings);
        assertEquals(2, holdings.size());
    }

    @Test
    @Transactional
    void testGetHolding() {
        String testUserID = generateUniqueUserID();
        // Setup: Register user and buy stock
        tradeService.register(
            testUserID,
            testPassword,
            "Test User",
            "123 Test St",
            "test@example.com",
            "1234-5678-9012-3456",
            new BigDecimal("10000.00")
        );

        Quote quote = new Quote("META", "Meta Platforms", 1000000.0,
                               new BigDecimal("350.00"), new BigDecimal("349.00"),
                               new BigDecimal("348.00"), new BigDecimal("351.00"), 1.0);
        quoteRepository.persist(quote);

        // Buy stock
        tradeService.buy(testUserID, "META", 8.0, TradeConfig.SYNCH);

        // Get the holding
        List<HoldingDTO> holdings = tradeService.getHoldings(testUserID);
        Integer holdingID = holdings.get(0).getHoldingID();

        // Get single holding
        HoldingDTO holding = tradeService.getHolding(holdingID);

        assertNotNull(holding);
        assertEquals(holdingID, holding.getHoldingID());
        assertEquals("META", holding.getSymbol());
        assertEquals(8.0, holding.getQuantity());
        assertEquals(new BigDecimal("350.00"), holding.getPurchasePrice());
    }

    @Test
    @Transactional
    void testGetAccountProfileData() {
        String testUserID = generateUniqueUserID();
        // Register user
        tradeService.register(
            testUserID,
            testPassword,
            "John Doe",
            "456 Main St",
            "john@example.com",
            "9876-5432-1098-7654",
            new BigDecimal("5000.00")
        );

        // Get profile
        AccountProfileDTO profile = tradeService.getAccountProfileData(testUserID);

        assertNotNull(profile);
        assertEquals(testUserID, profile.getUserID());
        assertEquals("John Doe", profile.getFullName());
        assertEquals("456 Main St", profile.getAddress());
        assertEquals("john@example.com", profile.getEmail());
        assertEquals("9876-5432-1098-7654", profile.getCreditCard());
    }

    @Test
    @Transactional
    void testUpdateAccountProfile() {
        String testUserID = generateUniqueUserID();
        // Register user
        tradeService.register(
            testUserID,
            testPassword,
            "Jane Smith",
            "789 Oak Ave",
            "jane@example.com",
            "1111-2222-3333-4444",
            new BigDecimal("7500.00")
        );

        // Update profile
        AccountProfileDTO updateData = new AccountProfileDTO();
        updateData.setUserID(testUserID);
        updateData.setPassword("newpassword456");
        updateData.setFullName("Jane Smith-Johnson");
        updateData.setAddress("999 Elm Street");
        updateData.setEmail("jane.johnson@example.com");
        updateData.setCreditCard("5555-6666-7777-8888");

        AccountProfileDTO updatedProfile = tradeService.updateAccountProfile(updateData);

        assertNotNull(updatedProfile);
        assertEquals(testUserID, updatedProfile.getUserID());
        assertEquals("Jane Smith-Johnson", updatedProfile.getFullName());
        assertEquals("999 Elm Street", updatedProfile.getAddress());
        assertEquals("jane.johnson@example.com", updatedProfile.getEmail());
        assertEquals("5555-6666-7777-8888", updatedProfile.getCreditCard());
    }

    @Test
    @Transactional
    void testGetMarketSummary() {
        String testUserID = generateUniqueUserID();
        // Create multiple quotes with different changes
        Quote quote1 = new Quote("GAINER1", "Top Gainer 1", 1000000.0,
                                new BigDecimal("100.00"), new BigDecimal("90.00"),
                                new BigDecimal("89.00"), new BigDecimal("101.00"), 10.0);
        Quote quote2 = new Quote("GAINER2", "Top Gainer 2", 2000000.0,
                                new BigDecimal("200.00"), new BigDecimal("190.00"),
                                new BigDecimal("189.00"), new BigDecimal("201.00"), 8.0);
        Quote quote3 = new Quote("LOSER1", "Top Loser 1", 1500000.0,
                                new BigDecimal("50.00"), new BigDecimal("60.00"),
                                new BigDecimal("49.00"), new BigDecimal("61.00"), -10.0);
        Quote quote4 = new Quote("LOSER2", "Top Loser 2", 1800000.0,
                                new BigDecimal("75.00"), new BigDecimal("85.00"),
                                new BigDecimal("74.00"), new BigDecimal("86.00"), -8.0);

        quoteRepository.persist(quote1);
        quoteRepository.persist(quote2);
        quoteRepository.persist(quote3);
        quoteRepository.persist(quote4);

        // Get market summary
        MarketSummaryDTO summary = tradeService.getMarketSummary();

        assertNotNull(summary);
        assertNotNull(summary.getTSIA());
        assertNotNull(summary.getOpenTSIA());
        assertTrue(summary.getVolume() > 0);
        assertNotNull(summary.getTopGainers());
        assertNotNull(summary.getTopLosers());
    }
}


