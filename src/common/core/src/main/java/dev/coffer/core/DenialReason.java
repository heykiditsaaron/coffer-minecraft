package dev.coffer.core;

/**
 * Canonical reasons the Core Engine may deny an exchange.
 *
 * This enum is CLOSED.
 * Meanings must never change.
 * New entries require a constitutional decision.
 */
public enum DenialReason {

    INVALID_CONTEXT,
    NO_PERMISSION,
    INVALID_VALUE,
    INSUFFICIENT_RESOURCES,
    INTERNAL_INCONSISTENCY
}
