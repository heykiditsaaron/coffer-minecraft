package dev.coffer.adapter.fabric.execution;

import dev.coffer.adapter.fabric.config.ShopCatalog;
import dev.coffer.adapter.fabric.config.ShopDefinition;
import dev.coffer.adapter.fabric.config.ShopEntry;
import dev.coffer.adapter.fabric.config.ValuationConfig;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class ShopPricingService {

    private final ShopCatalog catalog;
    private final ValuationConfig valuationConfig;
    private final String defaultCurrencyId;

    public ShopPricingService(ShopCatalog catalog, ValuationConfig valuationConfig, String currencyId) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.valuationConfig = Objects.requireNonNull(valuationConfig, "valuationConfig");
        this.defaultCurrencyId = Objects.requireNonNull(currencyId, "currencyId");
    }

    public PriceResult price(String shopName, String itemId, int quantity) {
        if (quantity <= 0) {
            return PriceResult.fail("Quantity must be positive.");
        }

        Optional<ShopDefinition> shop = catalog.shops().stream()
                .filter(s -> s.id().equalsIgnoreCase(shopName))
                .findFirst();

        if (shop.isEmpty()) {
            String known = catalog.shops().isEmpty()
                    ? "none"
                    : catalog.shops().stream().map(ShopDefinition::id).collect(Collectors.joining(", "));
            return PriceResult.fail("Shop not found: " + shopName + " (known: " + known + ")");
        }

        ShopEntry entry = findEntry(shop.get(), itemId);
        // explicit buy
        if (entry != null && entry.hasExplicitBuy()) {
            if (quantity % entry.buyQuantity() != 0) {
                return PriceResult.fail("Quantity must be a multiple of " + entry.buyQuantity());
            }
            long packs = quantity / entry.buyQuantity();
            long total = entry.buyPrice() * packs;
            return PriceResult.success(entry.buyPrice(), total, entry.buyCurrency());
        }

        long base = resolveBase(itemId);
        if (base <= 0) {
            return PriceResult.fail("No base value for this item.");
        }

        double multiplier = entry != null ? entry.multiplier() : 1.0;
        long additive = entry != null ? entry.additive() : 0L;

        double adjusted = base * multiplier + additive;
        if (adjusted <= 0) {
            return PriceResult.fail("Item not eligible (price would be zero or negative).");
        }

        long perItem = Math.round(adjusted);
        long total = perItem * quantity;

        return PriceResult.success(perItem, total, defaultCurrencyId);
    }

    private ShopEntry findEntry(ShopDefinition def, String itemId) {
        // item match first
        for (ShopEntry entry : def.entries()) {
            if (entry.kind() == ShopEntry.Kind.ITEM && entry.target().equals(itemId)) {
                return entry;
            }
        }
        // tag match
        Set<String> tags = resolveTags(itemId);
        for (ShopEntry entry : def.entries()) {
            if (entry.kind() == ShopEntry.Kind.TAG && tags.contains(entry.target())) {
                return entry;
            }
        }
        return null;
    }

    private static Set<String> resolveTags(String itemId) {
        try {
            var id = Identifier.of(itemId);
            var item = Registries.ITEM.get(id);
            if (item != null) {
                return item.getRegistryEntry().streamTags().map(TagKey::id).map(Identifier::toString).collect(Collectors.toSet());
            }
        } catch (Exception ignored) {
        }
        return Set.of();
    }

    private long resolveBase(String itemId) {
        Set<String> tags = resolveTags(itemId);
        return valuationConfig.resolve(itemId, tags, defaultCurrencyId).orElse(-1);
    }

    public static final class PriceResult {
        private final boolean success;
        private final String message;
        private final long perItem;
        private final long total;
        private final String currencyId;

        private PriceResult(boolean success, String message, long perItem, long total, String currencyId) {
            this.success = success;
            this.message = message;
            this.perItem = perItem;
            this.total = total;
            this.currencyId = currencyId;
        }

        public static PriceResult fail(String message) {
            return new PriceResult(false, Objects.requireNonNull(message), 0, 0, null);
        }

        public static PriceResult success(long perItem, long total, String currencyId) {
            return new PriceResult(true, null, perItem, total, currencyId);
        }

        public boolean success() {
            return success;
        }

        public String message() {
            return message;
        }

        public long perItem() {
            return perItem;
        }

        public long totalValue() {
            return total;
        }

        public String currencyId() {
            return currencyId;
        }
    }
}
