package dev.coffer.adapter.fabric.declaration;

import dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest;
import dev.coffer.adapter.fabric.boundary.DeclaredIdentity;
import dev.coffer.adapter.fabric.boundary.DeclaredItem;
import dev.coffer.adapter.fabric.boundary.ExchangeIntent;
import dev.coffer.adapter.fabric.boundary.InvocationContext;
import dev.coffer.adapter.fabric.boundary.MetadataRelevance;
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
 * INVENTORY DECLARATION BUILDER — PHASE 3C.2
 *
 * Constructs truthful adapter-side exchange declarations from authoritative
 * player-owned inventory state.
 *
 * Ownership rule:
 * - If the item could be placed into a chest, it is owned.
 * - UI state, screen handlers, and control surfaces are excluded by design.
 *
 * This phase:
 * - observes ownership only
 * - aggregates deterministically (within the current declared metadata stance)
 * - performs no valuation
 * - performs no mutation
 * - makes no UI assumptions
 */
public final class InventoryDeclarationBuilder {

    private InventoryDeclarationBuilder() {
        // utility
    }

    /**
     * Attempts to construct a truthful DeclaredExchangeRequest based solely
     * on what the player actually owns at the time of invocation.
     *
     * Absence is a valid and honest outcome.
     */
    public static Optional<DeclaredExchangeRequest> fromPlayer(ServerPlayerEntity player) {
        if (player == null) {
            throw new IllegalArgumentException("player must be non-null");
        }

        UUID playerId = player.getUuid();

        // Deterministic aggregation:
        // - LinkedHashMap preserves first-seen order (stable output).
        // - Key is the declared identity we are asserting (currently: itemId + declared relevance).
        Map<DeclaredKey, Integer> countsByKey = new LinkedHashMap<>();

        // Main inventory + hotbar (authoritative ownership)
        for (ItemStack stack : player.getInventory().main) {
            addStack(stack, countsByKey);
        }

        // Armor slots are owned items
        for (ItemStack stack : player.getInventory().armor) {
            addStack(stack, countsByKey);
        }

        // Offhand slot is owned item
        addStack(player.getOffHandStack(), countsByKey);

        if (countsByKey.isEmpty()) {
            return Optional.empty();
        }

        List<DeclaredItem> declaredItems = new ArrayList<>(countsByKey.size());
        for (Map.Entry<DeclaredKey, Integer> entry : countsByKey.entrySet()) {
            DeclaredKey key = entry.getKey();
            int count = entry.getValue();

            DeclaredItem declared =
                    DeclaredItem.withoutMetadata(
                            key.itemId(),
                            count,
                            key.relevance()
                    );

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

    /**
     * Adds an owned, non-empty stack into the aggregation map.
     */
    private static void addStack(ItemStack stack, Map<DeclaredKey, Integer> countsByKey) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
        int count = stack.getCount();

        // Phase 3C.2 (current stance):
        // - metadata is observed later; we do not interpret it here yet
        // - we explicitly declare metadata as ignored-by-declaration and omit it
        MetadataRelevance relevance = MetadataRelevance.IGNORED_BY_DECLARATION;

        DeclaredKey key = new DeclaredKey(itemId, relevance);

        int prior = countsByKey.getOrDefault(key, 0);
        countsByKey.put(key, prior + count);
    }

    /**
     * Declaration aggregation key for Phase 3C.2.
     *
     * IMPORTANT:
     * - This key is intentionally narrow for the current metadata stance.
     * - When metadata becomes declared (RELEVANT), this key must expand to include
     *   the declared metadata snapshot (and aggregation must only occur on exact match).
     */
    private record DeclaredKey(String itemId, MetadataRelevance relevance) {
        private DeclaredKey {
            if (itemId == null || itemId.isBlank()) throw new IllegalArgumentException("itemId must be non-empty");
            if (relevance == null) throw new IllegalArgumentException("relevance must be non-null");
        }
    }
}
