package com.daytrader.account.resource;

import com.daytrader.account.service.AccountService;
import com.daytrader.common.dto.AccountResponse;
import com.daytrader.common.dto.BalanceUpdateRequest;
import com.daytrader.common.dto.ProfileDTO;
import com.daytrader.common.dto.RegisterRequest;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.net.URI;

/**
 * REST Resource for Account operations
 */
@Path("/api/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Accounts", description = "Account management operations")
public class AccountResource {

    private static final Logger LOG = Logger.getLogger(AccountResource.class);

    @Inject
    AccountService accountService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Operation(
        summary = "Register a new user account",
        description = "Creates a new user account with the provided profile information. " +
                     "The user is created in both Keycloak (for authentication) and the account database. " +
                     "An initial balance can be specified."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Account created successfully",
            content = @Content(schema = @Schema(implementation = AccountResponse.class))
        ),
        @APIResponse(responseCode = "400", description = "Invalid request parameters"),
        @APIResponse(responseCode = "409", description = "User ID or email already exists"),
        @APIResponse(responseCode = "422", description = "Validation failed"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response registerAccount(
        @Valid RegisterRequest request,
        @Context UriInfo uriInfo
    ) {
        LOG.infof("Registering new account for user: %s", request.getUserId());

        AccountResponse account = accountService.register(request);

        URI location = uriInfo.getAbsolutePathBuilder()
            .path(account.id().toString())
            .build();

        return Response.created(location)
            .entity(account)
            .build();
    }

    @GET
    @Path("/{accountId}")
    @Operation(
        summary = "Get account by ID",
        description = "Returns account details. Users can only access their own account unless they have admin role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Account details",
            content = @Content(schema = @Schema(implementation = AccountResponse.class))
        ),
        @APIResponse(responseCode = "401", description = "Missing or invalid authentication token"),
        @APIResponse(responseCode = "403", description = "Insufficient permissions"),
        @APIResponse(responseCode = "404", description = "Account not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAccount(
        @Parameter(description = "Account ID", required = true)
        @PathParam("accountId") Long accountId
    ) {
        LOG.debugf("Getting account: %d", accountId);
        AccountResponse account = accountService.getAccount(accountId);
        return Response.ok(account).build();
    }

    @GET
    @Path("/me")
    @Operation(
        summary = "Get current user's account",
        description = "Returns the account details for the currently authenticated user."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Account details",
            content = @Content(schema = @Schema(implementation = AccountResponse.class))
        ),
        @APIResponse(responseCode = "401", description = "Missing or invalid authentication token"),
        @APIResponse(responseCode = "404", description = "Account not found for authenticated user"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getCurrentAccount() {
        // Extract userId from JWT token
        String userId = jwt.getSubject();
        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"No valid JWT token provided\"}")
                .build();
        }
        LOG.debugf("Getting current account for user: %s", userId);
        AccountResponse account = accountService.getAccountByUserId(userId);
        return Response.ok(account).build();
    }

    @GET
    @Path("/user/{userId}")
    @Operation(
        summary = "Get account by user ID",
        description = "Returns account details for the specified user ID. Used for internal service-to-service communication."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Account details",
            content = @Content(schema = @Schema(implementation = AccountResponse.class))
        ),
        @APIResponse(responseCode = "404", description = "Account not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAccountByUserId(
        @Parameter(description = "User ID", required = true)
        @PathParam("userId") String userId
    ) {
        LOG.debugf("Getting account for user: %s", userId);
        AccountResponse account = accountService.getAccountByUserId(userId);
        return Response.ok(account).build();
    }

    @PUT
    @Path("/{accountId}/balance")
    @PermitAll  // TODO: In production, secure with service mesh or internal auth
    @Operation(
        summary = "Update account balance",
        description = "Updates account balance by adding the specified amount (positive for credit, negative for debit). " +
                     "Used for internal service-to-service communication during order processing."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Balance updated successfully",
            content = @Content(schema = @Schema(implementation = AccountResponse.class))
        ),
        @APIResponse(responseCode = "400", description = "Invalid request parameters"),
        @APIResponse(responseCode = "404", description = "Account not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response updateBalance(
        @Parameter(description = "Account ID", required = true)
        @PathParam("accountId") Long accountId,
        @Valid BalanceUpdateRequest request
    ) {
        LOG.infof("Updating balance for account %d: amount=%s, reason=%s",
                accountId, request.getAmount(), request.getReason());

        // Get current account
        AccountResponse currentAccount = accountService.getAccount(accountId);

        // Calculate new balance: currentBalance + amount (amount can be negative for debit)
        BigDecimal newBalance = currentAccount.balance().add(request.getAmount());

        // Update balance
        accountService.updateBalance(accountId, newBalance);

        // Return updated account
        AccountResponse updatedAccount = accountService.getAccount(accountId);
        return Response.ok(updatedAccount).build();
    }
}

