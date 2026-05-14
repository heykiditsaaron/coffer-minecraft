package dev.coffer.minecraft.platform.fabric;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.function.LongSupplier;

final class CofferMinecraftLifecycleAccountability {
    private static final String FILE_NAME = "fabric-lifecycle.jsonl";
    private static final String FABRIC_CORE_SEAM = "fabric_core";
    private final Supplier<String> interactionIdFactory;
    private final LongSupplier timestampFactory;

    CofferMinecraftLifecycleAccountability(Supplier<String> interactionIdFactory) {
        this(interactionIdFactory, System::currentTimeMillis);
    }

    CofferMinecraftLifecycleAccountability(Supplier<String> interactionIdFactory, LongSupplier timestampFactory) {
        this.interactionIdFactory = Objects.requireNonNull(interactionIdFactory, "interactionIdFactory");
        this.timestampFactory = Objects.requireNonNull(timestampFactory, "timestampFactory");
    }

    static CofferMinecraftLifecycleAccountability create() {
        return new CofferMinecraftLifecycleAccountability(
                () -> "fabric-lifecycle-" + UUID.randomUUID(),
                System::currentTimeMillis);
    }

    void recordServerStarted(Path runDirectory) {
        append(runDirectory, "SER", "fabric_server_started", null, null);
    }

    void recordServerStopped(Path runDirectory) {
        append(runDirectory, "SER", "fabric_server_stopped", null, null);
    }

    void recordConstructionRefused(Path runDirectory, String code) {
        append(runDirectory, "SER", "fabric_construction_refused", null, code);
    }

    void recordCoreDenied(Path runDirectory, String code) {
        append(runDirectory, "CER", "fabric_core_denied", FABRIC_CORE_SEAM, code);
    }

    void recordCoreApproved(Path runDirectory) {
        append(runDirectory, "CER", "fabric_core_approved", FABRIC_CORE_SEAM, null);
    }

    Path logPath(Path runDirectory) {
        Objects.requireNonNull(runDirectory, "runDirectory");
        return runDirectory.resolve("logs").resolve("coffer").resolve(FILE_NAME);
    }

    String toJsonLine(String interactionId, String stage) {
        return toJsonLine(timestampFactory.getAsLong(), interactionId, "SER", stage, null, null);
    }

    String toJsonLine(String interactionId, String stage, String code) {
        return toJsonLine(timestampFactory.getAsLong(), interactionId, "SER", stage, null, code);
    }

    String toJsonLine(long timestamp, String interactionId, String stage) {
        return toJsonLine(timestamp, interactionId, "SER", stage, null, null);
    }

    String toJsonLine(long timestamp, String interactionId, String stage, String code) {
        return toJsonLine(timestamp, interactionId, "SER", stage, null, code);
    }

    String toJsonLine(String interactionId, String recordType, String stage, String code) {
        return toJsonLine(timestampFactory.getAsLong(), interactionId, recordType, stage, null, code);
    }

    String toJsonLine(long timestamp, String interactionId, String recordType, String stage, String code) {
        return toJsonLine(timestamp, interactionId, recordType, stage, null, code);
    }

    String toJsonLine(long timestamp, String interactionId, String recordType, String stage, String seam, String code) {
        Objects.requireNonNull(interactionId, "interactionId");
        Objects.requireNonNull(recordType, "recordType");
        Objects.requireNonNull(stage, "stage");
        StringBuilder json = new StringBuilder();
        json.append("{\"timestamp\":")
                .append(timestamp)
                .append(",\"interactionId\":\"")
                .append(escape(interactionId))
                .append("\",\"recordType\":\"")
                .append(escape(recordType))
                .append("\",\"stage\":\"")
                .append(escape(stage))
                .append("\"");
        if (seam != null) {
            json.append(",\"seam\":\"").append(escape(seam)).append("\"");
        }
        if (code != null) {
            json.append(",\"code\":\"").append(escape(code)).append("\"");
        }
        json.append('}');
        return json.toString();
    }

    private void append(Path runDirectory, String recordType, String stage, String seam, String code) {
        Path target = logPath(runDirectory);
        try {
            Files.createDirectories(Objects.requireNonNull(target.getParent(), "target parent"));
            Files.writeString(
                    target,
                    toJsonLine(timestampFactory.getAsLong(), interactionIdFactory.get(), recordType, stage, seam, code)
                            + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
