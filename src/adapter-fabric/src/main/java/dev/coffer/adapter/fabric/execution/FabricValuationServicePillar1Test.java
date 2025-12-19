package dev.coffer.adapter.fabric.execution;

import dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest;
import dev.coffer.adapter.fabric.boundary.DeclaredItem;
import dev.coffer.core.DenialReason;
import dev.coffer.core.ExchangeRequest;
import dev.coffer.core.ValuationItemResult;
import dev.coffer.core.ValuationService;
import dev.coffer.core.ValuationSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * FABRIC VALUATION SERVICE — PHASE 3D PILLAR 1 (TEST ONLY)
 *
 * Purpose:
 * - Provide a minimal, explicit valuation that allows the execution boundary
 *   (mutation skeleton) to be reached.
 *
 * Rules:
 * - Only minecraft:dirt is valued.
 * - All other items are rejected with INVALID_VALUE.
 *
 * This service is TEMPORARY scaffold and MUST be removed after Pillar 1.
 */
public final class FabricValuationServicePillar1Test implements ValuationService {

    private static final String TEST_ITEM_ID = "minecraft:dirt";
    private static final long UNIT_VALUE = 1L;

    @Override
    public ValuationSnapshot valuate(ExchangeRequest request) {
        Objects.requireNonNull(request, "request");

        Object payload = request.payload();
        if (!(payload instanceof DeclaredExchangeRequest declared)) {
            // Adapter cannot truthfully valuate unknown payload shapes.
            // Refuse by denying all.
            return new ValuationSnapshot(List.of(
                    ValuationItemResult.rejected(
                            payload,
                            0,
                            DenialReason.INVALID_VALUE
                    )
            ));
        }

        List<ValuationItemResult> results = new ArrayList<>();

        for (DeclaredItem item : declared.items()) {
            String itemId = item.itemId();
            long qty = item.count();

            if (TEST_ITEM_ID.equals(itemId)) {
                long totalValue = qty * UNIT_VALUE;
                results.add(ValuationItemResult.accepted(item, qty, totalValue));
            } else {
                results.add(ValuationItemResult.rejected(item, qty, DenialReason.INVALID_VALUE));
            }
        }

        return new ValuationSnapshot(results);
    }
}
