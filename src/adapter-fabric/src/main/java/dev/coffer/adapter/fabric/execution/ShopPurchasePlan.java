package dev.coffer.adapter.fabric.execution;

import java.util.Objects;
import java.util.UUID;

public final class ShopPurchasePlan {
    private final UUID playerId;
    private final String shopId;
    private final String itemId;
    private final int quantity;
    private final long cost;
    private final String currencyId;

    public ShopPurchasePlan(UUID playerId, String shopId, String itemId, int quantity, long cost, String currencyId) {
        this.playerId = Objects.requireNonNull(playerId, "playerId");
        if (shopId == null || shopId.isBlank()) throw new IllegalArgumentException("shopId must be non-empty");
        if (itemId == null || itemId.isBlank()) throw new IllegalArgumentException("itemId must be non-empty");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
        if (cost <= 0) throw new IllegalArgumentException("cost must be > 0");
        if (currencyId == null || currencyId.isBlank()) throw new IllegalArgumentException("currencyId must be non-empty");
        this.shopId = shopId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.cost = cost;
        this.currencyId = currencyId;
    }

    public UUID playerId() { return playerId; }
    public String shopId() { return shopId; }
    public String itemId() { return itemId; }
    public int quantity() { return quantity; }
    public long cost() { return cost; }
    public String currencyId() { return currencyId; }
}
