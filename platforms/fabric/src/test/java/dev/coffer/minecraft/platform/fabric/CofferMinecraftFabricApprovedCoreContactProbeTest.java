package dev.coffer.minecraft.platform.fabric;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.coffer.core.arbitration.ArbitrationResult;
import org.coffer.core.model.id.MutationPlanId;
import org.coffer.core.model.id.MutationRef;
import org.coffer.core.model.id.MutationRequirementRef;
import org.coffer.core.model.id.OutcomeId;
import org.coffer.core.model.id.PayloadId;
import org.coffer.core.model.outcome.Decision;
import org.coffer.core.model.outcome.Outcome;
import org.coffer.core.model.mutation.AuthorizedMutation;
import org.coffer.core.model.mutation.MutationPlan;
import org.coffer.core.model.request.ExchangePayload;
import org.coffer.core.model.support.OpaqueObject;
import org.coffer.core.model.support.ReferenceSet;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueExchangePayloadConstruction;
import org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CofferMinecraftFabricApprovedCoreContactProbeTest {
    private static final long TIMESTAMP = 1_700_000_000_000L;
    @TempDir
    Path tempDir;

    @Test
    void startupProbeRecordsApprovedCerWithoutRuntimeParticipation() throws IOException {
        AtomicInteger counter = new AtomicInteger();
        CofferMinecraftLifecycleAccountability accountability = new CofferMinecraftLifecycleAccountability(
                () -> "fabric-core-approved-" + counter.incrementAndGet(),
                () -> TIMESTAMP);
        CofferMinecraftFabricApprovedCoreContactProbe probe = new CofferMinecraftFabricApprovedCoreContactProbe(
                TransferableValueExchangePayloadConstruction::constructAtomicSwap,
                CofferMinecraftFabricApprovedCoreContactProbeTest::approvedArbitration,
                accountability);

        accountability.recordServerStarted(tempDir);
        probe.recordStartupProbe(tempDir);

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));

        assertIterableEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"fabric-core-approved-1\",\"recordType\":\"SER\",\"stage\":\"fabric_server_started\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"fabric-core-approved-2\",\"recordType\":\"CER\",\"stage\":\"fabric_core_approved\",\"seam\":\"fabric_core\"}"),
                lines);
        assertFalse(lines.get(1).contains("\"runtime\":"));
        assertFalse(lines.get(1).contains("\"execution\":"));
        assertFalse(lines.get(1).contains("\"mutation\":"));
        assertFalse(lines.get(1).contains("fabric_runtime"));
        assertFalse(lines.get(1).contains("fabric_mutation"));
        assertFalse(lines.get(1).contains("\"code\""));
        assertFalse(lines.get(1).contains(":null"));
    }

    @Test
    void coreApprovalRemainsDistinguishableFromCoreDenial() throws IOException {
        AtomicInteger counter = new AtomicInteger();
        CofferMinecraftLifecycleAccountability accountability = new CofferMinecraftLifecycleAccountability(
                () -> "fabric-core-approved-" + counter.incrementAndGet(),
                () -> TIMESTAMP);

        accountability.recordCoreDenied(tempDir, null);
        accountability.recordCoreApproved(tempDir);

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));

        assertIterableEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"fabric-core-approved-1\",\"recordType\":\"CER\",\"stage\":\"fabric_core_denied\",\"seam\":\"fabric_core\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"fabric-core-approved-2\",\"recordType\":\"CER\",\"stage\":\"fabric_core_approved\",\"seam\":\"fabric_core\"}"),
                lines);
        assertFalse(lines.get(1).contains("fabric_runtime"));
        assertFalse(lines.get(1).contains("fabric_mutation"));
    }

    private static ArbitrationResult approvedArbitration(ExchangePayload payload) {
        OutcomeId outcomeId = new OutcomeId("fabric-core-approved-test-outcome-1");
        MutationPlanId mutationPlanId = new MutationPlanId("fabric-core-approved-test-mutation-plan-1");
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
                                new MutationRef("fabric-core-approved-test-mutation-1"),
                                new MutationRequirementRef("fabric-core-approved-test-mutation-requirement-1"),
                                TransferableValueCoreAuthority.AUTHORITY_ID,
                                new OpaqueObject(java.util.Map.of()),
                                new ReferenceSet(java.util.Set.of(), java.util.Set.of())))));
    }
}
