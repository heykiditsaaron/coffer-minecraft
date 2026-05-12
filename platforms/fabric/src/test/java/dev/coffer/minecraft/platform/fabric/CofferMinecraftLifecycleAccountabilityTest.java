package dev.coffer.minecraft.platform.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CofferMinecraftLifecycleAccountabilityTest {
    private static final long TIMESTAMP = 1_700_000_000_000L;
    @TempDir
    Path tempDir;

    @Test
    void lifecycleRecordsAppendUnderLogsCoffer() throws IOException {
        CofferMinecraftLifecycleAccountability accountability =
                new CofferMinecraftLifecycleAccountability(() -> "unused", () -> TIMESTAMP);
        Path target = accountability.logPath(tempDir);

        assertEquals(tempDir.resolve("logs").resolve("coffer").resolve("fabric-lifecycle.jsonl"), target);
    }

    @Test
    void startupAndShutdownEmitMinimalSerLinesInAppendOrder() throws IOException {
        java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger();
        CofferMinecraftLifecycleAccountability accountability = new CofferMinecraftLifecycleAccountability(
                () -> "lifecycle-" + counter.incrementAndGet(),
                () -> TIMESTAMP);

        accountability.recordServerStarted(tempDir);
        accountability.recordServerStopped(tempDir);

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));

        assertIterableEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"lifecycle-1\",\"recordType\":\"SER\",\"stage\":\"fabric_server_started\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"lifecycle-2\",\"recordType\":\"SER\",\"stage\":\"fabric_server_stopped\"}"),
                lines);
    }

    @Test
    void lifecycleLinesPreserveOmissionAndLeftToRightReadability() throws IOException {
        CofferMinecraftLifecycleAccountability accountability =
                new CofferMinecraftLifecycleAccountability(() -> "lifecycle-readable", () -> TIMESTAMP);

        accountability.recordServerStarted(tempDir);

        String line = Files.readAllLines(accountability.logPath(tempDir)).get(0);

        assertEquals(
                "{\"timestamp\":1700000000000,\"interactionId\":\"lifecycle-readable\",\"recordType\":\"SER\",\"stage\":\"fabric_server_started\"}",
                line);
        assertTrue(line.startsWith("{\"timestamp\":1700000000000"));
        assertTrue(line.indexOf("\"timestamp\"") < line.indexOf("\"interactionId\""));
        assertTrue(line.indexOf("\"interactionId\"") < line.indexOf("\"recordType\""));
        assertTrue(line.indexOf("\"recordType\"") < line.indexOf("\"stage\""));
        assertFalse(line.contains("\"code\""));
        assertFalse(line.contains("\"runtime\":"));
        assertFalse(line.contains("\"timeline\":"));
        assertFalse(line.contains("\"explanation\""));
        assertFalse(line.contains(":null"));
    }

    @Test
    void constructionRefusalLineRemainsMinimalAndDistinguishableFromLifecycle() throws IOException {
        java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger();
        CofferMinecraftLifecycleAccountability accountability = new CofferMinecraftLifecycleAccountability(
                () -> "lifecycle-" + counter.incrementAndGet(),
                () -> TIMESTAMP);

        accountability.recordServerStarted(tempDir);
        accountability.recordConstructionRefused(tempDir, "MISSING_BINDING_ID");

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));

        assertIterableEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"lifecycle-1\",\"recordType\":\"SER\",\"stage\":\"fabric_server_started\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"lifecycle-2\",\"recordType\":\"SER\",\"stage\":\"fabric_construction_refused\",\"code\":\"MISSING_BINDING_ID\"}"),
                lines);
        assertFalse(lines.get(1).contains("\"runtime\":"));
        assertFalse(lines.get(1).contains("\"timeline\":"));
        assertFalse(lines.get(1).contains("\"explanation\""));
    }
}
