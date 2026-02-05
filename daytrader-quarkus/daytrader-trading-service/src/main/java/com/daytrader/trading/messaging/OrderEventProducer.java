package com.daytrader.trading.messaging;

import com.daytrader.common.event.OrderCompletedEvent;
import com.daytrader.common.event.OrderCreatedEvent;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import jakarta.inject.Inject;

/**
 * Producer for Order events.
 *
 * Uses in-memory channels by default for simplicity.
 * Can be switched to Kafka by activating the 'kafka' profile.
 * See ADR-002 for rationale.
 *
 * Emits order creation and completion events to the "orders-out" channel.
 */
@ApplicationScoped
public class OrderEventProducer {

    private static final Logger LOG = Logger.getLogger(OrderEventProducer.class);

    /**
     * Single emitter for all order events.
     * Uses Object type to support multiple event types (OrderCreatedEvent, OrderCompletedEvent)
     * on the same channel, as SmallRye Reactive Messaging cannot inject multiple emitters
     * with different generic types for the same channel.
     */
    @Inject
    @Channel("orders-out")
    @Broadcast
    Emitter<Object> orderEmitter;

    /**
     * Emit an OrderCreatedEvent to the orders-out channel
     */
    public void emitOrderCreated(OrderCreatedEvent event) {
        LOG.debugf("Emitting OrderCreatedEvent for order %d", event.orderId());
        orderEmitter.send(event);
    }

    /**
     * Emit an OrderCompletedEvent to the orders-out channel
     */
    public void emitOrderCompleted(OrderCompletedEvent event) {
        LOG.debugf("Emitting OrderCompletedEvent for order %d with status %s",
                   event.orderId(), event.orderStatus());
        orderEmitter.send(event);
    }
}

