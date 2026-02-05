package com.daytrader.account.resource;

import com.daytrader.account.service.AccountService;
import com.daytrader.common.dto.ChangePasswordRequest;
import com.daytrader.common.dto.ProfileDTO;
import com.daytrader.common.dto.UpdateProfileRequest;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * REST Resource for Profile operations
 */
@Path("/api/profiles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Profiles", description = "User profile management")
public class ProfileResource {

    private static final Logger LOG = Logger.getLogger(ProfileResource.class);

    @Inject
    AccountService accountService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/me")
    @Operation(
        summary = "Get current user's profile",
        description = "Returns the profile for the currently authenticated user."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Profile details",
            content = @Content(schema = @Schema(implementation = ProfileDTO.class))
        ),
        @APIResponse(responseCode = "401", description = "Missing or invalid authentication token"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getCurrentProfile() {
        // Extract userId from JWT token
        String userId = jwt.getSubject();
        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"No valid JWT token provided\"}")
                .build();
        }
        LOG.debugf("Getting profile for user: %s", userId);
        ProfileDTO profile = accountService.getProfile(userId);
        return Response.ok(profile).build();
    }

    @PUT
    @Path("/me")
    @RolesAllowed({"user", "trader", "admin"})
    @Operation(
        summary = "Update current user's profile",
        description = "Updates the profile for the currently authenticated user."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Profile updated successfully",
            content = @Content(schema = @Schema(implementation = ProfileDTO.class))
        ),
        @APIResponse(responseCode = "400", description = "Invalid input data"),
        @APIResponse(responseCode = "401", description = "Missing or invalid authentication token"),
        @APIResponse(responseCode = "409", description = "Email already in use"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response updateProfile(@Valid UpdateProfileRequest request) {
        // Extract userId from JWT token
        String userId = jwt.getSubject();
        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"No valid JWT token provided\"}")
                .build();
        }
        LOG.debugf("Updating profile for user: %s", userId);
        ProfileDTO profile = accountService.updateProfile(userId, request);
        return Response.ok(profile).build();
    }

    @PUT
    @Path("/me/password")
    @RolesAllowed({"user", "trader", "admin"})
    @Operation(
        summary = "Change current user's password",
        description = "Changes the password for the currently authenticated user."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "204",
            description = "Password changed successfully"
        ),
        @APIResponse(responseCode = "400", description = "Invalid input data or incorrect current password"),
        @APIResponse(responseCode = "401", description = "Missing or invalid authentication token"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response changePassword(@Valid ChangePasswordRequest request) {
        // Extract userId from JWT token
        String userId = jwt.getSubject();
        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"No valid JWT token provided\"}")
                .build();
        }
        LOG.debugf("Changing password for user: %s", userId);
        accountService.changePassword(userId, request);
        return Response.noContent().build();
    }
}

