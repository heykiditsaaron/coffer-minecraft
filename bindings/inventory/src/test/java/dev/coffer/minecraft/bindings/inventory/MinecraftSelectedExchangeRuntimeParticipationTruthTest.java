package dev.coffer.minecraft.bindings.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.coffer.core.arbitration.ArbitrationResult;
import org.coffer.core.arbitration.CofferCore;
import org.coffer.core.authority.ResolutionResult;
import org.coffer.core.model.id.ActorRef;
import org.coffer.core.model.id.MutationPlanId;
import org.coffer.core.model.id.MutationRequirementRef;
import org.coffer.core.model.id.OfferRef;
import org.coffer.core.model.id.OutcomeId;
import org.coffer.core.model.id.PayloadId;
import org.coffer.core.model.id.ReasonId;
import org.coffer.core.model.id.TruthRef;
import org.coffer.core.model.id.ValueRef;
import org.coffer.core.model.outcome.Decision;
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
import org.coffer.firstparty.authority.transferablevalue.runtime.TransferableValueRuntimeAuthority;
import org.coffer.runtime.CofferRuntime;
import org.coffer.runtime.model.execution.ExecutionResult;
import org.coffer.runtime.model.execution.ExecutionStatus;
import org.coffer.runtime.model.execution.MutationExecutionRequest;
import org.coffer.runtime.model.execution.MutationExecutionResponse;
import org.coffer.runtime.model.execution.MutationExecutionStatus;
import org.coffer.runtime.model.id.ExecutionPlanId;
import org.coffer.runtime.model.id.ExecutionResultId;
import org.coffer.runtime.model.id.ExecutionStepId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MinecraftSelectedExchangeRuntimeParticipationTruthTest {
    private static final UUID FIRST_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000951");
    private static final UUID SECOND_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000952");

    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void approvedSelectedExchangeParticipatesInRuntimeAndReportsSuccess() {
        List<ItemStack> firstHotbar = mutableSlots(new ItemStack(Items.STONE, 5), ItemStack.EMPTY);
        List<ItemStack> secondHotbar = mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY);
        Harness harness = harness(firstHotbar, secondHotbar, slots(firstHotbar, secondHotbar));

        ArbitrationResult arbitration = arbitrate(harness, 5, 7);
        ExecutionResult execution = execute(harness, arbitration);

        assertEquals(Decision.APPROVED, arbitration.outcome().decision());
        assertNotNull(arbitration.mutationPlan());
        assertEquals(ExecutionStatus.FULL_SUCCESS, execution.status());
        assertEquals(MutationExecutionStatus.MUTATION_SUCCEEDED, execution.mutationResults().get(0).status());
        assertEquals(Items.DIRT, firstHotbar.get(0).getItem());
        assertEquals(Items.STONE, secondHotbar.get(0).getItem());
    }

    @Test
    void deniedSelectedExchangeDoesNotEnterRuntime() {
        List<ItemStack> firstHotbar = mutableSlots(new ItemStack(Items.STONE, 4), ItemStack.EMPTY);
        List<ItemStack> secondHotbar = mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY);
        Harness harness = harness(firstHotbar, secondHotbar, slots(firstHotbar, secondHotbar));

        ArbitrationResult arbitration = arbitrate(harness, 5, 7);

        assertEquals(Decision.DENIED, arbitration.outcome().decision());
        assertNull(arbitration.mutationPlan());
        assertEquals(4, firstHotbar.get(0).getCount());
        assertEquals(7, secondHotbar.get(0).getCount());
    }

    @Test
    void postApprovalSelectedDriftReportsRuntimeFailureHonestly() {
        List<ItemStack> firstHotbar = mutableSlots(new ItemStack(Items.STONE, 5), ItemStack.EMPTY);
        List<ItemStack> secondHotbar = mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY);
        Harness harness = harness(firstHotbar, secondHotbar, slots(firstHotbar, secondHotbar));

        ArbitrationResult arbitration = arbitrate(harness, 5, 7);
        firstHotbar.get(0).setCount(4);
        ExecutionResult execution = execute(harness, arbitration);

        assertEquals(Decision.APPROVED, arbitration.outcome().decision());
        assertNotNull(arbitration.mutationPlan());
        assertEquals(ExecutionStatus.FULL_FAILURE, execution.status());
        assertEquals(MutationExecutionStatus.MUTATION_FAILED, execution.mutationResults().get(0).status());
        assertEquals(
                MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE,
                execution.mutationResults().get(0).detail().values().get("reasonCode"));
    }

    @Test
    void selectedExchangeRuntimeUnknownIsPreservedHonestly() {
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

        ArbitrationResult arbitration = arbitrate(harness, 5, 7);
        runtimePhase.set(true);
        ExecutionResult execution = execute(harness, arbitration);

        assertEquals(Decision.APPROVED, arbitration.outcome().decision());
        assertNotNull(arbitration.mutationPlan());
        assertEquals(MutationExecutionStatus.MUTATION_UNKNOWN, execution.mutationResults().get(0).status());
        assertEquals(
                MinecraftPlayerInventoryContainer.CONTAINER_UNAVAILABLE,
                execution.mutationResults().get(0).detail().values().get("reasonCode"));
        assertEquals(Items.STONE, firstHotbar.get(0).getItem());
        assertEquals(Items.DIRT, secondHotbar.get(0).getItem());
        assertTrue(secondHotbar.get(1).isEmpty());
    }

    @Test
    void malformedSelectedExchangeRuntimePayloadReportsUnknownWithoutCounterfeitSuccess() {
        List<ItemStack> firstHotbar = mutableSlots(new ItemStack(Items.STONE, 5), ItemStack.EMPTY);
        List<ItemStack> secondHotbar = mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY);
        Harness harness = harness(firstHotbar, secondHotbar, slots(firstHotbar, secondHotbar));

        ArbitrationResult arbitration = arbitrate(harness, 5, 7);
        MutationExecutionResponse response = harness.runtimeAuthority().execute(malformedRequest(
                arbitration,
                descriptor -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> runtimePayload =
                            new LinkedHashMap<>((Map<String, Object>) descriptor.get("runtimePayload"));
                    runtimePayload.put(MinecraftRuntimePayloadFactory.BINDING_ID, 7);
                    descriptor.put("runtimePayload", Map.copyOf(runtimePayload));
                }));

        assertEquals(MutationExecutionStatus.MUTATION_UNKNOWN, response.status());
        assertEquals("MALFORMED_RUNTIME_DESCRIPTOR", response.detail().values().get("reasonCode"));
        assertEquals(Items.STONE, firstHotbar.get(0).getItem());
        assertEquals(Items.DIRT, secondHotbar.get(0).getItem());
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
                reasonCode -> new OpaqueObject(Map.of("reasonCode", reasonCode)));
        return new Harness(firstHotbar, secondHotbar, coreAuthority, runtimeAuthority);
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

    private static ArbitrationResult arbitrate(Harness harness, long firstQuantity, long secondQuantity) {
        return CofferCore.arbitrate(
                exchangePayload(firstQuantity, secondQuantity),
                ignored -> new ResolutionResult.Resolved(harness.coreAuthority()),
                new OutcomeId("selected-runtime-outcome-1"),
                new MutationPlanId("selected-runtime-mutation-plan-1"),
                denialReasonIds());
    }

    private static ExecutionResult execute(Harness harness, ArbitrationResult arbitration) {
        return new CofferRuntime().execute(
                new ExecutionPlanId("selected-runtime-execution-plan-1"),
                new ExecutionResultId("selected-runtime-execution-result-1"),
                arbitration.mutationPlan(),
                List.of(new ExecutionStepId("selected-runtime-execution-step-1")),
                List.of(harness.runtimeAuthority()));
    }

    private static ExchangePayload exchangePayload(long firstQuantity, long secondQuantity) {
        List<ValueDeclaration> firstValues = List.of(value("selected-runtime-first-value", "minecraft:stone", firstQuantity));
        List<ValueDeclaration> secondValues = List.of(value("selected-runtime-second-value", "minecraft:dirt", secondQuantity));
        TransferableValueConstructionResult construction =
                TransferableValueExchangePayloadConstruction.constructAtomicSwap(
                        new TransferableValueAtomicSwapConstruction(
                                new TransferableValueAtomicSwapRefs(
                                        new PayloadId("selected-runtime-payload-1"),
                                        truthRefs(firstValues, "selected-runtime-first-truth-"),
                                        truthRefs(secondValues, "selected-runtime-second-truth-"),
                                        new TruthRef("selected-runtime-first-can-receive"),
                                        new TruthRef("selected-runtime-second-can-receive"),
                                        new MutationRequirementRef("selected-runtime-mutation-requirement-1")),
                                actor(FIRST_PLAYER_ID),
                                actor(SECOND_PLAYER_ID),
                                new OfferRef("selected-runtime-offer-1"),
                                new OfferRef("selected-runtime-offer-2"),
                                firstValues,
                                secondValues,
                                "minecraft-inventory"));
        Success success = (Success) construction;
        return success.payload();
    }

    private static Map<ValueRef, TruthRef> truthRefs(List<ValueDeclaration> values, String prefix) {
        Map<ValueRef, TruthRef> truthRefs = new LinkedHashMap<>();
        for (ValueDeclaration value : values) {
            truthRefs.put(value.valueRef(), new TruthRef(prefix + value.valueRef().value()));
        }
        return Map.copyOf(truthRefs);
    }

    private static ActorDeclaration actor(UUID playerId) {
        return new ActorDeclaration(
                new ActorRef("player:" + playerId + ":inventory:hotbar"),
                MinecraftContainerResolver.PLAYER_INVENTORY_KIND,
                new OpaqueObject(Map.of()));
    }

    private static ValueDeclaration value(String valueRef, String itemId, long quantity) {
        return new ValueDeclaration(
                new ValueRef(valueRef),
                TransferableValueCoreAuthority.AUTHORITY_ID,
                new OpaqueObject(Map.of(
                        MinecraftDescriptorFactory.ITEM_ID, itemId,
                        MinecraftDescriptorFactory.QUANTITY, quantity)));
    }

    private static List<ItemStack> mutableSlots(ItemStack... slots) {
        return new ArrayList<>(List.of(slots));
    }

    private static List<ReasonId> denialReasonIds() {
        List<ReasonId> reasonIds = new ArrayList<>();
        for (int index = 0; index < 8; index++) {
            reasonIds.add(new ReasonId("selected-runtime-reason-" + index));
        }
        return List.copyOf(reasonIds);
    }

    private static MutationExecutionRequest malformedRequest(
            ArbitrationResult arbitration,
            java.util.function.Consumer<Map<String, Object>> descriptorMutation) {
        org.coffer.core.model.mutation.AuthorizedMutation mutation = arbitration.mutationPlan().mutations().get(0);
        Map<String, Object> descriptor = new LinkedHashMap<>(mutation.descriptor().values());
        descriptorMutation.accept(descriptor);
        return new MutationExecutionRequest(
                mutation.mutationRef(),
                mutation.satisfies(),
                mutation.authority(),
                new OpaqueObject(Map.copyOf(descriptor)),
                mutation.scope());
    }

    private record Harness(
            List<ItemStack> firstHotbar,
            List<ItemStack> secondHotbar,
            TransferableValueCoreAuthority coreAuthority,
            TransferableValueRuntimeAuthority runtimeAuthority) {
    }
}
