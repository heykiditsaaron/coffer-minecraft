package dev.coffer.minecraft.platform.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.coffer.core.arbitration.ArbitrationResult;
import org.coffer.core.arbitration.CofferCore;
import org.coffer.core.model.id.ActorRef;
import org.coffer.core.model.id.MutationPlanId;
import org.coffer.core.model.id.MutationRef;
import org.coffer.core.model.id.MutationRequirementRef;
import org.coffer.core.model.id.OfferRef;
import org.coffer.core.model.id.OutcomeId;
import org.coffer.core.model.id.ReasonId;
import org.coffer.core.model.mutation.AuthorizedMutation;
import org.coffer.core.model.mutation.MutationPlan;
import org.coffer.core.model.outcome.Decision;
import org.coffer.core.model.outcome.Outcome;
import org.coffer.core.model.request.ExchangePayload;
import org.coffer.core.model.support.OpaqueObject;
import org.coffer.core.model.support.ReferenceSet;
import org.coffer.runtime.model.execution.ExecutionResult;
import org.coffer.runtime.model.execution.ExecutionStatus;
import org.coffer.runtime.model.execution.MutationExecutionResult;
import org.coffer.runtime.model.execution.MutationExecutionStatus;
import org.coffer.runtime.model.id.ExecutionPlanId;
import org.coffer.runtime.model.id.ExecutionResultId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CofferMinecraftSelectedExchangeSubmissionChainTest {
    private static final long TIMESTAMP = 1_700_000_000_000L;
    private static final UUID FIRST_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000971");
    private static final UUID SECOND_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000972");

    @TempDir
    Path tempDir;

    @Test
    void unconfirmedAndOneSidedConfirmedExchangeCannotSubmit() throws IOException {
        AtomicInteger runtimeCalls = new AtomicInteger();
        CofferMinecraftSelectedExchangeConfirmation confirmation = new CofferMinecraftSelectedExchangeConfirmation();
        CofferMinecraftLifecycleAccountability accountability = accountability();
        CofferMinecraftSelectedExchangeSubmissionChain chain = new CofferMinecraftSelectedExchangeSubmissionChain(
                confirmation,
                CofferMinecraftSelectedExchangeRequestAssembly.create(),
                deniedParticipation(runtimeCalls),
                new CofferMinecraftSelectedExchangeAccountabilityProjection(accountability));
        CofferMinecraftSelectedExchangeConfirmation.ExchangeState state = state(
                participant(FIRST_PLAYER_ID, "offer-first", 2, "minecraft:stone", 3, null),
                participant(SECOND_PLAYER_ID, "offer-second", 6, "minecraft:dirt", 2, null));

        CofferMinecraftSelectedExchangeSubmissionChain.SubmissionResult.NotSubmitted unconfirmed =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeSubmissionChain.SubmissionResult.NotSubmitted.class,
                        chain.submit(tempDir, state, CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger.empty()));
        CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger oneSidedLedger =
                confirmation.confirm(state, state.first().actorRef(), CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger.empty());
        CofferMinecraftSelectedExchangeSubmissionChain.SubmissionResult.NotSubmitted oneSided =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeSubmissionChain.SubmissionResult.NotSubmitted.class,
                        chain.submit(tempDir, state, oneSidedLedger));

        assertEquals(CofferMinecraftSelectedExchangeConfirmation.FIRST_PARTY_NOT_CONFIRMED, unconfirmed.reasonCode());
        assertEquals(CofferMinecraftSelectedExchangeConfirmation.SECOND_PARTY_NOT_CONFIRMED, oneSided.reasonCode());
        assertEquals(0, runtimeCalls.get());
        assertTrue(Files.notExists(accountability.logPath(tempDir)));
    }

    @Test
    void dualConfirmedExchangeTravelsThroughNonMutatingApprovalChainWithoutCounterfeitSuccess() throws IOException {
        AtomicReference<MutationPlan> seenPlan = new AtomicReference<>();
        CofferMinecraftSelectedExchangeConfirmation confirmation = new CofferMinecraftSelectedExchangeConfirmation();
        CofferMinecraftLifecycleAccountability accountability = accountability();
        CofferMinecraftSelectedExchangeSubmissionChain chain = new CofferMinecraftSelectedExchangeSubmissionChain(
                confirmation,
                CofferMinecraftSelectedExchangeRequestAssembly.create(),
                new CofferMinecraftSelectedExchangeRuntimeParticipation(
                        CofferMinecraftSelectedExchangeSubmissionChainTest::approvedArbitration,
                        mutationPlan -> {
                            seenPlan.set(mutationPlan);
                            return runtimeUnknown(mutationPlan, "MALFORMED_RUNTIME_DESCRIPTOR");
                        }),
                new CofferMinecraftSelectedExchangeAccountabilityProjection(accountability));
        CofferMinecraftSelectedExchangeConfirmation.ExchangeState state = state(
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
        CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger ledger =
                confirmation.confirm(state, state.first().actorRef(), CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger.empty());
        ledger = confirmation.confirm(state, state.second().actorRef(), ledger);

        CofferMinecraftSelectedExchangeSubmissionChain.SubmissionResult.Submitted submitted =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeSubmissionChain.SubmissionResult.Submitted.class,
                        chain.submit(tempDir, state, ledger));

        Map<String, Object> firstDescriptor = submitted.prepared().construction().firstValues().get(0).descriptor().values();
        assertEquals("minecraft:iron_sword", firstDescriptor.get("itemId"));
        assertEquals(1L, ((Number) firstDescriptor.get("quantity")).longValue());
        assertEquals(
                "{Enchantments:[{id:\"minecraft:sharpness\",lvl:3s}],Damage:5}",
                firstDescriptor.get("nbtPayload"));
        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated runtime =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated.class,
                        submitted.participation());
        assertEquals(Decision.APPROVED, runtime.arbitration().outcome().decision());
        assertSame(runtime.arbitration().mutationPlan(), seenPlan.get());
        assertEquals(ExecutionStatus.FULL_UNKNOWN, runtime.execution().status());
        assertEquals(MutationExecutionStatus.MUTATION_UNKNOWN, runtime.execution().mutationResults().get(0).status());

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));
        assertEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"selected-submission-1\",\"recordType\":\"CER\",\"stage\":\"fabric_core_approved\",\"seam\":\"fabric_core\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"selected-submission-2\",\"recordType\":\"CER\",\"stage\":\"fabric_runtime_unknown\",\"seam\":\"fabric_runtime\",\"code\":\"MALFORMED_RUNTIME_DESCRIPTOR\"}"),
                lines);
        assertFalse(lines.stream().anyMatch(line -> line.contains("fabric_runtime_succeeded")));
    }

    @Test
    void dualConfirmedExchangePreservesCoreDenialAndDoesNotProjectRuntimeParticipation() throws IOException {
        AtomicInteger runtimeCalls = new AtomicInteger();
        CofferMinecraftSelectedExchangeConfirmation confirmation = new CofferMinecraftSelectedExchangeConfirmation();
        CofferMinecraftLifecycleAccountability accountability = accountability();
        CofferMinecraftSelectedExchangeSubmissionChain chain = new CofferMinecraftSelectedExchangeSubmissionChain(
                confirmation,
                CofferMinecraftSelectedExchangeRequestAssembly.create(),
                deniedParticipation(runtimeCalls),
                new CofferMinecraftSelectedExchangeAccountabilityProjection(accountability));
        CofferMinecraftSelectedExchangeConfirmation.ExchangeState state = state(
                participant(FIRST_PLAYER_ID, "offer-first", 2, "minecraft:stone", 3, null),
                participant(SECOND_PLAYER_ID, "offer-second", 6, "minecraft:dirt", 2, null));
        CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger ledger =
                confirmation.confirm(state, state.first().actorRef(), CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger.empty());
        ledger = confirmation.confirm(state, state.second().actorRef(), ledger);

        CofferMinecraftSelectedExchangeSubmissionChain.SubmissionResult.Submitted submitted =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeSubmissionChain.SubmissionResult.Submitted.class,
                        chain.submit(tempDir, state, ledger));

        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.CoreDenied denied =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.CoreDenied.class,
                        submitted.participation());
        assertEquals(Decision.DENIED, denied.arbitration().outcome().decision());
        assertEquals(0, runtimeCalls.get());

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));
        assertEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"selected-submission-1\",\"recordType\":\"CER\",\"stage\":\"fabric_core_denied\",\"seam\":\"fabric_core\"}"),
                lines);
        assertFalse(lines.get(0).contains("fabric_runtime"));
    }

    private static CofferMinecraftSelectedExchangeRuntimeParticipation deniedParticipation(AtomicInteger runtimeCalls) {
        return new CofferMinecraftSelectedExchangeRuntimeParticipation(
                payload -> CofferCore.arbitrate(
                        payload,
                        unresolved -> new org.coffer.core.authority.ResolutionResult.Unresolved(),
                        new OutcomeId("selected-submission-denied-outcome-1"),
                        new MutationPlanId("selected-submission-denied-mutation-plan-1"),
                        denialReasonIds()),
                mutationPlan -> {
                    runtimeCalls.incrementAndGet();
                    return runtimeUnknown(mutationPlan, "UNEXPECTED_RUNTIME");
                });
    }

    private static CofferMinecraftLifecycleAccountability accountability() {
        AtomicInteger counter = new AtomicInteger();
        return new CofferMinecraftLifecycleAccountability(
                () -> "selected-submission-" + counter.incrementAndGet(),
                () -> TIMESTAMP);
    }

    private static ArbitrationResult approvedArbitration(ExchangePayload payload) {
        OutcomeId outcomeId = new OutcomeId("selected-submission-approved-outcome-1");
        MutationPlanId mutationPlanId = new MutationPlanId("selected-submission-approved-mutation-plan-1");
        return new ArbitrationResult(
                new Outcome(
                        outcomeId,
                        payload.payloadId(),
                        Decision.APPROVED,
                        List.of(),
                        mutationPlanId),
                new MutationPlan(
                        mutationPlanId,
                        payload.payloadId(),
                        outcomeId,
                        List.of(new AuthorizedMutation(
                                new MutationRef("selected-submission-mutation-1"),
                                new MutationRequirementRef("selected-submission-mutation-requirement-1"),
                                org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority.AUTHORITY_ID,
                                new OpaqueObject(Map.of("selected", true)),
                                new ReferenceSet(java.util.Set.of(), java.util.Set.of())))));
    }

    private static ExecutionResult runtimeUnknown(MutationPlan mutationPlan, String reasonCode) {
        AuthorizedMutation mutation = mutationPlan.mutations().get(0);
        return new ExecutionResult(
                new ExecutionResultId("selected-submission-execution-result-1"),
                new ExecutionPlanId("selected-submission-execution-plan-1"),
                mutationPlan.mutationPlanId(),
                ExecutionStatus.FULL_UNKNOWN,
                List.of(new MutationExecutionResult(
                        mutation.mutationRef(),
                        mutation.satisfies(),
                        mutation.authority(),
                        MutationExecutionStatus.MUTATION_UNKNOWN,
                        new OpaqueObject(Map.of("reasonCode", reasonCode)))));
    }

    private static CofferMinecraftSelectedExchangeConfirmation.ExchangeState state(
            CofferMinecraftSelectedExchangeRequestAssembly.SelectedParticipant first,
            CofferMinecraftSelectedExchangeRequestAssembly.SelectedParticipant second) {
        return new CofferMinecraftSelectedExchangeConfirmation.ExchangeState(first, second, "minecraft-inventory");
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
                                dev.coffer.minecraft.bindings.inventory.MinecraftPlayerInventoryContainer.Region.HOTBAR,
                                slotIndex),
                        Optional.of(new dev.coffer.minecraft.bindings.inventory.MinecraftItemDescriptor(
                                itemId,
                                quantity,
                                Optional.ofNullable(nbtPayload)))));
    }

    private static List<ReasonId> denialReasonIds() {
        java.util.ArrayList<ReasonId> reasonIds = new java.util.ArrayList<>();
        for (int index = 0; index < 8; index++) {
            reasonIds.add(new ReasonId("selected-submission-reason-" + index));
        }
        return List.copyOf(reasonIds);
    }
}
