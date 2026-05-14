package dev.coffer.minecraft.platform.fabric;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.coffer.core.model.id.MutationRef;
import org.coffer.core.model.id.MutationRequirementRef;
import org.coffer.core.model.mutation.AuthorizedMutation;
import org.coffer.core.model.mutation.MutationPlan;
import org.coffer.core.model.outcome.Decision;
import org.coffer.core.model.outcome.Outcome;
import org.coffer.core.model.support.OpaqueObject;
import org.coffer.core.model.support.ReferenceSet;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueExchangePayloadConstruction;
import org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CofferMinecraftFabricRuntimeContactProbeTest {
    private static final long TIMESTAMP = 1_700_000_000_000L;
    @TempDir
    Path tempDir;

    @Test
    void startupProbeReachesRuntimeAfterCoreApprovalWithoutMutationParticipation() throws IOException {
        AtomicInteger counter = new AtomicInteger();
        CofferMinecraftLifecycleAccountability accountability = new CofferMinecraftLifecycleAccountability(
                () -> "fabric-runtime-" + counter.incrementAndGet(),
                () -> TIMESTAMP);
        CofferMinecraftFabricApprovedCoreContactProbe coreProbe = new CofferMinecraftFabricApprovedCoreContactProbe(
                TransferableValueExchangePayloadConstruction::constructAtomicSwap,
                CofferMinecraftFabricRuntimeContactProbeTest::approvedArbitration,
                accountability);
        CofferMinecraftFabricRuntimeContactProbe runtimeProbe = CofferMinecraftFabricRuntimeContactProbe.create(accountability);

        accountability.recordServerStarted(tempDir);
        coreProbe.recordStartupProbe(tempDir);
        runtimeProbe.recordStartupProbe(tempDir);

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));

        assertIterableEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"fabric-runtime-1\",\"recordType\":\"SER\",\"stage\":\"fabric_server_started\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"fabric-runtime-2\",\"recordType\":\"CER\",\"stage\":\"fabric_core_approved\",\"seam\":\"fabric_core\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"fabric-runtime-3\",\"recordType\":\"CER\",\"stage\":\"fabric_runtime_unknown\",\"seam\":\"fabric_runtime\",\"code\":\"MALFORMED_RUNTIME_DESCRIPTOR\"}"),
                lines);
        assertFalse(lines.get(2).contains("\"execution\":"));
        assertFalse(lines.get(2).contains("\"mutation\":"));
        assertFalse(lines.get(2).contains("\"player\""));
        assertFalse(lines.get(2).contains("\"inventory\""));
        assertFalse(lines.get(0).contains("\"seam\""));
        assertFalse(lines.get(1).contains("\"seam\":\"fabric_runtime\""));
        assertFalse(lines.get(1).contains("\"code\""));
    }

    @Test
    void runtimeUnknownRemainsDistinctFromCoreOnlyApproval() throws IOException {
        AtomicInteger counter = new AtomicInteger();
        CofferMinecraftLifecycleAccountability accountability = new CofferMinecraftLifecycleAccountability(
                () -> "fabric-runtime-" + counter.incrementAndGet(),
                () -> TIMESTAMP);
        CofferMinecraftFabricApprovedCoreContactProbe coreProbe = new CofferMinecraftFabricApprovedCoreContactProbe(
                TransferableValueExchangePayloadConstruction::constructAtomicSwap,
                CofferMinecraftFabricRuntimeContactProbeTest::approvedArbitration,
                accountability);
        CofferMinecraftFabricRuntimeContactProbe runtimeProbe = CofferMinecraftFabricRuntimeContactProbe.create(accountability);

        coreProbe.recordStartupProbe(tempDir);
        runtimeProbe.recordStartupProbe(tempDir);

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));

        assertIterableEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"fabric-runtime-1\",\"recordType\":\"CER\",\"stage\":\"fabric_core_approved\",\"seam\":\"fabric_core\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"fabric-runtime-2\",\"recordType\":\"CER\",\"stage\":\"fabric_runtime_unknown\",\"seam\":\"fabric_runtime\",\"code\":\"MALFORMED_RUNTIME_DESCRIPTOR\"}"),
                lines);
        assertFalse(lines.get(0).contains("fabric_runtime"));
        assertFalse(lines.get(1).contains("fabric_core"));
    }

    @Test
    void runtimeSeamIsAbsentFromNonRuntimeContactRecords() throws IOException {
        AtomicInteger counter = new AtomicInteger();
        CofferMinecraftLifecycleAccountability accountability = new CofferMinecraftLifecycleAccountability(
                () -> "fabric-runtime-" + counter.incrementAndGet(),
                () -> TIMESTAMP);
        CofferMinecraftFabricApprovedCoreContactProbe coreProbe = new CofferMinecraftFabricApprovedCoreContactProbe(
                TransferableValueExchangePayloadConstruction::constructAtomicSwap,
                CofferMinecraftFabricRuntimeContactProbeTest::approvedArbitration,
                accountability);

        accountability.recordServerStarted(tempDir);
        accountability.recordConstructionRefused(tempDir, "MISSING_BINDING_ID");
        accountability.recordCoreDenied(tempDir, "VALUE_NOT_REMOVABLE");
        coreProbe.recordStartupProbe(tempDir);

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));

        assertIterableEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"fabric-runtime-1\",\"recordType\":\"SER\",\"stage\":\"fabric_server_started\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"fabric-runtime-2\",\"recordType\":\"SER\",\"stage\":\"fabric_construction_refused\",\"code\":\"MISSING_BINDING_ID\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"fabric-runtime-3\",\"recordType\":\"CER\",\"stage\":\"fabric_core_denied\",\"seam\":\"fabric_core\",\"code\":\"VALUE_NOT_REMOVABLE\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"fabric-runtime-4\",\"recordType\":\"CER\",\"stage\":\"fabric_core_approved\",\"seam\":\"fabric_core\"}"),
                lines);
        assertFalse(lines.get(0).contains("fabric_runtime"));
        assertFalse(lines.get(1).contains("fabric_runtime"));
        assertFalse(lines.get(2).contains("fabric_runtime"));
        assertFalse(lines.get(3).contains("fabric_runtime"));
        assertFalse(lines.get(0).contains("\"seam\""));
        assertFalse(lines.get(1).contains("\"seam\""));
        assertFalse(lines.get(2).contains("\"seam\":\"fabric_runtime\""));
        assertFalse(lines.get(3).contains("\"seam\":\"fabric_runtime\""));
    }

    private static org.coffer.core.arbitration.ArbitrationResult approvedArbitration(
            org.coffer.core.model.request.ExchangePayload payload) {
        org.coffer.core.model.id.OutcomeId outcomeId = new org.coffer.core.model.id.OutcomeId("fabric-runtime-test-outcome-1");
        org.coffer.core.model.id.MutationPlanId mutationPlanId =
                new org.coffer.core.model.id.MutationPlanId("fabric-runtime-test-mutation-plan-1");
        return new org.coffer.core.arbitration.ArbitrationResult(
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
                                new MutationRef("fabric-runtime-test-mutation-1"),
                                new MutationRequirementRef("fabric-runtime-test-mutation-requirement-1"),
                                TransferableValueCoreAuthority.AUTHORITY_ID,
                                new OpaqueObject(java.util.Map.of()),
                                new ReferenceSet(java.util.Set.of(), java.util.Set.of())))));
    }
}
