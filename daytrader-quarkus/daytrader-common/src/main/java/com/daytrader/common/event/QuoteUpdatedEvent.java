package com.daytrader.common.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event published when a quote is updated
 */
public record QuoteUpdatedEvent(
    String symbol,
    
    BigDecimal price,
    
    @JsonProperty("priceChange")
    double priceChange,
    
    double volume,
    
    @JsonProperty("eventTime")
    Instant eventTime
) {}

