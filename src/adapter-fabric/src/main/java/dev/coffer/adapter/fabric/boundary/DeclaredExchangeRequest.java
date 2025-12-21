package dev.coffer.adapter.fabric.boundary;

import java.util.List;
import java.util.Objects;

/**
 * FABRIC ADAPTER — DECLARED EXCHANGE REQUEST
 *
 * Responsibility:
 * - Immutable envelope of declared intent, invoker, target, and items.
 *
 * Not responsible for:
 * - Valuation, policy, or mutation.
 * - Fabric/Core concerns (keeps imports clean).
 *
 * Invariants:
 * - Facts only; no inferred meaning.
 * - Defensive copy of items; no null entries.
 */
public record DeclaredExchangeRequest(
        ExchangeIntent intent,
        InvocationContext invoker,
        DeclaredIdentity target,
        List<DeclaredItem> items
) {
    public DeclaredExchangeRequest {
        if (intent == null) throw new IllegalArgumentException("intent must be non-null");
        if (invoker == null) throw new IllegalArgumentException("invoker must be non-null");
        if (target == null) throw new IllegalArgumentException("target must be non-null");
        if (items == null) throw new IllegalArgumentException("items must be non-null");

        items = List.copyOf(items);

        for (DeclaredItem item : items) {
            Objects.requireNonNull(item, "items must not contain null entries");
        }
    }
}
