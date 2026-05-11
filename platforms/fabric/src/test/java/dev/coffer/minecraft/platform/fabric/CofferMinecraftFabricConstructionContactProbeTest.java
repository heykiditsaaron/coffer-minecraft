package dev.coffer.minecraft.platform.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueExchangePayloadConstruction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CofferMinecraftFabricConstructionContactProbeTest {
    @TempDir
    Path tempDir;

    @Test
    void startupProbeTouchesPavedConstructionAndRecordsRefusalWithoutRuntimeParticipation() throws IOException {
        AtomicInteger counter = new AtomicInteger();
        CofferMinecraftLifecycleAccountability accountability = new CofferMinecraftLifecycleAccountability(
                () -> "fabric-contact-" + counter.incrementAndGet());
        CofferMinecraftFabricConstructionContactProbe probe = new CofferMinecraftFabricConstructionContactProbe(
                TransferableValueExchangePayloadConstruction::constructAtomicSwap,
                accountability);

        accountability.recordServerStarted(tempDir);
        probe.recordStartupProbe(tempDir);

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));

        assertIterableEquals(
                List.of(
                        "{\"interactionId\":\"fabric-contact-1\",\"recordType\":\"SER\",\"stage\":\"fabric_server_started\"}",
                        "{\"interactionId\":\"fabric-contact-2\",\"recordType\":\"SER\",\"stage\":\"fabric_construction_refused\",\"code\":\"MISSING_BINDING_ID\"}"),
                lines);
        assertFalse(lines.get(1).contains("\"runtime\":"));
        assertFalse(lines.get(1).contains("\"timeline\":"));
        assertFalse(lines.get(1).contains("\"explanation\""));
    }

    @Test
    void startupProbeDeepensParticipationWithoutFabricatingCoreOrRuntime() throws IOException {
        AtomicInteger counter = new AtomicInteger();
        CofferMinecraftLifecycleAccountability accountability = new CofferMinecraftLifecycleAccountability(
                () -> "fabric-contact-" + counter.incrementAndGet());
        CofferMinecraftFabricConstructionContactProbe probe = new CofferMinecraftFabricConstructionContactProbe(
                TransferableValueExchangePayloadConstruction::constructAtomicSwap,
                accountability);

        probe.recordStartupProbe(tempDir);

        String line = Files.readAllLines(accountability.logPath(tempDir)).get(0);

        assertEquals(
                "{\"interactionId\":\"fabric-contact-1\",\"recordType\":\"SER\",\"stage\":\"fabric_construction_refused\",\"code\":\"MISSING_BINDING_ID\"}",
                line);
        assertFalse(line.contains("\"recordType\":\"CER\""));
        assertFalse(line.contains("\"runtime\":"));
        assertFalse(line.contains("\"execution\":"));
        assertFalse(line.contains("\"mutation\":"));
    }
}
