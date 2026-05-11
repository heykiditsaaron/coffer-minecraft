package dev.coffer.minecraft.bindings.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coffer.minecraft.testsupport.ghostadapter.GhostAdapterAccountabilityJsonlEmitter;
import dev.coffer.minecraft.testsupport.ghostadapter.GhostAdapterAccountabilityProjection;
import dev.coffer.minecraft.testsupport.ghostadapter.GhostAdapterExchangeHarness.GhostAdapterProjection;
import dev.coffer.minecraft.testsupport.ghostadapter.GhostAdapterExchangeHarness.ProjectionKind;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GhostAdapterAccountabilityJsonlEmitterTest {
    @TempDir
    Path tempDir;

    @Test
    void appendsOneJsonRecordPerLineInChronologicalOrder() throws IOException {
        Path target = GhostAdapterAccountabilityJsonlEmitter.cofferLogPath(tempDir.resolve("logs"), "ghost.jsonl");

        GhostAdapterAccountabilityJsonlEmitter.append(target, records("interaction-1", ProjectionKind.CORE_DENIED, "minecraft.value.not_removable"));
        GhostAdapterAccountabilityJsonlEmitter.append(target, records("interaction-2", ProjectionKind.RUNTIME_UNKNOWN, "MALFORMED_RUNTIME_DESCRIPTOR"));

        List<String> lines = Files.readAllLines(target);

        assertEquals(4, lines.size());
        assertTrue(lines.get(0).contains("\"interactionId\":\"interaction-1\""));
        assertTrue(lines.get(1).contains("\"interactionId\":\"interaction-1\""));
        assertTrue(lines.get(2).contains("\"interactionId\":\"interaction-2\""));
        assertTrue(lines.get(3).contains("\"interactionId\":\"interaction-2\""));
        assertEquals("{\"interactionId\":\"interaction-1\",\"recordType\":\"SER\",\"stage\":\"captured\"}", lines.get(0));
        assertEquals(
                "{\"interactionId\":\"interaction-1\",\"recordType\":\"CER\",\"stage\":\"core_denied\",\"code\":\"minecraft.value.not_removable\"}",
                lines.get(1));
        assertEquals(
                "{\"interactionId\":\"interaction-2\",\"recordType\":\"CER\",\"stage\":\"runtime_unknown\",\"code\":\"MALFORMED_RUNTIME_DESCRIPTOR\"}",
                lines.get(3));
    }

    @Test
    void omissionSurvivesSerializationWithoutPlaceholderFields() throws IOException {
        Path target = GhostAdapterAccountabilityJsonlEmitter.cofferLogPath(tempDir.resolve("logs"), "ghost.jsonl");

        GhostAdapterAccountabilityJsonlEmitter.append(target, records("interaction-3", ProjectionKind.RUNTIME_SUCCESS, null));

        List<String> lines = Files.readAllLines(target);

        assertEquals(2, lines.size());
        assertFalse(lines.get(0).contains("\"code\""));
        assertFalse(lines.get(1).contains("\"code\""));
        assertFalse(lines.get(0).contains("\"runtime\":"));
        assertFalse(lines.get(1).contains("\"runtime\":"));
        assertFalse(lines.get(0).contains("explanation"));
        assertFalse(lines.get(1).contains("explanation"));
    }

    @Test
    void serializationPreservesCanonicalIdentityAcrossInteractionRecords() throws IOException {
        Path target = GhostAdapterAccountabilityJsonlEmitter.cofferLogPath(tempDir.resolve("logs"), "ghost.jsonl");

        GhostAdapterAccountabilityJsonlEmitter.append(target, records("interaction-4", ProjectionKind.RUNTIME_FAILURE, "minecraft.value.not_removable"));

        List<Map<String, Object>> records = GhostAdapterAccountabilityProjection.toJsonlRecords(
                "interaction-4",
                projection(ProjectionKind.RUNTIME_FAILURE, "minecraft.value.not_removable"));
        List<String> lines = Files.readAllLines(target);

        assertEquals(records.size(), lines.size());
        assertIterableEquals(
                List.of("interaction-4", "interaction-4"),
                lines.stream().map(GhostAdapterAccountabilityJsonlEmitterTest::extractInteractionId).toList());
    }

    @Test
    void cofferLogPathPlacesJsonlUnderLogsCoffer() {
        Path target = GhostAdapterAccountabilityJsonlEmitter.cofferLogPath(Path.of("/tmp/platform-logs"), "ghost.jsonl");

        assertEquals(Path.of("/tmp/platform-logs/coffer/ghost.jsonl"), target);
    }

    private static List<Map<String, Object>> records(
            String interactionId,
            ProjectionKind kind,
            String reasonCode) {
        return GhostAdapterAccountabilityProjection.toJsonlRecords(
                interactionId,
                projection(kind, reasonCode));
    }

    private static GhostAdapterProjection projection(ProjectionKind kind, String reasonCode) {
        return new GhostAdapterProjection(kind, reasonCode, null, null, null);
    }

    private static String extractInteractionId(String line) {
        String prefix = "\"interactionId\":\"";
        int start = line.indexOf(prefix) + prefix.length();
        int end = line.indexOf('"', start);
        return line.substring(start, end);
    }
}
