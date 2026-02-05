package com.daytrader.trading.resource;

import com.daytrader.common.dto.AccountResponse;
import com.daytrader.common.dto.CreateOrderRequest;
import com.daytrader.common.dto.HoldingDTO;
import com.daytrader.common.dto.OrderDTO;
import com.daytrader.trading.client.AccountServiceClient;
import com.daytrader.trading.service.HoldingService;
import com.daytrader.trading.service.OrderService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.net.URI;
import java.util.List;

/**
 * REST Resource for Order operations
 */
@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Orders", description = "Buy and sell order operations")
public class OrderResource {

    private static final Logger LOG = Logger.getLogger(OrderResource.class);

    @Inject
    OrderService orderService;

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

    @POST
    @Operation(summary = "Create a new order", description = "Creates a new buy or sell order")
    public Response createOrder(@Valid CreateOrderRequest request) {
        // Extract userId from JWT token
        String userId = jwt.getSubject();
        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"No valid JWT token provided\"}")
                .build();
        }

        LOG.infof("Creating order for user: %s, request: %s", userId, request);

        // Get account ID from Account Service (forward authorization header)
        String authHeader = getAuthorizationHeader();
        AccountResponse account = accountServiceClient.getAccountByUserId(authHeader, userId);
        Long accountId = account.id();

        LOG.infof("Resolved accountId: %d for userId: %s", accountId, userId);

        // Convert request to OrderDTO with resolved accountId
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderType(request.getOrderType());
        orderDTO.setAccountId(accountId);
        orderDTO.setHoldingId(request.getHoldingId());

        // For sell orders, look up the holding to get the symbol and quantity
        String symbol = request.getSymbol();
        Double quantity = request.getQuantity();

        if ("sell".equalsIgnoreCase(request.getOrderType()) && request.getHoldingId() != null) {
            HoldingDTO holding = holdingService.getHolding(request.getHoldingId(), accountId);
            // Use symbol from holding if not provided in request
            if (symbol == null) {
                symbol = holding.getSymbol();
                LOG.infof("Resolved symbol from holding: %s for holdingId: %d", symbol, request.getHoldingId());
            }
            // Use quantity from holding if not provided in request (sell entire holding)
            if (quantity == null || quantity <= 0) {
                quantity = holding.getQuantity();
                LOG.infof("Resolved quantity from holding: %f for holdingId: %d", quantity, request.getHoldingId());
            }
        }

        orderDTO.setSymbol(symbol);
        orderDTO.setQuantity(quantity != null ? quantity : 0.0);

        OrderDTO created = orderService.createOrder(orderDTO);
        return Response.created(URI.create("/api/orders/" + created.getId()))
                .entity(created)
                .build();
    }

    @GET
    @Operation(summary = "List orders", description = "Returns a list of orders for the authenticated user")
    public List<OrderDTO> listOrders(@QueryParam("status") String status) {
        // Extract userId from JWT and get accountId
        String userId = jwt.getSubject();
        String authHeader = getAuthorizationHeader();
        AccountResponse account = accountServiceClient.getAccountByUserId(authHeader, userId);
        Long accountId = account.id();

        if (status != null) {
            return orderService.listOrdersByStatus(accountId, status);
        }
        return orderService.listOrders(accountId);
    }

    @GET
    @Path("/{orderId}")
    @Operation(summary = "Get order details", description = "Retrieves details of a specific order by ID")
    public OrderDTO getOrder(@PathParam("orderId") Long orderId) {
        // Extract userId from JWT and get accountId
        String userId = jwt.getSubject();
        String authHeader = getAuthorizationHeader();
        AccountResponse account = accountServiceClient.getAccountByUserId(authHeader, userId);
        Long accountId = account.id();

        return orderService.getOrder(orderId, accountId);
    }

    @POST
    @Path("/{orderId}/cancel")
    @Consumes(MediaType.WILDCARD)
    @Operation(summary = "Cancel an order", description = "Cancels an order that is still in 'open' status")
    public OrderDTO cancelOrder(@PathParam("orderId") Long orderId) {
        // Extract userId from JWT and get accountId
        String userId = jwt.getSubject();
        String authHeader = getAuthorizationHeader();
        AccountResponse account = accountServiceClient.getAccountByUserId(authHeader, userId);
        Long accountId = account.id();

        return orderService.cancelOrder(orderId, accountId);
    }
}

