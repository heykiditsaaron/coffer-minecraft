package dev.coffer.minecraft.platform.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coffer.minecraft.bindings.inventory.MinecraftItemDescriptor;
import dev.coffer.minecraft.bindings.inventory.MinecraftPlayerInventoryContainer;
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
import org.coffer.firstparty.authority.transferablevalue.port.MutationApplicationResult;
import org.coffer.firstparty.authority.transferablevalue.port.TransferableValueSet;
import org.coffer.runtime.model.execution.ExecutionResult;
import org.coffer.runtime.model.execution.ExecutionStatus;
import org.coffer.runtime.model.execution.MutationExecutionResult;
import org.coffer.runtime.model.execution.MutationExecutionStatus;
import org.coffer.runtime.model.id.ExecutionPlanId;
import org.coffer.runtime.model.id.ExecutionResultId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CofferMinecraftAdminShopPresetListingInterruptionPressureTest {
    private static final long TIMESTAMP = 1_700_000_000_000L;
    private static final UUID PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000001193");
    private static final UUID SHOP_STOCK_ID = UUID.fromString("00000000-0000-0000-0000-000000001194");
    private static final String SHOP_SUPPLY_UNAVAILABLE = "minecraft.shop.value.not_available";

    @TempDir
    Path tempDir;

    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void listingDisabledAfterReadinessBeforeSubmissionInvalidatesHonestly() throws IOException {
        AtomicBoolean runtimeEntered = new AtomicBoolean(false);
        CofferMinecraftLifecycleAccountability accountability = accountability();
        CofferMinecraftAdminShopPresetListingConfirmation confirmation = new CofferMinecraftAdminShopPresetListingConfirmation();
        CofferMinecraftAdminShopPresetListingSubmissionChain chain = new CofferMinecraftAdminShopPresetListingSubmissionChain(
                confirmation,
                CofferMinecraftAdminShopPresetListingRequestAssembly.create(),
                new CofferMinecraftSelectedExchangeRuntimeParticipation(
                        CofferMinecraftAdminShopPresetListingInterruptionPressureTest::approvedArbitration,
                        mutationPlan -> {
                            runtimeEntered.set(true);
                            throw new AssertionError("runtime must not be entered");
                        }),
                new CofferMinecraftAdminShopPresetListingAccountabilityProjection(accountability));
        CofferMinecraftAdminShopPresetListingConfirmation.ExchangeState confirmedState =
                state(enabledInfiniteListing(), selectedSnapshot("minecraft:emerald", 3, null));
        CofferMinecraftAdminShopPresetListingConfirmation.ConfirmationLedger ledger =
                confirmation.confirm(confirmedState, confirmedState.player().actorRef(),
                        CofferMinecraftAdminShopPresetListingConfirmation.ConfirmationLedger.empty());
        CofferMinecraftAdminShopPresetListingConfirmation.ExchangeState disabledState =
                state(disabledInfiniteListing(), selectedSnapshot("minecraft:emerald", 3, null));

        CofferMinecraftAdminShopPresetListingSubmissionChain.SubmissionResult.NotSubmitted notSubmitted =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingSubmissionChain.SubmissionResult.NotSubmitted.class,
                        chain.submit(tempDir, disabledState, ledger));

        assertEquals(CofferMinecraftAdminShopPresetListingConfirmation.PLAYER_NOT_CONFIRMED, notSubmitted.reasonCode());
        assertFalse(runtimeEntered.get());
        assertTrue(Files.notExists(accountability.logPath(tempDir)));
    }

    @Test
    void playerSelectedValueChangesAfterConfirmationInvalidatesHonestly() throws IOException {
        AtomicBoolean runtimeEntered = new AtomicBoolean(false);
        CofferMinecraftLifecycleAccountability accountability = accountability();
        CofferMinecraftAdminShopPresetListingConfirmation confirmation = new CofferMinecraftAdminShopPresetListingConfirmation();
        CofferMinecraftAdminShopPresetListingSubmissionChain chain = new CofferMinecraftAdminShopPresetListingSubmissionChain(
                confirmation,
                CofferMinecraftAdminShopPresetListingRequestAssembly.create(),
                new CofferMinecraftSelectedExchangeRuntimeParticipation(
                        CofferMinecraftAdminShopPresetListingInterruptionPressureTest::approvedArbitration,
                        mutationPlan -> {
                            runtimeEntered.set(true);
                            throw new AssertionError("runtime must not be entered");
                        }),
                new CofferMinecraftAdminShopPresetListingAccountabilityProjection(accountability));
        CofferMinecraftAdminShopPresetListingConfirmation.ExchangeState confirmedState =
                state(finiteListing(), selectedSnapshot("minecraft:emerald", 3, null));
        CofferMinecraftAdminShopPresetListingConfirmation.ConfirmationLedger ledger =
                confirmation.confirm(confirmedState, confirmedState.player().actorRef(),
                        CofferMinecraftAdminShopPresetListingConfirmation.ConfirmationLedger.empty());
        CofferMinecraftAdminShopPresetListingConfirmation.ExchangeState changedState =
                state(finiteListing(), selectedSnapshot("minecraft:emerald", 4, null));

        CofferMinecraftAdminShopPresetListingSubmissionChain.SubmissionResult.NotSubmitted notSubmitted =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingSubmissionChain.SubmissionResult.NotSubmitted.class,
                        chain.submit(tempDir, changedState, ledger));

        assertEquals(CofferMinecraftAdminShopPresetListingConfirmation.PLAYER_NOT_CONFIRMED, notSubmitted.reasonCode());
        assertFalse(runtimeEntered.get());
        assertTrue(Files.notExists(accountability.logPath(tempDir)));
    }

    @Test
    void finiteShopSupplyChangeAfterConfirmationBeforeExecutionFailsHonestlyWithoutHiddenMutation() throws IOException {
        List<ItemStack> playerSlots = mutableSlots(new ItemStack(Items.EMERALD, 3), ItemStack.EMPTY);
        List<ItemStack> shopSlots = mutableSlots(new ItemStack(Items.DIAMOND, 1), ItemStack.EMPTY);
        CofferMinecraftLifecycleAccountability accountability = accountability();
        CofferMinecraftAdminShopPresetListingConfirmation confirmation = new CofferMinecraftAdminShopPresetListingConfirmation();
        CofferMinecraftAdminShopPresetListingConfirmation.ExchangeState state =
                state(finiteListing(), selectedSnapshot("minecraft:emerald", 3, null));
        CofferMinecraftAdminShopPresetListingConfirmation.ConfirmationLedger ledger =
                confirmation.confirm(state, state.player().actorRef(),
                        CofferMinecraftAdminShopPresetListingConfirmation.ConfirmationLedger.empty());
        shopSlots.set(0, ItemStack.EMPTY);
        CofferMinecraftAdminShopPresetListingSubmissionChain chain = new CofferMinecraftAdminShopPresetListingSubmissionChain(
                confirmation,
                CofferMinecraftAdminShopPresetListingRequestAssembly.create(),
                new CofferMinecraftSelectedExchangeRuntimeParticipation(
                        CofferMinecraftAdminShopPresetListingInterruptionPressureTest::approvedArbitration,
                        mutationPlan -> finiteExecution(mutationPlan, state, playerSlots, shopSlots, false)),
                new CofferMinecraftAdminShopPresetListingAccountabilityProjection(accountability));

        CofferMinecraftAdminShopPresetListingSubmissionChain.SubmissionResult.Submitted submitted =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingSubmissionChain.SubmissionResult.Submitted.class,
                        chain.submit(tempDir, state, ledger));

        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated runtime =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated.class,
                        submitted.participation());
        assertEquals(ExecutionStatus.FULL_FAILURE, runtime.execution().status());
        assertEquals(MutationExecutionStatus.MUTATION_FAILED, runtime.execution().mutationResults().get(0).status());
        assertEquals(SHOP_SUPPLY_UNAVAILABLE, runtime.execution().mutationResults().get(0).detail().values().get("reasonCode"));
        assertEquals(Items.EMERALD, playerSlots.get(0).getItem());
        assertEquals(3, playerSlots.get(0).getCount());
        assertTrue(shopSlots.get(0).isEmpty());

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));
        assertEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"admin-shop-interruption-1\",\"recordType\":\"CER\",\"stage\":\"fabric_core_approved\",\"seam\":\"fabric_core\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"admin-shop-interruption-2\",\"recordType\":\"CER\",\"stage\":\"fabric_runtime_failed\",\"seam\":\"fabric_runtime\",\"code\":\"minecraft.shop.value.not_available\"}"),
                lines);
        assertFalse(lines.stream().anyMatch(line -> line.contains("fabric_runtime_succeeded")));
    }

    @Test
    void interruptionBeforeRuntimeDoesNotCounterfeitExecutionAndUnknownRemainsUnknown() throws IOException {
        List<ItemStack> playerSlots = mutableSlots(new ItemStack(Items.EMERALD, 3), ItemStack.EMPTY);
        List<ItemStack> shopSlots = mutableSlots(new ItemStack(Items.DIAMOND, 1), ItemStack.EMPTY);
        CofferMinecraftLifecycleAccountability accountability = accountability();
        CofferMinecraftAdminShopPresetListingConfirmation confirmation = new CofferMinecraftAdminShopPresetListingConfirmation();
        CofferMinecraftAdminShopPresetListingConfirmation.ExchangeState state =
                state(finiteListing(), selectedSnapshot("minecraft:emerald", 3, null));
        CofferMinecraftAdminShopPresetListingConfirmation.ConfirmationLedger ledger =
                confirmation.confirm(state, state.player().actorRef(),
                        CofferMinecraftAdminShopPresetListingConfirmation.ConfirmationLedger.empty());
        CofferMinecraftAdminShopPresetListingSubmissionChain chain = new CofferMinecraftAdminShopPresetListingSubmissionChain(
                confirmation,
                CofferMinecraftAdminShopPresetListingRequestAssembly.create(),
                new CofferMinecraftSelectedExchangeRuntimeParticipation(
                        CofferMinecraftAdminShopPresetListingInterruptionPressureTest::approvedArbitration,
                        mutationPlan -> finiteExecution(mutationPlan, state, playerSlots, shopSlots, true)),
                new CofferMinecraftAdminShopPresetListingAccountabilityProjection(accountability));

        CofferMinecraftAdminShopPresetListingSubmissionChain.SubmissionResult.Submitted submitted =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingSubmissionChain.SubmissionResult.Submitted.class,
                        chain.submit(tempDir, state, ledger));

        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated runtime =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated.class,
                        submitted.participation());
        assertEquals(Decision.APPROVED, runtime.arbitration().outcome().decision());
        assertEquals(ExecutionStatus.FULL_UNKNOWN, runtime.execution().status());
        assertEquals(MutationExecutionStatus.MUTATION_UNKNOWN, runtime.execution().mutationResults().get(0).status());
        assertEquals(
                MinecraftPlayerInventoryContainer.CONTAINER_UNAVAILABLE,
                runtime.execution().mutationResults().get(0).detail().values().get("reasonCode"));
        assertEquals(Items.EMERALD, playerSlots.get(0).getItem());
        assertEquals(3, playerSlots.get(0).getCount());
        assertEquals(Items.DIAMOND, shopSlots.get(0).getItem());
        assertEquals(1, shopSlots.get(0).getCount());

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));
        assertEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"admin-shop-interruption-1\",\"recordType\":\"CER\",\"stage\":\"fabric_core_approved\",\"seam\":\"fabric_core\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"admin-shop-interruption-2\",\"recordType\":\"CER\",\"stage\":\"fabric_runtime_unknown\",\"seam\":\"fabric_runtime\",\"code\":\"minecraft.container.unavailable\"}"),
                lines);
        assertFalse(lines.stream().anyMatch(line -> line.contains("fabric_runtime_succeeded")));
    }

    private static ExecutionResult finiteExecution(
            MutationPlan mutationPlan,
            CofferMinecraftAdminShopPresetListingConfirmation.ExchangeState state,
            List<ItemStack> playerSlots,
            List<ItemStack> shopSlots,
            boolean unavailableAtRuntime) {
        MinecraftPlayerInventoryContainer playerContainer = new MinecraftPlayerInventoryContainer(
                PLAYER_ID,
                MinecraftPlayerInventoryContainer.Region.HOTBAR,
                playerSlots);
        MinecraftPlayerInventoryContainer shopContainer = unavailableAtRuntime
                ? new MinecraftPlayerInventoryContainer(
                        "shop:" + SHOP_STOCK_ID + ":inventory:hotbar",
                        MinecraftPlayerInventoryContainer.Region.HOTBAR,
                        Optional::empty)
                : new MinecraftPlayerInventoryContainer(
                        SHOP_STOCK_ID,
                        MinecraftPlayerInventoryContainer.Region.HOTBAR,
                        shopSlots);

        MutationApplicationResult result = playerContainer.applyAtomicSwap(
                shopContainer,
                values(state.listing().orElseThrow().acceptedCounterOffer()),
                values(state.listing().orElseThrow().offeredValue()));

        if (result instanceof MutationApplicationResult.Success) {
            return executionResult(mutationPlan, ExecutionStatus.FULL_SUCCESS, MutationExecutionStatus.MUTATION_SUCCEEDED, null);
        }
        if (result instanceof MutationApplicationResult.Failed failed) {
            String reasonCode = MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE.equals(failed.reasonCode())
                    ? SHOP_SUPPLY_UNAVAILABLE
                    : failed.reasonCode();
            return executionResult(mutationPlan, ExecutionStatus.FULL_FAILURE, MutationExecutionStatus.MUTATION_FAILED, reasonCode);
        }
        MutationApplicationResult.Unknown unknown = (MutationApplicationResult.Unknown) result;
        return executionResult(mutationPlan, ExecutionStatus.FULL_UNKNOWN, MutationExecutionStatus.MUTATION_UNKNOWN, unknown.reasonCode());
    }

    private static ArbitrationResult approvedArbitration(ExchangePayload payload) {
        OutcomeId outcomeId = new OutcomeId("admin-shop-interruption-outcome-1");
        MutationPlanId mutationPlanId = new MutationPlanId("admin-shop-interruption-plan-1");
        return new ArbitrationResult(
                new Outcome(outcomeId, payload.payloadId(), Decision.APPROVED, List.of(), mutationPlanId),
                new MutationPlan(
                        mutationPlanId,
                        payload.payloadId(),
                        outcomeId,
                        List.of(new AuthorizedMutation(
                                new MutationRef("admin-shop-interruption-mutation-1"),
                                new MutationRequirementRef("admin-shop-interruption-mutation-requirement-1"),
                                org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority.AUTHORITY_ID,
                                new OpaqueObject(Map.of("shop", true)),
                                new ReferenceSet(java.util.Set.of(), java.util.Set.of())))));
    }

    private static ExecutionResult executionResult(
            MutationPlan mutationPlan,
            ExecutionStatus executionStatus,
            MutationExecutionStatus mutationStatus,
            String reasonCode) {
        AuthorizedMutation mutation = mutationPlan.mutations().get(0);
        OpaqueObject detail = reasonCode == null ? new OpaqueObject(Map.of()) : new OpaqueObject(Map.of("reasonCode", reasonCode));
        return new ExecutionResult(
                new ExecutionResultId("admin-shop-interruption-execution-result-1"),
                new ExecutionPlanId("admin-shop-interruption-execution-plan-1"),
                mutationPlan.mutationPlanId(),
                executionStatus,
                List.of(new MutationExecutionResult(
                        mutation.mutationRef(),
                        mutation.satisfies(),
                        mutation.authority(),
                        mutationStatus,
                        detail)));
    }

    private static TransferableValueSet values(MinecraftItemDescriptor descriptor) {
        return new TransferableValueSet(List.of(descriptor));
    }

    private static CofferMinecraftLifecycleAccountability accountability() {
        AtomicInteger counter = new AtomicInteger();
        return new CofferMinecraftLifecycleAccountability(
                () -> "admin-shop-interruption-" + counter.incrementAndGet(),
                () -> TIMESTAMP);
    }

    private static CofferMinecraftAdminShopPresetListingConfirmation.ExchangeState state(
            CofferMinecraftAdminShopPresetListingConstruction.PresetListing listing,
            CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot snapshot) {
        return new CofferMinecraftAdminShopPresetListingConfirmation.ExchangeState(
                new CofferMinecraftAdminShopPresetListingRequestAssembly.SelectedParticipant(
                        new org.coffer.core.model.id.ActorRef("player:" + PLAYER_ID + ":inventory:hotbar"),
                        new OfferRef("player-shop-offer"),
                        snapshot),
                Optional.of(listing),
                "minecraft-inventory");
    }

    private static CofferMinecraftAdminShopPresetListingConstruction.PresetListing enabledInfiniteListing() {
        return new CofferMinecraftAdminShopPresetListingConstruction.PresetListing(
                "listing-emerald-for-diamond",
                true,
                new CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.InfiniteFaucet("spawn-shop-wall-a"),
                descriptor("minecraft:diamond", 1, null),
                descriptor("minecraft:emerald", 3, null));
    }

    private static CofferMinecraftAdminShopPresetListingConstruction.PresetListing disabledInfiniteListing() {
        return new CofferMinecraftAdminShopPresetListingConstruction.PresetListing(
                "listing-emerald-for-diamond",
                false,
                new CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.InfiniteFaucet("spawn-shop-wall-a"),
                descriptor("minecraft:diamond", 1, null),
                descriptor("minecraft:emerald", 3, null));
    }

    private static CofferMinecraftAdminShopPresetListingConstruction.PresetListing finiteListing() {
        return new CofferMinecraftAdminShopPresetListingConstruction.PresetListing(
                "listing-finite-emerald-for-diamond",
                true,
                new CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.FiniteFaucet("shop:spawn:stock:diamond-a"),
                descriptor("minecraft:diamond", 1, null),
                descriptor("minecraft:emerald", 3, null));
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
                        1),
                Optional.of(descriptor(itemId, quantity, nbtPayload)));
    }

    private static MinecraftItemDescriptor descriptor(String itemId, long quantity, String nbtPayload) {
        return new MinecraftItemDescriptor(itemId, quantity, Optional.ofNullable(nbtPayload));
    }

    private static List<ItemStack> mutableSlots(ItemStack... slots) {
        return new ArrayList<>(List.of(slots));
    }

    private static List<ReasonId> denialReasonIds() {
        List<ReasonId> reasonIds = new ArrayList<>();
        for (int index = 0; index < 8; index++) {
            reasonIds.add(new ReasonId("admin-shop-interruption-reason-" + index));
        }
        return List.copyOf(reasonIds);
    }
}
