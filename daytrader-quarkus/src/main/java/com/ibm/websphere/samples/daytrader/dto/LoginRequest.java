package com.ibm.websphere.samples.daytrader.dto;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Request payload for user login.
 */
@Schema(description = "Login request payload")
public record LoginRequest(
    @NotBlank(message = "User ID is required")
    @Schema(description = "User ID", example = "uid:0", required = true)
    String userID,
    
    @NotBlank(message = "Password is required")
    @Schema(description = "User password", example = "xxx", required = true)
    String password
) {}

