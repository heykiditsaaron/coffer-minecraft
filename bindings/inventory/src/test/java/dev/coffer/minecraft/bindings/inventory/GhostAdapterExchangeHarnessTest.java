package dev.coffer.minecraft.bindings.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coffer.minecraft.testsupport.ghostadapter.GhostAdapterExchangeHarness;
import dev.coffer.minecraft.testsupport.ghostadapter.GhostAdapterExchangeHarness.ConstructionGateway;
import dev.coffer.minecraft.testsupport.ghostadapter.GhostAdapterExchangeHarness.CoreGateway;
import dev.coffer.minecraft.testsupport.ghostadapter.GhostAdapterExchangeHarness.GhostAdapterAtomicSwapRequest;
import dev.coffer.minecraft.testsupport.ghostadapter.GhostAdapterExchangeHarness.GhostAdapterProjection;
import dev.coffer.minecraft.testsupport.ghostadapter.GhostAdapterExchangeHarness.ProjectionKind;
import dev.coffer.minecraft.testsupport.ghostadapter.GhostAdapterExchangeHarness.RuntimeGateway;
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
import org.coffer.core.model.id.ValueRef;
import org.coffer.core.model.mutation.MutationPlan;
import org.coffer.core.model.request.ActorDeclaration;
import org.coffer.core.model.request.ExchangePayload;
import org.coffer.core.model.request.ValueDeclaration;
import org.coffer.core.model.support.OpaqueObject;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueConstructionRefusalReason;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueExchangePayloadConstruction;
import org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority;
import org.coffer.firstparty.authority.transferablevalue.runtime.TransferableValueRuntimeAuthority;
import org.coffer.runtime.CofferRuntime;
import org.coffer.runtime.model.execution.ExecutionResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GhostAdapterExchangeHarnessTest {
    private static final UUID FIRST_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");
    private static final UUID SECOND_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000302");
    private static final ActorRef FIRST_ACTOR =
            new ActorRef("player:" + FIRST_PLAYER_ID + ":inventory:main");
    private static final ActorRef SECOND_ACTOR =
            new ActorRef("player:" + SECOND_PLAYER_ID + ":inventory:main");

    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void constructionRefusalIsProjectedBeforeCore() {
        AtomicInteger constructionCalls = new AtomicInteger();
        AtomicInteger coreCalls = new AtomicInteger();
        AtomicInteger runtimeCalls = new AtomicInteger();
        GhostAdapterExchangeHarness harness = harness(
                mutableSlots(new ItemStack(Items.STONE, 5), ItemStack.EMPTY),
                mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY),
                countingConstructionGateway(constructionCalls),
                countingCoreGateway(coreCalls),
                countingRuntimeGateway(runtimeCalls),
                () -> {
                });

        GhostAdapterProjection projection = harness.submitAtomicSwap(request(5, 7, " "));

        assertEquals(ProjectionKind.CONSTRUCTION_REFUSED, projection.kind());
        assertEquals(TransferableValueConstructionRefusalReason.MISSING_BINDING_ID, projection.refusal().reason());
        assertNull(projection.arbitration());
        assertNull(projection.executionResult());
        assertEquals(1, constructionCalls.get());
        assertEquals(0, coreCalls.get());
        assertEquals(0, runtimeCalls.get());
    }

    @Test
    void coreDenialPreventsRuntimeInvocation() {
        AtomicInteger runtimeCalls = new AtomicInteger();
        GhostAdapterExchangeHarness harness = harness(
                mutableSlots(new ItemStack(Items.STONE, 4), ItemStack.EMPTY),
                mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY),
                TransferableValueExchangePayloadConstruction::constructAtomicSwap,
                countingCoreGateway(new AtomicInteger()),
                countingRuntimeGateway(runtimeCalls),
                () -> {
                });

        GhostAdapterProjection projection = harness.submitAtomicSwap(request(5, 7, "minecraft-inventory"));

        assertEquals(ProjectionKind.CORE_DENIED, projection.kind());
        assertEquals(MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE, projection.reasonCode());
        assertNotNull(projection.arbitration());
        assertNull(projection.executionResult());
        assertEquals(0, runtimeCalls.get());
    }

    @Test
    void coreApprovalInvokesRuntimeAndProjectsSuccess() {
        AtomicInteger runtimeCalls = new AtomicInteger();
        List<ItemStack> firstSlots = mutableSlots(new ItemStack(Items.STONE, 5), ItemStack.EMPTY);
        List<ItemStack> secondSlots = mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY);
        GhostAdapterExchangeHarness harness = harness(
                firstSlots,
                secondSlots,
                TransferableValueExchangePayloadConstruction::constructAtomicSwap,
                countingCoreGateway(new AtomicInteger()),
                countingRuntimeGateway(runtimeCalls),
                () -> {
                });

        GhostAdapterProjection projection = harness.submitAtomicSwap(request(5, 7, "minecraft-inventory"));

        assertEquals(ProjectionKind.RUNTIME_SUCCESS, projection.kind());
        assertNotNull(projection.executionResult());
        assertEquals(1, runtimeCalls.get());
        assertEquals(Items.DIRT, firstSlots.get(0).getItem());
        assertEquals(Items.STONE, secondSlots.get(0).getItem());
    }

    @Test
    void runtimeFailureIsProjectedDistinctly() {
        AtomicInteger runtimeCalls = new AtomicInteger();
        List<ItemStack> firstSlots = mutableSlots(new ItemStack(Items.STONE, 5), ItemStack.EMPTY);
        List<ItemStack> secondSlots = mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY);
        GhostAdapterExchangeHarness harness = harness(
                firstSlots,
                secondSlots,
                TransferableValueExchangePayloadConstruction::constructAtomicSwap,
                countingCoreGateway(new AtomicInteger()),
                countingRuntimeGateway(runtimeCalls),
                () -> firstSlots.get(0).setCount(4));

        GhostAdapterProjection projection = harness.submitAtomicSwap(request(5, 7, "minecraft-inventory"));

        assertEquals(ProjectionKind.RUNTIME_FAILURE, projection.kind());
        assertEquals(MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE, projection.reasonCode());
        assertEquals(1, runtimeCalls.get());
    }

    @Test
    void runtimeUnknownIsProjectedDistinctly() {
        AtomicBoolean runtimePhase = new AtomicBoolean(false);
        List<ItemStack> firstSlots = mutableSlots(new ItemStack(Items.STONE, 5), ItemStack.EMPTY);
        List<ItemStack> secondSlots = mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY);
        GhostAdapterExchangeHarness harness = harness(
                (playerId, region) -> {
                    if (FIRST_PLAYER_ID.equals(playerId)) {
                        return Optional.of(firstSlots);
                    }
                    if (SECOND_PLAYER_ID.equals(playerId)) {
                        return runtimePhase.get() ? Optional.empty() : Optional.of(secondSlots);
                    }
                    return Optional.empty();
                },
                TransferableValueExchangePayloadConstruction::constructAtomicSwap,
                countingCoreGateway(new AtomicInteger()),
                countingRuntimeGateway(new AtomicInteger()),
                () -> runtimePhase.set(true));

        GhostAdapterProjection projection = harness.submitAtomicSwap(request(5, 7, "minecraft-inventory"));

        assertEquals(ProjectionKind.RUNTIME_UNKNOWN, projection.kind());
        assertEquals(MinecraftPlayerInventoryContainer.CONTAINER_UNAVAILABLE, projection.reasonCode());
        assertEquals(Items.STONE, firstSlots.get(0).getItem());
        assertEquals(5, firstSlots.get(0).getCount());
        assertTrue(firstSlots.get(1).isEmpty());
        assertEquals(Items.DIRT, secondSlots.get(0).getItem());
        assertEquals(7, secondSlots.get(0).getCount());
        assertTrue(secondSlots.get(1).isEmpty());
    }

    @Test
    void ghostHarnessUsesPavedConstructionGateway() {
        AtomicInteger constructionCalls = new AtomicInteger();
        GhostAdapterExchangeHarness harness = harness(
                mutableSlots(new ItemStack(Items.STONE, 5), ItemStack.EMPTY),
                mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY),
                countingConstructionGateway(constructionCalls),
                countingCoreGateway(new AtomicInteger()),
                countingRuntimeGateway(new AtomicInteger()),
                () -> {
                });

        GhostAdapterProjection projection = harness.submitAtomicSwap(request(5, 7, "minecraft-inventory"));

        assertEquals(ProjectionKind.RUNTIME_SUCCESS, projection.kind());
        assertEquals(1, constructionCalls.get());
    }

    private static GhostAdapterExchangeHarness harness(
            List<ItemStack> firstSlots,
            List<ItemStack> secondSlots,
            ConstructionGateway constructionGateway,
            CoreGateway coreGateway,
            RuntimeGateway runtimeGateway,
            Runnable beforeRuntime) {
        return harness((playerId, region) -> {
            if (FIRST_PLAYER_ID.equals(playerId)) {
                return Optional.of(firstSlots);
            }
            if (SECOND_PLAYER_ID.equals(playerId)) {
                return Optional.of(secondSlots);
            }
            return Optional.empty();
        }, constructionGateway, coreGateway, runtimeGateway, beforeRuntime);
    }

    private static GhostAdapterExchangeHarness harness(
            MinecraftContainerResolver.PlayerInventorySlots slots,
            ConstructionGateway constructionGateway,
            CoreGateway coreGateway,
            RuntimeGateway runtimeGateway,
            Runnable beforeRuntime) {
        return new GhostAdapterExchangeHarness(
                slots,
                constructionGateway,
                coreGateway,
                runtimeGateway,
                beforeRuntime);
    }

    private static ConstructionGateway countingConstructionGateway(AtomicInteger calls) {
        return construction -> {
            calls.incrementAndGet();
            return TransferableValueExchangePayloadConstruction.constructAtomicSwap(construction);
        };
    }

    private static CoreGateway countingCoreGateway(AtomicInteger calls) {
        return (payload, authority) -> {
            calls.incrementAndGet();
            return CofferCore.arbitrate(
                    payload,
                    ignored -> new ResolutionResult.Resolved(authority),
                    new OutcomeId("ghost-adapter-outcome-1"),
                    new MutationPlanId("ghost-adapter-mutation-plan-1"),
                    denialReasonIds());
        };
    }

    private static RuntimeGateway countingRuntimeGateway(AtomicInteger calls) {
        return (mutationPlan, authority) -> {
            calls.incrementAndGet();
            return new CofferRuntime().execute(
                    new org.coffer.runtime.model.id.ExecutionPlanId("ghost-adapter-execution-plan-1"),
                    new org.coffer.runtime.model.id.ExecutionResultId("ghost-adapter-execution-result-1"),
                    mutationPlan,
                    executionStepIds(mutationPlan),
                    List.of(authority));
        };
    }

    private static GhostAdapterAtomicSwapRequest request(long firstQuantity, long secondQuantity, String bindingId) {
        return new GhostAdapterAtomicSwapRequest(
                bindingId,
                actor(FIRST_ACTOR),
                actor(SECOND_ACTOR),
                new OfferRef("ghost-offer-1"),
                new OfferRef("ghost-offer-2"),
                List.of(value("first-value", "minecraft:stone", firstQuantity)),
                List.of(value("second-value", "minecraft:dirt", secondQuantity)));
    }

    private static ActorDeclaration actor(ActorRef actorRef) {
        return new ActorDeclaration(
                actorRef,
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

    private static List<ReasonId> denialReasonIds() {
        List<ReasonId> reasonIds = new ArrayList<>();
        for (int index = 0; index < 8; index++) {
            reasonIds.add(new ReasonId("ghost-adapter-reason-" + index));
        }
        return List.copyOf(reasonIds);
    }

    private static List<org.coffer.runtime.model.id.ExecutionStepId> executionStepIds(MutationPlan mutationPlan) {
        List<org.coffer.runtime.model.id.ExecutionStepId> stepIds = new ArrayList<>();
        for (int index = 0; index < mutationPlan.mutations().size(); index++) {
            stepIds.add(new org.coffer.runtime.model.id.ExecutionStepId("ghost-adapter-step-" + index));
        }
        return List.copyOf(stepIds);
    }

    private static List<ItemStack> mutableSlots(ItemStack... slots) {
        return new ArrayList<>(List.of(slots));
    }
}
