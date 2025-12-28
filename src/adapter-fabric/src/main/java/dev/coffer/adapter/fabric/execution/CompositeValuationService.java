package dev.coffer.adapter.fabric.execution;

import dev.coffer.core.ExchangeRequest;
import dev.coffer.core.ValuationService;
import dev.coffer.core.ValuationSnapshot;

import java.util.Objects;

/**
 * Routes valuation based on payload type.
 */
public final class CompositeValuationService implements ValuationService {

    private final ValuationService sellValuation;
    private final ValuationService shopPurchaseValuation;

    public CompositeValuationService(ValuationService sellValuation,
                                     ValuationService shopPurchaseValuation) {
        this.sellValuation = Objects.requireNonNull(sellValuation, "sellValuation");
        this.shopPurchaseValuation = Objects.requireNonNull(shopPurchaseValuation, "shopPurchaseValuation");
    }

    @Override
    public ValuationSnapshot valuate(ExchangeRequest request) {
        Objects.requireNonNull(request, "request");
        Object payload = request.payload();
        if (payload instanceof dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest) {
            return sellValuation.valuate(request);
        }
        if (payload instanceof dev.coffer.adapter.fabric.boundary.DeclaredShopPurchase) {
            return shopPurchaseValuation.valuate(request);
        }
        return new ValuationSnapshot(java.util.List.of());
    }
}
