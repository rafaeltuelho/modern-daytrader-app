package com.ibm.websphere.samples.daytrader.resources;

import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.ibm.websphere.samples.daytrader.dto.BuyRequest;
import com.ibm.websphere.samples.daytrader.entities.Holding;
import com.ibm.websphere.samples.daytrader.entities.Order;
import com.ibm.websphere.samples.daytrader.services.TradeService;

/**
 * REST resource for trading operations.
 * All endpoints require JWT authentication with "user" role.
 */
@Path("/api/trade")
@Tag(name = "Trading", description = "Stock trading operations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
@SecurityRequirement(name = "jwt")
public class TradeResource {

    @Inject
    TradeService tradeService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/buy")
    @Operation(
        summary = "Buy stock",
        description = "Place a buy order for a specified stock symbol and quantity"
    )
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Buy order placed successfully",
            content = @Content(schema = @Schema(implementation = Order.class))),
        @APIResponse(responseCode = "400", description = "Invalid buy request"),
        @APIResponse(responseCode = "401", description = "User not authenticated"),
        @APIResponse(responseCode = "404", description = "Stock symbol not found")
    })
    public Response buy(
        @RequestBody(description = "Buy order details", required = true)
        @Valid BuyRequest request
    ) {
        String userID = jwt.getSubject();

        try {
            Order order = tradeService.buy(userID, request.symbol(), request.quantity());
            return Response.status(Response.Status.CREATED).entity(order).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(java.util.Map.of("error", e.getMessage()))
                .build();
        } catch (jakarta.ws.rs.BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(java.util.Map.of("error", e.getMessage()))
                .build();
        }
    }

    @POST
    @Path("/sell/{holdingId}")
    @Operation(
        summary = "Sell holding",
        description = "Sell a stock holding by its ID"
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Sell order placed successfully",
            content = @Content(schema = @Schema(implementation = Order.class))),
        @APIResponse(responseCode = "401", description = "User not authenticated"),
        @APIResponse(responseCode = "404", description = "Holding not found")
    })
    public Response sell(
        @Parameter(description = "ID of the holding to sell", required = true)
        @PathParam("holdingId") Long holdingId
    ) {
        String userID = jwt.getSubject();

        try {
            Order order = tradeService.sell(userID, holdingId);
            return Response.ok(order).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(java.util.Map.of("error", e.getMessage()))
                .build();
        } catch (jakarta.ws.rs.BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(java.util.Map.of("error", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/orders")
    @Operation(
        summary = "Get user's orders",
        description = "Retrieve all orders for the authenticated user"
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Orders retrieved successfully"),
        @APIResponse(responseCode = "401", description = "User not authenticated")
    })
    public Response getOrders() {
        String userID = jwt.getSubject();
        List<Order> orders = tradeService.getOrders(userID);
        return Response.ok(orders).build();
    }

    @GET
    @Path("/orders/closed")
    @Operation(
        summary = "Get closed orders",
        description = "Retrieve all closed orders for the authenticated user"
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Closed orders retrieved successfully"),
        @APIResponse(responseCode = "401", description = "User not authenticated")
    })
    public Response getClosedOrders() {
        String userID = jwt.getSubject();
        List<Order> closedOrders = tradeService.getClosedOrders(userID);
        return Response.ok(closedOrders).build();
    }

    @GET
    @Path("/holdings")
    @Operation(
        summary = "Get user's holdings",
        description = "Retrieve all stock holdings for the authenticated user"
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Holdings retrieved successfully"),
        @APIResponse(responseCode = "401", description = "User not authenticated")
    })
    public Response getHoldings() {
        String userID = jwt.getSubject();
        List<Holding> holdings = tradeService.getHoldings(userID);
        return Response.ok(holdings).build();
    }

    @GET
    @Path("/holdings/{id}")
    @Operation(
        summary = "Get specific holding",
        description = "Retrieve a specific stock holding by ID"
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Holding retrieved successfully",
            content = @Content(schema = @Schema(implementation = Holding.class))),
        @APIResponse(responseCode = "401", description = "User not authenticated"),
        @APIResponse(responseCode = "404", description = "Holding not found")
    })
    public Response getHolding(
        @Parameter(description = "ID of the holding to retrieve", required = true)
        @PathParam("id") Long holdingId
    ) {
        String userID = jwt.getSubject();

        try {
            Holding holding = tradeService.getHolding(userID, holdingId);
            if (holding == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(java.util.Map.of("error", "Holding not found"))
                    .build();
            }
            return Response.ok(holding).build();
        } catch (jakarta.ws.rs.BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(java.util.Map.of("error", e.getMessage()))
                .build();
        }
    }
}

