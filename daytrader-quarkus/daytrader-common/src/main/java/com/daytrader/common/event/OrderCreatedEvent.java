package com.daytrader.common.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event published when a new order is created
 */
public record OrderCreatedEvent(
    @JsonProperty("orderId")
    Long orderId,
    
    @JsonProperty("orderType")
    String orderType,
    
    @JsonProperty("accountId")
    Long accountId,
    
    @JsonProperty("quoteSymbol")
    String quoteSymbol,
    
    double quantity,
    
    BigDecimal price,
    
    @JsonProperty("eventTime")
    Instant eventTime
) {}

