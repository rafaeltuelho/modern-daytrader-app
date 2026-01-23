package com.ibm.websphere.samples.daytrader.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Request payload for buying stock.
 */
@Schema(description = "Buy stock request payload")
public record BuyRequest(
    @NotBlank(message = "Stock symbol is required")
    @Schema(description = "Stock symbol to buy", example = "s:0", required = true)
    String symbol,
    
    @Positive(message = "Quantity must be positive")
    @Schema(description = "Number of shares to buy", example = "100", required = true)
    double quantity
) {}

