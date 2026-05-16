package dev.coffer.minecraft.platform.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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

class CofferMinecraftSelectedExchangeCoreArbitrationTest {
    private static final UUID FIRST_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000921");
    private static final UUID SECOND_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000922");

    @Test
    void assembledSelectedOfferReachesCoreApprovalWithoutRuntimeAndPreservesIdentity() {
        ExchangePayload payload = assembledPayload(
                participant(
                        FIRST_PLAYER_ID,
                        "offer-first",
                        2,
                        "minecraft:iron_sword",
                        1,
                        "{Enchantments:[{id:\"minecraft:sharpness\",lvl:3s}],Damage:5}"),
                participant(
                        SECOND_PLAYER_ID,
                        "offer-second",
                        6,
                        "minecraft:shield",
                        1,
                        "{BlockEntityTag:{Base:11}}"));

        ArbitrationResult arbitration = CofferCore.arbitrate(
                payload,
                ignored -> new ResolutionResult.Resolved(new TrueAttestingAuthority()),
                new OutcomeId("selected-core-approved-outcome-1"),
                new MutationPlanId("selected-core-approved-mutation-plan-1"),
                denialReasonIds());

        assertEquals(Decision.APPROVED, arbitration.outcome().decision());
        assertNotNull(arbitration.mutationPlan());
        assertEquals(payload.payloadId(), arbitration.outcome().payloadId());
        assertEquals(1, arbitration.mutationPlan().mutations().size());
        assertFalse(arbitration.mutationPlan().mutations().get(0).descriptor().values().isEmpty());
    }

    @Test
    void assembledSelectedOfferIsDeniedWhenAuthorityIsUnresolvedAndRequiredTruthsAreMissing() {
        ExchangePayload payload = assembledPayload(
                participant(FIRST_PLAYER_ID, "offer-first", 2, "minecraft:stone", 3, null),
                participant(SECOND_PLAYER_ID, "offer-second", 6, "minecraft:dirt", 2, null));

        ArbitrationResult arbitration = CofferCore.arbitrate(
                payload,
                ignored -> new ResolutionResult.Unresolved(),
                new OutcomeId("selected-core-unresolved-outcome-1"),
                new MutationPlanId("selected-core-unresolved-mutation-plan-1"),
                denialReasonIds());

        assertEquals(Decision.DENIED, arbitration.outcome().decision());
        assertNull(arbitration.mutationPlan());
    }

    @Test
    void emptySelectedCaptureRemainsRefusedBeforeArbitration() {
        CofferMinecraftSelectedExchangeRequestAssembly assembly =
                new CofferMinecraftSelectedExchangeRequestAssembly(
                        org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueExchangePayloadConstruction::constructAtomicSwap);

        CofferMinecraftSelectedExchangeRequestAssembly.AssemblyResult.Refused refused =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRequestAssembly.AssemblyResult.Refused.class,
                        assembly.assemble(
                                participant(FIRST_PLAYER_ID, "offer-first", 2),
                                participant(SECOND_PLAYER_ID, "offer-second", 6, "minecraft:dirt", 1, null),
                                "minecraft-inventory"));

