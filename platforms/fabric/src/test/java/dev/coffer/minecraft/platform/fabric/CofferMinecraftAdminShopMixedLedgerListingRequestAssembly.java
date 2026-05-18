package dev.coffer.minecraft.platform.fabric;

import dev.coffer.minecraft.bindings.inventory.MinecraftContainerResolver;
import dev.coffer.minecraft.bindings.inventory.MinecraftDescriptorFactory;
import dev.coffer.minecraft.bindings.inventory.MinecraftItemDescriptor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.coffer.core.model.id.ActorRef;
import org.coffer.core.model.id.AuthorityIdentifier;
import org.coffer.core.model.id.OfferRef;
import org.coffer.core.model.id.PayloadId;
import org.coffer.core.model.id.TruthRef;
import org.coffer.core.model.id.ValueRef;
import org.coffer.core.model.request.ActorDeclaration;
import org.coffer.core.model.request.AuthorityRequirement;
import org.coffer.core.model.request.ExchangePayload;
import org.coffer.core.model.request.Offer;
import org.coffer.core.model.request.RequiredTruth;
import org.coffer.core.model.request.SubjectRef;
import org.coffer.core.model.request.ValueDeclaration;
import org.coffer.core.model.support.AuthorityDefinedRequirement;
import org.coffer.core.model.support.OpaqueObject;
import org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority;

final class CofferMinecraftAdminShopMixedLedgerListingRequestAssembly {
    static final AuthorityIdentifier TEST_LEDGER_AUTHORITY_ID = new AuthorityIdentifier("TestLedgerAuthority");
    static final String TEST_LEDGER_SCHEMA_VERSION = "ledger.test-only.v0.1";
    static final String TEST_LEDGER_VALUE_TYPE = "ledger.declarative.minor-unit";
    static final String TEST_LEDGER_CAN_DEBIT = "can-debit";
    static final String SHOP_LISTING_ACTOR_KIND = "minecraft_admin_shop_listing";
    static final String LISTING_ID = "shopListingId";
    static final String LISTING_VALUATION_ID = "shopListingValuationId";
    static final String LISTING_VALUATION_SURFACE_ID = "shopListingValuationSurfaceId";

    private static final PayloadId PAYLOAD_ID = new PayloadId("admin-shop-mixed-ledger-payload-1");

    private final CofferMinecraftAdminShopMixedLedgerListingConstruction listingConstruction;

    CofferMinecraftAdminShopMixedLedgerListingRequestAssembly(
            CofferMinecraftAdminShopMixedLedgerListingConstruction listingConstruction) {
        this.listingConstruction = Objects.requireNonNull(listingConstruction, "listingConstruction");
    }

    AssemblyResult assemble(
            Optional<CofferMinecraftAdminShopMixedLedgerListingConstruction.MixedListing> listing,
            SelectedParticipant player,
            String bindingId) {
        Objects.requireNonNull(listing, "listing");
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(bindingId, "bindingId");

        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult constructionResult =
                listingConstruction.prepare(listing, Optional.of(player.snapshot()));
        if (constructionResult instanceof CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Refused refused) {
            return new AssemblyResult.Refused(refused.reasonCode());
        }

        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Ready ready =
                (CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Ready) constructionResult;
        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConcreteExchange exchange = ready.exchange();

        ActorDeclaration playerActor = playerActor(player.actorRef());
        ActorDeclaration shopActor = shopActor(exchange);
        ValueDeclaration playerValue = value("mixed-player-value", exchange.acceptedTerm());
        ValueDeclaration shopValue = value("mixed-shop-value", exchange.offeredTerm());

        List<AuthorityRequirement> authorityRequirements = new ArrayList<>();
        authorityRequirements.add(inventoryAuthorityRequirement(exchange, playerActor, shopActor, playerValue, shopValue, bindingId));
        authorityRequirements.add(ledgerAuthorityRequirement(exchange, playerValue, shopValue));

        ExchangePayload payload = new ExchangePayload(
                PAYLOAD_ID,
                List.of(playerActor, shopActor),
                List.of(
                        new Offer(player.offerRef(), playerActor.actorRef(), List.of(playerValue)),
                        new Offer(new OfferRef("shop-mixed-offer-" + exchange.listingId()), shopActor.actorRef(), List.of(shopValue))),
                List.copyOf(authorityRequirements),
                List.of());
        return new AssemblyResult.Prepared(exchange, payload);
    }

