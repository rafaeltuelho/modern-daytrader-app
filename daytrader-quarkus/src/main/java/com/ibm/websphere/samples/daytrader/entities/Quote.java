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
import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "quoteejb")
public class Quote extends PanacheEntityBase {

    @Id
    @NotNull
    @Column(name = "SYMBOL", nullable = false)
    public String symbol;

    @Column(name = "COMPANYNAME")
    public String companyName;

    @NotNull
    @Column(name = "VOLUME", nullable = false)
    public double volume;

    @Column(name = "PRICE")
    public BigDecimal price;

    @Column(name = "OPEN1")
    public BigDecimal open;

    @Column(name = "LOW")
    public BigDecimal low;

    @Column(name = "HIGH")
    public BigDecimal high;

    @NotNull
    @Column(name = "CHANGE1", nullable = false)
    public double change;

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

    // Panache finder methods
    public static Quote findBySymbol(String symbol) {
        return findById(symbol);
    }

    public static List<Quote> findAllQuotes() {
        return listAll();
    }

    // Getters and setters for compatibility
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

