package com.daytrader.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Order Data Transfer Object
 */
public class OrderDTO {

    private Long id;

    @NotNull
    @JsonProperty("orderType")
    private String orderType;

    @NotNull
    @JsonProperty("orderStatus")
    private String orderStatus;

    @JsonProperty("openDate")
    private Instant openDate;

    @JsonProperty("completionDate")
    private Instant completionDate;

    @Positive
    private double quantity;

    private BigDecimal price;

    @JsonProperty("orderFee")
    private BigDecimal orderFee;

    @NotNull
    @JsonProperty("accountId")
    private Long accountId;

    @NotBlank
    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("holdingId")
    private Long holdingId;

    @JsonProperty("createdAt")
    private Instant createdAt;

    @JsonProperty("updatedAt")
    private Instant updatedAt;

    public OrderDTO() {
    }

    public OrderDTO(Long id, String orderType, String orderStatus, Instant openDate,
                   Instant completionDate, double quantity, BigDecimal price, BigDecimal orderFee,
                   Long accountId, String symbol, Long holdingId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.orderType = orderType;
        this.orderStatus = orderStatus;
        this.openDate = openDate;
        this.completionDate = completionDate;
        this.quantity = quantity;
        this.price = price;
        this.orderFee = orderFee;
        this.accountId = accountId;
        this.symbol = symbol;
        this.holdingId = holdingId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Instant getOpenDate() {
        return openDate;
    }

    public void setOpenDate(Instant openDate) {
        this.openDate = openDate;
    }

    public Instant getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Instant completionDate) {
        this.completionDate = completionDate;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOrderFee() {
        return orderFee;
    }

    public void setOrderFee(BigDecimal orderFee) {
        this.orderFee = orderFee;
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

    public Long getHoldingId() {
        return holdingId;
    }

    public void setHoldingId(Long holdingId) {
        this.holdingId = holdingId;
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
        OrderDTO orderDTO = (OrderDTO) o;
        return Double.compare(orderDTO.quantity, quantity) == 0 &&
               Objects.equals(id, orderDTO.id) &&
               Objects.equals(orderType, orderDTO.orderType) &&
               Objects.equals(orderStatus, orderDTO.orderStatus) &&
               Objects.equals(openDate, orderDTO.openDate) &&
               Objects.equals(completionDate, orderDTO.completionDate) &&
               Objects.equals(price, orderDTO.price) &&
               Objects.equals(orderFee, orderDTO.orderFee) &&
               Objects.equals(accountId, orderDTO.accountId) &&
               Objects.equals(symbol, orderDTO.symbol) &&
               Objects.equals(holdingId, orderDTO.holdingId) &&
               Objects.equals(createdAt, orderDTO.createdAt) &&
               Objects.equals(updatedAt, orderDTO.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderType, orderStatus, openDate, completionDate, quantity,
                          price, orderFee, accountId, symbol, holdingId, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "OrderDTO{" +
               "id=" + id +
               ", orderType='" + orderType + '\'' +
               ", orderStatus='" + orderStatus + '\'' +
               ", openDate=" + openDate +
               ", completionDate=" + completionDate +
               ", quantity=" + quantity +
               ", price=" + price +
               ", orderFee=" + orderFee +
               ", accountId=" + accountId +
               ", symbol='" + symbol + '\'' +
               ", holdingId=" + holdingId +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}

