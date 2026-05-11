package dev.coffer.minecraft.platform.fabric;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.coffer.core.arbitration.CofferCore;
import org.coffer.core.model.id.MutationPlanId;
import org.coffer.core.model.id.OutcomeId;
import org.coffer.core.model.id.ReasonId;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueExchangePayloadConstruction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CofferMinecraftFabricCoreContactProbeTest {
    @TempDir
    Path tempDir;

    @Test
    void startupProbeTouchesCoreAndRecordsDeniedCerWithoutRuntimeParticipation() throws IOException {
        AtomicInteger counter = new AtomicInteger();
        CofferMinecraftLifecycleAccountability accountability = new CofferMinecraftLifecycleAccountability(
                () -> "fabric-core-contact-" + counter.incrementAndGet());
        CofferMinecraftFabricCoreContactProbe probe = new CofferMinecraftFabricCoreContactProbe(
                TransferableValueExchangePayloadConstruction::constructAtomicSwap,
                payload -> CofferCore.arbitrate(
                        payload,
                        ignored -> new org.coffer.core.authority.ResolutionResult.Unresolved(),
                        new OutcomeId("fabric-core-test-outcome-1"),
                        new MutationPlanId("fabric-core-test-mutation-plan-1"),
                        denialReasonIds()),
                accountability);

        accountability.recordServerStarted(tempDir);
        probe.recordStartupProbe(tempDir);

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));

        assertIterableEquals(
                List.of(
                        "{\"interactionId\":\"fabric-core-contact-1\",\"recordType\":\"SER\",\"stage\":\"fabric_server_started\"}",
                        "{\"interactionId\":\"fabric-core-contact-2\",\"recordType\":\"CER\",\"stage\":\"fabric_core_denied\"}"),
                lines);
        assertFalse(lines.get(1).contains("\"runtime\":"));
        assertFalse(lines.get(1).contains("\"execution\":"));
        assertFalse(lines.get(1).contains("\"mutation\":"));
    }

    @Test
    void coreDenialRemainsDistinguishableFromConstructionRefusal() throws IOException {
        AtomicInteger counter = new AtomicInteger();
        CofferMinecraftLifecycleAccountability accountability = new CofferMinecraftLifecycleAccountability(
                () -> "fabric-core-contact-" + counter.incrementAndGet());

        accountability.recordConstructionRefused(tempDir, "MISSING_BINDING_ID");
        accountability.recordCoreDenied(tempDir, null);

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));

        assertIterableEquals(
                List.of(
                        "{\"interactionId\":\"fabric-core-contact-1\",\"recordType\":\"SER\",\"stage\":\"fabric_construction_refused\",\"code\":\"MISSING_BINDING_ID\"}",
                        "{\"interactionId\":\"fabric-core-contact-2\",\"recordType\":\"CER\",\"stage\":\"fabric_core_denied\"}"),
                lines);
    }

    private static List<ReasonId> denialReasonIds() {
        return List.of(
                new ReasonId("fabric-core-test-reason-1"),
                new ReasonId("fabric-core-test-reason-2"),
                new ReasonId("fabric-core-test-reason-3"),
                new ReasonId("fabric-core-test-reason-4"),
                new ReasonId("fabric-core-test-reason-5"),
                new ReasonId("fabric-core-test-reason-6"),
                new ReasonId("fabric-core-test-reason-7"),
                new ReasonId("fabric-core-test-reason-8"));
    }
}
