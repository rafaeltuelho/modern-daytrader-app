package com.daytrader.trading.messaging;

import com.daytrader.common.dto.BalanceUpdateRequest;
import com.daytrader.common.dto.HoldingDTO;
import com.daytrader.common.dto.OrderDTO;
import com.daytrader.common.dto.QuoteDTO;
import com.daytrader.common.event.OrderCompletedEvent;
import com.daytrader.common.event.OrderCreatedEvent;
import com.daytrader.trading.client.AccountServiceClient;
import com.daytrader.trading.client.MarketServiceClient;
import com.daytrader.trading.service.HoldingService;
import com.daytrader.trading.service.OrderService;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Consumer for Order events.
 *
 * Uses in-memory channels by default for simplicity.
 * Can be switched to Kafka by activating the 'kafka' profile.
 * See ADR-002 for rationale.
 *
 * Processes order events from the "orders-out" channel (internal wiring).
 * When using Kafka, configure separate topics with channel aliasing.
 *
 * Implements complete order processing workflow per ADR-003:
 * - Fetches current quote price from Market Service
 * - Updates account balance via Account Service
 * - Creates/updates holdings
 * - Completes or cancels orders based on processing result
 */
@ApplicationScoped
public class OrderEventConsumer {

    private static final Logger LOG = Logger.getLogger(OrderEventConsumer.class);
    private static final BigDecimal ORDER_FEE = new BigDecimal("9.95");

    @Inject
    OrderService orderService;

    @Inject
    HoldingService holdingService;

    @Inject
    @RestClient
    MarketServiceClient marketServiceClient;

    @Inject
    @RestClient
    AccountServiceClient accountServiceClient;

    /**
     * Process order events from the internal channel.
     * Implements complete order processing workflow.
     */
    @Incoming("orders-out")
    @Blocking
    public void processOrderEvent(Object event) {
        if (event instanceof OrderCreatedEvent orderCreated) {
            processOrderCreated(orderCreated);
        } else if (event instanceof OrderCompletedEvent orderCompleted) {
            LOG.infof("Order completed: orderId=%d, status=%s",
                    orderCompleted.orderId(), orderCompleted.orderStatus());
        } else {
            LOG.warnf("Received unknown event type: %s", event.getClass().getName());
        }
    }

    /**
     * Process OrderCreatedEvent - implements complete order workflow.
     * Per ADR-003: fetch price, update balance, create holding, complete order.
     */
    private void processOrderCreated(OrderCreatedEvent event) {
        LOG.infof("Processing order: id=%d, type=%s, symbol=%s, quantity=%.2f",
                event.orderId(), event.orderType(), event.quoteSymbol(), event.quantity());

        try {
            // 1. Get quote price (Market Service endpoints are public for GET)
            BigDecimal price = getQuotePrice(event.quoteSymbol());

            // 2. Calculate total
            BigDecimal quantity = BigDecimal.valueOf(event.quantity());
            BigDecimal total = quantity.multiply(price).add(ORDER_FEE);

            if ("buy".equalsIgnoreCase(event.orderType())) {
                processBuyOrder(event, price, total);
            } else if ("sell".equalsIgnoreCase(event.orderType())) {
                processSellOrder(event, price, total);
            }

        } catch (Exception e) {
            LOG.errorf(e, "Failed to process order %d, cancelling", event.orderId());
            cancelOrder(event.orderId(), event.accountId());
        }
    }

    /**
     * Process buy order: debit account, create holding, complete order.
     */
    private void processBuyOrder(OrderCreatedEvent event, BigDecimal price, BigDecimal total) {
        // 1. Debit account balance (use negative amount)
        updateAccountBalance(event.accountId(), total.negate(), "ORDER_BUY_" + event.orderId());

        // 2. Create holding
        HoldingDTO holding = new HoldingDTO();
        holding.setAccountId(event.accountId());
        holding.setSymbol(event.quoteSymbol());
        holding.setQuantity(event.quantity());
        holding.setPurchasePrice(price);
        holding.setPurchaseDate(Instant.now());

        HoldingDTO created = holdingService.createHolding(holding);
        LOG.infof("Created holding %d for order %d", created.getId(), event.orderId());

        // 3. Complete the order with price
        completeOrderWithPrice(event.orderId(), price);
    }

    /**
     * Process sell order: credit account, complete order.
     * TODO: Remove or update the holding (future enhancement).
     */
    private void processSellOrder(OrderCreatedEvent event, BigDecimal price, BigDecimal total) {
        // For sell orders, we credit the account (positive amount, minus fee)
        BigDecimal quantity = BigDecimal.valueOf(event.quantity());
        BigDecimal proceeds = quantity.multiply(price).subtract(ORDER_FEE);

        updateAccountBalance(event.accountId(), proceeds, "ORDER_SELL_" + event.orderId());

        // TODO: Remove or update the holding
        // For now, just complete the order
        completeOrderWithPrice(event.orderId(), price);
    }

    /**
     * Fetch current quote price from Market Service.
     */
    private BigDecimal getQuotePrice(String symbol) {
        // Market Service GET endpoints are public (no auth required based on QuoteResource)
        QuoteDTO quote = marketServiceClient.getQuote(null, symbol);
        return quote.getPrice();
    }

    /**
     * Update account balance via Account Service.
     * For internal service calls, we use null auth (endpoint is @PermitAll).
     */
    private void updateAccountBalance(Long accountId, BigDecimal amount, String reason) {
        BalanceUpdateRequest request = new BalanceUpdateRequest();
        request.setAmount(amount);
        request.setReason(reason);

        accountServiceClient.updateBalance(null, accountId, request);
        LOG.infof("Updated account %d balance by %s", accountId, amount);
    }

    /**
     * Complete order with the executed price.
     */
    private void completeOrderWithPrice(Long orderId, BigDecimal price) {
        OrderDTO completed = orderService.completeOrder(orderId);
        LOG.infof("Completed order %d with status %s", orderId, completed.getOrderStatus());
    }

    /**
     * Cancel order due to processing error.
     */
    private void cancelOrder(Long orderId, Long accountId) {
        try {
            orderService.cancelOrder(orderId, accountId);
            LOG.infof("Cancelled order %d due to processing error", orderId);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to cancel order %d", orderId);
        }
    }
}

