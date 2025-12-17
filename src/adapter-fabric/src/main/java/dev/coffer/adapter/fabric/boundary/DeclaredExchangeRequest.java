package dev.coffer.adapter.fabric.boundary;

import java.util.List;
import java.util.Objects;

/**
 * FABRIC ADAPTER — DECLARED EXCHANGE REQUEST (PHASE 3.B).
 *
 * This is the adapter-side declaration envelope that will later be translated
 * into Core requests. It contains ONLY declared facts, not inferred meaning.
 *
 * This file intentionally contains no Fabric imports and no Core imports.
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

        // Defensive copy to preserve immutability expectations.
        items = List.copyOf(items);

        // Prevent null entries (structural integrity only).
        for (DeclaredItem item : items) {
            Objects.requireNonNull(item, "items must not contain null entries");
        }
    }
}