    private static AuthorityRequirement inventoryAuthorityRequirement(
            CofferMinecraftAdminShopMixedLedgerListingConstruction.ConcreteExchange exchange,
            ActorDeclaration playerActor,
            ActorDeclaration shopActor,
            ValueDeclaration playerValue,
            ValueDeclaration shopValue,
            String bindingId) {
        if (exchange.offeredTerm() instanceof CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.InventoryTerm) {
            return new AuthorityRequirement(
                    TransferableValueCoreAuthority.AUTHORITY_ID,
                    List.of(
                            removablyOwnedTruth(
                                    new TruthRef("mixed-shop-inventory-removably-owned"),
                                    shopValue.valueRef(),
                                    bindingId),
                            canReceiveTruth(
                                    new TruthRef("mixed-player-can-receive-inventory"),
                                    playerActor.actorRef(),
                                    List.of(shopValue.valueRef()),
                                    bindingId)));
        }
        return new AuthorityRequirement(
                TransferableValueCoreAuthority.AUTHORITY_ID,
                List.of(
                        removablyOwnedTruth(
                                new TruthRef("mixed-player-inventory-removably-owned"),
                                playerValue.valueRef(),
                                bindingId),
                        canReceiveTruth(
                                new TruthRef("mixed-shop-can-receive-inventory"),
                                shopActor.actorRef(),
                                List.of(playerValue.valueRef()),
                                bindingId)));
    }

    private static AuthorityRequirement ledgerAuthorityRequirement(
            CofferMinecraftAdminShopMixedLedgerListingConstruction.ConcreteExchange exchange,
            ValueDeclaration playerValue,
            ValueDeclaration shopValue) {
        if (exchange.acceptedTerm() instanceof CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.LedgerTerm acceptedLedger) {
            return new AuthorityRequirement(
                    TEST_LEDGER_AUTHORITY_ID,
                    List.of(canDebitTruth(
                            new TruthRef("mixed-player-ledger-can-debit"),
                            playerValue.valueRef(),
                            acceptedLedger.value())));
        }
        CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.LedgerTerm offeredLedger =
                (CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.LedgerTerm) exchange.offeredTerm();
        return new AuthorityRequirement(
                TEST_LEDGER_AUTHORITY_ID,
                List.of(canDebitTruth(
                        new TruthRef("mixed-shop-ledger-can-debit"),
                        shopValue.valueRef(),
                        offeredLedger.value())));
    }

    private static ActorDeclaration playerActor(ActorRef actorRef) {
        return new ActorDeclaration(
                actorRef,
                MinecraftContainerResolver.PLAYER_INVENTORY_KIND,
                new OpaqueObject(Map.of()));
    }

