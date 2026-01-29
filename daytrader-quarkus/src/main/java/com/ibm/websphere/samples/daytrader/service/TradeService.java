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
package com.ibm.websphere.samples.daytrader.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.ibm.websphere.samples.daytrader.dto.AccountDTO;
import com.ibm.websphere.samples.daytrader.dto.AccountProfileDTO;
import com.ibm.websphere.samples.daytrader.dto.HoldingDTO;
import com.ibm.websphere.samples.daytrader.dto.MarketSummaryDTO;
import com.ibm.websphere.samples.daytrader.dto.OrderDTO;
import com.ibm.websphere.samples.daytrader.dto.PortfolioSummaryDTO;
import com.ibm.websphere.samples.daytrader.dto.QuoteDTO;
import com.ibm.websphere.samples.daytrader.entity.Account;
import com.ibm.websphere.samples.daytrader.entity.AccountProfile;
import com.ibm.websphere.samples.daytrader.entity.Holding;
import com.ibm.websphere.samples.daytrader.entity.Order;
import com.ibm.websphere.samples.daytrader.entity.Quote;
import com.ibm.websphere.samples.daytrader.repository.AccountProfileRepository;
import com.ibm.websphere.samples.daytrader.repository.AccountRepository;
import com.ibm.websphere.samples.daytrader.repository.HoldingRepository;
import com.ibm.websphere.samples.daytrader.repository.OrderRepository;
import com.ibm.websphere.samples.daytrader.repository.QuoteRepository;
import com.ibm.websphere.samples.daytrader.util.FinancialUtils;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.jboss.logging.Logger;

/**
 * Main trading service - replaces TradeSLSBBean
 * Per Phase 3: Backend Migration specification section 2.1
 * 
 * This is an @ApplicationScoped CDI bean that provides core trading operations.
 * All public methods form the primary internal API for trading operations.
 */
@ApplicationScoped
public class TradeService {

    private static final Logger LOG = Logger.getLogger(TradeService.class);

    @Inject
    AccountRepository accountRepository;

    @Inject
    AccountProfileRepository accountProfileRepository;

    @Inject
    QuoteRepository quoteRepository;

    @Inject
    OrderRepository orderRepository;

    @Inject
    HoldingRepository holdingRepository;

    @Inject
    MarketSummaryService marketSummaryService;

    @Inject
    EntityManager entityManager;

    /**
     * Login user and update login statistics
     */
    @Transactional
    public AccountDTO login(String userID, String password) {
        LOG.debugf("Login attempt for user: %s", userID);
        
        AccountProfile profile = accountProfileRepository.authenticate(userID, password)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        
        Account account = accountRepository.findByProfileUserID(userID)
                .orElseThrow(() -> new IllegalStateException("Account not found for user: " + userID));
        
        account.login(password);
        accountRepository.persist(account);
        
        LOG.infof("User %s logged in successfully", userID);
        return new AccountDTO(account);
    }

    /**
     * Logout user and update logout statistics
     */
    @Transactional
    public void logout(String userID) {
        LOG.debugf("Logout for user: %s", userID);
        
        Account account = accountRepository.findByProfileUserID(userID)
                .orElseThrow(() -> new IllegalArgumentException("Account not found for user: " + userID));
        
        account.logout();
        accountRepository.persist(account);
        
        LOG.infof("User %s logged out successfully", userID);
    }

