package dev.coffer.minecraft.platform.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.coffer.core.arbitration.CofferCore;
import org.coffer.core.arbitration.ArbitrationResult;
import org.coffer.core.model.id.ActorRef;
import org.coffer.core.model.id.MutationPlanId;
import org.coffer.core.model.id.MutationRef;
import org.coffer.core.model.id.MutationRequirementRef;
import org.coffer.core.model.id.OfferRef;
import org.coffer.core.model.id.OutcomeId;
import org.coffer.core.model.id.PayloadId;
import org.coffer.core.model.id.ReasonId;
import org.coffer.core.model.id.TruthRef;
import org.coffer.core.model.id.ValueRef;
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

class CofferMinecraftSelectedExchangeRuntimeParticipationTest {
    private static final UUID FIRST_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000941");
    private static final UUID SECOND_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000942");

    @Test
    void deniedCoreOutcomeDoesNotEnterRuntime() {
        AtomicInteger runtimeCalls = new AtomicInteger();
        ExchangePayload payload = assembledPayload("minecraft:stone", 3, null, "minecraft:dirt", 2, null);
        CofferMinecraftSelectedExchangeRuntimeParticipation participation =
                new CofferMinecraftSelectedExchangeRuntimeParticipation(
                        ignored -> CofferCore.arbitrate(
                                payload,
                                unresolved -> new org.coffer.core.authority.ResolutionResult.Unresolved(),
                                new OutcomeId("selected-runtime-denied-outcome-1"),
                                new MutationPlanId("selected-runtime-denied-mutation-plan-1"),
                                denialReasonIds()),
                        mutationPlan -> {
                            runtimeCalls.incrementAndGet();
                            return executionResult(mutationPlan, ExecutionStatus.FULL_SUCCESS, MutationExecutionStatus.MUTATION_SUCCEEDED);
                        });

        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.CoreDenied denied =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.CoreDenied.class,
                        participation.participate(payload));

