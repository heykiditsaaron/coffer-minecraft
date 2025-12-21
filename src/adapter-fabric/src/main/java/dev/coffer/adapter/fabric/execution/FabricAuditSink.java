package dev.coffer.adapter.fabric.execution;

import dev.coffer.core.AuditRecord;
import dev.coffer.core.AuditSink;

import java.util.Objects;

/**
 * FABRIC AUDIT SINK
 *
 * Responsibility:
 * - Minimal adapter-owned destination for audit records (stdout for now).
 */
public final class FabricAuditSink implements AuditSink {

    @Override
    public void emit(AuditRecord record) {
        Objects.requireNonNull(record, "record");

        System.out.println(
                "[Coffer][Audit] allowed=" + record.result().allowed() +
                " reason=" + record.result().denialReason()
        );
    }
}
