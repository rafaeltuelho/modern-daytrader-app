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
package com.ibm.websphere.samples.daytrader.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.ibm.websphere.samples.daytrader.entities.Account;
import com.ibm.websphere.samples.daytrader.entities.Holding;
import com.ibm.websphere.samples.daytrader.entities.Order;
import com.ibm.websphere.samples.daytrader.entities.Quote;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

/**
 * Trading service for buy/sell operations, orders, and holdings management.
 */
@ApplicationScoped
public class TradeService {

    private static final BigDecimal ORDER_FEE = new BigDecimal("24.95");

    @Inject
    MarketService marketService;

    /**
     * Purchase stock and create a new holding for the user.
     *
     * @param userID   the customer requesting the purchase
     * @param symbol   the stock symbol to purchase
     * @param quantity the number of shares to purchase
     * @return the created Order
     */
    @Transactional
    public Order buy(String userID, String symbol, double quantity) {
        validateUserID(userID);
        if (symbol == null || symbol.isBlank()) {
            throw new BadRequestException("Stock symbol is required");
        }
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }

        Account account = Account.findByProfileUserID(userID);
        if (account == null) {
            throw new NotFoundException("Account not found for user: " + userID);
        }

        Quote quote = Quote.findBySymbol(symbol);
        if (quote == null) {
            throw new NotFoundException("Quote not found for symbol: " + symbol);
        }

        // Calculate total cost
        BigDecimal price = quote.price;
        BigDecimal total = price.multiply(new BigDecimal(quantity)).add(ORDER_FEE);

        // Check sufficient funds
        if (account.balance.compareTo(total) < 0) {
            throw new BadRequestException("Insufficient funds. Required: " + total + 
                                         ", Available: " + account.balance);
        }

        // Debit account
        account.balance = account.balance.subtract(total);

        // Create holding
        Holding holding = new Holding(quantity, price, new Date(), account, quote);
        holding.persist();

        // Create and complete buy order
        Order order = new Order("buy", "closed", new Timestamp(System.currentTimeMillis()),
                               new Timestamp(System.currentTimeMillis()), quantity, 
                               price, ORDER_FEE, account, quote, holding);
        order.persist();

        // Update quote volume
        quote.volume += quantity;

        return order;
    }

    /**
     * Sell a stock holding and credit the user's account.
     *
     * @param userID    the customer requesting the sell
     * @param holdingId the holding to sell
     * @return the created sell Order
     */
    @Transactional
    public Order sell(String userID, Long holdingId) {
        validateUserID(userID);
        if (holdingId == null) {
            throw new BadRequestException("Holding ID is required");
        }

        Account account = Account.findByProfileUserID(userID);
        if (account == null) {
            throw new NotFoundException("Account not found for user: " + userID);
        }

        Holding holding = Holding.findById(holdingId);
        if (holding == null) {
            throw new NotFoundException("Holding not found: " + holdingId);
        }

        // Verify holding belongs to user
        if (holding.account == null || !holding.account.id.equals(account.id)) {
            throw new BadRequestException("Holding does not belong to user: " + userID);
        }

        Quote quote = holding.quote;
        BigDecimal price = quote.price;
        double quantity = holding.quantity;

        // Calculate proceeds
        BigDecimal proceeds = price.multiply(new BigDecimal(quantity)).subtract(ORDER_FEE);

        // Credit account
        account.balance = account.balance.add(proceeds);

        // Remove holding first (before creating order that would reference it)
        holding.delete();

        // Create sell order (holding is null since it's been deleted)
        Order order = new Order("sell", "closed", new Timestamp(System.currentTimeMillis()),
                               new Timestamp(System.currentTimeMillis()), quantity,
                               price, ORDER_FEE, account, quote, null);
        order.persist();

        // Update quote volume
        quote.volume += quantity;

        return order;
    }

    /**
     * Get all orders for a user.
     *
     * @param userID the customer account to retrieve orders for
     * @return Collection of orders
     */
    public List<Order> getOrders(String userID) {
        validateUserID(userID);
        return Order.findByUserID(userID);
    }

    /**
     * Get completed orders that need to be alerted to the user.
     * Also marks them as 'completed' to prevent repeated alerts.
     *
     * @param userID the customer account to retrieve orders for
     * @return Collection of closed orders
     */
    @Transactional
    public List<Order> getClosedOrders(String userID) {
        validateUserID(userID);
        List<Order> closedOrders = Order.findClosedOrdersByUserID(userID);

        // Mark orders as completed so they won't be returned again
        Order.completeClosedOrdersByUserID(userID);

        return closedOrders;
    }

    /**
     * Get all holdings for a user.
     *
     * @param userID the customer requesting the portfolio
     * @return Collection of holdings
     */
    public List<Holding> getHoldings(String userID) {
        validateUserID(userID);
        return Holding.findByUserID(userID);
    }

    /**
     * Get a specific holding by ID for a user.
     *
     * @param userID    the customer requesting the holding
     * @param holdingId the holding ID to return
     * @return the Holding, or null if not found or doesn't belong to user
     */
    public Holding getHolding(String userID, Long holdingId) {
        if (userID == null || userID.isBlank() || holdingId == null) {
            return null;
        }

        Holding holding = Holding.findById(holdingId);
        if (holding == null) {
            return null;
        }

        // Verify holding belongs to user
        Account account = Account.findByProfileUserID(userID);
        if (account == null || !holding.account.id.equals(account.id)) {
            return null;
        }

        return holding;
    }

    private void validateUserID(String userID) {
        if (userID == null || userID.isBlank()) {
            throw new BadRequestException("User ID is required");
        }
    }
}

