package dev.coffer.minecraft.platform.fabric;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

final class CofferMinecraftAdminShopPresetListingReceiptProjection {
    Optional<ParticipantReceipt> participantReceipt(ReceiptSource source) {
        Objects.requireNonNull(source, "source");
        return receipt(source).map(summary -> new ParticipantReceipt(
                summary.status(),
                summary.temporary(),
                summary.actionability(),
                summary.code(),
                summary.listingContext().map(ListingContext::supplyMode).orElse(null)));
    }

    Optional<AdminReceipt> adminReceipt(ReceiptSource source) {
        Objects.requireNonNull(source, "source");
        return receipt(source).map(summary -> new AdminReceipt(
                summary.status(),
                summary.temporary(),
                summary.actionability(),
                summary.latestStage(),
                summary.code(),
                summary.stages(),
                summary.listingContext().map(ListingContext::listingId).orElse(null),
                summary.listingContext().map(ListingContext::supplyMode).orElse(null)));
    }

    private static Optional<ReceiptSummary> receipt(ReceiptSource source) {
        if (source instanceof ReceiptSource.TemporaryStateVisibility temporary) {
            return Optional.of(new ReceiptSummary(
                    temporary.status(),
                    true,
                    temporary.actionability(),
                    temporary.visibilityStage(),
                    temporary.code(),
                    List.of(temporary.visibilityStage()),
                    temporary.listingContext()));
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

        return Optional.of(summary(latestStage, latestCode, List.copyOf(stages), trail.listingContext()));
    }

    private static ReceiptSummary summary(
            String latestStage,
            String latestCode,
            List<String> stages,
            Optional<ListingContext> listingContext) {
        return switch (latestStage) {
            case "fabric_core_denied" -> new ReceiptSummary(
                    "DENIED",
                    false,
                    "REVIEW_REQUIRED",
                    latestStage,
                    latestCode,
                    stages,
                    listingContext);
            case "fabric_core_approved" -> new ReceiptSummary(
                    "AUTHORIZED",
                    false,
                    "WAIT_RUNTIME",
                    latestStage,
                    latestCode,
                    stages,
                    listingContext);
            case "fabric_runtime_succeeded" -> new ReceiptSummary(
                    "COMPLETED",
                    false,
                    "NONE",
                    latestStage,
                    latestCode,
                    stages,
                    listingContext);
            case "fabric_runtime_failed" -> new ReceiptSummary(
                    "FAILED",
                    false,
                    "REVIEW_REQUIRED",
                    latestStage,
                    latestCode,
                    stages,
                    listingContext);
            case "fabric_runtime_unknown" -> new ReceiptSummary(
                    "UNKNOWN",
                    false,
                    "REVIEW_REQUIRED",
                    latestStage,
                    latestCode,
                    stages,
                    listingContext);
            case "fabric_construction_refused" -> new ReceiptSummary(
                    "STALE",
                    true,
                    "RECONFIRM_REQUIRED",
                    latestStage,
                    latestCode,
                    stages,
                    listingContext);
            case "fabric_server_stopped" -> new ReceiptSummary(
                    "INTERRUPTED",
                    true,
                    "REVIEW_REQUIRED",
                    latestStage,
                    latestCode,
                    stages,
                    listingContext);
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
        record AccountabilityTrail(
                List<String> jsonlLines,
                Optional<ListingContext> listingContext) implements ReceiptSource {
            public AccountabilityTrail {
                jsonlLines = List.copyOf(Objects.requireNonNull(jsonlLines, "jsonlLines"));
                listingContext = Objects.requireNonNull(listingContext, "listingContext");
            }
        }

        record TemporaryStateVisibility(
                String status,
                String actionability,
                String visibilityStage,
                String code,
                Optional<ListingContext> listingContext) implements ReceiptSource {
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
                listingContext = Objects.requireNonNull(listingContext, "listingContext");
            }
        }
    }

    record ListingContext(
            String listingId,
            String supplyMode) {
        ListingContext {
            if (listingId == null || listingId.isBlank()) {
                throw new IllegalArgumentException("listingId must not be null or blank");
            }
            if (supplyMode == null || supplyMode.isBlank()) {
                throw new IllegalArgumentException("supplyMode must not be null or blank");
            }
        }
    }

    record ParticipantReceipt(
            String status,
            boolean temporary,
            String actionability,
            String code,
            String supplyMode) {
    }

    record AdminReceipt(
            String status,
            boolean temporary,
            String actionability,
            String latestStage,
            String code,
            List<String> stages,
            String listingId,
            String supplyMode) {
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
            List<String> stages,
            Optional<ListingContext> listingContext) {
    }
}
