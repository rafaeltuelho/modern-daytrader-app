/**
 * (C) Copyright IBM Corporation 2024.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.websphere.samples.daytrader.rest;

import java.util.List;

import com.ibm.websphere.samples.daytrader.dto.HoldingDTO;
import com.ibm.websphere.samples.daytrader.service.TradeService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST resource for portfolio operations
 * Per Phase 2: Feature Implementation - Core Trading Operations
 * 
 * Exposes endpoints under /api/v1/portfolio
 */
@Path("/portfolio")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Portfolio", description = "Portfolio and holdings operations")
public class PortfolioResource {

    @Inject
    TradeService tradeService;

    @GET
    @Operation(summary = "Get user portfolio", description = "Retrieves all holdings for a user")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "List of holdings",
            content = @Content(schema = @Schema(implementation = HoldingDTO.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "User not found"
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid request"
        )
    })
    public Response getPortfolio(@QueryParam("userID") String userID) {
        if (userID == null || userID.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new QuoteResource.ErrorResponse("userID query parameter is required"))
                    .build();
        }

        try {
            List<HoldingDTO> holdings = tradeService.getHoldings(userID);
            return Response.ok(holdings).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new QuoteResource.ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{holdingId}")
    @Operation(summary = "Get holding by ID", description = "Retrieves a specific holding by its ID")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Holding found",
            content = @Content(schema = @Schema(implementation = HoldingDTO.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "Holding not found"
        )
    })
    public Response getHolding(@PathParam("holdingId") Integer holdingId) {
        try {
            HoldingDTO holding = tradeService.getHolding(holdingId);
            return Response.ok(holding).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new QuoteResource.ErrorResponse(e.getMessage()))
                    .build();
        }
    }
}

