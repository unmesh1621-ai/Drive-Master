package com.drivemaster.drivemastermain.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Hand-written Observer: decouples order creation from whatever should react to it
 * (currently, notifying the dealer). Spring auto-collects every {@link OrderCreatedListener}
 * bean into the constructor list -- no manual registration wiring needed.
 */
@Component
public class OrderEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final List<OrderCreatedListener> listeners;

    public OrderEventPublisher(List<OrderCreatedListener> listeners) {
        this.listeners = new CopyOnWriteArrayList<>(listeners);
    }

    public void publish(OrderEvent event) {
        for (OrderCreatedListener listener : listeners) {
            try {
                listener.onOrderCreated(event);
            } catch (Exception e) {
                log.error("Listener {} failed to handle OrderEvent for order {}",
                        listener.getClass().getSimpleName(), event.order().getId(), e);
            }
        }
    }
}
