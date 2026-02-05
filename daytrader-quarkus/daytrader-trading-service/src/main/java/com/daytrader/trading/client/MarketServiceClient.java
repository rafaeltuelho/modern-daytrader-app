package com.daytrader.trading.client;

import com.daytrader.common.dto.QuoteDTO;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST Client for Market Service.
 * Used to fetch current quote prices during order processing.
 */
@Path("/api/quotes")
@RegisterRestClient(configKey = "market-service")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface MarketServiceClient {

    /**
     * Get quote by symbol.
     * Market Service GET endpoints are public (no auth required).
     * 
     * @param authorization The Authorization header (can be null for public endpoints)
     * @param symbol The stock symbol
     * @return QuoteDTO with current price and details
     */
    @GET
    @Path("/{symbol}")
    QuoteDTO getQuote(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
                      @PathParam("symbol") String symbol);
}

