package dev.coffer.adapter.fabric.boundary;

/**
 * FABRIC ADAPTER — INVOKER KIND ONLY (PHASE 3.B).
 *
 * Used to prevent accidental assumptions that "caller is always a player".
 * This preserves clean non-command invocation and external integrations.
 */
public enum InvokerKind {
    PLAYER,
    CONSOLE,
    MOD,
    SYSTEM
}
