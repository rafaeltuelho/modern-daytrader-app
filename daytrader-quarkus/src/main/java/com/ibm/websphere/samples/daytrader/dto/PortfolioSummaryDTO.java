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
import java.math.RoundingMode;

/**
 * DTO for Portfolio Summary
 * Provides aggregated portfolio statistics for the dashboard
 */
public class PortfolioSummaryDTO {

    private Integer accountID;
    private BigDecimal balance;
    private BigDecimal openBalance;
    private BigDecimal holdingsValue;
    private BigDecimal totalValue;
    private BigDecimal gain;
    private BigDecimal gainPercent;
    private int numberOfHoldings;

    public PortfolioSummaryDTO() {
    }

    public PortfolioSummaryDTO(Integer accountID, BigDecimal balance, BigDecimal openBalance,
                               BigDecimal holdingsValue, int numberOfHoldings) {
        this.accountID = accountID;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.openBalance = openBalance != null ? openBalance : BigDecimal.ZERO;
        this.holdingsValue = holdingsValue != null ? holdingsValue : BigDecimal.ZERO;
        this.numberOfHoldings = numberOfHoldings;
        
        // Calculate total value (cash + holdings)
        this.totalValue = this.balance.add(this.holdingsValue);
        
        // Calculate gain (total value - open balance)
        this.gain = this.totalValue.subtract(this.openBalance);
        
        // Calculate gain percent
        if (this.openBalance.compareTo(BigDecimal.ZERO) > 0) {
            this.gainPercent = this.gain.multiply(new BigDecimal("100"))
                    .divide(this.openBalance, 2, RoundingMode.HALF_UP);
        } else {
            this.gainPercent = BigDecimal.ZERO;
        }
    }

    // Getters and Setters
    public Integer getAccountID() {
        return accountID;
    }

    public void setAccountID(Integer accountID) {
        this.accountID = accountID;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getOpenBalance() {
        return openBalance;
    }

    public void setOpenBalance(BigDecimal openBalance) {
        this.openBalance = openBalance;
    }

    public BigDecimal getHoldingsValue() {
        return holdingsValue;
    }

    public void setHoldingsValue(BigDecimal holdingsValue) {
        this.holdingsValue = holdingsValue;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public BigDecimal getGain() {
        return gain;
    }

    public void setGain(BigDecimal gain) {
        this.gain = gain;
    }

    public BigDecimal getGainPercent() {
        return gainPercent;
    }

    public void setGainPercent(BigDecimal gainPercent) {
        this.gainPercent = gainPercent;
    }

    public int getNumberOfHoldings() {
        return numberOfHoldings;
    }

    public void setNumberOfHoldings(int numberOfHoldings) {
        this.numberOfHoldings = numberOfHoldings;
    }
}

