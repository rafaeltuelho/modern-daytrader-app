package com.daytrader.account.resource;

import com.daytrader.account.security.JwtTokenService;
import com.daytrader.account.service.AccountService;
import com.daytrader.common.dto.LoginRequest;
import com.daytrader.common.dto.ProfileDTO;
import com.daytrader.common.exception.BusinessException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.Map;

/**
 * REST Resource for Authentication operations.
 * Uses simple JWT authentication (no OIDC/Keycloak).
 */
@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "User authentication operations")
public class AuthResource {

    private static final Logger LOG = Logger.getLogger(AuthResource.class);

    @Inject
    AccountService accountService;

    @Inject
    JwtTokenService jwtTokenService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/login")
    @Operation(
        summary = "Authenticate user and obtain JWT token",
        description = "Authenticates user credentials and returns a signed JWT access token. " +
                     "The token should be included in the Authorization header as 'Bearer <token>' for subsequent requests."
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Authentication successful, returns JWT token"),
        @APIResponse(responseCode = "401", description = "Invalid credentials"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response login(@Valid LoginRequest request) {
        LOG.infof("Login attempt for user: %s", request.getUserId());

        // Validate credentials and get profile
        ProfileDTO profile = accountService.validateCredentials(request.getUserId(), request.getPassword());
        if (profile == null) {
            throw new BusinessException("Invalid user ID or password", "INVALID_CREDENTIALS");
        }

        // Record the login
        accountService.recordLogin(request.getUserId());

        // Generate JWT token
        String token = jwtTokenService.generateTraderToken(
            profile.getUserId(),
            profile.getEmail(),
            profile.getFullName()
        );

        LOG.infof("Login successful for user: %s", request.getUserId());

        return Response.ok()
            .entity(Map.of(
                "token", token,
                "tokenType", "Bearer",
                "expiresIn", 3600,
                "userId", profile.getUserId()
            ))
            .build();
    }

    @POST
    @Path("/logout")
    @RolesAllowed({"user", "trader", "admin"})
    @Operation(
        summary = "Logout user",
        description = "Logs out the user by recording the logout event. " +
                     "Note: With stateless JWT, the token remains valid until expiry. " +
                     "Client should discard the token."
    )
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Logout successful"),
        @APIResponse(responseCode = "401", description = "Missing or invalid authentication token"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response logout() {
        String userId = jwt.getSubject();
        if (userId == null) {
            userId = "anonymous";
        }
        LOG.infof("Logout for user: %s", userId);

        if (!"anonymous".equals(userId)) {
            accountService.recordLogout(userId);
        }

        return Response.noContent().build();
    }
}

