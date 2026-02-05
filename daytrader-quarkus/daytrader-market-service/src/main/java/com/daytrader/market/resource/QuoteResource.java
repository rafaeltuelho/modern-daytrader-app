package com.daytrader.market.resource;

import com.daytrader.common.dto.QuoteDTO;
import com.daytrader.market.service.QuoteService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

/**
 * REST Resource for Quote operations
 */
@Path("/api/quotes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Quotes", description = "Stock quote operations")
public class QuoteResource {

    @Inject
    QuoteService quoteService;

    @GET
    @Path("/{symbol}")
    @Operation(summary = "Get quote by symbol", description = "Returns the current quote for a specific stock symbol")
    public QuoteDTO getQuote(@PathParam("symbol") String symbol) {
        return quoteService.getQuote(symbol);
    }

    @GET
    @Operation(summary = "List all quotes", description = "Returns all available stock quotes")
    public List<QuoteDTO> listQuotes() {
        return quoteService.listQuotes();
    }

    @POST
    @Operation(summary = "Create or update quote", description = "Creates a new quote or updates an existing one")
    public Response saveQuote(@Valid QuoteDTO quoteDTO) {
        QuoteDTO saved = quoteService.saveQuote(quoteDTO);
        return Response.ok(saved).build();
    }

    @PUT
    @Path("/{symbol}/price")
    @Operation(summary = "Update quote price", description = "Updates the price of a specific quote")
    public QuoteDTO updatePrice(
            @PathParam("symbol") String symbol,
            @QueryParam("price") java.math.BigDecimal price) {
        return quoteService.updateQuotePrice(symbol, price);
    }
}

