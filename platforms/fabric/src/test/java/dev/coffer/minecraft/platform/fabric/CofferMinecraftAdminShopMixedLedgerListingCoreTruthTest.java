package dev.coffer.minecraft.platform.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.coffer.minecraft.bindings.inventory.MinecraftItemDescriptor;
import dev.coffer.minecraft.bindings.inventory.MinecraftPlayerInventoryContainer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.coffer.core.arbitration.ArbitrationResult;
import org.coffer.core.arbitration.CofferCore;
import org.coffer.core.authority.Authority;
import org.coffer.core.authority.ResolutionResult;
import org.coffer.core.model.authority.AuthorityAttestation;
import org.coffer.core.model.authority.AuthorityRefusal;
import org.coffer.core.model.authority.AuthorityRequest;
import org.coffer.core.model.authority.AuthorityResponse;
import org.coffer.core.model.id.ActorRef;
import org.coffer.core.model.id.AuthorityIdentifier;
import org.coffer.core.model.id.MutationPlanId;
import org.coffer.core.model.id.MutationRef;
import org.coffer.core.model.id.MutationRequirementRef;
import org.coffer.core.model.id.OfferRef;
import org.coffer.core.model.id.OutcomeId;
import org.coffer.core.model.id.ReasonId;
import org.coffer.core.model.id.TruthRef;
import org.coffer.core.model.outcome.CoreReasonKind;
import org.coffer.core.model.outcome.Decision;
import org.coffer.core.model.outcome.ReasonKind;
import org.coffer.core.model.outcome.ReasonSource;
import org.coffer.core.model.request.AuthorityRequirement;
import org.coffer.core.model.request.ExchangePayload;
import org.coffer.core.model.request.MutationRequirement;
import org.coffer.core.model.request.RequiredTruth;
import org.coffer.core.model.request.SubjectRef;
import org.coffer.core.model.request.ValueDeclaration;
import org.coffer.core.model.support.AuthorityDefinedRequirement;
import org.coffer.core.model.support.OpaqueObject;
import org.coffer.core.model.support.ReferenceSet;
import org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority;
import org.junit.jupiter.api.Test;

class CofferMinecraftAdminShopMixedLedgerListingCoreTruthTest {
    private static final UUID PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000001241");
    private static final String SHOP_SUPPLY_UNAVAILABLE = "minecraft.shop.value.not_available";
    private static final String SHOP_NOT_RECEIVABLE = "minecraft.shop.value.not_receivable";

    @Test
    void inventoryOfferedAndLedgerAcceptedListingApprovesWhenExplicitInventoryAndLedgerTruthSucceed() {
        DeterministicLedgerTruthState ledgerState = ledgerState(
                Map.of(playerLedgerCoordinate(), 125L, shopLedgerCoordinate(), 0L),
                Map.of());

        ArbitrationResult arbitration = arbitrate(
                approvalReadyPayload(inventoryForLedgerPrepared().payload()),
                new InventoryTruthAuthority(new InventoryTruthPolicy(true, true)),
                new TestOnlyLedgerTruthAuthority(ledgerState));

        assertEquals(Decision.APPROVED, arbitration.outcome().decision());
        assertNotNull(arbitration.mutationPlan());
        assertEquals(125L, ledgerState.availableBalance(playerLedgerCoordinate()));
        assertEquals(0L, ledgerState.availableBalance(shopLedgerCoordinate()));
    }

    @Test
    void ledgerOfferedAndInventoryAcceptedListingApprovesWhenExplicitInventoryAndLedgerTruthSucceed() {
        DeterministicLedgerTruthState ledgerState = ledgerState(
                Map.of(shopLedgerCoordinate(), 125L),
                Map.of());

        ArbitrationResult arbitration = arbitrate(
                approvalReadyPayload(ledgerForInventoryPrepared().payload()),
                new InventoryTruthAuthority(new InventoryTruthPolicy(true, true)),
                new TestOnlyLedgerTruthAuthority(ledgerState));

        assertEquals(Decision.APPROVED, arbitration.outcome().decision());
        assertNotNull(arbitration.mutationPlan());
        assertEquals(125L, ledgerState.availableBalance(shopLedgerCoordinate()));
    }

