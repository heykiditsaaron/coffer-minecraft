package dev.coffer.core;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable audit record emitted by Core decisions.
 */
public final class AuditRecord {

    private final Instant timestamp;
    private final ExchangeRequest request;
    private final ExchangeEvaluationResult result;

    public AuditRecord(
            Instant timestamp,
            ExchangeRequest request,
            ExchangeEvaluationResult result
    ) {
        this.timestamp = Objects.requireNonNull(timestamp);
        this.request = Objects.requireNonNull(request);
        this.result = Objects.requireNonNull(result);
    }

    public Instant timestamp() {
        return timestamp;
    }

    public ExchangeRequest request() {
        return request;
    }

    public ExchangeEvaluationResult result() {
        return result;
    }
}
