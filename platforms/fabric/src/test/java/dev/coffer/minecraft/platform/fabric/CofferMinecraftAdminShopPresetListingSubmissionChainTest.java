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
import org.coffer.core.arbitration.CofferCore;
import org.coffer.core.arbitration.ArbitrationResult;
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

class CofferMinecraftAdminShopPresetListingSubmissionChainTest {
    private static final long TIMESTAMP = 1_700_000_000_000L;
    private static final UUID PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000001181");

    @TempDir
    Path tempDir;

    @Test
    void unconfirmedListingExchangeCannotSubmit() throws IOException {
        AtomicInteger runtimeCalls = new AtomicInteger();
        CofferMinecraftAdminShopPresetListingSubmissionChain chain = new CofferMinecraftAdminShopPresetListingSubmissionChain(
                new CofferMinecraftAdminShopPresetListingConfirmation(),
                CofferMinecraftAdminShopPresetListingRequestAssembly.create(),
                deniedParticipation(runtimeCalls),
                new CofferMinecraftAdminShopPresetListingAccountabilityProjection(accountability()));

        CofferMinecraftAdminShopPresetListingSubmissionChain.SubmissionResult.NotSubmitted notSubmitted =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingSubmissionChain.SubmissionResult.NotSubmitted.class,
                        chain.submit(tempDir, state(infiniteListing(), selectedSnapshot("minecraft:emerald", 3, null)),
                                CofferMinecraftAdminShopPresetListingConfirmation.ConfirmationLedger.empty()));

