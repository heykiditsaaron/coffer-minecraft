package dev.coffer.adapter.fabric.execution;

import dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest;
import dev.coffer.adapter.fabric.boundary.DeclaredItem;
import dev.coffer.adapter.fabric.boundary.MetadataRelevance;
import dev.coffer.core.DenialReason;
import dev.coffer.core.ExchangeRequest;
import dev.coffer.core.ValuationItemResult;
import dev.coffer.core.ValuationService;
import dev.coffer.core.ValuationSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * HARD-CODED VALUATION SERVICE — PHASE 3C.1
 *
 * This valuation source is intentionally non-authoritative.
 * Values are hard-coded to establish honest valuation behavior
 * without introducing configuration or persistence.
 *
 * DO NOT treat this implementation as final.
 */
public final class FabricValuationServiceStub implements ValuationService {

    @Override
    public ValuationSnapshot valuate(ExchangeRequest request) {
        Object payload = request.payload();

        if (!(payload instanceof DeclaredExchangeRequest declared)) {
            // No honest valuation possible
            return new ValuationSnapshot(List.of());
        }

        List<ValuationItemResult> results = new ArrayList<>();

        for (DeclaredItem item : declared.items()) {

            // Metadata gate — fail closed.
            // Only an explicit ignore declaration permits valuation.
            if (item.metadataRelevance() != MetadataRelevance.IGNORED_BY_DECLARATION) {
                results.add(
                        ValuationItemResult.rejected(
                                item,
                                0L,
                                DenialReason.INVALID_VALUE
                        )
                );
                continue;
            }

            // Explicit allowlist: minecraft:dirt only
            if ("minecraft:dirt".equals(item.itemId())) {
                results.add(
                        ValuationItemResult.accepted(
                                item,
                                1L,
                                1L
                        )
                );
            } else {
                results.add(
                        ValuationItemResult.rejected(
                                item,
                                0L,
                                DenialReason.INVALID_VALUE
                        )
                );
            }
        }

        return new ValuationSnapshot(results);
    }
}
