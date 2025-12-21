package dev.coffer.adapter.fabric.declaration;

import dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest;
import dev.coffer.adapter.fabric.boundary.DeclaredIdentity;
import dev.coffer.adapter.fabric.boundary.DeclaredItem;
import dev.coffer.adapter.fabric.boundary.ExchangeIntent;
import dev.coffer.adapter.fabric.boundary.InvocationContext;
import dev.coffer.adapter.fabric.boundary.MetadataRelevance;
import dev.coffer.adapter.fabric.config.MetadataPolicyConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * INVENTORY DECLARATION BUILDER
 *
 * Responsibility:
 * - Observe player-owned inventory surfaces.
 * - Aggregate identical items deterministically (respecting metadata stance).
 * - Build a truthful DeclaredExchangeRequest or refuse (empty).
 *
 * Not responsible for:
 * - Valuation, policy, or mutation.
 * - UI selection state (reads authoritative player inventory only).
 * - Metadata parsing (only stance resolution).
 *
 * Invariants:
 * - Only owned items are declared.
 * - Metadata relevance is explicit; unknown relevance causes refusal.
 * - Absence of eligible items returns empty (refusal before Core).
 */
public final class InventoryDeclarationBuilder {

    // Adapter-local, explicit config (file-backed later).
    private static final MetadataPolicyConfig METADATA_POLICY =
            MetadataPolicyConfig.permissiveDefault();

    private InventoryDeclarationBuilder() {
        // utility
    }

    /**
     * Attempt to construct a truthful DeclaredExchangeRequest based solely
     * on what the player actually owns at the time of invocation.
     *
     * Absence is a valid and honest outcome (empty Optional).
     */
    public static Optional<DeclaredExchangeRequest> fromPlayer(ServerPlayerEntity player) {
        if (player == null) {
            throw new IllegalArgumentException("player must be non-null");
        }

        UUID playerId = player.getUuid();

        // Deterministic aggregation.
        Map<DeclaredKey, Integer> countsByKey = new LinkedHashMap<>();

        // Main inventory + hotbar
        for (ItemStack stack : player.getInventory().main) {
            addStack(stack, countsByKey);
        }

        // Armor slots
        for (ItemStack stack : player.getInventory().armor) {
            addStack(stack, countsByKey);
        }

        // Offhand
        addStack(player.getOffHandStack(), countsByKey);

        if (countsByKey.isEmpty()) {
            return Optional.empty();
        }

        List<DeclaredItem> declaredItems = new ArrayList<>(countsByKey.size());
        for (Map.Entry<DeclaredKey, Integer> entry : countsByKey.entrySet()) {
            DeclaredKey key = entry.getKey();
            int count = entry.getValue();

            if (key.relevance == MetadataRelevance.UNDECLARED) {
                // Explicit refusal path — no guessing.
                return Optional.empty();
            }

            DeclaredItem declared;
            if (key.relevance == MetadataRelevance.RELEVANT) {
                // Metadata relevance is explicit, but metadata extraction is deferred.
                // If relevance is REQUIRED, but we cannot declare metadata yet, we refuse.
                return Optional.empty();
            } else {
                declared =
                        DeclaredItem.withoutMetadata(
                                key.itemId,
                                count,
                                key.relevance
                        );
            }

            declaredItems.add(declared);
        }

        DeclaredExchangeRequest request =
                new DeclaredExchangeRequest(
                        ExchangeIntent.SELL,
                        InvocationContext.player(playerId),
                        DeclaredIdentity.of(playerId),
                        declaredItems
                );

        return Optional.of(request);
    }

    private static void addStack(ItemStack stack, Map<DeclaredKey, Integer> countsByKey) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
        int count = stack.getCount();

        MetadataRelevance relevance = METADATA_POLICY.resolveForItem(itemId);
        DeclaredKey key = new DeclaredKey(itemId, relevance);

        int prior = countsByKey.getOrDefault(key, 0);
        countsByKey.put(key, prior + count);
    }

    /**
     * Aggregation key for deterministic grouping.
     *
     * NOTE:
     * - When metadata becomes declared (RELEVANT with metadata snapshot),
     *   this key must expand to include declared metadata identity.
     */
    private static final class DeclaredKey {
        private final String itemId;
        private final MetadataRelevance relevance;

        private DeclaredKey(String itemId, MetadataRelevance relevance) {
            if (itemId == null || itemId.isBlank()) {
                throw new IllegalArgumentException("itemId must be non-empty");
            }
            if (relevance == null) {
                throw new IllegalArgumentException("relevance must be non-null");
            }
            this.itemId = itemId;
            this.relevance = relevance;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DeclaredKey other)) return false;
            return itemId.equals(other.itemId) && relevance == other.relevance;
        }

        @Override
        public int hashCode() {
            int result = itemId.hashCode();
            result = 31 * result + relevance.hashCode();
            return result;
        }
    }
}
