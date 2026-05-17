package dev.coffer.minecraft.platform.fabric;

import dev.coffer.minecraft.bindings.inventory.MinecraftContainerResolver;
import dev.coffer.minecraft.bindings.inventory.MinecraftDescriptorFactory;
import dev.coffer.minecraft.bindings.inventory.MinecraftItemDescriptor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.coffer.core.model.id.ActorRef;
import org.coffer.core.model.id.MutationRequirementRef;
import org.coffer.core.model.id.OfferRef;
import org.coffer.core.model.id.PayloadId;
import org.coffer.core.model.id.TruthRef;
import org.coffer.core.model.id.ValueRef;
import org.coffer.core.model.request.ActorDeclaration;
import org.coffer.core.model.request.ExchangePayload;
import org.coffer.core.model.request.ValueDeclaration;
import org.coffer.core.model.support.OpaqueObject;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueAtomicSwapConstruction;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueAtomicSwapRefs;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueConstructionResult;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueConstructionResult.Success;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueExchangePayloadConstruction;
import org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority;

final class CofferMinecraftAdminShopPresetListingRequestAssembly {
    static final String SHOP_LISTING_ACTOR_KIND = "minecraft_admin_shop_listing";
    static final String SUPPLY_MODE = "shopSupplyMode";
    static final String LISTING_ID = "shopListingId";
    static final String LISTING_SURFACE_ID = "listingSurfaceId";
    static final String SUPPLY_CONTAINER_ID = "supplyContainerId";
    static final String INFINITE_FAUCET = "infinite_faucet";
    static final String FINITE_FAUCET = "finite_faucet";

    private final CofferMinecraftAdminShopPresetListingConstruction listingConstruction;
    private final ConstructionGateway constructionGateway;

    CofferMinecraftAdminShopPresetListingRequestAssembly(
            CofferMinecraftAdminShopPresetListingConstruction listingConstruction,
            ConstructionGateway constructionGateway) {
        this.listingConstruction = Objects.requireNonNull(listingConstruction, "listingConstruction");
        this.constructionGateway = Objects.requireNonNull(constructionGateway, "constructionGateway");
    }

    static CofferMinecraftAdminShopPresetListingRequestAssembly create() {
        return new CofferMinecraftAdminShopPresetListingRequestAssembly(
                new CofferMinecraftAdminShopPresetListingConstruction(),
                TransferableValueExchangePayloadConstruction::constructAtomicSwap);
    }

    AssemblyResult assemble(
            Optional<CofferMinecraftAdminShopPresetListingConstruction.PresetListing> listing,
            SelectedParticipant player,
            String bindingId) {
        Objects.requireNonNull(listing, "listing");
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(bindingId, "bindingId");

        CofferMinecraftAdminShopPresetListingConstruction.ConstructionResult constructionResult =
                listingConstruction.prepare(listing, player.snapshot());
        if (constructionResult instanceof CofferMinecraftAdminShopPresetListingConstruction.ConstructionResult.Refused refused) {
            return new AssemblyResult.Refused(refused.reasonCode());
        }

        CofferMinecraftAdminShopPresetListingConstruction.ConstructionResult.Ready ready =
                (CofferMinecraftAdminShopPresetListingConstruction.ConstructionResult.Ready) constructionResult;
        CofferMinecraftAdminShopPresetListingConstruction.ConcreteExchange exchange = ready.exchange();

        List<ValueDeclaration> playerValues = List.of(value("shop-player-value", exchange.playerSelectedValue().selectedValue().orElseThrow()));
        List<ValueDeclaration> shopValues = List.of(value("shop-offered-value", exchange.offeredValue()));
        TransferableValueAtomicSwapConstruction construction = new TransferableValueAtomicSwapConstruction(
                refs(playerValues, shopValues),
                playerActor(player.actorRef()),
                shopActor(exchange),
                player.offerRef(),
                new OfferRef("shop-listing-offer-" + exchange.listingId()),
                playerValues,
                shopValues,
                bindingId);

        TransferableValueConstructionResult payloadConstruction = constructionGateway.construct(construction);
        if (payloadConstruction instanceof Success success) {
            return new AssemblyResult.Prepared(exchange, construction, success.payload());
        }

        TransferableValueConstructionResult.Refused refused =
                (TransferableValueConstructionResult.Refused) payloadConstruction;
        return new AssemblyResult.Refused(refused.refusal().reason().name());
    }

