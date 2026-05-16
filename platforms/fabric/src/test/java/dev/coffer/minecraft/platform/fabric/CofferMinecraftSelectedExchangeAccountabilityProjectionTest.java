package dev.coffer.minecraft.platform.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coffer.minecraft.bindings.inventory.MinecraftPlayerInventoryContainer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.coffer.core.arbitration.ArbitrationResult;
import org.coffer.core.arbitration.CofferCore;
import org.coffer.core.authority.ResolutionResult;
import org.coffer.core.model.id.ActorRef;
import org.coffer.core.model.id.MutationPlanId;
import org.coffer.core.model.id.MutationRef;
import org.coffer.core.model.id.MutationRequirementRef;
import org.coffer.core.model.id.OfferRef;
import org.coffer.core.model.id.OutcomeId;
import org.coffer.core.model.id.ReasonId;
import org.coffer.core.model.outcome.Decision;
import org.coffer.core.model.outcome.Outcome;
import org.coffer.core.model.request.ExchangePayload;
import org.coffer.core.model.support.OpaqueObject;
import org.coffer.core.model.support.ReferenceSet;
import org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority;
import org.coffer.runtime.model.execution.ExecutionResult;
import org.coffer.runtime.model.execution.ExecutionStatus;
import org.coffer.runtime.model.execution.MutationExecutionResult;
import org.coffer.runtime.model.execution.MutationExecutionStatus;
import org.coffer.runtime.model.id.ExecutionPlanId;
import org.coffer.runtime.model.id.ExecutionResultId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CofferMinecraftSelectedExchangeAccountabilityProjectionTest {
    private static final long TIMESTAMP = 1_700_000_000_000L;
    private static final UUID FIRST_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000951");
    private static final UUID SECOND_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000952");

    @TempDir
    Path tempDir;

    @Test
    void emptySelectedCaptureProjectsSerConstructionRefusalBeforeCoreContact() throws IOException {
        CofferMinecraftLifecycleAccountability accountability = accountability();
        CofferMinecraftSelectedExchangeAccountabilityProjection projection =
                new CofferMinecraftSelectedExchangeAccountabilityProjection(accountability);
        CofferMinecraftSelectedExchangeRequestAssembly assembly = CofferMinecraftSelectedExchangeRequestAssembly.create();

        CofferMinecraftSelectedExchangeRequestAssembly.AssemblyResult.Refused refused =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRequestAssembly.AssemblyResult.Refused.class,
                        assembly.assemble(
                                participant(FIRST_PLAYER_ID, "offer-first", 2),
                                participant(SECOND_PLAYER_ID, "offer-second", 6, "minecraft:dirt", 2, null),
                                "minecraft-inventory"));

        projection.recordAssembly(tempDir, refused);

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));
        assertEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"selected-accountability-1\",\"recordType\":\"SER\",\"stage\":\"fabric_construction_refused\",\"code\":\"FIRST_SELECTED_VALUE_NOT_MATERIALIZED\"}"),
                lines);
        assertFalse(lines.get(0).contains("\"seam\""));
        assertFalse(lines.get(0).contains("fabric_core"));
        assertFalse(lines.get(0).contains("fabric_runtime"));
    }

    @Test
    void coreDenialProjectsOnlyCoreCerAndNoRuntimeRecord() throws IOException {
        CofferMinecraftLifecycleAccountability accountability = accountability();
        CofferMinecraftSelectedExchangeAccountabilityProjection projection =
                new CofferMinecraftSelectedExchangeAccountabilityProjection(accountability);
        ExchangePayload payload = assembledPayload("minecraft:stone", 3, null, "minecraft:dirt", 2, null);
        CofferMinecraftSelectedExchangeRuntimeParticipation participation =
                new CofferMinecraftSelectedExchangeRuntimeParticipation(
                        ignored -> CofferCore.arbitrate(
                                payload,
                                unresolved -> new ResolutionResult.Unresolved(),
                                new OutcomeId("selected-accountability-denied-outcome-1"),
                                new MutationPlanId("selected-accountability-denied-mutation-plan-1"),
                                denialReasonIds()),
                        mutationPlan -> executionResult(
                                mutationPlan,
                                ExecutionStatus.FULL_SUCCESS,
                                MutationExecutionStatus.MUTATION_SUCCEEDED,
                                null));

        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult result =
                participation.participate(payload);

        projection.recordParticipation(tempDir, result);

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));
        assertEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"selected-accountability-1\",\"recordType\":\"CER\",\"stage\":\"fabric_core_denied\",\"seam\":\"fabric_core\"}"),
                lines);
        assertFalse(lines.get(0).contains("fabric_runtime"));
        assertFalse(lines.get(0).contains("fabric_runtime_succeeded"));
    }

    @Test
    void runtimeSuccessProjectsCoreApprovalThenRuntimeSuccessWithoutCounterfeitExecutionBeforeRuntime() throws IOException {
        CofferMinecraftLifecycleAccountability accountability = accountability();
        CofferMinecraftSelectedExchangeAccountabilityProjection projection =
                new CofferMinecraftSelectedExchangeAccountabilityProjection(accountability);
        ExchangePayload payload = assembledPayload(
                "minecraft:iron_sword",
                1,
                "{Enchantments:[{id:\"minecraft:sharpness\",lvl:3s}],Damage:5}",
                "minecraft:shield",
                1,
                "{BlockEntityTag:{Base:11}}");
        ArbitrationResult approved = approvedArbitration(payload);
        CofferMinecraftSelectedExchangeRuntimeParticipation participation =
                new CofferMinecraftSelectedExchangeRuntimeParticipation(
                        ignored -> approved,
                        mutationPlan -> executionResult(
                                mutationPlan,
                                ExecutionStatus.FULL_SUCCESS,
                                MutationExecutionStatus.MUTATION_SUCCEEDED,
                                null));

        projection.recordParticipation(
                tempDir,
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated.class,
                        participation.participate(payload)));

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));
        assertEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"selected-accountability-1\",\"recordType\":\"CER\",\"stage\":\"fabric_core_approved\",\"seam\":\"fabric_core\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"selected-accountability-2\",\"recordType\":\"CER\",\"stage\":\"fabric_runtime_succeeded\",\"seam\":\"fabric_runtime\"}"),
                lines);
        assertTrue(lines.get(0).indexOf("\"stage\"") < lines.get(0).indexOf("\"seam\""));
        assertTrue(lines.get(1).contains("fabric_runtime_succeeded"));
        assertFalse(lines.get(0).contains("fabric_runtime_succeeded"));
        assertFalse(lines.get(1).contains("\"code\""));
    }

    @Test
    void runtimeFailureProjectsFailureWithoutCounterfeitDenialOrSuccess() throws IOException {
        CofferMinecraftLifecycleAccountability accountability = accountability();
        CofferMinecraftSelectedExchangeAccountabilityProjection projection =
                new CofferMinecraftSelectedExchangeAccountabilityProjection(accountability);
        ExchangePayload payload = assembledPayload("minecraft:stone", 3, null, "minecraft:dirt", 2, null);
        ArbitrationResult approved = approvedArbitration(payload);
        CofferMinecraftSelectedExchangeRuntimeParticipation participation =
                new CofferMinecraftSelectedExchangeRuntimeParticipation(
                        ignored -> approved,
                        mutationPlan -> executionResult(
                                mutationPlan,
                                ExecutionStatus.FULL_FAILURE,
                                MutationExecutionStatus.MUTATION_FAILED,
                                MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE));

        projection.recordParticipation(
                tempDir,
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated.class,
                        participation.participate(payload)));

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));
        assertEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"selected-accountability-1\",\"recordType\":\"CER\",\"stage\":\"fabric_core_approved\",\"seam\":\"fabric_core\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"selected-accountability-2\",\"recordType\":\"CER\",\"stage\":\"fabric_runtime_failed\",\"seam\":\"fabric_runtime\",\"code\":\"minecraft.value.not_removable\"}"),
                lines);
        assertFalse(lines.get(1).contains("fabric_core_denied"));
        assertFalse(lines.get(1).contains("fabric_runtime_succeeded"));
    }

    @Test
    void runtimeUnknownProjectsUnknownWithoutExecutionSuccessClaim() throws IOException {
        CofferMinecraftLifecycleAccountability accountability = accountability();
        CofferMinecraftSelectedExchangeAccountabilityProjection projection =
                new CofferMinecraftSelectedExchangeAccountabilityProjection(accountability);
        ExchangePayload payload = assembledPayload("minecraft:stone", 3, null, "minecraft:dirt", 2, null);
        ArbitrationResult approved = approvedArbitration(payload);
        CofferMinecraftSelectedExchangeRuntimeParticipation participation =
                new CofferMinecraftSelectedExchangeRuntimeParticipation(
                        ignored -> approved,
                        mutationPlan -> executionResult(
                                mutationPlan,
                                ExecutionStatus.FULL_UNKNOWN,
                                MutationExecutionStatus.MUTATION_UNKNOWN,
                                "MALFORMED_RUNTIME_DESCRIPTOR"));

        projection.recordParticipation(
                tempDir,
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated.class,
                        participation.participate(payload)));

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));
        assertEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"selected-accountability-1\",\"recordType\":\"CER\",\"stage\":\"fabric_core_approved\",\"seam\":\"fabric_core\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"selected-accountability-2\",\"recordType\":\"CER\",\"stage\":\"fabric_runtime_unknown\",\"seam\":\"fabric_runtime\",\"code\":\"MALFORMED_RUNTIME_DESCRIPTOR\"}"),
                lines);
        assertFalse(lines.get(1).contains("fabric_runtime_succeeded"));
        assertFalse(lines.get(1).contains("fabric_core_denied"));
    }

    private static CofferMinecraftLifecycleAccountability accountability() {
        AtomicInteger counter = new AtomicInteger();
        return new CofferMinecraftLifecycleAccountability(
                () -> "selected-accountability-" + counter.incrementAndGet(),
                () -> TIMESTAMP);
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

    private static ArbitrationResult approvedArbitration(ExchangePayload payload) {
        OutcomeId outcomeId = new OutcomeId("selected-accountability-approved-outcome-1");
        MutationPlanId mutationPlanId = new MutationPlanId("selected-accountability-approved-mutation-plan-1");
        return new ArbitrationResult(
                new Outcome(
                        outcomeId,
                        payload.payloadId(),
                        Decision.APPROVED,
                        List.of(),
                        mutationPlanId),
                new org.coffer.core.model.mutation.MutationPlan(
                        mutationPlanId,
                        payload.payloadId(),
                        outcomeId,
                        List.of(new org.coffer.core.model.mutation.AuthorizedMutation(
                                new MutationRef("selected-accountability-mutation-1"),
                                new MutationRequirementRef("selected-accountability-mutation-requirement-1"),
                                TransferableValueCoreAuthority.AUTHORITY_ID,
                                new OpaqueObject(Map.of("selected", true)),
                                new ReferenceSet(java.util.Set.of(), java.util.Set.of())))));
    }

    private static ExecutionResult executionResult(
            org.coffer.core.model.mutation.MutationPlan mutationPlan,
            ExecutionStatus executionStatus,
            MutationExecutionStatus mutationStatus,
            String reasonCode) {
        org.coffer.core.model.mutation.AuthorizedMutation mutation = mutationPlan.mutations().get(0);
        OpaqueObject detail = reasonCode == null
                ? new OpaqueObject(Map.of())
                : new OpaqueObject(Map.of("reasonCode", reasonCode));
        return new ExecutionResult(
                new ExecutionResultId("selected-accountability-execution-result-1"),
                new ExecutionPlanId("selected-accountability-execution-plan-1"),
                mutationPlan.mutationPlanId(),
                executionStatus,
                List.of(new MutationExecutionResult(
                        mutation.mutationRef(),
                        mutation.satisfies(),
                        mutation.authority(),
                        mutationStatus,
                        detail)));
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
            reasonIds.add(new ReasonId("selected-accountability-reason-" + index));
        }
        return List.copyOf(reasonIds);
    }
}
