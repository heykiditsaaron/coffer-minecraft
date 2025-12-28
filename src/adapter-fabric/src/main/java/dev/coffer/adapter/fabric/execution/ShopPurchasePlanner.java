package dev.coffer.adapter.fabric.execution;

import dev.coffer.adapter.fabric.boundary.DeclaredShopPurchase;
import dev.coffer.core.ExchangeEvaluationResult;
import dev.coffer.core.ValuationSnapshot;

import java.util.Objects;

public final class ShopPurchasePlanner {

    public ShopPurchasePlanningResult plan(
            DeclaredShopPurchase request,
            ExchangeEvaluationResult evaluationResult,
            String currencyId
    ) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(evaluationResult, "evaluationResult");
        Objects.requireNonNull(currencyId, "currencyId");

        if (!evaluationResult.allowed()) {
            return ShopPurchasePlanningResult.refused("Core denied the purchase; no changes will be made.");
        }

        Object snapshotObj = evaluationResult.valuationSnapshot();
        if (!(snapshotObj instanceof ValuationSnapshot snapshot)) {
            return ShopPurchasePlanningResult.refused("Unsupported valuation snapshot.");
        }
        if (!snapshot.hasAnyAccepted()) {
            return ShopPurchasePlanningResult.refused("No acceptable valuation for this purchase.");
        }
        long cost = snapshot.totalAcceptedValue();
        if (cost <= 0) {
            return ShopPurchasePlanningResult.refused("Calculated cost was zero or negative; purchase denied.");
        }

        ShopPurchasePlan plan =
                new ShopPurchasePlan(
                        request.target().id(),
                        request.shopId(),
                        request.itemId(),
                        request.quantity(),
                        cost,
                        currencyId
                );
        return ShopPurchasePlanningResult.planned(plan);
    }
}
