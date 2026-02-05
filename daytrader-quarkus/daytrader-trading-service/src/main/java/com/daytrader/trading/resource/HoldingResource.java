package com.daytrader.trading.resource;

import com.daytrader.common.dto.AccountResponse;
import com.daytrader.common.dto.HoldingDTO;
import com.daytrader.trading.client.AccountServiceClient;
import com.daytrader.trading.service.HoldingService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

/**
 * REST Resource for Holding operations
 */
@Path("/api/holdings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Holdings", description = "Portfolio holdings management")
public class HoldingResource {

    @Inject
    HoldingService holdingService;

    @Inject
    JsonWebToken jwt;

    @Inject
    @RestClient
    AccountServiceClient accountServiceClient;

    @Context
    HttpHeaders httpHeaders;

    private String getAuthorizationHeader() {
        return httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION);
    }

    /**
     * Resolves accountId from JWT token by calling Account Service.
     */
    private Long resolveAccountId() {
        String userId = jwt.getSubject();
        String authHeader = getAuthorizationHeader();
        AccountResponse account = accountServiceClient.getAccountByUserId(authHeader, userId);
        return account.id();
    }

    @GET
    @Operation(summary = "List holdings", description = "Returns all stock holdings in the user's portfolio")
    public List<HoldingDTO> listHoldings(
            @QueryParam("symbol") String symbol) {
        Long accountId = resolveAccountId();
        if (symbol != null) {
            return holdingService.listHoldingsBySymbol(accountId, symbol);
        }
        return holdingService.listHoldings(accountId);
    }

    @GET
    @Path("/{holdingId}")
    @Operation(summary = "Get holding details", description = "Retrieves details of a specific holding by ID")
    public HoldingDTO getHolding(
            @PathParam("holdingId") Long holdingId) {
        Long accountId = resolveAccountId();
        return holdingService.getHolding(holdingId, accountId);
    }

    @DELETE
    @Path("/{holdingId}")
    @Operation(summary = "Delete holding", description = "Deletes a holding from the portfolio")
    public Response deleteHolding(
            @PathParam("holdingId") Long holdingId) {
        Long accountId = resolveAccountId();
        holdingService.deleteHolding(holdingId, accountId);
        return Response.noContent().build();
    }
}

