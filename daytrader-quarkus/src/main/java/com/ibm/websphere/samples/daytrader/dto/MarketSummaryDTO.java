package com.ibm.websphere.samples.daytrader.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.ibm.websphere.samples.daytrader.entities.Quote;

/**
 * Market summary data transfer object containing market overview information.
 */
@Schema(description = "Market summary information")
public record MarketSummaryDTO(
    @Schema(description = "Dow Jones Industrial Average value")
    BigDecimal tsia,
    
    @Schema(description = "Opening value of the index")
    BigDecimal openTsia,
    
    @Schema(description = "Total trading volume")
    double volume,
    
    @Schema(description = "Top gaining stocks")
    List<Quote> topGainers,
    
    @Schema(description = "Top losing stocks")
    List<Quote> topLosers,
    
    @Schema(description = "Timestamp of the market summary")
    Instant summaryDate
) {
    /**
     * Creates a MarketSummaryDTO with the current timestamp.
     */
    public MarketSummaryDTO(BigDecimal tsia, BigDecimal openTsia, double volume,
                           List<Quote> topGainers, List<Quote> topLosers) {
        this(tsia, openTsia, volume, topGainers, topLosers, Instant.now());
    }
}

