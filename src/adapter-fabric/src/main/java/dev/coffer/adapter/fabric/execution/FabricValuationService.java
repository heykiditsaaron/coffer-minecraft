package dev.coffer.adapter.fabric.execution;

import dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest;
import dev.coffer.adapter.fabric.boundary.DeclaredItem;
import dev.coffer.adapter.fabric.config.ItemBlacklistConfig;
import dev.coffer.adapter.fabric.config.ValuationConfig;
import dev.coffer.core.DenialReason;
import dev.coffer.core.ExchangeRequest;
import dev.coffer.core.ValuationItemResult;
import dev.coffer.core.ValuationService;
import dev.coffer.core.ValuationSnapshot;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * FABRIC VALUATION SERVICE
 *
 * Responsibility:
 * - Adapter-owned valuation: explicit values only, deny-by-default.
 *
 * Invariants:
 * - Items not listed or <=0 value are rejected with INVALID_VALUE.
 * - No mutation or guessing.
 */
public final class FabricValuationService implements ValuationService {

    private final ValuationConfig config;
    private final ItemBlacklistConfig blacklist;
    private final String currencyId;

    public FabricValuationService(ValuationConfig config, ItemBlacklistConfig blacklist, String currencyId) {
        this.config = Objects.requireNonNull(config, "config");
        this.blacklist = Objects.requireNonNull(blacklist, "blacklist");
        this.currencyId = Objects.requireNonNull(currencyId, "currencyId");
    }

    @Override
    public ValuationSnapshot valuate(ExchangeRequest request) {
        Objects.requireNonNull(request, "request");

        Object payload = request.payload();

        if (!(payload instanceof DeclaredExchangeRequest declared)) {
            return new ValuationSnapshot(List.of());
        }

        List<ValuationItemResult> results = new ArrayList<>();

        for (DeclaredItem item : declared.items()) {
            long quantity = item.count();

            var tags = resolveTags(item.itemId());
            if (blacklist.isDenied(item.itemId(), tags)) {
                results.add(
                        ValuationItemResult.rejected(
                                item,
                                quantity,
                                DenialReason.INVALID_CONTEXT
                        )
                );
                continue;
            }

            var ruleOpt = config.resolveAny(item.itemId(), tags);
            if (ruleOpt.isEmpty()) {
                results.add(
                        ValuationItemResult.rejected(
                                item,
                                quantity,
                                DenialReason.INVALID_VALUE
                        )
                );
                continue;
            }
            var rule = ruleOpt.get();
            long unitValue = rule.value();
            if (unitValue <= 0) {
                results.add(
                        ValuationItemResult.rejected(
                                item,
                                quantity,
                                DenialReason.INVALID_VALUE
                        )
                );
            } else {
                long totalValue = unitValue * quantity;

                results.add(
                        ValuationItemResult.accepted(
                                item,
                                quantity,
                                totalValue,
                                rule.currencyId()
                        )
                );
            }
        }

        return new ValuationSnapshot(results);
    }

    private static Set<String> resolveTags(String itemId) {
        Set<String> tags = new HashSet<>();
        try {
            var id = Identifier.of(itemId);
            var item = Registries.ITEM.get(id);
            if (item != null) {
                for (TagKey<?> tag : item.getRegistryEntry().streamTags().toList()) {
                    tags.add(tag.id().toString());
                }
            }
        } catch (Exception e) {
            // ignore unknown
        }
        return tags;
    }
}
