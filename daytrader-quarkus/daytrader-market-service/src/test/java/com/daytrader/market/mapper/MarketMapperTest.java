package com.daytrader.market.mapper;

import com.daytrader.common.dto.QuoteDTO;
import com.daytrader.market.entity.Quote;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MarketMapper
 */
@QuarkusTest
class MarketMapperTest {

    @Inject
    MarketMapper mapper;

    @Test
    void testToQuoteDTO() {
        // Given
        Quote quote = new Quote();
        quote.symbol = "AAPL";
        quote.companyName = "Apple Inc.";
        quote.volume = 1000000.0;
        quote.price = new BigDecimal("175.50");
        quote.openPrice = new BigDecimal("173.00");
        quote.lowPrice = new BigDecimal("172.50");
        quote.highPrice = new BigDecimal("176.00");
        quote.priceChange = 2.5;
        quote.createdAt = Instant.now();
        quote.updatedAt = Instant.now();

        // When
        QuoteDTO dto = mapper.toQuoteDTO(quote);

        // Then
        assertNotNull(dto);
        assertEquals("AAPL", dto.getSymbol());
        assertEquals("Apple Inc.", dto.getCompanyName());
        assertEquals(1000000.0, dto.getVolume());
        assertEquals(new BigDecimal("175.50"), dto.getPrice());
        assertEquals(new BigDecimal("173.00"), dto.getOpenPrice());
        assertEquals(new BigDecimal("172.50"), dto.getLowPrice());
        assertEquals(new BigDecimal("176.00"), dto.getHighPrice());
        assertEquals(2.5, dto.getPriceChange());
        assertNotNull(dto.getCreatedAt());
        assertNotNull(dto.getUpdatedAt());
    }

    @Test
    void testToQuote() {
        // Given
        QuoteDTO dto = new QuoteDTO();
        dto.setSymbol("GOOGL");
        dto.setCompanyName("Alphabet Inc.");
        dto.setVolume(2000000.0);
        dto.setPrice(new BigDecimal("140.00"));
        dto.setOpenPrice(new BigDecimal("138.00"));
        dto.setLowPrice(new BigDecimal("137.50"));
        dto.setHighPrice(new BigDecimal("141.00"));
        dto.setPriceChange(2.0);

        // When
        Quote quote = mapper.toQuote(dto);

        // Then
        assertNotNull(quote);
        assertEquals("GOOGL", quote.symbol);
        assertEquals("Alphabet Inc.", quote.companyName);
        assertEquals(2000000.0, quote.volume);
        assertEquals(new BigDecimal("140.00"), quote.price);
        assertEquals(new BigDecimal("138.00"), quote.openPrice);
        assertEquals(new BigDecimal("137.50"), quote.lowPrice);
        assertEquals(new BigDecimal("141.00"), quote.highPrice);
        assertEquals(2.0, quote.priceChange);
    }

    @Test
    void testToQuoteDTO_NullInput() {
        QuoteDTO dto = mapper.toQuoteDTO(null);
        assertNull(dto);
    }

    @Test
    void testToQuote_NullInput() {
        Quote quote = mapper.toQuote(null);
        assertNull(quote);
    }
}

