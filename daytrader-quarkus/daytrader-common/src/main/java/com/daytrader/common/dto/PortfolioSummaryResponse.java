package com.daytrader.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * Portfolio Summary Response DTO
 * Per api-spec-trading.md specification
 */
public record PortfolioSummaryResponse(
    @JsonProperty("accountId")
    Long accountId,
    
    @JsonProperty("cashBalance")
    BigDecimal cashBalance,
    
    @JsonProperty("holdingsValue")
    BigDecimal holdingsValue,
    
    @JsonProperty("totalValue")
    BigDecimal totalValue,
    
    @JsonProperty("totalGain")
    BigDecimal totalGain,
    
    @JsonProperty("totalGainPercent")
    Double totalGainPercent,
    
    @JsonProperty("holdingsCount")
    Integer holdingsCount,
    
    @JsonProperty("recentOrders")
    List<OrderDTO> recentOrders,
    
    @JsonProperty("topHoldings")
    List<HoldingDTO> topHoldings
) {}

