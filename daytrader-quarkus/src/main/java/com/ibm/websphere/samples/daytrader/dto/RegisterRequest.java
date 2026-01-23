package com.ibm.websphere.samples.daytrader.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Request payload for new user registration.
 */
@Schema(description = "User registration request payload")
public record RegisterRequest(
    @NotBlank(message = "User ID is required")
    @Schema(description = "Unique user ID", example = "uid:100", required = true)
    String userID,
    
    @NotBlank(message = "Password is required")
    @Schema(description = "User password", example = "xxx", required = true)
    String password,
    
    @NotBlank(message = "Full name is required")
    @Schema(description = "User's full name", example = "John Doe", required = true)
    String fullName,
    
    @Schema(description = "User's address", example = "123 Main St, City, State 12345")
    String address,
    
    @Email(message = "Invalid email format")
    @Schema(description = "User's email address", example = "john.doe@example.com")
    String email,
    
    @Schema(description = "Credit card number", example = "1234-5678-9012-3456")
    String creditCard,
    
    @NotNull(message = "Opening balance is required")
    @Positive(message = "Opening balance must be positive")
    @Schema(description = "Initial account balance", example = "10000.00", required = true)
    BigDecimal openBalance
) {}

