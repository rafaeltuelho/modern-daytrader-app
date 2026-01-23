/**
 * (C) Copyright IBM Corporation 2015, 2025.
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
package com.ibm.websphere.samples.daytrader.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.ibm.websphere.samples.daytrader.dto.MarketSummaryDTO;
import com.ibm.websphere.samples.daytrader.entities.Quote;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

/**
 * Market service for quote operations and market summary data.
 */
@ApplicationScoped
public class MarketService {

    private static final int TOP_COUNT = 5;
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    /**
     * Get a quote by its stock symbol.
     *
     * @param symbol the stock symbol to retrieve
     * @return the Quote
     * @throws NotFoundException if quote doesn't exist
     */
    public Quote getQuote(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new BadRequestException("Stock symbol is required");
        }

        Quote quote = Quote.findBySymbol(symbol);
        if (quote == null) {
            throw new NotFoundException("Quote not found for symbol: " + symbol);
        }

        return quote;
    }

    /**
     * Get all quotes in the system.
     *
     * @return List of all quotes
     */
    public List<Quote> getAllQuotes() {
        return Quote.findAllQuotes();
    }

    /**
     * Compute and return a snapshot of current market conditions.
     * Includes TSIA index, volume, top gainers and losers.
     *
     * @return MarketSummaryDTO with market snapshot
     */
    public MarketSummaryDTO getMarketSummary() {
        List<Quote> allQuotes = Quote.findAllQuotes();

        if (allQuotes.isEmpty()) {
            return new MarketSummaryDTO(ZERO, ZERO, 0, new ArrayList<>(), new ArrayList<>());
        }

        // Calculate TSIA (Trade Stock Index Average), openTSIA, and total volume
        BigDecimal tsia = ZERO;
        BigDecimal openTsia = ZERO;
        double totalVolume = 0.0;

        for (Quote quote : allQuotes) {
            if (quote.price != null) {
                tsia = tsia.add(quote.price);
            }
            if (quote.open != null) {
                openTsia = openTsia.add(quote.open);
            }
            totalVolume += quote.volume;
        }

        int count = allQuotes.size();
        tsia = tsia.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
        openTsia = openTsia.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);

        // Sort quotes by change to find top gainers and losers
        List<Quote> sortedByChange = new ArrayList<>(allQuotes);
        sortedByChange.sort(Comparator.comparingDouble(q -> q.change));

        // Top losers are at the beginning (lowest change), top gainers at the end
        List<Quote> topLosers = new ArrayList<>();
        List<Quote> topGainers = new ArrayList<>();

        int losersCount = Math.min(TOP_COUNT, sortedByChange.size());
        for (int i = 0; i < losersCount; i++) {
            topLosers.add(sortedByChange.get(i));
        }

        int gainersCount = Math.min(TOP_COUNT, sortedByChange.size());
        for (int i = sortedByChange.size() - 1; i >= sortedByChange.size() - gainersCount; i--) {
            topGainers.add(sortedByChange.get(i));
        }

        return new MarketSummaryDTO(tsia, openTsia, totalVolume, topGainers, topLosers);
    }

    /**
     * Create a new stock quote.
     *
     * @param symbol      the stock symbol
     * @param companyName the company name
     * @param price       the initial stock price
     * @return the created Quote
     */
    @Transactional
    public Quote createQuote(String symbol, String companyName, BigDecimal price) {
        if (symbol == null || symbol.isBlank()) {
            throw new BadRequestException("Stock symbol is required");
        }
        if (price == null || price.compareTo(ZERO) <= 0) {
            throw new BadRequestException("Price must be greater than zero");
        }

        // Check if quote already exists
        Quote existing = Quote.findBySymbol(symbol);
        if (existing != null) {
            throw new BadRequestException("Quote already exists for symbol: " + symbol);
        }

        Quote quote = new Quote(symbol, companyName, 0, price, price, price, price, 0);
        quote.persist();

        return quote;
    }

    /**
     * Update the stock quote price and volume.
     *
     * @param symbol       the stock symbol to update
     * @param newPrice     the new price
     * @param sharesTraded the number of shares traded
     * @return the updated Quote
     * @throws NotFoundException if quote doesn't exist
     */
    @Transactional
    public Quote updateQuotePriceVolume(String symbol, BigDecimal newPrice, double sharesTraded) {
        if (symbol == null || symbol.isBlank()) {
            throw new BadRequestException("Stock symbol is required");
        }
        if (newPrice == null) {
            throw new BadRequestException("New price is required");
        }

        Quote quote = Quote.findBySymbol(symbol);
        if (quote == null) {
            throw new NotFoundException("Quote not found for symbol: " + symbol);
        }

        BigDecimal oldPrice = quote.price;

        // Update price and calculate change
        quote.price = newPrice;
        if (oldPrice != null && oldPrice.compareTo(ZERO) != 0) {
            BigDecimal change = newPrice.subtract(oldPrice);
            quote.change = change.doubleValue();
        }

        // Update volume
        quote.volume += sharesTraded;

        // Update high/low if needed
        if (quote.high == null || newPrice.compareTo(quote.high) > 0) {
            quote.high = newPrice;
        }
        if (quote.low == null || newPrice.compareTo(quote.low) < 0) {
            quote.low = newPrice;
        }

        return quote;
    }
}

