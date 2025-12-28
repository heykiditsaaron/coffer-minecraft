package dev.coffer.adapter.fabric.execution;

import java.util.Optional;

public final class ShopPurchasePlanningResult {
    private final ShopPurchasePlan plan;
    private final String refusalMessage;

    private ShopPurchasePlanningResult(ShopPurchasePlan plan, String refusalMessage) {
        this.plan = plan;
        this.refusalMessage = refusalMessage;
    }

    public static ShopPurchasePlanningResult planned(ShopPurchasePlan plan) {
        return new ShopPurchasePlanningResult(plan, null);
    }

    public static ShopPurchasePlanningResult refused(String message) {
        return new ShopPurchasePlanningResult(null, message);
    }

    public boolean planned() {
        return plan != null;
    }

    public Optional<ShopPurchasePlan> plan() {
        return Optional.ofNullable(plan);
    }

    public Optional<String> refusal() {
        return Optional.ofNullable(refusalMessage);
    }
}
