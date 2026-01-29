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

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.ibm.websphere.samples.daytrader.dto.MarketSummaryDTO;
import com.ibm.websphere.samples.daytrader.dto.QuoteDTO;
import com.ibm.websphere.samples.daytrader.entity.Quote;
import com.ibm.websphere.samples.daytrader.repository.QuoteRepository;
import com.ibm.websphere.samples.daytrader.util.FinancialUtils;

import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

/**
 * Market Summary Service - replaces MarketSummarySingleton
 * Per Phase 2: Market Summary & Profiles specification
 * 
 * This is an @ApplicationScoped CDI bean that caches market summary data
 * and refreshes it every 20 seconds using Quarkus Scheduler.
 */
@ApplicationScoped
public class MarketSummaryService {

    private static final Logger LOG = Logger.getLogger(MarketSummaryService.class);
    private static final int TOP_N = 5;

    @Inject
    QuoteRepository quoteRepository;

    private volatile MarketSummaryDTO cachedSummary;

    @PostConstruct
    void init() {
        LOG.info("MarketSummaryService initialized");
        refreshMarketSummary();
    }

    /**
     * Refresh market summary cache every 20 seconds
     * Matches legacy MarketSummarySingleton behavior
     */
    @Scheduled(every = "20s")
    public void refreshMarketSummary() {
        LOG.debug("Refreshing market summary");

        try {
            // Get all quotes ordered by change (descending)
            List<Quote> quotes = quoteRepository.findAllQuotesOrderedByChange();

            if (quotes.isEmpty()) {
                LOG.warn("No quotes found in database - market summary not updated");
                return;
            }

            // Calculate market statistics
            BigDecimal TSIA = FinancialUtils.ZERO;
            BigDecimal openTSIA = FinancialUtils.ZERO;
            double totalVolume = 0.0;

            // Calculate totals
            for (Quote quote : quotes) {
                TSIA = TSIA.add(quote.getPrice());
                openTSIA = openTSIA.add(quote.getOpen());
                totalVolume += quote.getVolume();
            }

            // Calculate averages
            int quoteCount = quotes.size();
            TSIA = TSIA.divide(new BigDecimal(quoteCount), FinancialUtils.SCALE, FinancialUtils.ROUND);
            openTSIA = openTSIA.divide(new BigDecimal(quoteCount), FinancialUtils.SCALE, FinancialUtils.ROUND);

            // Get top gainers (first N quotes with highest change)
            List<QuoteDTO> topGainers = quotes.stream()
                    .limit(Math.min(TOP_N, quoteCount))
                    .map(QuoteDTO::new)
                    .collect(Collectors.toList());

            // Get top losers (last N quotes with lowest change)
            List<QuoteDTO> topLosers = quotes.stream()
                    .skip(Math.max(0, quoteCount - TOP_N))
                    .map(QuoteDTO::new)
                    .collect(Collectors.toList());

            // Create new market summary
            cachedSummary = new MarketSummaryDTO(TSIA, openTSIA, totalVolume, topGainers, topLosers);

            LOG.debugf("Market summary refreshed: TSIA=%s, openTSIA=%s, volume=%s", TSIA, openTSIA, totalVolume);

        } catch (Exception e) {
            LOG.error("Failed to refresh market summary", e);
        }
    }

    /**
     * Get cached market summary
     * Thread-safe read of volatile field
     */
    public MarketSummaryDTO getMarketSummary() {
        MarketSummaryDTO summary = cachedSummary;
        if (summary == null) {
            LOG.warn("Market summary not yet initialized - refreshing now");
            refreshMarketSummary();
            summary = cachedSummary;
        }
        return summary;
    }
}

