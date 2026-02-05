package com.daytrader.market.messaging;

import com.daytrader.common.event.QuoteUpdatedEvent;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

/**
 * Producer for Quote events.
 *
 * Uses in-memory channels by default for simplicity.
 * Can be switched to Kafka by activating the 'kafka' profile.
 * See ADR-002 for rationale.
 *
 * Emits quote update events to the "quotes-out" channel.
 */
@ApplicationScoped
public class QuoteEventProducer {

    private static final Logger LOG = Logger.getLogger(QuoteEventProducer.class);

    @Inject
    @Channel("quotes-out")
    @Broadcast
    Emitter<QuoteUpdatedEvent> quoteUpdatedEmitter;

    /**
     * Emit a QuoteUpdatedEvent to Kafka
     */
    public void emitQuoteUpdated(QuoteUpdatedEvent event) {
        LOG.debugf("Emitting QuoteUpdatedEvent for symbol %s: price=%.2f, change=%.2f",
                   event.symbol(), event.price(), event.priceChange());
        quoteUpdatedEmitter.send(event);
    }
}