        assertEquals(CofferMinecraftAdminShopPresetListingConfirmation.PLAYER_NOT_CONFIRMED, notSubmitted.reasonCode());
        assertEquals(0, runtimeCalls.get());
        assertTrue(Files.notExists(accountability().logPath(tempDir)));
    }

    @Test
    void confirmedConcreteListingExchangeCanSubmitThroughApprovalChainWithoutCounterfeitSuccess() throws IOException {
        AtomicReference<MutationPlan> seenPlan = new AtomicReference<>();
        CofferMinecraftAdminShopPresetListingConfirmation confirmation = new CofferMinecraftAdminShopPresetListingConfirmation();
        CofferMinecraftLifecycleAccountability accountability = accountability();
        CofferMinecraftAdminShopPresetListingSubmissionChain chain = new CofferMinecraftAdminShopPresetListingSubmissionChain(
                confirmation,
                CofferMinecraftAdminShopPresetListingRequestAssembly.create(),
                new CofferMinecraftSelectedExchangeRuntimeParticipation(
                        CofferMinecraftAdminShopPresetListingSubmissionChainTest::approvedArbitration,
                        mutationPlan -> {
                            seenPlan.set(mutationPlan);
                            return runtimeResult(mutationPlan, ExecutionStatus.FULL_UNKNOWN, MutationExecutionStatus.MUTATION_UNKNOWN, "SHOP_RUNTIME_UNAVAILABLE");
                        }),
                new CofferMinecraftAdminShopPresetListingAccountabilityProjection(accountability));
        CofferMinecraftAdminShopPresetListingConfirmation.ExchangeState state =
                state(infiniteListing(), selectedSnapshot("minecraft:emerald", 3, null));
        CofferMinecraftAdminShopPresetListingConfirmation.ConfirmationLedger ledger =
                confirmation.confirm(state, state.player().actorRef(), CofferMinecraftAdminShopPresetListingConfirmation.ConfirmationLedger.empty());

        CofferMinecraftAdminShopPresetListingSubmissionChain.SubmissionResult.Submitted submitted =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingSubmissionChain.SubmissionResult.Submitted.class,
                        chain.submit(tempDir, state, ledger));

        assertEquals("listing-emerald-for-diamond", submitted.prepared().exchange().listingId());
        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated runtime =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated.class,
                        submitted.participation());
        assertEquals(Decision.APPROVED, runtime.arbitration().outcome().decision());
        assertSame(runtime.arbitration().mutationPlan(), seenPlan.get());
        assertEquals(ExecutionStatus.FULL_UNKNOWN, runtime.execution().status());

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));
        assertEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"admin-shop-submission-1\",\"recordType\":\"CER\",\"stage\":\"fabric_core_approved\",\"seam\":\"fabric_core\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"admin-shop-submission-2\",\"recordType\":\"CER\",\"stage\":\"fabric_runtime_unknown\",\"seam\":\"fabric_runtime\",\"code\":\"SHOP_RUNTIME_UNAVAILABLE\"}"),
                lines);
        assertFalse(lines.stream().anyMatch(line -> line.contains("fabric_runtime_succeeded")));
    }

    @Test
    void coreDenialRemainsCoreDenialAndDoesNotProjectRuntimeParticipation() throws IOException {
        AtomicInteger runtimeCalls = new AtomicInteger();
        CofferMinecraftAdminShopPresetListingConfirmation confirmation = new CofferMinecraftAdminShopPresetListingConfirmation();
        CofferMinecraftLifecycleAccountability accountability = accountability();
        CofferMinecraftAdminShopPresetListingSubmissionChain chain = new CofferMinecraftAdminShopPresetListingSubmissionChain(
                confirmation,
                CofferMinecraftAdminShopPresetListingRequestAssembly.create(),
                deniedParticipation(runtimeCalls),
                new CofferMinecraftAdminShopPresetListingAccountabilityProjection(accountability));
        CofferMinecraftAdminShopPresetListingConfirmation.ExchangeState state =
                state(finiteListing(), selectedSnapshot("minecraft:emerald", 3, null));
        CofferMinecraftAdminShopPresetListingConfirmation.ConfirmationLedger ledger =
                confirmation.confirm(state, state.player().actorRef(), CofferMinecraftAdminShopPresetListingConfirmation.ConfirmationLedger.empty());

        CofferMinecraftAdminShopPresetListingSubmissionChain.SubmissionResult.Submitted submitted =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingSubmissionChain.SubmissionResult.Submitted.class,
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
                        "{\"timestamp\":1700000000000,\"interactionId\":\"admin-shop-submission-1\",\"recordType\":\"CER\",\"stage\":\"fabric_core_denied\",\"seam\":\"fabric_core\"}"),
                lines);
    }

    private static CofferMinecraftSelectedExchangeRuntimeParticipation deniedParticipation(AtomicInteger runtimeCalls) {
        return new CofferMinecraftSelectedExchangeRuntimeParticipation(
                payload -> CofferCore.arbitrate(
                        payload,
                        unresolved -> new org.coffer.core.authority.ResolutionResult.Unresolved(),
                        new OutcomeId("admin-shop-submission-denied-outcome-1"),
                        new MutationPlanId("admin-shop-submission-denied-plan-1"),
                        denialReasonIds()),
                mutationPlan -> {
                    runtimeCalls.incrementAndGet();
                    return runtimeResult(mutationPlan, ExecutionStatus.FULL_UNKNOWN, MutationExecutionStatus.MUTATION_UNKNOWN, "UNEXPECTED_RUNTIME");
                });
    }

    private static CofferMinecraftLifecycleAccountability accountability() {
        AtomicInteger counter = new AtomicInteger();
        return new CofferMinecraftLifecycleAccountability(
                () -> "admin-shop-submission-" + counter.incrementAndGet(),
                () -> TIMESTAMP);
    }

    private static ArbitrationResult approvedArbitration(ExchangePayload payload) {
        OutcomeId outcomeId = new OutcomeId("admin-shop-submission-approved-outcome-1");
        MutationPlanId mutationPlanId = new MutationPlanId("admin-shop-submission-approved-plan-1");
        return new ArbitrationResult(
                new Outcome(outcomeId, payload.payloadId(), Decision.APPROVED, List.of(), mutationPlanId),
                new MutationPlan(
                        mutationPlanId,
                        payload.payloadId(),
                        outcomeId,
                        List.of(new AuthorizedMutation(
                                new MutationRef("admin-shop-submission-mutation-1"),
                                new MutationRequirementRef("admin-shop-submission-mutation-requirement-1"),
                                org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority.AUTHORITY_ID,
                                new OpaqueObject(Map.of("shop", true)),
                                new ReferenceSet(java.util.Set.of(), java.util.Set.of())))));
    }

    private static ExecutionResult runtimeResult(
            MutationPlan mutationPlan,
            ExecutionStatus executionStatus,
            MutationExecutionStatus mutationStatus,
            String reasonCode) {
        AuthorizedMutation mutation = mutationPlan.mutations().get(0);
        return new ExecutionResult(
                new ExecutionResultId("admin-shop-submission-execution-result-1"),
                new ExecutionPlanId("admin-shop-submission-execution-plan-1"),
                mutationPlan.mutationPlanId(),
                executionStatus,
                List.of(new MutationExecutionResult(
                        mutation.mutationRef(),
                        mutation.satisfies(),
                        mutation.authority(),
                        mutationStatus,
                        new OpaqueObject(Map.of("reasonCode", reasonCode)))));
    }

    private static CofferMinecraftAdminShopPresetListingConfirmation.ExchangeState state(
            CofferMinecraftAdminShopPresetListingConstruction.PresetListing listing,
            CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot snapshot) {
        return new CofferMinecraftAdminShopPresetListingConfirmation.ExchangeState(
                new CofferMinecraftAdminShopPresetListingRequestAssembly.SelectedParticipant(
                        new ActorRef("player:" + PLAYER_ID + ":inventory:hotbar"),
                        new OfferRef("player-shop-offer"),
                        snapshot),
                Optional.of(listing),
                "minecraft-inventory");
    }

    private static CofferMinecraftAdminShopPresetListingConstruction.PresetListing infiniteListing() {
        return new CofferMinecraftAdminShopPresetListingConstruction.PresetListing(
                "listing-emerald-for-diamond",
                true,
                new CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.InfiniteFaucet("spawn-shop-wall-a"),
                descriptor("minecraft:diamond", 1, null),
                descriptor("minecraft:emerald", 3, null));
    }

    private static CofferMinecraftAdminShopPresetListingConstruction.PresetListing finiteListing() {
        return new CofferMinecraftAdminShopPresetListingConstruction.PresetListing(
                "listing-finite-emerald-for-diamond",
                true,
                new CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.FiniteFaucet("shop:spawn:stock:diamond-a"),
                descriptor("minecraft:diamond", 1, "{Damage:2}"),
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
                        dev.coffer.minecraft.bindings.inventory.MinecraftPlayerInventoryContainer.Region.HOTBAR,
                        3),
                Optional.of(descriptor(itemId, quantity, nbtPayload)));
    }

    private static dev.coffer.minecraft.bindings.inventory.MinecraftItemDescriptor descriptor(
            String itemId,
            long quantity,
            String nbtPayload) {
        return new dev.coffer.minecraft.bindings.inventory.MinecraftItemDescriptor(
                itemId,
                quantity,
                Optional.ofNullable(nbtPayload));
    }

    private static List<ReasonId> denialReasonIds() {
        java.util.ArrayList<ReasonId> reasonIds = new java.util.ArrayList<>();
        for (int index = 0; index < 8; index++) {
            reasonIds.add(new ReasonId("admin-shop-submission-reason-" + index));
        }
        return List.copyOf(reasonIds);
    }
}
