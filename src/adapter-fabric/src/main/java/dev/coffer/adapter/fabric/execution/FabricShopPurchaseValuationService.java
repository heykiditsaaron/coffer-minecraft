package dev.coffer.adapter.fabric.execution;

import dev.coffer.adapter.fabric.boundary.DeclaredShopPurchase;
import dev.coffer.core.DenialReason;
import dev.coffer.core.ExchangeRequest;
import dev.coffer.core.ValuationItemResult;
import dev.coffer.core.ValuationService;
import dev.coffer.core.ValuationSnapshot;

import java.util.List;
import java.util.Objects;

/**
 * Valuation for admin shop BUY: uses shop pricing, denies when price is unavailable/<=0.
 */
public final class FabricShopPurchaseValuationService implements ValuationService {

    private final dev.coffer.adapter.fabric.execution.ShopPricingService pricingService;
    private final String currencyId;

    public FabricShopPurchaseValuationService(dev.coffer.adapter.fabric.execution.ShopPricingService pricingService, String currencyId) {
        this.pricingService = Objects.requireNonNull(pricingService, "pricingService");
        this.currencyId = Objects.requireNonNull(currencyId, "currencyId");
    }

    @Override
    public ValuationSnapshot valuate(ExchangeRequest request) {
        Objects.requireNonNull(request, "request");
        Object payload = request.payload();

        if (!(payload instanceof DeclaredShopPurchase purchase)) {
            return new ValuationSnapshot(List.of());
        }

        var price = pricingService.price(purchase.shopId(), purchase.itemId(), purchase.quantity());
        if (!price.success()) {
            ValuationItemResult rejected = ValuationItemResult.rejected(
                    dev.coffer.adapter.fabric.boundary.DeclaredItem.withoutMetadata(
                            purchase.itemId(),
                            purchase.quantity(),
                            dev.coffer.adapter.fabric.boundary.MetadataRelevance.IGNORED_BY_DECLARATION
                    ),
                    purchase.quantity(),
                    DenialReason.INVALID_VALUE
            );
            return new ValuationSnapshot(List.of(rejected));
        }

        long totalCost = price.totalValue();
        if (totalCost <= 0) {
            ValuationItemResult rejected = ValuationItemResult.rejected(
                    dev.coffer.adapter.fabric.boundary.DeclaredItem.withoutMetadata(
                            purchase.itemId(),
                            purchase.quantity(),
                            dev.coffer.adapter.fabric.boundary.MetadataRelevance.IGNORED_BY_DECLARATION
                    ),
                    purchase.quantity(),
                    DenialReason.INVALID_VALUE
            );
            return new ValuationSnapshot(List.of(rejected));
        }

        ValuationItemResult accepted = ValuationItemResult.accepted(
                dev.coffer.adapter.fabric.boundary.DeclaredItem.withoutMetadata(
                        purchase.itemId(),
                        purchase.quantity(),
                        dev.coffer.adapter.fabric.boundary.MetadataRelevance.IGNORED_BY_DECLARATION
                ),
                purchase.quantity(),
                totalCost,
                currencyId
        );
        return new ValuationSnapshot(List.of(accepted));
    }
}
