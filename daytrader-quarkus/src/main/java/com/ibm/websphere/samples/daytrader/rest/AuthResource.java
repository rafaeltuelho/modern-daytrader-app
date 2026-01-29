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

import com.ibm.websphere.samples.daytrader.dto.AccountDTO;
import com.ibm.websphere.samples.daytrader.dto.LoginResponseDTO;
import com.ibm.websphere.samples.daytrader.service.JwtService;
import com.ibm.websphere.samples.daytrader.service.TradeService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
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
 * REST resource for authentication operations
 * Per Phase 2: Feature Implementation - Core Trading Operations
 * 
 * Exposes endpoints under /api/v1/auth
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "User authentication operations")
public class AuthResource {

    @Inject
    TradeService tradeService;

    @Inject
    JwtService jwtService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/me")
    @RolesAllowed({"Trader", "User"})
    @Operation(summary = "Get current user", description = "Returns the currently authenticated user's account information")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User information retrieved successfully",
            content = @Content(schema = @Schema(implementation = AccountDTO.class))
        ),
        @APIResponse(
            responseCode = "401",
            description = "User not authenticated"
        )
    })
    public Response getCurrentUser() {
        String userID = jwt.getSubject();
        if (userID == null || userID.isBlank()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new QuoteResource.ErrorResponse("User not authenticated"))
                    .build();
        }

        try {
            AccountDTO account = tradeService.getAccountDataByUserID(userID);
            return Response.ok(account).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new QuoteResource.ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/login")
    @Operation(summary = "Login user", description = "Authenticates a user and returns account information with JWT token")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))
        ),
        @APIResponse(
            responseCode = "401",
            description = "Invalid credentials"
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid request"
        )
    })
    public Response login(LoginRequest request) {
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

        try {
            // Authenticate user
            AccountDTO account = tradeService.login(request.userID, request.password);

            // Generate JWT token
            String token = jwtService.generateTraderToken(request.userID);
            long expiresIn = jwtService.getTokenLifespan();

            // Return login response with token
            LoginResponseDTO loginResponse = new LoginResponseDTO(account, token, expiresIn);
            return Response.ok(loginResponse).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new QuoteResource.ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/logout")
    @Operation(summary = "Logout user", description = "Logs out a user and updates logout statistics")
    @APIResponses({
        @APIResponse(
            responseCode = "204",
            description = "Logout successful"
        ),
        @APIResponse(
            responseCode = "404",
            description = "User not found"
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid request"
        )
    })
    public Response logout(LogoutRequest request) {
        // Validate required fields
        if (request.userID == null || request.userID.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new QuoteResource.ErrorResponse("userID is required"))
                    .build();
        }

        try {
            tradeService.logout(request.userID);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new QuoteResource.ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Request DTO for login
     */
    public static class LoginRequest {
        public String userID;
        public String password;
    }

    /**
     * Request DTO for logout
     */
    public static class LogoutRequest {
        public String userID;
    }
}

