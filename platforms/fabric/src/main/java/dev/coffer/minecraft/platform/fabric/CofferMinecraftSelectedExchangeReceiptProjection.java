package dev.coffer.minecraft.platform.fabric;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

final class CofferMinecraftSelectedExchangeReceiptProjection {
    Optional<ParticipantReceipt> participantReceipt(ReceiptSource source) {
        Objects.requireNonNull(source, "source");
        return receipt(source).map(summary -> new ParticipantReceipt(
                summary.status(),
                summary.temporary(),
                summary.actionability(),
                summary.code()));
    }

    Optional<AdminReceipt> adminReceipt(ReceiptSource source) {
        Objects.requireNonNull(source, "source");
        return receipt(source).map(summary -> new AdminReceipt(
                summary.status(),
                summary.temporary(),
                summary.actionability(),
                summary.latestStage(),
                summary.code(),
                summary.stages()));
    }

    private static Optional<ReceiptSummary> receipt(ReceiptSource source) {
        if (source instanceof ReceiptSource.TemporaryStateVisibility temporary) {
            return Optional.of(new ReceiptSummary(
                    temporary.status(),
                    true,
                    temporary.actionability(),
                    temporary.visibilityStage(),
                    temporary.code(),
                    List.of(temporary.visibilityStage())));
        }

        ReceiptSource.AccountabilityTrail trail = (ReceiptSource.AccountabilityTrail) source;
        if (trail.jsonlLines().isEmpty()) {
            return Optional.empty();
        }

        List<String> stages = new ArrayList<>();
        String latestStage = null;
        String latestCode = null;
        for (String line : trail.jsonlLines()) {
            String stage = extractField(line, "\"stage\":\"");
            stages.add(stage);
            latestStage = stage;
            latestCode = extractOptionalField(line, "\"code\":\"");
        }

        return Optional.of(summary(latestStage, latestCode, List.copyOf(stages)));
    }

    private static ReceiptSummary summary(String latestStage, String latestCode, List<String> stages) {
        return switch (latestStage) {
            case "fabric_core_denied" -> new ReceiptSummary(
                    "DENIED",
                    false,
                    "REVIEW_REQUIRED",
                    latestStage,
                    latestCode,
                    stages);
            case "fabric_core_approved" -> new ReceiptSummary(
                    "AUTHORIZED",
                    false,
                    "WAIT_RUNTIME",
                    latestStage,
                    latestCode,
                    stages);
            case "fabric_runtime_succeeded" -> new ReceiptSummary(
                    "COMPLETED",
                    false,
                    "NONE",
                    latestStage,
                    latestCode,
                    stages);
            case "fabric_runtime_failed" -> new ReceiptSummary(
                    "FAILED",
                    false,
                    "REVIEW_REQUIRED",
                    latestStage,
                    latestCode,
                    stages);
            case "fabric_runtime_unknown" -> new ReceiptSummary(
                    "UNKNOWN",
                    false,
                    "REVIEW_REQUIRED",
                    latestStage,
                    latestCode,
                    stages);
            case "fabric_construction_refused" -> new ReceiptSummary(
                    "STALE",
                    true,
                    "RECONFIRM_REQUIRED",
                    latestStage,
                    latestCode,
                    stages);
            case "fabric_server_stopped" -> new ReceiptSummary(
                    "INTERRUPTED",
                    true,
                    "REVIEW_REQUIRED",
                    latestStage,
                    latestCode,
                    stages);
            default -> throw new IllegalArgumentException("Unsupported accountability stage: " + latestStage);
        };
    }

    private static String extractField(String line, String prefix) {
        int start = line.indexOf(prefix);
        if (start < 0) {
            throw new IllegalArgumentException("Missing field prefix " + prefix + " in line: " + line);
        }
        int valueStart = start + prefix.length();
        int valueEnd = line.indexOf('"', valueStart);
        if (valueEnd < 0) {
            throw new IllegalArgumentException("Unterminated field for prefix " + prefix + " in line: " + line);
        }
        return line.substring(valueStart, valueEnd);
    }

    private static String extractOptionalField(String line, String prefix) {
        int start = line.indexOf(prefix);
        if (start < 0) {
            return null;
        }
        int valueStart = start + prefix.length();
        int valueEnd = line.indexOf('"', valueStart);
        if (valueEnd < 0) {
            throw new IllegalArgumentException("Unterminated field for prefix " + prefix + " in line: " + line);
        }
        return line.substring(valueStart, valueEnd);
    }

    sealed interface ReceiptSource permits ReceiptSource.AccountabilityTrail, ReceiptSource.TemporaryStateVisibility {
        record AccountabilityTrail(List<String> jsonlLines) implements ReceiptSource {
            public AccountabilityTrail {
                jsonlLines = List.copyOf(Objects.requireNonNull(jsonlLines, "jsonlLines"));
            }
        }

        record TemporaryStateVisibility(
                String status,
                String actionability,
                String visibilityStage,
                String code) implements ReceiptSource {
            public TemporaryStateVisibility {
                if (status == null || status.isBlank()) {
                    throw new IllegalArgumentException("status must not be null or blank");
                }
                if (actionability == null || actionability.isBlank()) {
                    throw new IllegalArgumentException("actionability must not be null or blank");
                }
                if (visibilityStage == null || visibilityStage.isBlank()) {
                    throw new IllegalArgumentException("visibilityStage must not be null or blank");
                }
            }
        }
    }

    record ParticipantReceipt(
            String status,
            boolean temporary,
            String actionability,
            String code) {
    }

    record AdminReceipt(
            String status,
            boolean temporary,
            String actionability,
            String latestStage,
            String code,
            List<String> stages) {
        public AdminReceipt {
            stages = List.copyOf(stages);
        }
    }

    private record ReceiptSummary(
            String status,
            boolean temporary,
            String actionability,
            String latestStage,
            String code,
            List<String> stages) {
    }
}
