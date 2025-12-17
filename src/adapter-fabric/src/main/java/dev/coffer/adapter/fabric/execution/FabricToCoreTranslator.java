package dev.coffer.adapter.fabric.execution;

import dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest;
import dev.coffer.adapter.fabric.boundary.DeclaredItem;
import dev.coffer.adapter.fabric.boundary.MetadataRelevance;
import dev.coffer.core.ExchangeRequest;
import dev.coffer.core.ValuationItemRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * FABRIC → CORE TRANSLATOR (PHASE 3B.1).
 *
 * This class performs PURE TRANSLATION.
 *
 * It must:
 * - map declared adapter facts to Core request shapes
 * - perform NO valuation
 * - perform NO policy decisions
 * - perform NO mutation
 * - invent NO meaning
 *
 * Any declaration that cannot be translated truthfully
 * MUST result in failure at a higher layer (refusal),
 * not approximation here.
 */
public final class FabricToCoreTranslator {

    private FabricToCoreTranslator() {
        // utility-only
    }

    /**
     * Translate a declared Fabric exchange request into a Core exchange request.
     *
     * This method assumes:
     * - adapter readiness has already been verified
     * - permission checks are handled elsewhere
     *
     * This method does NOT:
     * - validate economic correctness
     * - validate metadata semantics
     * - apply policy
     */
    public static ExchangeRequest translate(DeclaredExchangeRequest declared) {
        Objects.requireNonNull(declared, "declared exchange request must be non-null");

        List<ValuationItemRequest> valuationItems = new ArrayList<>();

        for (DeclaredItem item : declared.items()) {
            valuationItems.add(translateItem(item));
        }

        return new ExchangeRequest(
                declared.target().uuid(),
                valuationItems
        );
    }

    private static ValuationItemRequest translateItem(DeclaredItem item) {
        Objects.requireNonNull(item, "declared item must be non-null");

        if (item.metadataRelevance() == MetadataRelevance.UNDECLARED) {
            throw new IllegalStateException(
                    "Cannot translate item with UNDECLARED metadata relevance"
            );
        }

        return new ValuationItemRequest(
                item.itemId(),
                item.count()
        );
    }
}
