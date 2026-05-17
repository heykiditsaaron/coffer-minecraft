package dev.coffer.minecraft.platform.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.coffer.minecraft.bindings.inventory.MinecraftItemDescriptor;
import dev.coffer.minecraft.bindings.inventory.MinecraftPlayerInventoryContainer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.coffer.core.arbitration.ArbitrationResult;
import org.coffer.core.arbitration.CofferCore;
import org.coffer.core.authority.Authority;
import org.coffer.core.authority.ResolutionResult;
import org.coffer.core.model.authority.AuthorityAttestation;
import org.coffer.core.model.authority.AuthorityRequest;
import org.coffer.core.model.authority.AuthorityResponse;
import org.coffer.core.model.authority.CandidateMutation;
import org.coffer.core.model.id.ActorRef;
import org.coffer.core.model.id.AuthorityIdentifier;
import org.coffer.core.model.id.MutationPlanId;
import org.coffer.core.model.id.MutationRef;
import org.coffer.core.model.id.OfferRef;
import org.coffer.core.model.id.OutcomeId;
import org.coffer.core.model.id.ReasonId;
import org.coffer.core.model.outcome.Decision;
import org.coffer.core.model.request.ExchangePayload;
import org.coffer.core.model.request.MutationRequirement;
import org.coffer.core.model.request.RequiredTruth;
import org.coffer.core.model.support.OpaqueObject;
import org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority;
import org.junit.jupiter.api.Test;

class CofferMinecraftAdminShopPresetListingCoreTruthTest {
    private static final UUID PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000001171");
    private static final String SHOP_SUPPLY_UNAVAILABLE = "minecraft.shop.value.not_available";
    private static final String SHOP_NOT_RECEIVABLE = "minecraft.shop.value.not_receivable";

    @Test
    void validFiniteFaucetSupplyCanAuthorizeWhenShopOfferedValueIsAvailable() {
        ArbitrationResult arbitration = arbitrate(
                assembledPayload(finiteListing(), selectedSnapshot("minecraft:emerald", 3, null)),
                new ShopListingAuthority(new TruthPolicy(true, true, true, true)));

        assertEquals(Decision.APPROVED, arbitration.outcome().decision());
        assertNotNull(arbitration.mutationPlan());
    }

    @Test
    void finiteFaucetSupplyDeniesWhenShopOfferedValueIsStaleOrUnavailable() {
        ExchangePayload payload = assembledPayload(finiteListing(), selectedSnapshot("minecraft:emerald", 3, null));

        ArbitrationResult arbitration = arbitrate(
                payload,
                new ShopListingAuthority(new TruthPolicy(true, false, true, true)));

        assertEquals(Decision.DENIED, arbitration.outcome().decision());
        assertNull(arbitration.mutationPlan());
        assertEquals(SHOP_SUPPLY_UNAVAILABLE, arbitration.outcome().reasons().get(0).detail().values().get("reasonCode"));
        assertEquals("admin-shop-preset-listing-payload-1", payload.payloadId().value());
    }

    @Test
    void validInfiniteFaucetSupplyCanAuthorizeWithoutFiniteInventoryDepletionSemantics() {
        CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Prepared prepared =
                prepared(infiniteListing(), selectedSnapshot("minecraft:emerald", 3, null));

        ArbitrationResult arbitration = arbitrate(
                prepared.payload(),
                new ShopListingAuthority(new TruthPolicy(true, true, true, true)));

        assertEquals(Decision.APPROVED, arbitration.outcome().decision());
        assertNotNull(arbitration.mutationPlan());
        assertInstanceOf(
                CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.InfiniteFaucet.class,
                prepared.exchange().supplyMode());
        assertFalse(prepared.exchange().supplyMode()
                instanceof CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.FiniteFaucet);
    }

    @Test
    void infiniteFaucetSupplyDoesNotCounterfeitFiniteRemovabilityOrDepletion() {
        CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Prepared prepared =
                prepared(infiniteListing(), selectedSnapshot("minecraft:emerald", 3, null));

        ArbitrationResult arbitration = arbitrate(
                prepared.payload(),
                new ShopListingAuthority(new TruthPolicy(true, true, true, true)));

        assertEquals(Decision.APPROVED, arbitration.outcome().decision());
        assertEquals("spawn-shop-wall-a",
                ((CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.InfiniteFaucet) prepared.exchange().supplyMode())
                        .listingSurfaceId());
        assertFalse(prepared.construction().secondActor().actorRef().value().contains("stock"));
    }

