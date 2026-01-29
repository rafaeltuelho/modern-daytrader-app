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

import com.ibm.websphere.samples.daytrader.dto.QuoteDTO;
import com.ibm.websphere.samples.daytrader.service.TradeService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
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
 * REST resource for quote operations
 * Per Phase 3: Backend Migration specification section 4
 * 
 * Exposes endpoints under /api/v1/quotes
 */
@Path("/quotes")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Quotes", description = "Stock quote operations")
public class QuoteResource {

    @Inject
    TradeService tradeService;

    @GET
    @Path("/{symbol}")
    @Operation(summary = "Get quote by symbol", description = "Retrieves the current quote for a given stock symbol")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Quote found",
            content = @Content(schema = @Schema(implementation = QuoteDTO.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "Quote not found"
        )
    })
    public Response getQuote(@PathParam("symbol") String symbol) {
        try {
            QuoteDTO quote = tradeService.getQuote(symbol);
            return Response.ok(quote).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Operation(summary = "Get all quotes", description = "Retrieves all available stock quotes")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "List of quotes",
            content = @Content(schema = @Schema(implementation = QuoteDTO.class))
        )
    })
    public Response getAllQuotes() {
        List<QuoteDTO> quotes = tradeService.getAllQuotes();
        return Response.ok(quotes).build();
    }

    /**
     * Simple error response DTO
     */
    public static class ErrorResponse {
        private String message;

        public ErrorResponse() {
        }

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

