/**
 * (C) Copyright IBM Corporation 2024.
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
package com.ibm.websphere.samples.daytrader.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.ibm.websphere.samples.daytrader.entity.Holding;

/**
 * DTO for Holding entity
 * Per Phase 3: Backend Migration specification section 4.2
 */
public class HoldingDTO {

    private Integer holdingID;
    private double quantity;
    private BigDecimal purchasePrice;
    private Date purchaseDate;
    private String symbol;
    private String companyName;
    private BigDecimal currentPrice;
    private BigDecimal marketValue;
    private BigDecimal gain;

    public HoldingDTO() {
    }

    public HoldingDTO(Holding holding) {
        this.holdingID = holding.getHoldingID();
        this.quantity = holding.getQuantity();
        this.purchasePrice = holding.getPurchasePrice();
        this.purchaseDate = holding.getPurchaseDate();
        
        if (holding.getQuote() != null) {
            this.symbol = holding.getQuote().getSymbol();
            this.companyName = holding.getQuote().getCompanyName();
            this.currentPrice = holding.getQuote().getPrice();
            
            // Calculate market value and gain
            if (this.currentPrice != null && this.purchasePrice != null) {
                this.marketValue = this.currentPrice.multiply(new BigDecimal(quantity));
                BigDecimal purchaseValue = this.purchasePrice.multiply(new BigDecimal(quantity));
                this.gain = this.marketValue.subtract(purchaseValue);
            }
        }
    }

    // Getters and Setters
    public Integer getHoldingID() {
        return holdingID;
    }

    public void setHoldingID(Integer holdingID) {
        this.holdingID = holdingID;
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

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(BigDecimal marketValue) {
        this.marketValue = marketValue;
    }

    public BigDecimal getGain() {
        return gain;
    }

    public void setGain(BigDecimal gain) {
        this.gain = gain;
    }
}

