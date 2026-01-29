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

import com.ibm.websphere.samples.daytrader.dto.OrderDTO;
import com.ibm.websphere.samples.daytrader.service.TradeService;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
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
 * REST resource for order operations
 * Per Phase 2: Feature Implementation - Core Trading Operations
 * 
 * Exposes endpoints under /api/v1/orders
 */
@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Orders", description = "Trading order operations")
public class OrderResource {

    @Inject
    TradeService tradeService;

    @GET
    @Operation(summary = "Get user orders", description = "Retrieves all orders for a user")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "List of orders",
            content = @Content(schema = @Schema(implementation = OrderDTO.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    public Response getOrders(@QueryParam("userID") String userID) {
        if (userID == null || userID.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new QuoteResource.ErrorResponse("userID query parameter is required"))
                    .build();
        }

        try {
            List<OrderDTO> orders = tradeService.getOrders(userID);
            return Response.ok(orders).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new QuoteResource.ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/buy")
    @Operation(summary = "Buy stock", description = "Creates a buy order for stock shares")
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Buy order created",
            content = @Content(schema = @Schema(implementation = OrderDTO.class))
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid request"
        ),
        @APIResponse(
            responseCode = "404",
            description = "User or quote not found"
        )
    })
    public Response buy(BuyRequest request) {
        // Validate required fields
        if (request.userID == null || request.userID.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new QuoteResource.ErrorResponse("userID is required"))
                    .build();
        }
        if (request.symbol == null || request.symbol.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new QuoteResource.ErrorResponse("symbol is required"))
                    .build();
        }
        if (request.quantity <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new QuoteResource.ErrorResponse("quantity must be greater than 0"))
                    .build();
        }

        try {
            int orderProcessingMode = request.orderProcessingMode != null ? 
                    request.orderProcessingMode : TradeConfig.SYNCH;
            OrderDTO order = tradeService.buy(request.userID, request.symbol, 
                    request.quantity, orderProcessingMode);
            return Response.status(Response.Status.CREATED).entity(order).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new QuoteResource.ErrorResponse(e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new QuoteResource.ErrorResponse("Buy operation failed: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/sell")
    @Operation(summary = "Sell holding", description = "Creates a sell order for a holding")
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Sell order created",
            content = @Content(schema = @Schema(implementation = OrderDTO.class))
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid request"
        )
    })
    public Response sell(SellRequest request) {
        // Validate required fields
        if (request.userID == null || request.userID.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new QuoteResource.ErrorResponse("userID is required"))
                    .build();
        }
        if (request.holdingID == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new QuoteResource.ErrorResponse("holdingID is required"))
                    .build();
        }

        try {
            int orderProcessingMode = request.orderProcessingMode != null ?
                    request.orderProcessingMode : TradeConfig.SYNCH;
            OrderDTO order = tradeService.sell(request.userID, request.holdingID, orderProcessingMode);
            return Response.status(Response.Status.CREATED).entity(order).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new QuoteResource.ErrorResponse(e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new QuoteResource.ErrorResponse("Sell operation failed: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Request DTO for buy operation
     */
    public static class BuyRequest {
        public String userID;
        public String symbol;
        public double quantity;
        public Integer orderProcessingMode;
    }

    /**
     * Request DTO for sell operation
     */
    public static class SellRequest {
        public String userID;
        public Integer holdingID;
        public Integer orderProcessingMode;
    }
}

