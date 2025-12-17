package dev.coffer.core;

import java.util.Objects;

/**
 * Result of a single policy layer evaluation.
 */
public final class PolicyDecision {

    private final boolean allowed;
    private final DenialReason denialReason;

    private PolicyDecision(boolean allowed, DenialReason denialReason) {
        this.allowed = allowed;
        this.denialReason = denialReason;
    }

    public static PolicyDecision allow() {
        return new PolicyDecision(true, null);
    }

    public static PolicyDecision deny(DenialReason reason) {
        return new PolicyDecision(false, Objects.requireNonNull(reason));
    }

    public boolean allowed() {
        return allowed;
    }

    public DenialReason denialReason() {
        return denialReason;
    }
}