    @Test
    void inventoryTruthDenialBlocksApproval() {
        DeterministicLedgerTruthState ledgerState = ledgerState(
                Map.of(shopLedgerCoordinate(), 125L),
                Map.of());

        ArbitrationResult arbitration = arbitrate(
                approvalReadyPayload(ledgerForInventoryPrepared().payload()),
                new InventoryTruthAuthority(new InventoryTruthPolicy(false, true)),
                new TestOnlyLedgerTruthAuthority(ledgerState));

        assertEquals(Decision.DENIED, arbitration.outcome().decision());
        assertNull(arbitration.mutationPlan());
        assertEquals(new ReasonKind.CoreReason(CoreReasonKind.REQUIRED_TRUTH_NOT_SATISFIED),
                arbitration.outcome().reasons().get(0).kind());
        assertEquals(MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE,
                arbitration.outcome().reasons().get(0).detail().values().get("reasonCode"));
    }

    @Test
    void ledgerTruthDenialBlocksApproval() {
        DeterministicLedgerTruthState ledgerState = ledgerState(
                Map.of(playerLedgerCoordinate(), 10L, shopLedgerCoordinate(), 0L),
                Map.of());

        ArbitrationResult arbitration = arbitrate(
                approvalReadyPayload(inventoryForLedgerPrepared().payload()),
                new InventoryTruthAuthority(new InventoryTruthPolicy(true, true)),
                new TestOnlyLedgerTruthAuthority(ledgerState));

        assertEquals(Decision.DENIED, arbitration.outcome().decision());
        assertNull(arbitration.mutationPlan());
        assertEquals(new ReasonKind.CoreReason(CoreReasonKind.REQUIRED_TRUTH_NOT_SATISFIED),
                arbitration.outcome().reasons().get(0).kind());
        assertEquals("INSUFFICIENT_BALANCE",
                arbitration.outcome().reasons().get(0).detail().values().get("reasonCode"));
        assertEquals(10L, ledgerState.availableBalance(playerLedgerCoordinate()));
    }

    @Test
    void ledgerUnavailableTruthBlocksApprovalWithoutCounterfeitDenialOrSuccess() {
        DeterministicLedgerTruthState ledgerState = ledgerState(
                Map.of(playerLedgerCoordinate(), 125L),
                Map.of(playerLedgerCoordinate(), TruthBehavior.REFUSED));

        ArbitrationResult arbitration = arbitrate(
                approvalReadyPayload(inventoryForLedgerPrepared().payload()),
                new InventoryTruthAuthority(new InventoryTruthPolicy(true, true)),
                new TestOnlyLedgerTruthAuthority(ledgerState));

        assertEquals(Decision.DENIED, arbitration.outcome().decision());
        assertNull(arbitration.mutationPlan());
        assertEquals(ReasonKind.AuthorityRefusalReason.AUTHORITY_REFUSAL,
                arbitration.outcome().reasons().get(0).kind());
        assertEquals(new ReasonSource.AuthoritySource(CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.TEST_LEDGER_AUTHORITY_ID),
                arbitration.outcome().reasons().get(0).source());
        assertEquals("LEDGER_UNAVAILABLE",
                arbitration.outcome().reasons().get(0).detail().values().get("reasonCode"));
    }

