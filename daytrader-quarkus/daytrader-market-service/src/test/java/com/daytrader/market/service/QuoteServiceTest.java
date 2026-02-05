package com.daytrader.market.service;

import com.daytrader.common.dto.QuoteDTO;
import com.daytrader.common.exception.ResourceNotFoundException;
import com.daytrader.market.entity.Quote;
import com.daytrader.market.mapper.MarketMapper;
import com.daytrader.market.messaging.QuoteEventProducer;
import com.daytrader.market.repository.QuoteRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for QuoteService
 */
@QuarkusTest
class QuoteServiceTest {

    @Inject
    QuoteService quoteService;

    @Inject
    QuoteRepository quoteRepository;

    @Inject
    MarketMapper mapper;

    private String testSymbol;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up any existing test data
        cleanupTestData();
        
        // Create test quote
        testSymbol = "TEST";
        Quote quote = new Quote();
        quote.symbol = testSymbol;
        quote.companyName = "Test Company";
        quote.volume = 1000000.0;
        quote.price = new BigDecimal("100.00");
        quote.openPrice = new BigDecimal("98.00");
        quote.lowPrice = new BigDecimal("97.00");
        quote.highPrice = new BigDecimal("102.00");
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
        QuoteDTO result = quoteService.getQuote(testSymbol);
        
        assertNotNull(result);
        assertEquals(testSymbol, result.getSymbol());
        assertEquals("Test Company", result.getCompanyName());
        assertEquals(1000000.0, result.getVolume());
        assertEquals(new BigDecimal("100.00"), result.getPrice());
        assertEquals(2.0, result.getPriceChange());
    }

    @Test
    void testGetQuote_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> {
            quoteService.getQuote("NONEXISTENT");
        });
    }

    @Test
    void testListQuotes() {
        // Create additional quotes
        createTestQuote("AAPL", "Apple Inc.", new BigDecimal("175.00"));
        createTestQuote("GOOGL", "Alphabet Inc.", new BigDecimal("140.00"));
        
        List<QuoteDTO> quotes = quoteService.listQuotes();
        
        assertNotNull(quotes);
        assertTrue(quotes.size() >= 3); // At least TEST, AAPL, GOOGL
    }

    @Test
    void testUpdateQuotePrice() {
        BigDecimal newPrice = new BigDecimal("105.00");
        
        QuoteDTO result = quoteService.updateQuotePrice(testSymbol, newPrice);
        
        assertNotNull(result);
        assertEquals(testSymbol, result.getSymbol());
        assertEquals(newPrice, result.getPrice());
        assertEquals(5.0, result.getPriceChange()); // 105 - 100 = 5
    }

    @Test
    void testUpdateQuotePrice_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> {
            quoteService.updateQuotePrice("NONEXISTENT", new BigDecimal("100.00"));
        });
    }

    @Test
    void testGetTopGainers() {
        // Create quotes with different price changes
        createTestQuoteWithChange("GAIN1", "Gainer 1", new BigDecimal("100.00"), 10.0);
        createTestQuoteWithChange("GAIN2", "Gainer 2", new BigDecimal("200.00"), 20.0);
        createTestQuoteWithChange("LOSS1", "Loser 1", new BigDecimal("50.00"), -5.0);
        
        List<QuoteDTO> gainers = quoteService.getTopGainers(2);
        
        assertNotNull(gainers);
        assertTrue(gainers.size() <= 2);
        // First should be the biggest gainer
        if (gainers.size() > 0) {
            assertTrue(gainers.get(0).getPriceChange() >= 0);
        }
    }

    @Test
    void testGetTopLosers() {
        // Create quotes with different price changes
        createTestQuoteWithChange("GAIN1", "Gainer 1", new BigDecimal("100.00"), 10.0);
        createTestQuoteWithChange("LOSS1", "Loser 1", new BigDecimal("50.00"), -10.0);
        createTestQuoteWithChange("LOSS2", "Loser 2", new BigDecimal("75.00"), -15.0);
        
        List<QuoteDTO> losers = quoteService.getTopLosers(2);
        
        assertNotNull(losers);
        assertTrue(losers.size() <= 2);
        // First should be the biggest loser
        if (losers.size() > 0) {
            assertTrue(losers.get(0).getPriceChange() <= 0);
        }
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

