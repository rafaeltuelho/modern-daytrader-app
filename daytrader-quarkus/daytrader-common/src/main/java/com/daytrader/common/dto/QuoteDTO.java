package com.daytrader.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Quote Data Transfer Object
 */
public class QuoteDTO {

    @NotBlank
    private String symbol;

    @JsonProperty("companyName")
    private String companyName;

    @Positive
    private double volume;

    @NotNull
    @Positive
    private BigDecimal price;

    @JsonProperty("openPrice")
    private BigDecimal openPrice;

    @JsonProperty("lowPrice")
    private BigDecimal lowPrice;

    @JsonProperty("highPrice")
    private BigDecimal highPrice;

    @JsonProperty("priceChange")
    private double priceChange;

    @JsonProperty("createdAt")
    private Instant createdAt;

    @JsonProperty("updatedAt")
    private Instant updatedAt;

    public QuoteDTO() {
    }

    public QuoteDTO(String symbol, String companyName, double volume, BigDecimal price,
                   BigDecimal openPrice, BigDecimal lowPrice, BigDecimal highPrice,
                   double priceChange, Instant createdAt, Instant updatedAt) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.volume = volume;
        this.price = price;
        this.openPrice = openPrice;
        this.lowPrice = lowPrice;
        this.highPrice = highPrice;
        this.priceChange = priceChange;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(BigDecimal openPrice) {
        this.openPrice = openPrice;
    }

    public BigDecimal getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(BigDecimal lowPrice) {
        this.lowPrice = lowPrice;
    }

    public BigDecimal getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(BigDecimal highPrice) {
        this.highPrice = highPrice;
    }

    public double getPriceChange() {
        return priceChange;
    }

    public void setPriceChange(double priceChange) {
        this.priceChange = priceChange;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuoteDTO quoteDTO = (QuoteDTO) o;
        return Double.compare(quoteDTO.volume, volume) == 0 &&
               Double.compare(quoteDTO.priceChange, priceChange) == 0 &&
               Objects.equals(symbol, quoteDTO.symbol) &&
               Objects.equals(companyName, quoteDTO.companyName) &&
               Objects.equals(price, quoteDTO.price) &&
               Objects.equals(openPrice, quoteDTO.openPrice) &&
               Objects.equals(lowPrice, quoteDTO.lowPrice) &&
               Objects.equals(highPrice, quoteDTO.highPrice) &&
               Objects.equals(createdAt, quoteDTO.createdAt) &&
               Objects.equals(updatedAt, quoteDTO.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, companyName, volume, price, openPrice, lowPrice,
                          highPrice, priceChange, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "QuoteDTO{" +
               "symbol='" + symbol + '\'' +
               ", companyName='" + companyName + '\'' +
               ", volume=" + volume +
               ", price=" + price +
               ", openPrice=" + openPrice +
               ", lowPrice=" + lowPrice +
               ", highPrice=" + highPrice +
               ", priceChange=" + priceChange +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}

