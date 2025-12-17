package dev.coffer.core;

import java.util.Objects;

/**
 * Explicit request to mutate a balance.
 *
 * This is NOT an exchange.
 * This is an intentional issuance or destruction of value.
 */
public final class BalanceMutation {

    private final Object actor;
    private final long amount;
    private final Object reason;

    public BalanceMutation(Object actor, long amount, Object reason) {
        this.actor = Objects.requireNonNull(actor, "actor");
        this.reason = Objects.requireNonNull(reason, "reason");

        if (amount == 0) {
            throw new IllegalArgumentException("amount must be non-zero");
        }

        this.amount = amount;
    }

    public Object actor() {
        return actor;
    }

    public long amount() {
        return amount;
    }

    public Object reason() {
        return reason;
    }

    public boolean isCredit() {
        return amount > 0;
    }

    public boolean isDebit() {
        return amount < 0;
    }
}
