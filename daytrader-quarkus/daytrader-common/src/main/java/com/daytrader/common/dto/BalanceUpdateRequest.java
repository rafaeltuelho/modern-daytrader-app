package com.daytrader.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Request DTO for updating account balance.
 * Used for internal service-to-service communication when processing orders.
 */
public class BalanceUpdateRequest {

    @NotNull
    @JsonProperty("amount")
    private BigDecimal amount;  // Positive for credit, negative for debit

    @JsonProperty("reason")
    private String reason;  // "ORDER_BUY", "ORDER_SELL", etc.

    public BalanceUpdateRequest() {
    }

    public BalanceUpdateRequest(BigDecimal amount, String reason) {
        this.amount = amount;
        this.reason = reason;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BalanceUpdateRequest that = (BalanceUpdateRequest) o;
        return Objects.equals(amount, that.amount) &&
               Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, reason);
    }

    @Override
    public String toString() {
        return "BalanceUpdateRequest{" +
               "amount=" + amount +
               ", reason='" + reason + '\'' +
               '}';
    }
}

