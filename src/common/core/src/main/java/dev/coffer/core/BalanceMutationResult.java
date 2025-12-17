package dev.coffer.core;

import java.util.Objects;

/**
 * Result of an explicit balance mutation attempt.
 */
public final class BalanceMutationResult {

    private final boolean applied;
    private final DenialReason denialReason;

    private BalanceMutationResult(boolean applied, DenialReason denialReason) {
        this.applied = applied;
        this.denialReason = denialReason;
    }

    public static BalanceMutationResult applied() {
        return new BalanceMutationResult(true, null);
    }

    public static BalanceMutationResult denied(DenialReason reason) {
        return new BalanceMutationResult(false, Objects.requireNonNull(reason));
    }

    public boolean isApplied() {
        return applied;
    }

    public DenialReason denialReason() {
        return denialReason;
    }
}
