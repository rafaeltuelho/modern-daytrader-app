package com.daytrader.market.service;

import com.daytrader.common.dto.QuoteDTO;
import com.daytrader.common.event.QuoteUpdatedEvent;
import com.daytrader.common.exception.ResourceNotFoundException;
import com.daytrader.market.entity.Quote;
import com.daytrader.market.mapper.MarketMapper;
import com.daytrader.market.messaging.QuoteEventProducer;
import com.daytrader.market.repository.QuoteRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Quote operations
 */
@ApplicationScoped
public class QuoteService {

    @Inject
    QuoteRepository quoteRepository;

    @Inject
    MarketMapper mapper;

    @Inject
    QuoteEventProducer quoteEventProducer;

    /**
     * Get quote by symbol
     */
    public QuoteDTO getQuote(String symbol) {
        Quote quote = quoteRepository.findBySymbol(symbol);
        if (quote == null) {
            throw new ResourceNotFoundException("Quote not found for symbol: " + symbol);
        }
        return mapper.toQuoteDTO(quote);
    }

    /**
     * List all quotes
     */
    public List<QuoteDTO> listQuotes() {
        return quoteRepository.findAllOrdered()
                .stream()
                .map(mapper::toQuoteDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create or update a quote
     */
    @Transactional
    public QuoteDTO saveQuote(QuoteDTO quoteDTO) {
        Quote quote = mapper.toQuote(quoteDTO);
        quoteRepository.persist(quote);
        return mapper.toQuoteDTO(quote);
    }

    /**
     * Update quote price
     */
    @Transactional
    public QuoteDTO updateQuotePrice(String symbol, java.math.BigDecimal newPrice) {
        Quote quote = quoteRepository.findBySymbol(symbol);
        if (quote == null) {
            throw new ResourceNotFoundException("Quote not found for symbol: " + symbol);
        }

        java.math.BigDecimal oldPrice = quote.price;
        quote.price = newPrice;
        quote.priceChange = newPrice.subtract(oldPrice).doubleValue();

        quoteRepository.persist(quote);

        // Emit QuoteUpdatedEvent to Kafka
        QuoteUpdatedEvent event = new QuoteUpdatedEvent(
            quote.symbol,
            quote.price,
            quote.priceChange,
            quote.volume,
            Instant.now()
        );
        quoteEventProducer.emitQuoteUpdated(event);

        return mapper.toQuoteDTO(quote);
    }

    /**
     * Get top gainers
     */
    public List<QuoteDTO> getTopGainers(int limit) {
        return quoteRepository.findTopGainers(limit)
                .stream()
                .map(mapper::toQuoteDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get top losers
     */
    public List<QuoteDTO> getTopLosers(int limit) {
        return quoteRepository.findTopLosers(limit)
                .stream()
                .map(mapper::toQuoteDTO)
                .collect(Collectors.toList());
    }

    /**
     * Calculate gain percentage from open price
     */
    public BigDecimal calculateGainPercent(BigDecimal currentPrice, BigDecimal openPrice) {
        if (openPrice == null || openPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal change = currentPrice.subtract(openPrice);
        return change.divide(openPrice, 4, RoundingMode.HALF_UP)
                     .multiply(new BigDecimal("100"))
                     .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Determine market status based on current time (US Eastern Time)
     */
    public String getMarketStatus() {
        // Get current time in US Eastern Time
        LocalTime now = LocalTime.now(ZoneId.of("America/New_York"));

        // Market hours: 9:30 AM - 4:00 PM ET
        LocalTime marketOpen = LocalTime.of(9, 30);
        LocalTime marketClose = LocalTime.of(16, 0);
        LocalTime preMarketStart = LocalTime.of(4, 0);
        LocalTime afterHoursEnd = LocalTime.of(20, 0);

        if (now.isAfter(marketOpen) && now.isBefore(marketClose)) {
            return "OPEN";
        } else if (now.isAfter(preMarketStart) && now.isBefore(marketOpen)) {
            return "PRE_MARKET";
        } else if (now.isAfter(marketClose) && now.isBefore(afterHoursEnd)) {
            return "AFTER_HOURS";
        } else {
            return "CLOSED";
        }
    }
}

