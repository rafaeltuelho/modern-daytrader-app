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

import com.ibm.websphere.samples.daytrader.dto.MarketSummaryDTO;
import com.ibm.websphere.samples.daytrader.service.TradeService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST Resource for market data operations
 * Per Phase 2: Market Summary & Profiles specification
 */
@Path("/market")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Market", description = "Market data operations")
public class MarketResource {

    @Inject
    TradeService tradeService;

    @GET
    @Path("/summary")
    @Operation(summary = "Get market summary", 
               description = "Returns current market summary including TSIA, volume, top gainers and losers")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Market summary retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = MarketSummaryDTO.class)
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public Response getMarketSummary() {
        try {
            MarketSummaryDTO summary = tradeService.getMarketSummary();
            
            if (summary == null) {
                return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity("Market summary not available")
                        .build();
            }
            
            return Response.ok(summary).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to retrieve market summary: " + e.getMessage())
                    .build();
        }
    }
}

