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

import com.ibm.websphere.samples.daytrader.entity.Order;

/**
 * DTO for Order entity
 * Per Phase 3: Backend Migration specification section 4.2
 */
public class OrderDTO {

    private Integer orderID;
    private String orderType;
    private String orderStatus;
    private Date openDate;
    private Date completionDate;
    private double quantity;
    private BigDecimal price;
    private BigDecimal orderFee;
    private String symbol;
    private String companyName;
    private Integer accountID;
    private Integer holdingID;

    public OrderDTO() {
    }

    public OrderDTO(Order order) {
        this.orderID = order.getOrderID();
        this.orderType = order.getOrderType();
        this.orderStatus = order.getOrderStatus();
        this.openDate = order.getOpenDate();
        this.completionDate = order.getCompletionDate();
        this.quantity = order.getQuantity();
        this.price = order.getPrice();
        this.orderFee = order.getOrderFee();
        
        if (order.getQuote() != null) {
            this.symbol = order.getQuote().getSymbol();
            this.companyName = order.getQuote().getCompanyName();
        }
        
        if (order.getAccount() != null) {
            this.accountID = order.getAccount().getAccountID();
        }
        
        if (order.getHolding() != null) {
            this.holdingID = order.getHolding().getHoldingID();
        }
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

    public Integer getAccountID() {
        return accountID;
    }

    public void setAccountID(Integer accountID) {
        this.accountID = accountID;
    }

    public Integer getHoldingID() {
        return holdingID;
    }

    public void setHoldingID(Integer holdingID) {
        this.holdingID = holdingID;
    }
}

