package dev.coffer.core;

import java.util.Objects;

/**
 * Immutable description of an attempted exchange.
 *
 * All fields are opaque to Core.
 */
public final class ExchangeRequest {

    private final Object actor;
    private final Object context;
    private final Object payload;

    public ExchangeRequest(Object actor, Object context, Object payload) {
        this.actor = Objects.requireNonNull(actor, "actor");
        this.context = Objects.requireNonNull(context, "context");
        this.payload = Objects.requireNonNull(payload, "payload");
    }

    public Object actor() {
        return actor;
    }

    public Object context() {
        return context;
    }

    public Object payload() {
        return payload;
    }
}
