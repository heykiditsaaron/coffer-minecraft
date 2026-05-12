package dev.coffer.minecraft.testsupport.ghostadapter;

import dev.coffer.minecraft.testsupport.ghostadapter.GhostAdapterExchangeHarness.GhostAdapterProjection;
import dev.coffer.minecraft.testsupport.ghostadapter.GhostAdapterExchangeHarness.ProjectionKind;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Test-only, intentionally minimal JSONL-friendly accountability projection.
 *
 * <p>This is not a final SER/CER schema. It exists only to pressure-test which
 * accountability records emerge from current ghost-adapter participation depth.
 */
public final class GhostAdapterAccountabilityProjection {
    private GhostAdapterAccountabilityProjection() {
    }

    public static List<Map<String, Object>> toJsonlRecords(
            String interactionId,
            GhostAdapterProjection projection) {
        return toJsonlRecords(System.currentTimeMillis(), interactionId, projection);
    }

    public static List<Map<String, Object>> toJsonlRecords(
            long timestamp,
            String interactionId,
            GhostAdapterProjection projection) {
        Objects.requireNonNull(interactionId, "interactionId");
        Objects.requireNonNull(projection, "projection");

        if (projection.kind() == ProjectionKind.CONSTRUCTION_REFUSED) {
            return List.of(record(
                    timestamp,
                    interactionId,
                    "SER",
                    "construction_refused",
                    projection.refusal().reason().name()));
        }

        Map<String, Object> ser = record(timestamp, interactionId, "SER", "captured", null);
        if (projection.kind() == ProjectionKind.CORE_DENIED) {
            return List.of(ser, record(timestamp, interactionId, "CER", "core_denied", projection.reasonCode()));
        }
        if (projection.kind() == ProjectionKind.RUNTIME_SUCCESS) {
            return List.of(ser, record(timestamp, interactionId, "CER", "runtime_succeeded", null));
        }
        if (projection.kind() == ProjectionKind.RUNTIME_FAILURE) {
            return List.of(ser, record(timestamp, interactionId, "CER", "runtime_failed", projection.reasonCode()));
        }
        return List.of(ser, record(timestamp, interactionId, "CER", "runtime_unknown", projection.reasonCode()));
    }

    private static Map<String, Object> record(
            long timestamp,
            String interactionId,
            String recordType,
            String stage,
            String code) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("timestamp", timestamp);
        record.put("interactionId", interactionId);
        record.put("recordType", recordType);
        record.put("stage", stage);
        if (code != null) {
            record.put("code", code);
        }
        return Map.copyOf(record);
    }
}
