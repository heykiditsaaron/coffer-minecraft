package dev.coffer.adapter.fabric.execution;

import dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest;
import dev.coffer.core.ExchangeRequest;

import java.util.Objects;

/**
 * FABRIC → CORE TRANSLATOR
 *
 * Contract-aligned:
 * - wraps adapter-declared facts into an opaque Core ExchangeRequest
 * - performs no valuation, mutation, or inference
 */
public final class FabricToCoreTranslator {

    private FabricToCoreTranslator() {
        // utility
    }

    public static ExchangeRequest translate(DeclaredExchangeRequest declared) {
        Objects.requireNonNull(declared, "declared");

        // Core treats actor, context, and payload as opaque Objects.
        // Use record accessors exactly as defined.
        Object actor = declared.invoker();
        Object context = declared.intent();
        Object payload = declared;

        return new ExchangeRequest(actor, context, payload);
    }
}
