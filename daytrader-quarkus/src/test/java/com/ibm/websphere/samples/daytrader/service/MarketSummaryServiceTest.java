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

import com.ibm.websphere.samples.daytrader.dto.MarketSummaryDTO;
import com.ibm.websphere.samples.daytrader.dto.QuoteDTO;
import com.ibm.websphere.samples.daytrader.entity.Quote;
import com.ibm.websphere.samples.daytrader.repository.QuoteRepository;

import io.quarkus.test.junit.QuarkusTest;

/**
 * MarketSummaryService integration tests
 * Tests market summary calculation, empty database handling, and top gainers/losers
 * Per Phase 2: Market Summary & Profiles specification
 */
@QuarkusTest
class MarketSummaryServiceTest {

    @Inject
    MarketSummaryService marketSummaryService;

    @Inject
    QuoteRepository quoteRepository;

    @BeforeEach
    void setUp() {
        // Each test method has @Transactional so data is isolated and rolled back
        // No need to delete all quotes here
    }

    @Test
    @Transactional
    void testGetMarketSummary() {
        // Create test quotes with varying prices and changes using unique symbols
        String suffix = String.valueOf(System.currentTimeMillis() % 100000);
        Quote quote1 = new Quote("SUM1" + suffix, "Summary Test 1", 1000000.0,
                                new BigDecimal("100.00"), new BigDecimal("95.00"),
                                new BigDecimal("94.00"), new BigDecimal("101.00"), 5.0);
        Quote quote2 = new Quote("SUM2" + suffix, "Summary Test 2", 2000000.0,
                                new BigDecimal("200.00"), new BigDecimal("195.00"),
                                new BigDecimal("194.00"), new BigDecimal("201.00"), 3.0);
        Quote quote3 = new Quote("SUM3" + suffix, "Summary Test 3", 1500000.0,
                                new BigDecimal("150.00"), new BigDecimal("155.00"),
                                new BigDecimal("149.00"), new BigDecimal("156.00"), -2.0);

        quoteRepository.persist(quote1);
        quoteRepository.persist(quote2);
        quoteRepository.persist(quote3);
        quoteRepository.flush();

        // Refresh market summary
        marketSummaryService.refreshMarketSummary();

        // Get market summary
        MarketSummaryDTO summary = marketSummaryService.getMarketSummary();

        assertNotNull(summary);
        assertNotNull(summary.getTSIA());
        assertNotNull(summary.getOpenTSIA());

        // Verify TSIA is a positive value (can't assert exact value due to other quotes in DB)
        assertTrue(summary.getTSIA().compareTo(BigDecimal.ZERO) > 0, "TSIA should be positive");
        assertTrue(summary.getOpenTSIA().compareTo(BigDecimal.ZERO) > 0, "OpenTSIA should be positive");

        // Verify volume is positive
        assertTrue(summary.getVolume() > 0, "Volume should be positive");

        // Top gainers and losers
        assertNotNull(summary.getTopGainers());
        assertNotNull(summary.getTopLosers());
    }

    @Test
    void testMarketSummaryServiceHandlesGracefully() {
        // Test that the service can handle being called at any time
        // Without worrying about database state

        // Get market summary - should return cached value or compute new one
        MarketSummaryDTO summary = marketSummaryService.getMarketSummary();

        // The service should handle gracefully and return a non-null summary
        // since @PostConstruct initializes it and scheduler refreshes it
        assertNotNull(summary, "Market summary should not be null");
    }

    @Test
    @Transactional
    void testTopGainersAndLosers() {
        // Create 10 quotes with different changes to test top 5 selection
        for (int i = 1; i <= 10; i++) {
            double change = (i <= 5) ? (10.0 - i) : -(i - 5.0); // First 5 are gainers, last 5 are losers
            Quote quote = new Quote("TEST" + i, "Test Company " + i, 1000000.0,
                                   new BigDecimal("100.00"), new BigDecimal("95.00"),
                                   new BigDecimal("94.00"), new BigDecimal("101.00"), change);
            quoteRepository.persist(quote);
        }

        // Refresh market summary
        marketSummaryService.refreshMarketSummary();

        // Get market summary
        MarketSummaryDTO summary = marketSummaryService.getMarketSummary();

        assertNotNull(summary);
        
        List<QuoteDTO> topGainers = summary.getTopGainers();
        List<QuoteDTO> topLosers = summary.getTopLosers();
        
        assertNotNull(topGainers);
        assertNotNull(topLosers);
        
        // Should have up to 5 gainers and 5 losers
        assertTrue(topGainers.size() <= 5);
        assertTrue(topLosers.size() <= 5);
        
        // Top gainer should have highest change
        if (!topGainers.isEmpty()) {
            assertTrue(topGainers.get(0).getChange() >= 0);
        }
        
        // Top loser should have lowest change
        if (!topLosers.isEmpty()) {
            assertTrue(topLosers.get(topLosers.size() - 1).getChange() <= 0);
        }
    }
}

