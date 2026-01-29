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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Quote entity - represents a stock quote/security
 * Migrated from QuoteDataBean per Phase 3: Backend Migration specification
 */
@Entity
@Table(name = "quoteejb")
@NamedQueries({
    @NamedQuery(name = "Quote.allQuotes", query = "SELECT q FROM Quote q"),
    @NamedQuery(name = "Quote.findBySymbol", query = "SELECT q FROM Quote q WHERE q.symbol = :symbol")
})
public class Quote implements Serializable {

    private static final long serialVersionUID = 1847932261895838791L;

    @Id
    @NotNull
    @Column(name = "SYMBOL", nullable = false)
    private String symbol;

    @Column(name = "COMPANYNAME")
    private String companyName;

    @NotNull
    @Column(name = "VOLUME", nullable = false)
    private double volume;

    @Column(name = "PRICE")
    private BigDecimal price;

    @Column(name = "OPEN1")
    private BigDecimal open;

    @Column(name = "LOW")
    private BigDecimal low;

    @Column(name = "HIGH")
    private BigDecimal high;

    @NotNull
    @Column(name = "CHANGE1", nullable = false)
    private double change;

    public Quote() {
    }

    public Quote(String symbol) {
        this.symbol = symbol;
    }

    public Quote(String symbol, String companyName, double volume, BigDecimal price, 
                 BigDecimal open, BigDecimal low, BigDecimal high, double change) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.volume = volume;
        this.price = price;
        this.open = open;
        this.low = low;
        this.high = high;
        this.change = change;
    }

    // Getters and Setters
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

    public BigDecimal getOpen() {
        return open;
    }

    public void setOpen(BigDecimal open) {
        this.open = open;
    }

    public BigDecimal getLow() {
        return low;
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    @Override
    public int hashCode() {
        return symbol != null ? symbol.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Quote)) return false;
        Quote other = (Quote) obj;
        return symbol != null && symbol.equals(other.symbol);
    }

    @Override
    public String toString() {
        return "Quote{" +
                "symbol='" + symbol + '\'' +
                ", companyName='" + companyName + '\'' +
                ", volume=" + volume +
                ", price=" + price +
                ", open=" + open +
                ", low=" + low +
                ", high=" + high +
                ", change=" + change +
                '}';
    }
}

