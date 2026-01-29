/**
 * (C) Copyright IBM Corporation 2015, 2024.
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
package com.ibm.websphere.samples.daytrader.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;

/**
 * Order entity - represents a trading order (buy, sell, cancel)
 * Migrated from OrderDataBean per Phase 3: Backend Migration specification
 */
@Entity
@Table(name = "orderejb")
@NamedQueries({
    @NamedQuery(name = "Order.findByOrderfee", query = "SELECT o FROM Order o WHERE o.orderFee = :orderfee"),
    @NamedQuery(name = "Order.findByCompletiondate", query = "SELECT o FROM Order o WHERE o.completionDate = :completiondate"),
    @NamedQuery(name = "Order.findByOrdertype", query = "SELECT o FROM Order o WHERE o.orderType = :ordertype"),
    @NamedQuery(name = "Order.findByOrderstatus", query = "SELECT o FROM Order o WHERE o.orderStatus = :orderstatus"),
    @NamedQuery(name = "Order.findByPrice", query = "SELECT o FROM Order o WHERE o.price = :price"),
    @NamedQuery(name = "Order.findByQuantity", query = "SELECT o FROM Order o WHERE o.quantity = :quantity"),
    @NamedQuery(name = "Order.findByOpendate", query = "SELECT o FROM Order o WHERE o.openDate = :opendate"),
    @NamedQuery(name = "Order.findByOrderid", query = "SELECT o FROM Order o WHERE o.orderID = :orderid"),
    @NamedQuery(name = "Order.findByAccountAccountid", query = "SELECT o FROM Order o WHERE o.account.accountID = :accountAccountid"),
    @NamedQuery(name = "Order.findByQuoteSymbol", query = "SELECT o FROM Order o WHERE o.quote.symbol = :quoteSymbol"),
    @NamedQuery(name = "Order.findByHoldingHoldingid", query = "SELECT o FROM Order o WHERE o.holding.holdingID = :holdingHoldingid"),
    @NamedQuery(name = "Order.closedOrders", query = "SELECT o FROM Order o WHERE o.orderStatus = 'closed' AND o.account.profile.userID = :userID"),
    @NamedQuery(name = "Order.completeClosedOrders", query = "UPDATE Order o SET o.orderStatus = 'completed' WHERE o.orderStatus = 'closed' AND o.account.profile.userID = :userID")
})
public class Order implements Serializable {

    private static final long serialVersionUID = 120650490200739057L;

    @Id
    @TableGenerator(
        name = "orderIdGen", 
        table = "KEYGENEJB", 
        pkColumnName = "KEYNAME", 
        valueColumnName = "KEYVAL", 
        pkColumnValue = "order", 
        allocationSize = 1000
    )
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "orderIdGen")
    @Column(name = "ORDERID", nullable = false)
    private Integer orderID;

    @Column(name = "ORDERTYPE")
    private String orderType; // buy, sell, etc.

    @Column(name = "ORDERSTATUS")
    private String orderStatus; // open, processing, completed, closed, cancelled

    @Column(name = "OPENDATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date openDate;

    @Column(name = "COMPLETIONDATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date completionDate;

    @NotNull
    @Column(name = "QUANTITY", nullable = false)
    private double quantity;

    @Column(name = "PRICE")
    private BigDecimal price;

    @Column(name = "ORDERFEE")
    private BigDecimal orderFee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_ACCOUNTID")
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "QUOTE_SYMBOL")
    private Quote quote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "HOLDING_HOLDINGID")
    private Holding holding;

    @Transient
    private String symbol;

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

    // Getters and Setters
    public Integer getOrderID() {
        return orderID;
    }

    public void setOrderID(Integer orderID) {
        this.orderID = orderID;
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
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public int hashCode() {
        return orderID != null ? orderID.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Order)) return false;
        Order other = (Order) obj;
        return orderID != null && orderID.equals(other.orderID);
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderID=" + orderID +
                ", orderType='" + orderType + '\'' +
                ", orderStatus='" + orderStatus + '\'' +
                ", openDate=" + openDate +
                ", completionDate=" + completionDate +
                ", quantity=" + quantity +
                ", price=" + price +
                ", orderFee=" + orderFee +
                ", symbol='" + symbol + '\'' +
                '}';
    }
}

