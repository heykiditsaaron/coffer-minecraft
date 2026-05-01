package dev.coffer.minecraft.bindings.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
import org.coffer.core.model.id.ReasonId;
import org.coffer.core.model.id.RequestId;
import org.coffer.core.model.id.ValueRef;
import org.coffer.core.model.outcome.Decision;
import org.coffer.core.model.request.ActorDeclaration;
import org.coffer.core.model.request.AuthorityRequirement;
import org.coffer.core.model.request.ExchangeRequest;
import org.coffer.core.model.request.MutationRequirement;
import org.coffer.core.model.request.Offer;
import org.coffer.core.model.request.ValueDeclaration;
import org.coffer.core.model.support.AuthorityDefinedRequirement;
import org.coffer.core.model.support.Metadata;
import org.coffer.core.model.support.OpaqueObject;
import org.coffer.core.model.support.ReferenceSet;
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

class MinecraftTransferableValueEndToEndTest {
    private static final UUID FIRST_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID SECOND_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000202");
    private static final ActorRef FIRST_ACTOR =
            new ActorRef("player:" + FIRST_PLAYER_ID + ":inventory:main");
    private static final ActorRef SECOND_ACTOR =
            new ActorRef("player:" + SECOND_PLAYER_ID + ":inventory:main");
    private static final ValueRef FIRST_VALUE = new ValueRef("first-value");
    private static final ValueRef SECOND_VALUE = new ValueRef("second-value");

    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void successfulAtomicSwapThroughCoreAndRuntimeMutatesInventories() {
        Harness harness = harness(
                mutableSlots(new ItemStack(Items.STONE, 5), ItemStack.EMPTY),
                mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY));

        ArbitrationResult arbitration = arbitrate(harness, 5, 7);
        ExecutionResult execution = execute(harness, arbitration);

