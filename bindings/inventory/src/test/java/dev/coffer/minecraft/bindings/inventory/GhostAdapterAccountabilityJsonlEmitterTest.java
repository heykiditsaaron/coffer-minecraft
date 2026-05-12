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
    private static final long TIMESTAMP = 1_700_000_000_000L;
    @TempDir
    Path tempDir;

    @Test
    void appendsOneJsonRecordPerLineInChronologicalOrder() throws IOException {
        Path target = GhostAdapterAccountabilityJsonlEmitter.cofferLogPath(tempDir.resolve("logs"), "ghost.jsonl");

        GhostAdapterAccountabilityJsonlEmitter.append(target, records("interaction-1", ProjectionKind.CORE_DENIED, "minecraft.value.not_removable"));
        GhostAdapterAccountabilityJsonlEmitter.append(target, records("interaction-2", ProjectionKind.RUNTIME_UNKNOWN, "MALFORMED_RUNTIME_DESCRIPTOR"));

        List<String> lines = Files.readAllLines(target);

        assertEquals(4, lines.size());
        assertTrue(lines.stream().allMatch(line -> line.startsWith("{\"timestamp\":")));
        assertTrue(lines.get(0).contains("\"interactionId\":\"interaction-1\""));
        assertTrue(lines.get(1).contains("\"interactionId\":\"interaction-1\""));
        assertTrue(lines.get(2).contains("\"interactionId\":\"interaction-2\""));
        assertTrue(lines.get(3).contains("\"interactionId\":\"interaction-2\""));
        assertEquals(jsonLine("interaction-1", "SER", "captured", null), lines.get(0));
        assertEquals(
                jsonLine("interaction-1", "CER", "core_denied", "minecraft.value.not_removable"),
                lines.get(1));
        assertEquals(
                jsonLine("interaction-2", "CER", "runtime_unknown", "MALFORMED_RUNTIME_DESCRIPTOR"),
                lines.get(3));
    }

    @Test
    void omissionSurvivesSerializationWithoutPlaceholderFields() throws IOException {
        Path target = GhostAdapterAccountabilityJsonlEmitter.cofferLogPath(tempDir.resolve("logs"), "ghost.jsonl");

        GhostAdapterAccountabilityJsonlEmitter.append(target, records("interaction-3", ProjectionKind.RUNTIME_SUCCESS, null));

        List<String> lines = Files.readAllLines(target);

        assertEquals(2, lines.size());
        assertTrue(lines.stream().allMatch(line -> line.startsWith("{\"timestamp\":")));
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
                TIMESTAMP,
                "interaction-4",
                projection(ProjectionKind.RUNTIME_FAILURE, "minecraft.value.not_removable"));
        List<String> lines = Files.readAllLines(target);

        assertEquals(records.size(), lines.size());
        assertTrue(lines.stream().allMatch(line -> line.startsWith("{\"timestamp\":")));
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
                        TIMESTAMP,
                        "interaction-13",
                        constructionRefusedProjection()));

        List<String> lines = Files.readAllLines(target);

        assertEquals(7, lines.size());
        assertTrue(lines.stream().allMatch(line -> line.startsWith("{\"timestamp\":")));
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
        assertTrue(lines.stream().allMatch(line -> line.startsWith("{\"timestamp\":")));
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
        assertTrue(lines.stream().allMatch(line -> line.startsWith("{\"timestamp\":")));
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
                GhostAdapterAccountabilityProjection.toJsonlRecords(TIMESTAMP, "interaction-40", constructionRefusedProjection()));
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
        assertTrue(lines.stream().allMatch(line -> line.startsWith("{\"timestamp\":")));
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

    @Test
    void rawLinesRemainLeftToRightReadableAcrossCurrentRecordTypes() throws IOException {
        Path target = GhostAdapterAccountabilityJsonlEmitter.cofferLogPath(tempDir.resolve("logs"), "ghost.jsonl");

        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                GhostAdapterAccountabilityProjection.toJsonlRecords(TIMESTAMP, "interaction-50", constructionRefusedProjection()));
        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                records("interaction-51", ProjectionKind.CORE_DENIED, "minecraft.value.not_removable"));
        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                records("interaction-52", ProjectionKind.RUNTIME_SUCCESS, null));
        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                records("interaction-53", ProjectionKind.RUNTIME_FAILURE, "minecraft.value.not_removable"));
        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                records("interaction-54", ProjectionKind.RUNTIME_UNKNOWN, "MALFORMED_RUNTIME_DESCRIPTOR"));

        List<String> lines = Files.readAllLines(target);

        assertTrue(lines.stream().allMatch(line -> line.startsWith("{\"timestamp\":")));
        assertIterableEquals(
                List.of(
                        jsonLine("interaction-50", "SER", "construction_refused", "MISSING_BINDING_ID"),
                        jsonLine("interaction-51", "SER", "captured", null),
                        jsonLine("interaction-51", "CER", "core_denied", "minecraft.value.not_removable"),
                        jsonLine("interaction-52", "SER", "captured", null),
                        jsonLine("interaction-52", "CER", "runtime_succeeded", null),
                        jsonLine("interaction-53", "SER", "captured", null),
                        jsonLine("interaction-53", "CER", "runtime_failed", "minecraft.value.not_removable"),
                        jsonLine("interaction-54", "SER", "captured", null),
                        jsonLine("interaction-54", "CER", "runtime_unknown", "MALFORMED_RUNTIME_DESCRIPTOR")),
                lines);
        assertTrue(lines.stream().allMatch(line -> hasOrderedFields(line, line.contains("\"code\""))));
    }

    @Test
    void codeAppearsOnlyWhenCurrentAccountabilityMeaningRequiresIt() throws IOException {
        Path target = GhostAdapterAccountabilityJsonlEmitter.cofferLogPath(tempDir.resolve("logs"), "ghost.jsonl");

        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                GhostAdapterAccountabilityProjection.toJsonlRecords(TIMESTAMP, "interaction-60", constructionRefusedProjection()));
        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                records("interaction-61", ProjectionKind.CORE_DENIED, "minecraft.value.not_removable"));
        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                records("interaction-62", ProjectionKind.RUNTIME_SUCCESS, null));
        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                records("interaction-63", ProjectionKind.RUNTIME_FAILURE, "minecraft.value.not_removable"));
        GhostAdapterAccountabilityJsonlEmitter.append(
                target,
                records("interaction-64", ProjectionKind.RUNTIME_UNKNOWN, "minecraft.container.unavailable"));

        List<String> lines = Files.readAllLines(target);

        assertTrue(lines.stream().allMatch(line -> line.startsWith("{\"timestamp\":")));
        assertTrue(lines.get(0).contains("\"code\":\"MISSING_BINDING_ID\""));
        assertFalse(lines.get(1).contains("\"code\""));
        assertTrue(lines.get(2).contains("\"code\":\"minecraft.value.not_removable\""));
        assertFalse(lines.get(3).contains("\"code\""));
        assertFalse(lines.get(4).contains("\"code\""));
        assertFalse(lines.get(5).contains("\"code\""));
        assertTrue(lines.get(6).contains("\"code\":\"minecraft.value.not_removable\""));
        assertFalse(lines.get(7).contains("\"code\""));
        assertTrue(lines.get(8).contains("\"code\":\"minecraft.container.unavailable\""));
        assertTrue(lines.stream().noneMatch(line -> line.contains(":null")));
        assertTrue(lines.stream().noneMatch(line -> line.contains("\"code\":\"\"")));
        assertTrue(lines.stream().noneMatch(line -> line.contains("\"explanation\"")));
    }

    private static List<Map<String, Object>> records(
            String interactionId,
            ProjectionKind kind,
            String reasonCode) {
        return GhostAdapterAccountabilityProjection.toJsonlRecords(
                TIMESTAMP,
                interactionId,
                projection(kind, reasonCode));
    }

    private static String jsonLine(String interactionId, String recordType, String stage, String code) {
        StringBuilder json = new StringBuilder();
        json.append("{\"timestamp\":").append(TIMESTAMP)
                .append(",\"interactionId\":\"").append(interactionId)
                .append("\",\"recordType\":\"").append(recordType)
                .append("\",\"stage\":\"").append(stage).append("\"");
        if (code != null) {
            json.append(",\"code\":\"").append(code).append("\"");
        }
        json.append('}');
        return json.toString();
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
        String timestampPrefix = "{\"timestamp\":";
        int timestampEnd = line.indexOf(',');
        if (line.indexOf(timestampPrefix) != 0 || timestampEnd < 0) {
            throw new IllegalStateException("Line does not start with a timestamp: " + line);
        }
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

    private static boolean hasOrderedFields(String line, boolean hasCode) {
        if (!line.startsWith("{\"timestamp\":")) {
            return false;
        }
        int timestampIndex = line.indexOf("\"timestamp\"");
        int interactionIndex = line.indexOf("\"interactionId\"");
        int recordTypeIndex = line.indexOf("\"recordType\"");
        int stageIndex = line.indexOf("\"stage\"");
        if (!(timestampIndex < interactionIndex && interactionIndex < recordTypeIndex && recordTypeIndex < stageIndex)) {
            return false;
        }
        if (!hasCode) {
            return line.indexOf("\"code\"") < 0;
        }
        int codeIndex = line.indexOf("\"code\"");
        return stageIndex < codeIndex;
    }
}
