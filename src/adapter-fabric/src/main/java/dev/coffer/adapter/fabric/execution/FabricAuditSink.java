package dev.coffer.adapter.fabric.execution;

import dev.coffer.core.AuditRecord;
import dev.coffer.core.AuditSink;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/**
 * FABRIC AUDIT SINK
 *
 * Responsibility:
 * - Adapter-owned destination for audit records.
 * - Stores a rolling buffer for diagnostic commands.
 * - Emits to stdout for minimal visibility.
 *
 * Invariants:
 * - Keeps at most maxRecords recent entries.
 */
public final class FabricAuditSink implements AuditSink {

    private static final int DEFAULT_MAX_RECORDS = 50;

    private final int maxRecords;
    private final Path logFile;
    private final boolean consoleEnabled;
    private final Deque<String> recent = new ArrayDeque<>();

    public FabricAuditSink() {
        this(DEFAULT_MAX_RECORDS, null, true);
    }

    public FabricAuditSink(int maxRecords, Path logFile, boolean consoleEnabled) {
        if (maxRecords <= 0) {
            throw new IllegalArgumentException("maxRecords must be > 0");
        }
        this.maxRecords = maxRecords;
        this.logFile = logFile;
        this.consoleEnabled = consoleEnabled;
    }

    @Override
    public synchronized void emit(AuditRecord record) {
        Objects.requireNonNull(record, "record");

        DateTimeFormatter fmt = DateTimeFormatter.ISO_INSTANT;
        String line = fmt.format(record.timestamp()) +
                " allowed=" + record.result().allowed() +
                " reason=" + record.result().denialReason();

        append(line);
        emitOut(line);
    }

    public synchronized void emitAdmin(String code, String message) {
        String line = code + " " + message;
        append(line);
        emitOut(line);
    }

    public synchronized List<String> describeRecent(int limit) {
        int toTake = Math.max(0, Math.min(limit, recent.size()));
        List<String> lines = new ArrayList<>(toTake);
        recent.descendingIterator().forEachRemaining(r -> {
            if (lines.size() >= toTake) return;
            lines.add(r);
        });
        return lines;
    }

    private void append(String line) {
        if (recent.size() >= maxRecords) {
            recent.removeFirst();
        }
        recent.addLast(line);
    }

    private void emitOut(String line) {
        if (consoleEnabled) {
            System.out.println("[Coffer][Audit] " + line);
        }
        if (logFile != null) {
            try {
                if (logFile.getParent() != null) {
                    Files.createDirectories(logFile.getParent());
                }
                Files.writeString(logFile,
                        "[Coffer][Audit] " + line + System.lineSeparator(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
            } catch (IOException ignored) {
                // avoid log storms from logging failures
            }
        }
    }
}
