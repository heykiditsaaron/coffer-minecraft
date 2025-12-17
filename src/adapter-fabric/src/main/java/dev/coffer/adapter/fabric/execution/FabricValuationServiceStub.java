package dev.coffer.adapter.fabric.execution;

import dev.coffer.core.ExchangeRequest;
import dev.coffer.core.ValuationItemResult;
import dev.coffer.core.ValuationService;
import dev.coffer.core.ValuationSnapshot;

import java.util.List;
import java.util.Objects;

/**
 * Phase 3B valuation stub.
 *
 * Produces a truthful, minimal snapshot with one accepted item.
 * No pricing semantics are locked.
 */
public final class FabricValuationServiceStub implements ValuationService {

    @Override
    public ValuationSnapshot valuate(ExchangeRequest request) {
        Objects.requireNonNull(request, "request");

        ValuationItemResult accepted =
                ValuationItemResult.accepted(
                        /* item */ "PLACEHOLDER",
                        /* quantity */ 1,
                        /* unitValue */ 1
                );

        return new ValuationSnapshot(List.of(accepted));
    }
}