    private static ActorDeclaration playerActor(ActorRef actorRef) {
        return new ActorDeclaration(
                actorRef,
                MinecraftContainerResolver.PLAYER_INVENTORY_KIND,
                new OpaqueObject(Map.of()));
    }

    private static ActorDeclaration shopActor(CofferMinecraftAdminShopPresetListingConstruction.ConcreteExchange exchange) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put(LISTING_ID, exchange.listingId());
        if (exchange.supplyMode() instanceof CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.InfiniteFaucet infinite) {
            metadata.put(SUPPLY_MODE, INFINITE_FAUCET);
            metadata.put(LISTING_SURFACE_ID, infinite.listingSurfaceId());
        } else {
            CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.FiniteFaucet finite =
                    (CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.FiniteFaucet) exchange.supplyMode();
            metadata.put(SUPPLY_MODE, FINITE_FAUCET);
            metadata.put(SUPPLY_CONTAINER_ID, finite.supplyContainerId());
        }
        return new ActorDeclaration(
                new ActorRef("admin-shop:listing:" + exchange.listingId()),
                SHOP_LISTING_ACTOR_KIND,
                new OpaqueObject(Map.copyOf(metadata)));
    }

    private static ValueDeclaration value(String valueRef, MinecraftItemDescriptor descriptor) {
        Map<String, Object> descriptorValues = new LinkedHashMap<>();
        descriptorValues.put(MinecraftDescriptorFactory.ITEM_ID, descriptor.itemId());
        descriptorValues.put(MinecraftDescriptorFactory.QUANTITY, descriptor.quantity());
        descriptor.nbtPayload().ifPresent(payload -> descriptorValues.put(MinecraftDescriptorFactory.NBT_PAYLOAD, payload));
        return new ValueDeclaration(
                new ValueRef(valueRef),
                TransferableValueCoreAuthority.AUTHORITY_ID,
                new OpaqueObject(Map.copyOf(descriptorValues)));
    }

    private static TransferableValueAtomicSwapRefs refs(
            List<ValueDeclaration> playerValues,
            List<ValueDeclaration> shopValues) {
        return new TransferableValueAtomicSwapRefs(
                new PayloadId("admin-shop-preset-listing-payload-1"),
                truthRefs(playerValues, "admin-shop-player-truth-"),
                truthRefs(shopValues, "admin-shop-shop-truth-"),
                new TruthRef("admin-shop-player-can-receive"),
                new TruthRef("admin-shop-shop-can-receive"),
                new MutationRequirementRef("admin-shop-preset-listing-mutation-requirement-1"));
    }

    private static Map<ValueRef, TruthRef> truthRefs(List<ValueDeclaration> values, String prefix) {
        Map<ValueRef, TruthRef> truthRefs = new LinkedHashMap<>();
        for (ValueDeclaration value : values) {
            truthRefs.put(value.valueRef(), new TruthRef(prefix + value.valueRef().value()));
        }
        return Map.copyOf(truthRefs);
    }

    record SelectedParticipant(
            ActorRef actorRef,
            OfferRef offerRef,
            CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot snapshot) {
        SelectedParticipant {
            Objects.requireNonNull(actorRef, "actorRef");
            Objects.requireNonNull(offerRef, "offerRef");
            Objects.requireNonNull(snapshot, "snapshot");
        }
    }

    sealed interface AssemblyResult permits AssemblyResult.Prepared, AssemblyResult.Refused {
        record Prepared(
                CofferMinecraftAdminShopPresetListingConstruction.ConcreteExchange exchange,
                TransferableValueAtomicSwapConstruction construction,
                ExchangePayload payload)
                implements AssemblyResult {
            public Prepared {
                Objects.requireNonNull(exchange, "exchange");
                Objects.requireNonNull(construction, "construction");
                Objects.requireNonNull(payload, "payload");
            }
        }

        record Refused(String reasonCode) implements AssemblyResult {
            public Refused {
                if (reasonCode == null || reasonCode.isBlank()) {
                    throw new IllegalArgumentException("reasonCode must not be null or blank");
                }
            }
        }
    }

    @FunctionalInterface
    interface ConstructionGateway {
        TransferableValueConstructionResult construct(TransferableValueAtomicSwapConstruction construction);
    }
}
