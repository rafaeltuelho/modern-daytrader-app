package com.ibm.websphere.samples.daytrader.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Request payload for selling stock holdings.
 */
@Schema(description = "Sell stock request payload")
public record SellRequest(
    @NotNull(message = "Holding ID is required")
    @Positive(message = "Holding ID must be positive")
    @Schema(description = "ID of the holding to sell", example = "1", required = true)
    Long holdingId
) {}

