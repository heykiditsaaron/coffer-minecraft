package dev.coffer.minecraft.bindings.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coffer.minecraft.testsupport.ghostadapter.GhostAdapterAccountabilityProjection;
import dev.coffer.minecraft.testsupport.ghostadapter.GhostAdapterExchangeHarness;
import dev.coffer.minecraft.testsupport.ghostadapter.GhostAdapterExchangeHarness.GhostAdapterAtomicSwapRequest;
import dev.coffer.minecraft.testsupport.ghostadapter.GhostAdapterExchangeHarness.GhostAdapterProjection;
import dev.coffer.minecraft.testsupport.ghostadapter.GhostAdapterExchangeHarness.ProjectionKind;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.coffer.core.model.id.ActorRef;
import org.coffer.core.model.id.OfferRef;
import org.coffer.core.model.id.ValueRef;
import org.coffer.core.model.request.ActorDeclaration;
import org.coffer.core.model.request.ValueDeclaration;
import org.coffer.core.model.support.OpaqueObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GhostAdapterAccountabilityProjectionTest {
    private static final long TIMESTAMP = 1_700_000_000_000L;
    private static final UUID FIRST_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000311");
    private static final UUID SECOND_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000312");
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
    void constructionRefusalProjectsOnlySerRecord() {
        GhostAdapterProjection projection = harness(
                        mutableSlots(new ItemStack(Items.STONE, 5), ItemStack.EMPTY),
                        mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY))
                .submitAtomicSwap(request(5, 7, " "));

        List<Map<String, Object>> records = GhostAdapterAccountabilityProjection.toJsonlRecords(
                TIMESTAMP,
                "interaction-1",
                projection);

        assertEquals(1, records.size());
        assertEquals(
                Map.of(
                        "timestamp", TIMESTAMP,
                        "interactionId", "interaction-1",
                        "recordType", "SER",
                        "stage", "construction_refused",
                        "code", "MISSING_BINDING_ID"),
                records.get(0));
    }

    @Test
    void coreDenialProjectsCerWithoutFabricatingRuntimeParticipation() {
        GhostAdapterProjection projection = harness(
                        mutableSlots(new ItemStack(Items.STONE, 4), ItemStack.EMPTY),
                        mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY))
                .submitAtomicSwap(request(5, 7, "minecraft-inventory"));

        List<Map<String, Object>> records = GhostAdapterAccountabilityProjection.toJsonlRecords(
                TIMESTAMP,
                "interaction-2",
                projection);

        assertIterableEquals(
                List.of("interaction-2", "interaction-2"),
                records.stream().map(record -> (String) record.get("interactionId")).toList());
        assertEquals(TIMESTAMP, records.get(0).get("timestamp"));
        assertEquals(TIMESTAMP, records.get(1).get("timestamp"));
        assertEquals("SER", records.get(0).get("recordType"));
        assertEquals("captured", records.get(0).get("stage"));
        assertFalse(records.get(0).containsKey("code"));
        assertEquals(
                Map.of(
                        "timestamp", TIMESTAMP,
                        "interactionId", "interaction-2",
                        "recordType", "CER",
                        "stage", "core_denied",
                        "code", MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE),
                records.get(1));
    }

    @Test
    void runtimeSuccessRemainsDistinctFromApproval() {
        GhostAdapterProjection projection = harness(
                        mutableSlots(new ItemStack(Items.STONE, 5), ItemStack.EMPTY),
                        mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY))
                .submitAtomicSwap(request(5, 7, "minecraft-inventory"));

        List<Map<String, Object>> records = GhostAdapterAccountabilityProjection.toJsonlRecords(
                TIMESTAMP,
                "interaction-3",
                projection);

        assertEquals(TIMESTAMP, records.get(0).get("timestamp"));
        assertEquals(TIMESTAMP, records.get(1).get("timestamp"));
        assertEquals(
                Map.of(
                        "timestamp", TIMESTAMP,
                        "interactionId", "interaction-3",
                        "recordType", "CER",
                        "stage", "runtime_succeeded"),
                records.get(1));
    }

    @Test
    void runtimeFailureProjectsDistinctCerOutcome() {
        List<ItemStack> firstSlots = mutableSlots(new ItemStack(Items.STONE, 5), ItemStack.EMPTY);
        GhostAdapterProjection projection = new GhostAdapterExchangeHarness(
                        playerInventorySlots(firstSlots, mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY)),
                        org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueExchangePayloadConstruction::constructAtomicSwap,
                        (payload, authority) -> org.coffer.core.arbitration.CofferCore.arbitrate(
                                payload,
                                ignored -> new org.coffer.core.authority.ResolutionResult.Resolved(authority),
                                new org.coffer.core.model.id.OutcomeId("ghost-ser-cer-outcome-1"),
                                new org.coffer.core.model.id.MutationPlanId("ghost-ser-cer-mutation-plan-1"),
                                denialReasonIds()),
                        (mutationPlan, authority) -> new org.coffer.runtime.CofferRuntime().execute(
                                new org.coffer.runtime.model.id.ExecutionPlanId("ghost-ser-cer-execution-plan-1"),
                                new org.coffer.runtime.model.id.ExecutionResultId("ghost-ser-cer-execution-result-1"),
                                mutationPlan,
                                executionStepIds(mutationPlan.mutations().size()),
                                List.of(authority)),
                        () -> firstSlots.get(0).setCount(4))
                .submitAtomicSwap(request(5, 7, "minecraft-inventory"));

        List<Map<String, Object>> records = GhostAdapterAccountabilityProjection.toJsonlRecords(
                TIMESTAMP,
                "interaction-4",
                projection);

        assertEquals(TIMESTAMP, records.get(0).get("timestamp"));
        assertEquals(TIMESTAMP, records.get(1).get("timestamp"));
        assertEquals(
                Map.of(
                        "timestamp", TIMESTAMP,
                        "interactionId", "interaction-4",
                        "recordType", "CER",
                        "stage", "runtime_failed",
                        "code", MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE),
                records.get(1));
    }

    @Test
    void runtimeUnknownProjectsDistinctCerOutcome() {
        AtomicBoolean runtimePhase = new AtomicBoolean(false);
        List<ItemStack> firstSlots = mutableSlots(new ItemStack(Items.STONE, 5), ItemStack.EMPTY);
        List<ItemStack> secondSlots = mutableSlots(new ItemStack(Items.DIRT, 7), ItemStack.EMPTY);
        GhostAdapterProjection projection = new GhostAdapterExchangeHarness(
                        (playerId, region) -> {
                            if (FIRST_PLAYER_ID.equals(playerId)) {
                                return Optional.of(firstSlots);
                            }
                            if (SECOND_PLAYER_ID.equals(playerId)) {
                                return runtimePhase.get() ? Optional.empty() : Optional.of(secondSlots);
                            }
                            return Optional.empty();
                        },
                        org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueExchangePayloadConstruction::constructAtomicSwap,
                        (payload, authority) -> org.coffer.core.arbitration.CofferCore.arbitrate(
                                payload,
                                ignored -> new org.coffer.core.authority.ResolutionResult.Resolved(authority),
                                new org.coffer.core.model.id.OutcomeId("ghost-ser-cer-outcome-2"),
                                new org.coffer.core.model.id.MutationPlanId("ghost-ser-cer-mutation-plan-2"),
                                denialReasonIds()),
                        (mutationPlan, authority) -> new org.coffer.runtime.CofferRuntime().execute(
                                new org.coffer.runtime.model.id.ExecutionPlanId("ghost-ser-cer-execution-plan-2"),
                                new org.coffer.runtime.model.id.ExecutionResultId("ghost-ser-cer-execution-result-2"),
                                mutationPlan,
                                executionStepIds(mutationPlan.mutations().size()),
                                List.of(authority)),
                        () -> runtimePhase.set(true))
                .submitAtomicSwap(request(5, 7, "minecraft-inventory"));

        List<Map<String, Object>> records = GhostAdapterAccountabilityProjection.toJsonlRecords(
                TIMESTAMP,
                "interaction-5",
                projection);

        assertEquals(
                Map.of(
                        "timestamp", TIMESTAMP,
                        "interactionId", "interaction-5",
                        "recordType", "CER",
                        "stage", "runtime_unknown",
                        "code", MinecraftPlayerInventoryContainer.CONTAINER_UNAVAILABLE),
                records.get(1));
        assertMinimal(records);
    }

    @Test
    void malformedRuntimeUnknownKeepsSameMinimalProjectionShape() {
        GhostAdapterProjection projection = new GhostAdapterProjection(
                ProjectionKind.RUNTIME_UNKNOWN,
                "MALFORMED_RUNTIME_DESCRIPTOR",
                null,
                null,
                null);

        List<Map<String, Object>> records = GhostAdapterAccountabilityProjection.toJsonlRecords(
                TIMESTAMP,
                "interaction-6",
                projection);

        assertEquals(2, records.size());
        assertEquals(
                Map.of(
                        "timestamp", TIMESTAMP,
                        "interactionId", "interaction-6",
                        "recordType", "SER",
                        "stage", "captured"),
                records.get(0));
        assertEquals(
                Map.of(
                        "timestamp", TIMESTAMP,
                        "interactionId", "interaction-6",
                        "recordType", "CER",
                        "stage", "runtime_unknown",
                        "code", "MALFORMED_RUNTIME_DESCRIPTOR"),
                records.get(1));
        assertMinimal(records);
    }

    @Test
    void unknownCauseVariantDoesNotCreateContradictoryIdentityOrExtraLayers() {
        List<Map<String, Object>> malformedRecords = GhostAdapterAccountabilityProjection.toJsonlRecords(
                TIMESTAMP,
                "interaction-7",
                new GhostAdapterProjection(
                        ProjectionKind.RUNTIME_UNKNOWN,
                        "MALFORMED_RUNTIME_DESCRIPTOR",
                        null,
                        null,
                        null));
        List<Map<String, Object>> disappearanceRecords = GhostAdapterAccountabilityProjection.toJsonlRecords(
                TIMESTAMP,
                "interaction-8",
                new GhostAdapterProjection(
                        ProjectionKind.RUNTIME_UNKNOWN,
                        MinecraftPlayerInventoryContainer.CONTAINER_UNAVAILABLE,
                        null,
                        null,
                        null));

        assertEquals(2, malformedRecords.size());
        assertEquals(2, disappearanceRecords.size());
        assertIterableEquals(
                List.of("interaction-7", "interaction-7"),
                malformedRecords.stream().map(record -> (String) record.get("interactionId")).toList());
        assertIterableEquals(
                List.of("interaction-8", "interaction-8"),
                disappearanceRecords.stream().map(record -> (String) record.get("interactionId")).toList());
        assertEquals(TIMESTAMP, malformedRecords.get(0).get("timestamp"));
        assertEquals(TIMESTAMP, malformedRecords.get(1).get("timestamp"));
        assertEquals(TIMESTAMP, disappearanceRecords.get(0).get("timestamp"));
        assertEquals(TIMESTAMP, disappearanceRecords.get(1).get("timestamp"));
        assertEquals("runtime_unknown", malformedRecords.get(1).get("stage"));
        assertEquals("runtime_unknown", disappearanceRecords.get(1).get("stage"));
        assertNotEquals(malformedRecords.get(1).get("code"), disappearanceRecords.get(1).get("code"));
        assertMinimal(malformedRecords);
        assertMinimal(disappearanceRecords);
    }

    private static GhostAdapterExchangeHarness harness(List<ItemStack> firstSlots, List<ItemStack> secondSlots) {
        return GhostAdapterExchangeHarness.create(playerInventorySlots(firstSlots, secondSlots));
    }

    private static MinecraftContainerResolver.PlayerInventorySlots playerInventorySlots(
            List<ItemStack> firstSlots,
            List<ItemStack> secondSlots) {
        return (playerId, region) -> {
            if (FIRST_PLAYER_ID.equals(playerId)) {
                return Optional.of(firstSlots);
            }
            if (SECOND_PLAYER_ID.equals(playerId)) {
                return Optional.of(secondSlots);
            }
            return Optional.empty();
        };
    }

    private static GhostAdapterAtomicSwapRequest request(long firstQuantity, long secondQuantity, String bindingId) {
        return new GhostAdapterAtomicSwapRequest(
                bindingId,
                actor(FIRST_ACTOR),
                actor(SECOND_ACTOR),
                new OfferRef("ghost-ser-cer-offer-1"),
                new OfferRef("ghost-ser-cer-offer-2"),
                List.of(value("ghost-ser-cer-first-value", "minecraft:stone", firstQuantity)),
                List.of(value("ghost-ser-cer-second-value", "minecraft:dirt", secondQuantity)));
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
                org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority.AUTHORITY_ID,
                new OpaqueObject(Map.of(
                        MinecraftDescriptorFactory.ITEM_ID, itemId,
                        MinecraftDescriptorFactory.QUANTITY, quantity)));
    }

    private static List<org.coffer.core.model.id.ReasonId> denialReasonIds() {
        List<org.coffer.core.model.id.ReasonId> reasonIds = new ArrayList<>();
        for (int index = 0; index < 8; index++) {
            reasonIds.add(new org.coffer.core.model.id.ReasonId("ghost-ser-cer-reason-" + index));
        }
        return List.copyOf(reasonIds);
    }

    private static List<org.coffer.runtime.model.id.ExecutionStepId> executionStepIds(int count) {
        List<org.coffer.runtime.model.id.ExecutionStepId> stepIds = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            stepIds.add(new org.coffer.runtime.model.id.ExecutionStepId("ghost-ser-cer-step-" + index));
        }
        return List.copyOf(stepIds);
    }

    private static List<ItemStack> mutableSlots(ItemStack... slots) {
        return new ArrayList<>(List.of(slots));
    }

    private static void assertMinimal(List<Map<String, Object>> records) {
        assertTrue(records.stream().allMatch(record -> record.size() <= 5));
        assertTrue(records.stream().allMatch(record -> record.containsKey("timestamp")));
        assertTrue(records.stream().allMatch(record -> record.get("timestamp") instanceof Number));
        assertTrue(records.stream().allMatch(record -> !record.containsKey("runtime")));
        assertTrue(records.stream().allMatch(record -> !record.containsKey("ser")));
        assertTrue(records.stream().allMatch(record -> !record.containsKey("cer")));
        assertTrue(records.stream().allMatch(record -> !record.containsKey("explanation")));
    }
}