        assertEquals(
                CofferMinecraftSelectedExchangeRequestAssembly.FIRST_SELECTED_VALUE_NOT_MATERIALIZED,
                refused.reasonCode());
    }

    @Test
    void explicitFalseTruthAttestationProducesCoreDenialWithoutRuntimeParticipation() {
        ExchangePayload payload = assembledPayload(
                participant(FIRST_PLAYER_ID, "offer-first", 2, "minecraft:stone", 3, null),
                participant(SECOND_PLAYER_ID, "offer-second", 6, "minecraft:dirt", 2, null));

        ArbitrationResult arbitration = CofferCore.arbitrate(
                payload,
                ignored -> new ResolutionResult.Resolved(new FalseAttestingAuthority()),
                new OutcomeId("selected-core-false-outcome-1"),
                new MutationPlanId("selected-core-false-mutation-plan-1"),
                denialReasonIds());

        assertEquals(Decision.DENIED, arbitration.outcome().decision());
        assertNull(arbitration.mutationPlan());
    }

    private static ExchangePayload assembledPayload(
            CofferMinecraftSelectedExchangeRequestAssembly.SelectedParticipant first,
            CofferMinecraftSelectedExchangeRequestAssembly.SelectedParticipant second) {
        CofferMinecraftSelectedExchangeRequestAssembly assembly = CofferMinecraftSelectedExchangeRequestAssembly.create();
        CofferMinecraftSelectedExchangeRequestAssembly.AssemblyResult.Prepared prepared =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRequestAssembly.AssemblyResult.Prepared.class,
                        assembly.assemble(first, second, "minecraft-inventory"));
        return prepared.payload();
    }

    private static CofferMinecraftSelectedExchangeRequestAssembly.SelectedParticipant participant(
            UUID playerId,
            String offerRef,
            int slotIndex,
            String itemId,
            int quantity,
            String nbtPayload) {
        return new CofferMinecraftSelectedExchangeRequestAssembly.SelectedParticipant(
                new ActorRef("player:" + playerId + ":inventory:hotbar"),
                new OfferRef(offerRef),
                new CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot(
                        playerId,
                        new CofferMinecraftSelectedInventoryCapture.SelectedValueBoundary(
                                "main_hand_hotbar",
                                MinecraftPlayerInventoryContainer.Region.HOTBAR,
                                slotIndex),
                        Optional.of(new dev.coffer.minecraft.bindings.inventory.MinecraftItemDescriptor(
                                itemId,
                                quantity,
                                Optional.ofNullable(nbtPayload)))));
    }

    private static CofferMinecraftSelectedExchangeRequestAssembly.SelectedParticipant participant(
            UUID playerId,
            String offerRef,
            int slotIndex) {
        return new CofferMinecraftSelectedExchangeRequestAssembly.SelectedParticipant(
                new ActorRef("player:" + playerId + ":inventory:hotbar"),
                new OfferRef(offerRef),
                new CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot(
                        playerId,
                        new CofferMinecraftSelectedInventoryCapture.SelectedValueBoundary(
                                "main_hand_hotbar",
                                MinecraftPlayerInventoryContainer.Region.HOTBAR,
                                slotIndex),
                        Optional.empty()));
    }

    private static List<ReasonId> denialReasonIds() {
        List<ReasonId> reasonIds = new ArrayList<>();
        for (int index = 0; index < 8; index++) {
            reasonIds.add(new ReasonId("selected-core-reason-" + index));
        }
        return List.copyOf(reasonIds);
    }

    private static final class TrueAttestingAuthority implements Authority {
        @Override
        public java.util.Set<AuthorityIdentifier> identifiers() {
            return java.util.Set.of(TransferableValueCoreAuthority.AUTHORITY_ID);
        }

        @Override
        public AuthorityResponse respond(AuthorityRequest request) {
            List<AuthorityAttestation> attestations = new ArrayList<>();
            for (RequiredTruth truth : request.requiredTruths()) {
                attestations.add(new AuthorityAttestation(
                        truth.truthRef(),
                        true,
                        new OpaqueObject(Map.of())));
            }

            List<CandidateMutation> candidateMutations = new ArrayList<>();
            int index = 0;
            for (MutationRequirement mutationRequirement : request.mutationRequirements()) {
                candidateMutations.add(new CandidateMutation(
                        new MutationRef("selected-core-true-mutation-" + index),
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
    }

    private static final class FalseAttestingAuthority implements Authority {
        @Override
        public java.util.Set<AuthorityIdentifier> identifiers() {
            return java.util.Set.of(TransferableValueCoreAuthority.AUTHORITY_ID);
        }

        @Override
        public AuthorityResponse respond(AuthorityRequest request) {
            List<AuthorityAttestation> attestations = new ArrayList<>();
            for (RequiredTruth truth : request.requiredTruths()) {
                attestations.add(new AuthorityAttestation(
                        truth.truthRef(),
                        false,
                        new OpaqueObject(Map.of())));
            }

            List<CandidateMutation> candidateMutations = new ArrayList<>();
            int index = 0;
            for (MutationRequirement mutationRequirement : request.mutationRequirements()) {
                candidateMutations.add(new CandidateMutation(
                        new MutationRef("selected-core-false-mutation-" + index),
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
    }
}