    private static ActorDeclaration shopActor(CofferMinecraftAdminShopMixedLedgerListingConstruction.ConcreteExchange exchange) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put(LISTING_ID, exchange.listingId());
        exchange.listingValuationParticipation().ifPresent(valuation -> {
            metadata.put(LISTING_VALUATION_ID, valuation.valuationId());
            metadata.put(LISTING_VALUATION_SURFACE_ID, valuation.valuationSurfaceId());
        });
        return new ActorDeclaration(
                new ActorRef("admin-shop:listing:" + exchange.listingId()),
                SHOP_LISTING_ACTOR_KIND,
                new OpaqueObject(Map.copyOf(metadata)));
    }

    private static ValueDeclaration value(
            String valueRef,
            CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm term) {
        if (term instanceof CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.InventoryTerm inventory) {
            return inventoryValue(valueRef, inventory.value());
        }
        CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.LedgerTerm ledger =
                (CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.LedgerTerm) term;
        return ledgerValue(valueRef, ledger.value());
    }

    private static ValueDeclaration inventoryValue(String valueRef, MinecraftItemDescriptor descriptor) {
        Map<String, Object> descriptorValues = new LinkedHashMap<>();
        descriptorValues.put(MinecraftDescriptorFactory.ITEM_ID, descriptor.itemId());
        descriptorValues.put(MinecraftDescriptorFactory.QUANTITY, descriptor.quantity());
        descriptor.nbtPayload().ifPresent(payload -> descriptorValues.put(MinecraftDescriptorFactory.NBT_PAYLOAD, payload));
        return new ValueDeclaration(
                new ValueRef(valueRef),
                TransferableValueCoreAuthority.AUTHORITY_ID,
                new OpaqueObject(Map.copyOf(descriptorValues)));
    }

    private static ValueDeclaration ledgerValue(
            String valueRef,
            CofferMinecraftAdminShopMixedLedgerListingConstruction.LedgerParticipation ledger) {
        return new ValueDeclaration(
                new ValueRef(valueRef),
                TEST_LEDGER_AUTHORITY_ID,
                new OpaqueObject(Map.of(
                        "schemaVersion", TEST_LEDGER_SCHEMA_VERSION,
                        "valueType", TEST_LEDGER_VALUE_TYPE,
                        "ledgerId", ledger.ledgerId(),
                        "accountId", ledger.accountId(),
                        "unitId", ledger.unitId(),
                        "amount", ledger.amount())));
    }

    private static RequiredTruth removablyOwnedTruth(TruthRef truthRef, ValueRef valueRef, String bindingId) {
        return new RequiredTruth(
                truthRef,
                new SubjectRef.ValueSubjectRef(valueRef),
                new AuthorityDefinedRequirement(requirement(
                        TransferableValueCoreAuthority.REMOVABLY_OWNED,
                        bindingId)));
    }

    private static RequiredTruth canReceiveTruth(
            TruthRef truthRef,
            ActorRef actorRef,
            List<ValueRef> valueRefs,
            String bindingId) {
        Map<String, Object> requirement = requirement(TransferableValueCoreAuthority.CAN_RECEIVE_AS_REMOVABLE, bindingId);
        requirement.put("valueRefs", valueRefs.stream().map(ValueRef::value).toList());
        return new RequiredTruth(
                truthRef,
                new SubjectRef.ActorSubjectRef(actorRef),
                new AuthorityDefinedRequirement(requirement));
    }

    private static RequiredTruth canDebitTruth(
            TruthRef truthRef,
            ValueRef valueRef,
            CofferMinecraftAdminShopMixedLedgerListingConstruction.LedgerParticipation ledger) {
        Map<String, Object> requirement = new LinkedHashMap<>();
        requirement.put("schemaVersion", TEST_LEDGER_SCHEMA_VERSION);
        requirement.put("type", TEST_LEDGER_CAN_DEBIT);
        requirement.put("ledgerId", ledger.ledgerId());
        requirement.put("accountId", ledger.accountId());
        requirement.put("unitId", ledger.unitId());
        requirement.put("amount", ledger.amount());
        return new RequiredTruth(
                truthRef,
                new SubjectRef.ValueSubjectRef(valueRef),
                new AuthorityDefinedRequirement(requirement));
    }

    private static Map<String, Object> requirement(String type, String bindingId) {
        Map<String, Object> requirement = new LinkedHashMap<>();
        requirement.put("schemaVersion", TransferableValueCoreAuthority.SCHEMA_VERSION);
        requirement.put("type", type);
        requirement.put("bindingId", bindingId);
        return requirement;
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
                CofferMinecraftAdminShopMixedLedgerListingConstruction.ConcreteExchange exchange,
                ExchangePayload payload)
                implements AssemblyResult {
            public Prepared {
                Objects.requireNonNull(exchange, "exchange");
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
}