    @Test
    void playerSelectedCounterOfferTruthIsStillRequired() {
        ArbitrationResult arbitration = arbitrate(
                assembledPayload(infiniteListing(), selectedSnapshot("minecraft:emerald", 3, null)),
                new ShopListingAuthority(new TruthPolicy(false, true, true, true)));

        assertEquals(Decision.DENIED, arbitration.outcome().decision());
        assertNull(arbitration.mutationPlan());
        assertEquals(
                MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE,
                arbitration.outcome().reasons().get(0).detail().values().get("reasonCode"));
    }

    @Test
    void playerCannotReceiveShopOfferedValueDeniesAtCore() {
        ArbitrationResult arbitration = arbitrate(
                assembledPayload(infiniteListing(), selectedSnapshot("minecraft:emerald", 3, null)),
                new ShopListingAuthority(new TruthPolicy(true, true, false, true)));

        assertEquals(Decision.DENIED, arbitration.outcome().decision());
        assertNull(arbitration.mutationPlan());
        assertEquals(
                MinecraftPlayerInventoryContainer.VALUE_NOT_RECEIVABLE,
                arbitration.outcome().reasons().get(0).detail().values().get("reasonCode"));
    }

    @Test
    void shopCannotReceivePlayerCounterOfferDeniesAtCoreWhenModeledSurfaceRequiresIt() {
        ArbitrationResult arbitration = arbitrate(
                assembledPayload(finiteListing(), selectedSnapshot("minecraft:emerald", 3, null)),
                new ShopListingAuthority(new TruthPolicy(true, true, true, false)));

        assertEquals(Decision.DENIED, arbitration.outcome().decision());
        assertNull(arbitration.mutationPlan());
        assertEquals(SHOP_NOT_RECEIVABLE, arbitration.outcome().reasons().get(0).detail().values().get("reasonCode"));
    }

    @Test
    void denialAfterSuccessfulAssemblyRemainsCoreDenialRatherThanConstructionRefusal() {
        CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Prepared prepared =
                prepared(finiteListing(), selectedSnapshot("minecraft:emerald", 3, null));

        ArbitrationResult arbitration = arbitrate(
                prepared.payload(),
                new ShopListingAuthority(new TruthPolicy(true, false, true, true)));

        assertEquals(Decision.DENIED, arbitration.outcome().decision());
        assertNull(arbitration.mutationPlan());
    }

    private static ArbitrationResult arbitrate(ExchangePayload payload, Authority authority) {
        return CofferCore.arbitrate(
                payload,
                ignored -> new ResolutionResult.Resolved(authority),
                new OutcomeId("admin-shop-core-outcome-1"),
                new MutationPlanId("admin-shop-core-plan-1"),
                denialReasonIds());
    }

    private static ExchangePayload assembledPayload(
            CofferMinecraftAdminShopPresetListingConstruction.PresetListing listing,
            CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot selectedSnapshot) {
        return prepared(listing, selectedSnapshot).payload();
    }

    private static CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Prepared prepared(
            CofferMinecraftAdminShopPresetListingConstruction.PresetListing listing,
            CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot selectedSnapshot) {
        CofferMinecraftAdminShopPresetListingRequestAssembly assembly =
                new CofferMinecraftAdminShopPresetListingRequestAssembly(
                        new CofferMinecraftAdminShopPresetListingConstruction(),
                        org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueExchangePayloadConstruction::constructAtomicSwap);
        return assertInstanceOf(
                CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Prepared.class,
                assembly.assemble(Optional.of(listing), playerParticipant(selectedSnapshot), "minecraft-inventory"));
    }

    private static CofferMinecraftAdminShopPresetListingRequestAssembly.SelectedParticipant playerParticipant(
            CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot snapshot) {
        return new CofferMinecraftAdminShopPresetListingRequestAssembly.SelectedParticipant(
                new ActorRef("player:" + PLAYER_ID + ":inventory:hotbar"),
                new OfferRef("player-shop-offer"),
                snapshot);
    }

    private static CofferMinecraftAdminShopPresetListingConstruction.PresetListing infiniteListing() {
        return new CofferMinecraftAdminShopPresetListingConstruction.PresetListing(
                "listing-emerald-for-diamond",
                true,
                new CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.InfiniteFaucet("spawn-shop-wall-a"),
                descriptor("minecraft:diamond", 1, null),
                descriptor("minecraft:emerald", 3, null));
    }

