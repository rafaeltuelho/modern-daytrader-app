package com.ibm.websphere.samples.daytrader.resources;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.ibm.websphere.samples.daytrader.dto.LoginRequest;
import com.ibm.websphere.samples.daytrader.dto.LoginResponse;
import com.ibm.websphere.samples.daytrader.dto.RegisterRequest;
import com.ibm.websphere.samples.daytrader.entities.Account;
import com.ibm.websphere.samples.daytrader.services.AuthService;
import com.ibm.websphere.samples.daytrader.services.JWTService;

/**
 * REST resource for authentication operations.
 * All endpoints in this resource are public (no authentication required).
 */
@Path("/api/auth")
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class AuthResource {

    @Inject
    AuthService authService;

    @Inject
    JWTService jwtService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/login")
    @Operation(
        summary = "User login",
        description = "Authenticate user with credentials and return JWT token"
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Login successful",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @APIResponse(responseCode = "401", description = "Invalid credentials"),
        @APIResponse(responseCode = "400", description = "Invalid request payload")
    })
    public Response login(
        @RequestBody(description = "Login credentials", required = true)
        @Valid LoginRequest request
    ) {
        try {
            Account account = authService.login(request.userID(), request.password());
            // Generate real JWT token using JWTService
            String token = jwtService.generateToken(request.userID());
            long expiresIn = jwtService.getTokenExpirationSeconds();
            LoginResponse response = new LoginResponse(request.userID(), token, expiresIn);
            return Response.ok(response).build();
        } catch (jakarta.ws.rs.NotAuthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(java.util.Map.of("error", "Invalid credentials"))
                .build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(java.util.Map.of("error", "User not found"))
                .build();
        }
    }

    @POST
    @Path("/logout")
    @RolesAllowed("user")
    @Operation(
        summary = "User logout",
        description = "Logout the current user and invalidate their session"
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Logout successful"),
        @APIResponse(responseCode = "401", description = "User not authenticated")
    })
    public Response logout() {
        String userID = jwt.getSubject();

        try {
            authService.logout(userID);
            return Response.ok(java.util.Map.of(
                "message", "Logout successful",
                "userID", userID
            )).build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(java.util.Map.of("error", "User not found"))
                .build();
        }
    }

    @POST
    @Path("/register")
    @Operation(
        summary = "Register new user",
        description = "Create a new user account with the provided information"
    )
    @APIResponses({
        @APIResponse(responseCode = "201", description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = Account.class))),
        @APIResponse(responseCode = "400", description = "Invalid registration data"),
        @APIResponse(responseCode = "409", description = "User already exists")
    })
    public Response register(
        @RequestBody(description = "Registration information", required = true)
        @Valid RegisterRequest request
    ) {
        try {
            Account account = authService.register(
                request.userID(),
                request.password(),
                request.fullName(),
                request.address(),
                request.email(),
                request.creditCard(),
                request.openBalance()
            );
            return Response.status(Response.Status.CREATED).entity(account).build();
        } catch (jakarta.ws.rs.BadRequestException e) {
            return Response.status(Response.Status.CONFLICT)
                .entity(java.util.Map.of("error", e.getMessage()))
                .build();
        }
    }
}

