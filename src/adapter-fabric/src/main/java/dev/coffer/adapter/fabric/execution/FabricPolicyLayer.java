package dev.coffer.adapter.fabric.execution;

import dev.coffer.adapter.fabric.config.ItemBlacklistConfig;
import dev.coffer.core.DenialReason;
import dev.coffer.core.ExchangeRequest;
import dev.coffer.core.PolicyDecision;
import dev.coffer.core.PolicyLayer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Adapter policy layer enforcing basic legitimacy and item allow/deny.
 *
 * Denies when:
 * - payload is not a DeclaredExchangeRequest
 * - intent is not SELL
 * - any declared item is denied by tag/item allow/deny config
 */
public final class FabricPolicyLayer implements PolicyLayer {

    private final ItemBlacklistConfig blacklistConfig;

    public FabricPolicyLayer(ItemBlacklistConfig blacklistConfig) {
        this.blacklistConfig = Objects.requireNonNull(blacklistConfig, "blacklistConfig");
    }

    @Override
    public PolicyDecision evaluate(ExchangeRequest request) {
        Objects.requireNonNull(request, "request");

        Object payload = request.payload();

        if (payload instanceof dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest declaredSell) {
            if (declaredSell.intent() != dev.coffer.adapter.fabric.boundary.ExchangeIntent.SELL) {
                return PolicyDecision.deny(DenialReason.INVALID_CONTEXT);
            }
            // Blacklist handled in valuation; allow mixed outcomes.
            return PolicyDecision.allow();
        }

        if (payload instanceof dev.coffer.adapter.fabric.boundary.DeclaredShopPurchase purchase) {
            if (isDenied(purchase.itemId())) {
                return PolicyDecision.deny(DenialReason.INVALID_CONTEXT);
            }
            return PolicyDecision.allow();
        }

        return PolicyDecision.deny(DenialReason.INVALID_CONTEXT);
    }

    private boolean isDenied(String itemId) {
        Set<String> tags = resolveTags(itemId);
        return blacklistConfig.isDenied(itemId, tags);
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
            // unknown item => no tags
        }
        return tags;
    }
}