        assertEquals(Decision.APPROVED, arbitration.outcome().decision());
        assertNotNull(arbitration.mutationPlan());
        assertEquals(ExecutionStatus.FULL_SUCCESS, execution.status());
        assertEquals(MutationExecutionStatus.MUTATION_SUCCEEDED, execution.mutationResults().get(0).status());
        assertEquals(Items.DIRT, harness.firstSlots().get(0).getItem());
        assertEquals(7, harness.firstSlots().get(0).getCount());
        assertEquals(0, harness.firstSlots().get(1).getCount());
        assertEquals(Items.STONE, harness.secondSlots().get(0).getItem());
        assertEquals(5, harness.secondSlots().get(0).getCount());
        assertEquals(0, harness.secondSlots().get(1).getCount());
    }

    @Test
    void insufficientQuantityIsDeniedByCoreWithMinecraftReasonCode() {
        Harness harness = harness(
                mutableSlots(new ItemStack(Items.STONE, 4), ItemStack.EMPTY),
                mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY));

        ArbitrationResult arbitration = arbitrate(harness, 5, 7);

        assertEquals(Decision.DENIED, arbitration.outcome().decision());
        assertNull(arbitration.mutationPlan());
        assertEquals(
                MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE,
                arbitration.outcome().reasons().get(0).detail().values().get("reasonCode"));
        assertEquals(4, harness.firstSlots().get(0).getCount());
        assertEquals(7, harness.secondSlots().get(0).getCount());
    }

    @Test
    void postApprovalDriftReportsRuntimeFailureWithoutSuccess() {
        Harness harness = harness(
                mutableSlots(new ItemStack(Items.STONE, 5), ItemStack.EMPTY),
                mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY));
        ArbitrationResult arbitration = arbitrate(harness, 5, 7);
        harness.firstSlots().get(0).setCount(4);

        ExecutionResult execution = execute(harness, arbitration);

        assertEquals(Decision.APPROVED, arbitration.outcome().decision());
        assertNotNull(arbitration.mutationPlan());
        assertEquals(ExecutionStatus.FULL_FAILURE, execution.status());
        assertEquals(MutationExecutionStatus.MUTATION_FAILED, execution.mutationResults().get(0).status());
        assertEquals(
                MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE,
                execution.mutationResults().get(0).detail().values().get("reasonCode"));
        assertEquals(4, harness.firstSlots().get(0).getCount());
        assertEquals(7, harness.secondSlots().get(0).getCount());
    }

    private static Harness harness(List<ItemStack> firstSlots, List<ItemStack> secondSlots) {
        MinecraftContainerResolver resolver = new MinecraftContainerResolver((playerId, region) -> {
            if (FIRST_PLAYER_ID.equals(playerId)) {
                return Optional.of(firstSlots);
            }
            if (SECOND_PLAYER_ID.equals(playerId)) {
                return Optional.of(secondSlots);
            }
            return Optional.empty();
        });
        TransferableValueCoreAuthority coreAuthority = new TransferableValueCoreAuthority(
                resolver,
                new MinecraftDescriptorFactory(),
                new MinecraftRuntimePayloadFactory());
        TransferableValueRuntimeAuthority runtimeAuthority = new TransferableValueRuntimeAuthority(
                resolver,
                new MinecraftRuntimeValueSetResolver(),
                new MinecraftRuntimePayloadInterpreter(),
                reasonCode -> new OpaqueObject(Map.of("reasonCode", reasonCode)));
        return new Harness(firstSlots, secondSlots, coreAuthority, runtimeAuthority);
    }

    private static ArbitrationResult arbitrate(Harness harness, long firstQuantity, long secondQuantity) {
        return CofferCore.arbitrate(
                exchangeRequest(firstQuantity, secondQuantity),
                ignored -> new ResolutionResult.Resolved(harness.coreAuthority()),
                new OutcomeId("outcome-1"),
                new MutationPlanId("mutation-plan-1"),
                List.of(new ReasonId("reason-1")),
                metadata());
    }

    private static ExecutionResult execute(Harness harness, ArbitrationResult arbitration) {
        return new CofferRuntime().execute(
                new ExecutionPlanId("execution-plan-1"),
                new ExecutionResultId("execution-result-1"),
                arbitration.mutationPlan(),
                List.of(new ExecutionStepId("execution-step-1")),
                List.of(harness.runtimeAuthority()),
                metadata());
    }

    private static ExchangeRequest exchangeRequest(long firstQuantity, long secondQuantity) {
        return new ExchangeRequest(
                new RequestId("request-1"),
                List.of(actorDeclaration(FIRST_ACTOR), actorDeclaration(SECOND_ACTOR)),
                List.of(
                        new Offer(
                                new OfferRef("offer-1"),
                                FIRST_ACTOR,
                                List.of(valueDeclaration(FIRST_VALUE, "minecraft:stone", firstQuantity))),
                        new Offer(
                                new OfferRef("offer-2"),
                                SECOND_ACTOR,
                                List.of(valueDeclaration(SECOND_VALUE, "minecraft:dirt", secondQuantity)))),
                List.of(new AuthorityRequirement(TransferableValueCoreAuthority.AUTHORITY_ID, List.of())),
                List.of(atomicSwapRequirement()),
                metadata());
    }

    private static MutationRequirement atomicSwapRequirement() {
        return new MutationRequirement(
                new MutationRequirementRef("mutation-requirement-1"),
                TransferableValueCoreAuthority.AUTHORITY_ID,
                new ReferenceSet(Set.of(FIRST_ACTOR, SECOND_ACTOR), Set.of(FIRST_VALUE, SECOND_VALUE)),
                new AuthorityDefinedRequirement(Map.of(
                        "schemaVersion", TransferableValueCoreAuthority.SCHEMA_VERSION,
                        "type", TransferableValueCoreAuthority.ATOMIC_SWAP,
                        "bindingId", "minecraft-inventory",
                        "firstActorRef", FIRST_ACTOR.value(),
                        "secondActorRef", SECOND_ACTOR.value(),
                        "firstValueRefs", List.of(FIRST_VALUE.value()),
                        "secondValueRefs", List.of(SECOND_VALUE.value()))));
    }

    private static ActorDeclaration actorDeclaration(ActorRef actorRef) {
        return new ActorDeclaration(
                actorRef,
                MinecraftContainerResolver.PLAYER_INVENTORY_KIND,
                new OpaqueObject(Map.of()));
    }

    private static ValueDeclaration valueDeclaration(ValueRef valueRef, String itemId, long quantity) {
        return new ValueDeclaration(
                valueRef,
                TransferableValueCoreAuthority.AUTHORITY_ID,
                new OpaqueObject(Map.of(
                        MinecraftDescriptorFactory.ITEM_ID, itemId,
                        MinecraftDescriptorFactory.QUANTITY, quantity)));
    }

    private static List<ItemStack> mutableSlots(ItemStack... slots) {
        return new ArrayList<>(List.of(slots));
    }

    private static Metadata metadata() {
        return new Metadata(Map.of());
    }

    private record Harness(
            List<ItemStack> firstSlots,
            List<ItemStack> secondSlots,
            TransferableValueCoreAuthority coreAuthority,
            TransferableValueRuntimeAuthority runtimeAuthority) {
    }
}
