package com.daytrader.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for creating a new order.
 * This DTO is used by the frontend - accountId is derived from the JWT token.
 */
public class CreateOrderRequest {

    @NotNull(message = "Order type is required")
    @JsonProperty("orderType")
    private String orderType;

    @JsonProperty("symbol")
    private String symbol;

    @Positive(message = "Quantity must be positive")
    private Double quantity;

    @JsonProperty("holdingId")
    private Long holdingId;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(String orderType, String symbol, Double quantity, Long holdingId) {
        this.orderType = orderType;
        this.symbol = symbol;
        this.quantity = quantity;
        this.holdingId = holdingId;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Long getHoldingId() {
        return holdingId;
    }

    public void setHoldingId(Long holdingId) {
        this.holdingId = holdingId;
    }

    @Override
    public String toString() {
        return "CreateOrderRequest{" +
               "orderType='" + orderType + '\'' +
               ", symbol='" + symbol + '\'' +
               ", quantity=" + quantity +
               ", holdingId=" + holdingId +
               '}';
    }
}

