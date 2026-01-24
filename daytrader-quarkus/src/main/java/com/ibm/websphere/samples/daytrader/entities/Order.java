/**
 * (C) Copyright IBM Corporation 2015, 2025.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.websphere.samples.daytrader.entities;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "orderejb")
public class Order extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "ORDERTYPE")
    public String orderType;

    @Column(name = "ORDERSTATUS")
    public String orderStatus;

    @Column(name = "OPENDATE")
    @Temporal(TemporalType.TIMESTAMP)
    public Date openDate;

    @Column(name = "COMPLETIONDATE")
    @Temporal(TemporalType.TIMESTAMP)
    public Date completionDate;

    @NotNull
    @Column(name = "QUANTITY", nullable = false)
    public double quantity;

    @Column(name = "PRICE")
    public BigDecimal price;

    @Column(name = "ORDERFEE")
    public BigDecimal orderFee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_ACCOUNTID")
    @JsonIgnore
    public Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "QUOTE_SYMBOL")
    public Quote quote;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "HOLDING_HOLDINGID")
    @JsonIgnore
    public Holding holding;

    public Order() {
    }

    public Order(String orderType, String orderStatus, Date openDate, Date completionDate,
                 double quantity, BigDecimal price, BigDecimal orderFee,
                 Account account, Quote quote, Holding holding) {
        this.orderType = orderType;
        this.orderStatus = orderStatus;
        this.openDate = openDate;
        this.completionDate = completionDate;
        this.quantity = quantity;
        this.price = price;
        this.orderFee = orderFee;
        this.account = account;
        this.quote = quote;
        this.holding = holding;
    }

    // Panache finder methods
    public static List<Order> findByAccountId(Long accountId) {
        return list("account.id", accountId);
    }

    public static List<Order> findByUserID(String userID) {
        return list("account.profile.userID", userID);
    }

    public static List<Order> findByStatus(String status) {
        return list("orderStatus", status);
    }

    public static List<Order> findClosedOrdersByUserID(String userID) {
        return list("orderStatus = ?1 and account.profile.userID = ?2", "closed", userID);
    }

    public static int completeClosedOrdersByUserID(String userID) {
        return update("orderStatus = 'completed' where orderStatus = 'closed' and account.profile.userID = ?1", userID);
    }

    public static List<Order> findBySymbol(String symbol) {
        return list("quote.symbol", symbol);
    }

    // Business methods
    public boolean isBuy() {
        return "buy".equalsIgnoreCase(orderType);
    }

    public boolean isSell() {
        return "sell".equalsIgnoreCase(orderType);
    }

    public boolean isOpen() {
        return "open".equalsIgnoreCase(orderStatus) || "processing".equalsIgnoreCase(orderStatus);
    }

    public boolean isCompleted() {
        return "completed".equalsIgnoreCase(orderStatus) ||
               "alertcompleted".equalsIgnoreCase(orderStatus) ||
               "cancelled".equalsIgnoreCase(orderStatus);
    }

    public boolean isCancelled() {
        return "cancelled".equalsIgnoreCase(orderStatus);
    }

    public void cancel() {
        this.orderStatus = "cancelled";
    }

    // Getters and setters for compatibility
    public Long getOrderID() {
        return id;
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

    public Date getOpenDate() {
        return openDate;
    }

    public void setOpenDate(Date openDate) {
        this.openDate = openDate;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
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

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Quote getQuote() {
        return quote;
    }

    public void setQuote(Quote quote) {
        this.quote = quote;
    }

    public Holding getHolding() {
        return holding;
    }

    public void setHolding(Holding holding) {
        this.holding = holding;
    }

    public String getSymbol() {
        return quote != null ? quote.symbol : null;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderType='" + orderType + '\'' +
                ", orderStatus='" + orderStatus + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", orderFee=" + orderFee +
                ", symbol=" + getSymbol() +
                '}';
    }
}

