package com.daytrader.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Account Response DTO (includes profile)
 */
public record AccountResponse(
    Long id,
    
    String userId,
    
    BigDecimal balance,
    
    @JsonProperty("openBalance")
    BigDecimal openBalance,
    
    @JsonProperty("loginCount")
    int loginCount,
    
    @JsonProperty("logoutCount")
    int logoutCount,
    
    @JsonProperty("lastLogin")
    Instant lastLogin,
    
    @JsonProperty("creationDate")
    Instant creationDate,
    
    ProfileDTO profile
) {}