    @Test
    void ledgerUnknownTruthBlocksApprovalWithoutCounterfeitDenialOrSuccess() {
        DeterministicLedgerTruthState ledgerState = ledgerState(
                Map.of(shopLedgerCoordinate(), 125L),
                Map.of(shopLedgerCoordinate(), TruthBehavior.UNKNOWN));

        ArbitrationResult arbitration = arbitrate(
                approvalReadyPayload(ledgerForInventoryPrepared().payload()),
                new InventoryTruthAuthority(new InventoryTruthPolicy(true, true)),
                new TestOnlyLedgerTruthAuthority(ledgerState));

        assertEquals(Decision.DENIED, arbitration.outcome().decision());
        assertNull(arbitration.mutationPlan());
        assertEquals(ReasonKind.AuthorityRefusalReason.AUTHORITY_REFUSAL,
                arbitration.outcome().reasons().get(0).kind());
        assertEquals(new ReasonSource.AuthoritySource(CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.TEST_LEDGER_AUTHORITY_ID),
                arbitration.outcome().reasons().get(0).source());
        assertEquals("LEDGER_TRUTH_UNKNOWN",
                arbitration.outcome().reasons().get(0).detail().values().get("reasonCode"));
    }

    @Test
    void coreTruthProofDoesNotEnterRuntimeOrMutateInventoryOrLedger() {
        DeterministicLedgerTruthState ledgerState = ledgerState(
                Map.of(playerLedgerCoordinate(), 125L, shopLedgerCoordinate(), 0L),
                Map.of());
        long beforePlayerBalance = ledgerState.availableBalance(playerLedgerCoordinate());
        long beforeShopBalance = ledgerState.availableBalance(shopLedgerCoordinate());

        ArbitrationResult arbitration = arbitrate(
                approvalReadyPayload(inventoryForLedgerPrepared().payload()),
                new InventoryTruthAuthority(new InventoryTruthPolicy(true, true)),
                new TestOnlyLedgerTruthAuthority(ledgerState));

        assertEquals(Decision.APPROVED, arbitration.outcome().decision());
        assertEquals(beforePlayerBalance, ledgerState.availableBalance(playerLedgerCoordinate()));
        assertEquals(beforeShopBalance, ledgerState.availableBalance(shopLedgerCoordinate()));
        assertEquals("noop",
                arbitration.mutationPlan().mutations().get(0).descriptor().values().get("type"));
        assertFalse(arbitration.mutationPlan().mutations().stream()
                .anyMatch(mutation -> CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.TEST_LEDGER_AUTHORITY_ID.equals(mutation.authority())
                        || TransferableValueCoreAuthority.AUTHORITY_ID.equals(mutation.authority())));
    }

    private static ArbitrationResult arbitrate(
            ExchangePayload payload,
            InventoryTruthAuthority inventoryAuthority,
            TestOnlyLedgerTruthAuthority ledgerAuthority) {
        return CofferCore.arbitrate(
                payload,
                authorityId -> resolveAuthority(authorityId, inventoryAuthority, ledgerAuthority, new NoOpMutationAuthority()),
                new OutcomeId("mixed-ledger-core-outcome-1"),
                new MutationPlanId("mixed-ledger-core-plan-1"),
                denialReasonIds());
    }

    private static ExchangePayload approvalReadyPayload(ExchangePayload payload) {
        List<AuthorityRequirement> authorityRequirements = new ArrayList<>(payload.authorityRequirements());
        authorityRequirements.add(new AuthorityRequirement(NoOpMutationAuthority.AUTHORITY_ID, List.of()));
        return new ExchangePayload(
                payload.payloadId(),
                payload.actors(),
                payload.offers(),
                List.copyOf(authorityRequirements),
                List.of(new MutationRequirement(
                        new MutationRequirementRef("mixed-ledger-noop-mutation-requirement-1"),
                        NoOpMutationAuthority.AUTHORITY_ID,
                        new ReferenceSet(
                                Set.copyOf(payload.actors().stream().map(actor -> actor.actorRef()).toList()),
                                Set.copyOf(payload.offers().stream().flatMap(offer -> offer.values().stream()).map(ValueDeclaration::valueRef).toList())),
                        new AuthorityDefinedRequirement(Map.of("type", "noop")))));
    }

