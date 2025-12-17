package dev.coffer.adapter.fabric.boundary;

/**
 * FABRIC ADAPTER — DECLARED INTENT ONLY (PHASE 3.B).
 *
 * This is NOT Core semantics.
 * This is a platform-facing declaration of "what the caller is attempting".
 *
 * No intent defaults are permitted at call sites.
 * Callers must declare intent explicitly.
 */
public enum ExchangeIntent {
    /**
     * Player attempting to sell items to receive value.
     */
    SELL,

    /**
     * Admin/system attempting to issue value to a target identity.
     * (Explicit mutation path; not an exchange.)
     */
    ISSUE,

    /**
     * Admin/system attempting to debit value from a target identity.
     * (Explicit mutation path; not an exchange.)
     */
    DEBIT
}
