package dev.coffer.adapter.fabric.config;

import java.util.Objects;

public final class ShopEntry {
    public enum Kind { ITEM, TAG }

    private final String target;
    private final Kind kind;
    private final double multiplier;
    private final long additive;

    // explicit pricing (optional)
    private final long buyPrice;
    private final String buyCurrency;
    private final int buyQuantity;

    private final long sellPrice;
    private final String sellCurrency;
    private final int sellQuantity;

    private final Integer slot;
    private final Integer page;

    public ShopEntry(String target, Kind kind, double multiplier, long additive,
                     long buyPrice, String buyCurrency, int buyQuantity,
                     long sellPrice, String sellCurrency, int sellQuantity,
                     Integer slot, Integer page) {
        if (target == null || target.isBlank()) {
            throw new IllegalArgumentException("target must be non-empty");
        }
        this.target = target;
        this.kind = Objects.requireNonNull(kind, "kind");
        this.multiplier = multiplier;
        this.additive = additive;
        this.buyPrice = Math.max(0, buyPrice);
        this.buyCurrency = buyCurrency;
        this.buyQuantity = Math.max(1, buyQuantity);
        this.sellPrice = Math.max(0, sellPrice);
        this.sellCurrency = sellCurrency;
        this.sellQuantity = Math.max(1, sellQuantity);
        this.slot = slot;
        this.page = page;
    }

    public String target() {
        return target;
    }

    public Kind kind() {
        return kind;
    }

    public double multiplier() {
        return multiplier;
    }

    public long additive() {
        return additive;
    }

    public boolean hasExplicitBuy() { return buyPrice > 0 && buyCurrency != null && !buyCurrency.isBlank(); }
    public boolean hasExplicitSell() { return sellPrice > 0 && sellCurrency != null && !sellCurrency.isBlank(); }
    public long buyPrice() { return buyPrice; }
    public String buyCurrency() { return buyCurrency; }
    public int buyQuantity() { return buyQuantity; }
    public long sellPrice() { return sellPrice; }
    public String sellCurrency() { return sellCurrency; }
    public int sellQuantity() { return sellQuantity; }
    public Integer slot() { return slot; }
    public Integer page() { return page; }
}
