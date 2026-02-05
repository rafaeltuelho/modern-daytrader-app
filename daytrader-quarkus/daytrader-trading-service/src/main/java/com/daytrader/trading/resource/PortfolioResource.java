package com.daytrader.trading.resource;

import com.daytrader.common.dto.PortfolioSummaryResponse;
import com.daytrader.trading.service.PortfolioService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.math.BigDecimal;

/**
 * REST Resource for Portfolio operations
 * Implements /api/portfolio/summary endpoint per api-spec-trading.md
 */
@Path("/api/portfolio")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Portfolio", description = "Portfolio summary and analytics")
public class PortfolioResource {

    private static final Logger LOG = Logger.getLogger(PortfolioResource.class);

    @Inject
    PortfolioService portfolioService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/summary")
    @Operation(
        summary = "Get portfolio summary",
        description = "Returns a summary of the user's portfolio including total value, " +
                     "total gain/loss, number of holdings, and recent orders."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Portfolio summary",
            content = @Content(schema = @Schema(implementation = PortfolioSummaryResponse.class))
        ),
        @APIResponse(responseCode = "401", description = "Missing or invalid authentication token"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getPortfolioSummary(
        @QueryParam("accountId") Long accountId,
        @QueryParam("cashBalance") BigDecimal cashBalance
    ) {
        // Extract userId from JWT token for future use
        String userId = jwt.getSubject();
        LOG.debugf("Getting portfolio summary for user: %s, accountId: %d", userId, accountId);

        // For now, use query parameters
        // TODO: In production, fetch account details from Account Service using userId
        if (accountId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"accountId is required\"}")
                .build();
        }

        if (cashBalance == null) {
            cashBalance = BigDecimal.ZERO;
        }

        PortfolioSummaryResponse summary = portfolioService.getPortfolioSummary(accountId, cashBalance);
        return Response.ok(summary).build();
    }
}

