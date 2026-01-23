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

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "holdingejb")
public class Holding extends PanacheEntity {

    @NotNull
    @Column(name = "QUANTITY", nullable = false)
    public double quantity;

    @Column(name = "PURCHASEPRICE")
    public BigDecimal purchasePrice;

    @Column(name = "PURCHASEDATE")
    @Temporal(TemporalType.TIMESTAMP)
    public Date purchaseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_ACCOUNTID")
    @JsonIgnore
    public Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "QUOTE_SYMBOL")
    public Quote quote;

    public Holding() {
    }

    public Holding(double quantity, BigDecimal purchasePrice, Date purchaseDate,
                   Account account, Quote quote) {
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.purchaseDate = purchaseDate;
        this.account = account;
        this.quote = quote;
    }

    // Panache finder methods
    public static List<Holding> findByAccountId(Long accountId) {
        return list("account.id", accountId);
    }

    public static List<Holding> findByUserID(String userID) {
        return list("account.profile.userID", userID);
    }

    public static List<Holding> findBySymbol(String symbol) {
        return list("quote.symbol", symbol);
    }

    // Getters and setters for compatibility
    public Long getHoldingID() {
        return id;
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

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
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

    public String getQuoteID() {
        return quote != null ? quote.symbol : null;
    }

    @Override
    public String toString() {
        return "Holding{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", purchasePrice=" + purchasePrice +
                ", purchaseDate=" + purchaseDate +
                ", quoteSymbol=" + (quote != null ? quote.symbol : null) +
                '}';
    }
}

