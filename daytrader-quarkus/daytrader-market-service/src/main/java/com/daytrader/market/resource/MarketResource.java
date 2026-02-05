package com.daytrader.market.resource;

import com.daytrader.common.dto.MarketSummaryDTO;
import com.daytrader.common.dto.QuoteDTO;
import com.daytrader.market.service.QuoteService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

/**
 * REST Resource for Market operations
 */
@Path("/api/market")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Market Summary", description = "Market overview and statistics")
public class MarketResource {

    @Inject
    QuoteService quoteService;

    @GET
    @Path("/gainers")
    @Operation(summary = "Get top gainers", description = "Returns the top gaining stocks")
    public List<QuoteDTO> getTopGainers(@QueryParam("limit") @DefaultValue("10") int limit) {
        return quoteService.getTopGainers(limit);
    }

    @GET
    @Path("/losers")
    @Operation(summary = "Get top losers", description = "Returns the top losing stocks")
    public List<QuoteDTO> getTopLosers(@QueryParam("limit") @DefaultValue("10") int limit) {
        return quoteService.getTopLosers(limit);
    }

    @GET
    @Path("/summary")
    @Operation(summary = "Get market summary", description = "Returns the current market summary")
    public MarketSummaryDTO getMarketSummary() {
        List<QuoteDTO> topGainers = quoteService.getTopGainers(5);
        List<QuoteDTO> topLosers = quoteService.getTopLosers(5);

        // Calculate TSIA (Trade Stock Index Average)
        java.math.BigDecimal tsia = java.math.BigDecimal.valueOf(10500.00);
        java.math.BigDecimal openTsia = java.math.BigDecimal.valueOf(10450.00);

        // Calculate gain percentage
        java.math.BigDecimal gainPercent = quoteService.calculateGainPercent(tsia, openTsia);

        // Get market status
        String marketStatus = quoteService.getMarketStatus();

        return new MarketSummaryDTO(
            tsia,
            openTsia,
            1000000.0,
            topGainers,
            topLosers,
            java.util.Collections.emptyList(),
            java.time.Instant.now(),
            gainPercent,
            marketStatus,
            topGainers.size(),
            topLosers.size()
        );
    }
}

