package dev.coffer.adapter.fabric.boundary;

import java.util.Objects;
import java.util.UUID;

/**
 * DECLARED SHOP PURCHASE REQUEST (BUY).
 */
public record DeclaredShopPurchase(
        String shopId,
        String itemId,
        int quantity,
        InvocationContext invoker,
        DeclaredIdentity target
) {
    public DeclaredShopPurchase {
        if (shopId == null || shopId.isBlank()) {
            throw new IllegalArgumentException("shopId must be non-empty");
        }
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("itemId must be non-empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        Objects.requireNonNull(invoker, "invoker");
        Objects.requireNonNull(target, "target");
    }

    public static DeclaredShopPurchase of(String shopId, String itemId, int quantity, UUID playerId) {
        return new DeclaredShopPurchase(
                shopId,
                itemId,
                quantity,
                InvocationContext.player(playerId),
                DeclaredIdentity.of(playerId)
        );
    }
}
