package com.ibm.websphere.samples.daytrader.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Response payload for successful login.
 */
@Schema(description = "Login response with JWT token")
public record LoginResponse(
    @Schema(description = "User ID", example = "uid:0")
    String userID,
    
    @Schema(description = "JWT authentication token")
    String token,
    
    @Schema(description = "Token type", example = "Bearer")
    String tokenType,
    
    @Schema(description = "Token expiration time in seconds", example = "3600")
    long expiresIn
) {
    /**
     * Creates a LoginResponse with default token type.
     */
    public LoginResponse(String userID, String token, long expiresIn) {
        this(userID, token, "Bearer", expiresIn);
    }
}