    private static ResolutionResult resolveAuthority(
            AuthorityIdentifier authorityId,
            InventoryTruthAuthority inventoryAuthority,
            TestOnlyLedgerTruthAuthority ledgerAuthority,
            NoOpMutationAuthority noOpMutationAuthority) {
        if (authorityId.equals(TransferableValueCoreAuthority.AUTHORITY_ID)) {
            return new ResolutionResult.Resolved(inventoryAuthority);
        }
        if (authorityId.equals(CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.TEST_LEDGER_AUTHORITY_ID)) {
            return new ResolutionResult.Resolved(ledgerAuthority);
        }
        if (authorityId.equals(NoOpMutationAuthority.AUTHORITY_ID)) {
            return new ResolutionResult.Resolved(noOpMutationAuthority);
        }
        return new ResolutionResult.Unresolved();
    }

    private static CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.AssemblyResult.Prepared inventoryForLedgerPrepared() {
        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly assembly =
                new CofferMinecraftAdminShopMixedLedgerListingRequestAssembly(
                        new CofferMinecraftAdminShopMixedLedgerListingConstruction(Set.of("proof:coin")));
        return assertInstanceOf(
                CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.AssemblyResult.Prepared.class,
                assembly.assemble(
                        Optional.of(inventoryForLedgerListing()),
                        playerParticipant(selectedSnapshot("minecraft:stick", 1, null)),
                        "minecraft-inventory"));
    }

    private static CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.AssemblyResult.Prepared ledgerForInventoryPrepared() {
        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly assembly =
                new CofferMinecraftAdminShopMixedLedgerListingRequestAssembly(
                        new CofferMinecraftAdminShopMixedLedgerListingConstruction(Set.of("proof:coin")));
        return assertInstanceOf(
                CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.AssemblyResult.Prepared.class,
                assembly.assemble(
                        Optional.of(ledgerForInventoryListing()),
                        playerParticipant(selectedSnapshot("minecraft:emerald", 3, "{custom:1b}")),
                        "minecraft-inventory"));
    }

    private static CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.SelectedParticipant playerParticipant(
            CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot snapshot) {
        return new CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.SelectedParticipant(
                new ActorRef("player:" + PLAYER_ID + ":inventory:hotbar"),
                new OfferRef("player-mixed-core-offer"),
                snapshot);
    }

    private static CofferMinecraftAdminShopMixedLedgerListingConstruction.MixedListing inventoryForLedgerListing() {
        return new CofferMinecraftAdminShopMixedLedgerListingConstruction.MixedListing(
                "listing-diamond-for-ledger",
                true,
                new CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.InventoryTerm(
                        descriptor("minecraft:diamond", 1, "{Damage:2}")),
                new CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.LedgerTerm(
                        ledger("proof-ledger-authority", "spawn-ledger", "player:" + PLAYER_ID + ":wallet", "proof:coin", 125L)),
                Optional.empty());
    }

    private static CofferMinecraftAdminShopMixedLedgerListingConstruction.MixedListing ledgerForInventoryListing() {
        return new CofferMinecraftAdminShopMixedLedgerListingConstruction.MixedListing(
                "listing-ledger-for-emerald",
                true,
                new CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.LedgerTerm(
                        ledger("proof-ledger-authority", "spawn-ledger", "shop:spawn:treasury", "proof:coin", 125L)),
                new CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.InventoryTerm(
                        descriptor("minecraft:emerald", 3, "{custom:1b}")),
                Optional.of(new CofferMinecraftAdminShopMixedLedgerListingConstruction.ListingValuationParticipation(
                        "spawn-shop-valuation-a",
                        "listing:spawn-shop:valuation-surface-a")));
    }