    /**
     * Get account information by accountID
     */
    public AccountDTO getAccountData(Integer accountID) {
        Account account = accountRepository.findByIdWithProfile(accountID)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountID));
        return new AccountDTO(account);
    }

    /**
     * Get account information by userID
     */
    public AccountDTO getAccountDataByUserID(String userID) {
        LOG.debugf("Get account data for user: %s", userID);
        Account account = accountRepository.findByProfileUserID(userID)
                .orElseThrow(() -> new IllegalArgumentException("Account not found for user: " + userID));
        return new AccountDTO(account);
    }

    /**
     * Get quote by symbol
     */
    public QuoteDTO getQuote(String symbol) {
        Quote quote = quoteRepository.findBySymbol(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found for symbol: " + symbol));
        return new QuoteDTO(quote);
    }

    /**
     * Get all quotes
     */
    public List<QuoteDTO> getAllQuotes() {
        return quoteRepository.findAllQuotes().stream()
                .map(QuoteDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Create a new quote
     */
    @Transactional
    public QuoteDTO createQuote(String symbol, String companyName, BigDecimal price) {
        Quote quote = new Quote(symbol, companyName, 0, price, price, price, price, 0.0);
        quoteRepository.persist(quote);
        LOG.infof("Created quote for symbol: %s", symbol);
        return new QuoteDTO(quote);
    }

    /**
     * Update quote price
     */
    @Transactional
    public QuoteDTO updateQuotePrice(String symbol, BigDecimal newPrice) {
        Quote quote = quoteRepository.findForUpdate(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found: " + symbol));
        
        BigDecimal oldPrice = quote.getPrice();
        quote.setPrice(newPrice);
        quote.setChange(newPrice.subtract(oldPrice).doubleValue());
        
        quoteRepository.persist(quote);
        LOG.debugf("Updated quote %s: old price=%s, new price=%s", symbol, oldPrice, newPrice);
        return new QuoteDTO(quote);
    }

    /**
     * Register a new user account
     */
    @Transactional
    public AccountDTO register(String userID, String password, String fullName,
                               String address, String email, String creditCard,
                               BigDecimal openBalance) {
        // Check if user already exists
        if (accountProfileRepository.existsByUserID(userID)) {
            throw new IllegalArgumentException("User ID already exists: " + userID);
        }

        // Create profile
        AccountProfile profile = new AccountProfile(userID, password, fullName, address, email, creditCard);
        accountProfileRepository.persist(profile);

        // Create account
        Account account = new Account();
        account.setCreationDate(new Date());
        account.setOpenBalance(openBalance);
        account.setBalance(openBalance);
        account.setLoginCount(0);
        account.setLogoutCount(0);
        account.setProfile(profile);
        accountRepository.persist(account);

        // Set bidirectional relationship
        profile.setAccount(account);
        accountProfileRepository.persist(profile);

        LOG.infof("Registered new user: %s with account ID: %s", userID, account.getAccountID());
        return new AccountDTO(account);
    }

    /**
     * Buy stock shares - creates an order and updates account balance
     * Per Phase 2: Feature Implementation - Core Trading Operations
     */
    @Transactional
    public OrderDTO buy(String userID, String symbol, double quantity, int orderProcessingMode) {
        LOG.debugf("Buy: user=%s, symbol=%s, quantity=%s, mode=%s", userID, symbol, quantity, orderProcessingMode);

        try {
            // Get user account and quote
            AccountProfile profile = accountProfileRepository.findByUserID(userID)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userID));
            Account account = profile.getAccount();
            Quote quote = quoteRepository.findBySymbol(symbol)
                    .orElseThrow(() -> new IllegalArgumentException("Quote not found: " + symbol));

            // Calculate total cost
            BigDecimal price = quote.getPrice();
            BigDecimal orderFee = TradeConfig.getOrderFee("buy");
            BigDecimal total = new BigDecimal(quantity).multiply(price).add(orderFee);

            // Check for insufficient funds
            if (account.getBalance().compareTo(total) < 0) {
                throw new IllegalArgumentException("Insufficient funds: balance=" + account.getBalance() + ", required=" + total);
            }

            // Create buy order
            Order order = createOrder(account, quote, null, "buy", quantity);

            // Deduct cost from account balance
            account.setBalance(account.getBalance().subtract(total));
            accountRepository.persist(account);

            // Flush to ensure balance update is persisted before completeOrder clears the entity manager
            entityManager.flush();

            // Complete order synchronously (async processing not implemented yet)
            OrderDTO completedOrder = null;
            if (orderProcessingMode == TradeConfig.SYNCH) {
                completedOrder = completeOrder(order.getOrderID());
            }

            LOG.infof("Buy order created: orderID=%s, user=%s, symbol=%s, quantity=%s",
                     order.getOrderID(), userID, symbol, quantity);

            // Return the completed order if synchronous, otherwise return the open order
            return completedOrder != null ? completedOrder : new OrderDTO(order);

        } catch (IllegalArgumentException e) {
            // Re-throw IllegalArgumentException directly for proper REST error handling
            LOG.warnf("Buy operation validation failed: %s", e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error("Buy operation failed", e);
            throw new RuntimeException("Buy operation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Sell holding - creates a sell order and updates account balance
     * Per Phase 2: Feature Implementation - Core Trading Operations
     */
    @Transactional
    public OrderDTO sell(String userID, Integer holdingID, int orderProcessingMode) {
        LOG.debugf("Sell: user=%s, holdingID=%s, mode=%s", userID, holdingID, orderProcessingMode);

        try {
            // Get account ID using a projection query to avoid loading the entity graph
            Integer accountId;
            try {
                accountId = (Integer) entityManager.createQuery(
                    "SELECT a.accountID FROM Account a WHERE a.profile.userID = :userId")
                    .setParameter("userId", userID)
                    .getSingleResult();
            } catch (jakarta.persistence.NoResultException e) {
                throw new IllegalArgumentException("User not found: " + userID);
            }

            if (accountId == null) {
                throw new IllegalArgumentException("User not found: " + userID);
            }

            // Use a projection query to get holding data without loading the entity graph
            // This avoids transient entity issues caused by entity relationships
            Object[] holdingData;
            try {
                holdingData = (Object[]) entityManager.createQuery(
                    "SELECT h.holdingID, h.quantity, h.quote.price, h.quote.symbol FROM Holding h WHERE h.holdingID = :id")
                    .setParameter("id", holdingID)
                    .getSingleResult();
            } catch (jakarta.persistence.NoResultException e) {
                LOG.warnf("User %s attempted to sell holding %s which has already been sold", userID, holdingID);
                Order cancelledOrder = new Order();
                cancelledOrder.setOrderStatus("cancelled");
                orderRepository.persist(cancelledOrder);
                return new OrderDTO(cancelledOrder);
            }

            double quantity = ((Number) holdingData[1]).doubleValue();
            BigDecimal price = (BigDecimal) holdingData[2];
            String symbol = (String) holdingData[3];

            // Clear persistence context to ensure clean state for order creation
            entityManager.clear();

            // Get the quote and account fresh after clearing
            Quote quote = quoteRepository.findBySymbol(symbol)
                    .orElseThrow(() -> new IllegalArgumentException("Quote not found: " + symbol));
            Account account = accountRepository.find("accountID", accountId).firstResult();

            // Create sell order WITHOUT holding reference to avoid transient entity issues
            Order order = createOrder(account, quote, null, "sell", quantity);
            Integer orderID = order.getOrderID();

            // Flush to persist the order
            orderRepository.flush();

            // Set holding reference via native SQL to avoid entity state issues
            orderRepository.setHoldingReference(orderID, holdingID);

            // Mark holding as "in-flight" by updating purchaseDate via native SQL
            entityManager.createNativeQuery(
                "UPDATE holdingejb SET PURCHASEDATE = ?1 WHERE HOLDINGID = ?2")
                .setParameter(1, new Timestamp(0))
                .setParameter(2, holdingID)
                .executeUpdate();

            // Credit account balance using native SQL to avoid entity graph issues
            BigDecimal orderFee = order.getOrderFee();
            BigDecimal total = new BigDecimal(quantity).multiply(price).subtract(orderFee);
            entityManager.createNativeQuery(
                "UPDATE accountejb SET BALANCE = BALANCE + ?1 WHERE ACCOUNTID = ?2")
                .setParameter(1, total)
                .setParameter(2, accountId)
                .executeUpdate();

            // Flush all pending changes and clear persistence context
            // This ensures completeOrder() fetches fresh data including the holding reference
            entityManager.flush();
            entityManager.clear();

            // Complete order synchronously (async processing not implemented yet)
            Integer orderIdToComplete = order.getOrderID();
            OrderDTO completedOrder = null;
            if (orderProcessingMode == TradeConfig.SYNCH) {
                completedOrder = completeOrder(orderIdToComplete);
            }

            LOG.infof("Sell order created: orderID=%s, user=%s, holdingID=%s, quantity=%s",
                     order.getOrderID(), userID, holdingID, quantity);

            // Return the completed order if synchronous, otherwise return the open order
            return completedOrder != null ? completedOrder : new OrderDTO(order);

        } catch (IllegalArgumentException e) {
            // Re-throw IllegalArgumentException directly for proper REST error handling
            LOG.warnf("Sell operation validation failed: %s", e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error("Sell operation failed", e);
            throw new RuntimeException("Sell operation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Complete an order - creates holding for buy, removes holding for sell
     * Per Phase 2: Feature Implementation - Core Trading Operations
     */
    @Transactional
    public OrderDTO completeOrder(Integer orderID) {
        LOG.debugf("Complete order: orderID=%s", orderID);

        // Get order type and holding ID using projection query to avoid loading entity graph
        Object[] orderData = (Object[]) entityManager.createQuery(
            "SELECT o.orderType, o.holding.holdingID, o.orderStatus, o.price, o.quantity, " +
            "o.account.accountID, o.quote.symbol " +
            "FROM Order o WHERE o.orderID = :id")
            .setParameter("id", orderID)
            .getSingleResult();

        if (orderData == null) {
            throw new IllegalArgumentException("Order not found: " + orderID);
        }

        String orderType = (String) orderData[0];
        Integer holdingId = (Integer) orderData[1];
        String orderStatus = (String) orderData[2];
        BigDecimal price = (BigDecimal) orderData[3];
        double quantity = ((Number) orderData[4]).doubleValue();
        Integer accountId = (Integer) orderData[5];
        String symbol = (String) orderData[6];

        if ("completed".equals(orderStatus) || "closed".equals(orderStatus)) {
            throw new IllegalStateException("Order already completed: " + orderID);
        }

        if ("buy".equalsIgnoreCase(orderType)) {
            // For buy orders, load full entities since we need to create new holding
            entityManager.clear();
            Order order = orderRepository.findByIdWithDetails(orderID)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderID));
            Account account = order.getAccount();
            Quote quote = order.getQuote();

            // Create new holding for buy order
            Holding newHolding = createHolding(account, quote, quantity, price);
            order.setHolding(newHolding);

            // Mark order as closed
            order.setOrderStatus("closed");
            order.setCompletionDate(new Timestamp(System.currentTimeMillis()));
            orderRepository.persist(order);

            LOG.infof("Order completed: orderID=%s, type=%s", orderID, orderType);
            return new OrderDTO(order);

        } else if ("sell".equalsIgnoreCase(orderType)) {
            // For sell orders, use native SQL to avoid entity graph issues
            if (holdingId == null) {
                // Mark order as cancelled via native SQL
                entityManager.createNativeQuery(
                    "UPDATE orderejb SET ORDERSTATUS = 'cancelled', COMPLETIONDATE = ?1 WHERE ORDERID = ?2")
                    .setParameter(1, new Timestamp(System.currentTimeMillis()))
                    .setParameter(2, orderID)
                    .executeUpdate();
                throw new IllegalStateException("Unable to sell order " + orderID + " - holding already sold");
            }

            // Clear ALL orders' references to this holding via native SQL
            entityManager.createNativeQuery(
                "UPDATE orderejb SET HOLDING_HOLDINGID = NULL WHERE HOLDING_HOLDINGID = ?1")
                .setParameter(1, holdingId)
                .executeUpdate();

            // Delete the holding via native SQL
            entityManager.createNativeQuery(
                "DELETE FROM holdingejb WHERE HOLDINGID = ?1")
                .setParameter(1, holdingId)
                .executeUpdate();

            // Mark order as closed via native SQL
            entityManager.createNativeQuery(
                "UPDATE orderejb SET ORDERSTATUS = 'closed', COMPLETIONDATE = ?1 WHERE ORDERID = ?2")
                .setParameter(1, new Timestamp(System.currentTimeMillis()))
                .setParameter(2, orderID)
                .executeUpdate();

            LOG.infof("Order completed: orderID=%s, type=%s", orderID, orderType);

            // Return a minimal DTO for sell orders
            Order orderForDto = new Order();
            orderForDto.setOrderID(orderID);
            orderForDto.setOrderType(orderType);
            orderForDto.setOrderStatus("closed");
            orderForDto.setCompletionDate(new Timestamp(System.currentTimeMillis()));
            orderForDto.setPrice(price);
            orderForDto.setQuantity(quantity);
            return new OrderDTO(orderForDto);
        }

        throw new IllegalStateException("Unknown order type: " + orderType);
    }

    /**
     * Cancel an order
     * Per Phase 2: Feature Implementation - Core Trading Operations
     */
    @Transactional
    public void cancelOrder(Integer orderID) {
        LOG.debugf("Cancel order: orderID=%s", orderID);

        Order order = orderRepository.findByIdWithDetails(orderID)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderID));

        order.setOrderStatus("cancelled");
        order.setCompletionDate(new Timestamp(System.currentTimeMillis()));
        orderRepository.persist(order);

        LOG.infof("Order cancelled: orderID=%s", orderID);
    }

    /**
     * Get user's orders
     * Per Phase 2: Feature Implementation - Core Trading Operations
     */
    public List<OrderDTO> getOrders(String userID) {
        LOG.debugf("Get orders for user: %s", userID);

        Account account = accountRepository.findByProfileUserID(userID)
                .orElseThrow(() -> new IllegalArgumentException("Account not found for user: " + userID));

        return orderRepository.findByAccountId(account.getAccountID()).stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get closed orders and mark them as completed
     * Per Phase 2: Feature Implementation - Core Trading Operations
     */
    @Transactional
    public List<OrderDTO> getClosedOrders(String userID) {
        LOG.debugf("Get closed orders for user: %s", userID);

        List<Order> closedOrders = orderRepository.findClosedOrdersByUserId(userID);

        // Mark closed orders as completed
        orderRepository.completeClosedOrders(userID);

        return closedOrders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get user's portfolio holdings
     * Per Phase 2: Feature Implementation - Core Trading Operations
     */
    public List<HoldingDTO> getHoldings(String userID) {
        LOG.debugf("Get holdings for user: %s", userID);

        Account account = accountRepository.findByProfileUserID(userID)
                .orElseThrow(() -> new IllegalArgumentException("Account not found for user: " + userID));

        return holdingRepository.findByAccountIdWithQuote(account.getAccountID()).stream()
                .map(HoldingDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get single holding by ID
     * Per Phase 2: Feature Implementation - Core Trading Operations
     */
    public HoldingDTO getHolding(Integer holdingID) {
        LOG.debugf("Get holding: holdingID=%s", holdingID);

        Holding holding = holdingRepository.findByIdWithQuote(holdingID)
                .orElseThrow(() -> new IllegalArgumentException("Holding not found: " + holdingID));

        return new HoldingDTO(holding);
    }

    /**
     * Get portfolio summary for a user
     * Calculates total holdings value, gains, and other portfolio statistics
     */
    public PortfolioSummaryDTO getPortfolioSummary(String userID) {
        LOG.debugf("Get portfolio summary for user: %s", userID);

        Account account = accountRepository.findByProfileUserID(userID)
                .orElseThrow(() -> new IllegalArgumentException("Account not found for user: " + userID));

        // Get all holdings with current prices
        List<Holding> holdings = holdingRepository.findByAccountIdWithQuote(account.getAccountID());

        // Calculate total holdings value
        BigDecimal holdingsValue = BigDecimal.ZERO;
        for (Holding holding : holdings) {
            if (holding.getQuote() != null && holding.getQuote().getPrice() != null) {
                BigDecimal marketValue = holding.getQuote().getPrice()
                        .multiply(new BigDecimal(holding.getQuantity()));
                holdingsValue = holdingsValue.add(marketValue);
            }
        }

        return new PortfolioSummaryDTO(
                account.getAccountID(),
                account.getBalance(),
                account.getOpenBalance(),
                holdingsValue,
                holdings.size()
        );
    }

    /**
     * Get account profile data
     * Per Phase 2: Market Summary & Profiles
     */
    public AccountProfileDTO getAccountProfileData(String userID) {
        LOG.debugf("Get account profile: userID=%s", userID);

        AccountProfile profile = accountProfileRepository.findByUserID(userID)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + userID));

        return new AccountProfileDTO(profile);
    }

    /**
     * Update account profile
     * Per Phase 2: Market Summary & Profiles
     */
    @Transactional
    public AccountProfileDTO updateAccountProfile(AccountProfileDTO profileData) {
        LOG.debugf("Update account profile: userID=%s", profileData.getUserID());

        AccountProfile profile = accountProfileRepository.findByUserID(profileData.getUserID())
                .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + profileData.getUserID()));

        profile.setAddress(profileData.getAddress());
        profile.setPassword(profileData.getPassword());
        profile.setFullName(profileData.getFullName());
        profile.setCreditCard(profileData.getCreditCard());
        profile.setEmail(profileData.getEmail());

        accountProfileRepository.persist(profile);

        LOG.infof("Profile updated: userID=%s", profileData.getUserID());
        return new AccountProfileDTO(profile);
    }

    /**
     * Update quote price and volume
     * Per Phase 2: Market Summary & Profiles
     */
    @Transactional
    public QuoteDTO updateQuotePriceVolume(String symbol, BigDecimal changeFactor, double sharesTraded) {
        LOG.debugf("Update quote price/volume: symbol=%s, changeFactor=%s, sharesTraded=%s",
                  symbol, changeFactor, sharesTraded);

        Quote quote = quoteRepository.findForUpdate(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found: " + symbol));

        BigDecimal oldPrice = quote.getPrice();

        // Handle penny stocks and maximum price
        if (oldPrice.compareTo(TradeConfig.PENNY_STOCK_PRICE) == 0) {
            changeFactor = TradeConfig.PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER;
        } else if (oldPrice.compareTo(TradeConfig.MAXIMUM_STOCK_PRICE) > 0) {
            changeFactor = TradeConfig.MAXIMUM_STOCK_SPLIT_MULTIPLIER;
        }

        BigDecimal newPrice = oldPrice.multiply(changeFactor).setScale(FinancialUtils.SCALE, FinancialUtils.ROUND);
        quote.setPrice(newPrice);
        quote.setChange(newPrice.subtract(oldPrice).doubleValue());
        quote.setVolume(quote.getVolume() + sharesTraded);

        quoteRepository.persist(quote);

        LOG.debugf("Quote updated: symbol=%s, oldPrice=%s, newPrice=%s", symbol, oldPrice, newPrice);
        return new QuoteDTO(quote);
    }

    /**
     * Get market summary
     * Per Phase 2: Market Summary & Profiles
     */
    public MarketSummaryDTO getMarketSummary() {
        LOG.debug("Get market summary");
        return marketSummaryService.getMarketSummary();
    }

    /**
     * Helper method to create an order
     */
    private Order createOrder(Account account, Quote quote, Holding holding, String orderType, double quantity) {
        LOG.debugf("Create order: account=%s, symbol=%s, type=%s, quantity=%s",
                  account.getAccountID(), quote.getSymbol(), orderType, quantity);

        BigDecimal price = quote.getPrice().setScale(FinancialUtils.SCALE, FinancialUtils.ROUND);
        BigDecimal orderFee = TradeConfig.getOrderFee(orderType);

        // Ensure holding reference is properly managed in the persistence context
        // Use getReference to get a proxy without loading, avoiding transient entity issues
        Holding managedHolding = null;
        if (holding != null && holding.getHoldingID() != null) {
            managedHolding = entityManager.getReference(Holding.class, holding.getHoldingID());
        }

        Order order = new Order(
            orderType,
            "open",
            new Timestamp(System.currentTimeMillis()),
            null,
            quantity,
            price,
            orderFee,
            account,
            quote,
            managedHolding
        );

        orderRepository.persist(order);
        return order;
    }

    /**
     * Helper method to create a holding
     */
    private Holding createHolding(Account account, Quote quote, double quantity, BigDecimal purchasePrice) {
        LOG.debugf("Create holding: account=%s, symbol=%s, quantity=%s, price=%s",
                  account.getAccountID(), quote.getSymbol(), quantity, purchasePrice);

        Holding holding = new Holding(
            quantity,
            purchasePrice,
            new Timestamp(System.currentTimeMillis()),
            account,
            quote
        );

        holdingRepository.persist(holding);
        holdingRepository.flush(); // Ensure holding is persisted before referencing in order
        return holding;
    }
}

