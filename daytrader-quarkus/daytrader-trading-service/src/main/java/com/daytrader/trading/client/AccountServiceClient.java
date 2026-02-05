package com.daytrader.trading.client;

import com.daytrader.common.dto.AccountResponse;
import com.daytrader.common.dto.BalanceUpdateRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST Client for Account Service.
 * Used to look up account information by user ID and update balances.
 */
@Path("/api/accounts")
@RegisterRestClient(configKey = "account-service")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface AccountServiceClient {

    /**
     * Get account by user ID.
     * @param authorization The Authorization header (Bearer token) to forward
     * @param userId The user's unique identifier
     * @return AccountResponse with account details including accountId
     */
    @GET
    @Path("/user/{userId}")
    AccountResponse getAccountByUserId(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @PathParam("userId") String userId);

    /**
     * Update account balance.
     * Used during order processing to debit/credit account balances.
     *
     * @param authorization The Authorization header (can be null for internal calls)
     * @param accountId The account ID
     * @param request The balance update request (amount and reason)
     * @return Updated AccountResponse
     */
    @PUT
    @Path("/{accountId}/balance")
    AccountResponse updateBalance(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
                                  @PathParam("accountId") Long accountId,
                                  BalanceUpdateRequest request);
}

