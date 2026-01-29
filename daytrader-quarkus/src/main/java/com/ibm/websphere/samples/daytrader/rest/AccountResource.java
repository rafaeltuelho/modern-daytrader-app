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

import java.math.BigDecimal;

import com.ibm.websphere.samples.daytrader.dto.AccountDTO;
import com.ibm.websphere.samples.daytrader.dto.AccountProfileDTO;
import com.ibm.websphere.samples.daytrader.service.TradeService;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST resource for account operations
 * Per Phase 3: Backend Migration specification section 4
 * 
 * Exposes endpoints under /api/v1/accounts
 */
@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Accounts", description = "Account management operations")
public class AccountResource {

    @Inject
    TradeService tradeService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/{accountId}")
    @RolesAllowed({"Trader", "User"})
    @Operation(summary = "Get account by ID", description = "Retrieves account information by account ID")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Account found",
            content = @Content(schema = @Schema(implementation = AccountDTO.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "Account not found"
        )
    })
    public Response getAccount(@PathParam("accountId") Integer accountId) {
        try {
            AccountDTO account = tradeService.getAccountData(accountId);
            return Response.ok(account).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new QuoteResource.ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/profile")
    @RolesAllowed({"Trader", "User"})
    @Operation(summary = "Get current user's profile", description = "Retrieves the profile of the currently authenticated user")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Profile found",
            content = @Content(schema = @Schema(implementation = AccountProfileDTO.class))
        ),
        @APIResponse(
            responseCode = "401",
            description = "User not authenticated"
        ),
        @APIResponse(
            responseCode = "404",
            description = "Profile not found"
        )
    })
    public Response getProfile() {
        String userID = jwt.getSubject();
        if (userID == null || userID.isBlank()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new QuoteResource.ErrorResponse("User not authenticated"))
                    .build();
        }
        try {
            AccountProfileDTO profile = tradeService.getAccountProfileData(userID);
            return Response.ok(profile).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new QuoteResource.ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/profile")
    @RolesAllowed({"Trader", "User"})
    @Operation(summary = "Update current user's profile", description = "Updates the profile of the currently authenticated user")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Profile updated",
            content = @Content(schema = @Schema(implementation = AccountProfileDTO.class))
        ),
        @APIResponse(
            responseCode = "401",
            description = "User not authenticated"
        ),
        @APIResponse(
            responseCode = "404",
            description = "Profile not found"
        )
    })
    public Response updateProfile(UpdateProfileRequest request) {
        String userID = jwt.getSubject();
        if (userID == null || userID.isBlank()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new QuoteResource.ErrorResponse("User not authenticated"))
                    .build();
        }
        try {
            AccountProfileDTO profileData = new AccountProfileDTO();
            profileData.setUserID(userID);
            profileData.setFullName(request.fullName);
            profileData.setAddress(request.address);
            profileData.setEmail(request.email);
            profileData.setCreditCard(request.creditCard);
            profileData.setPassword(request.password);

            AccountProfileDTO updatedProfile = tradeService.updateAccountProfile(profileData);
            return Response.ok(updatedProfile).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new QuoteResource.ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Request DTO for profile update
     */
    public static class UpdateProfileRequest {
        public String fullName;
        public String address;
        public String email;
        public String creditCard;
        public String password;
    }

    @POST
    @PermitAll
    @Operation(summary = "Register new account", description = "Creates a new user account and profile")
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Account created",
            content = @Content(schema = @Schema(implementation = AccountDTO.class))
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid request or user already exists"
        )
    })
    public Response register(RegisterRequest request) {
        // Validate required fields
        if (request.userID == null || request.userID.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new QuoteResource.ErrorResponse("userID is required"))
                    .build();
        }
        if (request.password == null || request.password.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new QuoteResource.ErrorResponse("password is required"))
                    .build();
        }
        if (request.fullName == null || request.fullName.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new QuoteResource.ErrorResponse("fullName is required"))
                    .build();
        }
        if (request.email == null || request.email.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new QuoteResource.ErrorResponse("email is required"))
                    .build();
        }

        try {
            AccountDTO account = tradeService.register(
                request.userID,
                request.password,
                request.fullName,
                request.address,
                request.email,
                request.creditCard,
                request.openBalance != null ? request.openBalance : new BigDecimal("100000.00")
            );
            return Response.status(Response.Status.CREATED).entity(account).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new QuoteResource.ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Request DTO for account registration
     */
    public static class RegisterRequest {
        public String userID;
        public String password;
        public String fullName;
        public String address;
        public String email;
        public String creditCard;
        public BigDecimal openBalance;
    }
}

