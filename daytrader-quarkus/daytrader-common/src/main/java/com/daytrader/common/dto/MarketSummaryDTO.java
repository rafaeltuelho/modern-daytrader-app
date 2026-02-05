package com.daytrader.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Market Summary Data Transfer Object
 */
public record MarketSummaryDTO(
    @JsonProperty("tsia")
    BigDecimal tsia,

    @JsonProperty("openTsia")
    BigDecimal openTsia,

    double volume,

    @JsonProperty("topGainers")
    List<QuoteDTO> topGainers,

    @JsonProperty("topLosers")
    List<QuoteDTO> topLosers,

    @JsonProperty("mostActive")
    List<QuoteDTO> mostActive,

    @JsonProperty("summaryDate")
    Instant summaryDate,

    @JsonProperty("gainPercent")
    BigDecimal gainPercent,

    @JsonProperty("marketStatus")
    String marketStatus,

    @JsonProperty("topGainersCount")
    int topGainersCount,

    @JsonProperty("topLosersCount")
    int topLosersCount
) {}

