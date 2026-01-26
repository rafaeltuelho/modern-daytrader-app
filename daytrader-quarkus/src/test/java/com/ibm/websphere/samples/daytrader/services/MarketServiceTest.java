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

import com.ibm.websphere.samples.daytrader.dto.MarketSummaryDTO;
import com.ibm.websphere.samples.daytrader.entities.Quote;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@Transactional
@DisplayName("MarketService Unit Tests")
class MarketServiceTest {

    @Inject
    MarketService marketService;

    @BeforeEach
    @Transactional
    void setUp() {
        Quote.deleteAll();
    }

    @Nested
    @DisplayName("getQuote() tests")
    class GetQuoteTests {

        @Test
        @DisplayName("should return quote for existing symbol")
        void getQuoteSuccess() {
            // Given
            marketService.createQuote("AAPL", "Apple Inc.", new BigDecimal("150"));

            // When
            Quote quote = marketService.getQuote("AAPL");

            // Then
            assertNotNull(quote);
            assertEquals("AAPL", quote.symbol);
            assertEquals("Apple Inc.", quote.companyName);
            assertEquals(0, new BigDecimal("150").compareTo(quote.price));
        }

        @Test
        @DisplayName("should throw BadRequestException when symbol is null")
        void getQuoteNullSymbol() {
            assertThrows(BadRequestException.class, () -> marketService.getQuote(null));
        }

        @Test
        @DisplayName("should throw BadRequestException when symbol is blank")
        void getQuoteBlankSymbol() {
            assertThrows(BadRequestException.class, () -> marketService.getQuote("  "));
        }

        @Test
        @DisplayName("should throw NotFoundException when symbol does not exist")
        void getQuoteNotFound() {
            assertThrows(NotFoundException.class, () -> marketService.getQuote("INVALID"));
        }
    }

    @Nested
    @DisplayName("getAllQuotes() tests")
    class GetAllQuotesTests {

        @Test
        @DisplayName("should return all quotes")
        void getAllQuotesSuccess() {
            // Given
            marketService.createQuote("AAPL", "Apple Inc.", new BigDecimal("150"));
            marketService.createQuote("GOOGL", "Alphabet Inc.", new BigDecimal("2800"));
            marketService.createQuote("MSFT", "Microsoft Corp.", new BigDecimal("350"));

            // When
            List<Quote> quotes = marketService.getAllQuotes();

            // Then
            assertNotNull(quotes);
            assertEquals(3, quotes.size());
        }

        @Test
        @DisplayName("should return empty list when no quotes exist")
        void getAllQuotesEmpty() {
            // When
            List<Quote> quotes = marketService.getAllQuotes();

            // Then
            assertNotNull(quotes);
            assertTrue(quotes.isEmpty());
        }
    }

    @Nested
    @DisplayName("getMarketSummary() tests")
    class GetMarketSummaryTests {

        @Test
        @DisplayName("should return market summary with quotes")
        void getMarketSummarySuccess() {
            // Given
            marketService.createQuote("AAPL", "Apple", new BigDecimal("150"));
            marketService.createQuote("GOOGL", "Google", new BigDecimal("100"));

            // When
            MarketSummaryDTO summary = marketService.getMarketSummary();

            // Then
            assertNotNull(summary);
            assertNotNull(summary.tsia());
            assertNotNull(summary.topGainers());
            assertNotNull(summary.topLosers());
        }

        @Test
        @DisplayName("should return empty summary when no quotes exist")
        void getMarketSummaryEmpty() {
            // When
            MarketSummaryDTO summary = marketService.getMarketSummary();

            // Then
            assertNotNull(summary);
            assertEquals(BigDecimal.ZERO, summary.tsia());
            assertEquals(0, summary.volume());
            assertTrue(summary.topGainers().isEmpty());
            assertTrue(summary.topLosers().isEmpty());
        }
    }

    @Nested
    @DisplayName("createQuote() tests")
    class CreateQuoteTests {

