package dev.coffer.core;

import java.util.Objects;

/**
 * Final result of an exchange evaluation.
 *
 * PASS indicates an honest mutation is possible.
 * DENY indicates no honest mutation is possible.
 */
public final class ExchangeEvaluationResult {

    private final boolean allowed;
    private final DenialReason denialReason;
    private final Object valuationSnapshot;

    private ExchangeEvaluationResult(
            boolean allowed,
            DenialReason denialReason,
            Object valuationSnapshot
    ) {
        this.allowed = allowed;
        this.denialReason = denialReason;
        this.valuationSnapshot = valuationSnapshot;
    }

    public static ExchangeEvaluationResult pass(Object valuationSnapshot) {
        return new ExchangeEvaluationResult(
                true,
                null,
                Objects.requireNonNull(valuationSnapshot)
        );
    }

    public static ExchangeEvaluationResult deny(DenialReason reason) {
        return new ExchangeEvaluationResult(
                false,
                Objects.requireNonNull(reason),
                null
        );
    }

    public boolean allowed() {
        return allowed;
    }

    public DenialReason denialReason() {
        return denialReason;
    }

    public Object valuationSnapshot() {
        return valuationSnapshot;
    }
}
