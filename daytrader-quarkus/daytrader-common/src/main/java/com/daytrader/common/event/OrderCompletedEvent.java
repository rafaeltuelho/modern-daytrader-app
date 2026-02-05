package com.daytrader.common.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event published when an order is completed
 */
public record OrderCompletedEvent(
    @JsonProperty("orderId")
    Long orderId,
    
    @JsonProperty("orderType")
    String orderType,
    
    @JsonProperty("orderStatus")
    String orderStatus,
    
    @JsonProperty("accountId")
    Long accountId,
    
    @JsonProperty("quoteSymbol")
    String quoteSymbol,
    
    double quantity,
    
    BigDecimal price,
    
    @JsonProperty("orderFee")
    BigDecimal orderFee,
    
    @JsonProperty("completionDate")
    Instant completionDate,
    
    @JsonProperty("eventTime")
    Instant eventTime
) {}

