package dev.coffer.adapter.fabric.execution;

import dev.coffer.core.AuditRecord;
import dev.coffer.core.AuditSink;

import java.util.Objects;

public final class FabricAuditSink implements AuditSink {

    @Override
    public void emit(AuditRecord record) {
        Objects.requireNonNull(record, "record");

        // Phase 3B: minimal, non-durable observability
        System.out.println(
                "[Coffer][Audit] allowed=" + record.result().allowed() +
                " reason=" + record.result().denialReason()
        );
    }
}
