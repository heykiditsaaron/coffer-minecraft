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

    @Test
    void mixedInteractionStreamRemainsChronologicallyReconstructable() throws IOException {
        Path target = GhostAdapterAccountabilityJsonlEmitter.cofferLogPath(tempDir.resolve("logs"), "ghost.jsonl");

        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                records("interaction-10", ProjectionKind.CORE_DENIED, "minecraft.value.not_removable"));
        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                records("interaction-11", ProjectionKind.RUNTIME_SUCCESS, null));
        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                records("interaction-12", ProjectionKind.RUNTIME_UNKNOWN, "MALFORMED_RUNTIME_DESCRIPTOR"));
        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                GhostAdapterAccountabilityProjection.toJsonlRecords(
                        "interaction-13",
                        constructionRefusedProjection()));

        List<String> lines = Files.readAllLines(target);

        assertEquals(7, lines.size());
        assertIterableEquals(
                List.of(
                        "interaction-10",
                        "interaction-10",
                        "interaction-11",
                        "interaction-11",
                        "interaction-12",
                        "interaction-12",
                        "interaction-13"),
                lines.stream().map(GhostAdapterAccountabilityJsonlEmitterTest::extractInteractionId).toList());
        assertIterableEquals(
                List.of(
                        "captured",
                        "core_denied",
                        "captured",
                        "runtime_succeeded",
                        "captured",
                        "runtime_unknown",
                        "construction_refused"),
                lines.stream().map(GhostAdapterAccountabilityJsonlEmitterTest::extractStage).toList());
    }

    @Test
    void repeatedAppendPressurePreservesLineageCoherenceWithoutNestedTimelines() throws IOException {
        Path target = GhostAdapterAccountabilityJsonlEmitter.cofferLogPath(tempDir.resolve("logs"), "ghost.jsonl");

        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                records("interaction-20", ProjectionKind.RUNTIME_FAILURE, "minecraft.value.not_removable"));
        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                records("interaction-21", ProjectionKind.RUNTIME_UNKNOWN, "minecraft.container.unavailable"));
        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                records("interaction-22", ProjectionKind.RUNTIME_SUCCESS, null));

        List<String> lines = Files.readAllLines(target);

        assertEquals(6, lines.size());
        assertLineage(lines, 0, "interaction-20", "captured", "runtime_failed");
        assertLineage(lines, 2, "interaction-21", "captured", "runtime_unknown");
        assertLineage(lines, 4, "interaction-22", "captured", "runtime_succeeded");
        assertTrue(lines.stream().noneMatch(line -> line.contains("\"timeline\":")));
        assertTrue(lines.stream().noneMatch(line -> line.contains("\"events\":")));
        assertTrue(lines.stream().noneMatch(line -> line.contains("\"history\":")));
    }

    @Test
    void interleavedLineagesRemainReconstructableFromIdentityAndAppendOrder() throws IOException {
        Path target = GhostAdapterAccountabilityJsonlEmitter.cofferLogPath(tempDir.resolve("logs"), "ghost.jsonl");
        List<Map<String, Object>> failureRecords =
                records("interaction-30", ProjectionKind.RUNTIME_FAILURE, "minecraft.value.not_removable");
        List<Map<String, Object>> unknownRecords =
                records("interaction-31", ProjectionKind.RUNTIME_UNKNOWN, "MALFORMED_RUNTIME_DESCRIPTOR");
        List<Map<String, Object>> successRecords =
                records("interaction-32", ProjectionKind.RUNTIME_SUCCESS, null);

        GhostAdapterAccountabilityJsonlEmitter.append(target, List.of(failureRecords.get(0)));
        GhostAdapterAccountabilityJsonlEmitter.append(target, List.of(unknownRecords.get(0)));
        GhostAdapterAccountabilityJsonlEmitter.append(target, List.of(successRecords.get(0)));
        GhostAdapterAccountabilityJsonlEmitter.append(target, List.of(failureRecords.get(1)));
        GhostAdapterAccountabilityJsonlEmitter.append(target, List.of(unknownRecords.get(1)));
        GhostAdapterAccountabilityJsonlEmitter.append(target, List.of(successRecords.get(1)));

        List<String> lines = Files.readAllLines(target);

        assertEquals(6, lines.size());
        assertIterableEquals(
                List.of(
                        "interaction-30",
                        "interaction-31",
                        "interaction-32",
                        "interaction-30",
                        "interaction-31",
                        "interaction-32"),
                lines.stream().map(GhostAdapterAccountabilityJsonlEmitterTest::extractInteractionId).toList());
        assertIterableEquals(
                List.of(
                        "captured",
                        "captured",
                        "captured",
                        "runtime_failed",
                        "runtime_unknown",
                        "runtime_succeeded"),
                lines.stream().map(GhostAdapterAccountabilityJsonlEmitterTest::extractStage).toList());
        assertFalse(lines.get(0).contains("\"code\""));
        assertFalse(lines.get(1).contains("\"code\""));
        assertFalse(lines.get(2).contains("\"code\""));
        assertTrue(lines.get(3).contains("\"code\":\"minecraft.value.not_removable\""));
        assertTrue(lines.get(4).contains("\"code\":\"MALFORMED_RUNTIME_DESCRIPTOR\""));
        assertFalse(lines.get(5).contains("\"code\""));
    }

    @Test
    void denseMixedStreamPreservesReadabilityWithoutSchemaInflation() throws IOException {
        Path target = GhostAdapterAccountabilityJsonlEmitter.cofferLogPath(tempDir.resolve("logs"), "ghost.jsonl");

        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                GhostAdapterAccountabilityProjection.toJsonlRecords("interaction-40", constructionRefusedProjection()));
        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                records("interaction-41", ProjectionKind.CORE_DENIED, "minecraft.value.not_removable"));
        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                records("interaction-42", ProjectionKind.RUNTIME_UNKNOWN, "minecraft.container.unavailable"));
        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                records("interaction-43", ProjectionKind.RUNTIME_UNKNOWN, "MALFORMED_RUNTIME_DESCRIPTOR"));
        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                records("interaction-44", ProjectionKind.RUNTIME_SUCCESS, null));

        List<String> lines = Files.readAllLines(target);

        assertEquals(9, lines.size());
        assertIterableEquals(
                List.of(
                        "interaction-40",
                        "interaction-41",
                        "interaction-41",
                        "interaction-42",
                        "interaction-42",
                        "interaction-43",
                        "interaction-43",
                        "interaction-44",
                        "interaction-44"),
                lines.stream().map(GhostAdapterAccountabilityJsonlEmitterTest::extractInteractionId).toList());
        assertTrue(lines.stream().noneMatch(line -> line.contains("\"sequence\"")));
        assertTrue(lines.stream().noneMatch(line -> line.contains("\"timeline\"")));
        assertTrue(lines.stream().noneMatch(line -> line.contains("\"history\"")));
        assertTrue(lines.stream().noneMatch(line -> line.contains("\"participation\"")));
        assertTrue(lines.stream().noneMatch(line -> line.contains("\"runtime\":")));
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

    private static GhostAdapterProjection constructionRefusedProjection() {
        return new GhostAdapterProjection(
                ProjectionKind.CONSTRUCTION_REFUSED,
                null,
                new org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueConstructionRefusal(
                        org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueConstructionRefusalReason.MISSING_BINDING_ID,
                        new org.coffer.core.model.support.OpaqueObject(Map.of())),
                null,
                null);
    }

    private static String extractInteractionId(String line) {
        String prefix = "\"interactionId\":\"";
        int start = line.indexOf(prefix) + prefix.length();
        int end = line.indexOf('"', start);
        return line.substring(start, end);
    }

    private static String extractStage(String line) {
        String prefix = "\"stage\":\"";
        int start = line.indexOf(prefix) + prefix.length();
        int end = line.indexOf('"', start);
        return line.substring(start, end);
    }

    private static void assertLineage(
            List<String> lines,
            int startIndex,
            String interactionId,
            String firstStage,
            String secondStage) {
        assertEquals(interactionId, extractInteractionId(lines.get(startIndex)));
        assertEquals(interactionId, extractInteractionId(lines.get(startIndex + 1)));
        assertEquals(firstStage, extractStage(lines.get(startIndex)));
        assertEquals(secondStage, extractStage(lines.get(startIndex + 1)));
    }
}
