package dev.coffer.adapter.fabric.execution;

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

    public static ExchangeRequest translate(Object declared) {
        Objects.requireNonNull(declared, "declared");

        if (declared instanceof dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest sell) {
            Object actor = sell.invoker();
            Object context = sell.intent();
            Object payload = sell;
            return new ExchangeRequest(actor, context, payload);
        }

        if (declared instanceof dev.coffer.adapter.fabric.boundary.DeclaredShopPurchase buy) {
            Object actor = buy.invoker();
            Object context = dev.coffer.adapter.fabric.boundary.ExchangeIntent.BUY;
            Object payload = buy;
            return new ExchangeRequest(actor, context, payload);
        }

        throw new IllegalArgumentException("Unsupported declared exchange type: " + declared.getClass());
    }
}
