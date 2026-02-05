package com.daytrader.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Holding Data Transfer Object
 */
public class HoldingDTO {

    private Long id;

    @NotNull
    @JsonProperty("accountId")
    private Long accountId;

    @NotBlank
    @JsonProperty("symbol")
    private String symbol;

    @Positive
    private double quantity;

    @JsonProperty("purchasePrice")
    private BigDecimal purchasePrice;

    @JsonProperty("purchaseDate")
    private Instant purchaseDate;

    @JsonProperty("createdAt")
    private Instant createdAt;

    @JsonProperty("updatedAt")
    private Instant updatedAt;

    public HoldingDTO() {
    }

    public HoldingDTO(Long id, Long accountId, String symbol, double quantity,
                     BigDecimal purchasePrice, Instant purchaseDate, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.accountId = accountId;
        this.symbol = symbol;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.purchaseDate = purchaseDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Instant getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Instant purchaseDate) {
        this.purchaseDate = purchaseDate;
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
        HoldingDTO that = (HoldingDTO) o;
        return Double.compare(that.quantity, quantity) == 0 &&
               Objects.equals(id, that.id) &&
               Objects.equals(accountId, that.accountId) &&
               Objects.equals(symbol, that.symbol) &&
               Objects.equals(purchasePrice, that.purchasePrice) &&
               Objects.equals(purchaseDate, that.purchaseDate) &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountId, symbol, quantity, purchasePrice,
                          purchaseDate, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "HoldingDTO{" +
               "id=" + id +
               ", accountId=" + accountId +
               ", symbol='" + symbol + '\'' +
               ", quantity=" + quantity +
               ", purchasePrice=" + purchasePrice +
               ", purchaseDate=" + purchaseDate +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}