    private static CofferMinecraftAdminShopPresetListingConstruction.PresetListing finiteListing() {
        return new CofferMinecraftAdminShopPresetListingConstruction.PresetListing(
                "listing-finite-emerald-for-diamond",
                true,
                new CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.FiniteFaucet("shop:spawn:stock:diamond-a"),
                descriptor("minecraft:diamond", 1, "{Damage:2}"),
                descriptor("minecraft:emerald", 3, null));
    }

    private static CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot selectedSnapshot(
            String itemId,
            long quantity,
            String nbtPayload) {
        return new CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot(
                PLAYER_ID,
                new CofferMinecraftSelectedInventoryCapture.SelectedValueBoundary(
                        "main_hand_hotbar",
                        MinecraftPlayerInventoryContainer.Region.HOTBAR,
                        2),
                Optional.of(descriptor(itemId, quantity, nbtPayload)));
    }

    private static MinecraftItemDescriptor descriptor(String itemId, long quantity, String nbtPayload) {
        return new MinecraftItemDescriptor(itemId, quantity, Optional.ofNullable(nbtPayload));
    }

    private static List<ReasonId> denialReasonIds() {
        List<ReasonId> reasonIds = new ArrayList<>();
        for (int index = 0; index < 8; index++) {
            reasonIds.add(new ReasonId("admin-shop-core-reason-" + index));
        }
        return List.copyOf(reasonIds);
    }

    private record TruthPolicy(
            boolean playerCounterOfferRemovable,
            boolean shopOfferedValueAvailable,
            boolean playerCanReceive,
            boolean shopCanReceivePlayerCounterOffer) {
    }

    private static final class ShopListingAuthority implements Authority {
        private final TruthPolicy truthPolicy;

        private ShopListingAuthority(TruthPolicy truthPolicy) {
            this.truthPolicy = truthPolicy;
        }

        @Override
        public java.util.Set<AuthorityIdentifier> identifiers() {
            return java.util.Set.of(TransferableValueCoreAuthority.AUTHORITY_ID);
        }

        @Override
        public AuthorityResponse respond(AuthorityRequest request) {
            List<AuthorityAttestation> attestations = new ArrayList<>();
            for (RequiredTruth truth : request.requiredTruths()) {
                String truthRef = truth.truthRef().value();
                if (truthRef.startsWith("admin-shop-player-truth-")) {
                    attestations.add(attestation(
                            truth,
                            truthPolicy.playerCounterOfferRemovable(),
                            MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE));
                    continue;
                }
                if (truthRef.startsWith("admin-shop-shop-truth-")) {
                    attestations.add(attestation(
                            truth,
                            truthPolicy.shopOfferedValueAvailable(),
                            SHOP_SUPPLY_UNAVAILABLE));
                    continue;
                }
                if ("admin-shop-player-can-receive".equals(truthRef)) {
                    attestations.add(attestation(
                            truth,
                            truthPolicy.playerCanReceive(),
                            MinecraftPlayerInventoryContainer.VALUE_NOT_RECEIVABLE));
                    continue;
                }
                if ("admin-shop-shop-can-receive".equals(truthRef)) {
                    attestations.add(attestation(
                            truth,
                            truthPolicy.shopCanReceivePlayerCounterOffer(),
                            SHOP_NOT_RECEIVABLE));
                    continue;
                }
                attestations.add(new AuthorityAttestation(
                        truth.truthRef(),
                        false,
                        new OpaqueObject(Map.of("reasonCode", "UNKNOWN_ADMIN_SHOP_TRUTH"))));
            }

            List<CandidateMutation> candidateMutations = new ArrayList<>();
            int index = 0;
            for (MutationRequirement mutationRequirement : request.mutationRequirements()) {
                candidateMutations.add(new CandidateMutation(
                        new MutationRef("admin-shop-core-mutation-" + index),
                        mutationRequirement.mutationRequirementRef(),
                        mutationRequirement.authority(),
                        new OpaqueObject(mutationRequirement.requirement().values()),
                        mutationRequirement.appliesTo()));
                index++;
            }

            return new AuthorityResponse(
                    TransferableValueCoreAuthority.AUTHORITY_ID,
                    List.copyOf(attestations),
                    List.of(),
                    List.copyOf(candidateMutations));
        }

        private static AuthorityAttestation attestation(
                RequiredTruth truth,
                boolean attested,
                String reasonCode) {
            return new AuthorityAttestation(
                    truth.truthRef(),
                    attested,
                    new OpaqueObject(attested ? Map.of() : Map.of("reasonCode", reasonCode)));
        }
    }
}
