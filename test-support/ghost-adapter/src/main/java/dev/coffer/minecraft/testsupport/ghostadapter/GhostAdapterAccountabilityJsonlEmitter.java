package dev.coffer.minecraft.testsupport.ghostadapter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Test-only append-only JSONL emitter for the current minimal accountability
 * projection shape.
 *
 * <p>This is not production logging infrastructure.
 */
public final class GhostAdapterAccountabilityJsonlEmitter {
    private static final List<String> FIELD_ORDER = List.of("timestamp", "interactionId", "recordType", "stage", "code");
    private static final Set<String> ALLOWED_FIELDS = Set.copyOf(FIELD_ORDER);

    private GhostAdapterAccountabilityJsonlEmitter() {
    }

    public static Path cofferLogPath(Path platformLogsDirectory, String fileName) {
        Objects.requireNonNull(platformLogsDirectory, "platformLogsDirectory");
        Objects.requireNonNull(fileName, "fileName");
        return platformLogsDirectory.resolve("coffer").resolve(fileName);
    }

    public static void append(Path target, List<Map<String, Object>> records) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(records, "records");

        try {
            Files.createDirectories(Objects.requireNonNull(target.getParent(), "target parent"));
            for (Map<String, Object> record : records) {
                validate(record);
                Files.writeString(
                        target,
                        toJson(record) + System.lineSeparator(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    static String toJson(Map<String, Object> record) {
        validate(record);

        StringBuilder json = new StringBuilder();
        json.append('{');
        boolean first = true;
        for (String field : FIELD_ORDER) {
            if (!record.containsKey(field)) {
                continue;
            }
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append('"').append(field).append('"').append(':');
            Object value = record.get(field);
            if ("timestamp".equals(field)) {
                json.append(((Number) value).longValue());
            } else {
                json.append('"').append(escape((String) value)).append('"');
            }
        }
        json.append('}');
        return json.toString();
    }

    private static void validate(Map<String, Object> record) {
        Set<String> unexpectedFields = new LinkedHashSet<>(record.keySet());
        unexpectedFields.removeAll(ALLOWED_FIELDS);
        if (!unexpectedFields.isEmpty()) {
            throw new IllegalArgumentException("Unsupported accountability fields: " + unexpectedFields);
        }
        if (!record.containsKey("timestamp")) {
            throw new IllegalArgumentException("Missing required accountability field: timestamp");
        }
        for (String field : FIELD_ORDER) {
            Object value = record.get(field);
            if (value == null) {
                continue;
            }
            if ("timestamp".equals(field)) {
                if (!(value instanceof Number)) {
                    throw new IllegalArgumentException("Field timestamp must be numeric");
                }
                continue;
            }
            if (!(value instanceof String)) {
                throw new IllegalArgumentException("Field " + field + " must be a string");
            }
        }
    }

    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
