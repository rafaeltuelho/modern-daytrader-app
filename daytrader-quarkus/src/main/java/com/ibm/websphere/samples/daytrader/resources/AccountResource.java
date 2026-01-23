package com.ibm.websphere.samples.daytrader.resources;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.ibm.websphere.samples.daytrader.entities.Account;
import com.ibm.websphere.samples.daytrader.entities.AccountProfile;
import com.ibm.websphere.samples.daytrader.services.AccountService;

/**
 * REST resource for account management operations.
 * All endpoints require JWT authentication with "user" role.
 */
@Path("/api/account")
@Tag(name = "Account", description = "Account management endpoints")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
@SecurityRequirement(name = "jwt")
public class AccountResource {

    @Inject
    AccountService accountService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Operation(
        summary = "Get current user's account",
        description = "Retrieve the account information for the authenticated user"
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Account retrieved successfully",
            content = @Content(schema = @Schema(implementation = Account.class))),
        @APIResponse(responseCode = "401", description = "User not authenticated"),
        @APIResponse(responseCode = "404", description = "Account not found")
    })
    public Response getAccount() {
        String userID = jwt.getSubject();

        try {
            Account account = accountService.getAccountData(userID);
            return Response.ok(account).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(java.util.Map.of("error", "Account not found"))
                .build();
        }
    }

    @GET
    @Path("/profile")
    @Operation(
        summary = "Get current user's profile",
        description = "Retrieve the profile information for the authenticated user"
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = AccountProfile.class))),
        @APIResponse(responseCode = "401", description = "User not authenticated"),
        @APIResponse(responseCode = "404", description = "Profile not found")
    })
    public Response getProfile() {
        String userID = jwt.getSubject();

        try {
            AccountProfile profile = accountService.getAccountProfileData(userID);
            return Response.ok(profile).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(java.util.Map.of("error", "Profile not found"))
                .build();
        }
    }

    @PUT
    @Path("/profile")
    @Operation(
        summary = "Update user's profile",
        description = "Update the profile information for the authenticated user"
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Profile updated successfully",
            content = @Content(schema = @Schema(implementation = AccountProfile.class))),
        @APIResponse(responseCode = "401", description = "User not authenticated"),
        @APIResponse(responseCode = "400", description = "Invalid profile data"),
        @APIResponse(responseCode = "404", description = "Profile not found")
    })
    public Response updateProfile(
        @RequestBody(description = "Updated profile information", required = true)
        @Valid AccountProfile profileUpdate
    ) {
        String userID = jwt.getSubject();

        // Ensure the profile userID matches the authenticated user
        profileUpdate.userID = userID;

        try {
            AccountProfile updatedProfile = accountService.updateAccountProfile(profileUpdate);
            return Response.ok(updatedProfile).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(java.util.Map.of("error", "Profile not found"))
                .build();
        } catch (jakarta.ws.rs.BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(java.util.Map.of("error", e.getMessage()))
                .build();
        }
    }
}