        assertEquals(Decision.DENIED, denied.arbitration().outcome().decision());
        assertEquals(0, runtimeCalls.get());
    }

    @Test
    void approvedMutationPlanIsPassedToRuntimeWithoutAdapterModification() {
        ExchangePayload payload = assembledPayload("minecraft:stone", 3, null, "minecraft:dirt", 2, null);
        ArbitrationResult approved = approvedArbitration(payload);
        AtomicReference<MutationPlan> seenPlan = new AtomicReference<>();
        CofferMinecraftSelectedExchangeRuntimeParticipation participation =
                new CofferMinecraftSelectedExchangeRuntimeParticipation(
                        ignored -> approved,
                        mutationPlan -> {
                            seenPlan.set(mutationPlan);
                            return executionResult(mutationPlan, ExecutionStatus.FULL_SUCCESS, MutationExecutionStatus.MUTATION_SUCCEEDED);
                        });

        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated runtime =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated.class,
                        participation.participate(payload));

        assertSame(approved.mutationPlan(), seenPlan.get());
        assertSame(approved.mutationPlan(), runtime.arbitration().mutationPlan());
        assertEquals(ExecutionStatus.FULL_SUCCESS, runtime.execution().status());
    }

    @Test
    void runtimeParticipationPreservesSuccessFailureAndUnknownHonestly() {
        ExchangePayload payload = assembledPayload("minecraft:stone", 3, null, "minecraft:dirt", 2, null);
        ArbitrationResult approved = approvedArbitration(payload);

        CofferMinecraftSelectedExchangeRuntimeParticipation successParticipation =
                new CofferMinecraftSelectedExchangeRuntimeParticipation(
                        ignored -> approved,
                        mutationPlan -> executionResult(mutationPlan, ExecutionStatus.FULL_SUCCESS, MutationExecutionStatus.MUTATION_SUCCEEDED));
        CofferMinecraftSelectedExchangeRuntimeParticipation failureParticipation =
                new CofferMinecraftSelectedExchangeRuntimeParticipation(
                        ignored -> approved,
                        mutationPlan -> executionResult(mutationPlan, ExecutionStatus.FULL_FAILURE, MutationExecutionStatus.MUTATION_FAILED));
        CofferMinecraftSelectedExchangeRuntimeParticipation unknownParticipation =
                new CofferMinecraftSelectedExchangeRuntimeParticipation(
                        ignored -> approved,
                        mutationPlan -> executionResult(mutationPlan, ExecutionStatus.FULL_UNKNOWN, MutationExecutionStatus.MUTATION_UNKNOWN));

        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated success =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated.class,
                        successParticipation.participate(payload));
        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated failure =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated.class,
                        failureParticipation.participate(payload));
        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated unknown =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated.class,
                        unknownParticipation.participate(payload));

        assertEquals(ExecutionStatus.FULL_SUCCESS, success.execution().status());
        assertEquals(MutationExecutionStatus.MUTATION_SUCCEEDED, success.execution().mutationResults().get(0).status());
        assertEquals(ExecutionStatus.FULL_FAILURE, failure.execution().status());
        assertEquals(MutationExecutionStatus.MUTATION_FAILED, failure.execution().mutationResults().get(0).status());
        assertEquals(ExecutionStatus.FULL_UNKNOWN, unknown.execution().status());
        assertEquals(MutationExecutionStatus.MUTATION_UNKNOWN, unknown.execution().mutationResults().get(0).status());
    }

    @Test
    void runtimeParticipationSurfaceDoesNotExposeConfirmationReceiptOrGameplay() {
        List<String> methodNames =
                Arrays.stream(CofferMinecraftSelectedExchangeRuntimeParticipation.class.getDeclaredMethods())
                        .map(Method::getName)
                        .map(String::toLowerCase)
                        .toList();

        assertTrue(methodNames.contains("participate"));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("confirm")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("receipt")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("player")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("gameplay")));
    }

    private static ExecutionResult executionResult(
            MutationPlan mutationPlan,
            ExecutionStatus executionStatus,
            MutationExecutionStatus mutationStatus) {
        AuthorizedMutation mutation = mutationPlan.mutations().get(0);
        return new ExecutionResult(
                new ExecutionResultId("selected-runtime-execution-result-1"),
                new ExecutionPlanId("selected-runtime-execution-plan-1"),
                mutationPlan.mutationPlanId(),
                executionStatus,
                List.of(new MutationExecutionResult(
                        mutation.mutationRef(),
                        mutation.satisfies(),
                        mutation.authority(),
                        mutationStatus,
                        new OpaqueObject(Map.of("reasonCode", mutationStatus.name())))));
    }

    private static ArbitrationResult approvedArbitration(ExchangePayload payload) {
        OutcomeId outcomeId = new OutcomeId("selected-runtime-approved-outcome-1");
        MutationPlanId mutationPlanId = new MutationPlanId("selected-runtime-approved-mutation-plan-1");
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
                                new MutationRef("selected-runtime-mutation-1"),
                                new MutationRequirementRef("selected-runtime-mutation-requirement-1"),
                                org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority.AUTHORITY_ID,
                                new OpaqueObject(Map.of("selected", true)),
                                new ReferenceSet(java.util.Set.of(), java.util.Set.of())))));
    }

    private static ExchangePayload assembledPayload(
            String firstItemId,
            int firstQuantity,
            String firstNbt,
            String secondItemId,
            int secondQuantity,
            String secondNbt) {
        CofferMinecraftSelectedExchangeRequestAssembly assembly = CofferMinecraftSelectedExchangeRequestAssembly.create();
        CofferMinecraftSelectedExchangeRequestAssembly.AssemblyResult.Prepared prepared =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRequestAssembly.AssemblyResult.Prepared.class,
                        assembly.assemble(
                                participant(FIRST_PLAYER_ID, "offer-first", 2, firstItemId, firstQuantity, firstNbt),
                                participant(SECOND_PLAYER_ID, "offer-second", 6, secondItemId, secondQuantity, secondNbt),
                                "minecraft-inventory"));
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
                                dev.coffer.minecraft.bindings.inventory.MinecraftPlayerInventoryContainer.Region.HOTBAR,
                                slotIndex),
                        Optional.of(new dev.coffer.minecraft.bindings.inventory.MinecraftItemDescriptor(
                                itemId,
                                quantity,
                                Optional.ofNullable(nbtPayload)))));
    }

    private static List<org.coffer.core.model.id.ReasonId> denialReasonIds() {
        List<org.coffer.core.model.id.ReasonId> reasonIds = new java.util.ArrayList<>();
        for (int index = 0; index < 8; index++) {
            reasonIds.add(new org.coffer.core.model.id.ReasonId("selected-runtime-participation-reason-" + index));
        }
        return List.copyOf(reasonIds);
    }
}