        @Test
        @DisplayName("should create quote successfully")
        void createQuoteSuccess() {
            // When
            Quote quote = marketService.createQuote("AAPL", "Apple Inc.", new BigDecimal("150"));

            // Then
            assertNotNull(quote);
            assertNotNull(quote.symbol);
            assertEquals("AAPL", quote.symbol);
            assertEquals("Apple Inc.", quote.companyName);
            assertEquals(new BigDecimal("150"), quote.price);
        }

        @Test
        @DisplayName("should throw BadRequestException when symbol is null")
        void createQuoteNullSymbol() {
            assertThrows(BadRequestException.class,
                    () -> marketService.createQuote(null, "Company", new BigDecimal("100")));
        }

        @Test
        @DisplayName("should throw BadRequestException when symbol is blank")
        void createQuoteBlankSymbol() {
            assertThrows(BadRequestException.class,
                    () -> marketService.createQuote("  ", "Company", new BigDecimal("100")));
        }

        @Test
        @DisplayName("should throw BadRequestException when price is null")
        void createQuoteNullPrice() {
            assertThrows(BadRequestException.class,
                    () -> marketService.createQuote("AAPL", "Apple", null));
        }

        @Test
        @DisplayName("should throw BadRequestException when price is zero or negative")
        void createQuoteInvalidPrice() {
            assertThrows(BadRequestException.class,
                    () -> marketService.createQuote("AAPL", "Apple", BigDecimal.ZERO));
            assertThrows(BadRequestException.class,
                    () -> marketService.createQuote("AAPL", "Apple", new BigDecimal("-10")));
        }

        @Test
        @DisplayName("should throw BadRequestException when quote already exists")
        void createQuoteDuplicate() {
            // Given
            marketService.createQuote("AAPL", "Apple Inc.", new BigDecimal("150"));

            // When/Then
            assertThrows(BadRequestException.class,
                    () -> marketService.createQuote("AAPL", "Different Name", new BigDecimal("200")));
        }
    }

    @Nested
    @DisplayName("updateQuotePriceVolume() tests")
    class UpdateQuotePriceVolumeTests {

        @Test
        @DisplayName("should update quote price and volume")
        void updateQuotePriceVolumeSuccess() {
            // Given
            marketService.createQuote("AAPL", "Apple Inc.", new BigDecimal("150"));

            // When
            Quote updated = marketService.updateQuotePriceVolume("AAPL", new BigDecimal("160"), 100);

            // Then
            assertNotNull(updated);
            assertEquals(new BigDecimal("160"), updated.price);
            assertEquals(100.0, updated.volume, 0.01);
        }

        @Test
        @DisplayName("should throw BadRequestException when symbol is null")
        void updateQuoteNullSymbol() {
            assertThrows(BadRequestException.class,
                    () -> marketService.updateQuotePriceVolume(null, new BigDecimal("100"), 10));
        }

        @Test
        @DisplayName("should throw BadRequestException when newPrice is null")
        void updateQuoteNullPrice() {
            marketService.createQuote("AAPL", "Apple", new BigDecimal("150"));
            assertThrows(BadRequestException.class,
                    () -> marketService.updateQuotePriceVolume("AAPL", null, 10));
        }

        @Test
        @DisplayName("should throw NotFoundException when symbol does not exist")
        void updateQuoteNotFound() {
            assertThrows(NotFoundException.class,
                    () -> marketService.updateQuotePriceVolume("INVALID", new BigDecimal("100"), 10));
        }

        @Test
        @DisplayName("should update high/low when price changes")
        void updateQuotePriceHighLow() {
            // Given
            marketService.createQuote("AAPL", "Apple", new BigDecimal("150"));

            // When - set higher price
            Quote updated = marketService.updateQuotePriceVolume("AAPL", new BigDecimal("200"), 50);

            // Then
            assertEquals(new BigDecimal("200"), updated.high);

            // When - set lower price
            updated = marketService.updateQuotePriceVolume("AAPL", new BigDecimal("100"), 50);

            // Then
            assertEquals(new BigDecimal("100"), updated.low);
        }
    }
}

