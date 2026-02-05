package com.daytrader.trading.service;

import com.daytrader.common.dto.HoldingDTO;
import com.daytrader.common.dto.OrderDTO;
import com.daytrader.common.dto.PortfolioSummaryResponse;
import com.daytrader.trading.repository.HoldingRepository;
import com.daytrader.trading.repository.OrderRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PortfolioService
 */
@QuarkusTest
class PortfolioServiceTest {

    @Inject
    PortfolioService portfolioService;

    @Inject
    HoldingService holdingService;

    @Inject
    OrderService orderService;

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
    }

    @AfterEach
    @Transactional
    void tearDown() {
        cleanupTestData();
    }

    @Transactional
    void cleanupTestData() {
        // Delete all test data
        orderRepository.deleteAll();
        holdingRepository.deleteAll();
    }

    @Test
    void testGetPortfolioSummary_Empty() {
        BigDecimal cashBalance = new BigDecimal("10000.00");

        PortfolioSummaryResponse summary = portfolioService.getPortfolioSummary(testAccountId, cashBalance);

        assertNotNull(summary);
        assertEquals(testAccountId, summary.accountId());
        assertEquals(cashBalance, summary.cashBalance());
        assertEquals(BigDecimal.ZERO, summary.holdingsValue());
        assertEquals(cashBalance, summary.totalValue());
        assertEquals(BigDecimal.ZERO, summary.totalGain());
        assertEquals(0.0, summary.totalGainPercent());
        assertEquals(0, summary.holdingsCount());
        assertTrue(summary.recentOrders().isEmpty());
        assertTrue(summary.topHoldings().isEmpty());
    }

    @Test
    void testGetPortfolioSummary_WithHoldings() {
        BigDecimal cashBalance = new BigDecimal("5000.00");

        // Create holdings
        HoldingDTO holding1 = new HoldingDTO();
        holding1.setAccountId(testAccountId);
        holding1.setSymbol("IBM");
        holding1.setQuantity(100.0);
        holding1.setPurchasePrice(new BigDecimal("150.00"));
        holding1.setPurchaseDate(Instant.now());
        holdingService.createHolding(holding1);

        HoldingDTO holding2 = new HoldingDTO();
        holding2.setAccountId(testAccountId);
        holding2.setSymbol("AAPL");
        holding2.setQuantity(50.0);
        holding2.setPurchasePrice(new BigDecimal("175.00"));
        holding2.setPurchaseDate(Instant.now());
        holdingService.createHolding(holding2);

        PortfolioSummaryResponse summary = portfolioService.getPortfolioSummary(testAccountId, cashBalance);

        assertNotNull(summary);
        assertEquals(testAccountId, summary.accountId());
        assertEquals(cashBalance, summary.cashBalance());

        // Holdings value = (100 * 150) + (50 * 175) = 15000 + 8750 = 23750
        BigDecimal expectedHoldingsValue = new BigDecimal("23750.00");
        assertEquals(0, expectedHoldingsValue.compareTo(summary.holdingsValue()));

        // Total value = cash + holdings = 5000 + 23750 = 28750
        BigDecimal expectedTotalValue = new BigDecimal("28750.00");
        assertEquals(0, expectedTotalValue.compareTo(summary.totalValue()));

        // Total gain = 0 (using purchase price as current price)
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.totalGain()));
        assertEquals(0.0, summary.totalGainPercent());

        assertEquals(2, summary.holdingsCount());
        assertEquals(2, summary.topHoldings().size());
    }

    @Test
    void testGetPortfolioSummary_WithOrders() {
        BigDecimal cashBalance = new BigDecimal("10000.00");

        // Create orders
        for (int i = 0; i < 3; i++) {
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setOrderType("buy");
            orderDTO.setAccountId(testAccountId);
            orderDTO.setSymbol("STOCK" + i);
            orderDTO.setQuantity(100.0);
            orderDTO.setPrice(new BigDecimal("100.00"));
            orderService.createOrder(orderDTO);
        }

        PortfolioSummaryResponse summary = portfolioService.getPortfolioSummary(testAccountId, cashBalance);

        assertNotNull(summary);
        assertEquals(3, summary.recentOrders().size());
    }

    @Test
    void testGetPortfolioSummary_TopHoldings() {
        BigDecimal cashBalance = new BigDecimal("10000.00");

        // Create 7 holdings with different values
        String[] symbols = {"IBM", "AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "NFLX"};
        BigDecimal[] prices = {
            new BigDecimal("150.00"),
            new BigDecimal("175.00"),
            new BigDecimal("300.00"),
            new BigDecimal("2800.00"),
            new BigDecimal("3300.00"),
            new BigDecimal("700.00"),
            new BigDecimal("500.00")
        };

        for (int i = 0; i < symbols.length; i++) {
            HoldingDTO holdingDTO = new HoldingDTO();
            holdingDTO.setAccountId(testAccountId);
            holdingDTO.setSymbol(symbols[i]);
            holdingDTO.setQuantity(10.0);
            holdingDTO.setPurchasePrice(prices[i]);
            holdingDTO.setPurchaseDate(Instant.now());
            holdingService.createHolding(holdingDTO);
        }

        PortfolioSummaryResponse summary = portfolioService.getPortfolioSummary(testAccountId, cashBalance);

        assertNotNull(summary);
        assertEquals(7, summary.holdingsCount());
        // Should return only top 5 holdings
        assertEquals(5, summary.topHoldings().size());
        
        // Top holding should be AMZN (10 * 3300 = 33000)
        assertEquals("AMZN", summary.topHoldings().get(0).getSymbol());
    }
}

