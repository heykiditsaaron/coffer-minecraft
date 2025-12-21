package dev.coffer.adapter.fabric.execution;

import dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest;
import dev.coffer.adapter.fabric.boundary.DeclaredItem;
import dev.coffer.adapter.fabric.config.ValuationConfig;
import dev.coffer.core.DenialReason;
import dev.coffer.core.ExchangeRequest;
import dev.coffer.core.ValuationItemResult;
import dev.coffer.core.ValuationService;
import dev.coffer.core.ValuationSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * FABRIC VALUATION SERVICE
 *
 * Responsibility:
 * - Adapter-owned valuation: explicit values only, deny-by-default.
 *
 * Invariants:
 * - Items not listed or <=0 value are rejected with INVALID_VALUE.
 * - No mutation or guessing.
 */
public final class FabricValuationService implements ValuationService {

    private final ValuationConfig config;

    public FabricValuationService(ValuationConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    @Override
    public ValuationSnapshot valuate(ExchangeRequest request) {
        Objects.requireNonNull(request, "request");

        Object payload = request.payload();

        if (!(payload instanceof DeclaredExchangeRequest declared)) {
            return new ValuationSnapshot(List.of());
        }

        List<ValuationItemResult> results = new ArrayList<>();

        for (DeclaredItem item : declared.items()) {
            long quantity = item.count();
            Long unitValue = config.getValueForItem(item.itemId());

            if (unitValue == null || unitValue <= 0) {
                results.add(
                        ValuationItemResult.rejected(
                                item,
                                quantity,
                                DenialReason.INVALID_VALUE
                        )
                );
            } else {
                long totalValue = unitValue * quantity;

                results.add(
                        ValuationItemResult.accepted(
                                item,
                                quantity,
                                totalValue
                        )
                );
            }
        }

        return new ValuationSnapshot(results);
    }
}
