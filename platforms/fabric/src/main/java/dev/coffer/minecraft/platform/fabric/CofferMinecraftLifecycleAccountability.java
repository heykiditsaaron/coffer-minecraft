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

final class CofferMinecraftLifecycleAccountability {
    private static final String FILE_NAME = "fabric-lifecycle.jsonl";
    private final Supplier<String> interactionIdFactory;

    CofferMinecraftLifecycleAccountability(Supplier<String> interactionIdFactory) {
        this.interactionIdFactory = Objects.requireNonNull(interactionIdFactory, "interactionIdFactory");
    }

    static CofferMinecraftLifecycleAccountability create() {
        return new CofferMinecraftLifecycleAccountability(() -> "fabric-lifecycle-" + UUID.randomUUID());
    }

    void recordServerStarted(Path runDirectory) {
        append(runDirectory, "fabric_server_started");
    }

    void recordServerStopped(Path runDirectory) {
        append(runDirectory, "fabric_server_stopped");
    }

    Path logPath(Path runDirectory) {
        Objects.requireNonNull(runDirectory, "runDirectory");
        return runDirectory.resolve("logs").resolve("coffer").resolve(FILE_NAME);
    }

    String toJsonLine(String interactionId, String stage) {
        Objects.requireNonNull(interactionId, "interactionId");
        Objects.requireNonNull(stage, "stage");
        return "{\"interactionId\":\""
                + escape(interactionId)
                + "\",\"recordType\":\"SER\",\"stage\":\""
                + escape(stage)
                + "\"}";
    }

    private void append(Path runDirectory, String stage) {
        Path target = logPath(runDirectory);
        try {
            Files.createDirectories(Objects.requireNonNull(target.getParent(), "target parent"));
            Files.writeString(
                    target,
                    toJsonLine(interactionIdFactory.get(), stage) + System.lineSeparator(),
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
