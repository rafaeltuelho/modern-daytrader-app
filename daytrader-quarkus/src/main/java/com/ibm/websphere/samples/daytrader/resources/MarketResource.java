package com.ibm.websphere.samples.daytrader.resources;

import java.util.List;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.ibm.websphere.samples.daytrader.dto.MarketSummaryDTO;
import com.ibm.websphere.samples.daytrader.entities.Quote;
import com.ibm.websphere.samples.daytrader.services.MarketService;

/**
 * REST resource for market data operations.
 * All endpoints are public (no authentication required) as market data is read-only.
 */
@Path("/api/market")
@Tag(name = "Market", description = "Market data and stock quote endpoints")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class MarketResource {

    @Inject
    MarketService marketService;

    @GET
    @Path("/quotes")
    @Operation(
        summary = "Get all quotes",
        description = "Retrieve all available stock quotes"
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Quotes retrieved successfully")
    })
    public Response getAllQuotes() {
        List<Quote> quotes = marketService.getAllQuotes();
        return Response.ok(quotes).build();
    }

    @GET
    @Path("/quotes/{symbol}")
    @Operation(
        summary = "Get quote by symbol",
        description = "Retrieve a specific stock quote by its symbol"
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Quote retrieved successfully",
            content = @Content(schema = @Schema(implementation = Quote.class))),
        @APIResponse(responseCode = "404", description = "Quote not found")
    })
    public Response getQuote(
        @Parameter(description = "Stock symbol", required = true, example = "s:0")
        @PathParam("symbol") String symbol
    ) {
        try {
            Quote quote = marketService.getQuote(symbol);
            return Response.ok(quote).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(java.util.Map.of("error", "Quote not found for symbol: " + symbol))
                .build();
        } catch (jakarta.ws.rs.BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(java.util.Map.of("error", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/summary")
    @Operation(
        summary = "Get market summary",
        description = "Retrieve market summary including index values and top gainers/losers"
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Market summary retrieved successfully",
            content = @Content(schema = @Schema(implementation = MarketSummaryDTO.class)))
    })
    public Response getMarketSummary() {
        MarketSummaryDTO summary = marketService.getMarketSummary();
        return Response.ok(summary).build();
    }
}

