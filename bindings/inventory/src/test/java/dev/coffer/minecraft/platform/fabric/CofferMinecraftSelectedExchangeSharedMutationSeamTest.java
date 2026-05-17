package dev.coffer.minecraft.platform.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coffer.minecraft.bindings.inventory.MinecraftContainerResolver;
import dev.coffer.minecraft.bindings.inventory.MinecraftDescriptorFactory;
import dev.coffer.minecraft.bindings.inventory.MinecraftPlayerInventoryContainer;
import dev.coffer.minecraft.bindings.inventory.MinecraftRuntimePayloadFactory;
import dev.coffer.minecraft.bindings.inventory.MinecraftRuntimePayloadInterpreter;
import dev.coffer.minecraft.bindings.inventory.MinecraftRuntimeValueSetResolver;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.coffer.core.arbitration.ArbitrationResult;
import org.coffer.core.arbitration.CofferCore;
import org.coffer.core.authority.ResolutionResult;
import org.coffer.core.model.id.ActorRef;
import org.coffer.core.model.id.MutationPlanId;
import org.coffer.core.model.id.OfferRef;
import org.coffer.core.model.id.OutcomeId;
import org.coffer.core.model.id.ReasonId;
import org.coffer.core.model.outcome.Decision;
import org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority;
import org.coffer.firstparty.authority.transferablevalue.runtime.TransferableValueRuntimeAuthority;
import org.coffer.runtime.CofferRuntime;
import org.coffer.runtime.model.execution.ExecutionResult;
import org.coffer.runtime.model.execution.ExecutionStatus;
import org.coffer.runtime.model.execution.MutationExecutionStatus;
import org.coffer.runtime.model.id.ExecutionPlanId;
import org.coffer.runtime.model.id.ExecutionResultId;
import org.coffer.runtime.model.id.ExecutionStepId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CofferMinecraftSelectedExchangeSharedMutationSeamTest {
    private static final long TIMESTAMP = 1_700_000_000_000L;
    private static final UUID FIRST_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000991");
    private static final UUID SECOND_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000992");

    @TempDir
    Path tempDir;

    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void coreDenialLeavesInventoryUntouchedAndDoesNotMutateBeforeApproval() throws IOException {
        List<ItemStack> firstHotbar = mutableSlots(new ItemStack(Items.STONE, 4), ItemStack.EMPTY);
        List<ItemStack> secondHotbar = mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY);
        AtomicBoolean runtimeEntered = new AtomicBoolean(false);
        Harness harness = harness(firstHotbar, secondHotbar, slots(firstHotbar, secondHotbar));
        CofferMinecraftSelectedExchangeSubmissionChain chain = chain(
                harness,
                accountability(),
                tempDir,
                mutationPlan -> {
                    runtimeEntered.set(true);
                    return execute(harness, mutationPlan);
                });
        CofferMinecraftSelectedExchangeConfirmation.ExchangeState state = state(5, 7);

        CofferMinecraftSelectedExchangeSubmissionChain.SubmissionResult.Submitted submitted =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeSubmissionChain.SubmissionResult.Submitted.class,
                        chain.submit(tempDir, state, confirmedLedger(state)));

        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.CoreDenied denied =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.CoreDenied.class,
                        submitted.participation());

        assertEquals(Decision.DENIED, denied.arbitration().outcome().decision());
        assertNull(denied.arbitration().mutationPlan());
        assertFalse(runtimeEntered.get());
        assertEquals(Items.STONE, firstHotbar.get(0).getItem());
        assertEquals(4, firstHotbar.get(0).getCount());
        assertEquals(Items.DIRT, secondHotbar.get(0).getItem());
        assertEquals(7, secondHotbar.get(0).getCount());

        List<String> lines = Files.readAllLines(accountability().logPath(tempDir));
        assertEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"selected-shared-seam-1\",\"recordType\":\"CER\",\"stage\":\"fabric_core_denied\",\"seam\":\"fabric_core\",\"code\":\"minecraft.value.not_removable\"}"),
                lines);
    }

    @Test
    void coreApprovalCanEnterRealRuntimeExecutionAndMutateInventoriesTruthfully() throws IOException {
        List<ItemStack> firstHotbar = mutableSlots(new ItemStack(Items.STONE, 5), ItemStack.EMPTY);
        List<ItemStack> secondHotbar = mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY);
        Harness harness = harness(firstHotbar, secondHotbar, slots(firstHotbar, secondHotbar));
        CofferMinecraftLifecycleAccountability accountability = accountability();
        CofferMinecraftSelectedExchangeSubmissionChain chain = chain(harness, accountability, tempDir, mutationPlan -> execute(harness, mutationPlan));
        CofferMinecraftSelectedExchangeConfirmation.ExchangeState state = state(5, 7);

        CofferMinecraftSelectedExchangeSubmissionChain.SubmissionResult.Submitted submitted =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeSubmissionChain.SubmissionResult.Submitted.class,
                        chain.submit(tempDir, state, confirmedLedger(state)));

        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated runtime =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated.class,
                        submitted.participation());

        assertEquals(Decision.APPROVED, runtime.arbitration().outcome().decision());
        assertNotNull(runtime.arbitration().mutationPlan());
        assertEquals(ExecutionStatus.FULL_SUCCESS, runtime.execution().status());
        assertEquals(MutationExecutionStatus.MUTATION_SUCCEEDED, runtime.execution().mutationResults().get(0).status());
        assertEquals(Items.DIRT, firstHotbar.get(0).getItem());
        assertEquals(7, firstHotbar.get(0).getCount());
        assertEquals(Items.STONE, secondHotbar.get(0).getItem());
        assertEquals(5, secondHotbar.get(0).getCount());

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));
        assertEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"selected-shared-seam-1\",\"recordType\":\"CER\",\"stage\":\"fabric_core_approved\",\"seam\":\"fabric_core\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"selected-shared-seam-2\",\"recordType\":\"CER\",\"stage\":\"fabric_runtime_succeeded\",\"seam\":\"fabric_runtime\"}"),
                lines);
    }

    @Test
    void failureRemainsDistinctAndHonestWithMatchingFabricAccountability() throws IOException {
        List<ItemStack> firstHotbar = mutableSlots(new ItemStack(Items.STONE, 5), ItemStack.EMPTY);
        List<ItemStack> secondHotbar = mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY);
        Harness harness = harness(firstHotbar, secondHotbar, slots(firstHotbar, secondHotbar));
        CofferMinecraftLifecycleAccountability accountability = accountability();
        CofferMinecraftSelectedExchangeSubmissionChain chain = chain(
                harness,
                accountability,
                tempDir,
                mutationPlan -> {
                    firstHotbar.get(0).setCount(4);
                    return execute(harness, mutationPlan);
                });
        CofferMinecraftSelectedExchangeConfirmation.ExchangeState state = state(5, 7);

        CofferMinecraftSelectedExchangeSubmissionChain.SubmissionResult.Submitted submitted =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeSubmissionChain.SubmissionResult.Submitted.class,
                        chain.submit(tempDir, state, confirmedLedger(state)));

        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated runtime =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated.class,
                        submitted.participation());

        assertEquals(Decision.APPROVED, runtime.arbitration().outcome().decision());
        assertNotNull(runtime.arbitration().mutationPlan());
        assertEquals(ExecutionStatus.FULL_FAILURE, runtime.execution().status());
        assertEquals(MutationExecutionStatus.MUTATION_FAILED, runtime.execution().mutationResults().get(0).status());
        assertEquals(
                MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE,
                runtime.execution().mutationResults().get(0).detail().values().get("reasonCode"));
        assertEquals(Items.STONE, firstHotbar.get(0).getItem());
        assertEquals(4, firstHotbar.get(0).getCount());
        assertEquals(Items.DIRT, secondHotbar.get(0).getItem());
        assertEquals(7, secondHotbar.get(0).getCount());

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));
        assertEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"selected-shared-seam-1\",\"recordType\":\"CER\",\"stage\":\"fabric_core_approved\",\"seam\":\"fabric_core\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"selected-shared-seam-2\",\"recordType\":\"CER\",\"stage\":\"fabric_runtime_failed\",\"seam\":\"fabric_runtime\",\"code\":\"minecraft.value.not_removable\"}"),
                lines);
        assertTrue(lines.stream().noneMatch(line -> line.contains("fabric_runtime_succeeded")));
    }

    @Test
    void unknownRemainsDistinctAndHonestWithMatchingFabricAccountability() throws IOException {
        List<ItemStack> firstHotbar = mutableSlots(new ItemStack(Items.STONE, 5), ItemStack.EMPTY);
        List<ItemStack> secondHotbar = mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY);
        AtomicBoolean runtimePhase = new AtomicBoolean(false);
        Harness harness = harness(
                firstHotbar,
                secondHotbar,
                (playerId, region) -> {
                    if (region != MinecraftPlayerInventoryContainer.Region.HOTBAR) {
                        return Optional.empty();
                    }
                    if (FIRST_PLAYER_ID.equals(playerId)) {
                        return Optional.of(firstHotbar);
                    }
                    if (SECOND_PLAYER_ID.equals(playerId)) {
                        return runtimePhase.get() ? Optional.empty() : Optional.of(secondHotbar);
                    }
                    return Optional.empty();
                });
        CofferMinecraftLifecycleAccountability accountability = accountability();
        CofferMinecraftSelectedExchangeSubmissionChain chain = chain(
                harness,
                accountability,
                tempDir,
                mutationPlan -> {
                    runtimePhase.set(true);
                    return execute(harness, mutationPlan);
                });
        CofferMinecraftSelectedExchangeConfirmation.ExchangeState state = state(5, 7);

        CofferMinecraftSelectedExchangeSubmissionChain.SubmissionResult.Submitted submitted =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeSubmissionChain.SubmissionResult.Submitted.class,
                        chain.submit(tempDir, state, confirmedLedger(state)));

        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated runtime =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated.class,
                        submitted.participation());

        assertEquals(Decision.APPROVED, runtime.arbitration().outcome().decision());
        assertNotNull(runtime.arbitration().mutationPlan());
        assertEquals(ExecutionStatus.FULL_UNKNOWN, runtime.execution().status());
        assertEquals(MutationExecutionStatus.MUTATION_UNKNOWN, runtime.execution().mutationResults().get(0).status());
        assertEquals(
                MinecraftPlayerInventoryContainer.CONTAINER_UNAVAILABLE,
                runtime.execution().mutationResults().get(0).detail().values().get("reasonCode"));
        assertEquals(Items.STONE, firstHotbar.get(0).getItem());
        assertEquals(5, firstHotbar.get(0).getCount());
        assertEquals(Items.DIRT, secondHotbar.get(0).getItem());
        assertEquals(7, secondHotbar.get(0).getCount());

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));
        assertEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"selected-shared-seam-1\",\"recordType\":\"CER\",\"stage\":\"fabric_core_approved\",\"seam\":\"fabric_core\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"selected-shared-seam-2\",\"recordType\":\"CER\",\"stage\":\"fabric_runtime_unknown\",\"seam\":\"fabric_runtime\",\"code\":\"minecraft.container.unavailable\"}"),
                lines);
        assertTrue(lines.stream().noneMatch(line -> line.contains("fabric_runtime_succeeded")));
    }

    @Test
    void postApprovalCapacityDriftStillCompletesConsistentlyForSingleSelectedValues() throws IOException {
        List<ItemStack> firstHotbar = mutableSlots(new ItemStack(Items.STONE, 64), ItemStack.EMPTY);
        List<ItemStack> secondSimulationHotbar = mutableSlots(new ItemStack(Items.DIRT, 64), ItemStack.EMPTY);
        List<ItemStack> secondMutationHotbar = mutableSlots(new ItemStack(Items.DIRT, 64));
        AtomicBoolean runtimePhase = new AtomicBoolean(false);
        AtomicInteger runtimeResolutions = new AtomicInteger();
        Harness harness = harness(
                firstHotbar,
                secondMutationHotbar,
                (playerId, region) -> {
                    if (region != MinecraftPlayerInventoryContainer.Region.HOTBAR) {
                        return Optional.empty();
                    }
                    if (FIRST_PLAYER_ID.equals(playerId)) {
                        return Optional.of(firstHotbar);
                    }
                    if (SECOND_PLAYER_ID.equals(playerId)) {
                        if (!runtimePhase.get()) {
                            return Optional.of(secondSimulationHotbar);
                        }
                        return Optional.of(runtimeResolutions.getAndIncrement() == 0
                                ? secondSimulationHotbar
                                : secondMutationHotbar);
                    }
                    return Optional.empty();
                });
        CofferMinecraftLifecycleAccountability accountability = accountability();
        CofferMinecraftSelectedExchangeSubmissionChain chain = chain(
                harness,
                accountability,
                tempDir,
                mutationPlan -> {
                    runtimePhase.set(true);
                    return execute(harness, mutationPlan);
                });
        CofferMinecraftSelectedExchangeConfirmation.ExchangeState state = state(64, 64);

        CofferMinecraftSelectedExchangeSubmissionChain.SubmissionResult.Submitted submitted =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeSubmissionChain.SubmissionResult.Submitted.class,
                        chain.submit(tempDir, state, confirmedLedger(state)));

        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated runtime =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated.class,
                        submitted.participation());

        assertEquals(Decision.APPROVED, runtime.arbitration().outcome().decision());
        assertNotNull(runtime.arbitration().mutationPlan());
        assertEquals(ExecutionStatus.FULL_SUCCESS, runtime.execution().status());
        assertEquals(MutationExecutionStatus.MUTATION_SUCCEEDED, runtime.execution().mutationResults().get(0).status());
        assertEquals(Items.DIRT, firstHotbar.get(0).getItem());
        assertEquals(64, firstHotbar.get(0).getCount());
        assertTrue(firstHotbar.get(1).isEmpty());
        assertEquals(Items.STONE, secondMutationHotbar.get(0).getItem());
        assertEquals(64, secondMutationHotbar.get(0).getCount());

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));
        assertEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"selected-shared-seam-1\",\"recordType\":\"CER\",\"stage\":\"fabric_core_approved\",\"seam\":\"fabric_core\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"selected-shared-seam-2\",\"recordType\":\"CER\",\"stage\":\"fabric_runtime_succeeded\",\"seam\":\"fabric_runtime\"}"),
                lines);
    }


    private static CofferMinecraftSelectedExchangeSubmissionChain chain(
            Harness harness,
            CofferMinecraftLifecycleAccountability accountability,
            Path runDirectory,
            CofferMinecraftSelectedExchangeRuntimeParticipation.RuntimeGateway runtimeGateway) {
        return new CofferMinecraftSelectedExchangeSubmissionChain(
                new CofferMinecraftSelectedExchangeConfirmation(),
                CofferMinecraftSelectedExchangeRequestAssembly.create(),
                new CofferMinecraftSelectedExchangeRuntimeParticipation(
                        payload -> CofferCore.arbitrate(
                                payload,
                                ignored -> new ResolutionResult.Resolved(harness.coreAuthority()),
                                new OutcomeId("selected-shared-seam-outcome-1"),
                                new MutationPlanId("selected-shared-seam-plan-1"),
                                denialReasonIds()),
                        runtimeGateway),
                new CofferMinecraftSelectedExchangeAccountabilityProjection(accountability));
    }

    private static ExecutionResult execute(Harness harness, org.coffer.core.model.mutation.MutationPlan mutationPlan) {
        return new CofferRuntime().execute(
                new ExecutionPlanId("selected-shared-seam-execution-plan-1"),
                new ExecutionResultId("selected-shared-seam-execution-result-1"),
                mutationPlan,
                List.of(new ExecutionStepId("selected-shared-seam-step-1")),
                List.of(harness.runtimeAuthority()));
    }

    private static Harness harness(
            List<ItemStack> firstHotbar,
            List<ItemStack> secondHotbar,
            MinecraftContainerResolver.PlayerInventorySlots slots) {
        MinecraftContainerResolver resolver = new MinecraftContainerResolver(slots);
        TransferableValueCoreAuthority coreAuthority = new TransferableValueCoreAuthority(
                resolver,
                new MinecraftDescriptorFactory(),
                new MinecraftRuntimePayloadFactory());
        TransferableValueRuntimeAuthority runtimeAuthority = new TransferableValueRuntimeAuthority(
                resolver,
                new MinecraftRuntimeValueSetResolver(),
                new MinecraftRuntimePayloadInterpreter(),
                reasonCode -> new org.coffer.core.model.support.OpaqueObject(Map.of("reasonCode", reasonCode)));
        return new Harness(coreAuthority, runtimeAuthority);
    }

    private static MinecraftContainerResolver.PlayerInventorySlots slots(
            List<ItemStack> firstHotbar,
            List<ItemStack> secondHotbar) {
        return (playerId, region) -> {
            if (region != MinecraftPlayerInventoryContainer.Region.HOTBAR) {
                return Optional.empty();
            }
            if (FIRST_PLAYER_ID.equals(playerId)) {
                return Optional.of(firstHotbar);
            }
            if (SECOND_PLAYER_ID.equals(playerId)) {
                return Optional.of(secondHotbar);
            }
            return Optional.empty();
        };
    }

    private static CofferMinecraftLifecycleAccountability accountability() {
        AtomicInteger counter = new AtomicInteger();
        return new CofferMinecraftLifecycleAccountability(
                () -> "selected-shared-seam-" + counter.incrementAndGet(),
                () -> TIMESTAMP);
    }

    private static CofferMinecraftSelectedExchangeConfirmation.ExchangeState state(long firstQuantity, long secondQuantity) {
        return new CofferMinecraftSelectedExchangeConfirmation.ExchangeState(
                participant(FIRST_PLAYER_ID, "offer-first", 0, "minecraft:stone", firstQuantity),
                participant(SECOND_PLAYER_ID, "offer-second", 0, "minecraft:dirt", secondQuantity),
                "minecraft-inventory");
    }


    private static CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger confirmedLedger(
            CofferMinecraftSelectedExchangeConfirmation.ExchangeState state) {
        CofferMinecraftSelectedExchangeConfirmation confirmation = new CofferMinecraftSelectedExchangeConfirmation();
        CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger ledger =
                confirmation.confirm(state, state.first().actorRef(), CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger.empty());
        return confirmation.confirm(state, state.second().actorRef(), ledger);
    }

    private static CofferMinecraftSelectedExchangeRequestAssembly.SelectedParticipant participant(
            UUID playerId,
            String offerRef,
            int slotIndex,
            String itemId,
            long quantity) {
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
                                Optional.empty()))));
    }

    private static List<ItemStack> mutableSlots(ItemStack... slots) {
        return new ArrayList<>(List.of(slots));
    }

    private static List<ReasonId> denialReasonIds() {
        List<ReasonId> reasonIds = new ArrayList<>();
        for (int index = 0; index < 8; index++) {
            reasonIds.add(new ReasonId("selected-shared-seam-reason-" + index));
        }
        return List.copyOf(reasonIds);
    }

    private record Harness(
            TransferableValueCoreAuthority coreAuthority,
            TransferableValueRuntimeAuthority runtimeAuthority) {
    }
}