    private static CofferMinecraftAdminShopMixedLedgerListingConstruction.LedgerParticipation ledger(
            String authorityId,
            String ledgerId,
            String accountId,
            String unitId,
            long amount) {
        return new CofferMinecraftAdminShopMixedLedgerListingConstruction.LedgerParticipation(
                authorityId,
                ledgerId,
                accountId,
                unitId,
                amount);
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

    private static DeterministicLedgerTruthState ledgerState(
            Map<LedgerCoordinate, Long> balances,
            Map<LedgerCoordinate, TruthBehavior> debitBehaviors) {
        return new DeterministicLedgerTruthState(balances, debitBehaviors);
    }

    private static LedgerCoordinate playerLedgerCoordinate() {
        return new LedgerCoordinate("spawn-ledger", "player:" + PLAYER_ID + ":wallet", "proof:coin");
    }

    private static LedgerCoordinate shopLedgerCoordinate() {
        return new LedgerCoordinate("spawn-ledger", "shop:spawn:treasury", "proof:coin");
    }

    private static List<ReasonId> denialReasonIds() {
        List<ReasonId> reasonIds = new ArrayList<>();
        for (int index = 0; index < 8; index++) {
            reasonIds.add(new ReasonId("mixed-ledger-core-reason-" + index));
        }
        return List.copyOf(reasonIds);
    }

    private record InventoryTruthPolicy(boolean offeredInventoryAvailable, boolean receiverCanReceive) {
    }

    private static final class InventoryTruthAuthority implements Authority {
        private final InventoryTruthPolicy truthPolicy;

        private InventoryTruthAuthority(InventoryTruthPolicy truthPolicy) {
            this.truthPolicy = truthPolicy;
        }

        @Override
        public Set<AuthorityIdentifier> identifiers() {
            return Set.of(TransferableValueCoreAuthority.AUTHORITY_ID);
        }

        @Override
        public AuthorityResponse respond(AuthorityRequest request) {
            List<AuthorityAttestation> attestations = new ArrayList<>();
            for (RequiredTruth truth : request.requiredTruths()) {
                String truthRef = truth.truthRef().value();
                if (truthRef.contains("inventory-removably-owned")) {
                    attestations.add(attestation(
                            truth,
                            truthPolicy.offeredInventoryAvailable(),
                            truthRef.startsWith("mixed-shop-")
                                    ? SHOP_SUPPLY_UNAVAILABLE
                                    : MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE));
                    continue;
                }
                if (truthRef.contains("can-receive-inventory")) {
                    attestations.add(attestation(
                            truth,
                            truthPolicy.receiverCanReceive(),
                            truthRef.startsWith("mixed-shop-")
                                    ? SHOP_NOT_RECEIVABLE
                                    : MinecraftPlayerInventoryContainer.VALUE_NOT_RECEIVABLE));
                    continue;
                }
                attestations.add(new AuthorityAttestation(
                        truth.truthRef(),
                        false,
                        new OpaqueObject(Map.of("reasonCode", "UNKNOWN_MIXED_INVENTORY_TRUTH"))));
            }
            return new AuthorityResponse(TransferableValueCoreAuthority.AUTHORITY_ID, List.copyOf(attestations), List.of(), List.of());
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

    private static final class TestOnlyLedgerTruthAuthority implements Authority {
        private final DeterministicLedgerTruthState truthState;

        private TestOnlyLedgerTruthAuthority(DeterministicLedgerTruthState truthState) {
            this.truthState = truthState;
        }

        @Override
        public Set<AuthorityIdentifier> identifiers() {
            return Set.of(CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.TEST_LEDGER_AUTHORITY_ID);
        }

        @Override
        public AuthorityResponse respond(AuthorityRequest request) {
            List<AuthorityAttestation> attestations = new ArrayList<>();
            List<AuthorityRefusal> refusals = new ArrayList<>();
            for (RequiredTruth truth : request.requiredTruths()) {
                Map<String, Object> requirement = truth.requirement().values();
                LedgerCoordinate coordinate = new LedgerCoordinate(
                        (String) requirement.get("ledgerId"),
                        (String) requirement.get("accountId"),
                        (String) requirement.get("unitId"));
                long amount = ((Number) requirement.get("amount")).longValue();
                TruthBehavior behavior = truthState.debitBehavior(coordinate);
                if (behavior == TruthBehavior.REFUSED) {
                    refusals.add(new AuthorityRefusal.TruthRefusal(
                            truth.truthRef(),
                            "LEDGER_UNAVAILABLE",
                            detail("LEDGER_UNAVAILABLE", coordinate, amount, Map.of())));
                    continue;
                }
                if (behavior == TruthBehavior.UNKNOWN) {
                    refusals.add(new AuthorityRefusal.TruthRefusal(
                            truth.truthRef(),
                            "LEDGER_TRUTH_UNKNOWN",
                            detail("LEDGER_TRUTH_UNKNOWN", coordinate, amount, Map.of())));
                    continue;
                }
                long available = truthState.availableBalance(coordinate);
                if (available >= amount) {
                    attestations.add(new AuthorityAttestation(
                            truth.truthRef(),
                            true,
                            detail("SUCCESS", coordinate, amount, Map.of())));
                } else {
                    attestations.add(new AuthorityAttestation(
                            truth.truthRef(),
                            false,
                            detail("INSUFFICIENT_BALANCE", coordinate, amount, Map.of("availableBalance", available))));
                }
            }
            return new AuthorityResponse(
                    CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.TEST_LEDGER_AUTHORITY_ID,
                    List.copyOf(attestations),
                    List.copyOf(refusals),
                    List.of());
        }

        private static OpaqueObject detail(
                String reasonCode,
                LedgerCoordinate coordinate,
                long amount,
                Map<String, Object> extras) {
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("reasonCode", reasonCode);
            values.put("ledgerId", coordinate.ledgerId());
            values.put("accountId", coordinate.accountId());
            values.put("unitId", coordinate.unitId());
            values.put("amount", amount);
            values.putAll(extras);
            return new OpaqueObject(values);
        }
    }

    private static final class NoOpMutationAuthority implements Authority {
        private static final AuthorityIdentifier AUTHORITY_ID = new AuthorityIdentifier("NoOpMutationAuthority");

        @Override
        public Set<AuthorityIdentifier> identifiers() {
            return Set.of(AUTHORITY_ID);
        }

        @Override
        public AuthorityResponse respond(AuthorityRequest request) {
            return new AuthorityResponse(
                    AUTHORITY_ID,
                    List.of(),
                    List.of(),
                    List.of(new org.coffer.core.model.authority.CandidateMutation(
                            new MutationRef("noop-mutation-1"),
                            request.mutationRequirements().get(0).mutationRequirementRef(),
                            AUTHORITY_ID,
                            new OpaqueObject(Map.of("type", "noop")),
                            request.mutationRequirements().get(0).appliesTo())));
        }
    }

    private enum TruthBehavior {
        AVAILABLE,
        REFUSED,
        UNKNOWN
    }

    private record LedgerCoordinate(String ledgerId, String accountId, String unitId) {
    }

    private record DeterministicLedgerTruthState(
            Map<LedgerCoordinate, Long> availableBalances,
            Map<LedgerCoordinate, TruthBehavior> debitBehaviors) {
        private DeterministicLedgerTruthState {
            availableBalances = Map.copyOf(availableBalances);
            debitBehaviors = Map.copyOf(debitBehaviors);
        }

        private long availableBalance(LedgerCoordinate coordinate) {
            return availableBalances.getOrDefault(coordinate, 0L);
        }

        private TruthBehavior debitBehavior(LedgerCoordinate coordinate) {
            return debitBehaviors.getOrDefault(coordinate, TruthBehavior.AVAILABLE);
        }
    }
}
